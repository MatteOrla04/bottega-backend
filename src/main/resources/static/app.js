let utenteAttuale = null; 
let carrello = [];

// --- OROLOGIO E TEMA ---
function updateClock(){
    const now = new Date(); 
    const timeString = now.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' }); 
    document.getElementById('clock').textContent = timeString; 
}
setInterval(updateClock, 1000); 
updateClock();

const themeToggle = document.getElementById('theme-toggle');
const body = document.body;

document.addEventListener("DOMContentLoaded", () => {
    const temaSalvato = localStorage.getItem('tema_bottega');
    if (temaSalvato === 'dark') {
        body.classList.add('dark');
        if (themeToggle) themeToggle.textContent = "Light Mode";
    } else {
        body.classList.remove('dark');
        if (themeToggle) themeToggle.textContent = "Dark Mode";
    }
});

if (themeToggle) {
    // Gestione corretta dello switch visivo
    themeToggle.addEventListener('click', (evento) => {
        evento.preventDefault();
        body.classList.toggle('dark');
        
        if (body.classList.contains('dark')) {
            themeToggle.textContent = "Light Mode";
            localStorage.setItem('tema_bottega', 'dark'); 
        } else {
            themeToggle.textContent = "Dark Mode";
            localStorage.setItem('tema_bottega', 'light'); 
        }
    });
}

// --- NAVIGAZIONE SCHERMATE ---
const btnAvviaSpesa = document.querySelector('#card-spesa .action-btn'); 
const dashboard = document.getElementById('dashboard');
const posSystem = document.getElementById('pos-system');
const logo = document.querySelector('.logo');
const scannerInput = document.getElementById('scanner-input');

logo.style.cursor = 'pointer';

btnAvviaSpesa.addEventListener('click', () => {
    dashboard.classList.add('hidden');
    posSystem.classList.remove('hidden');
    scannerInput.focus();
});

logo.addEventListener('click', () => {
    posSystem.classList.add('hidden');
    dashboard.classList.remove('hidden');
});


// --- MOTORE CENTRALE CASSA (SCANNER) ---
const manualInput = document.getElementById('manual-input');
const btnManualSearch = document.getElementById('btn-manual-search');

function processaScansione(codiceLetto) {
    if (!codiceLetto || codiceLetto.trim() === "") return;
    codiceLetto = codiceLetto.trim().toUpperCase(); 

    if (codiceLetto.startsWith("TESS-")) { 
        fetch('/api/cassa/beneficiario?tessera=' + codiceLetto)
            .then(response => response.text()) 
            .then(testo => {
                if (!testo) {
                    alert("Tessera inesistente nel database!");
                    return;
                }
                const cliente = JSON.parse(testo); 
                utenteAttuale = cliente;
                
                document.getElementById('user-name').textContent = cliente.nome + " " + cliente.cognome;
                document.getElementById('user-budget').textContent = cliente.saldoPunti;
                document.getElementById('user-info').classList.remove('hidden');
                document.getElementById('status-message').textContent = "UTENTE RICONOSCIUTO - SCANSIONA PRODOTTI";
                
                document.getElementById('pos-header').classList.remove('state-0');
                document.getElementById('pos-header').style.backgroundColor = 'var(--primary-color, #007aff)';
                document.getElementById('pos-header').style.color = 'white';
                
                const btnDocPos = document.getElementById('btn-vedi-documento-pos');
                if (btnDocPos) {
                    btnDocPos.style.display = (cliente.documentoBase64 && cliente.documentoBase64.trim() !== "") ? 'inline-block' : 'none';
                }

                aggiornaSchermoCassa();
            })
            .catch(errore => console.error("Errore di rete:", errore));
    } else {
        fetch('/api/cassa/prodotto?barre=' + codiceLetto)
            .then(response => response.text())
            .then(testo => {
                if (!testo) {
                    alert("Prodotto non trovato in magazzino!");
                    return;
                }
                const prodotto = JSON.parse(testo);
                aggiungiAlCarrello(prodotto);
            })
            .catch(errore => console.error("Errore di rete:", errore));
    }
}

scannerInput.addEventListener('keypress', function(evento) {
    if (evento.key === 'Enter') {
        processaScansione(scannerInput.value); 
        scannerInput.value = ''; 
    }
});

btnManualSearch.addEventListener('click', () => {
    processaScansione(manualInput.value); 
    manualInput.value = ''; 
    scannerInput.focus();
});

manualInput.addEventListener('keypress', function(evento) {
    if (evento.key === 'Enter') btnManualSearch.click(); 
});


function aggiungiAlCarrello(prodotto) {
    let costoAttuale = 0;
    carrello.forEach(item => costoAttuale += (item.costoUnitario * item.quantita));
    let nuovoTotale = costoAttuale + prodotto.puntiCosto;
    
    if (utenteAttuale && (utenteAttuale.saldoPunti - nuovoTotale < 0)) {
        alert("ATTENZIONE: Credito insufficiente!");
        return;
    }

    // ECCO LA CORREZIONE: Ora cerca solo per Codice e Descrizione, senza confondersi coi campi vuoti!
    let voceEsistente = carrello.find(item => item.codiceBarre === prodotto.codiceBarre && item.descrizione === prodotto.descrizione);
    
    if (voceEsistente) {
        voceEsistente.quantita++; // Aumenta la quantità a x2, x3, ecc.
    } else {
        carrello.push({
            codiceBarre: prodotto.codiceBarre,
            descrizione: prodotto.descrizione,
            costoUnitario: prodotto.puntiCosto,
            quantita: 1,
            note: prodotto.note || null,
            prezzoCustom: prodotto.prezzoCustom || null 
        });
    }
    aggiornaSchermoCassa();
}
// Funzione per caricare la lista dei dipendenti
function caricaDipendenti() {
    fetch('/api/admin/operatori')
        .then(response => response.json())
        .then(operatori => {
            const tbody = document.getElementById('tabella-operatori');
            tbody.innerHTML = '';
            
            operatori.forEach(op => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${op.id}</td>
                    <td>${op.username}</td>
                    <td>${op.ruolo || 'Operatore'}</td>
                    <td>
                        <button class="btn btn-danger btn-sm" onclick="eliminaDipendente(${op.id})">Elimina</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error('Errore nel caricamento dei dipendenti:', err));
}

function eliminaDipendente(id) {
    if (confirm("Sei sicuro di voler eliminare questo dipendente?")) {
        fetch(`/api/admin/operatori/${id}`, { method: 'DELETE' })
            .then(response => {
                if (response.ok) {
                    alert("Dipendente eliminato con successo!");
                    caricaDipendenti();
                } else {
                    alert("Errore durante l'eliminazione.");
                }
            });
    }
}


// Chiama questa funzione quando apri la dashboard admin
// caricaDipendenti();
function aggiungiProdottoLibero() {
    let descrizione = prompt("Descrizione prodotto (Es. 1kg Zucchine):");
    if (!descrizione || descrizione.trim() === "") return;
    let punti = prompt("Costo in Punti (Es. 4):");
    if (!punti || isNaN(punti) || parseInt(punti) < 0) return;

    let prodottoJolly = {
        codiceBarre: "VARIE",
        descrizione: "VARIE - " + descrizione.trim(),
        puntiCosto: parseInt(punti),
        note: descrizione.trim(),
        prezzoCustom: parseInt(punti)
    };
    aggiungiAlCarrello(prodottoJolly);
}

function aggiornaSchermoCassa() {
    const tbody = document.getElementById('cart-items');
    tbody.innerHTML = ''; 
    let totaleSpesa = 0;
    
    carrello.forEach((item, index) => {
        let costoRiga = item.costoUnitario * item.quantita;
        totaleSpesa += costoRiga;
        let tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.descrizione}</td>
            <td>${item.costoUnitario}</td>
            <td><strong>${item.quantita}</strong></td>
            <td><button onclick="rimuoviDalCarrello(${index})" class="btn-red" style="padding: 0.3rem 0.6rem;">X</button></td>
        `;
        tbody.appendChild(tr);
    });
    
    document.getElementById('total-cost').textContent = totaleSpesa;
    
    const btnConfirm = document.getElementById('btn-confirm');
    const btnCancel = document.getElementById('btn-cancel');
    btnCancel.disabled = (!utenteAttuale && carrello.length === 0);
    
    let bloccaConferma = (!utenteAttuale || carrello.length === 0);
    if (utenteAttuale) {
        let saldoResiduo = utenteAttuale.saldoPunti - totaleSpesa;
        document.getElementById('remaining-balance').textContent = saldoResiduo;
        if (saldoResiduo < 0) {
            document.getElementById('remaining-balance').style.color = '#dc3545'; 
            bloccaConferma = true; 
        } else {
            document.getElementById('remaining-balance').style.color = 'var(--green-accent)'; 
        }
    }
    btnConfirm.disabled = bloccaConferma;
}


function rimuoviDalCarrello(index) {
    if (carrello[index].quantita > 1) carrello[index].quantita--;
    else carrello.splice(index, 1);
    aggiornaSchermoCassa();
    document.getElementById('scanner-input').focus();
}

document.getElementById('btn-confirm').addEventListener('click', () => {
    if (!utenteAttuale || carrello.length === 0) return;
    document.getElementById('btn-confirm').disabled = true;

    const scontrino = {
        codiceTessera: utenteAttuale.codiceTessera, 
        elementi: carrello.map(item => ({
            codiceBarre: item.codiceBarre,
            quantita: item.quantita,
            note: item.note,               
            prezzoCustom: item.prezzoCustom 
        }))
    };

    fetch('/api/cassa/acquisto', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(scontrino) 
    })
    .then(response => {
        if (!response.ok) throw new Error("Errore dal server: " + response.status); 
        return response.text(); 
    })
    .then(messaggio => {
        generaEStampaScontrino();
        alert("Transazione completata! Il database è aggiornato.");
        resetCassa(); 
    })
    .catch(errore => {
        alert("Errore durante l'invio! Riprova.");
        document.getElementById('btn-confirm').disabled = false; 
    });
});

document.getElementById('btn-cancel').addEventListener('click', () => {
    if (confirm("Sicuro di annullare?")) resetCassa();
});

function resetCassa() {
    utenteAttuale = null;
    carrello = [];
    aggiornaSchermoCassa();
    document.getElementById('user-info').classList.add('hidden');
    document.getElementById('status-message').textContent = "IN ATTESA - SCANSIONA TESSERA";
    document.getElementById('pos-header').style.backgroundColor = '';
    document.getElementById('pos-header').style.color = '';
    document.getElementById('remaining-balance').textContent = '0';
    document.getElementById('remaining-balance').style.color = ''; 
    
    const btnDocPos = document.getElementById('btn-vedi-documento-pos');
    if (btnDocPos) btnDocPos.style.display = 'none';
    
    document.getElementById('scanner-input').focus();
}

function generaEStampaScontrino() {
    document.getElementById('stampa-data').textContent = new Date().toLocaleString('it-IT');
    document.getElementById('stampa-tessera').textContent = utenteAttuale.codiceTessera;
    document.getElementById('stampa-nome').textContent = utenteAttuale.nome + " " + utenteAttuale.cognome;
    const tbodyStampa = document.getElementById('stampa-prodotti');
    tbodyStampa.innerHTML = ''; 
    let totaleSpesa = 0;
    carrello.forEach(item => {
        let costoRiga = item.costoUnitario * item.quantita;
        totaleSpesa += costoRiga;
        let tr = document.createElement('tr');
        tr.innerHTML = `<td>${item.quantita}x</td><td>${item.descrizione}</td><td style="text-align: right;">${costoRiga}</td>`;
        tbodyStampa.appendChild(tr);
    });
    document.getElementById('stampa-totale').textContent = totaleSpesa;
    document.getElementById('stampa-residuo').textContent = (utenteAttuale.saldoPunti - totaleSpesa);
    window.print();
}

// --- MODALI ---
function apriModale(idModale) { document.getElementById(idModale).style.display = 'block'; }
function chiudiModale(idModale) { document.getElementById(idModale).style.display = 'none'; }


// --- REGISTRAZIONE NUOVO UTENTE E CALCOLO PUNTI ---
const inputNucleo = document.getElementById('nuovo-nucleo');
const inputPunti = document.getElementById('nuovo-punti-previsti');

function aggiornaPuntiVisivi() {
    if (!inputNucleo || !inputPunti) return;
    let nucleo = parseInt(inputNucleo.value);
    
    if (isNaN(nucleo) || nucleo <= 1) {
        inputPunti.value = 10;
    } else {
        inputPunti.value = 20 + ((nucleo - 2) * 5);
    }
}

if (inputNucleo) {
    inputNucleo.addEventListener('input', aggiornaPuntiVisivi);
    inputNucleo.addEventListener('change', aggiornaPuntiVisivi);
    inputNucleo.addEventListener('keyup', aggiornaPuntiVisivi);
}

const formIscrizione = document.getElementById('form-nuova-iscrizione');
const fileInput = document.getElementById('nuovo-documento');
const previewContainer = document.getElementById('preview-container');
const btnRimuoviFotoReg = document.getElementById('btn-rimuovi-foto-reg');

function cancellaAnteprima() {
    if (fileInput) fileInput.value = ""; 
    if (previewContainer) previewContainer.innerHTML = ""; 
    if (btnRimuoviFotoReg) btnRimuoviFotoReg.style.display = 'none';
}
if (btnRimuoviFotoReg) btnRimuoviFotoReg.addEventListener('click', cancellaAnteprima);

if (fileInput) {
    fileInput.addEventListener('change', function() {
        previewContainer.innerHTML = ""; 
        if (this.files.length > 0) {
            btnRimuoviFotoReg.style.display = 'inline-block';
            Array.from(this.files).forEach(file => {
                const reader = new FileReader();
                reader.onload = function(e) {
                    let img = document.createElement('img');
                    img.src = e.target.result;
                    img.style = "max-height: 100px; border-radius: 5px; border: 1px dashed #ccc;";
                    previewContainer.appendChild(img);
                }
                reader.readAsDataURL(file);
            });
        } else {
            cancellaAnteprima();
        }
    });
}

function convertiInBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
        reader.readAsDataURL(file);
    });
}

function prendiValoreHtml(idNome) {
    let elemento = document.getElementById(idNome);
    return elemento ? elemento.value : "";
}

if (formIscrizione) {
    formIscrizione.addEventListener('submit', async function(evento) {
        evento.preventDefault(); 
        
        if (!formIscrizione.checkValidity()) {
            formIscrizione.reportValidity();
            return;
        }
        
        try {
            let base64Array = [];
            if (fileInput.files.length > 0) {
                for (let i = 0; i < fileInput.files.length; i++) {
                    let stringaB64 = await convertiInBase64(fileInput.files[i]);
                    base64Array.push(stringaB64);
                }
            }
            let pacchettoImmagini = base64Array.length > 0 ? JSON.stringify(base64Array) : null;
            
            const nuovoUtente = {
                nome: prendiValoreHtml('nuovo-nome'),
                cognome: prendiValoreHtml('nuovo-cognome'),
                codiceTessera: prendiValoreHtml('nuova-tessera').toUpperCase(),
                telefono: prendiValoreHtml('nuovo-telefono'),
                citta: prendiValoreHtml('nuova-citta'),
                provincia: prendiValoreHtml('nuova-provincia'),
                indirizzo_abitazione: prendiValoreHtml('nuovo-indirizzo'),
                numero_civico: prendiValoreHtml('nuovo-civico'),
                cittadinanza: prendiValoreHtml('nuova-cittadinanza'),
                numeroNucleoFamiliare: parseInt(prendiValoreHtml('nuovo-nucleo')) || 1,
                valoreIsee: prendiValoreHtml('nuovo-isee'),
                documentoBase64: pacchettoImmagini
            };

            fetch('/api/cassa/beneficiario', { 
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(nuovoUtente)
            })
            .then(async response => {
                const testoRisposta = await response.text();
                if (!response.ok) throw new Error(testoRisposta);
                
                alert("✅ " + testoRisposta);
                formIscrizione.reset();
                if (document.getElementById('nuovo-punti-previsti')) {
                    document.getElementById('nuovo-punti-previsti').value = 10; 
                }
                cancellaAnteprima();
                chiudiModale('modal-nuova-iscrizione');
            })
            .catch(errore => alert("❌ Errore dal server: " + errore.message));

        } catch (erroreJS) {
            alert("⚠️ Errore interno Javascript: " + erroreJS);
        }
    });
}


// --- ANAGRAFICA E DETTAGLI ---
let utenteInDettaglio = null;

function apriDettagliUtente(tessera) {
    fetch('/api/cassa/beneficiario?tessera=' + tessera)
        .then(response => response.json())
        .then(cliente => {
            utenteInDettaglio = cliente;
            
            document.getElementById('dettaglio-nome').textContent = cliente.nome + " " + cliente.cognome;
            document.getElementById('dettaglio-tessera').textContent = cliente.codiceTessera;
            document.getElementById('dettaglio-saldo').textContent = cliente.saldoPunti;
            
            const areaDoc = document.getElementById('area-documento');
            
            if (cliente.documentoBase64 && cliente.documentoBase64.trim() !== "") {
                areaDoc.style.display = 'block'; 
            } else {
                areaDoc.style.display = 'none';
            }
            
            apriModale('modal-dettagli-utente');
        })
        .catch(errore => alert("Errore durante il caricamento dei dettagli."));
}

document.getElementById('btn-esegui-ricarica').addEventListener('click', () => {
    if (!utenteInDettaglio) return;
    let punti = prompt("Quanti punti vuoi caricare sulla tessera di " + utenteInDettaglio.nome + "?");
    if (!punti || isNaN(punti) || parseInt(punti) <= 0) return;
    
    fetch(`/api/cassa/beneficiario/ricarica?tessera=${utenteInDettaglio.codiceTessera}&puntiAggiuntivi=${parseInt(punti)}`, { method: 'POST' })
    .then(async response => {
        const testoRisposta = await response.text();
        if (!response.ok) throw new Error(testoRisposta);
        alert("✅ " + testoRisposta);
        chiudiModale('modal-dettagli-utente');
    })
    .catch(errore => alert("❌ " + errore.message));
});

document.getElementById('btn-elimina-doc-db').addEventListener('click', () => {
    if (!utenteInDettaglio) return;
    
    if (confirm("Sei sicuro di voler eliminare tutte le immagini associate a " + utenteInDettaglio.nome + " dal database? L'operazione non può essere annullata.")) {
        
        fetch(`/api/cassa/beneficiario/documento?tessera=${utenteInDettaglio.codiceTessera}`, { method: 'DELETE' })
        .then(async response => {
            const testo = await response.text();
            if (!response.ok) throw new Error(testo);
            
            alert("✅ " + testo);
            document.getElementById('area-documento').style.display = 'none';
            utenteInDettaglio.documentoBase64 = null; 
        })
        .catch(errore => alert("❌ " + errore.message));
    }
});


// ==========================================
// 15. VERIFICA IDENTITÀ (CASSA E ANAGRAFICA)
// ==========================================

function mostraDocumentoPos() {
    if (!utenteAttuale || !utenteAttuale.documentoBase64) return;
    apriVisualizzatoreDocumenti(utenteAttuale.documentoBase64);
}

function mostraDocumentoAnagrafica() {
    if (!utenteInDettaglio || !utenteInDettaglio.documentoBase64) return;
    apriVisualizzatoreDocumenti(utenteInDettaglio.documentoBase64);
}

function apriVisualizzatoreDocumenti(stringaBase64) {
    const contenitore = document.getElementById('contenitore-doc-pos');
    contenitore.innerHTML = ""; 
    
    let arrayImmagini = [];
    if (stringaBase64.startsWith('[')) {
        arrayImmagini = JSON.parse(stringaBase64); 
    } else {
        arrayImmagini = [stringaBase64];
    }

    arrayImmagini.forEach(imgBase64 => {
        let img = document.createElement('img');
        img.src = imgBase64;
        img.style = "max-width: 100%; border-radius: 8px; border: 1px solid #ccc; box-shadow: 0 4px 6px rgba(0,0,0,0.1); margin-bottom: 10px;";
        contenitore.appendChild(img);
    });
    
    const modaleAnagrafica = document.getElementById('modal-dettagli-utente');
    if (modaleAnagrafica) {
        modaleAnagrafica.style.zIndex = "1000";
    }
    
    const modaleFoto = document.getElementById('modal-documento-pos');
    modaleFoto.style.zIndex = "99999";
    
    apriModale('modal-documento-pos');
}

function apriModaleEListaDipendenti() {
    caricaDipendenti(); // Scarica la lista
    apriModale('modal-gestione-dipendenti'); // Apre la schermata
}

function inviaNuovoDipendente(event) {
    event.preventDefault();
    const username = document.getElementById('dipendente-username').value;
    const password = document.getElementById('dipendente-password').value;

   
    const dati = {
        username: username,
        passwordHash: password,
        ruolo: 'OPERATORE' // Assegniamo un ruolo di base
    };

    fetch('/api/admin/operatori', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dati)
    })
    .then(response => {
        if (response.ok) {
            alert("Dipendente creato con successo!");
            document.getElementById('form-nuovo-dipendente').reset();
            caricaDipendenti(); // Ricarica subito la tabella sotto
        } else {
            alert("Errore durante la creazione del dipendente. L'username potrebbe già esistere.");
        }
    })
    .catch(err => console.error('Errore:', err));
}