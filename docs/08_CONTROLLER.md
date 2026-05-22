# 08 – REST API Endpoints

## 🎯 Wofür ist das?

Der Controller ist das **Eingangstor** zur Blockchain. Jede HTTP-Anfrage kommt hier an und wird an den passenden Service weitergeleitet. Alle Endpoints sind unter `http://localhost:8080/api/blockchain` erreichbar.

---

## 🔌 Alle Endpoints auf einen Blick

| # | Methode | URL | Was es macht |
|---|---|---|---|
| 1 | `POST` | `/transaction` | Transaktion hinzufügen |
| 2 | `POST` | `/mine` | Block minen |
| 3 | `GET` | `/blocks` | Alle Blöcke abrufen |
| 4 | `GET` | `/blocks/{id}` | Einzelnen Block abrufen |
| 5 | `GET` | `/isValid` | Blockchain validieren |
| 6 | `GET` | `/status` | Status abrufen |
| 7 | `GET` | `/pending-transactions` | Offene Transaktionen |
| 8 | `POST` | `/reset` | Blockchain zurücksetzen |
| 9 | `GET` | `/wallets` | Alle Wallets abrufen |
| 10 | `GET` | `/wallets/{address}` | Wallet-Saldo abfragen |
| 11 | `POST` | `/wallets` | Neue Wallet erstellen |
| 12 | `POST` | `/difficulty` | Difficulty ändern |

---

## 📡 Endpoint-Details

---

### 1. Transaktion hinzufügen

```
POST /api/blockchain/transaction
```

**Was passiert:** Erstellt eine neue Transaktion und fügt sie zur Warteschlange hinzu.

**Request Body:**
```json
{
    "sender": "Alice",
    "receiver": "Peter",
    "amount": 10.0
}
```

**Response 201 (Erfolg):**
```json
"Transaktion hinzugefügt!"
```

**Response 400 (Fehler):**
```json
"Nicht genug Saldo!"
```
oder
```json
"Transaktion-Signatur ungültig!"
```

**Fehler-Cases:**
- ❌ `sender` existiert nicht → `RuntimeException` (Signatur fehlgeschlagen)
- ❌ `receiver` existiert nicht → wird erst beim Mining geprüft
- ❌ Nicht genug Saldo → 400 Bad Request
- ❌ Signatur ungültig → 400 Bad Request

**cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Alice","receiver":"Peter","amount":10.0}'
```

---

### 2. Block minen

```
POST /api/blockchain/mine
```

**Was passiert:** Startet Mining – alle pending Transaktionen werden in einen neuen Block gepackt.

**Request Body:** keiner

**Response 201 (Erfolg):** Der neu geminte Block
```json
{
    "index": 1,
    "timestamp": 1716300000000,
    "previousHash": "00ba51c9275570d214e8432f4bde23d3276817426e4028063ef9b3166545b163",
    "transactions": [
        {
            "sender": "Alice",
            "receiver": "Peter",
            "amount": 10.0,
            "timestamp": 1716299900000,
            "signature": "MEQCIHxk...",
            "verified": true,
            "validationError": null
        }
    ],
    "hash": "003eb58e7048dd1c63fa415fcad1150081f19c30a8c003d90eb790db4ecf7143",
    "nonce": 266
}
```

**⏱️ Hinweis:** Mining dauert 1-30+ Sekunden je nach Difficulty. Der Request wartet, bis Mining fertig ist.

**cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/mine
```

---

### 3. Alle Blöcke abrufen

```
GET /api/blockchain/blocks
```

**Was passiert:** Gibt alle Blöcke der Blockchain zurück.

**Response 200:**
```json
[
    {
        "index": 0,
        "previousHash": "0",
        "transactions": [],
        "hash": "00ba51c9...",
        "nonce": 107
    },
    {
        "index": 1,
        "previousHash": "00ba51c9...",
        "transactions": [...],
        "hash": "003eb58e...",
        "nonce": 266
    }
]
```

**cURL:**
```bash
curl http://localhost:8080/api/blockchain/blocks
```

---

### 4. Einzelnen Block abrufen

```
GET /api/blockchain/blocks/{id}
```

**URL Parameter:**
- `id` = Block-Index (0 = Genesis, 1 = erster Block usw.)

**Response 200:**
```json
{
    "index": 1,
    "timestamp": 1716300000000,
    "previousHash": "00ba51c9...",
    "transactions": [...],
    "hash": "003eb58e...",
    "nonce": 266
}
```

**Response 404:**
```json
"Block nicht gefunden"
```

**cURL:**
```bash
curl http://localhost:8080/api/blockchain/blocks/0   # Genesis
curl http://localhost:8080/api/blockchain/blocks/1   # Erster Block
```

---

### 5. Blockchain validieren

```
GET /api/blockchain/isValid
```

**Was passiert:** Prüft die gesamte Blockchain auf Integrität.

**Response 200 (gültig):**
```json
{
    "valid": true,
    "message": "Blockchain gültig",
    "blockCount": 3,
    "errorBlock": null,
    "errorDescription": null
}
```

**Response 400 (ungültig):**
```json
{
    "valid": false,
    "message": "Blockchain ungültig",
    "blockCount": 3,
    "errorBlock": 2,
    "errorDescription": "Block 2 hat ungültigen Hash"
}
```

**Mögliche errorDescriptions:**
- `"Block X hat ungültigen Hash"`
- `"Block X zeigt auf falschen vorherigen Block"`
- `"Block X erfüllt nicht die Proof-of-Work Bedingung"`

**cURL:**
```bash
curl http://localhost:8080/api/blockchain/isValid
```

---

### 6. Status abrufen

```
GET /api/blockchain/status
```

**Response 200:**
```json
{
    "valid": true,
    "blockCount": 2,
    "difficulty": 2,
    "lastBlockHash": "003eb58e...",
    "totalTransactions": 3
}
```

**cURL:**
```bash
curl http://localhost:8080/api/blockchain/status
```

---

### 7. Pending Transactions abrufen

```
GET /api/blockchain/pending-transactions
```

**Response 200 (mit pending TX):**
```json
[
    {
        "sender": "Alice",
        "receiver": "Peter",
        "amount": 10.0,
        "timestamp": 1716299900000,
        "signature": "MEQCIHxk...",
        "verified": true
    }
]
```

**Response 200 (keine pending TX):**
```json
[]
```

**cURL:**
```bash
curl http://localhost:8080/api/blockchain/pending-transactions
```

---

### 8. Blockchain zurücksetzen

```
POST /api/blockchain/reset
```

**⚠️ WARNUNG: Löscht die gesamte Blockchain!**

**Response 200:**
```json
"Blockchain wurde zurückgesetzt"
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/reset
```

---

### 9. Alle Wallets abrufen

```
GET /api/blockchain/wallets
```

> ⚠️ Nutzt einen separaten WalletService-Instanz (siehe [07_SERVICES.md](07_SERVICES.md))

**Response 200:**
```json
[
    {"address": "Alice", "balance": 0.0, "createdAt": 1716300000000},
    {"address": "Peter", "balance": 0.0, "createdAt": 1716300000001},
    {"address": "Nina",  "balance": 0.0, "createdAt": 1716300000002},
    {"address": "system","balance": 10000.0, "createdAt": 1716300000003}
]
```

**cURL:**
```bash
curl http://localhost:8080/api/blockchain/wallets
```

---

### 10. Wallet-Saldo abfragen

```
GET /api/blockchain/wallets/{address}
```

**Response 200:**
```json
100.0
```

**cURL:**
```bash
curl http://localhost:8080/api/blockchain/wallets/Alice
```

---

### 11. Neue Wallet erstellen

```
POST /api/blockchain/wallets
```

**Request Body:** Einfacher String (kein JSON-Objekt!)
```
"Bob"
```

**Response 201:**
```json
{
    "address": "Bob",
    "balance": 0.0,
    "createdAt": 1716300000000
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/wallets \
  -H "Content-Type: application/json" \
  -d '"Bob"'
```

---

### 12. Difficulty ändern

```
POST /api/blockchain/difficulty
```

**Request Body:**
```json
{"difficulty": 3}
```

**Response 201:**
```json
3
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/blockchain/difficulty \
  -H "Content-Type: application/json" \
  -d '{"difficulty": 3}'
```

---

## ⚠️ Wichtig zu wissen

**Fehlerbehandlung:**  
Der Controller fängt `IllegalArgumentException` in der Transaktion-Route ab. Andere Fehler (z.B. RuntimeException) können als 500 zurückkommen.

**Keine Authentifizierung:**  
Alle Endpoints sind öffentlich zugänglich. Jeder kann Transaktionen hinzufügen und minen.

**Content-Type:**  
Alle POST-Requests brauchen `Content-Type: application/json` im Header (außer `/wallets` bei einigen Clients).

---

## 📎 Siehe auch

- [07_SERVICES.md](07_SERVICES.md) – Was die Services intern machen
- [09_FLOW_EXAMPLES.md](09_FLOW_EXAMPLES.md) – Vollständige Workflow-Beispiele
