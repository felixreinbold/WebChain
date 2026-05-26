const url = "http://localhost:8080"

async function addWallet(){

    const walletAddress = document.getElementById("walletAddress").value;
    const resultDiv = document.getElementById("result");

    if (!walletAddress || walletAddress.trim() === "") {
        alert("Wallet-Adresse erforderlich!");
        return;
    }
    let error;
    try {
    const response = await fetch(url+"/api/blockchain/wallets", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",

        },
        body:walletAddress


    }) ;

    if(!response.ok) {
        error = await response.json();
        resultDiv.innerHTML = `
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

    const newWallet = await response.json();
    console.log("Wallet erstellt:", newWallet);
        localStorage.setItem("address", newWallet.address);

        resultDiv.innerHTML = `
                    <div class="alert alert-success">
                        <h5>Wallet erstellt!</h5>
                        <p><strong>Adresse:</strong> ${newWallet.address}</p>
                        <p><strong>Balance:</strong> ${newWallet.balance} BTC</p>
                        <p><strong>Erstellt:</strong> ${new Date(newWallet.createdAt).toLocaleString()}</p>
                    </div>
                `;

        document.getElementById("walletAddress").value = "";

} catch (error) {

    console.error("Netzwerkfehler:", error);

}

}

async function getWallet(address) {

    const myWallet = document.getElementById("myWallet");

    // Validierung
    if (!address || address.trim() === "") {
        myWallet.innerHTML = `
            <div class="alert alert-danger">
                <h5>Fehler!</h5>
                <p>Wallet-Adresse erforderlich!</p>
            </div>
        `;
        return;
    }

    // Loading anzeigen
    myWallet.innerHTML = `<div class="spinner-border"></div> Wird geladen...`;

    try {
        const response = await fetch(`${url}/api/blockchain/wallets/${address}`, {
            method: "GET"
        });

        if (!response.ok) {
            const error = await response.json();
            myWallet.innerHTML = `
                <div class="alert alert-danger">
                    <h5>❌ ${error.errorCode}</h5>
                    <p>${error.message}</p>
                </div>
            `;
            return;
        }

        const wallet = await response.json();
        console.log("Wallet:", wallet);
        localStorage.setItem("address", wallet.address);

        myWallet.innerHTML = `
            <div>
                <p><strong>Adresse:</strong> ${wallet.address}</p>
                <p><strong>Balance:</strong> ${wallet.balance} BTC</p>
            </div>
        `;

    } catch (error) {
        console.error("Netzwerkfehler:", error);
        myWallet.innerHTML = `
            <div class="alert alert-danger">
                <h5>Netzwerkfehler!</h5>
                <p>${error.message}</p>
            </div>
        `;
    }
}

function logout(){
    localStorage.removeItem("address");
    const myWallet = document.getElementById("myWallet");
    myWallet.innerHTML="";

}
