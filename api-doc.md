# 🔗 Blockchain API Dokumentation

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080/api/blockchain`  
**Content-Type:** `application/json`

---

## 📋 Inhaltsverzeichnis

1. [Übersicht](#übersicht)
2. [Endpoints](#endpoints)
3. [Data Transfer Objects (DTOs)](#dtos)
4. [Error Handling](#error-handling)
5. [Beispiele](#beispiele)

---

## 🎯 Übersicht

Die Blockchain API ermöglicht es, eine verteilte Blockchain zu verwalten. Sie können:
- ✅ Transaktionen hinzufügen
- ✅ Blöcke minen
- ✅ Blockchain validieren
- ✅ Blöcke abrufen
- ✅ Status überprüfen
- ✅ Pending Transactions anzeigen
- ✅ Blockchain zurücksetzen

**Schwierigkeit:** 2 (Blöcke müssen mit 2 führenden Nullen beginnen)
**Total Endpoints:** 8

---

## 🔌 Endpoints

### 1. Transaktion hinzufügen

```
POST /api/blockchain/transaction
```

**Beschreibung:** Fügt eine neue Transaktion zur Warteschlange (Pending Transactions) hinzu.

**Request Body:**
```json
{
  "sender": "Alice",
  "receiver": "Bob",
  "amount": 50.0
}
```

**Response (201 Created):**
```json
"Transaktion hinzugefügt!"
```

**Status Codes:**
- `201 Created` - Transaktion erfolgreich hinzugefügt
- `400 Bad Request` - Ungültige Daten

**Beispiel mit cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Alice","receiver":"Bob","amount":50.0}'
```

---

### 2. Block minen

```
POST /api/blockchain/mine
```

**Beschreibung:** Startet den Mining-Prozess. Alle Pending Transactions werden in einen neuen Block gepackt und gemined.

**Request Body:** Keine

**Response (201 Created):**
```json
{
  "index": 1,
  "timestamp": 1779366938898,
  "previousHash": "00ba51c9275570d214e8432f4bde23d3276817426e4028063ef9b3166545b163",
  "transactions": [
    {
      "sender": "Alice",
      "receiver": "Bob",
      "amount": 50.0,
      "timestamp": 1779366920000
    },
    {
      "sender": "Bob",
      "receiver": "Carol",
      "amount": 20.0,
      "timestamp": 1779366925000
    }
  ],
  "hash": "003eb58e7048dd1c63fa415fcad1150081f19c30a8c003d90eb790db4ecf7143",
  "nonce": 266
}
```

**Status Codes:**
- `201 Created` - Block erfolgreich gemined
- `400 Bad Request` - Keine Transaktionen zum Minen

**Hinweise:**
- Mining kann je nach Schwierigkeit mehrere Sekunden dauern
- Nach erfolgreichem Mining werden Pending Transactions geleert

**Beispiel mit cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/mine
```

---

### 3. Alle Blöcke abrufen

```
GET /api/blockchain/blocks
```

**Beschreibung:** Gibt alle Blöcke in der Blockchain zurück.

**Request Body:** Keine

**Response (200 OK):**
```json
[
  {
    "index": 0,
    "timestamp": 1779366897871,
    "previousHash": "0",
    "transactions": [],
    "hash": "00ba51c9275570d214e8432f4bde23d3276817426e4028063ef9b3166545b163",
    "nonce": 107
  },
  {
    "index": 1,
    "timestamp": 1779366938898,
    "previousHash": "00ba51c9275570d214e8432f4bde23d3276817426e4028063ef9b3166545b163",
    "transactions": [
      {
        "sender": "Alice",
        "receiver": "Bob",
        "amount": 50.0,
        "timestamp": 1779366920000
      }
    ],
    "hash": "003eb58e7048dd1c63fa415fcad1150081f19c30a8c003d90eb790db4ecf7143",
    "nonce": 266
  }
]
```

**Status Codes:**
- `200 OK` - Erfolgreich abgerufen

**Beispiel mit cURL:**
```bash
curl http://localhost:8080/api/blockchain/blocks
```

---

### 4. Einzelnen Block abrufen

```
GET /api/blockchain/blocks/{id}
```

**Beschreibung:** Gibt einen spezifischen Block nach Index zurück.

**URL Parameter:**
- `id` (int) - Block Index (0-basiert)

**Response (200 OK):**
```json
{
  "index": 1,
  "timestamp": 1779366938898,
  "previousHash": "00ba51c9275570d214e8432f4bde23d3276817426e4028063ef9b3166545b163",
  "transactions": [
    {
      "sender": "Alice",
      "receiver": "Bob",
      "amount": 50.0,
      "timestamp": 1779366920000
    }
  ],
  "hash": "003eb58e7048dd1c63fa415fcad1150081f19c30a8c003d90eb790db4ecf7143",
  "nonce": 266
}
```

**Status Codes:**
- `200 OK` - Block gefunden
- `404 Not Found` - Block existiert nicht

**Beispiel mit cURL:**
```bash
curl http://localhost:8080/api/blockchain/blocks/1
```

---

### 5. Blockchain validieren

```
GET /api/blockchain/isValid
```

**Beschreibung:** Validiert die komplette Blockchain. Prüft:
- Hash-Integrität aller Blöcke
- Korrekte Verkettung (previousHash)
- Proof-of-Work (führende Nullen)

**Request Body:** Keine

**Response (200 OK) - Valid:**
```json
{
  "isValid": true,
  "message": "Blockchain gültig",
  "blockCount": 3,
  "errorBlock": null,
  "errorDescription": null
}
```

**Response (400 Bad Request) - Invalid:**
```json
{
  "isValid": false,
  "message": "Blockchain ungültig",
  "blockCount": 3,
  "errorBlock": 2,
  "errorDescription": "Block 2 hat ungültigen Hash"
}
```

**Mögliche Error Descriptions:**
- `"Block X hat ungültigen Hash"` - Hash stimmt nicht überein
- `"Block X zeigt auf falschen vorherigen Block"` - Previous Hash passt nicht
- `"Block X erfüllt nicht die Proof-of-Work Bedingung"` - Hash beginnt nicht mit führenden Nullen

**Status Codes:**
- `200 OK` - Blockchain ist gültig
- `400 Bad Request` - Blockchain ist ungültig

**Beispiel mit cURL:**
```bash
curl http://localhost:8080/api/blockchain/isValid
```

---

### 6. Status abrufen

```
GET /api/blockchain/status
```

**Beschreibung:** Gibt schnelle Statusinformationen zur Blockchain zurück.

**Request Body:** Keine

**Response (200 OK):**
```json
{
  "isValid": true,
  "blockCount": 3,
  "difficulty": 2,
  "lastBlockHash": "003eb58e7048dd1c63fa415fcad1150081f19c30a8c003d90eb790db4ecf7143",
  "totalTransactions": 5
}
```

**Status Codes:**
- `200 OK` - Erfolgreich abgerufen

**Beispiel mit cURL:**
```bash
curl http://localhost:8080/api/blockchain/status
```

---

### 7. Pending Transactions abrufen

```
GET /api/blockchain/pending-transactions
```

**Beschreibung:** Gibt alle Transaktionen zurück, die noch nicht gemined wurden (Pending Transactions).

**Request Body:** Keine

**Response (200 OK):**
```json
[
  {
    "sender": "Alice",
    "receiver": "Bob",
    "amount": 50.0,
    "timestamp": 1779366920000
  },
  {
    "sender": "Bob",
    "receiver": "Carol",
    "amount": 30.0,
    "timestamp": 1779366925000
  },
  {
    "sender": "Carol",
    "receiver": "David",
    "amount": 20.0,
    "timestamp": 1779366930000
  }
]
```

**Status Codes:**
- `200 OK` - Erfolgreich abgerufen
- `200 OK` (leeres Array) - Keine ausstehenden Transaktionen

**Hinweise:**
- Diese Transaktionen werden zum nächsten Block hinzugefügt, wenn Mining gestartet wird
- Nach erfolgreichem Mining wird diese Liste geleert

**Beispiel mit cURL:**
```bash
curl http://localhost:8080/api/blockchain/pending-transactions
```

---

### 8. Blockchain zurücksetzen

```
POST /api/blockchain/reset
```

**Beschreibung:** Setzt die gesamte Blockchain zurück. Es wird ein neuer Genesis Block erstellt und alle Daten werden gelöscht. **Warnung: Diese Operation ist nicht rückgängig zu machen!**

**Request Body:** Keine

**Response (200 OK):**
```json
"Blockchain wurde zurückgesetzt"
```

**Status Codes:**
- `200 OK` - Blockchain erfolgreich zurückgesetzt

**Hinweise:**
- Alle Blöcke werden gelöscht
- Alle Pending Transactions werden gelöscht
- Ein neuer Genesis Block wird automatisch erstellt
- Die Schwierigkeit bleibt gleich (2)

**Beispiel mit cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/reset
```

---

## 📦 DTOs

### TransactionDTO

```json
{
  "sender": "string",      // Absender der Transaktion
  "receiver": "string",    // Empfänger der Transaktion
  "amount": "number"       // Betrag in BTC
}
```

### BlockDTO

```json
{
  "index": "integer",                    // Block-Position in der Kette
  "timestamp": "long",                   // Unix Timestamp der Erstellung
  "previousHash": "string",              // Hash des vorherigen Blocks
  "transactions": ["TransactionDTO[]"],  // Array von Transaktionen
  "hash": "string",                      // SHA-256 Hash des Blocks
  "nonce": "integer"                     // Proof-of-Work Nonce
}
```

### StatusDTO

```json
{
  "isValid": "boolean",         // Blockchain valid?
  "blockCount": "integer",      // Anzahl der Blöcke
  "difficulty": "integer",      // Proof-of-Work Schwierigkeit
  "lastBlockHash": "string",    // Hash des letzten Blocks
  "totalTransactions": "integer" // Gesamtanzahl aller Transaktionen
}
```

### ValidationResponseDTO

```json
{
  "isValid": "boolean",           // Blockchain valid?
  "message": "string",            // Aussagekräftige Nachricht
  "blockCount": "integer",        // Anzahl der Blöcke
  "errorBlock": "integer|null",   // Index des fehlerhaften Blocks (null wenn valid)
  "errorDescription": "string|null" // Beschreibung des Fehlers (null wenn valid)
}
```

---

## ❌ Error Handling

### Generische Fehler

**404 Not Found:**
```json
"Block nicht gefunden"
```

**400 Bad Request:**
```json
"Ungültige Anfrageparameter"
```

**500 Internal Server Error:**
```json
{
  "error": "Interner Fehler",
  "message": "Fehlerbeschreibung"
}
```

---

## 📚 Beispiele

### Workflow: Transaktionen hinzufügen und minen

```bash
# 1. Starte den Server
java -jar blockchain-web.jar

# 2. Überprüfe Pending Transactions (sollte leer sein)
curl http://localhost:8080/api/blockchain/pending-transactions

# 3. Füge 3 Transaktionen hinzu
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Alice","receiver":"Bob","amount":50.0}'

curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Bob","receiver":"Carol","amount":30.0}'

curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Carol","receiver":"David","amount":20.0}'

# 4. Überprüfe Pending Transactions (sollte 3 haben)
curl http://localhost:8080/api/blockchain/pending-transactions

# 5. Starte Mining
curl -X POST http://localhost:8080/api/blockchain/mine

# 6. Überprüfe Pending Transactions (sollte wieder leer sein)
curl http://localhost:8080/api/blockchain/pending-transactions

# 7. Überprüfe alle Blöcke
curl http://localhost:8080/api/blockchain/blocks

# 8. Validiere Blockchain
curl http://localhost:8080/api/blockchain/isValid

# 9. Überprüfe Status
curl http://localhost:8080/api/blockchain/status

# 10. Setze Blockchain zurück (für neuen Test)
curl -X POST http://localhost:8080/api/blockchain/reset
```

### Schnelltest aller Endpoints

```bash
# 1. Status
curl http://localhost:8080/api/blockchain/status

# 2. Add Transaction
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Test","receiver":"User","amount":100.0}'

# 3. Pending Transactions
curl http://localhost:8080/api/blockchain/pending-transactions

# 4. Mine
curl -X POST http://localhost:8080/api/blockchain/mine

# 5. Get Blocks
curl http://localhost:8080/api/blockchain/blocks

# 6. Get Single Block
curl http://localhost:8080/api/blockchain/blocks/1

# 7. Validate
curl http://localhost:8080/api/blockchain/isValid

# 8. Reset
curl -X POST http://localhost:8080/api/blockchain/reset
```

### Mit Postman testen

**1. Neuer Block für Transaktionen:**
- Method: POST
- URL: `http://localhost:8080/api/blockchain/transaction`
- Body (JSON):
```json
{
  "sender": "Alice",
  "receiver": "Bob",
  "amount": 50.0
}
```

**2. Mining starten:**
- Method: POST
- URL: `http://localhost:8080/api/blockchain/mine`
- Body: (leer)

**3. Blöcke abrufen:**
- Method: GET
- URL: `http://localhost:8080/api/blockchain/blocks`

---

## ⚡ Performance & Limits

- **Mining-Zeit:** 1-5 Sekunden (abhängig von Schwierigkeit)
- **Max. Transaktionen pro Block:** Unbegrenzt
- **Blockchain-Größe:** Im RAM begrenzt

---

## 🔐 Sicherheit

⚠️ **Dies ist eine Lernimplementierung, NICHT für Produktion!**

- Keine Authentifizierung
- Keine Verschlüsselung
- Keine Raten限制
- Transaktionen sind pseudonym, nicht privat

---

## 📝 Hinweise

- Genesis Block (Index 0) wird automatisch erstellt
- Alle Hashes sind SHA-256
- Timestamps sind Unix Milliseconds
- Die Blockchain ist persistent im RAM (wird bei Neustart zurückgesetzt)

---

**Letzte Aktualisierung:** 2025-05-21