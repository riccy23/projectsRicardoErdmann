
# SnapStash: The Innovative Photo Web Application

Welcome to SnapStash! This application allows users to upload and organize their photos. Once you start using SnapStash, you won't want to go back to Google Photos, Apple Photos, or any other photo service!

## Installation

Follow the steps below to install and set up SnapStash:

### 1. Clone the Repository

Clone the SnapStash repository from GitHub:

```bash
git clone https://git.thm.de/informatik-projekt/sose-2024-suess-rupp/gruppe-10.git
```

### 2. Open the Correct Directory

Open the following path in your favorite IDE:

```
/gruppe-10/backend/snapstash
```

### 3. Start the Server

Open the `MainVerticle.java` file and run the file by clicking the green play button in your IDE.

### 4. Setting up the Database

Make sure you have MariaDB installed. Create a new database for the SnapStash application:

```sql
CREATE DATABASE snapStashDB;
```

Execute the script `"snapStash.sql"` to set up the database. 

### 5. Access SnapStash

Open a browser of your choice and enter the following URL:

```
http://localhost:8888
```

### 6. Log In

Enter admin credentials and let the fun begin! 😊

---

## Fully documented DDL/SQL script

[SQL - Script](https://git.thm.de/informatik-projekt/sose-2024-suess-rupp/gruppe-10/-/blob/9e7343c638b58ca5809680aac0873449c46749b9/Non-Functional%20Requirements/snapStash.sql)




-------

## ERM Model

### 1. See the ERM Diagramm
![PNG of ERM Diagramm](Non-Functional%20Requirements/ERM.png)

### 2. Description of ERM Diagramm


#### The diagramm shows an Entity-Relationship Model for a photo-album database featuring Users, Albums, Photos, and Keywords. Users own Albums, Albums contain Photos, Albums and Photos can have any amount of Keywords assigned to them.


--- 

## RESTful-API Description

| **Endpoint**                                             | **Description**                                                                                  | **Possible Status Codes**                                                                                     | **Returned Attributes on Success**                         |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| `post("/login")`                                         | Checks user credentials for validity. Returns success on valid credentials or errors for issues. | 200 (success: login successful), 400 (invalid request: missing credentials), 404 (not found: user not found), 500 (server error) | `{"user_id": <user_id>}`                                   |
| `get("/logout")`                                         | Logs out the current user and redirects to the login page.                                       | 303 (redirect: logout successful)                                                                                               | None                                                        |
| **/home**                                                |                                                                                                  |                                                                                                                |                                                             |
| `router.route("/home/*").handler(loginHandler::isAuth)`  | Ensures a user is authenticated before accessing any home functions.                             |                                                                                                                | None                                                        |
| **/users**                                               |                                                                                                  |                                                                                                                |                                                             |
| `get("/users")`                                          | Retrieves all users from the database.                                                           | 200 (success: users retrieved), 404 (not found: no users found), 500 (server error)                                          | `[{ "user_id": <user_id>, "username": "<username>" }, ...]` |
| `post("/users")`                                         | Creates a new user in the database.                                                              | 201 (created: user successfully created), 303 (redirect: creation complete), 400 (invalid request: bad input), 409 (conflict: user already exists), 500 (server error) | `{"username": "<username>", "user_id": <user_id>}`          |
| `patch("/users/:user_id")`                               | Updates user credentials for the specified user ID.                                              | 201 (created: credentials updated), 303 (redirect: update complete), 400 (invalid request: bad input), 500 (server error) | `{"success": "User successfully updated"}`                  |
| `delete("/users/:user_id")`                              | Deletes the user with the specified user ID.                                                     | 201 (created: user successfully deleted), 500 (server error)                                                                 | `{"success": "User successfully deleted"}`                  |
| `get("/users/search/:searched_string")`                  | Searches for users matching the specified string.                                                | 200 (success: users found), 404 (not found: no matching users), 500 (server error)                                          | `[{ "user_id": <user_id>, "username": "<username>" }, ...]` |
| `get("/users/curUser")`                                  | Retrieves data for the currently logged-in user.                                                 | 201 (created: user data retrieved)                                                                                                 | `{"username": "<username>", "user_id": <user_id>, "isAdmin": <true/false>}` |
| **/albums**                                              |                                                                                                  |                                                                                                                |                                                             |
| `get("/home/albums")`                                    | Retrieves all albums associated with the logged-in user.                                         | 200 (success: albums retrieved), 404 (not found: no albums found), 500 (server error)                                          | `[{ "album_id": <album_id>, "title": "<album_title>" }, ...]` |
| `get("/home/albums/search/:searched_string")`            | Searches for albums matching the specified string.                                               | 200 (success: albums found), 404 (not found: no matching albums), 500 (server error)                                          | `[{ "album_id": <album_id>, "title": "<album_title>" }, ...]` |
| `post("/home/albums")`                                   | Creates a new album for the logged-in user.                                                      | 201 (created: album successfully created), 400 (invalid request: bad input), 500 (server error)                               | `{"album_id": <album_id>, "title": "<album_title>"}`        |
| `put("/home/albums/:album_id")`                          | Updates the title of the specified album.                                                        | 200 (success: album updated), 400 (invalid request: bad input), 404 (not found: album not found), 500 (server error)          | `{"message": "Title updated successfully"}`                 |
| `delete("/home/albums/:album_id")`                       | Deletes the specified album.                                                                     | 204 (no content: album deleted), 400 (invalid request: bad input), 404 (not found: album not found), 500 (server error)       | None                                                        |
| `post("/home/albums/:album_id/keywords/:keyword")`       | Assigns a keyword to the specified album.                                                        | 201 (created: keyword assigned), 400 (invalid request: bad input), 404 (not found: album or keyword not found), 500 (server error)  | `{"message": "Keyword successfully assigned"}`              |
| `delete("/home/albums/:album_id/keywords/:keyword")`     | Unassigns a keyword from the specified album.                                                    | 201 (created: keyword unassigned), 404 (not found: album or keyword not found), 500 (server error)                            | `{"message": "Keyword successfully unsigned"}`              |
| `get("/home/albums/:album_id/keywords")`                 | Retrieves all keywords associated with the specified album.                                      | 200 (success: keywords retrieved), 404 (not found: album or keywords not found)                                               | `{"keywords": ["<keyword1>", "<keyword2>", ...]}`           |
| **/photos**                                              |                                                                                                  |                                                                                                                |                                                             |
| `get("/home/albums/:album_id/photos")`                   | Retrieves all photos within the specified album.                                                 | 200 (success: photos retrieved), 404 (not found: album or photos not found), 500 (server error)                               | `[{ "photo_id": <photo_id>, "album_id": <album_id>, "title": "<title>", "url": "<url>", "creation_date": "<date>" }, ...]` |
| `get("/home/albums/:album_id/photos/:photo_id")`         | Retrieves the specified photo from the album.                                                    | 200 (success: photo retrieved), 302 (found: photo moved), 500 (server error)                                                  | `{"photo_id": <photo_id>, "album_id": <album_id>, "title": "<title>", "url": "<url>", "creation_date": "<date>"}` |
| `get("/home/albums/:album_id/photos/search/:searched_string")` | Searches for photos in the album matching the specified string.                             | 200 (success: photos found), 404 (not found: no matching photos), 500 (server error)                                          | `[{ "photo_id": <photo_id>, "album_id": <album_id>, "title": "<title>", "url": "<url>", "creation_date": "<date>" }, ...]` |
| `post("/home/albums/:album_id/photos")`                  | Adds a new photo to the specified album.                                                         | 201 (created: photo added), 204 (no content: photo successfully processed), 400 (invalid request: bad input), 500 (server error)   | `{"photo_id": <photo_id>, "album_id": <album_id>, "title": "<title>", "url": "<url>"}` |
| `patch("/home/albums/:album_id/photos/:photo_id")`       | Updates the title or creation date of the specified photo.                                       | 200 (success: photo updated), 400 (invalid request: bad input), 404 (not found: photo not found), 500 (server error)          | `{"message": "Photo updated successfully"}`                 |
| `delete("/home/albums/:album_id/photos/:photo_id")`      | Deletes the specified photo from the album.                                                      | 204 (no content: photo deleted), 400 (invalid request: bad input), 404 (not found: photo not found), 500 (server error)       | None                                                        |
| `post("/home/albums/:album_id/photos/:photo_id/keywords/:keyword")` | Assigns a keyword to the specified photo.                                                  | 201 (created: keyword assigned), 404 (not found: photo or keyword not found), 500 (server error)                              | `{"message": "Keyword successfully assigned"}`              |
| `delete("/home/albums/:album_id/photos/:photo_id/keywords/:keyword")` | Unassigns a keyword from the specified photo.                                              | 201 (created: keyword unassigned), 404 (not found: photo or keyword not found), 500 (server error)                            | `{"message": "Keyword successfully unsigned"}`              |
| `get("/home/albums/:album_id/photos/:photo_id/keywords/")` | Retrieves all keywords associated with the specified photo.                                 | 200 (success: keywords retrieved), 404 (not found: photo or keywords not found)                                               | `{"keywords": ["<keyword1>", "<keyword2>", ...]}`           |
| **/all photos**                                          |                                                                                                  |                                                                                                                |                                                             |
| `get("/home/albums/allPhotos/")`                         | Retrieves all photos from all albums of the logged-in user.                                      | 200 (success: photos retrieved), 404 (not found: no photos found), 500 (server error)                                         | `[{ "photo_id": <photo_id>, "album_id": <album_id>, "title": "<title>", "url": "<url>", "creation_date": "<date>" }, ...]` |


---
## Requirements met / not met

| Anforderung                                      | Erfüllt  | Kommentar |
|--------------------------------------------------|:--------:|:---------:|
| Login-Seite mit Nutzernamen und Passwort         |  ✅      |
| Validierung der Eingaben                         | ✅     |
| Weiterleitung nach erfolgreicher Anmeldung       | ✅      |
| Fehlermeldung und Bleiben auf Login-Seite bei Fehlern | ✅       |
| Automatische Weiterleitung bei Abmeldung         | ✅      |
| Admin muss bei erster Nutzung in Datenbank existieren | ✅       |
| Admin kann Nutzerkonten verwalten                 | ✅       |
| Keine weiteren Admins erstellbar                 | ✅      |
| Nutzerkonto enthält Nutzernamen, Passwort, Rolle | ✅      |
| Nutzername muss eindeutig sein                    | ✅      |
| Passwörter müssen gehasht gespeichert werden     | ✅       |
| Verwalten von Fotos                               | ✅       |
| Fotos müssen Titel und Datum der Aufnahme enthalten | ✅      |
| Suchfunktion für Fotos                            | ✅       |
| Titel, Datum und Schlagworte bei Fotos änderbar   | ✅       |
| Jeder Nutzer sieht nur seine Fotos                | ✅      |
| Verwalten von Fotoalben                           | ✅       |
| Fotoalbum muss Titel haben                        | ✅       |
| Suchfunktion für Fotoalben                        | ✅      |
| Titel und Schlagworte bei Alben änderbar          | ✅       |
| Fotoalbum enthält beliebig viele Fotos,wobei jedes Foto in beliebig vielen Alben vorkommen kann                 | ✅/❌       | Falls unklar gerne fragen |
| Jeder Nutzer sieht nur seine Alben                | ✅       |
| Suchfunktion für Alben in Suchergebnissen         | ✅      |

---
## Bonus Tasks 🏆

| Optionale Bonusaufgaben                                   | Erfüllt  |
|-----------------------------------------------------------|:--------:|
| Auslesen und Anzeigen der Geo-Code Informationen der Bilder | ✅       |
| Kartenanzeige für Fotos mit Geo-Code Informationen        | ✅       |
| Kommentarfunktion für Fotos und Berücksichtigung in der Suche | ❌       |
| Statistiken zu angesehenen Alben und Fotos bereitstellen  | ❌       |

---
## License and Copyright

This THM Computer Science project is copyrighted by Robin Fey and Ricardo Erdmann, 2024. All rights reserved.
