# 03 – Das Wallet-System

## 🎯 Wofür ist das?

Ein **Wallet** ist wie ein Konto in der Blockchain. Es hat eine Adresse (den Namen), einen Kontostand und ein Schlüsselpaar zum Signieren von Transaktionen. Der `WalletService` ist der "Bankschalter" – er verwaltet alle Wallets und führt Überweisungen durch.

---

## 🔧 Wie funktioniert es?

### Wallet-Speicherung

Alle Wallets leben in einer `HashMap`:

```java
private Map<String, Wallet> wallets;
// Key:   "Alice"
// Value: Wallet { address="Alice", balance=100.0, keys=... }
```

Das bedeutet: Um eine Wallet zu finden, braucht man nur den Namen (Adresse).

### Beim Start vorbelegte Wallets

```java
public WalletService() {
    this.wallets = new HashMap<>();
    addWallet("Alice");          // balance = 0 BTC
    addWallet("Peter");          // balance = 0 BTC
    addWallet("Nina");           // balance = 0 BTC
    addWallet("system", 10000.0); // balance = 10.000 BTC (Ausgabe-Wallet)
}
```

```
┌─────────────────────────────────────┐
│  WalletService (HashMap)            │
│                                     │
│  "Alice"  → Wallet(0.0 BTC)        │
│  "Peter"  → Wallet(0.0 BTC)        │
│  "Nina"   → Wallet(0.0 BTC)        │
│  "system" → Wallet(10000.0 BTC)    │
└─────────────────────────────────────┘
```

> 💡 Die Genesis-Transaktionen (system → Alice/Peter/Nina je 100 BTC) sind beim Start als **pending** gesetzt. Erst nach dem ersten **Mining** haben Alice, Peter und Nina wirklich 100 BTC!

---

## 💡 Beispiel: Wallet erstellen

```java
// Neue Wallet anlegen (via API: POST /api/blockchain/wallets)
walletService.addWallet("Bob");
// → Wallet { address="Bob", balance=0.0, createdAt=..., publicKey=..., privateKey=... }

// Wallet mit Startguthaben
walletService.addWallet("Miner", 50.0);
```

---

## 🔧 Die wichtigsten Methoden

### `canTransact(sender, amount)` – Kann der Sender zahlen?

```java
public boolean canTransact(String sender, double amount) {
    // Wallet existiert nicht?
    if (wallets.get(sender) == null) {
        return false;  // ❌ Unbekannte Wallet
    }
    // Zu wenig Geld?
    if (wallets.get(sender).getBalance() < amount) {
        return false;  // ❌ Nicht genug Saldo
    }
    return true;       // ✅ Transaktion möglich
}
```

**Beispiele:**
```
Alice hat 100 BTC:
  canTransact("Alice", 50.0)  → true  ✅ (100 >= 50)
  canTransact("Alice", 200.0) → false ❌ (100 < 200)
  canTransact("Bob", 10.0)    → false ❌ (Bob existiert nicht)
```

---

### `executeTransaction(sender, receiver, amount)` – Überweisung durchführen

```java
public void executeTransaction(String sender, String receiver, double amount) {
    if (canTransact(sender, amount)) {
        Wallet senderWallet   = wallets.get(sender);
        Wallet receiverWallet = wallets.get(receiver);

        // Empfänger muss existieren!
        if (receiverWallet == null) {
            throw new RuntimeException("Wallet '" + receiver + "' existiert nicht!");
        }

        // Saldo anpassen
        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
    }
}
```

**Beispiel:**
```
Vorher:  Alice = 100 BTC,  Peter = 100 BTC
Aufruf:  executeTransaction("Alice", "Peter", 30.0)
Nachher: Alice = 70 BTC,   Peter = 130 BTC
```

> ⚠️ Diese Methode wird beim **Mining** aufgerufen – nicht sofort wenn die Transaktion hinzugefügt wird! Erst wenn der Block gemined wird, ändert sich der Saldo.

---

### `signTransaction(transaction)` – Digitale Unterschrift

```java
public void signTransaction(Transaction transaction) {
    Wallet senderWallet = wallets.get(transaction.getSender());

    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(senderWallet.getPrivateKey()); // Privater Key des Senders
    sig.update(transaction.toString().getBytes()); // Inhalt = "Alice -> Peter: 10.0 BTC"

    byte[] signatureBytes = sig.sign();
    // In Base64 umwandeln (damit es als String speicherbar ist)
    String signatureStr = Base64.getEncoder().encodeToString(signatureBytes);
    transaction.setSignature(signatureStr); // An die Transaktion anheften
}
```

**Stell dir vor:** Alice unterzeichnet einen Scheck mit ihrer persönlichen Unterschrift. Jeder kann prüfen, ob die Unterschrift echt ist, aber nur Alice kann sie erstellen.

---

### `validateTransaction(tx)` – Signatur überprüfen

```java
public boolean validateTransaction(Transaction tx) {
    Wallet senderWallet = wallets.get(tx.getSender());

    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initVerify(senderWallet.getPublicKey()); // Öffentlicher Key des Senders
    sig.update(tx.toString().getBytes());          // Gleicher Inhalt wie beim Signieren

    byte[] signatureBytes = Base64.getDecoder().decode(tx.getSignature());
    return sig.verify(signatureBytes); // true = Signatur ist echt
}
```

---

### `addReward(address, amount)` – Mining-Belohnung

```java
public void addReward(String minerAddress, double amount) {
    Wallet empfänger = wallets.get(minerAddress);
    empfänger.setBalance(empfänger.getBalance() + amount);
}
// Wird nach jedem Mining aufgerufen: addReward("system", 10.0)
// → system bekommt 10 BTC als Belohnung
```

---

## ⚠️ Wichtig zu wissen

**Saldo-Änderung erst beim Mining!**  
```
addPendingTransaction("Alice", "Peter", 50 BTC)
  → Prüfung: Hat Alice 50 BTC? JA → Transaktion akzeptiert
  → Alice hat aber NOCH IMMER 100 BTC (noch nicht abgezogen!)

mineBlock()
  → JETZT wird executeTransaction aufgerufen
  → Alice: 100 → 50 BTC
  → Peter: 100 → 150 BTC
```

Das ist ein wichtiger Unterschied! Könnte man mehrere Transaktionen machen, die zusammen mehr BTC ausgeben als man hat – solange man sie vor dem Mining einreicht (bekanntes Problem in simplen Implementierungen).

**Mining-Reward geht ans System:**  
Im aktuellen Code bekommt `"system"` die 10 BTC Reward – nicht ein externer Miner. In echten Blockchains würde der Miner belohnt werden.

---

## 📎 Siehe auch

- [02_MODELS.md](02_MODELS.md) – Wallet-Klasse im Detail
- [04_TRANSACTIONS.md](04_TRANSACTIONS.md) – Signaturen erklärt
- [10_SECURITY.md](10_SECURITY.md) – Warum RSA-2048?
