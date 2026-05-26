const pendingTransactionContainer = document.getElementById("pending-transaction-container");
const feedbackContainer = document.getElementById("transaction-feedback-container");

async function getPendingTransactions(){

    pendingTransactionContainer.innerHTML = `<div class="spinner-border"></div> Wird geladen...`;

    try {

        const response = await fetch(`${url}/api/blockchain/pending-transactions`, {
            method: "GET"
        });

        if (!response.ok) {
            const error = await response.json();
            console.log(error);
            pendingTransactionContainer.innerHTML = `
                <div class="alert alert-danger">
                    <h5>${error.errorCode}</h5>
                    <p>${error.message}</p>
                </div>
            `;
            return;
        }

        const pendingTransactions = await response.json();
        console.log("Pending-Transactions:", pendingTransactions);

        pendingTransactionContainer.innerHTML = "";  // ← Kleinbuchstaben!

        if (!pendingTransactions || pendingTransactions.length === 0) {
            pendingTransactionContainer.innerHTML = `<div class="alert alert-info">Keine ausstehenden Transaktionen</div>`;
            return;
        }

        for (let i = 0; i < pendingTransactions.length; i++) {
            const tx = pendingTransactions[i];  // ← Abkürzung für Klarheit
            const newTx = document.createElement("div");
            newTx.className = "card mb-2";
            newTx.innerHTML = `
                <div class="card-body">
                    <h6>Transaktion #${i + 1}</h6>
                    <p><strong>Sender:</strong> ${tx.sender}</p>
                    <p><strong>Empfänger:</strong> ${tx.receiver}</p>
                    <p><strong>Betrag:</strong> ${tx.amount} BTC</p>
                    <p><strong>Signature:</strong> <code>${tx.signature.substring(0, 20)}...</code></p>
                    <p><strong>Verified:</strong> ${tx.verified ? "True" : "False"}</p>
                </div>
            `;
            pendingTransactionContainer.appendChild(newTx);
        }

    } catch (error) {
        console.error("Netzwerkfehler:", error);
        pendingTransactionContainer.innerHTML = `
            <div class="alert alert-danger">
                <h5>Netzwerkfehler!</h5>
                <p>${error.message}</p>
            </div>
        `;
    }

}

const transactionContainer = document.getElementById("transaction-container");

async function addTransaction() {
    // Input-Werte auslesen
    const sender = document.getElementById("sender").value;
    const receiver = document.getElementById("receiver").value;
    const amount = document.getElementById("amount").value;

    // Validierung
    if (!sender || !receiver || !amount) {
        console.log("Alle Felder erforderlich!");
        feedbackContainer.innerHTML = `
                    <div class="alert alert-error">
                        <h5>Alle Felder erforderlich!</h5>
                    </div>
                `;
        return;
    }

    try {
        // POST-Request
        const response = await fetch(`${url}/api/blockchain/transaction`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                sender: sender,
                receiver: receiver,
                amount: parseFloat(amount)
            })
        });

        // Error-Handling
        if (!response.ok) {
            const error = await response.json();
            feedbackContainer.innerHTML = `
                    <div class="alert alert-error">
                        <h5>Fehler!</h5>
                        <p><strong>Status:</strong> ${error.status}</p>
                        <p><strong>message:</strong> ${error.message} BTC</p>
                        <p><strong>errorCode:</strong> ${error.errorCode} BTC</p>
                        <p><strong>timestamp:</strong> ${new Date(error.createdAt).toLocaleString()}</p>
                    </div>
                `;
            return;
        }


        document.getElementById("sender").value = "";
        document.getElementById("receiver").value = "";
        document.getElementById("amount").value = "";


        console.log("Transaktion erfolgreich hinzugefügt!");
        feedbackContainer.innerHTML = `
                    <div class="alert alert-success">
                        <h5>Transaktion Erfolgreich!</h5>
                        <p>Betrag wurde erfolgreich transferiert!</p>
                    </div>
                `;

        getPendingTransactions();

    } catch (error) {
        console.error("Netzwerkfehler:", error);
        alert(" Fehler: " + error.message);
    }
}