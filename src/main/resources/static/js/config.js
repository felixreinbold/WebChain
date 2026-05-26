


async function setDifficulty() {
    // Input-Werte auslesen
    const difficultyInput = document.getElementById("difficulty-input").value;
    const feedbackContainer = document.getElementById("config-feedback-container");

    // Validierung
    if (!difficultyInput) {
        console.log("Difficulty Input fehlt!");
        feedbackContainer.innerHTML = `
                    <div class="alert alert-error">
                        <h5>Alle Felder erforderlich!</h5>
                    </div>
                `;
        return;
    }

    try {
        // POST-Request
        const response = await fetch(`${url}/api/blockchain/difficulty`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                difficulty: parseInt(difficultyInput)
            })
        });

        if (!response.ok) {
            const error = await response.json();
            feedbackContainer.innerHTML = `
                    <div class="alert alert-error">
                        <h5>Alle Felder erforderlich!</h5>
                        <p><strong>Status:</strong> ${error.status}</p>
                        <p><strong>message:</strong> ${error.message}</p>
                        <p><strong>errorCode:</strong> ${error.errorCode}</p>
                        <p><strong>timestamp:</strong> ${new Date(error.createdAt).toLocaleString()}</p>
                    </div>
                `;
            return;
        }


        document.getElementById("difficulty-input").value = "";



        console.log("Schwierigkeit gesetzt!");
        feedbackContainer.innerHTML = `
                    <div class="alert alert-success">
                        <h5>Schwierigkeit gesetzt!</h5>
                    </div>
                `;

        getStatus();

    } catch (error) {
        console.error("Netzwerkfehler:", error);
        alert(" Fehler: " + error.message);
    }
}