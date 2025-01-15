package de.thm.mni.informatikProjekt.snapstash.handler;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PhotoHandler handles all operations that have to do with the photos like:
 * searching for photos, getting all phots for a user, creating a photo, updating a photo
 * and deleting a photo
 */
public class PhotoHandler {
  JDBCPool sqlClient;

  /**
   * Constructor for the PhotoHandler with the needed SQL client
   * @param sqlClient
   */
  public PhotoHandler(JDBCPool sqlClient) {
    this.sqlClient = sqlClient;
  }


  /**
   * This Method is used for searching for photos that contain the searched after keyword or title
   * @param routingContext
   */
  public void getPhotoByTitleOrKeyword(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");
    //Getting the search word from the path parameter
    String searched_string = routingContext.pathParam("searched_string").trim();

    //Query for finding the photos with the given keyword or title from the database
    //for this query the help of chatGPT (https://chatgpt.com) was used to an extent
    sqlClient.preparedQuery("SELECT DISTINCT photos.*, photos_albums.album_id " +
        "FROM photos " +
        "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
        "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
        "JOIN users ON albums_users.user_id = users.user_id " +
        "LEFT JOIN photos_keywords ON photos.photo_id = photos_keywords.photo_id " +
        "LEFT JOIN keywords ON photos_keywords.keyword = keywords.keyword " +
        "WHERE users.user_id = ? " +
        "AND (photos.title LIKE CONCAT('%', ?, '%') OR keywords.keyword LIKE CONCAT('%', ?, '%')); ")
      .execute(Tuple.of(user_id, searched_string, searched_string ), arPh -> {
        if (arPh.succeeded()) {
          //Query was executes successfully
          RowSet<Row> photoRes = arPh.result();
          if (photoRes.size() > 0) {
            //Array for the found photos
            JsonArray albumsArray = new JsonArray();
            for (Row row : photoRes) {
              //Putting every found photo into a JSON Object and then into the Array
              JsonObject photoRow = new JsonObject()
                .put("photo_id", row.getInteger("photo_id"))
                .put("album_id", row.getInteger("album_id"))
                .put("title", row.getString("title"))
                .put("url", "/photos/" + row.getString("url"))
                .put("creation_date", row.getLocalDate("creation_date").toString());
              albumsArray.add(photoRow);
            }

            //Response with the found photos
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(Json.encodePrettily(albumsArray));
          } else {
            //Case when no photos were found
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(404)
              .end(new JsonObject().put("error", "No photos found").encodePrettily());
          }
        } else {
          //Case when the query failed
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(500)
            .end(new JsonObject().put("error", "Database query failed").encodePrettily());
        }
      });
  }

  /**
   * This Method gets a photo by its ID for the current user in a certain album.
   * @param routingContext
   */
  public void getPhotoById(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");
    //Getting the album_id from the path parameter
    String album_id = routingContext.pathParam("album_id");
    //Getting the photo_id from the path parameter
    String photo_id = routingContext.pathParam("photo_id");

    //Query for getting a photo by its ID
    sqlClient.preparedQuery("SELECT DISTINCT photos.photo_id, photos.url, photos.title, photos.creation_date, photos_albums.album_id " +
        "FROM photos " +
        "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
        "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
        "WHERE albums_users.album_id = ? " +
        "AND photos.photo_id = ? " +
        "AND albums_users.user_id = ?; ")
      .execute(Tuple.of(album_id, photo_id, user_id), ar -> {
        if (ar.succeeded()) {
          // Query executed successfully
          RowSet<Row> photoRes = ar.result();
          if (photoRes.size() > 0) {
            //Convert the found photo into a JSON object
            JsonObject photoRow = new JsonObject();
            for (Row row : photoRes) {
              photoRow
                .put("photo_id", row.getInteger("photo_id"))
                .put("album_id", row.getInteger("album_id"))
                .put("title", row.getString("title"))
                .put("url",  "/photos/" + row.getString("url"))
                .put("creation_date", row.getLocalDate("creation_date").toString());
              }
            // Send the JSON response with the found photo
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(Json.encodePrettily(photoRow));
          } else {
            //Case when no photo was found, redirection to home
            routingContext.response()
              .setStatusCode(302)
              .putHeader("Location", "/home")
              .end();
          }
        } else {
          //Case when query failed
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(500)
            .end(new JsonObject().put("error", "Database query failed").encodePrettily());
        }
      });
  }


  /**
   * This Method gets all photos from a certain album
   *
   * @param routingContext
   */
  public void getAllPhotosFromAlbum(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");
    //Getting the album_id from the path parameter
    String albumId = routingContext.pathParam("album_id");

    //Query to get all photos in a certain album from the database
    sqlClient.preparedQuery("SELECT photos.*, photos_albums.album_id FROM photos " +
      "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
      "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
      "WHERE photos_albums.album_id = ? AND albums_users.user_id = ?;").execute(Tuple.of(albumId, user_id), ar -> {
      if (ar.succeeded()) {
        // Query executed successfully
        RowSet<Row> photosRes = ar.result();
        //Array for the found photos
        JsonArray result = new JsonArray();
        for (Row row : photosRes) {
          //Putting every found photo into a JSON Object and then into the Array
          JsonObject photosRow = new JsonObject()
            .put("photo_id", row.getInteger("photo_id"))
            .put("album_id", row.getInteger("album_id"))
            .put("url", "/photos/" + row.getString("url"))
            .put("title", row.getString("title"))
            .put("creation_date", row.getLocalDate("creation_date").toString());
          result.add(photosRow);
        }
        if (!result.isEmpty()) {
          //Return the found photos
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(200)
            .end(Json.encodePrettily(result));
        } else {
          //No photos found in album
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(404)
            .end();
        }
      } else {
        //Query failed
        routingContext.response()
          .putHeader("content-type", "application/json")
          .setStatusCode(500)
          .end(new JsonObject().put("error", "Database query failed").encodePrettily());
      }
    });
  }

  /**
   * This Method checks if the current user has access to a certain album and proceeds with the routing.
   * @param routingContext
   */
  public void checkAlbumsUsers(RoutingContext routingContext){
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");
    //Getting the album_id from the path parameter
    String albumId = routingContext.pathParam("album_id");

    //Query to look up if the user has access to the album
    sqlClient.preparedQuery("SELECT * FROM albums_users WHERE user_id = ? AND album_id = ?").execute(Tuple.of(user_id, albumId), ar -> {
      if (ar.succeeded()){
        // Query executed successfully
        RowSet<Row> albumRes = ar.result();
        if (albumRes.size() > 0) {
          //User has access to the album
          routingContext.next();
        }else {
          //User does not have access to the album
          routingContext.response()
            .setStatusCode(302)
            .putHeader("Location", "/home")
            .end();
        }
      }else {
        //Query failed
        routingContext.response()
          .putHeader("content-type", "application/json")
          .setStatusCode(500)
          .end(new JsonObject().put("error", "Database query failed").encodePrettily());
      }
    });
  }

  /**
   * This Method gets all photos of a user across all albums
   * @param routingContext
   */
  public void getAllPhotosFromUser(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");
    System.out.println("User ID: " + user_id); // Debugging

    //Query to get all photos across all albums
    sqlClient.preparedQuery("SELECT photos.*, photos_albums.album_id FROM photos JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
      "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
      "JOIN users ON albums_users.user_id = users.user_id " +
      "WHERE users.user_id = ?;").execute(Tuple.of(user_id), ar -> {
      if (ar.succeeded()) {
        // Query executed successfully
        RowSet<Row> photosRes = ar.result();
        //Array for the found photos
        JsonArray result = new JsonArray();
        for (Row row : photosRes) {
          //Putting every found photo into a JSON Object and then into the Array
          JsonObject photosRow = new JsonObject()
            .put("photo_id", row.getInteger("photo_id"))
            .put("album_id", row.getInteger("album_id"))
            .put("url", "/photos/" + row.getString("url"))
            .put("title", row.getString("title"))
            .put("creation_date", row.getLocalDate("creation_date").toString());
          result.add(photosRow);
        }
        System.out.println("Result size: " + result.size()); // Debugging
        if (!result.isEmpty()) {
          //Return the photos
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(200)
            .end(Json.encodePrettily(result));
        } else {
          //User has no photos
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(404)
            .end();
        }
      } else {
        //Query failed
        routingContext.response()
          .putHeader("content-type", "application/json")
          .setStatusCode(500)
          .end(new JsonObject().put("error", "Database query failed").encodePrettily());
      }
    });
  }



  /**
   * This Method creates a new photo and adds it to the current album
   *
   * @param routingContext
   */
  //For this method we received assistance from group 1
  public void createPhoto(RoutingContext routingContext) {
    // Initialize Vert.x instance
    Vertx vertx = Vertx.vertx();
    //Getting the user_id from the current session
    int userId = routingContext.user().get("user_id");
    //Getting the album_id from the path parameter
    String albumId = routingContext.pathParam("album_id");

    // Check if exactly one photo is uploaded
    if (routingContext.fileUploads().size() == 1) {
      FileUpload file = routingContext.fileUploads().get(0);
      String fileName = file.fileName().trim();
      String fileNameUpload = file.uploadedFileName();
      String fileNameEx = fileName.substring(fileName.lastIndexOf(".") + 1);
      String newFileName = fileNameUpload.replace("photos\\", "") + "." + fileNameEx;
      System.out.println(fileName + "  " + newFileName + "   " + fileNameEx + "  ");

      //Check if file is a valid Image
      if (!fileNameEx.equals("png") && !fileNameEx.equals("jpg") && !fileNameEx.equals("jpeg")) {
        //Delete Image and send error if format is invalid
        vertx.fileSystem().delete(fileNameUpload);

        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Please upload a valid Photo (.png, .jpg or .jpeg)").encodePrettily());
        return;
      }

      //Move and rename the uploaded file to the photos directory
      vertx.fileSystem().move(fileNameUpload, "photos/" + newFileName, renameResult -> {
        if (renameResult.succeeded()) {
          //Getting the photo title from the attributes
          String photoTitle = routingContext.request().formAttributes().get("title").trim();

          // Check if the title is valid
          if (photoTitle.isEmpty() || fileNameUpload.isEmpty()) {
            routingContext.response()
              .setStatusCode(400)
              .putHeader("content-type", "application/json")
              .end(new JsonObject().put("error", "Please enter valid Data.").encodePrettily());
            return;
          }

          //Query to insert the new photo into the database
          sqlClient.preparedQuery("INSERT INTO photos (title, url) VALUES (?, ?);")
            .execute(Tuple.of(photoTitle, newFileName), ar -> {
              if (ar.succeeded()) {
                //Query to get the ID of the newly created photo
                sqlClient.query("SELECT LAST_INSERT_ID();").execute(ar2 -> {
                  if (ar2.succeeded()) {
                    RowSet<Row> photoRow = ar2.result();
                    if (photoRow.size() > 0) {
                      int photoId = photoRow.iterator().next().getInteger("LAST_INSERT_ID()");
                      //Query to link album and photo
                      sqlClient.preparedQuery("INSERT INTO photos_albums (photo_id, album_id) SELECT ?, ? FROM albums_users WHERE user_id = ? AND album_id = ?;")
                        .execute(Tuple.of(photoId, albumId, userId, albumId), ar3 -> {
                          if (ar3.succeeded()) {
                            if (ar3.result().rowCount() > 0) {
                              //Photo connection successful
                              JsonObject result = new JsonObject()
                                .put("photo_id", photoId)
                                .put("album_id", albumId)
                                .put("title", photoTitle)
                                .put("url", "/photos/" + newFileName);
                              routingContext.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(201)
                                .end(Json.encodePrettily(result));
                            } else {
                              // Delete photo if linking was not successful
                              vertx.fileSystem().delete(newFileName);
                              sqlClient.preparedQuery("DELETE FROM photos WHERE photo_id = ?;")
                                .execute(Tuple.of(photoId), ar4 -> {
                                  if (ar4.succeeded()) {
                                    routingContext.response()
                                      .putHeader("content-type", "application/json")
                                      .setStatusCode(500)
                                      .end(new JsonObject().put("error", "Database query: LINK Photo TO Album failed, Photo deleted").encodePrettily());
                                  } else {
                                    routingContext.response()
                                      .putHeader("content-type", "application/json")
                                      .setStatusCode(500)
                                      .end(new JsonObject().put("error", "Database query: LINK Photo TO Album failed, Photo deletion failed").encodePrettily());
                                  }
                                });
                            }
                          } else {
                            // Delete photo if linking was not successful
                            vertx.fileSystem().delete(newFileName);
                            sqlClient.preparedQuery("DELETE FROM photos WHERE photo_id = ?;")
                              .execute(Tuple.of(photoId), ar4 -> {
                                if (ar4.succeeded()) {
                                  routingContext.response()
                                    .putHeader("content-type", "application/json")
                                    .setStatusCode(204)
                                    .end(new JsonObject().put("error", "Initial Query canceled").encodePrettily());
                                } else {
                                  routingContext.response()
                                    .putHeader("content-type", "application/json")
                                    .setStatusCode(500)
                                    .end(new JsonObject().put("error", "Database Error").encodePrettily());
                                }
                              });
                          }
                        });
                    } else {
                      // No photo ID returned
                      vertx.fileSystem().delete(newFileName);
                      routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(500)
                        .end(new JsonObject().put("error", "No photo returned").encodePrettily());
                    }
                  } else {
                    //Query for last inserted ID failed
                    vertx.fileSystem().delete(newFileName);
                    routingContext.response()
                      .putHeader("content-type", "application/json")
                      .setStatusCode(500)
                      .end(new JsonObject().put("error", "Database query: SELECT LAST_INSERT_ID failed").encodePrettily());
                  }
                });
              } else {
                // Failed to insert photo
                vertx.fileSystem().delete(newFileName);
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database query: INSERT Photo failed").encodePrettily());
              }
            });
        } else {
          // Failed to move the file
          vertx.fileSystem().delete(newFileName);
          routingContext.response()
            .setStatusCode(500)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Image could not be saved").encodePrettily());
        }
      });


    } else {
      // Handle multiple file uploads
      routingContext.response().setStatusCode(400).end("Only one Photo allowed.");
    }
  }

  /**
   * This Method updates details of already existing photos
   * @param routingContext
   */
  public void updatePhoto(RoutingContext routingContext) {
    //Getting the photo_id from the path parameter
    String photoId = routingContext.pathParam("photo_id");

    // Handle the request body
    routingContext.request().bodyHandler(body -> {
      JsonObject jsonObject;
      try {
        //Convert the content of the request body to a JSON Object
        jsonObject = body.toJsonObject();
      } catch (Exception e) {
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
        return;
      }

      //Read the new title and creation date from the JSON body
      String newTitle = jsonObject.getString("title", "").trim();
      String newCreationDate = jsonObject.getString("creation_date", "").trim();

      //Getting the user_id from the current session
      int userId = routingContext.user().get("user_id");

      //Query to update the photo details in the database
      sqlClient.preparedQuery("UPDATE photos " +
        "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
        "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
        "SET photos.title = ?, photos.creation_date = ?" +
        "WHERE photos.photo_id = ? AND albums_users.user_id = ?;").execute(Tuple.of(newTitle, newCreationDate, photoId, userId), ar -> {
        if (ar.succeeded()) {
          if (ar.result().rowCount() > 0) {
            // Photo updated successfully
            routingContext.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(new JsonObject().put("message", "Photo updated succesfully").encodePrettily());
          } else {
            // Photo not found
            routingContext.response()
              .setStatusCode(404)
              .putHeader("content-type", "application/json")
              .end(new JsonObject().put("error", "Photo not found").encodePrettily());
          }

        } else {
          //Query to update failed
          routingContext.response()
            .setStatusCode(500)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
        }
      });

    });
  }

  /**
   * This Method deletes a photo for the current user from a certain album.
   *
   * @param routingContext
   */
  public void deletePhoto(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");
    //Getting the album_id from the path parameter
    String albumId = routingContext.pathParam("album_id");
    //Getting the photo_id from the path parameter
    String photoId = routingContext.pathParam("photo_id");

    // Check if the album_id and photo_id are valid
    if (!albumId.isEmpty() && !photoId.isEmpty()) {

      //Query to check if the user owns the photo
      sqlClient.preparedQuery("SELECT photos.photo_id " +
        "FROM photos " +
        "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
        "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
        "WHERE photos.photo_id = ? AND albums_users.user_id = ?;").execute(Tuple.of(photoId, user_id), ar -> {
        if (ar.failed()) {
          // Failed to execute the query
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(500)
            .end(new JsonObject().put("error", "SELECT Query Failed").encodePrettily());
        } else {
          RowSet<Row> rows = ar.result();
          if (rows.size() == 0) {
            // User does not own the photo (no permission)
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(404)
              .end(new JsonObject().put("error", "No Photo Found").encodePrettily());
          } else {
            //Query to delete the photo
            sqlClient.preparedQuery("DELETE photos " +
                "FROM photos " +
                "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
                "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
                "WHERE photos.photo_id = ? AND albums_users.user_id = ?;")
              .execute(Tuple.of(photoId, user_id), ar2 -> {
                if (ar2.failed()) {
                  // Failed to delete the photo
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", "DELETE query failed").encodePrettily());
                } else {
                  // Photo successfully deleted
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(204)
                    .end(new JsonObject().put("message", "Photo successfully deleted").encodePrettily());
                }
              });
          }
        }
      });
    } else {
      //Handle invalid album_id or photo_id
      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(400)
        .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
    }

  }

 //This Method was never used
/*  public void assignKeyword(RoutingContext routingContext) {


    String albumId = routingContext.pathParam("album_id");
    String keywordId = routingContext.pathParam("keyword_id");
    int userIdSess = routingContext.user().get("user_id");
    int albumIdInt = -1;
    int keywordIdInt = -1;

    try {
      albumIdInt = Integer.parseInt(albumId);
      keywordIdInt = Integer.parseInt(keywordId);
    } catch (Exception e) {
      e.getMessage();
    }

    final int finalAlbumIdInt = albumIdInt;
    final int finalKeywordIdInt = keywordIdInt;
    sqlClient.preparedQuery("SELECT * FROM albums WHERE album_id = ? AND album_id IN (SELECT album_id FROM albums_users WHERE user_id = ?); ")
      .execute(Tuple.of(albumIdInt, userIdSess), albumAr -> {

        RowSet<Row> albumRows = albumAr.result();
        if (albumRows.size() == 0) {
          routingContext.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Photo not found").encodePrettily());
        } else {

          sqlClient.preparedQuery("SELECT * FROM keywords WHERE keyword = ?")
            .execute(Tuple.of(finalKeywordIdInt), keywordAr -> {
              RowSet<Row> keywordRows = keywordAr.result();
              if (keywordRows.size() == 0) {
                routingContext.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("error", "keyword not found").encodePrettily());
              }
            });


          sqlClient.preparedQuery("INSERT INTO albums_keywords (album_id, keyword) VALUES (?,?);")
            .execute(Tuple.of(finalAlbumIdInt, finalKeywordIdInt), ar -> {

              if (ar.failed()) {
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database error").encodePrettily());
              }

              if (ar.succeeded()) {
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(201)
                  .end(new JsonObject().put("message", "Keyword successfully assigned").encodePrettily());
              } else {
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(400)
                  .end(new JsonObject().put("error", "invalid Input").encodePrettily());
              }

            });
        }
      });
  }

  //This Method was never used
  public void deleteKeyword(RoutingContext routingContext) {

  }*/

}
