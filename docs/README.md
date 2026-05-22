# 🔗 WebChain – Blockchain Backend

Willkommen zur Dokumentation des **WebChain** Blockchain-Backends!  
Dieses Projekt ist eine lehrreiche Implementierung einer echten Blockchain mit Java & Spring Boot.

---

## 🚀 Was ist WebChain?

WebChain ist ein vollständiges Blockchain-Backend, das die wichtigsten Konzepte einer echten Blockchain umsetzt:

| Feature | Beschreibung |
|---|---|
| ⛓️ Blockchain | Verkettete Blöcke mit SHA-256-Hashes |
| ⛏️ Mining | Proof-of-Work mit einstellbarer Schwierigkeit |
| 💸 Transaktionen | Signierte & verifizierte Überweisungen |
| 👛 Wallets | RSA-2048-verschlüsselte Wallets |
| 🌐 REST API | 11 Endpoints via Spring Boot |

---

## 📁 Projektstruktur

```
src/main/java/felix/webchain/
│
├── model/              ← Daten-Klassen
│   ├── Wallet.java         Wallet mit RSA-Keys
│   ├── Transaction.java    Eine Überweisung
│   ├── Block.java          Ein Block in der Kette
│   └── BlockChain.java     Die gesamte Blockchain
│
├── service/            ← Geschäftslogik
│   ├── WalletService.java      Wallets verwalten & signieren
│   ├── BlockService.java       Mining-Logik
│   └── BlockChainService.java  Blockchain orchestrieren
│
├── controller/         ← REST API
│   └── BlockChainController.java   Alle HTTP-Endpoints
│
└── dto/                ← Daten-Transfer-Objekte
    ├── TransactionDTO.java
    ├── BlockDTO.java
    ├── StatusDTO.java
    ├── ValidationResponseDTO.java
    ├── WalletDTO.java
    └── DifficultyDTO.java
```

---

## ⚡ Quick-Start

### Voraussetzungen
- Java 17+
- Maven

### Starten

```bash
# Option 1: Maven
./mvnw spring-boot:run

# Option 2: JAR direkt
java -jar target/WebChain-0.0.1-SNAPSHOT.jar
```

Der Server läuft auf: **http://localhost:8080**

### Erster Test

```bash
# Blockchain-Status abfragen
curl http://localhost:8080/api/blockchain/status

# Erste Transaktion hinzufügen (Alice hat 100 BTC nach Genesis-Mining)
curl -X POST http://localhost:8080/api/blockchain/transaction \
  -H "Content-Type: application/json" \
  -d '{"sender":"Alice","receiver":"Peter","amount":10.0}'

# Block minen
curl -X POST http://localhost:8080/api/blockchain/mine
```

---

## 📚 Alle Dokumente

| Datei | Thema |
|---|---|
| [01_ARCHITECTURE.md](01_ARCHITECTURE.md) | Gesamtarchitektur & Schichten |
| [02_MODELS.md](02_MODELS.md) | Model-Klassen erklärt |
| [03_WALLET_SYSTEM.md](03_WALLET_SYSTEM.md) | Wallets & Saldo-Verwaltung |
| [04_TRANSACTIONS.md](04_TRANSACTIONS.md) | Transaktionen & Signaturen |
| [05_BLOCKCHAIN.md](05_BLOCKCHAIN.md) | Blockchain & Blöcke |
| [06_MINING.md](06_MINING.md) | Mining & Proof-of-Work |
| [07_SERVICES.md](07_SERVICES.md) | Service-Layer Logik |
| [08_CONTROLLER.md](08_CONTROLLER.md) | REST API Endpoints |
| [09_FLOW_EXAMPLES.md](09_FLOW_EXAMPLES.md) | Komplette Szenarien |
| [10_SECURITY.md](10_SECURITY.md) | Sicherheits-Features |

---

## 🎯 Vorbelegte Wallets beim Start

Beim Start erstellt das System automatisch:

| Wallet | Startguthaben | Zweck |
|---|---|---|
| `Alice` | 0 BTC (bekommt 100 nach Genesis-Mining) | Test-Nutzer |
| `Peter` | 0 BTC (bekommt 100 nach Genesis-Mining) | Test-Nutzer |
| `Nina` | 0 BTC (bekommt 100 nach Genesis-Mining) | Test-Nutzer |
| `system` | 10.000 BTC | Ausgabe von Genesis-BTC |

> ⚠️ **Wichtig:** Die Genesis-Transaktionen (system → Alice/Peter/Nina je 100 BTC) werden erst nach dem **ersten Mining** aktiv!

---

## 🔌 API Basis-URL

```
http://localhost:8080/api/blockchain
```

---

## 📝 Hinweise

- ⚠️ Dies ist eine **Lernimplementierung** – nicht für Produktion geeignet
- 💾 Die Blockchain lebt im RAM – nach Neustart ist alles weg
- 🔄 Mit `POST /reset` kann alles zurückgesetzt werden
