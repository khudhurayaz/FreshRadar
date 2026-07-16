import {error, createAlertWarningOrDanger, pushAlertWithOffset, debug} from "./Util.js";

/**
 * Repräsentiert eine Kategorie im System.
 */
export class Category {

    /**
     * Erstellt eine neue Instanz einer Kategorie.
     * @param {string} name - Der Name der Kategorie.
     */
    constructor(name) {
        this.name = name;
    }

    /**
     * Gibt eine String-Repräsentation der Kategorie zurück.
     * @returns {string} Textuelle Darstellung der Kategorie.
     */
    toString() {
        return `name=${this.name}`;
    }
}

/**
 * Sendet eine neue Kategorie an das Backend.
 * Validiert den Namen vorab und gibt entsprechende Erfolgs- oder
 * Fehlermeldungen über die UI aus.
 * @param {Category} category - Das zu speichernde Kategorie-Objekt.
 * @throws {Error} Wirft einen Fehler, wenn die Server-Antwort fehlschlägt.
 */
async function saveCategory(category) {
    // Validierung: Der Kategoriename darf nicht leer sein oder nur aus Leerzeichen bestehen
    if (!category || !category.name.trim()) {
        console.warn("Validierungsfehler: Kategorie-Name ist leer.");
        pushAlertWithOffset(false, "Bitte geben Sie ein Kategoriename ein.", 5000);
        return;
    }

    const data = {
        name: category.name.trim()
    };

    // API-Request an das Backend absetzen
    const response = await fetch('/api/category/add', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });

    // Fehlerbehandlung bei HTTP-Statuscodes außerhalb von 2xx
    if (!response.ok) {
        const errorText = await response.text();
        error("Backend-Fehler:" + errorText);
        pushAlertWithOffset(false, "Fehlermeldung: " + errorText.message, 5000, 0);
        return;
    }

    // Wenn die Antwort erfolgreich verarbeitet wurde, Erfolgsmeldung anzeigen
    const text = await response.text();
    debug("in response.json(): " + text);
    debug("in response.status(): " + text.status);
    pushAlertWithOffset(true, 'Kategorie erfolgreich gespeichert!', 5000, 0);
}

/**
 * Initialisiert das Formular-Handling, sobald das DOM vollständig geladen ist.
 * Die Logik wird nur ausgeführt, wenn der URL-Parameter 'tab' den Wert 'createCategory' hat.
 */
document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const aktuellerTab = urlParams.get('tab');

    // Skript-Ausführung abbrechen, wenn wir uns nicht im Kategorie-Tab befinden
    if (aktuellerTab !== "createCategory") {
        return;
    }

    const elmName = document.getElementById('inputCategory');
    const categoryForm = document.getElementById('createCategoryForm');

    if (categoryForm) {
        categoryForm.addEventListener("submit", (event) => {
            // Standard-Submit des Browsers (Page Reload) verhindern
            event.preventDefault();

            const category = new Category(elmName.value);
            console.log("Erstellte Kategorie:", category.toString());

            // Kategorie speichern und UI zurücksetzen oder Fehler abfangen
            saveCategory(category).then(r => elmName.value = "")
                .catch(err => {
                        pushAlertWithOffset(false, "Es ist ein Fehler aufgetreten: " + err.message, 5000, 0)
                    }
                );
        });
    }
});