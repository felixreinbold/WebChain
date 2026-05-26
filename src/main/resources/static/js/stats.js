

async function getStatus(){

    const statsContainer= document.getElementById("stats-container");

    try {

        const response = await fetch(`${url}/api/blockchain/status`, {
            method: "GET"
        });
        if (!response.ok) {
            statsContainer.innerHTML = "";
            const error = await response.json();
            console.log(error);
            statsContainer.innerHTML = `
                <div class="alert alert-danger">
                    <h5>${error.errorCode}</h5>
                    <p>${error.status}</p>
                    <p>${error.message}</p>
                </div>
            `;
            return;
        }

        const status = await response.json();
        console.log("status:", status);

        statsContainer.innerHTML = `
            <div class="card">
                <div class="card-body">
                    <p><strong>Gültig:</strong> ${status.valid ? "Ja" : "Nein"}</p>
                    <p><strong>Blöcke:</strong> ${status.blockCount}</p>
                    <p><strong>Difficulty:</strong> ${status.difficulty}</p>
                    <p><strong>Transaktionen:</strong> ${status.totalTransactions}</p>
                </div>
            </div>
        `;




    }catch (error) {
        console.error("Netzwerkfehler:", error);
        statsContainer.innerHTML = "";
        statsContainer.innerHTML = `
            <div class="alert alert-danger">
                <h5>Netzwerkfehler!</h5>
                <p>${error.message}</p>
            </div>
        `;
    }

}