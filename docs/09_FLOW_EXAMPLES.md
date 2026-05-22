# 09 – Beispiel-Szenarien

## 🎯 Wofür ist das?

Hier siehst du komplette Szenarien von Anfang bis Ende – welche Klassen beteiligt sind, was genau passiert und was das Ergebnis ist.

---

## 🎬 Szenario 1: Eine Transaktion machen

**Ziel:** Alice schickt Peter 10 BTC

### Schritt-für-Schritt

**① API-Aufruf:**
```bash
POST /api/blockchain/transaction
{"sender": "Alice", "receiver": "Peter", "amount": 10.0}
```

**② Controller empfängt Anfrage:**
```java
// BlockChainController.addTransaction()
Transaction transaction = new Transaction("Alice", "Peter", 10.0);
// Zustand: { sender="Alice", receiver="Peter", amount=10.0,
//            timestamp=now, signature=null, isVerified=false }

blockChainService.addPendingTransaction(transaction);
```

**③ BlockChainService.addPendingTransaction():**
```java
// Schritt 1: Signieren
walletService.signTransaction(tx);
// → Holt Alices private Key
// → Signiert "Alice -> Peter: 10.0 BTC" mit RSA-2048
// → tx.signature = "MEQCIHxkn7R3+k8Bv2Q..." (Base64)

// Schritt 2: Signatur validieren
walletService.validateTransaction(tx); // → true ✅
// → Prüft Signatur mit Alices public Key
// → Text stimmt mit Signatur überein

// Schritt 3: Saldo prüfen
walletService.canTransact("Alice", 10.0);
// → Alice hat 100 BTC (nach Genesis-Mining)
// → 100 >= 10 → true ✅

// Schritt 4: Zur Warteschlange
tx.setVerified(true);
pendingTransactions.add(tx);
```

**④ Zustand nach dem API-Aufruf:**
```
Pending Transactions:
  [0] Alice → Peter : 10.0 BTC ✅ verified

Wallets:
  Alice:  100 BTC  ← noch unverändert!
  Peter:  100 BTC  ← noch unverändert!
```

**⑤ Response:**
```json
"Transaktion hinzugefügt!" (HTTP 201)
```

---

## 🎬 Szenario 2: Mining starten

**Voraussetzung:** Szenario 1 abgeschlossen (1 Transaktion pending)

**① API-Aufruf:**
```bash
POST /api/blockchain/mine
```

**② BlockChainService.mineBlock():**

```java
// PHASE 1: Transaktionen ausführen (Saldi anpassen)
for (Transaction tx : pendingTransactions) {
    walletService.executeTransaction("Alice", "Peter", 10.0);
    // Alice: 100 → 90 BTC
    // Peter: 100 → 110 BTC
}

// PHASE 2: Neuen Block vorbereiten
String prevHash = chain.get(chain.size()-1).getHash();
// prevHash = "00ba51c9..." (Hash des Genesis-Blocks)

Block newBlock = new Block(
    1,           // index
    prevHash,    // previousHash
    [tx(Alice→Peter, 10 BTC)]  // transactions
);
// nonce = 0, hash = "a7f3c2..." (zufälliger Hash)
```

**③ BlockService.mineBlock() – der Mining-Loop:**
```
target = "00"  (difficulty = 2)

nonce=0:   hash = "a7f3c2..." ← NEIN (beginnt nicht mit "00")
nonce=1:   hash = "5b9e1a..." ← NEIN
nonce=2:   hash = "f8d4b7..." ← NEIN
...
nonce=266: hash = "003eb5..." ← JA! ✅ (beginnt mit "00")
→ Mining abgeschlossen!
```

**④ Nach dem Mining:**
```java
// Mining-Reward
walletService.addReward("system", 10.0);
// system: 10.000 → 10.010 BTC

// Block zur Blockchain
chain.add(newBlock);

// Pending-Liste leeren
pendingTransactions.clear();
```

**⑤ Zustand nach Mining:**
```
Blockchain:
  Block[0] Genesis  (hash: "00ba51...")
  Block[1] index=1  (hash: "003eb5...") ← NEU!
    Transaktionen: Alice → Peter : 10 BTC

Wallets:
  Alice:  90 BTC   ← geändert!
  Peter:  110 BTC  ← geändert!
  system: 10.010 BTC ← +10 BTC Reward

Pending Transactions: [] ← leer
```

**⑥ Response:**
```json
{
    "index": 1,
    "hash": "003eb58e7048dd1c63fa415fcad1150081f19c30a8c003d90eb790db4ecf7143",
    "nonce": 266,
    "transactions": [...]
}
```

---

## 🎬 Szenario 3: Genesis-Mining (Beim ersten Start)

**Was passiert beim allerersten `POST /mine`?**

Beim Server-Start werden automatisch 3 Genesis-Transaktionen als pending gesetzt:
```
system → Alice : 100 BTC
system → Peter : 100 BTC
system → Nina  : 100 BTC
```

Wenn jetzt `/mine` aufgerufen wird:

```java
// 1. Transaktionen ausführen
executeTransaction("system", "Alice", 100.0);
// system: 10.000 → 9.900 BTC
// Alice:  0 → 100 BTC

executeTransaction("system", "Peter", 100.0);
// system: 9.900 → 9.800 BTC
// Peter:  0 → 100 BTC

executeTransaction("system", "Nina", 100.0);
// system: 9.800 → 9.700 BTC
// Nina:   0 → 100 BTC

// 2. Block minen (mit 3 Transaktionen)
// 3. Mining-Reward: system + 10 BTC → 9.710 BTC
// 4. Pending leeren
```

**Nach dem ersten Mining:**
```
Wallets:
  Alice:  100 BTC  ✅
  Peter:  100 BTC  ✅
  Nina:   100 BTC  ✅
  system: 9.710 BTC
```

---

## 🎬 Szenario 4: Blockchain validieren

**API-Aufruf:**
```bash
GET /api/blockchain/isValid
```

**Was passiert:**
```java
// Blockchain hat 2 Blöcke: [0]=Genesis, [1]=Erster Block

// i=1: Block[1] prüfen
Block current  = chain.get(1); // Block mit Alice→Peter
Block previous = chain.get(0); // Genesis-Block

// Prüfung 1: Hash-Integrität
current.calculateHash()  == current.getHash()
// "003eb5..." == "003eb5..." → ✅

// Prüfung 2: Kettenverkettung
current.getPreviousHash() == previous.getHash()
// "00ba51..." == "00ba51..." → ✅

// Prüfung 3: Proof-of-Work
current.getHash().startsWith("00")
// "003eb5...".startsWith("00") → ✅

// Alle Blöcke geprüft → GÜLTIG!
```

**Response:**
```json
{
    "valid": true,
    "message": "Blockchain gültig",
    "blockCount": 2,
    "errorBlock": null,
    "errorDescription": null
}
```

---

## 🎬 Szenario 5: Fehler – Nicht genug Saldo

**Alice hat 90 BTC und versucht 200 BTC zu senden:**

```bash
POST /api/blockchain/transaction
{"sender": "Alice", "receiver": "Peter", "amount": 200.0}
```

**Was passiert:**
```java
// 1. signTransaction → OK (Signatur wird trotzdem erstellt)
// 2. validateTransaction → true (Signatur ist gültig)
// 3. canTransact("Alice", 200.0)
//    → Alice hat 90 BTC
//    → 90 < 200 → false ❌

tx.setValidationError("Nicht genug Saldo");
tx.setVerified(false);
throw new IllegalArgumentException("Nicht genug Saldo!");
```

**Response:**
```json
"Nicht genug Saldo!" (HTTP 400)
```

Die Transaktion landet **nicht** in der pending-Liste!

---

## 🎬 Szenario 6: Blockchain zurücksetzen

```bash
POST /api/blockchain/reset
```

**Was passiert:**
```java
this.blockChain = new BlockChain(2);  // Alles löschen
createGenesisBlock();                  // Neuer Genesis-Block

// ACHTUNG: initializeGenesisTransactions() wird NICHT aufgerufen!
// Alice, Peter, Nina bekommen kein Startgeld mehr.
```

**Zustand nach Reset:**
```
Blockchain:
  Block[0] Genesis (neuer Hash)

Wallets (unverändert):
  Alice:  90 BTC  ← der alte Saldo bleibt!
  Peter: 110 BTC  ← der alte Saldo bleibt!
  Nina:  100 BTC
  system: 9.710 BTC

Pending Transactions: []
```

> 💡 Die Wallets werden beim Reset **nicht** zurückgesetzt – nur die Blockchain. Das ist ein Design-Merkmal des aktuellen Codes.

---

## 📎 Typischer kompletter Workflow

```bash
# 1. Server starten → Genesis-Block wird automatisch gemined

# 2. Genesis-Mining (Alice/Peter/Nina je 100 BTC)
curl -X POST http://localhost:8080/api/blockchain/mine

# 3. Transaktion hinzufügen
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Alice","receiver":"Peter","amount":10.0}'

# 4. Noch eine Transaktion
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Peter","receiver":"Nina","amount":5.0}'

# 5. Pending Transaktionen prüfen
curl http://localhost:8080/api/blockchain/pending-transactions

# 6. Block minen
curl -X POST http://localhost:8080/api/blockchain/mine

# 7. Blockchain validieren
curl http://localhost:8080/api/blockchain/isValid

# 8. Status prüfen
curl http://localhost:8080/api/blockchain/status
```

---

## 📎 Siehe auch

- [07_SERVICES.md](07_SERVICES.md) – Service-Logik im Detail
- [08_CONTROLLER.md](08_CONTROLLER.md) – Alle Endpoints
- [06_MINING.md](06_MINING.md) – Mining erklärt
