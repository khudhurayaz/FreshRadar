import {createAlert, createAlertWarningOrDanger, pushAlertWithOffset} from "./Util.js";

export class Location {
    constructor(name) {
        this.name = name;
    }

    toString() {
        return `name=${this.name}`;
    }
}

async function saveLocation(location) {
    if (!location || !location.name.trim()) {
        console.log("Lagerort ist leer!");
        pushAlertWithOffset(false, "Bitte geben Sie einen Lagerort ein.", 5000, 0);
        return;
    }

    const data = {
        location: location.name.trim()
    };

    const response = await fetch('/api/location/add', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        const error = await response.text();
        console.error("Backend-Fehler:", error);
        throw new Error("Server-Fehler (" + response.status + ")");
    }

    const responseText = await response.text();

    if (!responseText) {
        pushAlertWithOffset(true, 'Lagerort erfolgreich gespeichert!', 5000, 0);
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const aktuellerTab = urlParams.get('tab');

    if (aktuellerTab !== "createLocation") {
        return;
    }

    const elmName = document.getElementById('inputLocation');
    const locationForm = document.getElementById('createLocationForm');
    if (locationForm) {
        locationForm.addEventListener("submit", (event) => {
            event.preventDefault();

            const location = new Location(elmName.value);
            console.log(location.toString());

            saveLocation(location)
                .then(() => {
                    elmName.value = "";
                })
                .catch(err => {
                    createAlertWarningOrDanger('danger', "Es ist ein Fehler aufgetreten: " + err.message, 5000);
                });
        });
    }
});