CREATE DATABASE IF NOT EXISTS bottega_solidale;
USE bottega_solidale;

-- Tabella Beneficiari
CREATE TABLE beneficiari(
    id INT AUTO_INCREMENT PRIMARY KEY,
    codice_tessera VARCHAR(50) UNIQUE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL, 
    saldo_punti INT NOT NULL DEFAULT 0,
    telefono VARCHAR(20),
    cittadinanza VARCHAR(100), 
    citta VARCHAR(100),
    indirizzo_abitazione VARCHAR(255),
    numero_civico INT,
    provincia VARCHAR(5),
    numero_nucleo_familiare INT DEFAULT 1,
    valore_isee VARCHAR(50),
    CONSTRAINT chk_saldo_non_negativo CHECK (saldo_punti >= 0)
);

-- Tabella Prodotti
CREATE TABLE prodotti(
    id INT AUTO_INCREMENT PRIMARY KEY,
    codice_barre VARCHAR(50) UNIQUE NOT NULL,
    descrizione VARCHAR(255) NOT NULL,
    punti_costo INT NOT NULL,
    scorta_magazzino INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_punti_costo_positivo CHECK (punti_costo >= 0),
    CONSTRAINT chk_scorta_magazzino CHECK (scorta_magazzino >= 0)
);

-- Tabella Spese
CREATE TABLE spese(
    id INT AUTO_INCREMENT PRIMARY KEY,
    beneficiario_id INT NOT NULL,
    data_ora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    totale_punti_spesi INT NOT NULL DEFAULT 0,
    FOREIGN KEY(beneficiario_id) REFERENCES beneficiari(id) ON DELETE RESTRICT,
    CONSTRAINT chk_totale_positivo CHECK (totale_punti_spesi >= 0)
);

-- Tabella Dettagli Spesa
CREATE TABLE dettagli_spesa(
    spesa_id INT NOT NULL,
    prodotto_id INT NOT NULL,
    quantita INT NOT NULL DEFAULT 1,
    PRIMARY KEY(spesa_id, prodotto_id),
    FOREIGN KEY(spesa_id) REFERENCES spese(id) ON DELETE CASCADE,
    FOREIGN KEY(prodotto_id) REFERENCES prodotti(id) ON DELETE RESTRICT,
    CONSTRAINT chk_quantita_valida CHECK (quantita > 0)
);

-- Tabella Transazioni (Corretta)
CREATE TABLE transazioni (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codice_tessera VARCHAR(50) NOT NULL,
    codice_barre VARCHAR(50) NOT NULL,
    quantita INT NOT NULL,
    punti_spesi INT NOT NULL,
    data_operazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);