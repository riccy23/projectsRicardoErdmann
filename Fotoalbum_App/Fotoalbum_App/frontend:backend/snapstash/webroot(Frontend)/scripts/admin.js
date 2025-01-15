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
    //Load Photos
    yield loadUsers();
    //Refresh Photos
    const refreshBtn = document.getElementById("refreshUsers");
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            window.location.reload();
        }));
    }
    //Load searched Users only
    const searchBtn = document.getElementById("searchButton");
    if (searchBtn) {
        searchBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Search button clicked");
            const searched_stringInput = document.getElementById('searchInput');
            if (searched_stringInput) {
                const searched_string = searched_stringInput.value.trim();
                //clear table and load searched users
                if (searched_string) {
                    const table = document.getElementById("users");
                    table.innerHTML = null;
                    yield searchUser(searched_string);
                }
            }
            else {
                console.error("User input not found");
            }
        }));
    }
    //Update User
    const saveChangesBtn = document.getElementById('editUser');
    if (saveChangesBtn) {
        saveChangesBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Edit User button clicked");
            const usernameInput = document.getElementById('username-edit');
            const userPasswordInput = document.getElementById('user-edit-password');
            const hiddenUserIdInput = document.getElementById('user-edit-id');
            if (usernameInput && userPasswordInput && hiddenUserIdInput) {
                const username = usernameInput.value.trim();
                const password = userPasswordInput.value.trim();
                // check if username and userid are not empty, required fields
                if (username && hiddenUserIdInput) {
                    yield saveChanges(username, parseInt(hiddenUserIdInput.value), password);
                }
                else {
                    alert("Every User needs a username!");
                }
            }
            else {
                console.error("username input not found");
            }
        }));
    }
    else {
        console.error("Edit User button not found");
    }
    // Create User
    const createUserBtn = document.getElementById('createUserBtn');
    if (createUserBtn) {
        createUserBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Create User button clicked");
            const usernameInput = document.getElementById('username');
            const userPasswordInput = document.getElementById('user-password');
            if (usernameInput && userPasswordInput) {
                const username = usernameInput.value.trim();
                const userPassword = userPasswordInput.value.trim();
                if (username && userPassword) {
                    yield createUser(username, userPassword).then(() => {
                        window.location.reload();
                    });
                }
                else {
                    alert("Every User needs a username and password!");
                }
            }
            else {
                alert("Username and password is empty");
            }
        }));
    }
    else {
        console.error("Create User button not found");
    }
    //Search User
    /**
     * Send a GET request to the server to search for users by a Username.
     *
     * @param {string} searched_string - The Username to search for.
     * @return {Promise<void>} A Promise that resolves when the request is done.
     *
     * @description
     * Based on a given username, this function performs a search for users.
     *
     * @example
     * const searchBtn = document.getElementById("searchButton");
     *   if (searchBtn) {
     *     searchBtn.addEventListener('click', async () => {
     *       console.log("Search button clicked");
     *       const searched_stringInput = document.getElementById('searchInput') as HTMLInputElement;
     *       if (searched_stringInput) {
     *         const searched_string = searched_stringInput.value.trim();
     *         if (searched_string) {
     *           const table = document.getElementById("users");
     *           table.innerHTML = null;
     *           await searchUser(searched_string);
     *         }
     *       } else {
     *         console.error("User input not found");
     *       }
     *
     *     });
     *   }
     *
     * @remarks
     * - If the request is successful, create a table with the users found.
     * - If the request fails, an alert will be displayed.
     * - The user will be redirected to the home page if an error occurs during the request.
     *
     * @see addUsersTable
     *
     */
    function searchUser(searched_string) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/users/search/${searched_string}`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    const user = yield res.json();
                    //Show Users
                    addUsersTable(user);
                    console.log("Users found");
                }
                else {
                    alert("No Users found");
                }
            }
            catch (error) {
                window.location.href = "/home";
            }
        });
    }
    /**
     * Sends a POST request to the server to create a new user.
     *
     * @param {string} userName - The username for the new user.
     * @param {string} password - The password for the new user.
     * @return {Promise<void>} A Promise that resolves when the request is done.
     *
     * @example
     * await createUser("username", "userPassword").then(() => {
     *      window.location.reload();
     * });
     *
     * @remarks
     * The page will reload if the request is successful.
     * An alert is displayed if the request fails.
     * In case of a network error, the user will be redirected to the home page.
     */
    function createUser(userName, password) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/users`, {
                    method: 'POST',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ username: userName, password: password })
                });
                if (res.ok) {
                    console.log("User created successfully");
                    window.location.reload();
                }
                else {
                    alert("Failed to create User");
                }
            }
            catch (error) {
                window.location.href = "/home";
            }
        });
    }
    /**
     * Loads all users from the server (GET) and displays them in the users table.
  
     *
     * @return {Promise<void>} A promise that resolves when the query is done.
     *
     * @remarks
     * - If the query is successful, the users are added to the table
     * - If the request fails, a alert is displayed that informs the user that there are no users.
     * - If an error happens during the query, the user is redirected to the home page.
     *
     * @example
     * await loadUsers();
     *
     * @see addUsersTable
     *
     */
    function loadUsers() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/users`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    }
                });
                if (res.ok) {
                    const users = yield res.json();
                    //Show Users
                    addUsersTable(users);
                    console.log(users);
                }
                else {
                    alert("Currently there are no users.");
                }
            }
            catch (error) {
                window.location.href = "/home";
            }
        });
    }
    /**
     * Generates a new HTML table row element for a specified user.
     *
     * @param {User} user - The user object to create the row for.
     * @return {HTMLTableRowElement} The newly created table row.
     *
     * @description
     * This function creates a new table row element with the cells: UserID, Username, Edit and Delete buttons.
     *
     * @remarks
     * - The function creates a "Delete" button that calls the `deleteUser` function when clicked.
     * - It creates an "Edit" button that opens a modal for editing the user.
     * - The "Edit" button fills the modal with the user's current data.
     *
     * @example
     * const user: User = { user_id: 1, username: "ricardo" };
     * const row = createUserRow(user);
     * document.getElementById('tableBody').appendChild(row);
     *
     * @see deleteUser
     *
     */
    function createUserRow(user) {
        const row = document.createElement('tr');
        const th = document.createElement('th');
        th.scope = 'row';
        th.textContent = String(user.user_id);
        row.appendChild(th);
        const usernameElement = document.createElement('td');
        usernameElement.textContent = user.username;
        row.appendChild(usernameElement);
        const managment = document.createElement('td');
        const btnGroup = document.createElement('div');
        btnGroup.className = 'btn-group';
        btnGroup.role = 'group';
        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.className = 'btn btn-danger';
        deleteButton.textContent = 'Delete';
        deleteButton.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            yield deleteUser(user.user_id);
        }));
        btnGroup.appendChild(deleteButton);
        const editButton = document.createElement('button');
        editButton.type = 'button';
        editButton.className = 'btn btn-success';
        editButton.textContent = 'Edit';
        editButton.setAttribute('data-bs-toggle', 'modal');
        editButton.setAttribute('data-bs-target', '#modalEditUser');
        editButton.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            const userIdInput = document.getElementById('user-edit-id');
            const usernameInput = document.getElementById('username-edit');
            if (userIdInput && usernameInput) {
                userIdInput.value = String(user.user_id);
                usernameInput.value = user.username;
                if (!userIdInput.value) {
                    console.log(`user_id is empty: ${user.user_id}`);
                }
            }
        }));
        //Add the buttons to the row
        btnGroup.appendChild(editButton);
        managment.appendChild(btnGroup);
        row.appendChild(managment);
        return row;
    }
    /**
     * Adds a body to the table with the id users.
     *
     * @param {User[]} users - An array of user objects to be listed in the table.
     * @return {void} This function returns nothing. It displays the content directly in the HTML.
     *
     * @description
     * This function clears the existing table body and adds new rows for each user
     *
     * @see createUserRow
     *
     */
    function addUsersTable(users) {
        const tableBody = document.getElementById("users");
        if (tableBody) {
            tableBody.innerHTML = "";
            users.forEach(user => {
                const userRow = createUserRow(user);
                tableBody.appendChild(userRow);
            });
        }
    }
    /**
     * Sends a delete request to the server to delete a user with the specified user_id.
     *
     * @param {number} user_id - The ID of the user you want to delete.
     * @return {Promise<void>} A Promise that resolves when the delete request is done.
     *
     * @remarks
     * This function sends a DELETE request to the server and handles the response.
     * If successful, it displays an alert and reloads the page.
     * If unsuccessful, it displays an error alert.
     * In case of a network error, it redirects to the home page.
     *
     */
    function deleteUser(user_id) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/users/${user_id}`, {
                    method: 'DELETE',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    alert("User with ID:" + user_id + " has been deleted!");
                    window.location.reload();
                }
                else {
                    alert("Failed to delete User");
                }
            }
            catch (error) {
                window.location.href = "/home";
            }
        });
    }
    /**
     * Sends a PATCH request to the server to save changes to a user's account.
     *
     * @param {string} username - The new username for the user.
     * @param {number} user_id - The user's ID to update the user's data.
     * @param {string} userPassword - The new password for the user.
     * @return {Promise<void>} A Promise that resolves when the delete request is done.
     *
     * @example
     * await saveChanges("Robin", 2, "newPassword");
     *
     * @remarks
     * If the request is successful, the page will reload.
     * If the request fails, an alert will be displayed.
     * In case of a network error, the user will be redirected to the home page.
     */
    function saveChanges(username, user_id, userPassword) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/users/${user_id}`, {
                    method: 'PATCH',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ username: username, password: userPassword })
                });
                if (res.ok) {
                    window.location.reload();
                }
                else {
                    alert("Failed to edit User");
                }
            }
            catch (error) {
                window.location.href = "/home";
            }
        });
    }
}));
//# sourceMappingURL=admin.js.map