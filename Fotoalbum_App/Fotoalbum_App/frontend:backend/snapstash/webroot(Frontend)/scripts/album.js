var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
// the URL with which a user is redirected to the full screen
const photoURL = 'http://localhost:8888/home/album/photo/';
//load albumId:
const currentUrl = new URL(window.location.href);
// Parameter auslesen
const album_id = currentUrl.searchParams.get('albumId');
const showAllPhotos = currentUrl.searchParams.get('allPhotos');
document.addEventListener('DOMContentLoaded', () => __awaiter(this, void 0, void 0, function* () {
    //Load Photos
    yield loadPhotos();
    //Show Album Keywords
    const albumKeywords = yield loadKeywordsFromAlbum(parseInt(album_id));
    const albumKeywordsLabel = document.getElementById("album-keywords");
    albumKeywordsLabel.textContent = albumKeywords;
    //Refresh Photos
    const refreshBtn = document.getElementById("refreshPhotos");
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("refreshed");
            const container = document.getElementById("photos");
            container.innerHTML = null;
            yield loadPhotos();
        }));
    }
    //Load searched Photos only
    const searchBtn = document.getElementById("searchButton");
    if (searchBtn) {
        searchBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Search button clicked");
            const searched_stringInput = document.getElementById('searchInput');
            if (searched_stringInput) {
                const searched_string = searched_stringInput.value.trim();
                if (searched_string) {
                    const container = document.getElementById("photos");
                    container.innerHTML = null;
                    yield searchPhoto(searched_string);
                }
            }
            else {
                console.error("Photo title input not found");
            }
        }));
    }
    //Update Photo
    const saveChangesBtn = document.getElementById('editPhotoToDB');
    if (saveChangesBtn) {
        saveChangesBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Edit Photo button clicked");
            const photoTitleInput = document.getElementById('photo-edit-title');
            const photoCreationInput = document.getElementById('photo-creationDate');
            const hiddenPhotoIdInput = document.getElementById('photo-edit-id');
            const photoKeywordsInput = document.getElementById('photo-edit-keywords'); //optional
            if (photoTitleInput && photoCreationInput && photoKeywordsInput) {
                const photoTitle = photoTitleInput.value.trim();
                const photoKeywords = photoKeywordsInput.value.trim();
                //update photo details when all necessary inputs are filled
                if (photoTitle && hiddenPhotoIdInput && photoKeywordsInput) {
                    yield saveChanges(photoTitle, parseInt(hiddenPhotoIdInput.value), photoCreationInput.value, photoKeywords);
                }
                else {
                    alert("Every Photo needs a title!");
                }
            }
            else {
                alert("Please insert a creation Date");
            }
        }));
    }
    else {
        console.error("Edit Photo button not found");
    }
    // Create Photo
    const addPhotoBtn = document.getElementById('addPhotoToDB');
    if (addPhotoBtn) {
        addPhotoBtn.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            console.log("Add Photo button clicked");
            const photoTitleInput = document.getElementById('photo-title');
            const fileInput = document.getElementById('photoUpload');
            const photoKeywordsInput = document.getElementById('photo-keywords'); //optional
            if (photoTitleInput && fileInput.files && fileInput.files.length > 0 && photoKeywordsInput) {
                const photoTitle = photoTitleInput.value.trim();
                const image = fileInput.files[0];
                const photoKeywords = photoKeywordsInput.value.trim();
                // create a photo when all necessary inputs are filled
                if (photoTitle && image) {
                    yield createPhoto(photoTitle, image, photoKeywords);
                    photoTitleInput.value = "";
                    fileInput.value = "";
                    photoKeywordsInput.value = "";
                }
                else {
                    alert("Every Photo needs a title!");
                }
            }
            else {
                alert("Please select a file");
            }
        }));
    }
    else {
        console.error("Add Photo button not found");
    }
    /**
     * Fetches all photos from the server and displays them
     *
     * @return {Promise<void>} A Promise that resolves when the request is done.
     *
     * @description
     * This function sends a GET request to retrieve all photos from the server.
     * If successful, it displays the photos using the `addPhotos` function.
     *
     * @remarks
     * - If the request is successful, create a table with the users found.
     * - Displays an alert if no photos are found.
     * - If a network error occurs, the error will be logged in the console.
     *
     *
     * @example
     * await getAllPhotosFunc();
     *
     * @see addPhotos
     *
     */
    function getAllPhotosFunc() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/allPhotos/`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    const photos = yield res.json();
                    //Show Photos
                    addPhotos(photos);
                    console.log("Photo found");
                }
                else {
                    alert("No Photo found");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    //Search Photo
    /**
     * Searches for photos in an album by title and keyword.
     *
     * @param {string} searched_string - The string to search for in the album's photos (title and keywords).
     * @return {Promise<void>} A Promise that resolves when the search is complete.
     *
     * @description
     * This function sends a GET request to the server to search for photos in an album.
     * - The string provided can be a substring of the photo's title or keywords.
     *
     * @remarks
     * - If the request is successful, the found photos are displayed using the `addPhotos` function.
     * - If the request fails, an alert is displayed.
     * - If a network error occurs, the error is logged in the console.
     *
     * @example
     * await searchPhoto("search_string");
     *
     * @see addPhotos
     *
     */
    function searchPhoto(searched_string) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos/search/${searched_string}`, {
                    method: 'GET',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    const photos = yield res.json();
                    //Show Photos
                    addPhotos(photos);
                    console.log("Photo found");
                }
                else {
                    alert("No Photo found");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    /**
     * Creates a new photo with the given title and the given photo file name. Optionally, keywords can be assigned to the photo.
     *
     * @param {string} title - The title of the photo.
     * @param {File} photo - The photo file to be uploaded.
     * @param {string} [photoKeywords=""] - The keywords to assign to the photo (optional). Should divide by ";".
     * @return {Promise<void>} A Promise that resolves when the photo is created successfully.
     *
     *
     * @remark
     * - If the photo is created successfully, the window is reloaded.
     * - If the photo creation fails, an alert is displayed.
     * - If a network error occurs, the error is logged in the console.
     * - The photo file and title are sent as FormData.
     *
     * @example
     * await createPhoto("title", image, "keyword; keyword2; keyword3");
     *
     * @see assignOrUnsignKeywords
     *
     */
    function createPhoto(title_1, photo_1) {
        return __awaiter(this, arguments, void 0, function* (title, photo, photoKeywords = "") {
            try {
                const formData = new FormData();
                //append Data to FormData
                formData.append("title", title);
                formData.append("photo", photo);
                const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos`, {
                    method: 'POST',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {},
                    body: formData
                });
                if (res.ok) {
                    const photoJson = yield res.json();
                    console.log("Photo created successfully");
                    console.log(photoJson);
                    yield assignOrUnsignKeywords(photoJson.photo_id, photoKeywords);
                    window.location.reload();
                    //createPhotoCard(photoJson.title, photoJson.photo_id, photoJson.album_id, "",photoJson.url);
                }
                else {
                    alert("Failed to create photo");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    /**
     * Loads and displays photos from current album or all.
     *
     * @return {Promise<void>} A Promise that resolves when the photos are loaded and displayed.
     *
     * @description
     * This function handles two scenarios:
     * 1. Loading all photos across albums when `showAllPhotos` is "true".
     * 2. Loading photos for a specific album when `album_id` is provided.
     *
     * @example
     * // To load all photos
     * showAllPhotos = "true"; //const showAllPhotos = currentUrl.searchParams.get('allPhotos');
     * await loadPhotos();
     * // To load photos for a specific album
     * album_id = 123; //const album_id = currentUrl.searchParams.get('albumId');
     * showAllPhotos = "false";
     * await loadPhotos();
     *
     * @see getAllPhotosFunc
     * @see addPhotos
     * @see Photo
     *
     */
    function loadPhotos() {
        return __awaiter(this, void 0, void 0, function* () {
            if (showAllPhotos == "true") {
                //remove components
                const addPhotoButton = document.getElementById("addPhoto");
                const keywordsDiv = document.getElementById("keywordsDiv");
                const searchForm = document.getElementById("searchForm");
                addPhotoButton.remove();
                keywordsDiv.remove();
                searchForm.remove();
                //load Photos
                yield getAllPhotosFunc();
            }
            else if (album_id) {
                try {
                    const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos`, {
                        method: 'GET',
                        mode: 'cors',
                        credentials: 'include',
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });
                    // check response status and redirect if necessary for example if user is not logged in
                    if (res.redirected) {
                        window.location.href = res.url;
                    }
                    else if (res.ok) {
                        const photos = yield res.json();
                        //Show Photos
                        addPhotos(photos);
                        console.log(photos);
                        console.log(photos.length);
                    }
                    else {
                        alert("You currently dont have any photos, click the green button to add one ;)");
                    }
                }
                catch (error) {
                    console.error("Error fetching photos:", error);
                }
            }
        });
    }
    /**
     * Create a card for each photo in the given array.
     *
     * @param {Photo[]} photos - The array of photos to be added.
     * @return {void} No return value.
     */
    function addPhotos(photos) {
        photos.forEach(photo => {
            createPhotoCard(photo.title, photo.photo_id, photo.album_id, photo.creation_date, photo.url);
        });
    }
    /**
     * Create a photo card to present the given photo.
     *
     * @param {string} title - The title of the photo.
     * @param {number} photo_id - The ID of the photo.
     * @param {number} album_id - The ID of the album that contains the photo.
     * @param {string} [creation_date=""] - Photo creation date.
     * @param {string} [url="https://placehold.co/600x400"] - The URL of the photo.
     * @return {void} No return value.
     */
    function createPhotoCard(title, photo_id, album_id, creation_date = "", url = "https://placehold.co/600x400") {
        const container = document.getElementById("photos");
        //const link = document.createElement("a");
        //link.style.textDecoration = 'none';
        //link.href = "/home/photo";
        //Card
        const photoDiv = document.createElement("div");
        photoDiv.className = "col";
        const cardDiv = document.createElement('div');
        cardDiv.className = 'card shadow-sm';
        //Image Element
        const img = document.createElement('img');
        img.className = 'card-img-top';
        img.setAttribute('width', '100%');
        img.setAttribute('height', '225');
        img.setAttribute('src', url);
        img.setAttribute('alt', title);
        //Photo Title
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
        editButton.setAttribute('data-bs-target', '#modalEditPhoto');
        editButton.addEventListener('click', () => __awaiter(this, void 0, void 0, function* () {
            const photoTitleInput = document.getElementById('photo-edit-title');
            const photoCreationDateInput = document.getElementById('photo-creationDate');
            const hiddenPhotoIdInput = document.getElementById('photo-edit-id');
            const photoKeywordsInput = document.getElementById('photo-edit-keywords');
            // prepare the edit form with the photo details
            if (photoTitleInput && hiddenPhotoIdInput && photoCreationDateInput) {
                photoTitleInput.value = title;
                photoCreationDateInput.value = creation_date;
                hiddenPhotoIdInput.value = String(photo_id);
                photoKeywordsInput.value = yield loadKeywordsFromPhoto(photo_id);
                if (hiddenPhotoIdInput.value == null || hiddenPhotoIdInput.value == "") {
                    console.log("photo_ID is empty: " + photo_id);
                }
            }
        }));
        btnGroup.appendChild(editButton);
        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.className = 'btn btn-sm btn-outline-danger';
        deleteButton.textContent = 'Delete';
        deleteButton.addEventListener('click', (event) => {
            deletePhoto(photo_id);
        });
        const viewPhotosButton = document.createElement('button');
        viewPhotosButton.type = 'button';
        viewPhotosButton.className = 'btn btn-sm btn-outline-success';
        viewPhotosButton.textContent = 'Full Screen';
        viewPhotosButton.addEventListener('click', () => {
            //Transmission of the photoID and albumID to the other page
            const photoUrl = new URL(photoURL);
            photoUrl.searchParams.append('album_id', album_id.toString());
            photoUrl.searchParams.append('photo_id', photo_id.toString());
            window.location.href = photoUrl.toString();
        });
        // remove the buttons when showAllPhotos is "true"
        if (showAllPhotos != "true") {
            btnGroup.appendChild(deleteButton);
            btnGroup.appendChild(viewPhotosButton);
            cardButtons.appendChild(btnGroup);
            cardBody.appendChild(cardButtons);
        }
        //Merge Components
        cardDiv.appendChild(img);
        cardDiv.appendChild(cardBody);
        photoDiv.appendChild(cardDiv);
        //link.appendChild(photoDiv);
        container.appendChild(photoDiv);
    }
    /**
     * Sends a DELETE request to the server to remove a photo from an album.
     *
     * @param {number} photoId - The ID of the photo you want to delete.
     * @return {Promise<void>} A promise that resolves when the photo is deleted or fails.
     *
     *
     * @remarks
     * - If the request is successful, the photo is removed from the DOM.
     * - If the request fails, an alert is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await deletePhoto(1);
     *
     * @see LoadPhotos
     *
     */
    function deletePhoto(photoId) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos/${photoId}`, {
                    method: 'DELETE',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                });
                if (res.ok) {
                    const container = document.getElementById("photos");
                    container.innerHTML = null;
                    loadPhotos();
                    alert("Photo with ID:" + photoId + " has been deleted!");
                }
                else {
                    alert("failed to delete Photo");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    /**
     * Update the details of a photo.
     *
     * @param {string} photoTitle - The title of the photo.
     * @param {number} photoId - The ID of the photo.
     * @param {string} photoCreationDate - The creation date of the photo.
     * @param {string} photoKeywords - The photo's keywords.
     * @return {Promise<void>} A Promise that resolves after saving the changes.
     *
     *
     * @remarks
     * - If the request is successful, window is reloaded.
     * - If the request fails, an alert is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await saveChanges("title", 1, "creation_date", "keywords");
     *
     */
    function saveChanges(photoTitle, photoId, photoCreationDate, photoKeywords) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos/${photoId}`, {
                    method: 'PATCH',
                    mode: 'cors',
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ title: photoTitle, creation_date: photoCreationDate })
                });
                if (res.ok) {
                    const photoJson = yield res.json();
                    console.log("Photo edited successfully");
                    console.log(photoJson);
                    yield assignOrUnsignKeywords(photoId, photoKeywords).then(() => window.location.reload() //reload page
                    );
                }
                else {
                    alert("Failed to edit photo");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
        });
    }
    //Keywords
    /**
     * Assigns or unsigns keywords to a photo.
     *
     * @param {number} photo_id - The ID of the photo.
     * @param {string} keywordsNew - The new keywords to assign.
     * @return {Promise<void>} A Promise that resolves when the keywords are assigned or unassigned.
     *
     * @description
     * Compares the new keyword set with the existing keywords for a photo,
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
     * @see loadKeywordsFromPhoto
     *
     */
    function assignOrUnsignKeywords(photo_id, keywordsNew) {
        return __awaiter(this, void 0, void 0, function* () {
            const keywordsOld = yield loadKeywordsFromPhoto(photo_id);
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
                yield assignKeywords(photo_id, addKeywords);
                yield unsignKeywords(photo_id, removeKeywords);
            }
            else {
                yield unsignKeywords(photo_id, oldKeywordsArray);
            }
        });
    }
    /**
     * Assigns keywords to a photo.
     *
     * @param {number} photo_id - The ID of the photo to assign keywords to.
     * @param {string[]} keywordsArray - An array of keywords to assign to the photo.
     * @return {Promise<void>} A Promise that resolves when the keywords are assigned.
     *
     * @description
     * This function assign the given keywords to the photo with the photo_id.
     *
     * @remarks
     * - If the request is successful, the keywords are assigned to the photo.
     * - If the request fails, a message in the console is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await assignKeywords(1, ["keyword1", "keyword2", "keyword3"]);
     *
     */
    function assignKeywords(photo_id, keywordsArray) {
        return __awaiter(this, void 0, void 0, function* () {
            //Assign Keywords
            for (const keyword of keywordsArray) {
                try {
                    const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos/${photo_id}/keywords/${keyword}`, {
                        method: 'POST',
                        mode: 'cors',
                        credentials: 'include'
                    });
                    if (res.ok) {
                        const keywordJson = yield res.json();
                        console.log(keywordJson);
                    }
                    else {
                        console.error("Failed to assign Keyword to Photo!");
                    }
                }
                catch (error) {
                    console.error("Error occured ", error);
                }
            }
        });
    }
    /**
     * Removes the given keywords from the given photo.
     *
     * @param {number} photo_id - The ID of the photo from which you want to remove keywords.
     * @param {string[]} keywordsArray - An array of keywords to remove from the photo.
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
    function unsignKeywords(photo_id, keywordsArray) {
        return __awaiter(this, void 0, void 0, function* () {
            //Unsign Keywords
            for (const keyword of keywordsArray) {
                try {
                    const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos/${photo_id}/keywords/${keyword}`, {
                        method: 'DELETE',
                        mode: 'cors',
                        credentials: 'include'
                    });
                    if (res.ok) {
                        const keywordJson = yield res.json();
                        console.log(keywordJson);
                    }
                    else {
                        console.error("Failed to Unsign Keyword from Photo!");
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
    /**
     * Loads the keywords from a photo with the ID you have given.
     *
     * @param {number} photo_id - The ID of the photo from which you want to load keywords.
     * @return {Promise<string>} A promise that resolves to a string of keywords separated by semicolons.
     *
     *
     * @remarks
     * - If the request is successful, the keywords are returned as a string separated by semicolons.
     * - If the request fails, an message in the console is displayed.
     * - Contains an error message in the console if a network error occurs.
     *
     * @example
     * await loadKeywordsFromPhoto(1);
     *
     */
    function loadKeywordsFromPhoto(photo_id) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${album_id}/photos/${photo_id}/keywords/`, {
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
                    console.error("Failed to load any Photos!");
                }
            }
            catch (error) {
                console.error("Error occured ", error);
            }
            return "";
        });
    }
}));
//# sourceMappingURL=album.js.map