document.addEventListener("DOMContentLoaded", function () {

    // --- 1. GLOBALER ANIMATIONS-OBSERVER (Wird immer ausgeführt) ---
    // Damit werden Animationen auf JEDER Seite (inkl. Login/Footer) aktiv.
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const el = entry.target;
                const anim = el.getAttribute('data-animation');
                el.classList.add('is-visible', 'animate__animated');
                if (anim) el.classList.add(anim);
            } else {
                const el = entry.target;
                const anim = el.getAttribute('data-animation');
                el.classList.remove('is-visible', 'animate__animated', anim);
            }
        });
    }, { threshold: 0 });

    document.querySelectorAll('[data-animation]').forEach(el => {
        observer.observe(el);
    });

    // --- 2. NAVBAR-LOGIK (Wird ignoriert, wenn static-nav-page vorhanden ist) ---
    if (document.body.classList.contains('static-nav-page')) {
        return; // Beende das Skript hier für die Login-Seite, Animationen oben laufen aber weiter!
    }

    const navbar = document.querySelector('.customNavbar');
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    const sections = document.querySelectorAll('div[id]');

    navLinks.forEach(link => link.classList.remove('active'));

    window.addEventListener('scroll', function () {
        if (navbar) {
            if (window.scrollY > 60) {
                navbar.classList.add('navbar-scrolled');
                navbar.classList.remove('navbar-hidden');
            } else {
                navbar.classList.remove('navbar-scrolled');
                navbar.classList.add('navbar-hidden');
            }
        }

        let current = "";
        sections.forEach((section) => {
            const sectionTop = section.offsetTop;
            if (window.scrollY >= (sectionTop - 150)) {
                current = section.getAttribute("id");
            }
        });

        navLinks.forEach((link) => {
            link.classList.remove("active");
            if (current !== "" && link.getAttribute("href").includes(current)) {
                link.classList.add("active");
            }
        });
    });

    navLinks.forEach(link => {
        link.addEventListener('click', function () {
            navLinks.forEach(item => item.classList.remove('active'));
            this.classList.add('active');
        });
    });

    window.dispatchEvent(new Event('scroll'));
});