import {pushAlertWithOffset, error, debug} from './Util.js';

const filePlaceholder = document.getElementById("filePlaceholder");
const logoFile = document.getElementById("logoFile");
const imageUpload = document.querySelector(".imageUpload");
const existingProfileImageInput = document.querySelector('input[name="profileImage"]');

if (logoFile && filePlaceholder) {
    logoFile.addEventListener("change", function (event) {
        event.preventDefault();

        const file = logoFile.files[0];
        if (!file) return;

        const allowedTypes = ["image/png", "image/jpeg"];
        if (!allowedTypes.includes(file.type)) {
            if (imageUpload) imageUpload.classList.add("error");
            pushAlertWithOffset(false, "Bitte nur PNG, JPG oder JPEG hochladen.", 5000, 0);
            error("Bitte nur PNG, JPG oder JPEG hochladen.");
            logoFile.value = "";
            return;
        }

        const img = filePlaceholder.querySelector("img");
        if (img) {
            img.src = URL.createObjectURL(file);
        }

        const text = document.getElementById("create-project-image-text");
        if (text) {
            text.textContent = "Logo ausgewählt";
        }

        if (imageUpload) {
            imageUpload.classList.remove("error");
        }

        pushAlertWithOffset(true, "Logo ausgewählt!", 5000, 0);
    });
}

const updateProfileForm = document.getElementById("updateProfile");

if (updateProfileForm) {
    updateProfileForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const inputFirstname = document.getElementById("inputFirstname");
        const inputLastname = document.getElementById("inputLastname");
        const inputArea = document.getElementById("inputArea");
        const inputInfo = document.getElementById("inputInfo");
        const inputLocation = document.getElementById("inputLocation");
        const inputEmail = document.getElementById("inputEmail");

        const existingProfileImage = (existingProfileImageInput && existingProfileImageInput.value.trim() !== "")
            ? existingProfileImageInput.value.trim()
            : "/assets/images/profile/default/defaultProfile.png";

        debug("existingProfileImage: " + existingProfileImage);

        const inputs = [
            { input: inputFirstname, text: "Vorname ist leer!", type: "text" },
            { input: inputLastname, text: "Nachname ist leer!", type: "text" },
            { input: inputArea, text: "Beruf ist leer!", type: "text" },
            { input: inputInfo, text: "Information ist leer!", type: "text" },
            { input: inputLocation, text: "Wohnort ist leer!", type: "text" },
            { input: inputEmail, text: "E-Mail ist leer!", type: "email" },
            { input: logoFile, text: "Logo ist leer!", type: "file" }
        ];

        const formData = new FormData();
        formData.append("firstname", inputFirstname ? inputFirstname.value.trim() : "");
        formData.append("lastname", inputLastname ? inputLastname.value.trim() : "");
        formData.append("area", inputArea ? inputArea.value.trim() : "");
        formData.append("info", inputInfo ? inputInfo.value.trim() : "");
        formData.append("location", inputLocation ? inputLocation.value.trim() : "");
        formData.append("email", inputEmail ? inputEmail.value.trim() : "");
        formData.append("existingProfileImage", existingProfileImage);

        if (logoFile && logoFile.files.length > 0) {
            formData.append("logoFile", logoFile.files[0]);
        }

        try {
            const response = await fetch("/api/profile/edit/update", {
                method: "POST",
                body: formData
            });

            if (!response.ok) {
                const errorText = await response.text();
                error("Backend-Fehler:", errorText);
                pushAlertWithOffset(false, "Profil konnte nicht gespeichert werden.", 5000, 0);
                throw new Error("API-Fehler: " + response.status);
            }

            pushAlertWithOffset(
                true,
                "Profil erfolgreich gespeichert!",
                5000,
                0
            );
        } catch (err) {
            console.error(err);
        }
    });
}
