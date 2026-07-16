import {error, info, debug, warn, pushAlertWithOffset} from './Util.js';
import {createAlert} from "./Util.js";

/**
 * Verarbeitet das Absenden des Kontaktformulars.
 * Validiert die Eingaben, sendet die Daten an die API und setzt das Formular bei Erfolg zurück.
 * @param {Event} e - Das Submit-Event des Formulars.
 * @returns {Promise<void>}
 */
async function insertContact(e) {
    // Standard-Submit des Browsers (Seiten-Reload) verhindern
    e.preventDefault();

    // Formulardaten aus dem DOM auslesen
    const firstName = document.getElementById('inputFirstName').value;
    const lastName = document.getElementById('inputLastName').value;
    const email = document.getElementById('inputEmail').value;
    const subject = document.getElementById('inputSubject').value;
    const message = document.getElementById('inputMessage').value;

    const payload = {
        firstName,
        lastName,
        email,
        subject,
        message,
    };

    // Validierung: Es müssen alle Felder ausgefüllt sein
    if (!firstName || !lastName || !email || !subject || !message) {
        pushAlertWithOffset(false, 'Nachricht kann nicht gesendet werden, wenn die Felder nicht ausgefüllt sind!', 5000, 0);
        error("[DEBUG][Contact.js] Validierungsfehler: Unvollständige Felder.");
        return;
    }

    debug("[DEBUG][Contact.js] Nachricht senden...", payload);

    try {
        const response = await fetch(`/api/contact/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            pushAlertWithOffset(true, 'Nachricht erfolgreich gesendet!', 5000, 0);
            info("[DEBUG][contact.js] Nachricht erfolgreich gesendet!");
        } else {
            const errorText = await response.text();
            pushAlertWithOffset(false, `Fehler beim Senden (${response.status}).`, 5000, 0);
            error("[DEBUG][contact.js] Backend-Fehler:", response.status, errorText);
            return;
        }

        // Formular leicht verzögert zurücksetzen, damit der User die Erfolgsmeldung registrieren kann
        setTimeout(() => {
            const form = document.getElementById('contactForm');
            if (form) form.reset();
        }, 5100);

    } catch (error) {
        // Fängt Netzwerk- oder Server-Ausfälle ab
        createAlert(false, "Verbindungsfehler. Bitte versuche es erneut.", 5000);
        error("[DEBUG][contact.js] Netzwerkfehler beim Fetch-Request:", error);
    }
}

// Event-Listener an das Formular binden
const contactForm = document.getElementById('contactForm');
if (contactForm) {
    contactForm.addEventListener('submit', insertContact);
} else {
    warn("[DEBUG][contact.js] #contactForm konnte nicht im DOM gefunden werden.");
}