document.addEventListener('DOMContentLoaded', function() {
    const passwordField = document.getElementById('loginPassword');
    const toggle = document.getElementById('toggleLoginPassword');
    if (!toggle || !passwordField) return;

    const eyeOpen = toggle.querySelector('.eye-open');
    const eyeClosed = toggle.querySelector('.eye-closed');

    function toggleVisibility(event) {
        if (event) event.preventDefault();
        const isPassword = passwordField.type === 'password';
        passwordField.type = isPassword ? 'text' : 'password';
        toggle.setAttribute('data-visible', isPassword ? 'true' : 'false');
        if (eyeOpen && eyeClosed) {
            if (isPassword) {
                eyeOpen.classList.add('hidden');
                eyeClosed.classList.remove('hidden');
            } else {
                eyeClosed.classList.add('hidden');
                eyeOpen.classList.remove('hidden');
            }
        }
    }

    toggle.addEventListener('click', toggleVisibility);
});

document.addEventListener('DOMContentLoaded', function() {
    const passwordField = document.getElementById('loginPassword');
    const toggle = document.getElementById('toggleLoginPassword');

    if (toggle && passwordField) {
        const eyeOpen = toggle.querySelector('.eye-open');
        const eyeClosed = toggle.querySelector('.eye-closed');

        function toggleVisibility(event) {
            if (event) event.preventDefault();
            const currentlyVisible = toggle.getAttribute('data-visible') === 'true' || passwordField.type === 'text';
            const newVisible = !currentlyVisible;
            toggle.setAttribute('data-visible', newVisible);
            passwordField.type = newVisible ? 'text' : 'password';
            if (eyeOpen && eyeClosed) {
                if (newVisible) {
                    eyeOpen.classList.add('hidden');
                    eyeClosed.classList.remove('hidden');
                } else {
                    eyeOpen.classList.remove('hidden');
                    eyeClosed.classList.add('hidden');
                }
            }
        }

        // Bind only on the button; clicks on inner <img> bubble to the button
        toggle.addEventListener('click', toggleVisibility);
    }
});


