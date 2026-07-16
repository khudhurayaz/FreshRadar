
const reactiveOrDeactivateBtn = document.getElementById("reactiveOrDeactivate");
const upgradeOrDowngradeBtn = document.getElementById("upgradePlanOrDowngrade");
const buttonText = reactiveOrDeactivateBtn.textContent.trim();
const buttonTextUpgradeOrDowngrade = upgradeOrDowngradeBtn.textContent.trim();
const cancelBtn = document.getElementById("cancel");

reactiveOrDeactivateBtn.addEventListener("click", async function (event) {
    event.preventDefault();
    const userId = this.dataset.userId;
    const subscriptionId = this.dataset.subscriptionId;
    let status = "";

    if (buttonText  === "Reaktivieren") {
        status = "active";
    } else if(buttonText === "Deaktivieren"){
        status = "pause";
    }

    const data = {
        userId: userId,
        subscriptionId: subscriptionId,
        status: status
    };
    await fetch('/api/subscription/reactive', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });

    location.reload();
});

upgradeOrDowngradeBtn.addEventListener("click", async function (event) {
    event.preventDefault();
    const userId = this.dataset.userId;
    const subscriptionId = this.dataset.subscriptionId;
    const status = this.dataset.status;
    let planType = "";

    if (buttonTextUpgradeOrDowngrade  === "Downgrade auf Basic") {
        planType = "basic";
    } else if(buttonTextUpgradeOrDowngrade === "Upgrad auf Pro"){
        planType = "pro";
    }

    const data = {
        userId: userId,
        id: subscriptionId,
        status: status,
        planType: planType,
        purchasedAt: null
    };
    await fetch('/api/subscription/upgradeOrDowngrade', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });

    location.reload();
});

cancelBtn.addEventListener("click", async function (event) {
    event.preventDefault();

    const userId = this.dataset.userId;
    const subscriptionId = this.dataset.subscriptionId;

    if (!userId || !subscriptionId) {
        console.error("Fehlende Datenattribute am Cancel-Button");
        return;
    }

    const response = await fetch(`/api/subscription/ended?userId=${userId}&subscriptionId=${subscriptionId}`, {
        method: 'DELETE'
    });

    if (!response.ok) {
        console.error("Fehler:", await response.text());
        return;
    }
    location.reload();
});