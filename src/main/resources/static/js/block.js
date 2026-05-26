

async function getBlocks(){

    const blocksDiv= document.getElementById("blocks");

    blocksDiv.innerHTML = `<div class="spinner-border"></div> Wird geladen...`;

    try {

        const response = await fetch(`${url}/api/blockchain/blocks`, {
            method: "GET"
        });
        if (!response.ok) {
            blocksDiv.innerHTML = "";
            const error = await response.json();
            console.log(error);
            blocksDiv.innerHTML = `
                <div class="alert alert-danger">
                    <h5>${error.errorCode}</h5>
                    <p>${error.status}</p>
                    <p>${error.message}</p>
                </div>
            `;
            return;
        }

        const blocks = await response.json();
        console.log("Blocks:", blocks);

        blocksDiv.innerHTML = "";
        for (let i = 0; i < blocks.length; i++) {
            const newBlock = document.createElement("div");
            newBlock.className = "block";
            newBlock.innerHTML = `
                <div class="block-index">Block #${blocks[i].index}</div>
                <h6>Hash</h6>
                <p class="block-hash">${blocks[i].hash}</p>
                <h6>Transaktionen</h6>
                <p>${blocks[i].transactions.length}</p>
                <h6>Nonce</h6>
                <p>${blocks[i].nonce}</p>
            `;
            blocksDiv.appendChild(newBlock);
        }


    }catch (error) {
        console.error("Netzwerkfehler:", error);
        blocksDiv.innerHTML = "";
        blocksDiv.innerHTML = `
            <div class="alert alert-danger">
                <h5>Netzwerkfehler!</h5>
                <p>${error.message}</p>
            </div>
        `;
    }

}