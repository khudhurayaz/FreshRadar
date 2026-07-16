import {createAlert, createAlertWarningOrDanger, debug, error, info, pushAlertWithOffset, warn} from './Util.js';
import "./Category.js";
import "./Location.js";

/* GLOBAL */
const locationMap = new Map();
const categoryMap = new Map();

window.toggleEdit = toggleEdit;
window.deleteProduct = deleteProduct;

const paginationHandlers = {
    bestandsverwaltung,
    haltbarkeit,
    sollIstBedarf
};

/* HILFSFUNKTIONEN */
async function fetchJson(url) {
    const response = await fetch(url, {
        headers: { "Accept": "application/json" }
    });

    if (!response.ok) {
        throw new Error(`Fehler bei ${url}: ${response.status}`);
    }

    return await response.json();
}
async function loadReferenceData() {
    const [locations, categories] = await Promise.all([
        fetchJson("/api/location/get"),
        fetchJson("/api/category/show")
    ]);

    locationMap.clear();
    categoryMap.clear();

    locations.forEach(location => {
        locationMap.set(
            location.id,
            location.name ?? location.location ?? location.ort ?? `#${location.id}`
        );
    });

    categories.forEach(category => {
        categoryMap.set(
            category.id,
            category.name ?? category.category ?? `#${category.id}`
        );
    });
}
function renderLocationOptions(selectedId = null) {
    return `
        <option value="">Bitte Lager auswählen...</option>
        ${Array.from(locationMap.entries()).map(([id, name]) => `
            <option value="${id}" ${Number(selectedId) === Number(id) ? "selected" : ""}>
                ${name}
            </option>
        `).join("")}
    `;
}
function renderCategoryOptions(selectedId = null) {
    return `
        <option value="">Bitte Kategorie auswählen...</option>
        ${Array.from(categoryMap.entries()).map(([id, name]) => `
            <option value="${id}" ${Number(selectedId) === Number(id) ? "selected" : ""}>
                ${name}
            </option>
        `).join("")}
    `;
}
function updateUrl(names, values) {
    const url = new URL(window.location.href);

    for (let i = 0; i < names.length; i++) {
        const value = values[i];
        if (value === null || value === undefined || value === "") {
            url.searchParams.delete(names[i]);
        } else {
            url.searchParams.set(names[i], values[i]);
        }
    }

    window.history.replaceState({}, "", url);
}

function formatDateTime(value) {
    if (!value) return "-";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;

    return date.toLocaleString("de-DE", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function validateAndStyle(element) {
    if (!element || !element.value.trim()) {
        if (element) {
            element.style.backgroundColor = "darkred";
            element.style.color = "white";
        }
        return false;
    } else {
        element.style.backgroundColor = "white";
        element.style.color = "black";
        return true;
    }
}

function getInputValue(element, fieldName) {
    if (!element) {
        warn(`${fieldName}Input ist nicht vorhanden!`);
        return null;
    }

    const value = parseInt(element.value, 10);
    if (isNaN(value)) {
        pushAlertWithOffset(false, `Im Feld '${fieldName}' konnte die Variable nicht umgewandelt werden!`, 5000, 0);
        return undefined; // undefined signalisiert einen Fehler
    }

    return value;
}

/* AMPEL */
async function updateAmpel() {
    const ampelSignal = document.getElementById('ampelSignal');
    const ampelSignal1 = document.getElementById('ampelSignal1');
    const ampelSignal2 = document.getElementById('ampelSignal2');

    if (!ampelSignal || !ampelSignal1 || !ampelSignal2) return;
    let expiring = false;

    try {
        const response = await fetch('/api/product/expiring');
        const responseAllProduct = await fetch('/api/product/all');
        const expiringProducts = await fetch("/api/product/expiring/expiringProducts")
        let count = await response.json();
        const allProducts = await responseAllProduct.json();
        const expiringProductsResponse = await expiringProducts.json();
        let ampelAktiv = false;

        ampelSignal.classList.remove('rot-aktive', 'gelb-aktive', 'gruen-aktive');
        ampelSignal1.classList.remove('rot-aktive', 'gelb-aktive', 'gruen-aktive');
        ampelSignal2.classList.remove('rot-aktive', 'gelb-aktive', 'gruen-aktive');

        if (expiringProductsResponse.length <= 0) {
            ampelSignal.classList.remove("productsCount");
        }

        if (count <= 0) {
            ampelSignal.classList.remove("productsCount");
        }

        function removeStyle(ampel, styleClass) {
            ampel.classList.remove(styleClass);
        }

        function showAmpel(color, textVerstecken){
            ampelSignal.classList.add(color);
            ampelSignal1.classList.add(color);
            ampelSignal2.classList.add(color);
            if (textVerstecken) {
                document.querySelectorAll(".ampelText").forEach(el => {
                    el.style.display = "none";
                });
                document.querySelectorAll(".ampel").forEach(el => {
                    el.style.display = "block";
                    el.style.margin = "0 auto";
                });
            }
        }

        if (count >= 4 || expiringProductsResponse.length >= 2 || expiringProductsResponse.length === allProducts.length) {
            showAmpel('rot-aktiv', true);
            expiring = true;
        } else {
            if (count === 2) {
                ampelSignal.classList.add('gelb-aktiv');
                ampelSignal1.classList.add('gelb-aktiv');
                ampelSignal2.classList.add('gelb-aktiv');
                document.getElementById("productsExpire").style.display = 'none';
                ampelSignal1.classList.add('productsCount');
                ampelSignal1.innerText = count;
                ampelAktiv = true;
            }

            if (expiringProductsResponse.length >= 1) {
                document.getElementById("productsExpire").style.display = 'block';
                removeStyle(ampelSignal, 'gelb-aktiv');
                ampelSignal.classList.add('rot-aktiv');
                ampelSignal.classList.add('productsCount');
                ampelSignal.innerText = expiringProductsResponse.length;
                expiring = true
            }
        }


        //Nachrichten anzeigen
        if (expiring && expiringProductsResponse.length > 0) {
            pushAlertWithOffset(false,
                `Es ${expiringProductsResponse.length === 1 ? 'wurde' : 'wurden'} <strong>${expiringProductsResponse.length}</strong> ${expiringProductsResponse.length === 1 ? 'Produkt' : 'Produkte'} gefunden, die abgelaufen ${expiringProductsResponse.length === 1 ? 'ist' : 'sind'}!`,
                5000, 0);
        }

        if (count >= 2) {
            createAlertWarningOrDanger(
                count === 2 ? 'warning' : 'danger',
                `Es ${count === 1 ? 'wurde' : 'wurden'} <strong>${count}</strong> ${count === 1 ? 'Produkt' : 'Produkte'} gefunden, die bald ablaufen!`,
                5000
            );
        }

        if (!expiring && !ampelAktiv || expiringProductsResponse.length === 0) {
            showAmpel("gruen-aktiv", true);
            pushAlertWithOffset(true, "Alles im Grünen Bereich!", 5000, 0);
        }
    } catch (err) {
        error("UpdateAmpel: Fehler beim Ampel-Update:", err);
        pushAlertWithOffset(false, "Fehler beim Ampel update! Nachricht: '" + err + "'");
    }
}

/* SAVE */
async function saveProduct(productId, updatedData) {
    const response = await fetch(`/api/product/update/${productId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedData)
    });

    if (!response.ok) {
        let userMessage = "Es ist ein Fehler aufgetreten.";
        if (response.status === 401) userMessage = "Du bist nicht eingeloggt.";
        else if (response.status === 403) userMessage = "Du hast keine Berechtigung.";
        else if (response.status === 404) userMessage = "Die Daten wurden nicht gefunden.";
        else if (response.status >= 500) userMessage = "Serverfehler. Bitte versuche es später erneut.";
        throw new Error(userMessage);
    }

    pushAlertWithOffset(true, 'Produkt erfolgreich gespeichert ', 5000, 0);
}

/* TOGGLE EDIT */
async function toggleEdit(button, productId) {
    const row = button.closest('tr');
    const editFields = row.querySelectorAll('.edit-text');
    const deleteButton = row.querySelector(`#delete-${productId}`);
    const viewMode = row.querySelector('.view-mode');
    const editMode = row.querySelector('.edit-mode');

    if (button.innerText === "Ändern") {
        editFields.forEach(span => {
            const val = span.innerText;

            if (span.classList.contains('edit-date')) {
                const rawDate = span.getAttribute('data-raw');
                const isoDate = rawDate ? rawDate.split('T')[0] : "";
                span.innerHTML = `<input type="date" class="form-control" value="${isoDate}">`;
            } else if (span.classList.contains('edit-boolean')) {
                const isChecked = val.trim() === 'Geöffnet';
                span.innerHTML = `<input type="checkbox" class="form-check-input" ${isChecked ? 'checked' : ''}>`;
            } else {
                span.innerHTML = `<input type="text" class="form-control" value="${val}">`;
            }
        });


        updateViewModeEdit(
            viewMode,
            editMode,
            button,
            'Speichern',
            deleteButton,
            'Abbrechen',
            false,
            ["btn-info", "btn-success"],
            ["btn-danger", "btn-warning"]);

        button.classList.add("mb-2");
        function abbrechenHandler(e) {
            e.preventDefault();
            e.stopImmediatePropagation();

            editFields.forEach(span => {
                const input = span.querySelector('input');
                if (input) {
                    if (span.classList.contains('edit-boolean')) {
                        span.innerText = input.defaultChecked ? 'Geöffnet' : 'Geschlossen';
                    } else if (span.classList.contains('edit-date')) {
                        const rawDate = span.getAttribute('data-raw');
                        span.innerText = rawDate ? new Date(rawDate).toLocaleDateString() : '-';
                    } else {
                        span.innerText = input.defaultValue;
                    }
                }
            });

            updateViewModeEdit(
                viewMode,
                editMode,
                button,
                'Ändern',
                deleteButton,
                'Löschen',
                true,
                ["btn-success", "btn-info"],
                ["btn-warning", "btn-danger"]);
            button.classList.add("mb-2");
            deleteButton.classList.add("mb-2");
        }

        deleteButton.addEventListener('click', abbrechenHandler);

    } else {
        const openCheckbox = row.querySelector(`#open-${productId} input[type="checkbox"]`);
        const isOpen = openCheckbox ? openCheckbox.checked : undefined;

        const expiryInput = row.querySelector(`#expiry-${productId} input`);
        let isoDate = expiryInput ? (expiryInput.value ? new Date(expiryInput.value).toISOString() : null) : undefined;

        const nameInput = row.querySelector(`#name-${productId} input`);
        const unitInput = row.querySelector(`#unit-${productId} input`);
        const sollInput = row.querySelector(`#soll-${productId} input`);
        const istInput = row.querySelector(`#ist-${productId} input`);
        const sollValue = getInputValue(sollInput, "Soll");
        const istValue = getInputValue(istInput, "Ist");

        const locationSelect = row.querySelector('.edit-location-id');
        const categorySelect = row.querySelector('.edit-category-id');

        const fallbackLocationId = row.dataset.locationId;
        const fallbackCategoryId = row.dataset.categoryId;

        const updatedData = {
            id: productId,
            locationId: locationSelect?.value
                ? parseInt(locationSelect.value, 10)
                : (fallbackLocationId ? parseInt(fallbackLocationId, 10) : null),
            categoryId: categorySelect?.value
                ? parseInt(categorySelect.value, 10)
                : (fallbackCategoryId ? parseInt(fallbackCategoryId, 10) : null),
            ...(nameInput && { name: nameInput.value }),
            ...(unitInput && { unit: unitInput.value }),
            ...(sollValue !== undefined && { soll: sollValue }),
            ...(istValue !== undefined && { ist: istValue }),
            ...(openCheckbox !== null && isOpen !== undefined && { isOpen }),
            ...(expiryInput !== null && isoDate !== undefined && { expiryDate: isoDate }),
        };

        try {
            await saveProduct(productId, updatedData);

            if (sollValue !== undefined || istValue !== undefined) {
                const responseGetInventory = await fetch(`api/inventory/${productId}`);
                if (!responseGetInventory.ok) {
                    const responseGetInventoryText = await responseGetInventory.text();
                    pushAlertWithOffset(false, responseGetInventoryText.message || "API Fehler", 5000, 0);
                    throw new Error(`API Fehler`);
                }

                const responseInventory = await responseGetInventory.json();

                const inventoryRequest = {
                  productId: productId,
                  quantity: sollValue,
                  currentQuantity: istValue
                };

                await fetch(`api/updateInventory/${responseInventory.inventoryId}`, {
                    method: 'PUT',
                    body: JSON.stringify(inventoryRequest)
                });
            }

            editFields.forEach(span => {
                const input = span.querySelector('input');

                if (span.classList.contains('edit-date')) {
                    const dateToShow = isOpen === true ? new Date(isoDate) : (input && input.value ? new Date(input.value) : null);
                    if (dateToShow) {
                        span.setAttribute('data-raw', dateToShow.toISOString());
                        span.innerText = dateToShow.toLocaleDateString();
                    } else {
                        span.innerText = '-';
                    }
                } else if (span.classList.contains('edit-boolean')) {
                    span.innerText = input.checked ? 'Geöffnet' : 'Geschlossen';
                } else if (input) {
                    span.innerText = input.value;
                }
            });

            const locationText = row.querySelector('.product-location-id');
            const categoryText = row.querySelector('.product-category-id');

            if (locationText && locationSelect) {
                locationText.innerText = locationSelect.selectedOptions[0]?.textContent ?? "-";
                row.dataset.locationId = locationSelect.value;
            }

            if (categoryText && categorySelect) {
                categoryText.innerText = categorySelect.selectedOptions[0]?.textContent ?? "-";
                row.dataset.categoryId = categorySelect.value;
            }

            updateViewModeEdit(
                viewMode,
                editMode,
                button,
                'Ändern',
                deleteButton,
                'Löschen',
                true,
                ["btn-success", "btn-info"],
                ["btn-warning", "btn-danger"]);
            button.classList.add("mb-2");
            await bestandsverwaltung();
            await updateAmpel();

        } catch (err) {
            error("Speichern fehlgeschlagen:", err);
            pushAlertWithOffset(false, err.message, 5000, 0);
        }
    }
}

function updateViewModeEdit(viewMode, editMode, button, buttonText, deleteButton, deleteButtonText, removeView, buttonStyleList, deleteButtonStyleList){
    if (removeView) {
        if (viewMode) viewMode.classList.remove("d-none");
        if (editMode) editMode.classList.add("d-none");
    } else {
        if (viewMode) viewMode.classList.add("d-none");
        if (editMode) editMode.classList.remove("d-none");
    }

    button.innerText = buttonText;
    button.classList.replace(buttonStyleList[0], buttonStyleList[1]);
    button.classList.remove("mb-2");

    deleteButton.innerText = deleteButtonText;
    deleteButton.classList.replace(deleteButtonStyleList[0], deleteButtonStyleList[1]);
}

/* DELETE */
async function deleteProduct(productId) {
    const existing = document.getElementById('confirmModal');
    if (existing) existing.remove();

    const modalHtml = `
        <div class="modal fade" id="confirmModal" tabindex="-1">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title">Produktlöschung!</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body">
                <p>Möchten Sie wirklich das Produkt <strong>#${productId}</strong> löschen?</p>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Abbrechen</button>
                <button type="button" class="btn btn-danger" id="confirmDelete">Produkt löschen</button>
              </div>
            </div>
          </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

    const modalEl = document.getElementById('confirmModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    document.getElementById('confirmDelete').addEventListener('click', async () => {
        modal.hide();
        try {
            await fetch(`/api/product/delete/${productId}`, { method: 'DELETE' });
            createAlert(true, "Produkt wurde erfolgreich gelöscht", 5000);
            await bestandsverwaltung();
        } catch (err) {
            createAlert(false, err, 5000);
        }
    });

    modalEl.addEventListener('hidden.bs.modal', () => modalEl.remove());
}

/* GENERIC PAGINATION + LOAD */
async function loadPaginatedSection(config, pageArg = null) {
    const tableBody = document.getElementById(config.tableBodyId);
    if (!tableBody) return;

    const section = document.getElementById(config.sectionId);
    const paginationContainer = document.getElementById(config.paginationId);
    const params = new URLSearchParams(window.location.search);

    const currentPage = pageArg !== null
        ? pageArg
        : parseInt(params.get("page") || section?.dataset.currentPage || "0", 10);

    const size = parseInt(params.get("size") || section?.dataset.pageSize || config.defaultSize || "5", 10);

    const query = typeof config.getQuery === "function"
        ? config.getQuery({ params, section, page: currentPage, size })
        : {};

    const url = new URL(config.endpoint, window.location.origin);
    url.searchParams.set("page", currentPage);
    url.searchParams.set("size", size);

    Object.entries(query).forEach(([key, value]) => {
        if (value !== null && value !== undefined && value !== "") {
            url.searchParams.set(key, value);
        }
    });

    try {
        const response = await fetch(url.toString(), {
            headers: { "Accept": "application/json" }
        });

        if (!response.ok) {
            const text = await response.text();
            pushAlertWithOffset(false, text || "API Fehler", 5000, 0);
            throw new Error(`${config.name}: API Fehler`);
        }

        const pageData = await response.json();
        const rows = pageData.content || [];

        tableBody.innerHTML = "";
        if (paginationContainer) paginationContainer.innerHTML = "";

        if (rows.length === 0) {
            tableBody.innerHTML = config.emptyHtml || "<tr><td colspan='7'>Keine Daten gefunden.</td></tr>";

            updateUrl(
                config.urlKeys || ["tab", "page", "size"],
                typeof config.getUrlValues === "function"
                    ? config.getUrlValues({ pageData, query, page: currentPage, size })
                    : [config.tab, currentPage, size]
            );
            return;
        }

        rows.forEach((row, index) => {
            tableBody.insertAdjacentHTML("beforeend", config.renderRow(row, index, pageData));
        });

        renderGenericPagination({
            paginationId: config.paginationId,
            pageData,
            pageHandler: config.pageHandler
        });

        updateUrl(
            config.urlKeys || ["tab", "page", "size"],
            typeof config.getUrlValues === "function"
                ? config.getUrlValues({ pageData, query, page: currentPage, size })
                : [config.tab, pageData.number, pageData.size]
        );

    } catch (err) {
        error(`Fehler bei ${config.name}:`, err);
        tableBody.innerHTML = config.errorHtml || "<tr><td colspan='7'>Fehler beim Laden.</td></tr>";
        if (paginationContainer) paginationContainer.innerHTML = "";
    }
}

function renderGenericPagination({ paginationId, pageData, pageHandler }) {
    const container = document.getElementById(paginationId);
    if (!container) return;

    const totalPages = pageData.totalPages || 0;
    const currentPage = pageData.number || 0;

    if (totalPages <= 1) {
        container.innerHTML = "";
        return;
    }

    let html = `
        <nav aria-label="Pagination">
            <ul class="pagination justify-content-center mb-0 flex-wrap gap-2">
    `;

    html += `
        <li class="page-item ${pageData.first ? "disabled" : ""}">
            <button type="button" class="page-link"
                    data-page="${currentPage - 1}"
                    data-handler="${pageHandler}"
                    ${pageData.first ? "disabled" : ""}>
                Zurück
            </button>
        </li>
    `;

    for (let i = 0; i < totalPages; i++) {
        html += `
            <li class="page-item ${i === currentPage ? "active" : ""}">
                <button type="button" class="page-link"
                        data-page="${i}"
                        data-handler="${pageHandler}"
                        ${i === currentPage ? "disabled" : ""}>
                    ${i + 1}
                </button>
            </li>
        `;
    }

    html += `
        <li class="page-item ${pageData.last ? "disabled" : ""}">
            <button type="button" class="page-link"
                    data-page="${currentPage + 1}"
                    data-handler="${pageHandler}"
                    ${pageData.last ? "disabled" : ""}>
                Weiter
            </button>
        </li>
    `;

    html += `
            </ul>
        </nav>
    `;

    container.innerHTML = html;
}

/* RENDER */
function renderBestandsRow(product, index, pageData) {
    const firstLetter = product?.name?.trim()?.charAt(0)?.toUpperCase();

    const isOpen = product?.isOpen === true;
    const statusClass = isOpen ? "bg-success" : "bg-secondary";
    const statusText = isOpen ? "Geöffnet" : "Geschlossen";

    const ortText =
        product?.locationName ??
        product?.location ??
        locationMap.get(product?.locationId) ??
        (product?.locationId != null ? `ID ${product.locationId}` : "-");

    const categoryText =
        product?.categoryName ??
        product?.category ??
        categoryMap.get(product?.categoryId) ??
        (product?.categoryId != null ? `ID ${product.categoryId}` : "-");

    performHealthCheck();

    return `
        <tr data-product-id="${product?.id ?? ""}"
            data-location-id="${product?.locationId ?? ""}"
            data-category-id="${product?.categoryId ?? ""}">
            <td class="text-muted">
                ${index + 1 + ((pageData?.number ?? 0) * (pageData?.size ?? 0))}
            </td>

            <td>
                <div class="d-flex align-items-center gap-3">
                    <div class="rounded-circle border shadow-sm d-flex align-items-center justify-content-center bg-light text-dark fw-semibold"
                         style="width:56px;height:56px;">
                        <span>${firstLetter}</span>
                    </div>

                    <div class="w-100">
                        <div class="fw-semibold text-dark edit-text" id="name-${product.id}">
                            ${product?.name ?? "-"}
                        </div>
                        <small class="text-muted edit-text" id="unit-${product.id}">
                            ${product?.unit ?? "-"}
                        </small>
                    </div>
                </div>
            </td>

            <td>
                <div class="view-mode">
                    <small class="text-muted d-block">
                        <b>Ort:</b> <br>
                        <span class="product-location-id">${ortText}</span>
                    </small>
                    <div class="hr-v2"></div>
                    <small class="text-muted d-block">
                        <b>Kategorie:</b> <br>
                        <span class="product-category-id">${categoryText}</span>
                    </small>
                </div>

                <div class="edit-mode d-none mt-2">
                    <div class="row g-2">
                        <div class="col-12">
                            <label class="form-label">Lagerort</label>
                            <select class="form-select form-select-sm edit-location-id" name="locationId">
                                ${renderLocationOptions(product?.locationId)}
                            </select>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Kategorie</label>
                            <select class="form-select form-select-sm edit-category-id" name="categoryId">
                                ${renderCategoryOptions(product?.categoryId)}
                            </select>
                        </div>
                    </div>
                </div>
            </td>
            
            <td>
                <span class="badge ${statusClass} edit-text edit-boolean" id="open-${product?.id}">
                    ${statusText}
                </span>
            </td>

            <td>
                <div class="text-dark">
                    Ablaufdatum:
                    <span class="edit-text edit-date" id="expiry-${product.id}" data-raw="${product.expiryDate ?? ""}">${formatDateTime(product?.expiryDate)}</span>
                </div>
                <small class="text-muted d-block">
                    Hinzugefügt:
                    <span>${formatDateTime(product?.addedAt)}</span>
                </small>
            </td>
            <td>
                <div class="d-flex flex-wrap gap-2">
                    <button type="button"
                            class="btn btn-info btn-sm"
                            onclick="toggleEdit(this, ${product?.id})">
                        Ändern
                    </button>

                    <button type="button"
                            class="btn btn-danger btn-sm"
                            id="delete-${product?.id}"
                            data-id="${product?.id}">
                        Löschen
                    </button>
                </div>
            </td>
        </tr>
    `;
}

async function performHealthCheck() {
    try {
        const response = await fetch('api/inventory/healthCareCheckup');
        const responseText = await response.text();

        if (!response.ok) {
            pushAlertWithOffset(false, responseText || "API Fehler", 5000, 0);
            throw new Error(`API Fehler`);
        }

        pushAlertWithOffset(true, responseText, 5000, 0);
    } catch (error) {
        error("Health-Check fehlgeschlagen:", error);
    }
}

function bestandsverwaltung(page = null) {
    return loadPaginatedSection({
        name: "Bestandsverwaltung",
        tab: "bestandsverwaltung",
        endpoint: "/api/product/products",
        sectionId: "bestandsverwaltung-section",
        tableBodyId: "bestandsverwaltung",
        paginationId: "bestandsverwaltung-pagination",
        defaultSize: 5,
        pageHandler: "bestandsverwaltung",
        renderRow: renderBestandsRow,
        emptyHtml: "<tr><td colspan='7'>Keine Produkte gefunden.</td></tr>",
        errorHtml: "<tr><td colspan='7'>Fehler beim Laden der Produkte.</td></tr>"
    }, page);
}

function renderHaltbarkeitRow(p, index, pageData) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let rowClass = "";
    const stockPercent = p.vorratProzent ?? 0;
    let stockClass = "bg-success";
    let stockTextClass = "";
    let stockProgressClass = "";

    if (p.expiryDate) {
        const expiry = new Date(p.expiryDate);
        expiry.setHours(0, 0, 0, 0);

        const diffTime = expiry - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const maxDays = p.isOpen ? 2 : 4;

        if (diffDays >= 0 && diffDays <= maxDays) {
            rowClass = "table-warning";
        } else if (diffDays < 0) {
            rowClass = "table-danger";
        }
    }

    if (stockPercent === 0) {
        stockTextClass = "text-danger fw-bold";
        stockProgressClass = "bg-danger";
    }

    if (stockPercent < 30) stockClass = "bg-danger";
    else if (stockPercent < 70) stockClass = "bg-warning";

    return `
        <tr class="${rowClass}">
            <td>${index + 1 + (pageData.number * pageData.size)}</td>
            <td>${p.name ?? "-"}</td>
            <td>${p.unit ?? "-"}</td>
            <td>${p.isOpen ? "Ja" : "Nein"}</td>
            <td>${p.expiryDate ? new Date(p.expiryDate).toLocaleDateString() : "-"}</td>
            <td>${p.addedAt ? new Date(p.addedAt).toLocaleDateString() : "-"}</td>
            <td class="stock-cell">
                <div class="stock-label ${stockTextClass}">
                    <span>Bestand</span>
                </div>
                <div class="progress ${stockProgressClass}">
                    <div class="progress-bar ${stockClass}"
                         role="progressbar"
                         style="width: ${stockPercent}%"
                         aria-valuenow="${stockPercent}"
                         aria-valuemin="0"
                         aria-valuemax="100">
                        ${stockPercent}%
                    </div>
                </div>
            </td>
        </tr>
    `;
}

function haltbarkeit(page = null) {
    return loadPaginatedSection({
        name: "Haltbarkeit",
        tab: "haltbarkeit",
        endpoint: "/api/product/products",
        sectionId: "filter-data",
        tableBodyId: "product-body",
        paginationId: "haltbarkeit-pagination",
        defaultSize: 5,
        pageHandler: "haltbarkeit",
        renderRow: renderHaltbarkeitRow,
        emptyHtml: "<tr><td colspan='7' class='text-center'>Keine Produkte gefunden.</td></tr>",
        errorHtml: "<tr><td colspan='7' class='text-center'>Fehler beim Laden der Produkte.</td></tr>",
        getQuery: ({ params, section }) => ({
            category: params.get("category") || section?.dataset.cat || "",
            location: params.get("location") || section?.dataset.loc || ""
        }),
        urlKeys: ["tab", "page", "size", "category", "location"],
        getUrlValues: ({ pageData, query }) => [
            "haltbarkeit",
            pageData.number,
            pageData.size,
            query.category || "",
            query.location || ""
        ]
    }, page);
}

function renderSollIstRow(p, index, pageData) {
    return `
        <tr data-product-id="${p?.id ?? ""}"
            data-location-id="${p?.locationId ?? ""}"
            data-category-id="${p?.categoryId ?? ""}">
            <td>${index + 1 + (pageData.number * pageData.size)}</td>
            <td><span id="name-${p.id}">${p.name ?? "-"}</span></td>
            <td><span class="edit-text" id="soll-${p.id}">${p.soll ?? "-"}</span></td>
            <td><span class="edit-text" id="ist-${p.id}">${p.ist ?? "-"}</span></td>
            <td>
                <button class="btn btn-info btn-sm w-100 mb-2" onclick="toggleEdit(this, ${p.id})">Ändern</button>
                <button class="btn btn-danger btn-sm w-100" id="delete-${p.id}" data-id="${p.id}">Löschen</button>
            </td>
        </tr>
    `;
}

function sollIstBedarf(page = null) {
    return loadPaginatedSection({
        name: "Soll-Ist-Bedarf",
        tab: "soll_ist",
        endpoint: "/api/product/products",
        sectionId: "soll-ist-section",
        tableBodyId: "sollIstBedarf",
        paginationId: "soll-ist-pagination",
        defaultSize: 5,
        pageHandler: "sollIstBedarf",
        renderRow: renderSollIstRow,
        emptyHtml: "<tr><td colspan='5' class='text-center text-muted py-4'>Keine Produkte gefunden.</td></tr>",
        errorHtml: "<tr><td colspan='5' class='text-center text-muted py-4'>Fehler beim Laden der Produkte.</td></tr>",
        urlKeys: ["tab", "page", "size"],
        getUrlValues: ({ pageData }) => [
            "soll_ist",
            pageData.number,
            pageData.size
        ]
    }, page);
}

async function initAddProduct() {
    const inputDate = document.getElementById('inputDate');
    if (!inputDate) return;

    const today = new Date().toISOString().split('T')[0];
    inputDate.value = today;

    const form = document.querySelector('.create form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const inputProduct = document.getElementById("inputProduct");
        const inputUnit = document.getElementById("inputUnit");
        const isOpen = document.getElementById("gridCheck");
        const inputStatelagerort = document.getElementById("inputStatelagerort");
        const inputState = document.getElementById("inputState");
        const inputSoll = document.getElementById("inputSoll");
        const inputIst = document.getElementById("inputIst");

        if (!inputProduct || !inputUnit || !isOpen || !inputStatelagerort || !inputState || !inputSoll || !inputIst) return;

        const isNameValid = validateAndStyle(inputProduct);
        const isUnitValid = validateAndStyle(inputUnit);
        const isLagerortValid = validateAndStyle(inputStatelagerort);
        const isKategorieValid = validateAndStyle(inputState);
        const isDateValid = validateAndStyle(inputDate);
        const isSoll = validateAndStyle(inputSoll);
        const isIst = validateAndStyle(inputIst);

        if (!isNameValid || !isUnitValid || !isLagerortValid || !isKategorieValid || !isDateValid || !isSoll || !isIst) {
            return;
        }

        let isoDate = inputDate.value ? new Date(inputDate.value).toISOString() : null;

        const newProductData = {
            name: inputProduct.value,
            unit: inputUnit.value,
            isOpen: isOpen.checked,
            locationId: parseInt(inputStatelagerort.value, 10),
            categoryId: parseInt(inputState.value, 10),
            expiryDate: isoDate,
            soll: inputSoll.value,
            ist: inputIst.value,
            addedAt: today
        };

        try {
            const response = await fetch('/api/product/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newProductData)
            });

            let responseText;
            if (!response.ok) {
                const errorText = await response.text();
                createAlert(false, errorText, 5000, 0);
                error("Fehler beim Erstellen des Produkts");
                return;
            } else {
                responseText = await response.text();
            }

            createAlert(true, responseText, 5000);
            form.reset();
            document.getElementById('inputDate').value = today;
        } catch (err) {
            error("Fehler beim Hinzufügen:", err);
        }
    });
}

document.addEventListener('DOMContentLoaded', async () => {
    await loadReferenceData();
    updateAmpel();
    bestandsverwaltung();
    haltbarkeit();
    sollIstBedarf();
    initAddProduct();

    document.addEventListener('click', e => {
        const btn = e.target.closest('[id^="delete-"]');
        if (btn && btn.innerText.trim() === "Löschen") {
            const productId = btn.getAttribute('data-id');
            deleteProduct(productId);
            return;
        }

        const pageButton = e.target.closest(".pagination .page-link[data-handler]");
        if (!pageButton || pageButton.disabled) return;

        const handlerName = pageButton.dataset.handler;
        const page = parseInt(pageButton.dataset.page, 10);
        if (!handlerName || Number.isNaN(page) || page < 0) return;

        const handler = paginationHandlers[handlerName];
        if (typeof handler === "function") {
            handler(page);
        }
    });
});