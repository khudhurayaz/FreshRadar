const errors = {
    // 4xx - Client-Fehler
    400: { title: "Ungültige Anfrage", text: "Die Anfrage konnte vom Server aufgrund fehlerhafter Syntax nicht verarbeitet werden." },
    401: { title: "Nicht autorisiert", text: "Für diese Anfrage ist eine Authentifizierung erforderlich. Bitte melde dich an." },
    403: { title: "Zugriff verweigert", text: "Du hast keine Berechtigung, auf diese Ressource zuzugreifen." },
    404: { title: "Seite nicht gefunden", text: "Die angeforderte Seite existiert nicht oder wurde verschoben." },
    405: { title: "Methode nicht erlaubt", text: "Die verwendete HTTP-Methode wird für diese Ressource nicht unterstützt." },
    406: { title: "Nicht akzeptabel", text: "Der angeforderte Inhaltstyp wird vom Server nicht unterstützt." },
    408: { title: "Zeitüberschreitung", text: "Die Anfrage hat zu lange gedauert. Bitte versuche es erneut." },
    409: { title: "Konflikt", text: "Die Anfrage steht im Konflikt mit dem aktuellen Zustand der Ressource." },
    410: { title: "Nicht mehr verfügbar", text: "Diese Ressource wurde dauerhaft entfernt und ist nicht mehr erreichbar." },
    413: { title: "Anfrage zu groß", text: "Die übermittelten Daten überschreiten die zulässige Größe." },
    415: { title: "Nicht unterstützter Medientyp", text: "Das übermittelte Dateiformat wird nicht unterstützt." },
    422: { title: "Fehlerhafte Eingabe", text: "Die Anfrage war korrekt aufgebaut, enthält aber ungültige Daten." },
    429: { title: "Zu viele Anfragen", text: "Du hast das Anfragelimit erreicht. Bitte versuche es später erneut." },

    // 5xx - Server-Fehler
    500: { title: "Interner Serverfehler", text: "Es ist ein unerwarteter Fehler aufgetreten. Bitte versuche es später erneut." },
    502: { title: "Fehlerhaftes Gateway", text: "Der Server hat eine ungültige Antwort von einem anderen Server erhalten." },
    503: { title: "Dienst nicht verfügbar", text: "Der Server ist momentan überlastet oder wird gewartet. Bitte versuche es später erneut." },
    504: { title: "Gateway-Zeitüberschreitung", text: "Der Server hat keine rechtzeitige Antwort von einem anderen Server erhalten." },
};

document.addEventListener("DOMContentLoaded", () => {
    const status = document.body.dataset.status;
    const path = document.body.dataset.path;
    const customMessage = document.body.dataset.message;

    const code = parseInt(status, 10);
    const e = errors[code] || { title: "Unbekannter Fehler", text: "Es ist ein unerwarteter Fehler aufgetreten." };

    document.getElementById("errorCode").textContent = code || "?";
    document.title = `${code} - ${e.title}`;
    document.getElementById("errorTitle").textContent = e.title;

    // Nutzt die spezifische Message, falls vorhanden, sonst den generischen Text
    document.getElementById("errorText").textContent =
        (customMessage && customMessage !== "null" && customMessage !== "") ? customMessage : e.text;

    document.getElementById("errorPath").textContent = path || "/unbekannt";
});