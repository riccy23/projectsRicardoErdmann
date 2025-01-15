interface Photo {
  title: string;
  photo_id: number;
  album_id: number;
  creation_date: string;
  url: string;
}

//url to current photo
const currentPhotoUrl = new URL(window.location.href) ;

// read parameters from current url
const photoAlbum_id = currentPhotoUrl.searchParams.get('album_id');
const photo_id = currentPhotoUrl.searchParams.get('photo_id');

document.addEventListener('DOMContentLoaded', async () => {

  //Load Photos
  await loadPhoto();

  const photoKeywords = await loadKeywordsFromPhoto(parseInt(photoAlbum_id), parseInt(photo_id));
  const photoKeywordsLabel = document.getElementById("photo-keywords");
  photoKeywordsLabel.textContent = photoKeywords;


  /**
   * Load a specific photo from server and display it.
   *
   * @returns {Promise<void>} A promise that resolves when the photo is loaded.
   *
   * @description
   * - If redirected (User is not logged in), it navigates to the new URL.
   * - If successful, it processes and displays the photo.
   * - If no photos are found, it shows an alert.
   * 3. Logs errors that occur during the process.
   *
   * @example
   * await loadPhoto();
   *
   * @requires
   * - Global variables: photoAlbum_id, photo_id (path parameters)
   *
   * @see addPhotos
   *
   */
  async function loadPhoto() {
    try {
      const res = await fetch(`http://localhost:8888/home/albums/${photoAlbum_id}/photos/${photo_id}`, {
        method: 'GET',
        mode: 'cors',
        credentials: 'include',
        headers: {
          "Content-Type": "application/json"
        }
      });

      if (res.redirected) {
        window.location.href = res.url;
      } else if (res.ok) {
        const photo : Photo = await res.json();
        //Show Photos
        addPhotos(photo);
        console.log(photo);
      } else {
        alert("You currently dont have any photos, click the green button to add one ;)")
      }

    } catch (error) {
      console.error("Error fetching photos:", error);
    }
  }

  /**
   * Adds the specified photo data to the UI.
   *
   * @description
   * This function updates the UI with the photo data. And calls the getExif function to get the EXIF data.
   *
   * @param {Photo} photo - The photo object that contains the photo data.
   * @returns {void}
   *
   * @see getExif
   */
  function addPhotos(photo: Photo): void {
    //Show Photo data
    document.getElementById('imageTitle').innerText = photo.title;
    document.getElementById('imageCreationDate').innerText = photo.creation_date;
    const img = document.getElementById('imagePhoto') as HTMLImageElement;
    img.setAttribute('width', '100%');
    img.setAttribute('height', '225');
    img.setAttribute('src', photo.url);
    img.setAttribute('alt', photo.title);

    img.onload = () => {
      getExif(img);
    };

  }

  /**
   * Loads the keywords from a photo with the photoID and albumID you have given.
   *
   * @param {number} album_id - The ID of the album that contains the photo.
   * @param {number} photo_id - The ID of the photo from which you want to load keywords.
   * @return {Promise<string>} A promise that resolves to a string of keywords separated by semicolons.
   *
   *
   * @remarks
   * - If the request is successful, the keywords are returned as a string separated by semicolons.
   * - If the request fails, a message in the console is displayed.
   * - Contains an error message in the console if a network error occurs.
   *
   * @example
   * await loadKeywordsFromPhoto(1, 2);
   *
   */
  async function loadKeywordsFromPhoto(album_id:number, photo_id: number): Promise<string> {
    try {
      const res = await fetch(`http://localhost:8888/home/albums/${album_id}/photos/${photo_id}/keywords/`, {
        method: 'GET',
        mode: 'cors',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (res.ok) {
        const keywordJson: Keyword = await res.json();
        console.log(keywordJson);
        return keywordJson.keywords.join(" ; ");
      } else {
        console.error("Failed to load any Photos!");
      }
    } catch (error) {
      console.error("Error occured ", error);
    }
    return "";
  }

  /**
   * Extract EXIF data from an image, specifically GPS coordinates, and display them on a map.
   *
   * @param {HTMLImageElement} img - The image element where the EXIF data will be extracted.
   *
   * @description
   * This function performs the following steps:
   * 1. Extracts from the EXIF metadata of the image the GPS data (latitude and longitude).
   * 2. Converts GPS coordinates into decimals.
   * 3. Displays the coordinates in an HTML element named "geoCode".
   * 4. Creates a leaflet map centred on the extracted coordinates.
   * 5. Mark the position of the picture on the map.
   *
   * @requires EXIF - An external library for reading EXIF metadata.
   * @requires Leaflet - A OpenStreetMap library to display the map.
   *
   * @example
   * // Assuming you have an image element with id "myImage":
   * const img = document.getElementById("myImage");
   * getExif(img);
   *
   * @see convertGPSDataToDecimal
   *
   * @see https://leafletjs.com/index.html
   *
   */
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

        //Set the start view of the map and initiate the map
        const map = L.map('map').setView([lat, -lon], 13);

        //Add the OpenStreetMap tile layer
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        //Add the marker that shows the location of the picture
        L.marker([lat, lon]).addTo(map)
          .bindPopup('Picture was taken here.')
          .openPopup();

        //Set the map view to the location of the picture, so the marker is centered
        map.setView([lat, lon], 13);
      }
    });
  }
});

/**
 * Converts GPS coordinates from degrees, minutes, seconds format to decimal degrees.
 *
 * @param {[number, number, number] | null} coord - An array that contains degrees, minutes, and seconds, in that order.
 * @param {'N' | 'S' | 'E' | 'W'} ref - The reference to the Earth hemisphere. N' for North, 'S' for South, 'E' for East, 'W' for West.
 *
 * @returns {number | null} The converted coordinate in decimal deg or zero if the input coordinate is zero.
 *
 * @description
 * This function works as follows
 * 1. It checks to see if the input coordinate is zero. If it is, it returns zero.
 * 2. Divides the input array into degrees, minutes and seconds.
 * 3. Calculates the decimal degrees using the formula: degrees + (minutes / 60) + (seconds / 3600).
 * 4. Based on the hemisphere reference, adjusts the sign of the result.
 *
 * @example
 * const latitude = [42, 12, 46.23];
 * const latRef = 'N';
 * const decimalLat = convertGPSDataToDecimal(latitude, latRef);
 * console.log(decimalLat);
 *
 *
 * @copyright https://stackoverflow.com/a/1140335
 *
 */
function convertGPSDataToDecimal(coord, ref) {
  if (!coord) return null;

  const [degrees, minutes, seconds] = coord;
  let decimal = degrees + (minutes / 60) + (seconds / 3600);

  if (ref === 'S' || ref === 'W') {
    decimal = -decimal;
  }

  return decimal;
}
