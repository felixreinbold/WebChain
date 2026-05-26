const miningContainer = document.getElementById("mining-container");

async function mineBlock() {
    const minerAddress = localStorage.getItem("address");

    if(minerAddress==null){
        miningContainer.innerHTML="Bitte erst mit Wallet verbinden";
    }

    try {
        const response = await fetch(`${url}/api/blockchain/mine`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                minerAddress: minerAddress
            })
        });

        if(!response.ok) {
            error = await response.json();
            miningContainer.innerHTML = `
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



        const result = await response.json();
        console.log("Erfolg:", result);

                const newBlock = document.createElement("div");
                newBlock.className = "card mb-2";
                newBlock.innerHTML = `
            <div class="card-body">
                <h6>Block #${result.index}</h6>
                <p><strong>Hash:</strong> <code>${result.hash.substring(0, 20)}...</code></p>
                <p><strong>Transaktionen:</strong> ${result.transactions.length}</p>
                <p><strong>Nonce:</strong> ${result.nonce}</p>
                <p><small>Zeitstempel: ${new Date(result.timestamp).toLocaleString()}</small></p>
            </div>
        `;
               miningContainer.appendChild(newBlock);


        document.getElementById("minerAddress").value = "";

    } catch (error) {
        console.error("Fehler:", error);
    }
}