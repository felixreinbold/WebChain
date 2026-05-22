# 02 – Die Model-Klassen

## 🎯 Wofür ist das?

Models sind die **Daten-Klassen** des Projekts. Sie speichern Informationen und stellen einfache Berechnungen bereit. Man kann sich Models wie Baupläne vorstellen – jede Klasse beschreibt, wie ein Objekt aussieht.

---

## 🔧 Wie funktioniert es?

### Überblick aller Models

```
┌──────────────┐    enthält     ┌──────────────┐
│  BlockChain  │──────────────▶│   Block[]    │
│              │                └──────┬───────┘
│  difficulty  │                       │ enthält
│  pending TX  │                ┌──────▼───────┐
└──────────────┘                │Transaction[] │
                                └──────────────┘

┌──────────────┐
│   Wallet     │
│  RSA-Keys    │
│  Balance     │
└──────────────┘
```

---

## 💡 Wallet.java

### Was speichert eine Wallet?

```java
public class Wallet {
    private String address;      // Name/ID der Wallet (z.B. "Alice")
    private double balance;      // Kontostand in BTC
    private long createdAt;      // Erstellungszeitpunkt (Unix-Timestamp)
    private PublicKey publicKey; // Öffentlicher RSA-Schlüssel
    private PrivateKey privateKey; // Privater RSA-Schlüssel (GEHEIM!)
}
```

### Wie wird eine Wallet erstellt?

```java
// Im Konstruktor passiert alles automatisch:
public Wallet(String address, double balance) {
    this.address = address;
    this.balance = balance;
    this.createdAt = System.currentTimeMillis(); // Jetzt

    // RSA-Schlüsselpaar generieren
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);            // 2048 Bit = sehr sicher
    KeyPair keyPair = keyGen.generateKeyPair();

    this.privateKey = keyPair.getPrivate(); // Zum Signieren
    this.publicKey = keyPair.getPublic();   // Zum Verifizieren
}
```

### Methoden

| Methode | Was sie tut |
|---|---|
| `getAddress()` | Gibt den Namen zurück |
| `getBalance()` | Gibt den Kontostand zurück |
| `setBalance(double)` | Setzt neuen Kontostand |
| `getPublicKey()` | Gibt den öffentlichen Schlüssel zurück |
| `getPrivateKey()` | Gibt den privaten Schlüssel zurück |

> ⚠️ **Sicherheits-Hinweis:** Der private Key verlässt die Wallet nie nach außen! Die `WalletDTO` gibt ihn bewusst nicht zurück.

---

## 💡 Transaction.java

### Was ist eine Transaktion?

Eine Transaktion ist eine **Überweisung**: Alice schickt Peter 10 BTC.

```java
public class Transaction {
    private final String sender;    // Wer schickt? z.B. "Alice"
    private final String receiver;  // Wer bekommt? z.B. "Peter"
    private final double amount;    // Wie viel? z.B. 10.0
    private final long timestamp;   // Wann? (automatisch gesetzt)
    private String signature;       // Kryptographische Unterschrift
    private boolean isVerified;     // Wurde die Signatur geprüft?
    private String validationError; // Fehlermeldung, falls ungültig
}
```

### Lebenszyklus einer Transaktion

```
1. ERSTELLT     → sender, receiver, amount, timestamp
                  (signature = null, isVerified = false)
                     ↓
2. SIGNIERT     → signature wird gesetzt
                  (WalletService.signTransaction)
                     ↓
3. VERIFIZIERT  → isVerified = true
                  (WalletService.validateTransaction)
                     ↓
4. PENDING      → Liegt in blockChain.pendingTransactions
                     ↓
5. GEMINED      → Landet in einem Block (unveränderlich!)
```

### Methoden

| Methode | Was sie tut |
|---|---|
| `toString()` | `"Alice -> Peter: 10.0 BTC"` (Wird zum Signieren genutzt!) |
| `setSignature(String)` | Setzt die Signatur |
| `setVerified(boolean)` | Markiert als verifiziert |
| `setValidationError(String)` | Fehlermeldung setzen |

> 💡 **Tipp:** `toString()` ist sehr wichtig! Die Signatur wird über diesen String erzeugt. Ändert sich Sender, Empfänger oder Betrag, wird die Signatur ungültig.

---

## 💡 Block.java

### Was ist ein Block?

Ein Block ist wie eine **Seite im Kassenbuch** – er enthält mehrere Transaktionen und ist durch seinen Hash mit dem vorherigen Block verbunden.

```java
public class Block {
    private final int index;                    // Position in der Kette (0, 1, 2, ...)
    private final long timestamp;              // Wann erstellt?
    private final String previousHash;         // Hash des Vorgänger-Blocks
    private final List<Transaction> transactions; // Alle enthaltenen TX
    private String hash;                       // Eigener SHA-256 Hash
    private int nonce;                         // Mining-Zahl (wird hochgezählt)
}
```

### Wie wird der Hash berechnet?

```java
public String calculateHash() {
    // Alle Transaktionen als Text zusammensetzen
    String transactionString = "";
    for (Transaction tx : transactions) {
        transactionString += tx.toString(); // z.B. "Alice -> Peter: 10.0 BTC"
    }

    // Alle Felder zu einem langen String verbinden
    String input = index + previousHash + timestamp + transactionString + nonce;
    //              ↑         ↑              ↑              ↑               ↑
    //           "1"   "00abc..." "1716300000"  "Alice..."    "42"

    // SHA-256 anwenden → 64 Zeichen langer Hex-String
    // z.B. "003eb58e7048dd1c63fa415fcad1150081f19c30..."
}
```

> 💡 **Wichtig:** Der Nonce ist der einzige Teil, der beim Mining geändert wird. Dadurch ändert sich der gesamte Hash.

---

## 💡 BlockChain.java

### Was ist die Blockchain-Klasse?

Sie ist der **Container** für alles:

```java
public class BlockChain {
    private List<Block> chain;                    // Alle Blöcke (unveränderlich nach Mining)
    private int difficulty;                        // Wie viele führende Nullen braucht ein Hash?
    private List<Transaction> pendingTransactions; // Noch nicht geminte Transaktionen
}
```

### Wichtige Werte

| Feld | Startwert | Bedeutung |
|---|---|---|
| `difficulty` | `2` | Hashes müssen mit `"00"` beginnen |
| `chain` | leer | Wird sofort mit Genesis-Block gefüllt |
| `pendingTransactions` | leer | Wächst durch `addPendingTransaction` |

---

## ⚠️ Wichtig zu wissen

**`final` Felder in Transaction:**  
Sender, Empfänger und Betrag sind `final` – sie können nach der Erstellung **nicht mehr geändert** werden. Das ist Absicht: Eine Transaktion ist unveränderlich.

**Lombok-Annotations:**  
`@Getter` auf den Klassen bedeutet, dass Lombok automatisch alle `getXxx()`-Methoden generiert. Du musst sie nicht selbst schreiben!

**Balance ist `double`:**  
In echter Kryptowährung würde man `BigDecimal` nutzen, um Rundungsfehler zu vermeiden. Für eine Lernimplementierung reicht `double`.

---

## 📎 Siehe auch

- [03_WALLET_SYSTEM.md](03_WALLET_SYSTEM.md) – Wie Wallets genutzt werden
- [04_TRANSACTIONS.md](04_TRANSACTIONS.md) – Transaktionen im Detail
- [05_BLOCKCHAIN.md](05_BLOCKCHAIN.md) – Die Blockchain im Detail
