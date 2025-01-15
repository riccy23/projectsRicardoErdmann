package de.thm.mni.informatikProjekt.snapstash.handler;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

/**
 * The AlbumHandler handles all operations that have to do with the albums like:
 * searching for albums, getting all albums for a user, creating an album, updating an album
 * and deleting an album
 */
public class AlbumHandler {
  JDBCPool sqlClient;

  /**
   * Constructor for the AlbumHandler with the needed SQL client
   * @param sqlClient
   */
  public AlbumHandler(JDBCPool sqlClient) {
    this.sqlClient = sqlClient;
  }


  /**
   * This Method is used for searching for albums that contain the searched after keyword or title
   * @param routingContext
   */
  public void getAlbumByTitleOrKeyword(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");
    //Getting the search word from the path parameter
    String searchedString = routingContext.pathParam("searched_string");

    //Query for finding the albums with the given keyword or title from the database
    //for this query the help of chatGPT (https://chatgpt.com) was used to an extent
    sqlClient.preparedQuery("SELECT DISTINCT albums.title AS album_title, albums.album_id " +
        "FROM albums " +
        "JOIN albums_users ON albums.album_id = albums_users.album_id " +
        "LEFT JOIN albums_keywords ON albums.album_id = albums_keywords.album_id " +
        "LEFT JOIN keywords ON albums_keywords.keyword = keywords.keyword " +
        "JOIN users ON albums_users.user_id = users.user_id " +
        "WHERE users.user_id = ? " +
        "AND (albums.title LIKE CONCAT('%', ?, '%') " +
        "OR keywords.keyword LIKE CONCAT('%', ?, '%')); ")
      .execute(Tuple.of(user_id, searchedString, searchedString), ar -> {
        if (ar.succeeded()) {
          //Query was executes successfully
          RowSet<Row> albumsRes = ar.result();
          if (albumsRes.size() > 0) {
            //Array for the found Albums
            JsonArray albumsArray = new JsonArray();
            for (Row row : albumsRes) {
              //Putting every found Album into a JSON Object and then into the Array
              JsonObject albumRow = new JsonObject()
                .put("album_id", row.getInteger("album_id"))
                .put("title", row.getString("album_title"));
              albumsArray.add(albumRow);
            }

            //Response with the found Albums
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(Json.encodePrettily(albumsArray));
          } else {
            //Case when no albums were found
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(404)
              .end(new JsonObject().put("error", "No albums found").encodePrettily());
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
   * This Method gets all Albums for a certain user
   * @param routingContext
   */
  public void getAllAlbums(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int user_id = routingContext.user().get("user_id");

    //Query to get all the Albums for a user from the database
    sqlClient.preparedQuery("SELECT albums.album_id, users.user_id, albums.title AS albumTitle " +
      "FROM albums_users JOIN users ON albums_users.user_id = users.user_id " +
      "JOIN albums ON albums_users.album_id = albums.album_id WHERE users.user_id = ?;").execute(Tuple.of(user_id), ar -> {
      if (ar.succeeded()) {
        //Query executed successfully
        RowSet<Row> albumsRes = ar.result();
        JsonArray result = new JsonArray();
        for (Row row : albumsRes) {
          //Putting each album into an Array
          JsonObject albumsRow = new JsonObject()
            .put("album_id", row.getInteger("album_id"))
            .put("title", row.getString("albumTitle"));
          result.add(albumsRow);
        }
        if (!result.isEmpty()) {
          //Returning the users Albums
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(200)
            .end(Json.encodePrettily(result));
        } else {
          //User has no Albums
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(404)
            .end();
        }
      } else {
        //Case if the query failed
        routingContext.response()
          .putHeader("content-type", "application/json")
          .setStatusCode(500)
          .end(new JsonObject().put("error", "Database query failed").encodePrettily());
      }
    });
  }

  /**
   * This Method creates a new Album for the current user
   * @param routingContext
   */
  public void createAlbum(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int userId = routingContext.user().get("user_id");

    //Handle the request body
    routingContext.request().bodyHandler(body -> {
      JsonObject jsonObject;
      try {
        //Converts the content of the body into a JSON Object
        jsonObject = body.toJsonObject();
      } catch (Exception e) {
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
        return;
      }
      //Getting the Album title from the JSON Object
      String albumTitle = jsonObject.getString("title", "").trim();

      //Check if input is valid
      if (albumTitle.isEmpty()) {
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Please enter an albumTitle").encodePrettily());
        return;
      }

      //Create a new Album in the database
      sqlClient.preparedQuery("INSERT INTO albums (title) VALUES (?);")
        .execute(Tuple.of(albumTitle), ar -> {
          if (ar.succeeded()) {
            //Getting the album_id of the new Album
            sqlClient.query("SELECT LAST_INSERT_ID();").execute(ar2 -> {
              if (ar2.succeeded()) {
                RowSet<Row> albumRow = ar2.result();
                if (albumRow.size() > 0) {
                  int albumId = albumRow.iterator().next().getInteger("LAST_INSERT_ID()");
                  // Linking User and Album
                  sqlClient.preparedQuery("INSERT INTO albums_users (user_id, album_id) VALUES (?, ?);")
                    .execute(Tuple.of(userId, albumId), ar3 -> {
                      if (ar3.succeeded()) {
                        //Album connection successful
                        JsonObject result = new JsonObject()
                          .put("album_id", albumId)
                          .put("title", albumTitle);
                        routingContext.response()
                          .putHeader("content-type", "application/json")
                          .setStatusCode(201)
                          .end(Json.encodePrettily(result));
                      } else {
                        //DELETE album, when Linking was not successful
                        sqlClient.preparedQuery("DELETE FROM albums WHERE album_id = ?;")
                          .execute(Tuple.of(albumId), ar4 -> {
                            if (ar4.succeeded()) {
                              //Case when deletion was successful
                              routingContext.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", "Database query: LINK ALBUM TO USER failed, album deleted").encodePrettily());
                            } else {
                              //Case when deletion failed
                              routingContext.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", "Database query: LINK ALBUM TO USER failed, album deletion failed").encodePrettily());
                            }
                          });
                      }
                    });
                } else {
                  //Case when no album_id was returned
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", "No album_id returned").encodePrettily());
                }
              } else {
                //Case when query to get the last inserted ID failed
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database query: SELECT LAST_INSERT_ID failed").encodePrettily());
              }
            });
          } else {
            //Case when query to add new Album failed
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(500)
              .end(new JsonObject().put("error", "Database query: INSERT ALBUM failed").encodePrettily());
          }
        });
    });
  }

  /**
   * This Method updates the title of an already existing Album
   * @param routingContext
   */
  public void updateAlbumTitle(RoutingContext routingContext) {
    //Handle the request body
    routingContext.request().bodyHandler(body -> {
      JsonObject jsonObject;
      try {
        //Converts the content of the body into a JSON Object
        jsonObject = body.toJsonObject();
      } catch (Exception e) {
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
        return;
      }

      //Read the new Album title from the JSON Object
      String newTitle = jsonObject.getString("title", "").trim();

      //Getting the ID of the current User
      int userId = routingContext.user().get("user_id");
      //Getting the album_id from the path parameter
      String albumIdPath = routingContext.pathParam("album_id");
      int albumIdInt = -1;
      try {
        //Parsing the album_id from the path into an Integer
        albumIdInt = Integer.parseInt(albumIdPath);
      } catch (Exception e) {
        e.getMessage();
      }

      //Query for updating the Album title in the database
      sqlClient.preparedQuery("UPDATE albums SET title = ? WHERE album_id = ? AND album_id IN (SELECT album_id FROM albums_users WHERE user_id = ?);").execute(Tuple.of(newTitle, albumIdInt, userId), ar -> {
        if (ar.succeeded()) {
          if (ar.result().rowCount() > 0) {
            //Album title updated successfully
            routingContext.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(new JsonObject().put("message", "Title updated successfully").encodePrettily());
          } else {
            //Album not found
            routingContext.response()
              .setStatusCode(404)
              .putHeader("content-type", "application/json")
              .end(new JsonObject().put("error", "Album not found").encodePrettily());
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
   * This Method deletes an Album for the current User
   *
   * @param routingContext
   */
  public void deleteAlbum(RoutingContext routingContext) {
    //Getting the ID of the current User
    int user_id = routingContext.user().get("user_id");
    //Getting the album_id from the path parameter
    String albumId = routingContext.pathParam("album_id");

    if (!albumId.isEmpty()) {
      int albumIdInt = Integer.parseInt(albumId);
      // Check if user owns album
      sqlClient.preparedQuery("SELECT * FROM albums_users WHERE album_id = ? and user_id = ?;").execute(Tuple.of(albumIdInt, user_id), ar -> {
        if (ar.failed()) {
          //Query failed
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(500)
            .end(new JsonObject().put("error", "SELCET Query Failed").encodePrettily());
        } else {
          RowSet<Row> rows = ar.result();
          if (rows.size() == 0) {
            //Current User has no permission to delete the Album
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(404)
              .end(new JsonObject().put("error", "No Album Found").encodePrettily());
          } else {
            //Delete the Album
            sqlClient.preparedQuery("DELETE FROM albums WHERE album_id = ?;")
              .execute(Tuple.of(albumIdInt), ar2 -> {
                if (ar2.failed()) {
                  //Case when the deletion query failed
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", "DELETE query failed").encodePrettily());
                } else {
                  //Case when the deletion was successful<
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(204)
                    .end(new JsonObject().put("message", "Album successfully deleted").encodePrettily());
                }
              });
          }
        }
      });
    } else {
      //Case when no album_id was found
      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(400)
        .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
    }

  }

}
