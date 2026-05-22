# 07 – Die Service-Schicht

## 🎯 Wofür ist das?

Services enthalten die **Geschäftslogik** – also die Regeln, was erlaubt ist und wie Dinge passieren. Sie sind das Herzstück des Projekts. Der Controller gibt Befehle, aber die Services entscheiden, ob und wie sie ausgeführt werden.

---

## 🔧 Übersicht der drei Services

```
┌──────────────────────────────────────────────────────┐
│                  BlockChainService                    │
│  (Die Zentrale – orchestriert alles)                  │
│                                                       │
│  hat:  BlockChain (Daten)                             │
│  hat:  BlockService (Mining-Helfer)                   │
│  hat:  WalletService (Wallet-Helfer)                  │
└─────────────────────┬────────────────────────────────┘
                      │
         ┌────────────┴────────────┐
         ▼                         ▼
┌─────────────────┐       ┌─────────────────────┐
│  BlockService   │       │   WalletService     │
│  (Mining-Logik) │       │  (Wallet & Signaturen)│
│                 │       │                     │
│  mineBlock()   │       │  addWallet()        │
└─────────────────┘       │  signTransaction()  │
                          │  validateTransaction│
                          │  canTransact()      │
                          │  executeTransaction │
                          │  addReward()        │
                          └─────────────────────┘
```

---

## 💡 BlockChainService – Die Zentrale

### Initialisierung

```java
public BlockChainService() {
    this.blockChain = new BlockChain(2); // difficulty=2
    createGenesisBlock();                // Block 0 minen
    initializeGenesisTransactions();     // system → Alice/Peter/Nina als pending
}
```

### addPendingTransaction(tx)

Fügt eine validierte Transaktion zur Warteschlange hinzu.

```
Eingabe: Transaction(sender, receiver, amount)
    ↓
1. signTransaction(tx)           ← Signatur erstellen
    ↓
2. validateTransaction(tx) → false?  → Exception: "Signatur ungültig!"
    ↓
3. canTransact(sender, amount) → false? → Exception: "Nicht genug Saldo!"
    ↓
4. tx.setVerified(true)
5. pendingTransactions.add(tx)
    ↓
Ausgabe: Transaktion liegt in der Warteschlange
```

### mineBlock()

```
Eingabe: (keine – nimmt alle pending Transaktionen)
    ↓
1. executeTransaction() für jede pending TX
   (Saldi werden jetzt wirklich geändert)
    ↓
2. Neuen Block erstellen
   index = chain.size()
   previousHash = letzterBlock.hash
   transactions = pendingTransactions
    ↓
3. blockService.mineBlock(newBlock, difficulty)
   (Nonce finden – kann Sekunden dauern)
    ↓
4. addReward("system", 10.0)
   (Mining-Belohnung: system +10 BTC)
    ↓
5. chain.add(newBlock)
6. clearPendingTransactions()
    ↓
Ausgabe: Neuer Block in der Blockchain, pending leer
```

### isValid()

```java
// Überprüft jeden Block ab Index 1
for (int i = 1; i < chain.size(); i++) {
    Block current  = chain.get(i);
    Block previous = chain.get(i - 1);

    // Prüfung 1: Hash-Integrität
    if (!current.calculateHash().equals(current.getHash())) {
        return UNGÜLTIG ("Block X hat ungültigen Hash")
    }

    // Prüfung 2: Kettenverkettung
    if (!current.getPreviousHash().equals(previous.getHash())) {
        return UNGÜLTIG ("Block X zeigt auf falschen vorherigen Block")
    }

    // Prüfung 3: Proof-of-Work
    if (!current.getHash().startsWith("00")) {
        return UNGÜLTIG ("Block X erfüllt nicht die Proof-of-Work Bedingung")
    }
}
return GÜLTIG
```

### getStatus()

Gibt schnell Blockchain-Infos zurück:

```java
return new StatusDTO(
    isValid().isValid(),          // Ist die Blockchain gültig?
    getTotalBlocks(),             // Wie viele Blöcke?
    blockChain.getDifficulty(),   // Aktuelle Difficulty
    getLastBlock().getHash(),     // Hash des letzten Blocks
    totalTransactions             // Summe aller Transaktionen
);
```

### reset()

```java
public void reset() {
    this.blockChain = new BlockChain(2);  // Alles löschen
    createGenesisBlock();                  // Neuen Genesis-Block erstellen
    // ⚠️ initializeGenesisTransactions() wird NICHT aufgerufen!
    // → Nach Reset haben Alice/Peter/Nina kein Startgeld mehr
}
```

---

## 💡 BlockService – Der Mining-Helfer

Ein einfacher, spezialisierter Service mit nur einer Methode:

```java
public void mineBlock(Block block, int difficulty) {
    String target = "0".repeat(difficulty); // z.B. "00" für difficulty=2

    while (!block.getHash().startsWith(target)) {
        block.setNonce(block.getNonce() + 1); // Nonce hochzählen
        block.calculateHash();                 // Neuen Hash berechnen
    }
    // Block hat jetzt einen gültigen Hash!
}
```

**Warum ist BlockService eine eigene Klasse?**  
Separation of Concerns: Das Minen eines einzelnen Blocks ist eine klar abgrenzbare Aufgabe. So kann man BlockService unabhängig testen und austauschen.

---

## 💡 WalletService – Wallet-Verwaltung

### Überblick aller Methoden

| Methode | Signatur | Was es tut |
|---|---|---|
| `addWallet` | `(address)` | Neue Wallet mit 0 BTC |
| `addWallet` | `(address, balance)` | Neue Wallet mit Startguthaben |
| `canTransact` | `(sender, amount) → boolean` | Kann der Sender zahlen? |
| `executeTransaction` | `(sender, receiver, amount)` | Überweisung durchführen |
| `signTransaction` | `(tx)` | Transaktion mit Private Key signieren |
| `validateTransaction` | `(tx) → boolean` | Signatur mit Public Key prüfen |
| `addReward` | `(address, amount)` | Betrag direkt zu Wallet addieren |
| `getBalance` | `(address) → double` | Kontostand abfragen |
| `getAllWallets` | `() → List<Wallet>` | Alle Wallets als Liste |
| `getAllWalletsHashMap` | `() → Map<String, Wallet>` | Alle Wallets als HashMap |

### Wichtige Implementation-Details

```java
// canTransact prüft ZWEI Dinge:
public boolean canTransact(String sender, double amount) {
    if (wallets.get(sender) == null) return false; // Wallet existiert?
    if (wallets.get(sender).getBalance() < amount) return false; // Genug Saldo?
    return true;
}

// executeTransaction prüft SENDER UND EMPFÄNGER:
public void executeTransaction(...) {
    if (canTransact(sender, amount)) {
        Wallet receiverWallet = wallets.get(receiver);
        if (receiverWallet == null) {
            throw new RuntimeException("Wallet '" + receiver + "' existiert nicht!");
        }
        // Saldi anpassen
    }
}
```

---

## ⚠️ Wichtig zu wissen

**Services sind KEINE Spring Beans (kein @Service):**  
Die Services werden manuell instanziiert (`new WalletService()`), nicht durch Spring-Injection. Das bedeutet: Jede `new BlockChainService()`-Instanz hat ihre eigenen, unabhängigen WalletService und BlockService.

**Zwei WalletService-Instanzen im Controller:**  
```java
// Im BlockChainController:
private BlockChainService blockChainService = new BlockChainService();
// → hat intern seinen eigenen WalletService (Wallets für Transaktionen)

private WalletService walletService = new WalletService();
// → eigenständige Instanz (für /wallets Endpoints)
```

Diese beiden Services teilen sich **keine** Wallets. Das ist ein Design-Problem im aktuellen Code – die `/wallets`-Endpoints zeigen andere Wallets als die, die für Transaktionen genutzt werden.

**Keine Persistenz:**  
Alle Daten leben im RAM. Bei einem Server-Neustart oder einem `reset()` sind alle Daten weg.

---

## 📎 Siehe auch

- [06_MINING.md](06_MINING.md) – Mining im Detail
- [03_WALLET_SYSTEM.md](03_WALLET_SYSTEM.md) – Wallet-System
- [08_CONTROLLER.md](08_CONTROLLER.md) – Wie Services aufgerufen werden
- [09_FLOW_EXAMPLES.md](09_FLOW_EXAMPLES.md) – Komplette Szenarien
