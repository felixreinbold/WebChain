# 🔗 WebChain API Dokumentation

**Base URL:** `http://localhost:8080/api/blockchain`

---

## 📊 Blockchain Status & Verwaltung

### 1. Status abrufen
```
GET /status
```
**Response:** `200 OK`
```json
{
  "isValid": true,
  "blockCount": 3,
  "difficulty": 2,
  "lastBlockHash": "00a1b2c3...",
  "totalTransactions": 5
}
```

### 2. Blockchain validieren
```
GET /isValid
```
**Response:** `200 OK` oder `400 BAD_REQUEST`
```json
{
  "isValid": true,
  "message": "Blockchain gültig",
  "blockCount": 3,
  "errorBlock": null,
  "errorDescription": null
}
```

### 3. Blockchain zurücksetzen
```
POST /reset
```
**Response:** `200 OK`
```json
"Blockchain wurde zurückgesetzt"
```

**Fehler:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Zurücksetzen der Blockchain",
  "errorCode": "RESET_FAILED",
  "timestamp": 1718275200000
}
```

---

## ⛏️ Mining

### 1. Block minen
```
POST /mine
Content-Type: application/json

{
  "minerAddress": "Alice"
}
```
**Response:** `201 CREATED`
```json
{
  "index": 2,
  "timestamp": 1718275200000,
  "previousHash": "00a1b2c3...",
  "transactions": [
    {
      "sender": "Alice",
      "receiver": "Peter",
      "amount": 10.0,
      "timestamp": 1718275100000,
      "signature": "abc123...",
      "verified": true
    }
  ],
  "hash": "00xyz789...",
  "nonce": 156
}
```

**Fehler - Miner-Adresse erforderlich:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Miner-Adresse erforderlich!",
  "errorCode": "INVALID_MINER_ADDRESS",
  "timestamp": 1718275200000
}
```

**Fehler - Miner-Wallet nicht gefunden:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "empfänger nicht gefunden",
  "errorCode": "MINER_WALLET_NOT_FOUND",
  "timestamp": 1718275200000
}
```

**Fehler - Mining-Fehler:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Mining: ...",
  "errorCode": "MINING_FAILED",
  "timestamp": 1718275200000
}
```

---

## 💸 Transaktionen

### 1. Transaktion hinzufügen
```
POST /transaction
Content-Type: application/json

{
  "sender": "Alice",
  "receiver": "Peter",
  "amount": 25.5
}
```
**Response:** `201 CREATED`
```json
"Transaktion hinzugefügt!"
```

**Fehler - Sender erforderlich:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Sender erforderlich!",
  "errorCode": "INVALID_SENDER",
  "timestamp": 1718275200000
}
```

**Fehler - Empfänger erforderlich:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Empfänger erforderlich!",
  "errorCode": "INVALID_RECEIVER",
  "timestamp": 1718275200000
}
```

**Fehler - Betrag ungültig:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Betrag muss größer als 0 sein!",
  "errorCode": "INVALID_AMOUNT",
  "timestamp": 1718275200000
}
```

**Fehler - Nicht genug Saldo:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Nicht genug Saldo!",
  "errorCode": "TRANSACTION_VALIDATION_FAILED",
  "timestamp": 1718275200000
}
```

**Fehler - Signatur ungültig:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Transaktion-Signatur ungültig!",
  "errorCode": "TRANSACTION_VALIDATION_FAILED",
  "timestamp": 1718275200000
}
```

**Fehler - Empfänger-Wallet nicht gefunden:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Wallet 'Bob' existiert nicht!",
  "errorCode": "TRANSACTION_ERROR",
  "timestamp": 1718275200000
}
```

### 2. Ausstehende Transaktionen abrufen
```
GET /pending-transactions
```
**Response:** `200 OK`
```json
[
  {
    "sender": "Alice",
    "receiver": "Peter",
    "amount": 10.0,
    "timestamp": 1718275100000,
    "signature": "abc123...",
    "verified": true
  }
]
```

**Fehler:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Abrufen der Transaktionen",
  "errorCode": "INTERNAL_ERROR",
  "timestamp": 1718275200000
}
```

---

## 📦 Blöcke

### 1. Alle Blöcke abrufen
```
GET /blocks
```
**Response:** `200 OK`
```json
[
  {
    "index": 0,
    "timestamp": 1718275000000,
    "previousHash": "0",
    "transactions": [],
    "hash": "00genesis...",
    "nonce": 0
  },
  {
    "index": 1,
    "timestamp": 1718275100000,
    "previousHash": "00genesis...",
    "transactions": [...],
    "hash": "00a1b2c3...",
    "nonce": 42
  }
]
```

**Fehler:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Abrufen der Blöcke",
  "errorCode": "INTERNAL_ERROR",
  "timestamp": 1718275200000
}
```

### 2. Einen Block abrufen (nach Index)
```
GET /blocks/1
```
**Response:** `200 OK`
```json
{
  "index": 1,
  "timestamp": 1718275100000,
  "previousHash": "00genesis...",
  "transactions": [...],
  "hash": "00a1b2c3...",
  "nonce": 42
}
```

**Fehler - Negativer Index:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Block-Index kann nicht negativ sein!",
  "errorCode": "INVALID_BLOCK_INDEX",
  "timestamp": 1718275200000
}
```

**Fehler - Block nicht gefunden:** `404 NOT_FOUND`
```json
{
  "status": 404,
  "message": "Block mit Index 99 nicht gefunden",
  "errorCode": "BLOCK_NOT_FOUND",
  "timestamp": 1718275200000
}
```

**Fehler - Interner Fehler:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Abrufen des Blocks",
  "errorCode": "INTERNAL_ERROR",
  "timestamp": 1718275200000
}
```

---

## 👛 Wallets & Konten

### 1. Alle Wallets abrufen
```
GET /wallets
```
**Response:** `200 OK`
```json
[
  {
    "address": "Alice",
    "balance": 185.0,
    "createdAt": 1718275000000
  },
  {
    "address": "Peter",
    "balance": 110.0,
    "createdAt": 1718275000000
  },
  {
    "address": "Nina",
    "balance": 100.0,
    "createdAt": 1718275000000
  },
  {
    "address": "system",
    "balance": 9605.0,
    "createdAt": 1718275000000
  }
]
```

**Fehler:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Abrufen der Wallets",
  "errorCode": "INTERNAL_ERROR",
  "timestamp": 1718275200000
}
```

### 2. Balance eines Wallets abrufen
```
GET /wallets/Alice
```
**Response:** `200 OK`
```json
185.0
```

**Fehler - Adresse erforderlich:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Wallet-Adresse erforderlich!",
  "errorCode": "INVALID_ADDRESS",
  "timestamp": 1718275200000
}
```

**Fehler - Wallet nicht gefunden:** `404 NOT_FOUND`
```json
{
  "status": 404,
  "message": "Wallet 'Bob' nicht gefunden",
  "errorCode": "WALLET_NOT_FOUND",
  "timestamp": 1718275200000
}
```

### 3. Neues Wallet erstellen
```
POST /wallets
Content-Type: application/json

"Bob"
```
**Response:** `201 CREATED`
```json
{
  "address": "Bob",
  "balance": 0.0,
  "createdAt": 1718275200000
}
```

**Fehler - Adresse erforderlich:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Wallet-Adresse erforderlich!",
  "errorCode": "INVALID_ADDRESS",
  "timestamp": 1718275200000
}
```

**Fehler - Erstellung fehlgeschlagen:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Erstellen des Wallets: ...",
  "errorCode": "WALLET_CREATION_FAILED",
  "timestamp": 1718275200000
}
```

---

## ⚙️ Konfiguration

### 1. Schwierigkeit ändern (Difficulty)
```
POST /difficulty
Content-Type: application/json

{
  "difficulty": 3
}
```
**Response:** `200 OK`
```json
"Mining-Schwierigkeit auf 3 gesetzt"
```

**Fehler - Difficulty ≤ 0:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Difficulty muss größer als 0 sein!",
  "errorCode": "INVALID_DIFFICULTY",
  "timestamp": 1718275200000
}
```

**Fehler - Difficulty > 10:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Difficulty kann maximal 10 sein!",
  "errorCode": "DIFFICULTY_EXCEEDS_MAX",
  "timestamp": 1718275200000
}
```

**Fehler - Update fehlgeschlagen:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Aktualisieren der Schwierigkeit",
  "errorCode": "DIFFICULTY_UPDATE_FAILED",
  "timestamp": 1718275200000
}
```

### 2. Mining-Reward ändern
```
POST /reward
Content-Type: application/json

{
  "amount": 50.0
}
```
**Response:** `200 OK`
```json
"Mining-Reward auf 50.0 BTC aktualisiert"
```

**Fehler - Reward ≤ 0:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Reward muss größer als 0 sein!",
  "errorCode": "INVALID_REWARD_AMOUNT",
  "timestamp": 1718275200000
}
```

**Fehler - Reward > 1000:** `400 BAD_REQUEST`
```json
{
  "status": 400,
  "message": "Reward kann maximal 1000 BTC sein!",
  "errorCode": "REWARD_EXCEEDS_MAX",
  "timestamp": 1718275200000
}
```

**Fehler - Update fehlgeschlagen:** `500 INTERNAL_SERVER_ERROR`
```json
{
  "status": 500,
  "message": "Fehler beim Aktualisieren des Rewards",
  "errorCode": "REWARD_UPDATE_FAILED",
  "timestamp": 1718275200000
}
```

---

## 📋 Fehlerbehandlung

### ErrorResponseDTO Format
```json
{
  "status": 400,
  "message": "Benutzerfreundliche Fehlermeldung",
  "errorCode": "MASCHINENLESBARER_CODE",
  "timestamp": 1718275200000
}
```

### HTTP Status Codes
- `200 OK` - Erfolgreiche GET-Anfrage
- `201 CREATED` - Erfolgreiche POST-Anfrage
- `400 BAD_REQUEST` - Ungültige Anfrage/Validierungsfehler
- `404 NOT_FOUND` - Ressource nicht gefunden
- `500 INTERNAL_SERVER_ERROR` - Server-Fehler

### Error Codes Übersicht
| Code | Status | Bedeutung |
|------|--------|-----------|
| `INVALID_SENDER` | 400 | Sender nicht angegeben |
| `INVALID_RECEIVER` | 400 | Empfänger nicht angegeben |
| `INVALID_AMOUNT` | 400 | Betrag ≤ 0 |
| `INVALID_MINER_ADDRESS` | 400 | Miner-Adresse nicht angegeben |
| `INVALID_ADDRESS` | 400 | Wallet-Adresse nicht angegeben |
| `INVALID_BLOCK_INDEX` | 400 | Block-Index negativ |
| `INVALID_DIFFICULTY` | 400 | Difficulty ≤ 0 |
| `INVALID_REWARD_AMOUNT` | 400 | Reward ≤ 0 |
| `BLOCK_NOT_FOUND` | 404 | Block nicht vorhanden |
| `WALLET_NOT_FOUND` | 404 | Wallet existiert nicht |
| `DIFFICULTY_EXCEEDS_MAX` | 400 | Difficulty > 10 |
| `REWARD_EXCEEDS_MAX` | 400 | Reward > 1000 |
| `TRANSACTION_VALIDATION_FAILED` | 400 | Transaktions-Validierung fehlgeschlagen |
| `MINING_FAILED` | 500 | Mining-Fehler |
| `WALLET_CREATION_FAILED` | 500 | Wallet-Erstellung fehlgeschlagen |
| `INTERNAL_ERROR` | 500 | Unerwarteter Fehler |

---

## 🧪 Beispiel: Kompletter Workflow

```bash
# 1. Status überprüfen
curl http://localhost:8080/api/blockchain/status

# 2. Neues Wallet erstellen
curl -X POST http://localhost:8080/api/blockchain/wallets \
  -H "Content-Type: application/json" \
  -d '"Bob"'

# 3. Transaktion hinzufügen (Validierung wird durchgeführt)
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"system","receiver":"Bob","amount":50}'

# 4. Mining durchführen (Bob ist Miner)
curl -X POST http://localhost:8080/api/blockchain/mine \
  -H "Content-Type: application/json" \
  -d '{"minerAddress":"Bob"}'

# 5. Bob's Balance überprüfen (sollte 50 + Mining-Reward sein)
curl http://localhost:8080/api/blockchain/wallets/Bob

# 6. Alle Blöcke anschauen
curl http://localhost:8080/api/blockchain/blocks
```

---

## 🧪 Beispiel: Fehlerbehandlung testen

```bash
# Test 1: Ungültige Miner-Adresse
curl -X POST http://localhost:8080/api/blockchain/mine \
  -H "Content-Type: application/json" \
  -d '{"minerAddress":""}'

# Response:
{
  "status": 400,
  "message": "Miner-Adresse erforderlich!",
  "errorCode": "INVALID_MINER_ADDRESS",
  "timestamp": 1718275200000
}

# Test 2: Zu hoher Reward
curl -X POST http://localhost:8080/api/blockchain/reward \
  -H "Content-Type: application/json" \
  -d '{"amount":5000}'

# Response:
{
  "status": 400,
  "message": "Reward kann maximal 1000 BTC sein!",
  "errorCode": "REWARD_EXCEEDS_MAX",
  "timestamp": 1718275200000
}

# Test 3: Block nicht gefunden
curl http://localhost:8080/api/blockchain/blocks/999

# Response:
{
  "status": 404,
  "message": "Block mit Index 999 nicht gefunden",
  "errorCode": "BLOCK_NOT_FOUND",
  "timestamp": 1718275200000
}
```

---

## 📊 Datentypen

| Feld | Typ | Beschreibung |
|------|-----|-------------|
| address | String | Wallet-Adresse |
| amount | double | Geldmenge in BTC |
| balance | double | Wallet-Saldo |
| difficulty | int | Mining-Schwierigkeit (0-10) |
| index | int | Block-Nummer in der Chain |
| nonce | int | Proof-of-Work Zähler |
| hash | String | SHA-256 Block-Hash |
| previousHash | String | Hash des vorherigen Blocks |
| timestamp | long | Unix-Timestamp in ms |
| signature | String | Base64-kodierte RSA-Signatur |
| verified | boolean | Transaktion validiert? |
| status | int | HTTP Status Code |
| errorCode | String | Fehler-Code |
| message | String | Benutzerfreundliche Nachricht |

---

## 🔐 Sicherheit

- ✅ Transaktionen werden mit RSA-2048 signiert
- ✅ Signaturen werden bei jeder Transaktion validiert
- ✅ Blöcke werden mit SHA-256 gehashed
- ✅ Blockchain wird auf Integrität überprüft
- ✅ Nur Wallets mit ausreichend Saldo können überweisen
- ✅ Umfassende Input-Validierung auf allen Endpoints

---

## 📝 Hinweise für Frontend

1. **Polling:** Status/Blöcke müssen regelmäßig abgerufen werden (z.B. alle 2s)
2. **Timestamps:** `timestamp` ist in Millisekunden (nicht Sekunden!)
3. **Wallet-Erstellung:** Automatisch mit 0 BTC erstellt
4. **Genesis:** Der erste Block (Index 0) hat keine Transaktionen
5. **Error Handling:** Immer auf `errorCode` prüfen für spezifische UI-Reaktionen
6. **Standardwerte:**
    - Difficulty: 2
    - Mining-Reward: 10 BTC
    - Vorbereitete Wallets: Alice, Peter, Nina (je 100 BTC nach Genesis-Mining)
    - System-Wallet: 10.000 BTC