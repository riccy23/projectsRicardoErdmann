var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
document.addEventListener('DOMContentLoaded', () => __awaiter(this, void 0, void 0, function* () {
    // Handle form submission
    const loginForm = document.querySelector('form');
    // @ts-ignore
    loginForm === null || loginForm === void 0 ? void 0 : loginForm.addEventListener('submit', (event) => __awaiter(this, void 0, void 0, function* () {
        event.preventDefault();
        const userName = document.getElementById('floatingInput').value;
        const password = document.getElementById('floatingPassword').value;
        console.log(`Username: ${userName}, Password: ${password}`);
        // Senden Sie den Benutzernamen und das Passwort als JSON-Body an den Server
        const res = yield fetch('http://localhost:8888/login', {
            method: 'POST',
            mode: 'cors',
            credentials: 'include',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username: userName, password: password })
        });
        // Handhaben Sie die Antwort vom Server
        if (res.ok) {
            const data = yield res.json();
            console.log(data);
            window.location.href = `/home`;
        }
        else {
            console.error('Login failed');
            const data = yield res.json();
            showError(data.error);
        }
    }));
    function showError(message) {
        const alertContainer = document.getElementById('alertContainer');
        if (alertContainer) {
            alertContainer.innerHTML = `
            <div class="alert alert-danger fade show" role="alert">
                ${message}
            </div>
        `;
        }
    }
}));
//# sourceMappingURL=login.js.map