export function createAlertWarningOrDanger(warningOrError, text, time) {
    const alertElement = document.getElementById('alert');
    if (alertElement) {
        alertElement.innerHTML = `
            <div class="alert alert-${warningOrError} alert-dismissible fade show" role="alert">
                ${text}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;

        setTimeoutEnable(alertElement, time);
    }
}

export function createAlert(isSuccess, text, time) {
    const alertElement = document.getElementById('alert');
    if (!alertElement) {
        console.error("Alert-Container mit id='alert' nicht gefunden");
        return;
    }

    const alertType = isSuccess ? 'alert-success' : 'alert-danger';

    const alertBox = document.createElement('div');
    alertBox.className = `alert ${alertType} alert-dismissible fade show mt-2`;
    alertBox.setAttribute('role', 'alert');
    alertBox.innerHTML = `
        ${text}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    alertElement.appendChild(alertBox);

    setTimeout(() => {
        alertBox.classList.remove('show');
        setTimeout(() => {
            alertBox.remove();
        }, 150);
    }, time);
}

export function setTimeoutEnable(alertElement, timerInMillis) {
    setTimeout(() => {
        const alertBox = alertElement.querySelector('.alert');
        if (alertBox) {
            alertBox.classList.remove('show');
            setTimeout(() => {
                alertElement.innerHTML = '';
            }, 150);
        }
    }, timerInMillis);
}


export function pushAlertWithOffset(isSuccess, text, time, offset) {
    const alertElement = document.getElementById("alert");
    if (!alertElement) {
        console.error("Alert-Container mit id='alert' nicht gefunden");
        return;
    }

    const alertType = isSuccess ? "alert-success" : "alert-danger";

    setTimeout(() => {
        const alertBox = document.createElement("div");
        alertBox.className = `alert ${alertType} alert-dismissible fade show mt-2`;
        alertBox.setAttribute("role", "alert");
        alertBox.innerHTML = `
            ${text}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        alertElement.appendChild(alertBox);

        setTimeout(() => {
            alertBox.classList.remove("show");
            setTimeout(() => {
                alertBox.remove();
            }, 150);
        }, time);
    }, offset);
}

export const DEBUGGING = true;
export function debug(message){
    if (DEBUGGING) {
        console.log(message)
    }
}

export function info(message){
    console.info(message);
}

export function error(message) {
    console.error(message);
}

export function warn(message) {
    console.warn(message);
}