var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
// the URL with which a user is redirected to the album
const albumURL = 'http://localhost:8888/home/album/';
document.addEventListener('DOMContentLoaded', () => __awaiter(this, void 0, void 0, function* () {
    //Load Albums
    yield loadAlbums();
    //Refresh Albums
    const refreshBtn = document.getElementById("refreshAlbums");
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("refreshed");
            const container = document.getElementById("albums");
            container.innerHTML = null;
            yield loadAlbums();
        }));
    }
    const getAllPhotos = document.getElementById("showAllPhotos");
    if (getAllPhotos) {
        getAllPhotos.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("All Photos button clicked");
            const albumUrl = new URL(albumURL);
            albumUrl.searchParams.append('allPhotos', "true"); //pass as parameter true to display all photos
            window.location.href = albumUrl.toString();
        }));
    }
    //Load searched Albums only
    const searchBtn = document.getElementById("searchButton");
    if (searchBtn) {
        searchBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Search button clicked");
            const albumTitleInput = document.getElementById('searchInputTitle');
            if (albumTitleInput) {
                const albumTitle = albumTitleInput.value.trim();
                if (albumTitle) {
                    const container = document.getElementById("albums");
                    container.innerHTML = null;
                    yield searchAlbum(albumTitle);
                }
            }
            else {
                console.error("Album title input not found");
            }
        }));
    }
    //Update Album
    const saveChangesBtn = document.getElementById('editAlbumToDB');
    if (saveChangesBtn) {
        saveChangesBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Edit Album button clicked");
            const albumTitleInput = document.getElementById('album-edit-title');
            const hiddenAlbumIdInput = document.getElementById('album-edit-id');
            const albumKeywordsInput = document.getElementById('album-edit-keywords');
            if (albumTitleInput && albumKeywordsInput) {
                const albumTitle = albumTitleInput.value.trim();
                const albumKeywords = albumKeywordsInput.value.trim();
                //update album details when all necessary inputs are filled
                if (albumTitle && hiddenAlbumIdInput && albumKeywordsInput) {
                    yield saveChanges(albumTitle, parseInt(hiddenAlbumIdInput.value), albumKeywords);
                }
                else {
                    alert("Every Album needs a title!");
                }
            }
            else {
                console.error("Album title input not found");
            }
        }));
    }
    else {
        console.error("Edit Album button not found");
    }
    // Create Album
    const addAlbumBtn = document.getElementById('addAlbumToDB');
    if (addAlbumBtn) {
        addAlbumBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Add Album button clicked");
            const albumTitleInput = document.getElementById('album-title');
            const albumKeywordsInput = document.getElementById('album-keywords'); //optional
            if (albumTitleInput && albumKeywordsInput) {
                const albumTitle = albumTitleInput.value.trim();
                const albumKeywords = albumKeywordsInput.value.trim();
                // create a album when all necessary inputs are filled
                if (albumTitle) {
                    yield createAlbum(albumTitle, albumKeywords);
                    albumTitleInput.value = "";
                    albumKeywordsInput.value = "";
                }
                else {
                    alert("Every Album needs a title!");
                }
            }
            else {
                console.error("Album title input not found");
            }
        }));
    }
    else {
        console.error("Add Album button not found");
    }
    //Search Album
    /**
     * Searches for albums by title and keyword.
     *
     * @param {string} searched_string - The string to search for albums (title and keywords).
     * @return {Promise<void>} A Promise that resolves when the search is complete.
     *
     * @description
     * This function sends a GET request to the server to search for albums.
     * - The string provided can be a substring of the album's title or keywords.
     *
     * @remarks
     * - If the request is successful, the found albums are displayed using the `addAlbums` function.
     * - If the request fails, an alert is displayed.
     * - If a network error occurs, the error is logged in the console.
     *
     * @example
     * await searchAlbum("search_string");
     *
     * @see addAlbums
     *
     */
    function searchAlbum(searched_string) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/search/${searched_string}`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    const albums = yield res.json();
                    //Show Albums
                    addAlbums(albums);
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
    /**
     * Creates a new album with the given title and the optional given keywords.
     *
     * @param {string} title - The title of the new album.
     * @param {string} [albumKeywords=""] - The keywords associated with the album (optional).
     * @return {Promise<void>} A Promise that resolves after the album creation process is complete.
     *
     * @remark
     * - If the album is created successfully, the new album will be displayed using the `createAlbumCard` function.
     * And the `assignOrUnsignKeywords` function will be called.
     * - If the album creation fails, an alert is displayed.
     * - If a network error occurs, the error is logged in the console.
     *
     * @example
     * await createAlbum("title");
     *
     * @see assignOrUnsignKeywords
     *
     */
    function createAlbum(title_1) {
        return __awaiter(this, arguments, void 0, function* (title, albumKeywords = "") {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums`, {
                    method: 'POST',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ title: title })
                });
                if (res.ok) {
                    const albumJson = yield res.json();
                    console.log("Album created successfully");
                    console.log(albumJson);
                    yield assignOrUnsignKeywords(albumJson.album_id, albumKeywords);
                    createAlbumCard(title, albumJson.album_id);
                }
                else {
                    alert("Failed to create album");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    /**
     * Loads and displays all user albums from the server (GET).
     *
     * @return {Promise<void>} A Promise that resolves when the albums are loaded.
     *
     * @description
     * - If the request is successful, the fetched albums are displayed using the `addAlbums` function.
     * - If the request fails, an alert is displayed.
     * - If a network error occurs, the error is logged in the console.
     *
     * @example
     * await loadAlbums();
     *
     * @see addAlbums
     *
     */
    function loadAlbums() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    }
                });
                if (res.ok) {
                    const albums = yield res.json();
                    //Show Albums
                    addAlbums(albums);
                    console.log(albums);
                }
                else {
                    alert("You currently dont have any albums, click the green button to add one ;)");
                }
            }
            catch (error) {
                console.error("Error fetching albums:", error);
            }
        });
    }
    /**
     * Create a card for each album in the given array.
     *
     * @param {Album[]} albums - The array of albums to be added.
     * @return {void} No return value.
     */
    function addAlbums(albums) {
        albums.forEach(album => {
            createAlbumCard(album.title, album.album_id, album.imgUrl);
        });
    }
    /**
     * Creates an album card to present the given album.
     *
     * @param {string} title - The title of the album.
     * @param {number} album_id - The ID of the album.
     * @param {string} [imgUrl="https://placehold.co/600x400"] - The URL of the album's image. Image is never displayed.
     * @return {void} No return value.
     */
    function createAlbumCard(title, album_id, imgUrl = "https://placehold.co/600x400") {
        const container = document.getElementById("albums");
        //const link = document.createElement("a");
        //link.style.textDecoration = 'none';
        //link.href = "/home/album";
        //Card
        const albumDiv = document.createElement("div");
        albumDiv.className = "col";
        const cardDiv = document.createElement('div');
        cardDiv.className = 'card shadow-sm';
        //Image Element
        const img = document.createElement('img');
        img.className = 'card-img-top';
        img.setAttribute('width', '100%');
        img.setAttribute('height', '225');
        img.setAttribute('src', imgUrl);
        img.setAttribute('alt', title);
        //Album Title
        const cardBody = document.createElement('div');
        cardBody.className = 'card-body';
        const cardText = document.createElement('h3');
        cardText.className = 'card-text';
        cardText.textContent = title;
        cardBody.appendChild(cardText);
        //Buttons
        const cardButtons = document.createElement('div');
        cardButtons.className = 'd-flex justify-content-between align-items-center';
        const btnGroup = document.createElement('div');
        btnGroup.className = 'btn-group';
        const editButton = document.createElement('button');
        editButton.type = 'button';
        editButton.className = 'btn btn-sm btn-outline-secondary';
        editButton.textContent = 'Edit';
        editButton.setAttribute('data-bs-toggle', 'modal');
        editButton.setAttribute('data-bs-target', '#modalEditAlbum');
        editButton.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            const albumTitleInput = document.getElementById('album-edit-title');
            const hiddenAlbumIdInput = document.getElementById('album-edit-id');
            const albumKeywordsInput = document.getElementById('album-edit-keywords');
            // prepare the edit form with the album details
            if (albumTitleInput && hiddenAlbumIdInput && albumKeywordsInput) {
                albumTitleInput.value = title;
                hiddenAlbumIdInput.value = String(album_id);
                albumKeywordsInput.value = yield loadKeywordsFromAlbum(album_id);
                if (hiddenAlbumIdInput.value == null || hiddenAlbumIdInput.value == "") {
                    console.log("album_ID is empty: " + album_id);
                }
            }
        }));
        btnGroup.appendChild(editButton);
        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.className = 'btn btn-sm btn-outline-danger';
        deleteButton.textContent = 'Delete';
        deleteButton.addEventListener('click', (event) => {
            deleteAlbum(album_id);
        });
        const viewPhotosButton = document.createElement('button');
        viewPhotosButton.type = 'button';
        viewPhotosButton.className = 'btn btn-sm btn-outline-success';
        viewPhotosButton.textContent = 'View Photos';
        viewPhotosButton.setAttribute('data-bs-toggle', 'modal');
        viewPhotosButton.setAttribute('data-bs-target', '#modalViewPhotos');
        //Transmission of the albumID to the other page
        viewPhotosButton.addEventListener('click', () => {
            const albumUrl = new URL(albumURL);
            albumUrl.searchParams.append('albumId', album_id.toString());
            window.location.href = albumUrl.toString();
        });
        btnGroup.appendChild(deleteButton);
        btnGroup.appendChild(viewPhotosButton);
        cardButtons.appendChild(btnGroup);
        //Merge Components
        cardBody.appendChild(cardButtons);
        //Responsiable for displaying Album Cover
        //cardDiv.appendChild(img);
        cardDiv.appendChild(cardBody);
        albumDiv.appendChild(cardDiv);
        //link.appendChild(albumDiv);
        container.appendChild(albumDiv);
    }
    /**
     * Sends a DELETE request to the server to remove a specific album
     *
     * @param {number} albumId - The ID of the album you want to delete.
     * @return {Promise<void>} A promise that resolves when the album is deleted or fails.
     *
     *
     * @remarks
     * - If the request is successful, the album is removed from the DOM.
     * - If the request fails, an alert is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await deleteAlbum(1);
     *
     * @see loadAlbums
     *
     */
    function deleteAlbum(albumId) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${albumId}`, {
                    method: 'DELETE',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    const container = document.getElementById("albums");
                    container.innerHTML = null;
                    loadAlbums();
                    alert("Album with ID:" + albumId + " has been deleted!");
                }
                else {
                    alert("failed to delete Album");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    /**
     * Update the details of a album.
     *
     * @param {string} albumTitle - The title of the album.
     * @param {number} albumId - The ID of the album.
     * @param {string} albumKeywords - The album's keywords.
     * @return {Promise<void>} A Promise that resolves after saving the changes.
     *
     *
     * @remarks
     * - If the request is successful, window is reloaded after assignOrUnsignKeyword has finished.
     * - If the request fails, an alert is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await saveChanges("title", 1, "keywords");
     *
     */
    function saveChanges(albumTitle, albumId, albumKeywords) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${albumId}`, {
                    method: 'PUT',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ title: albumTitle })
                });
                if (res.ok) {
                    const albumJson = yield res.json();
                    console.log(albumJson);
                    //Reload after assignOrUnsignKeywords has finished
                    yield assignOrUnsignKeywords(albumId, albumKeywords).then(() => window.location.reload() //reload page
                    );
                }
                else {
                    alert("Failed to edit album");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    //Keywords
    /**
     * Assigns or unsigns keywords to a album.
     *
     * @param {number} album_id - The ID of the album.
     * @param {string} keywordsNew - The new keywords to assign.
     * @return {Promise<void>} A Promise that resolves when the keywords are assigned or unassigned.
     *
     * @description
     * Compares the new keyword set with the existing keywords for a album,
     * then assigns new keywords and/or removes old keywords as needed.
     *
     * @remarks
     * - Keywords are assigned if
     *  1. they are present in the new keyword chain but not in the existing keywords.
     * - Keywords will not be part of the new keyword chain if
     *    1. they are present in the existing keywords, but not in the new keyword chain.
     *    2. the new tag chain is empty, removing all existing keywords.
     *
     * @example
     * await assignOrUnsignKeywords(1, "keyword1;keyword2;keyword3");
     *
     * @see unsignKeywords
     * @see assignKeywords
     * @see loadKeywordsFromAlbum
     *
     */
    function assignOrUnsignKeywords(album_id, keywordsNew) {
        return __awaiter(this, void 0, void 0, function* () {
            const keywordsOld = yield loadKeywordsFromAlbum(album_id);
            const oldKeywordsArray = keywordsOld.split(";").map(keyword => keyword.trim());
            //Keywords given then assign them to album
            if (keywordsNew != null && keywordsNew != "") {
                //Convert String to multiple Keywords
                const newKeywordsArray = keywordsNew.split(";").map(keyword => keyword.trim());
                //Filter new Keywords and Keywords to remove
                const oldKeywordsSet = new Set(oldKeywordsArray);
                const newKeywordsSet = new Set(newKeywordsArray);
                const addKeywords = newKeywordsArray.filter(keyword => !oldKeywordsSet.has(keyword)); //Keyword was not in String before
                const removeKeywords = oldKeywordsArray.filter(keyword => !newKeywordsSet.has(keyword)); //Keyword is not anymore in String
                yield assignKeywords(album_id, addKeywords);
                yield unsignKeywords(album_id, removeKeywords);
            }
            else { //when last Keyword should remove
                yield unsignKeywords(album_id, oldKeywordsArray);
            }
        });
    }
    /**
     * Assigns keywords to a photo.
     *
     * @param {number} album_id - The ID of the album to assign keywords to.
     * @param {string[]} keywordsArray - An array of keywords to assign to the album.
     * @return {Promise<void>} A Promise that resolves when the keywords are assigned.
     *
     * @description
     * This function assign the given keywords to the album with the album_id.
     *
     * @remarks
     * - If the request is successful, the keywords are assigned to the album and the response is printed in the console.
     * - If the request fails, a message in the console is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await assignKeywords(1, ["keyword1", "keyword2", "keyword3"]);
     *
     */
    function assignKeywords(album_id, keywordsArray) {
        return __awaiter(this, void 0, void 0, function* () {
            //Assign Keywords
            for (const keyword of keywordsArray) {
                try {
                    const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/keywords/${keyword}`, {
                        method: 'POST',
                        mode: 'cors',
                        credentials: 'include'
                    });
                    if (res.ok) {
                        const keywordJson = yield res.json();
                        console.log(keywordJson);
                    }
                    else {
                        console.error("Failed to assign Keyword to Album!");
                    }
                }
                catch (error) {
                    console.error("Error occured ", error);
                }
            }
        });
    }
    /**
     * Removes the given keywords from the given album.
     *
     * @param {number} album_id - The ID of the album from which you want to remove keywords.
     * @param {string[]} keywordsArray - An array of keywords to remove from the album.
     * @return {Promise<void>} A Promise that resolves when the keywords are removed.
     *
     *
     * @remarks
     * - If the request is successful, print the response in the console.
     * - If the request fails, an alert is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await unsignKeywords(1, ["keyword1", "keyword2", "keyword3"]);
     *
     */
    function unsignKeywords(album_id, keywordsArray) {
        return __awaiter(this, void 0, void 0, function* () {
            //Unsign Keywords
            for (const keyword of keywordsArray) {
                try {
                    const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/keywords/${keyword}`, {
                        method: 'DELETE',
                        mode: 'cors',
                        credentials: 'include'
                    });
                    if (res.ok) {
                        const keywordJson = yield res.json();
                        console.log(keywordJson);
                    }
                    else {
                        console.error("Failed to Unsign Keyword from Album!");
                    }
                }
                catch (error) {
                    console.error("Error occured ", error);
                }
            }
        });
    }
    /**
     * Loads the keywords from an album with the given ID.
     *
     * @param {number} album_id - The ID of the album from which to load keywords.
     * @return {Promise<string>} A promise that resolves to a string of keywords separated by semicolons.
     *
     * @remarks
     * - If the request is successful, the keywords are returned as a string separated by semicolons.
     * - If the request fails, an message in the console is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await loadKeywordsFromAlbum(1);
     *
     */
    function loadKeywordsFromAlbum(album_id) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/keywords`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                if (res.ok) {
                    const keywordJson = yield res.json();
                    console.log(keywordJson);
                    return keywordJson.keywords.join(" ; ");
                }
                else {
                    console.error("Failed to load any Album!");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
            return "";
        });
    }
}));
//# sourceMappingURL=home.js.map