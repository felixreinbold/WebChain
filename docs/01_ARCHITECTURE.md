# 01 – Architektur

## 🎯 Wofür ist das?

Hier bekommst du den **Gesamtüberblick**: Wie ist das Projekt aufgebaut? Welche Teile gibt es? Wie hängen sie zusammen?

---

## 🔧 Die 3 Schichten

Das Projekt folgt einem klassischen **3-Schichten-Modell**:

```
┌─────────────────────────────────────────┐
│           HTTP-Anfrage (Browser/Postman) │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         CONTROLLER-SCHICHT              │
│    BlockChainController.java            │
│  • Nimmt HTTP-Anfragen entgegen         │
│  • Gibt HTTP-Antworten zurück           │
│  • Kennt nur Services, keine Models     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         SERVICE-SCHICHT                 │
│  BlockChainService / WalletService      │
│  BlockService                           │
│  • Die eigentliche Geschäftslogik       │
│  • Entscheidet: Was darf passieren?     │
│  • Orchestriert Models                  │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         MODEL-SCHICHT                   │
│  Wallet / Transaction / Block /         │
│  BlockChain                             │
│  • Reine Datenhaltung                   │
│  • Einfache Berechnungen (z.B. Hash)    │
│  • Kein Wissen über Services            │
└─────────────────────────────────────────┘
```

---

## 🔧 Wie funktioniert es?

### Die Klassen im Überblick

```
felix.webchain/
│
├── model/
│   ├── Wallet          → Hat Adresse, Saldo, RSA-Keys
│   ├── Transaction     → Eine Überweisung (Sender → Empfänger : Betrag)
│   ├── Block           → Container für Transaktionen + Krypto-Hash
│   ├── BlockChain      → Liste aller Blöcke + ausstehende Transaktionen
│   └── Account         → (Hilfsklasse, noch nicht vollständig genutzt)
│
├── service/
│   ├── WalletService   → Wallets erstellen, signieren, Saldo prüfen
│   ├── BlockService    → Einen einzelnen Block minen
│   └── BlockChainService → Alles orchestrieren (die "Zentrale")
│
├── controller/
│   └── BlockChainController → REST-Endpoints, leitet an Services weiter
│
└── dto/
    ├── TransactionDTO  → JSON-Format für eingehende Transaktionen
    ├── BlockDTO        → JSON-Format für ausgehende Blöcke
    ├── StatusDTO       → Blockchain-Status als JSON
    ├── ValidationResponseDTO → Ergebnis der Validierung
    ├── WalletDTO       → Wallet-Info ohne private Keys
    └── DifficultyDTO   → Difficulty-Änderung als JSON
```

### Datenfluss – Beispiel: Transaktion hinzufügen

```
POST /api/blockchain/transaction
        │
        ▼
BlockChainController.addTransaction()
    → erstellt Transaction-Objekt aus DTO
        │
        ▼
BlockChainService.addPendingTransaction()
    → WalletService.signTransaction()     ← Signatur erstellen
    → WalletService.validateTransaction() ← Signatur prüfen
    → WalletService.canTransact()         ← Saldo prüfen
    → blockChain.getPendingTransactions().add(tx)
        │
        ▼
HTTP Response: "Transaktion hinzugefügt!" (201)
```

---

## 💡 Beispiel: Komplett-Überblick beim Start

Wenn der Server startet, passiert folgendes automatisch:

```java
// 1. BlockChainService wird erstellt
new BlockChainService()
    → new BlockChain(difficulty=2)      // Leere Blockchain, Difficulty = 2
    → createGenesisBlock()              // Ersten Block minen
    → initializeGenesisTransactions()   // System → Alice/Peter/Nina je 100 BTC
                                        // (als Pending, noch nicht gemined)
```

```
Zustand nach Start:
┌─────────────────────────────┐
│ BLOCKCHAIN                  │
│  Block[0] = Genesis         │
│  Difficulty = 2             │
│                             │
│ Pending Transactions:       │
│  system → Alice : 100 BTC   │
│  system → Peter : 100 BTC   │
│  system → Nina  : 100 BTC   │
└─────────────────────────────┘
```

---

## ⚠️ Wichtig zu wissen

**Zwei separate WalletService-Instanzen!**

Im Controller gibt es aktuell **zwei unabhängige Instanzen**:

```java
// In BlockChainController:
private BlockChainService blockChainService = new BlockChainService();
// BlockChainService hat intern seinen eigenen WalletService!

private WalletService walletService = new WalletService();
// Das ist ein ZWEITER, unabhängiger WalletService!
```

Das bedeutet: Die Wallets über `/api/blockchain/wallets` sind **nicht dieselben** wie die Wallets, die für Transaktionen genutzt werden. Das ist ein bekanntes Design-Problem im aktuellen Code.

**DTOs – warum?**

DTOs (Data Transfer Objects) trennen das interne Datenmodell vom API-Format:
- `Wallet` hat private Keys → darf nie direkt als JSON zurückgegeben werden!
- `WalletDTO` hat nur: `address`, `balance`, `createdAt` → sicher nach außen

---

## 📎 Siehe auch

- [02_MODELS.md](02_MODELS.md) – Alle Klassen im Detail
- [07_SERVICES.md](07_SERVICES.md) – Service-Logik erklärt
- [08_CONTROLLER.md](08_CONTROLLER.md) – Alle Endpoints
