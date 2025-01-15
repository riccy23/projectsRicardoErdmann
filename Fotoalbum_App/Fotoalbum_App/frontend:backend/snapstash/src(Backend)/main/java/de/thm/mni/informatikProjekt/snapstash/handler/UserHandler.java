package de.thm.mni.informatikProjekt.snapstash.handler;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * The UserHandler class manages user-related operations like:
 * creating, updating, deleting, and getting user information.
 */
public class UserHandler {
  JDBCPool sqlClient;

  /**
   * Constructor for the UserHandler with the needed SQL client
   * @param sqlClient
   */
  public UserHandler(JDBCPool sqlClient) {
    this.sqlClient = sqlClient;
  }

  /**
   * This Method gets all non-admin users from the database.
   * @param routingContext
   */
  public void getUsers(RoutingContext routingContext) {
    //This variable is not necessary for this method
    int userId = routingContext.user().get("user_id");

    //Query to get all non-admin users
    sqlClient.query("SELECT * FROM users WHERE isAdmin = 0").execute(ar2 -> {
      if (ar2.succeeded()) {
        RowSet<Row> usersRes = ar2.result();
        //Convert users to a JSON array
        JsonArray result = new JsonArray();
        for (Row row : usersRes) {
          //Putting every found user into the array
          JsonObject usersRow = new JsonObject()
            .put("user_id", row.getInteger("user_id"))
            .put("username", row.getString("username"));
          result.add(usersRow);
        }
        if (!result.isEmpty()) {
          //Return the list of users
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(200)
            .end(Json.encodePrettily(result));
        } else {
          //No users found
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
   * This Method creates a new user with the given username and password.
   * @param routingContext
   */
  public void createUser(RoutingContext routingContext) {
    //Getting the user_id from the current session
    int userId = routingContext.user().get("user_id");

    //Handles the request body
    routingContext.request().bodyHandler(body -> {
      JsonObject jsonObject;
      try {
        //Convert the request body to a JSON Object
        jsonObject = body.toJsonObject();
      } catch (Exception e) {
        //Handle invalid JSON input
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
        return;
      }
      //Get username and password from the request
      String userName = jsonObject.getString("username", "").trim();
      String password = jsonObject.getString("password", "").trim();

      //Check if input is valid
      if (userName.isEmpty() || password.isEmpty()) {
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Please enter username and password").encodePrettily());
        return;
      }

      //Hash Password
      String hashPw = BCrypt.hashpw(password, BCrypt.gensalt());

      //Query to check if the user is an admin
      sqlClient.preparedQuery("SELECT * FROM users WHERE isAdmin = 1 AND user_id = ?")
        .execute(Tuple.of(userId), ar -> {
          if (ar.succeeded()) {
            RowSet<Row> adminRow = ar.result();
            if (adminRow.size() == 0) {
              //The current user isnt an admin
              routingContext.response()
                .setStatusCode(303)
                .putHeader("Location", "/home")
                .end();
              return;
            }
            //Query to check if the username already exists
            sqlClient.preparedQuery("SELECT count(*) as userCount FROM users WHERE username = ?").execute(Tuple.of(userName), ar2 -> {
              if (ar2.succeeded()) {
                RowSet<Row> userRow = ar2.result();
                if (userRow.iterator().next().getInteger("userCount") == 0) {
                  System.out.println(userRow.iterator().next().getInteger("userCount"));
                  //Create the new user
                  sqlClient.preparedQuery("INSERT INTO users (username, password) VALUES (?,?);")
                    .execute(Tuple.of(userName, hashPw), ar3 -> {
                      if (ar3.succeeded()) {
                        //User successfully inserted and Query to get the ID of the newly created user
                        sqlClient.query("SELECT LAST_INSERT_ID();").execute(arUser -> {
                          if (arUser.succeeded()) {
                            RowSet<Row> userRowWithID = arUser.result();
                            if (userRowWithID.size() > 0) {
                              int user_id = userRowWithID.iterator().next().getInteger("LAST_INSERT_ID()");
                              //Newly added user converted into a JSON Object
                              JsonObject result = new JsonObject()
                                .put("username", userName)
                                .put("user_id", user_id);
                              routingContext.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(201)
                                .end(Json.encodePrettily(result));
                            } else {
                              //No User with given ID found
                              routingContext.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(500)
                                .end(new JsonObject().put("error", "No user_id returned").encodePrettily());
                            }
                          } else {
                            //Query failed
                            routingContext.response()
                              .putHeader("content-type", "application/json")
                              .setStatusCode(500)
                              .end(new JsonObject().put("error", "Database query: SELECT LAST_INSERT_ID failed").encodePrettily());
                          }
                        });
                      } else {
                        //User creation failed
                        routingContext.response()
                          .putHeader("content-type", "application/json")
                          .setStatusCode(500)
                          .end(new JsonObject().put("error", "Database query: INSERT USER failed").encodePrettily());
                      }
                    });
                } else {
                  //Username already exists
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(409)
                    .end(new JsonObject().put("error", "Username already exists").encodePrettily());
                }
              } else {
                //Username check failed
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database query: Username check failed").encodePrettily());
              }
            });
          } else {
            //Admin check failed
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(500)
              .end(new JsonObject().put("error", "Database query: Admin Check failed").encodePrettily());
          }
        });
    });
  }

  /**
   * This Method updates the username or password of a user.
   * @param routingContext
   */
  public void updateUser(RoutingContext routingContext) {
    //Both these variables are unnecessary in this Method
    int userId = routingContext.user().get("user_id");
    String userIdFromPath = routingContext.pathParam("user_id");

    //Handle the request body
    routingContext.request().bodyHandler(body -> {
      JsonObject jsonObject;
      try {
        //Convert the request body to a JSON object
        jsonObject = body.toJsonObject();
      } catch (Exception e) {
        //Handle invalid JSON input
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
        return;
      }
      // Get username and password from the request
      String userName = jsonObject.getString("username", "").trim();
      String password = jsonObject.getString("password", "").trim();

      //Check which fields need to be updated
      if (userName.isEmpty() && !password.isEmpty()) {
        //Update only the password
        updateUserPW(routingContext, password);
      } else if (!userName.isEmpty() && password.isEmpty()) {
        //Update only the username
        updateUserName(routingContext, userName);
      } else if (!userName.isEmpty()) {
        //Update both username and password
        updateUserWhole(routingContext, userName, password);
      } else {
        //Invalid input
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Please enter username and password").encodePrettily());
      }
    });
  }

  /**
   * This Method updates only the username of a user.
   * @param routingContext
   * @param newUsername the new username that is wished to be set
   */
  private void updateUserName(RoutingContext routingContext, String newUsername) {
    //Getting the user_id from the current session
    int userId = routingContext.user().get("user_id");
    //Getting the user_id from the path parameter
    String userIdFromPath = routingContext.pathParam("user_id");


    //Query to check if the user is an admin
    sqlClient.preparedQuery("SELECT * FROM users WHERE isAdmin = 1 AND user_id = ?")
      .execute(Tuple.of(userId), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> adminRow = ar.result();
          if (adminRow.size() == 0) {
            //Redirect if not an admin
            routingContext.response()
              .setStatusCode(303)
              .putHeader("Location", "/home")
              .end();
            return;
          }
          //Query to update the username
          sqlClient.preparedQuery("UPDATE users SET username = ? WHERE user_id = ?")
            .execute(Tuple.of(newUsername, userIdFromPath), arUser -> {
              if (arUser.succeeded()) {
                //Username successfully updated
                if (arUser.result().rowCount() > 0) {
                  //Username successfully updated
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(201)
                    .end(new JsonObject().put("success", "Username successfully updated").encodePrettily());
                } else {
                  //No user_id returned
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", "No user_id returned").encodePrettily());
                }

              } else {
                //Updating the user was not successful
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database query: INSERT USER failed").encodePrettily());
              }
            });
        }
      });
  }

  /**
   * This Method updates only the password of a user.
   * @param routingContext
   * @param newPassword the new password to set
   */
  private void updateUserPW(RoutingContext routingContext, String newPassword) {
    //Getting the user_id from the current session
    int userId = routingContext.user().get("user_id");
    //Getting the user_id from the path parameter
    String userIdFromPath = routingContext.pathParam("user_id");

    //Hash Password
    String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt());

    //Query to check if user is an admin
    sqlClient.preparedQuery("SELECT * FROM users WHERE isAdmin = 1 AND user_id = ?")
      .execute(Tuple.of(userId), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> adminRow = ar.result();
          if (adminRow.size() == 0) {
            //Redirect if not an admin
            routingContext.response()
              .setStatusCode(303)
              .putHeader("Location", "/home/")
              .end();
            return;
          }
          //Query to update the password
          sqlClient.preparedQuery("UPDATE users SET password = ? WHERE user_id = ?")
            .execute(Tuple.of(hashPw, userIdFromPath), arUser -> {
              if (arUser.succeeded()) {
                // Password successfully updated
                if (arUser.result().rowCount() > 0) {
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(201)
                    .end(new JsonObject().put("success", "User password successfully updated").encodePrettily());
                } else {
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", "No user_id returned").encodePrettily());
                }
              } else {
                //No user_id returned
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database query: INSERT USER failed").encodePrettily());
              }
            });
        }
      });
  }

  /**
   * This Method updates both the username and password of a user.
   * @param routingContext
   * @param newUserName the new username that is wished to be set
   * @param newPassword the new password to set
   */
  private void updateUserWhole(RoutingContext routingContext, String newUserName, String newPassword) {
    //Getting the user_id from the current session
    int userId = routingContext.user().get("user_id");
    //Getting the user_id from the path parameter
    String userIdFromPath = routingContext.pathParam("user_id");

    //Hash Password
    String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt());

    //Query to check  if user is an admin
    sqlClient.preparedQuery("SELECT * FROM users WHERE isAdmin = 1 AND user_id = ?")
      .execute(Tuple.of(userId), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> adminRow = ar.result();
          if (adminRow.size() == 0) {
            //Redirect if not an admin
            routingContext.response()
              .setStatusCode(303)
              .putHeader("Location", "/home/")
              .end();
            return;
          }
          //Query to update the password and the username
          sqlClient.preparedQuery("UPDATE users SET password = ?, username = ? WHERE user_id = ?")
            .execute(Tuple.of(hashPw, newUserName,userIdFromPath), arUser -> {
              if (arUser.succeeded()) {
                //Password and Username successfully updated
                if (arUser.result().rowCount() > 0) {
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(201)
                    .end(new JsonObject().put("success", "User successfully updated").encodePrettily());
                } else {
                  //No changes made
                  routingContext.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(new JsonObject().put("error", "No user_id returned").encodePrettily());
                }
              } else {
                //Failed to update user
                routingContext.response()
                  .putHeader("content-type", "application/json")
                  .setStatusCode(500)
                  .end(new JsonObject().put("error", "Database query: INSERT USER failed").encodePrettily());
              }
            });
        }
      });
  }

  /**
   * This Method deletes a user with the given user_id.
   * @param routingContext
   */
  public void deleteUser(RoutingContext routingContext) {
    //This variable is unnecessary for this method
    int userId = routingContext.user().get("user_id");
    //Getting the user_id from the path parameter
    String userIdFromPath = routingContext.pathParam("user_id");

    //Query to delete the user
    sqlClient.preparedQuery("DELETE FROM users WHERE user_id = ?")
      .execute(Tuple.of(userIdFromPath), arUser -> {
        if (arUser.succeeded()) {
          //User successfully deleted
          if (arUser.result().rowCount() > 0) {
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(201)
              .end(new JsonObject().put("success", "User successfully deleted").encodePrettily());
          } else {
            //No user ID returned
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(500)
              .end(new JsonObject().put("error", "No user_id returned").encodePrettily());
          }
        } else {
          //Deletion failed
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(500)
            .end(new JsonObject().put("error", "Database query: INSERT USER failed").encodePrettily());
        }
      });
  }

  /**
   * This Method searches for users by their username.
   * @param routingContext
   */
  public void getUserByName(RoutingContext routingContext) {
    //This variable is unnecessary for this method
    int userId = routingContext.user().get("user_id");
    //Getting the searched after username from the path parameter
    String searchedString = routingContext.pathParam("searched_string");

    //Query to search for users by username
    sqlClient.preparedQuery("SELECT * from users WHERE username LIKE CONCAT('%', ?, '%') AND isAdmin = 0")
      .execute(Tuple.of(searchedString), arUser -> {
        if (arUser.succeeded()) {
          RowSet<Row> userRes = arUser.result();
          if (userRes.size() > 0) {
            //Add an array for all found users
            JsonArray userArray = new JsonArray();
            for (Row row : userRes) {
              //Putting every found user into an JSON Object and adding it to the array
              JsonObject userRow = new JsonObject()
                .put("user_id", row.getInteger("user_id"))
                .put("username", row.getString("username"));
              userArray.add(userRow);
            }
            //Return the list of users
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(Json.encodePrettily(userArray));
          } else {
            //No users found
            routingContext.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(404)
              .end(new JsonObject().put("error", "No User found").encodePrettily());
          }
        } else {
          //Search user failed
          routingContext.response()
            .putHeader("content-type", "application/json")
            .setStatusCode(500)
            .end(new JsonObject().put("error", "Database query: Search USER failed").encodePrettily());
        }
      });
  }

  /**
   * This Method checks if the current user is an admin.
   * @param routingContext
   */
  public void isAdmin(RoutingContext routingContext) {
    if (routingContext.user() != null && routingContext.user().get("isAdmin").equals(true)) {
      routingContext.next();
    } else {
      //If current user isnt an admin, then redirected to login page
      routingContext.response()
        .setStatusCode(303)
        .putHeader("Location", "/login")
        .end();
    }
  }

  /**
   * This Method gets all the current user's data from the session.
   * @param routingContext
   */
  public void getCurrentUserData(RoutingContext routingContext) {
    if (routingContext.user() != null){
      JsonObject result = new JsonObject()
        .put("username", routingContext.user().get("username"))
        .put("user_id", routingContext.user().get("user_id"))
        .put("isAdmin", routingContext.user().get("isAdmin"));
      routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(201)
        .end(Json.encodePrettily(result));
    }
  }
}
