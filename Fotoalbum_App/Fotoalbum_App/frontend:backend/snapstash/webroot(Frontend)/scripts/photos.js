var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
const currentPhotoUrl = new URL(window.location.href);
// Parameter auslesen
const photoAlbum_id = currentPhotoUrl.searchParams.get('album_id');
const photo_id = currentPhotoUrl.searchParams.get('photo_id');
document.addEventListener('DOMContentLoaded', () => __awaiter(this, void 0, void 0, function* () {
    //Load Photos
    yield loadPhoto();
    const photoKeywords = yield loadKeywordsFromPhoto(parseInt(photoAlbum_id), parseInt(photo_id));
    const photoKeywordsLabel = document.getElementById("photo-keywords");
    photoKeywordsLabel.textContent = photoKeywords;
    function loadPhoto() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const res = yield fetch(`http://localhost:8888/home/albums/${photoAlbum_id}/photos/${photo_id}`, {
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
                    const photo = yield res.json();
                    //Show Photos
                    addPhotos(photo);
                    console.log(photo);
                }
                else {
                    alert("You currently dont have any photos, click the green button to add one ;)");
                }
            }
            catch (error) {
                console.error("Error fetching photos:", error);
            }
        });
    }
    function addPhotos(photo) {
        //Show Photo data
        document.getElementById('imageTitle').innerText = photo.title;
        document.getElementById('imageCreationDate').innerText = photo.creation_date;
        const img = document.getElementById('imagePhoto');
        img.setAttribute('width', '100%');
        img.setAttribute('height', '225');
        img.setAttribute('src', photo.url);
        img.setAttribute('alt', photo.title);
        img.onload = () => {
            getExif(img);
        };
    }
    function loadKeywordsFromPhoto(album_id, photo_id) {
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
    function getExif(img) {
        EXIF.getData(img, function () {
            const latitude = EXIF.getTag(this, "GPSLatitude");
            const longitude = EXIF.getTag(this, "GPSLongitude");
            const latitudeRef = EXIF.getTag(this, "GPSLatitudeRef") || 'N';
            const longitudeRef = EXIF.getTag(this, "GPSLongitudeRef") || 'E';
            const geoCode = document.getElementById("geoCode");
            if (geoCode && latitude && longitude) {
                const lat = convertGPSDataToDecimal(latitude, latitudeRef);
                const lon = convertGPSDataToDecimal(longitude, longitudeRef);
                geoCode.innerHTML = `Latitude: ${lat}°, Longitude: ${lon}°`;
                const map = L.map('map').setView([lat, -lon], 13);
                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                }).addTo(map);
                L.marker([lat, lon]).addTo(map)
                    .bindPopup('Picture was taken here.')
                    .openPopup();
                map.setView([lat, lon], 13);
            }
        });
    }
}));
function convertGPSDataToDecimal(coord, ref) {
    if (!coord)
        return null;
    const [degrees, minutes, seconds] = coord;
    let decimal = degrees + (minutes / 60) + (seconds / 3600);
    if (ref === 'S' || ref === 'W') {
        decimal = -decimal;
    }
    return decimal;
}
//# sourceMappingURL=photos.js.map