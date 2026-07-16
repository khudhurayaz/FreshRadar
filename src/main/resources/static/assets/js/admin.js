import {pushAlertWithOffset} from './Util.js';

document.addEventListener("DOMContentLoaded", () => {
    toggleBenutzer();
    toggleProducts();
    deleteMessage();
});

function toggleBenutzer(){
    const deleteModalElement = document.getElementById("deleteProfileModal");
    const deleteProfileName = document.getElementById("deleteProfileName");
    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");

    const deleteModal = new bootstrap.Modal(deleteModalElement);

    let currentDeleteProfileId = null;
    let currentDeleteRow = null;

    let offsetIndex = 0;
    const alertVisibleTime = 5000;
    const offsetStep = 500;

    document.querySelectorAll("tr[data-profile-id]").forEach((row) => {
        const editBtn = row.querySelector(".btn-edit-toggle");
        const saveBtn = row.querySelector(".btn-save");
        const cancelBtn = row.querySelector(".btn-cancel");
        const deleteBtn = row.querySelector(".btn-delete");

        const viewModes = row.querySelectorAll(".view-mode");
        const editModes = row.querySelectorAll(".edit-mode");

        const initialValues = {
            firstname: row.querySelector(".edit-firstname")?.value || "",
            lastname: row.querySelector(".edit-lastname")?.value || "",
            area: row.querySelector(".edit-area")?.value || "",
            email: row.querySelector(".edit-email")?.value || "",
            info: row.querySelector(".edit-info")?.value || "",
            location: row.querySelector(".edit-location")?.value || ""
        };

        editBtn.addEventListener("click", () => {
            viewModes.forEach(el => el.classList.add("d-none"));
            editModes.forEach(el => el.classList.remove("d-none"));

            editBtn.classList.add("d-none");
            saveBtn.classList.remove("d-none");
            cancelBtn.classList.remove("d-none");
        });

        cancelBtn.addEventListener("click", () => {
            row.querySelector(".edit-firstname").value = initialValues.firstname;
            row.querySelector(".edit-lastname").value = initialValues.lastname;
            row.querySelector(".edit-area").value = initialValues.area;
            row.querySelector(".edit-email").value = initialValues.email;
            row.querySelector(".edit-info").value = initialValues.info;
            row.querySelector(".edit-location").value = initialValues.location;

            editModes.forEach(el => el.classList.add("d-none"));
            viewModes.forEach(el => el.classList.remove("d-none"));

            saveBtn.classList.add("d-none");
            cancelBtn.classList.add("d-none");
            editBtn.classList.remove("d-none");
        });

        saveBtn.addEventListener("click", async () => {
            const profileId = row.dataset.profileId;

            const payload = {
                id: Number(row.dataset.profileId),
                user: {
                    email: row.querySelector(".edit-email")?.value
                },
                firstname: row.querySelector(".edit-firstname")?.value,
                lastname: row.querySelector(".edit-lastname")?.value,
                area: row.querySelector(".edit-area")?.value,
                info: row.querySelector(".edit-info")?.value,
                location: row.querySelector(".edit-location")?.value,
            };
            console.log("firstname:", row.querySelector(".edit-firstname")?.value);
            console.log("lastname:", row.querySelector(".edit-lastname")?.value);
            console.log("area:", row.querySelector(".edit-area")?.value);
            console.log("email:", row.querySelector(".edit-email")?.value);
            console.log("info:", row.querySelector(".edit-info")?.value);
            console.log("location:", row.querySelector(".edit-location")?.value);

            try {
                const response = await fetch(`/admin/profile/${profileId}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    offsetIndex++;
                    pushAlertWithOffset(false, `Fehler beim Speichern: ${response.status}`, alertVisibleTime, offsetIndex*offsetStep);
                    throw new Error(`Fehler beim Speichern: ${response.status}`);
                }

                row.querySelector(".profile-fullname").textContent = `${payload.firstname} ${payload.lastname}`;
                row.querySelector(".profile-area").textContent = payload.area;
                row.querySelector(".profile-email").textContent = payload.user.email;
                row.querySelector(".profile-info").textContent = payload.info;
                row.querySelector(".profile-location").textContent = payload.location;

                initialValues.firstname = payload.firstname;
                initialValues.lastname = payload.lastname;
                initialValues.area = payload.area;
                initialValues.email = payload.email;
                initialValues.info = payload.info;
                initialValues.location = payload.location;

                editModes.forEach(el => el.classList.add("d-none"));
                viewModes.forEach(el => el.classList.remove("d-none"));

                saveBtn.classList.add("d-none");
                cancelBtn.classList.add("d-none");
                editBtn.classList.remove("d-none");
                offsetIndex++;
                pushAlertWithOffset(true, "Profile erfolgreich gespeichert!", alertVisibleTime, offsetIndex*offsetStep);
            } catch (error) {
                console.error(error);
                offsetIndex++;
                pushAlertWithOffset(false, `Speichern fehlgeschlagen.`, alertVisibleTime, offsetIndex*offsetStep);
            }
        });



        deleteBtn.addEventListener("click", () => {
            currentDeleteProfileId = deleteBtn.dataset.profileId;
            currentDeleteRow = row;
            deleteProfileName.textContent = deleteBtn.dataset.profileName;
            deleteModal.show();
        });
    });

    confirmDeleteBtn.addEventListener("click", async () => {
        if (!currentDeleteProfileId) return;

        try {
            const response = await fetch(`/admin/profile/${currentDeleteProfileId}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                offsetIndex++;
                pushAlertWithOffset(false, "Fehler beim Löschen " + response.status,  alertVisibleTime, offsetIndex*offsetStep)
                throw new Error(`Fehler beim Löschen: ${response.status}`);
            }

            currentDeleteRow.remove();
            deleteModal.hide();

            pushAlertWithOffset(true, "Benutzer erfolgreich gelöscht!", alertVisibleTime, offsetIndex * offsetStep);
        } catch (error) {
            console.error(error);
            offsetIndex++;
            pushAlertWithOffset(false, "Löschen fehlgeschlagen",  alertVisibleTime, offsetIndex*offsetStep);
        }
    });
}

function toggleProducts() {
    const deleteModalElement = document.getElementById("deleteProductModal");
    const deleteProductName = document.getElementById("deleteProductName");
    const confirmDeleteBtn = document.getElementById("confirmDeleteProductBtn");

    const deleteModal = new bootstrap.Modal(deleteModalElement);

    let currentDeleteProductId = null;
    let currentDeleteRow = null;

    let offsetIndex = 0;
    const alertVisibleTime = 5000;
    const offsetStep = 500;

    document.querySelectorAll("tr[data-product-id]").forEach((row) => {
        const editBtn = row.querySelector(".btn-edit-toggle-product");
        const saveBtn = row.querySelector(".btn-save-product");
        const cancelBtn = row.querySelector(".btn-cancel-product");
        const deleteBtn = row.querySelector(".btn-delete-product");

        const viewModes = row.querySelectorAll(".view-mode");
        const editModes = row.querySelectorAll(".edit-mode");

        const initialValues = {
            productName: row.querySelector(".edit-product-name")?.value || "",
            unit: row.querySelector(".edit-unit")?.value || "",
            userId: row.querySelector(".edit-user-id")?.value || "",
            locationId: row.querySelector(".edit-location-id")?.value || "",
            categoryId: row.querySelector(".edit-category-id")?.value || "",
            isOpen: row.querySelector(".edit-is-open")?.value || "",
            expiryDate: row.querySelector(".edit-expiry-date")?.value || "",
            addedAt: row.querySelector(".edit-added-at")?.value || "",
            soll: row.querySelector(".edit-soll")?.value || "",
            ist: row.querySelector(".edit-ist")?.value || "",
        };

        editBtn.addEventListener("click", () => {
            viewModes.forEach(el => el.classList.add("d-none"));
            editModes.forEach(el => el.classList.remove("d-none"));

            editBtn.classList.add("d-none");
            saveBtn.classList.remove("d-none");
            cancelBtn.classList.remove("d-none");
        });

        cancelBtn.addEventListener("click", () => {
            row.querySelector(".edit-product-name").value = initialValues.productName;
            row.querySelector(".edit-unit").value = initialValues.unit;
            row.querySelector(".edit-user-id").value = initialValues.userId;
            row.querySelector(".edit-location-id").value = initialValues.locationId;
            row.querySelector(".edit-category-id").value = initialValues.categoryId;
            row.querySelector(".edit-is-open").value = initialValues.isOpen;
            row.querySelector(".edit-expiry-date").value = initialValues.expiryDate;
            row.querySelector(".edit-added-at").value = initialValues.addedAt;
            row.querySelector(".edit-soll").value = initialValues.soll;
            row.querySelector(".edit-ist").value = initialValues.ist;

            editModes.forEach(el => el.classList.add("d-none"));
            viewModes.forEach(el => el.classList.remove("d-none"));

            saveBtn.classList.add("d-none");
            cancelBtn.classList.add("d-none");
            editBtn.classList.remove("d-none");
        });

        saveBtn.addEventListener("click", async () => {
            const productId = row.dataset.productId;

            const payload = {
                id: Number(row.dataset.productId),
                userId: row.querySelector(".edit-user-id")?.value,
                locationId: row.querySelector(".edit-location-id")?.value,
                categoryId: row.querySelector(".edit-category-id")?.value,
                name: row.querySelector(".edit-product-name")?.value,
                isOpen: row.querySelector(".edit-is-open")?.value,
                unit: row.querySelector(".edit-unit")?.value,
                vorratProzent: 0,
                soll: row.querySelector(".edit-soll")?.value,
                ist: row.querySelector(".edit-ist")?.value,
                vorratFarbe: '',
                expiryDate: row.querySelector(".edit-expiry-date")?.value,
                addedAt: row.querySelector(".edit-added-at")?.value,
            };

            try {
                const response = await fetch(`/admin/product/${productId}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    offsetIndex++;
                    pushAlertWithOffset(false, `Fehler beim Speichern: ${response.status}`, alertVisibleTime, offsetIndex * offsetStep);
                    throw new Error(`Fehler beim Speichern: ${response.status}`);
                }

                row.querySelector(".product-name").textContent = `${payload.name}`;
                row.querySelector(".product-user-id").textContent = payload.userId;
                row.querySelector(".product-location-id").textContent = payload.locationId;
                row.querySelector(".product-category-id").textContent = payload.categoryId;
                row.querySelector(".product-sollte").textContent = payload.soll;
                row.querySelector(".product-ist").textContent = payload.ist;
                row.querySelector(".product-is-open").textContent = payload.isOpen;
                row.querySelector(".product-expiry-date").textContent = payload.expiryDate;
                row.querySelector(".product-added-at").textContent = payload.addedAt;

                initialValues.userId = payload.userId;
                initialValues.locationId = payload.locationId;
                initialValues.categoryId = payload.categoryId;
                initialValues.productName = payload.name;
                initialValues.unit = payload.unit;
                initialValues.soll = payload.soll;
                initialValues.ist = payload.ist;
                initialValues.isOpen = payload.isOpen;
                initialValues.expiryDate = payload.expiryDate;
                initialValues.addedAt = payload.addedAt;

                editModes.forEach(el => el.classList.add("d-none"));
                viewModes.forEach(el => el.classList.remove("d-none"));

                saveBtn.classList.add("d-none");
                cancelBtn.classList.add("d-none");
                editBtn.classList.remove("d-none");
                offsetIndex++;
                pushAlertWithOffset(true, "Product erfolgreich gespeichert!", alertVisibleTime, offsetIndex * offsetStep);
            } catch (error) {
                console.error(error);
                offsetIndex++;
                pushAlertWithOffset(false, `Speichern fehlgeschlagen.`, alertVisibleTime, offsetIndex * offsetStep);
            }
        });


        deleteBtn.addEventListener("click", () => {
            currentDeleteProductId = deleteBtn.dataset.productId;
            currentDeleteRow = row;
            deleteProductName.textContent = deleteBtn.dataset.productName;
            deleteModal.show();
        });
    });

    confirmDeleteBtn.addEventListener("click", async () => {
        if (!currentDeleteProductId) return;

        try {
            const response = await fetch(`/admin/product/${currentDeleteProductId}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                offsetIndex++;
                pushAlertWithOffset(false, "Fehler beim Löschen " + response.status, alertVisibleTime, offsetIndex * offsetStep)
                throw new Error(`Fehler beim Löschen: ${response.status}`);
            }

            currentDeleteRow.remove();
            deleteModal.hide();
            pushAlertWithOffset(true, "Produkt erfolgreich gelöscht!", alertVisibleTime, offsetIndex * offsetStep);
        } catch (error) {
            console.error(error);
            offsetIndex++;
            pushAlertWithOffset(false, "Löschen fehlgeschlagen", alertVisibleTime, offsetIndex * offsetStep);
        }
    });
}


function deleteMessage() {
    const deleteModalElement = document.getElementById("deleteMessageModal");
    const deleteMessageName = document.getElementById("deleteMessageName");
    const confirmDeleteBtn = document.getElementById("confirmDeleteMessageBtn");

    const deleteModal = new bootstrap.Modal(deleteModalElement);

    let currentDeleteMessageId = null;
    let currentDeleteRow = null;

    let offsetIndex = 0;
    const alertVisibleTime = 5000;
    const offsetStep = 500;

    document.querySelectorAll("tr[data-contact-id]").forEach((row) => {
        const deleteBtn = row.querySelector(".btn-delete-contact");
        deleteBtn.addEventListener("click", () => {
            currentDeleteMessageId = deleteBtn.dataset.contactId;
            currentDeleteRow = row;
            deleteMessageName.textContent = deleteBtn.dataset.contactEmail;
            deleteModal.show();
        });
    });

    confirmDeleteBtn.addEventListener("click", async () => {
        if (!currentDeleteMessageId) return;

        try {
            const response = await fetch(`/admin/message/${currentDeleteMessageId}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                offsetIndex++;
                pushAlertWithOffset(false, "Fehler beim Löschen " + response.status, alertVisibleTime, offsetIndex * offsetStep)
                throw new Error(`Fehler beim Löschen: ${response.status}`);
            }

            currentDeleteRow.remove();
            deleteModal.hide();
            pushAlertWithOffset(true, "Nachricht erfolgreich gelöscht!", alertVisibleTime, offsetIndex * offsetStep);
        } catch (error) {
            console.error(error);
            offsetIndex++;
            pushAlertWithOffset(false, "Löschen fehlgeschlagen", alertVisibleTime, offsetIndex * offsetStep);
        }
    });
}