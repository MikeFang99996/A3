const apiBaseUrl = "http://localhost:8081";

const inputArea = document.getElementById('inputArea');
const screen = document.getElementById('screen');

function addToInput(value) {
    if (inputArea.value) {
        inputArea.value += ' ';
    }
    inputArea.value += value;
}

async function submitInput() {
    const inputText = inputArea.value;
    if (inputText) {
        screen.textContent = inputText;
        inputArea.value = '';
    }
}

async function updateScreen(logs) {
    screen.textContent = logs;
}

async function fetchGameStatus() {
    const response = await fetch('http://localhost:8081/api/game/status');
    const status = await response.json();
    updateScreen(JSON.stringify(status, null, 2));
}

async function playTurn() {
    try {
        const response = await fetch(`${apiBaseUrl}/api/game/start`, { method: "GET",
            headers: {
                "Content-Type": "application/json",  // Optional, depending on your API's content type
                "Access-Control-Allow-Origin": "http://localhost:3000", // Specify the origin of your frontend
                'Access-Control-Allow-Credentials': 'true'
            }});
        const result = await response.text();
        console.log("Stand Response:", result);

    } catch (error) {
        console.error("Error in stand:", error);
    }
}