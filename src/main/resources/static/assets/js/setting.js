import {debug, error, info, pushAlertWithOffset} from "./Util.js";

document.addEventListener("DOMContentLoaded", () => {
    const oldPassword = document.getElementById("oldPassword");
    const newPassword = document.getElementById("newPassword");
    const repeatPassword = document.getElementById("repeatPassword");
    const passwordForm = document.getElementById("passwordForm");

    passwordForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const inputs = [
            { input: oldPassword, text: "Altes passwort ist leer!", type: "password" },
            { input: newPassword, text: "Neues passwort ist leer!", type: "password" },
            { input: repeatPassword, text: "Passwort wiederholen ist leer!", type: "password" }
        ];

        let offsetIndex = 0;
        const alertVisibleTime = 3000;
        const offsetStep = 500;
        let hasErrors = false;

        inputs.forEach(({ input, text, type }) => {
            if (!input) return;
            if (input.value.trim() === "") {
                hasErrors = true;

                input.classList.add("error");

                const offset = offsetIndex * offsetStep;
                pushAlertWithOffset(false, text, alertVisibleTime, offset);
                offsetIndex++;

                if (type === "password") {
                    input.addEventListener("input", function () {
                        if (input.value.trim() !== "") {
                            input.classList.remove("error");
                        }
                    }, { once: true });
                }
            }
        });
        const formData = {
            "oldPassword": oldPassword.value,
            "newPassword": newPassword.value,
            "repeatPassword": repeatPassword.value
        };

        try {
            const response = await fetch("/setting/changePassword", {
                method: "POST",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });
            if (!response.ok) {
                const errorText = await response.text();
                error("Backend-Fehler:", errorText);
                throw new Error("API-Fehler: " + response.status);
            } else {
                pushAlertWithOffset(
                    true,
                    "Passwort erfolgreich geändert!",
                    5000,
                    0
                );
            }
        } catch (err) {
            error(err);
        }
    });

    const paginationForm = document.getElementById("paginationSettingForm");
    paginationForm.addEventListener("submit", async ev => {
        ev.preventDefault();

        const data = document.getElementById("globalPageSize");
        let optionSelectedValue = data.options[data.selectedIndex].text;
        try {
            const response = await fetch(`/setting/pagination?globalPageSize=${encodeURIComponent(optionSelectedValue)}`, {
                method: "POST"
            });
            const responseText = await response.text();
            if (!response.ok) {
                error("Backend-Fehler:", responseText);
                pushAlertWithOffset(false, responseText, 5000, 0);
                throw new Error("API-Fehler: " + response.status);
            } else {
                pushAlertWithOffset(true,responseText, 3000, 0);
            }
        } catch (err) {
            error(err);
        }
    });
});