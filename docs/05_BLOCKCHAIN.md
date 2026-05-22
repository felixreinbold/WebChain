# 05 – Die Blockchain

## 🎯 Wofür ist das?

Eine **Blockchain** ist eine Kette von Blöcken. Jeder Block enthält Transaktionen und ist mit dem vorherigen Block verbunden – durch seinen **Hash**. Das macht es unmöglich, einen Block nachträglich zu verändern, ohne alle folgenden Blöcke ebenfalls ungültig zu machen.

---

## 🔧 Wie sind Blöcke verbunden?

```
Block 0 (Genesis)         Block 1                   Block 2
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ index: 0        │       │ index: 1        │       │ index: 2        │
│ prevHash: "0"   │       │ prevHash: ━━━━━━━━━━━▶  │ prevHash: ━━━━━━━━━━━▶
│                 │  ┌───▶│                 │  ┌───▶│                 │
│ transactions:[] │  │    │ transactions:   │  │    │ transactions:   │
│                 │  │    │  Alice→Peter    │  │    │  Peter→Nina     │
│ nonce: 107      │  │    │  10 BTC         │  │    │  20 BTC         │
│ hash: "00ba..." │──┘    │ nonce: 266      │  │    │ nonce: 512      │
└─────────────────┘       │ hash: "003e..." │──┘    │ hash: "00f1..." │
                          └─────────────────┘       └─────────────────┘
```

Jeder Block kennt den **Hash seines Vorgängers**. Ändert man Block 1, ändert sich sein Hash → Block 2's `previousHash` stimmt nicht mehr → Block 2 ist ungültig → Block 3 ist ungültig usw.

---

## 🔧 Was ist der Genesis-Block?

Der Genesis-Block ist der **erste Block** (Index 0). Er hat keine echten Vorgänger:

```java
// Genesis-Block hat als previousHash einfach "0"
Block genesisBlock = new Block(0, "0", new ArrayList<>());
```

```
Genesis Block:
┌─────────────────────────────┐
│ index:        0             │
│ previousHash: "0"           │  ← "0" statt echtem Hash
│ transactions: []            │  ← leer!
│ nonce:        107           │  (gefunden beim Mining)
│ hash:         "00ba51c9..." │  ← beginnt mit "00" (Difficulty 2)
└─────────────────────────────┘
```

---

## 🔧 Wie wächst die Blockchain?

```java
// Beim Mining:
String prevHash = blockChain.getChain()
                            .get(blockChain.getChain().size() - 1)
                            .getHash(); // Hash des letzten Blocks holen

Block newBlock = new Block(
    blockChain.getChain().size(), // Index = Länge der Kette
    prevHash,                     // previousHash = Hash des letzten Blocks
    new ArrayList<>(pendingTransactions) // Alle offenen Transaktionen
);
```

**Schritt für Schritt:**
```
Blockchain hat 1 Block (Genesis):
  Chain.size() = 1
  newBlock.index = 1
  newBlock.previousHash = genesis.hash

Nach Mining: Chain hat 2 Blöcke
  Chain.size() = 2
  nächster newBlock.index = 2
  nächster newBlock.previousHash = block1.hash
```

---

## 🔧 Block-Struktur im Detail

### Was geht in den Hash?

```java
String input = index            // 1
             + previousHash     // "00ba51c9..."
             + timestamp        // 1716300000000
             + transactionString // "Alice -> Peter: 10.0 BTC..."
             + nonce;           // 266

// SHA-256(input) → "003eb58e7048dd1c63fa415fcad1150081f19c30..."
```

**Jede Änderung an irgendeinem Feld → anderer Hash!**

| Geändertes Feld | Auswirkung |
|---|---|
| `amount` in einer Transaktion | `transactionString` ändert sich → anderer Hash |
| `timestamp` | anderer Hash |
| `nonce` | anderer Hash (wird bewusst beim Mining genutzt) |
| `previousHash` | anderer Hash (Kettenbruch!) |

---

## 💡 Beispiel: Blockchain-Validierung

Die `isValid()`-Methode prüft drei Dinge für jeden Block (außer Genesis):

```java
for (int i = 1; i < chain.size(); i++) {
    Block current  = chain.get(i);
    Block previous = chain.get(i - 1);

    // ❶ Hash-Integrität: Hat der Block seinen eigenen Hash nicht verändert?
    if (!current.calculateHash().equals(current.getHash())) {
        // Jemand hat die Transaktionen manipuliert!
        return UNGÜLTIG;
    }

    // ❷ Kettenintegrität: Zeigt der Block auf den richtigen Vorgänger?
    if (!current.getPreviousHash().equals(previous.getHash())) {
        // Die Kette ist unterbrochen!
        return UNGÜLTIG;
    }

    // ❸ Proof-of-Work: Erfüllt der Block die Mining-Bedingung?
    if (!current.getHash().startsWith("00")) { // bei difficulty=2
        // Block wurde nicht richtig gemined!
        return UNGÜLTIG;
    }
}
```

### Validierungsfehler

| Fehler | Ursache |
|---|---|
| "Block X hat ungültigen Hash" | Transaktionen oder Felder wurden manipuliert |
| "Block X zeigt auf falschen vorherigen Block" | `previousHash` stimmt nicht mehr |
| "Block X erfüllt nicht die Proof-of-Work Bedingung" | Hash beginnt nicht mit `"00"` |

---

## ⚠️ Wichtig zu wissen

**Die Blockchain ist nicht wirklich verteilt:**  
In echten Blockchains (Bitcoin, Ethereum) läuft die Blockchain auf tausenden von Computern gleichzeitig. Hier lebt sie nur im RAM dieses einen Servers. Bei Neustart ist alles weg!

**Reset-Funktion:**  
```java
public void reset() {
    this.blockChain = new BlockChain(2);  // Neue leere Blockchain
    createGenesisBlock();                  // Neuer Genesis-Block
    // initializeGenesisTransactions() wird NICHT aufgerufen!
    // → Alice, Peter, Nina bekommen beim Reset KEIN Startgeld mehr
}
```

**Genesis-Block wird nicht validiert:**  
Die `isValid()`-Schleife startet bei `i=1` – der Genesis-Block (Index 0) wird übersprungen. Das ist korrekt, da er als Anfangspunkt gilt.

---

## 📎 Glossar

| Begriff | Bedeutung |
|---|---|
| **Genesis-Block** | Der allererste Block, Startpunkt der Kette |
| **previousHash** | Referenz auf den Vorgänger-Block |
| **Hash-Integrität** | Der gespeicherte Hash muss zum Inhalt passen |
| **Kettenintegrität** | `previousHash` muss zum echten Vorgänger passen |

---

## 📎 Siehe auch

- [02_MODELS.md](02_MODELS.md) – Block-Klasse im Detail
- [06_MINING.md](06_MINING.md) – Wie Blöcke gemined werden
- [07_SERVICES.md](07_SERVICES.md) – BlockChainService erklärt
