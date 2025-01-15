package de.thm.mni.informatikProjekt.snapstash.handler;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

/**
 * The KeywordHandler class manages operations related to keywords like:
 * creating, assigning, and removing keywords from albums and photos.
 */
public class KeywordHandler {
  JDBCPool sqlClient;

  /**
   * Constructor for the KeywordHandler with the needed SQL client
   * @param sqlClient
   */
  public KeywordHandler(JDBCPool sqlClient) {
    this.sqlClient = sqlClient;
  }

  /**
   * This Method creates a new keyword if its not already in the database.
   * @param routingContext
   */
  public void createKeyword(RoutingContext routingContext) {
    //Getting the keyword from the path parameter
    String keyword = routingContext.pathParam("keyword");

    //Query to check if the keyword already exists in the database
    sqlClient.preparedQuery("SELECT * FROM keywords WHERE keyword = ?;").execute(Tuple.of(keyword), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> keywordsRow = ar.result();
        if (keywordsRow.size() == 0) {
          // Keyword does not exist, so it gets inserted into the database
          sqlClient.preparedQuery("INSERT INTO keywords (keyword) VALUES (?);").execute(Tuple.of(keyword), ar2 -> {
            if (ar2.failed()) {
              //Query to insert keyword failed
              routingContext.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "Query Failed!").encodePrettily());
            }
          });
        }
        routingContext.next();
      } else {
        //Query failed
        routingContext.response()
          .setStatusCode(500)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("message", "Query Failed!").encodePrettily());
      }
    });
  }

  /**
   * This Method assigns a keyword to an album for the current user.
   * @param routingContext
   */
  public void assignKeywordToAlbum(RoutingContext routingContext) {
    // Get the album_id and keyword from the path parameters
    String albumId = routingContext.pathParam("album_id");
    String keyword = routingContext.pathParam("keyword");
    //Getting the user_id from the current session
    int userIdSess = routingContext.user().get("user_id");

    //Query to check if the user has access to the album
    sqlClient.preparedQuery("SELECT * FROM albums WHERE album_id = ? " +
        "AND album_id IN (SELECT album_id FROM albums_users WHERE user_id = ?); ")
      .execute(Tuple.of(albumId, userIdSess), albumAr -> {

        RowSet<Row> albumRows = albumAr.result();
        if (albumRows.size() == 0) {
          //Album not found or no access
          routingContext.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Album not found").encodePrettily());
        } else {
          //Query to check if the keyword exists
          sqlClient.preparedQuery("SELECT * FROM keywords WHERE keyword = ?")
            .execute(Tuple.of(keyword), keywordAr -> {
              RowSet<Row> keywordRows = keywordAr.result();
              if (keywordRows.size() == 0) {
                //Keyword not found
                routingContext.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("error", "keyword not found").encodePrettily());
              }
            });
          //Query to assign the keyword to the album
          sqlClient.preparedQuery("INSERT INTO albums_keywords (album_id, keyword) VALUES (?,?);")
            .execute(Tuple.of(albumId, keyword), ar -> {

              if (ar.failed()) {
                //Assinging keyword failed
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database error").encodePrettily());
              }

              if (ar.succeeded()) {
                //Keyword successfully assigned
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

  /**
   * This Method assigns a keyword to a photo for the current user.
   * @param routingContext
   */
  public void assignKeywordToPhoto(RoutingContext routingContext) {
    //Get the album_id, photo_id, and keyword from the path parameters
    String albumId = routingContext.pathParam("album_id");
    String photoId = routingContext.pathParam("photo_id");
    String keyword = routingContext.pathParam("keyword");
    //Getting the user_id from the current session
    int userIdSess = routingContext.user().get("user_id");

    //Query to check if the user has access to the photo
    sqlClient.preparedQuery("SELECT photos.photo_id FROM photos " +
        "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
        "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
        "WHERE photos_albums.album_id = ? AND photos.photo_id = ? AND albums_users.user_id = ?;")
      .execute(Tuple.of(albumId, photoId, userIdSess), photoAr -> {

        RowSet<Row> photosRow = photoAr.result();
        if (photosRow.size() == 0) {
          // Photo not found or no access
          routingContext.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Photo not found").encodePrettily());
        } else {

          //Query to check if the keyword exists
          sqlClient.preparedQuery("SELECT * FROM keywords WHERE keyword = ?")
            .execute(Tuple.of(keyword), keywordAr -> {
              RowSet<Row> keywordRows = keywordAr.result();
              if (keywordRows.size() == 0) {
                //Keyword not found
                routingContext.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("error", "keyword not found").encodePrettily());
              }else {
                //Query to assign the keyword to the photo
                sqlClient.preparedQuery("INSERT INTO photos_keywords (photo_id, keyword) VALUES (?, ?);")
                  .execute(Tuple.of(photoId, keyword), ar -> {

                    if (ar.failed()) {
                      //Query to assign keyword failed
                      routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(500)
                        .end(new JsonObject().put("error", "Database error").encodePrettily());
                    }else {
                      //Keyword was successfully linked
                      routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(201)
                        .end(new JsonObject().put("message", "Keyword successfully assigned").encodePrettily());
                    }

                  });
              }
            });
        }
      });
  }

  /**
   * This Method Removes a keyword from a photo for the current user.
   * @param routingContext
   */
  public void deleteKeywordFromPhoto(RoutingContext routingContext) {
    //Get the album_id, photo_id, and keyword from the path parameters
    String albumId = routingContext.pathParam("album_id");
    String photoId = routingContext.pathParam("photo_id");
    String keyword = routingContext.pathParam("keyword");
    //Getting the user_id from the current session
    int userIdSess = routingContext.user().get("user_id");

    //Query to check if the user has access to the photo
    sqlClient.preparedQuery("SELECT photos.photo_id FROM photos " +
        "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
        "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
        "WHERE photos_albums.album_id = ? AND photos.photo_id = ? AND albums_users.user_id = ?;")
      .execute(Tuple.of(albumId, photoId, userIdSess), photoAr -> {

        RowSet<Row> photosRow = photoAr.result();
        if (photosRow.size() == 0) {
          //Photo not found or no access
          routingContext.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Photo not found").encodePrettily());
        } else {

          //Query to check if the keyword exists
          sqlClient.preparedQuery("SELECT * FROM keywords WHERE keyword = ?")
            .execute(Tuple.of(keyword), keywordAr -> {
              RowSet<Row> keywordRows = keywordAr.result();
              if (keywordRows.size() == 0) {
                //Keyword not found
                routingContext.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("error", "keyword not found").encodePrettily());
              }else {
                //Query to delete the keyword from the photo
                sqlClient.preparedQuery("DELETE FROM photos_keywords WHERE photo_id = ? AND keyword = ?;")
                  .execute(Tuple.of(photoId, keyword), ar -> {

                    if (ar.failed()) {
                      //Query to delete keyword failed
                      routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(500)
                        .end(new JsonObject().put("error", "Database error").encodePrettily());
                    }else {
                      //Keyword successfully deleted
                      routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(201)
                        .end(new JsonObject().put("message", "Keyword successfully unsigned.").encodePrettily());
                    }

                  });
              }
            });
        }
      });

  }


  /**
   * This Method removes a keyword from an album for the current user.
   * @param routingContext
   */
  public void deleteKeywordFromAlbum(RoutingContext routingContext) {
    //Get the album_id and keyword from the path parameters
    String albumId = routingContext.pathParam("album_id");
    String keyword = routingContext.pathParam("keyword");
    //Getting the user_id from the current session
    int userIdSess = routingContext.user().get("user_id");

    //Query to check if the user has access to the album
    sqlClient.preparedQuery("SELECT * FROM albums WHERE album_id = ? AND album_id " +
        "IN (SELECT album_id FROM albums_users WHERE user_id = ?); ")
      .execute(Tuple.of(albumId, userIdSess), albumAr -> {

        RowSet<Row> albumRows = albumAr.result();
        if (albumRows.size() == 0) {
          //Album not found or no access
          routingContext.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Album not found").encodePrettily());
        } else {

          //Query to check if the keyword already exists
          sqlClient.preparedQuery("SELECT * FROM keywords WHERE keyword = ?")
            .execute(Tuple.of(keyword), keywordAr -> {
              RowSet<Row> keywordRows = keywordAr.result();
              if (keywordRows.size() == 0) {
                //Keyword not found
                routingContext.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("error", "keyword not found").encodePrettily());
              }else {

                //Query to delete the keyword from the album
                sqlClient.preparedQuery("DELETE FROM albums_keywords WHERE album_id = ? AND keyword = ?")
                  .execute(Tuple.of(albumId, keyword), ar -> {

                    if (ar.failed()) {
                      //Query to delete keyword failed
                      routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(500)
                        .end(new JsonObject().put("error", "Database error").encodePrettily());
                    }else {
                      //Keyword successfully deleted
                      routingContext.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(201)
                        .end(new JsonObject().put("message", "Keyword successfully unsigned").encodePrettily());
                    }
                  });
              }
            });
        }
      });
  }

  /**
   * This Method gets all keywords linked to a certain album for the current user.
   * @param routingContext
   */
  public void getKeywordsFromAlbum(RoutingContext routingContext) {
    //Get the album_id from the path parameters
    String albumId = routingContext.pathParam("album_id");
    //Getting the user_id from the current session
    int userIdSess = routingContext.user().get("user_id");

    //Query to check if the user has access to the album
    sqlClient.preparedQuery("SELECT * FROM albums WHERE album_id = ? AND album_id " +
        "IN (SELECT album_id FROM albums_users WHERE user_id = ?);")
      .execute(Tuple.of(albumId, userIdSess), albumAr -> {

        RowSet<Row> albumRows = albumAr.result();
        if (albumRows.size() == 0) {
          //Album not found or no access
          routingContext.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Album not found").encodePrettily());
        } else {

          //Query to get all keywords linked with the album
          sqlClient.preparedQuery("SELECT keywords.keyword FROM keywords " +
              "JOIN albums_keywords ON keywords.keyword = albums_keywords.keyword " +
              "WHERE albums_keywords.album_id = ?;")
            .execute(Tuple.of(albumId), keywordAr -> {
              RowSet<Row> keywordRows = keywordAr.result();
              if (keywordRows.size() > 0) {
                //Convert keywords to a JSON array
                JsonArray keywordsArray = new JsonArray();
                for (Row row : keywordRows) {
                  keywordsArray.add(row.getString("keyword"));
                }
                //Send the JSON response with the keyword-array
                routingContext.response()
                  .setStatusCode(200)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("keywords", keywordsArray).encodePrettily());
              } else {
                //No keywords found
                routingContext.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("error", "keyword not found").encodePrettily());
              }
            });
        }
      });
  }

  /**
   * This Method gets  all keywords linked to a certain photo for the current user.
   * @param routingContext
   */
  public void getKeywordsFromPhoto(RoutingContext routingContext) {
    //Get the album_id and keyword from the path parameters
    String albumId = routingContext.pathParam("album_id");
    String photoId = routingContext.pathParam("photo_id");
    //Getting the user_id from the current session
    int userIdSess = routingContext.user().get("user_id");

    //Query to check if the user has access to the photo
    sqlClient.preparedQuery("SELECT photos.photo_id FROM photos " +
        "JOIN photos_albums ON photos.photo_id = photos_albums.photo_id " +
        "JOIN albums_users ON photos_albums.album_id = albums_users.album_id " +
        "WHERE photos_albums.album_id = ? AND " +
        "photos.photo_id = ? AND albums_users.user_id = ?;")
      .execute(Tuple.of(albumId, photoId, userIdSess), photoAr -> {

        RowSet<Row> photoRows = photoAr.result();
        if (photoRows.size() == 0) {
          //Photo not found or no access
          routingContext.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Album not found").encodePrettily());
        } else {

          //Query to get all keywords linked to the photo
          sqlClient.preparedQuery("SELECT keywords.keyword FROM keywords " +
              "JOIN photos_keywords ON keywords.keyword = photos_keywords.keyword " +
              "WHERE photos_keywords.photo_id = ?;")
            .execute(Tuple.of(photoId), keywordAr -> {
              RowSet<Row> keywordRows = keywordAr.result();
              if (keywordRows.size() > 0) {
                //Convert keywords to a JSON array
                JsonArray keywordsArray = new JsonArray();
                for (Row row : keywordRows) {
                  keywordsArray.add(row.getString("keyword"));
                }
                //Send the JSON response with the found keywords
                routingContext.response()
                  .setStatusCode(200)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("keywords", keywordsArray).encodePrettily());
              } else {
                //No keywords found
                routingContext.response()
                  .setStatusCode(404)
                  .putHeader("content-type", "application/json")
                  .end(new JsonObject().put("error", "keyword not found").encodePrettily());
              }
            });
        }
      });
  }

  //This Method was never used
  /*public void getAlbumByKeywords(RoutingContext routingContext) {
    int user_id = routingContext.user().get("user_id");
    String album_title = routingContext.pathParam("keyword");

    sqlClient.preparedQuery("SELECT DISTINCT albums.title AS album_title, albums.album_id " +
        "FROM albums " +
        "JOIN albums_users ON albums.album_id = albums_users.album_id " +
        "JOIN albums_keywords ON albums.album_id = albums_keywords.album_id " +
        "JOIN keywords ON albums_keywords.keyword = keywords.keyword " +
        "JOIN users ON albums_users.user_id = users.user_id " +
        "WHERE users.user_id = ? AND keywords.keyword LIKE CONCAT('%', ?, '%');")
      .execute(Tuple.of(user_id, album_title), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> albumsRes = ar.result();
          if (albumsRes.size() > 0) {
            JsonArray albumsArray = new JsonArray();
            for (Row row : albumsRes) {
              JsonObject albumRow = new JsonObject()
                .put("album_id", row.getInteger("album_id"))
                .put("title", row.getString("album_title"));
              albumsArray.add(albumRow);
            }

            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(Json.encodePrettily(albumsArray));
          } else {
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(404)
              .end(new JsonObject().put("error", "No albums found").encodePrettily());
          }
        } else {
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(500)
            .end(new JsonObject().put("error", "Database query failed").encodePrettily());
        }
      });
  }*/

}
