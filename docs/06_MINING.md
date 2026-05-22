# 06 – Mining & Proof-of-Work

## 🎯 Wofür ist das?

**Mining** ist der Prozess, durch den neue Blöcke zur Blockchain hinzugefügt werden. Es ist absichtlich aufwändig – der Computer muss eine bestimmte "Rätsel-Zahl" (Nonce) finden, die den Hash des Blocks mit einer bestimmten Anzahl führender Nullen beginnen lässt. Das nennt sich **Proof-of-Work**.

---

## 🔧 Was ist Proof-of-Work?

**Das Prinzip:** Finde eine Zahl (Nonce), sodass `SHA-256(BlockInhalt + Nonce)` mit X Nullen beginnt.

```
Difficulty = 2 → Hash muss mit "00" beginnen
Difficulty = 3 → Hash muss mit "000" beginnen
Difficulty = 4 → Hash muss mit "0000" beginnen
```

**Warum ist das schwierig?**  
SHA-256 ist unvorhersehbar. Es gibt keine Abkürzung – man muss einfach viele Nonces ausprobieren, bis man einen passenden Hash findet. Durchschnittlich:

```
Difficulty 1 → ~16 Versuche
Difficulty 2 → ~256 Versuche
Difficulty 3 → ~4.096 Versuche
Difficulty 4 → ~65.536 Versuche
Difficulty 5 → ~1.048.576 Versuche
```

---

## 🔧 Wie funktioniert der Mining-Prozess?

### Der Mining-Loop in BlockService

```java
public void mineBlock(Block block, int difficulty) {
    // Ziel-String erstellen (z.B. "00" für difficulty=2)
    String target = "";
    for (int i = 0; i < difficulty; i++) {
        target += "0";  // → "00"
    }

    long startTime = System.currentTimeMillis();

    // Nonce hochzählen bis der Hash stimmt
    while (!block.getHash().startsWith(target)) {
        int nonce = block.getNonce();
        block.setNonce(nonce + 1);    // Nonce: 0, 1, 2, 3, ...
        block.calculateHash();         // Neuen Hash berechnen
    }

    long endTime = System.currentTimeMillis();
    // Ausgabe: "Block 1 geminded! Nonce: 266, Zeit: 12ms"
}
```

### Visualisierung: Nonce-Suche

```
Nonce=0:   SHA-256("1" + "00ba..." + "1716300000" + "Alice..." + "0")
           → "a7f3c2..." ← beginnt NICHT mit "00" ❌

Nonce=1:   SHA-256("1" + "00ba..." + "1716300000" + "Alice..." + "1")
           → "5b9e1a..." ← beginnt NICHT mit "00" ❌

Nonce=2:   SHA-256("1" + "00ba..." + "1716300000" + "Alice..." + "2")
           → "f8d4b7..." ← beginnt NICHT mit "00" ❌

... (viele Versuche) ...

Nonce=266: SHA-256("1" + "00ba..." + "1716300000" + "Alice..." + "266")
           → "003eb5..." ← beginnt mit "00" ✅ FERTIG!
```

---

## 💡 Beispiel: Difficulty 2 vs 3

```
Difficulty = 2 (Standardwert):
  Ziel: Hash beginnt mit "00"
  Wahrscheinlichkeit pro Versuch: 1/256 ≈ 0,4%
  Typische Dauer: < 1 Sekunde

Difficulty = 3:
  Ziel: Hash beginnt mit "000"
  Wahrscheinlichkeit pro Versuch: 1/4096 ≈ 0,02%
  Typische Dauer: 1-5 Sekunden

Difficulty = 4:
  Ziel: Hash beginnt mit "0000"
  Wahrscheinlichkeit pro Versuch: 1/65536 ≈ 0,0015%
  Typische Dauer: 10-60 Sekunden
```

**Difficulty ändern via API:**
```bash
POST /api/blockchain/difficulty
{"difficulty": 3}
```

---

## 🔧 Der vollständige Mining-Ablauf

```java
public void mineBlock() {  // In BlockChainService

    // 1. Alle pending Transaktionen ausführen (Saldi aktualisieren)
    for (Transaction tx : blockChain.getPendingTransactions()) {
        walletService.executeTransaction(
            tx.getSender(), tx.getReceiver(), tx.getAmount()
        );
    }
    //    → Alice: 100 → 50 BTC (falls sie 50 überwiesen hat)
    //    → Peter: 100 → 150 BTC

    // 2. Hash des letzten Blocks holen
    String prevHash = blockChain.getChain()
                                .get(chain.size() - 1)
                                .getHash();

    // 3. Neuen Block erstellen (startet mit nonce=0)
    Block newBlock = new Block(
        chain.size(),      // Index
        prevHash,          // previousHash
        pendingTransactions // Transaktionen
    );

    // 4. Mining starten (dauert je nach Difficulty)
    blockService.mineBlock(newBlock, blockChain.getDifficulty());
    //    → Nonce wird hochgezählt bis Hash mit "00" beginnt

    // 5. Mining-Reward vergeben (10 BTC an "system")
    walletService.addReward("system", 10.0);

    // 6. Block zur Blockchain hinzufügen
    blockChain.getChain().add(newBlock);

    // 7. Pending-Liste leeren
    clearPendingTransaction();
}
```

---

## 🔧 Mining-Reward

Nach jedem erfolgreich geminten Block bekommt **"system"** 10 BTC:

```java
walletService.addReward("system", 10.0);
```

> 💡 In echten Blockchains wie Bitcoin würde der Miner-Wallet die Belohnung bekommen. Hier vereinfacht: Die `system`-Wallet (die ohnehin 10.000 BTC hat) bekommt zusätzlich 10 BTC. Das dient hier als Platzhalter für die Miner-Belohnung.

---

## ⚠️ Wichtig zu wissen

**Mining blockiert den Server!**  
Während des Minings läuft eine `while`-Schleife – der Thread ist blockiert. Bei Difficulty 4+ kann das den Server für Sekunden oder Minuten blockieren. In Produktion würde man Mining asynchron (in einem eigenen Thread) ausführen.

**Nonce startet immer bei 0:**  
Jeder neue Block startet den Mining-Prozess bei `nonce=0`. Das ist ineffizient, aber korrekt. In echten Systemen wird manchmal mit einem Zufalls-Nonce gestartet.

**Warum macht Mining Blöcke sicher?**  
Um einen alten Block zu manipulieren, müsste man:
1. Den alten Block neu hashen (Mining!)
2. Alle folgenden Blöcke ebenfalls neu minen
3. Das schneller machen als die restliche Blockchain

Bei echten Blockchains mit vielen Minern ist das praktisch unmöglich.

**Console-Ausgaben beim Mining:**
```
Mining Block 1...
Hash in Hexadezimal: a7f3c2...
Hash in Hexadezimal: 5b9e1a...
... (viele Zeilen) ...
Hash in Hexadezimal: 003eb5...
Block 1 geminded!
Nonce1266
Hash003eb58e7048dd1c63fa415fcad1150081f19c30...
Zeit: 12 ms
```

---

## 📎 Glossar

| Begriff | Bedeutung |
|---|---|
| **Mining** | Prozess zum Hinzufügen neuer Blöcke |
| **Proof-of-Work** | Beweis durch Rechenaufwand |
| **Nonce** | "Number used once" – Zahl die hochgezählt wird |
| **Difficulty** | Wie viele führende Nullen der Hash braucht |
| **Target** | Der Ziel-String (z.B. "00" bei Difficulty 2) |
| **Mining-Reward** | Belohnung für das erfolgreiche Mining |

---

## 📎 Siehe auch

- [05_BLOCKCHAIN.md](05_BLOCKCHAIN.md) – Warum Mining Blöcke sicher macht
- [07_SERVICES.md](07_SERVICES.md) – BlockService & BlockChainService
- [08_CONTROLLER.md](08_CONTROLLER.md) – `/mine` Endpoint
