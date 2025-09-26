document.addEventListener('DOMContentLoaded', function() {
    console.log('[register.js] DOM loaded');
    const passwordField = document.getElementById('passwordField');
    const passwordStrength = document.getElementById('passwordStrength');
    const passwordStrengthBar = document.getElementById('passwordStrengthBar');
    const submitBtn = document.getElementById('submitBtn');
    const form = document.getElementById('registerForm');
    const toggle = document.getElementById('toggleRegisterPassword');
    if (!toggle) {
        console.warn('[register.js] toggleRegisterPassword not found');
    }

    if (passwordField) {
        passwordField.addEventListener('input', function() {
            const password = this.value;
            const strength = calculatePasswordStrength(password);

            if (password.length > 0) {
                passwordStrength.classList.add('active');
                passwordStrengthBar.className = 'password-strength-bar ' + strength;
            } else {
                passwordStrength.classList.remove('active');
                passwordStrengthBar.className = 'password-strength-bar';
            }
        });
    }

    if (form) {
        form.addEventListener('submit', function(event) {
            const strength = calculatePasswordStrength(passwordField.value);
            if (strength !== 'strong') {
                alert('Password too weak. Use 8+ chars with upper, lower, number, symbol.');
                event.preventDefault();
                return false;
            }
            submitBtn.classList.add('loading');
            submitBtn.textContent = 'Creating Account...';
        });
    }

    if (toggle && passwordField) {
        const eyeOpen = toggle.querySelector('.eye-open');
        const eyeClosed = toggle.querySelector('.eye-closed');

        function toggleVisibility(event) {
            if (event) event.preventDefault();
            console.log('[register.js] Toggle clicked');
            const currentlyVisible = toggle.getAttribute('data-visible') === 'true' || passwordField.type === 'text';
            const newVisible = !currentlyVisible;
            toggle.setAttribute('data-visible', newVisible);
            passwordField.type = newVisible ? 'text' : 'password';
            console.log('[register.js] Password visibility:', newVisible);
            if (eyeOpen && eyeClosed) {
                if (newVisible) {
                    // when password visible, show the slashed eye
                    eyeOpen.classList.add('hidden');
                    eyeClosed.classList.remove('hidden');
                } else {
                    // when password hidden, show the open eye
                    eyeOpen.classList.remove('hidden');
                    eyeClosed.classList.add('hidden');
                }
            }
        }

        // Bind directly on the button
        toggle.addEventListener('click', toggleVisibility);

        // Also delegate on the container to catch clicks on inner SVGs/images
        const container = document.querySelector('.password-input');
        if (container) {
            container.addEventListener('click', function(e) {
                const btn = e.target.closest('#toggleRegisterPassword');
                if (btn) {
                    toggleVisibility(e);
                }
            });
        }
    }

    function calculatePasswordStrength(password) {
        let strength = 0;
        if (password.length >= 8) strength++;
        if (/[a-z]/.test(password)) strength++;
        if (/[A-Z]/.test(password)) strength++;
        if (/[0-9]/.test(password)) strength++;
        if (/[^a-zA-Z0-9]/.test(password)) strength++;
        if (strength < 3) return 'weak';
        if (strength < 4) return 'medium';
        return 'strong';
    }
});

// Enhanced password strength indicator & visibility toggle with SVG icons
document.addEventListener('DOMContentLoaded', function() {
    const passwordField = document.getElementById('passwordField');
    const passwordStrength = document.getElementById('passwordStrength');
    const passwordStrengthBar = document.getElementById('passwordStrengthBar');
    const submitBtn = document.getElementById('submitBtn');
    const form = document.getElementById('registerForm');
    const toggle = document.getElementById('toggleRegisterPassword');
    const eyeOpen = toggle.querySelector('.eye-open');
    const eyeClosed = toggle.querySelector('.eye-closed');

    // Password strength calculation
    passwordField.addEventListener('input', function() {
        const password = this.value;
        const strength = calculatePasswordStrength(password);

        if (password.length > 0) {
            passwordStrength.classList.add('active');
            passwordStrengthBar.className = 'password-strength-bar ' + strength;
        } else {
            passwordStrength.classList.remove('active');
            passwordStrengthBar.className = 'password-strength-bar';
        }
    });

    // Form submission animation
    form.addEventListener('submit', function(event) {
        const strength = calculatePasswordStrength(passwordField.value);
        if (strength !== 'strong') {
            alert('Password too weak. Use 8+ chars with upper, lower, number, symbol.');
            event.preventDefault();
            return false;
        }

        submitBtn.classList.add('loading');
        submitBtn.textContent = 'Creating Account...';
    });

    // Toggle password visibility with SVG swap
    if (toggle) {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();

            const isPassword = passwordField.type === 'password';
            passwordField.type = isPassword ? 'text' : 'password';

            toggle.setAttribute('data-visible', isPassword ? 'true' : 'false');

            if (isPassword) {
                eyeOpen.classList.add('hidden');
                eyeClosed.classList.remove('hidden');
            } else {
                eyeClosed.classList.add('hidden');
                eyeOpen.classList.remove('hidden');
            }

            // Small bounce effect
            passwordField.style.transform = 'scale(1.02)';
            setTimeout(() => {
                passwordField.style.transform = 'scale(1)';
            }, 150);
        });
    }

    // Password strength checker
    function calculatePasswordStrength(password) {
        let strength = 0;
        if (password.length >= 8) strength++;
        if (password.length >= 12) strength++;
        if (password.match(/[a-z]/)) strength++;
        if (password.match(/[A-Z]/)) strength++;
        if (password.match(/[0-9]/)) strength++;
        if (password.match(/[^a-zA-Z0-9]/)) strength++;

        if (strength <= 2) return 'weak';
        if (strength <= 4) return 'medium';
        return 'strong';
    }

    // Entrance animation
    const formElements = document.querySelectorAll('label, button[type="submit"]');
    formElements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';

        setTimeout(() => {
            element.style.transition = 'all 0.5s ease';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, 200 + (index * 100));
    });

    // Floating label effect
    const inputs = document.querySelectorAll('input[type="text"], input[type="password"]');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
        });

        input.addEventListener('blur', function() {
            if (!this.value) {
                this.parentElement.classList.remove('focused');
            }
        });
    });
});
