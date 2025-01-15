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
    //load userData
    yield loadUserData();
    const logoutButton = document.getElementById("logout");
    logoutButton.addEventListener('click', (event) => {
        event.preventDefault();
        logoutUser();
    });
    function loadUserData() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/users/curUser`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    const user = yield res.json();
                    changeUI(user);
                }
                else {
                    alert("No Album found");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    function changeUI(user) {
        const usernameLabel = document.getElementById("usernameLabel");
        const usermanagmentLink = document.getElementById("usermanagmentLink");
        usernameLabel.textContent = user.username;
        if (!user.isAdmin) {
            usermanagmentLink.remove();
        }
    }
}));
function logoutUser() {
    return __awaiter(this, void 0, void 0, function* () {
        try {
            const res = yield fetch(`http://localhost:8888/logout`, {
                method: 'GET',
                mode: 'cors',
                credentials: 'include',
                headers: {
                    "Content-Type": "application/json"
                }
            });
            if (res.redirected) {
                window.location.href = res.url;
            }
            else if (res.ok) {
                window.location.href = "/login";
            }
            else {
                alert("Failed to logout user");
            }
        }
        catch (error) {
            console.error("Error logout user:", error);
        }
    });
}
//# sourceMappingURL=navigation.js.map