# 04 – Transaktionen & Signaturen

## 🎯 Wofür ist das?

Eine **Transaktion** ist eine Überweisung zwischen zwei Wallets. Das Besondere: Jede Transaktion wird **digital signiert** – das stellt sicher, dass wirklich der Besitzer der Wallet die Überweisung autorisiert hat und niemand sie nachträglich manipulieren kann.

---

## 🔧 Der Lebenszyklus einer Transaktion

```
Schritt 1: ERSTELLEN
  new Transaction("Alice", "Peter", 10.0)
  ┌────────────────────────────┐
  │ sender    = "Alice"        │
  │ receiver  = "Peter"        │
  │ amount    = 10.0           │
  │ timestamp = 1716300000000  │
  │ signature = null           │  ← noch keine Signatur!
  │ isVerified = false         │
  └────────────────────────────┘
         ↓
Schritt 2: SIGNIEREN
  walletService.signTransaction(tx)
  ┌────────────────────────────┐
  │ signature = "MEQCIHxk..."  │  ← Base64-kodierte Signatur
  └────────────────────────────┘
         ↓
Schritt 3: VALIDIEREN
  walletService.validateTransaction(tx) → true ✅
  ┌────────────────────────────┐
  │ isVerified = true          │
  └────────────────────────────┘
         ↓
Schritt 4: IN PENDING-LISTE
  blockChain.pendingTransactions.add(tx)
         ↓
Schritt 5: MINEN
  mineBlock() → Transaktion landet in Block
         ↓
Schritt 6: UNVERÄNDERLICH
  Im Block gespeichert → Kann nicht mehr geändert werden!
```

---

## 🔧 Digitale Signaturen – Schritt für Schritt

### Was ist eine digitale Signatur?

Stell dir vor, Alice schreibt einen Scheck:
- **Ohne Signatur:** Jeder könnte den Betrag ändern
- **Mit Signatur:** Nur Alice kann unterschreiben – und jeder kann prüfen, ob es Alices Unterschrift ist

Das funktioniert mit **asymmetrischer Kryptographie (RSA)**:
- Alice hat einen **privaten Schlüssel** (geheim, nur sie kennt ihn)
- Alice hat einen **öffentlichen Schlüssel** (kann jeder sehen)

```
Alice signiert:                    Jeder verifiziert:
──────────────                     ──────────────────
Transaktionstext                   Transaktionstext
     +                                    +
Privater Key                       Öffentlicher Key
     ↓                                    ↓
  SIGNATUR           ──────────▶   PRÜFUNG: ✅ / ❌
```

---

## 💡 Beispiel: Signieren

```java
// Was wird signiert? → der toString()-Text der Transaktion
// tx.toString() gibt zurück: "Alice -> Peter: 10.0 BTC"

public void signTransaction(Transaction transaction) {
    Wallet sender = wallets.get(transaction.getSender()); // Alices Wallet

    // SHA256withRSA = erst SHA-256-Hash berechnen, dann mit RSA signieren
    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(sender.getPrivateKey()); // Alices privaten Key verwenden

    // Text der Transaktion einfließen lassen
    sig.update(transaction.toString().getBytes());
    // z.B. "Alice -> Peter: 10.0 BTC".getBytes()

    // Signatur berechnen (ergibt viele Bytes)
    byte[] signatureBytes = sig.sign();

    // Bytes in lesbaren Base64-String umwandeln
    String signatureStr = Base64.getEncoder().encodeToString(signatureBytes);
    // Ergebnis: "MEQCIHxkn7R3+k8Bv2Q..." (lange Zeichenkette)

    transaction.setSignature(signatureStr); // An Transaktion hängen
}
```

---

## 💡 Beispiel: Validieren

```java
public boolean validateTransaction(Transaction tx) {
    Wallet sender = wallets.get(tx.getSender()); // Alices Wallet (öffentlicher Key!)

    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initVerify(sender.getPublicKey()); // NUR den öffentlichen Key!

    // Gleichen Text wie beim Signieren verwenden
    sig.update(tx.toString().getBytes());

    // Signatur aus Base64 zurück in Bytes umwandeln
    byte[] signatureBytes = Base64.getDecoder().decode(tx.getSignature());

    // Prüfen: Stimmt die Signatur mit dem Text überein?
    return sig.verify(signatureBytes);
    // true  → ✅ Signatur echt, Transaktion unverändert
    // false → ❌ Signatur falsch oder Transaktion manipuliert
}
```

### Was passiert bei Manipulation?

```
Original:     "Alice -> Peter: 10.0 BTC"  → Signatur: "MEQCIHxk..."
Manipuliert:  "Alice -> Peter: 99.0 BTC"  → Signatur ungültig! ❌
```

Wenn jemand den Betrag ändert, stimmt der Text nicht mehr mit der Signatur überein → Validierung schlägt fehl!

---

## 🔧 SHA256withRSA erklärt

Der Algorithmus `SHA256withRSA` macht zwei Dinge:

```
1. SHA-256:
   "Alice -> Peter: 10.0 BTC"
   → Hash: "a3f7b2c9d1e4..." (immer gleich für gleichen Input)
   (Komprimiert beliebig langen Text auf 32 Bytes)

2. RSA:
   Hash wird mit privatem Key verschlüsselt
   → Signatur: "MEQCIHxk..." (nur mit privatem Key erzeugbar)
```

Beim Verifizieren:
```
Signatur + öffentlicher Key → Entschlüsselter Hash
Verglichen mit: SHA-256(Transaktionstext)
GLEICH? → ✅  UNGLEICH? → ❌
```

---

## 🔧 Ablauf in `addPendingTransaction()`

```java
public void addPendingTransaction(Transaction tx) {
    // 1. Signieren
    walletService.signTransaction(tx);
    //    → tx.signature wird gesetzt

    // 2. Signatur prüfen
    if (!walletService.validateTransaction(tx)) {
        throw new IllegalArgumentException("Transaktion-Signatur ungültig!");
    }

    // 3. Saldo prüfen
    if (!walletService.canTransact(tx.getSender(), tx.getAmount())) {
        tx.setValidationError("Nicht genug Saldo");
        tx.setVerified(false);
        throw new IllegalArgumentException("Nicht genug Saldo!");
    }

    // 4. Alles OK → Als pending markieren und zur Liste hinzufügen
    tx.setVerified(true);
    blockChain.getPendingTransactions().add(tx);
}
```

---

## ⚠️ Wichtig zu wissen

**Signieren und dann sofort validieren:**  
Im Code wird die Transaktion signiert und sofort danach validiert. Das klingt redundant – aber in einem echten System könnten Signatur und Transaktion von verschiedenen Stellen kommen (z.B. über das Netzwerk). Dann ist die Validierung essenziell.

**`toString()` darf sich nie ändern:**  
Die Signatur basiert auf `sender + " -> " + receiver + ": " + amount + " BTC"`. Würde man diesen String ändern, würden alle alten Transaktionen ungültig. Das ist ein fixer Vertrag.

**System-Wallet kann alles signieren:**  
Die Genesis-Transaktionen (`system → Alice`, etc.) werden ebenfalls signiert – mit dem privaten Key der `system`-Wallet. Da das System seine eigene Wallet hat, funktioniert das genauso wie bei normalen Wallets.

---

## 📎 Glossar

| Begriff | Bedeutung |
|---|---|
| **Signatur** | Kryptographischer Beweis, dass der Besitzer zugestimmt hat |
| **RSA** | Asymmetrisches Verschlüsselungsverfahren (Public/Private Key) |
| **SHA-256** | Hash-Funktion, erzeugt 32 Byte aus beliebigem Input |
| **Base64** | Kodierung von Bytes in lesbaren ASCII-Text |
| **Public Key** | Öffentlicher Schlüssel – kann jeder sehen |
| **Private Key** | Geheimer Schlüssel – nur der Besitzer kennt ihn |

---

## 📎 Siehe auch

- [03_WALLET_SYSTEM.md](03_WALLET_SYSTEM.md) – Wallets & Keys
- [06_MINING.md](06_MINING.md) – Was nach der Validierung passiert
- [10_SECURITY.md](10_SECURITY.md) – Warum Signaturen wichtig sind
