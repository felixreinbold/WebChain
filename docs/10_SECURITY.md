# 10 – Sicherheits-Features

## 🎯 Wofür ist das?

Hier werden alle Sicherheitsmechanismen erklärt, die das System robust gegen Manipulation machen. Eine Blockchain ist kein Zaubertrick – sie nutzt bekannte kryptographische Prinzipien sehr clever.

---

## 🔐 Feature 1: Digitale Signaturen

### Warum brauchen wir Signaturen?

**Ohne Signaturen** könnte jeder behaupten, eine Transaktion stamme von Alice:
```
❌ Angreifer sendet: {"sender":"Alice","receiver":"Hacker","amount":100.0}
→ Ohne Signatur: API akzeptiert das!
```

**Mit Signaturen** kann nur Alice ihre eigenen Transaktionen autorisieren:
```
✅ Nur Alice kann mit ihrem privaten Key signieren
✅ Jeder kann mit Alices öffentlichem Key die Signatur prüfen
✅ Angreifer kann keine gültige Signatur für Alices Wallet erstellen
```

### Wie stark ist die Signatur?

Das Verfahren `SHA256withRSA`:
1. **SHA-256:** Erzeugt aus dem Transaktionstext einen 256-Bit-Hash
2. **RSA 2048:** Verschlüsselt den Hash mit dem 2048-Bit-Private-Key

RSA 2048 gilt als sicher bis mindestens 2030 (NIST-Empfehlung). Mit heutigen Computern würde das Knacken eines einzelnen Keys tausende Jahre dauern.

```
Transaktionstext
"Alice -> Peter: 10.0 BTC"
         ↓ SHA-256
"a3f7b2c9d1e4..." (256 Bit)
         ↓ RSA-2048 mit Private Key
"MEQCIHxkn7R3+k8Bv2Q..." (Base64-Signatur)
```

---

## 🔐 Feature 2: Hash-Integrität

### Was schützt der Hash?

Jeder Block enthält einen SHA-256-Hash seines gesamten Inhalts:
```
hash = SHA-256(index + previousHash + timestamp + transactions + nonce)
```

**Jede Manipulation am Block ändert den Hash:**

```
Original:
  transactions = "Alice -> Peter: 10.0 BTC"
  hash = "003eb58e7048dd..."

Manipulation versucht:
  transactions = "Alice -> Peter: 99.0 BTC"  ← geändert!
  hash = "a7f3c2b9e1d4..."  ← KOMPLETT anders!

isValid() prüft:
  calculateHash() == gespeicherter hash?
  "a7f3c2..." == "003eb5..."?  → NEIN! ❌ → UNGÜLTIG!
```

### SHA-256 – Warum ist es zuverlässig?

- **Deterministisch:** Gleicher Input → immer gleicher Output
- **Kaskadierend:** Winzige Änderung → komplett anderer Hash
- **Einweg:** Aus dem Hash kann man nicht zurück auf den Input schließen
- **Kollisionsfrei:** Praktisch unmöglich, zwei verschiedene Inputs mit gleichem Hash zu finden

---

## 🔐 Feature 3: Ketten-Integrität (previousHash)

### Wie verhindert die Kette Manipulationen?

Jeder Block referenziert den Hash seines Vorgängers:

```
Block[0]: hash = "00ba51..."
Block[1]: previousHash = "00ba51..." , hash = "003eb5..."
Block[2]: previousHash = "003eb5..." , hash = "00f1a2..."
```

**Was passiert wenn Block[1] manipuliert wird?**

```
Angreifer ändert Block[1]:
  → Block[1].hash ändert sich zu "xyz789..."

isValid() prüft Block[2]:
  Block[2].previousHash = "003eb5..."  (alter Hash)
  Block[1].hash = "xyz789..."          (neuer Hash nach Manipulation)
  "003eb5..." == "xyz789..."?  → NEIN! ❌ → UNGÜLTIG!
```

Der Angreifer müsste **alle folgenden Blöcke** neu minen – bei echter Blockchain mit vielen Minern praktisch unmöglich.

---

## 🔐 Feature 4: Proof-of-Work

### Warum ist der Mining-Aufwand eine Sicherheitsfunktion?

Mining ist absichtlich schwierig:
```
Difficulty = 2: ~256 Versuche (< 1 Sekunde)
Difficulty = 4: ~65.536 Versuche (10-60 Sekunden)
Difficulty = 6: ~16.777.216 Versuche (Minuten)
```

**Manipulation bedeutet Neu-Mining:**

Wenn ein Angreifer Block[1] manipuliert:
1. Block[1] neu minen (rechenintensiv!)
2. Block[2] neu minen (rechenintensiv!)
3. Block[3] neu minen (rechenintensiv!)
4. ... alle weiteren Blöcke

In echten Netzwerken mit vielen Minern wächst die ehrliche Kette schneller als der Angreifer nachkommen kann – das ist die Grundlage von **51%-Angriffen** (die erst bei enormer Rechenleistung möglich wären).

---

## 🔐 Feature 5: Saldo-Validierung

### Niemand kann mehr ausgeben als er hat

```java
public boolean canTransact(String sender, double amount) {
    // Wallet muss existieren
    if (wallets.get(sender) == null) return false;

    // Saldo muss ausreichen
    if (wallets.get(sender).getBalance() < amount) return false;

    return true;
}
```

**Schutz:**
- ❌ Wallet existiert nicht → Transaktion abgelehnt
- ❌ Saldo zu niedrig → Transaktion abgelehnt
- ❌ Negativer Betrag → würde `balance < amount` nie erfüllen

### Timing der Saldo-Prüfung

```
addPendingTransaction():  Prüft ob Sender genug hat → JETZT
executeTransaction():     Zieht tatsächlich ab → beim MINING
```

> ⚠️ **Bekannte Schwachstelle:** Zwischen der Prüfung und dem Mining könnte der Saldo durch andere Transaktionen sinken. Eine robustere Implementierung würde den Saldo auch beim Mining erneut prüfen.

---

## ⚠️ Grenzen dieser Implementierung

Dies ist eine **Lernimplementierung** – nicht für Produktion. Fehlende Sicherheitsfeatures:

| Feature | Status | Beschreibung |
|---|---|---|
| Authentifizierung | ❌ | Jeder kann Transaktionen erstellen |
| Rate Limiting | ❌ | Keine Begrenzung von API-Anfragen |
| Netzwerk-Verteilung | ❌ | Nur ein Server, kein Konsens |
| Persistenz | ❌ | Alles im RAM, kein Datenbank |
| Eingabe-Validierung | ⚠️ | Nur Basis-Validierung |
| HTTPS | ❌ | Keine Transport-Verschlüsselung |
| Wallet-Persistenz | ❌ | Private Keys sind nicht sicher gespeichert |

---

## ✅ Was gut funktioniert

| Feature | ✅ Gut |
|---|---|
| Digitale Signaturen | RSA-2048 ist produktionsreif |
| Hash-Integrität | SHA-256 ist kryptographisch stark |
| Ketten-Integrität | Manipulationen werden zuverlässig erkannt |
| Proof-of-Work | Grundprinzip korrekt implementiert |
| Saldo-Grundprüfung | Verhindert das Ausgeben von nicht vorhandenem Geld |

---

## 📎 Glossar

| Begriff | Bedeutung |
|---|---|
| **SHA-256** | Kryptographische Hash-Funktion (256 Bit Output) |
| **RSA-2048** | Asymmetrisches Verschlüsselungsverfahren, 2048 Bit |
| **Signatur** | Kryptographischer Beweis der Herkunft |
| **Integrität** | Garantie, dass Daten nicht verändert wurden |
| **Proof-of-Work** | Beweis durch Rechenaufwand |
| **51%-Angriff** | Theoretischer Angriff mit > 50% der Mining-Power |

---

## 📎 Siehe auch

- [04_TRANSACTIONS.md](04_TRANSACTIONS.md) – Signaturen im Detail
- [06_MINING.md](06_MINING.md) – Proof-of-Work erklärt
- [05_BLOCKCHAIN.md](05_BLOCKCHAIN.md) – Hash-Verkettung
