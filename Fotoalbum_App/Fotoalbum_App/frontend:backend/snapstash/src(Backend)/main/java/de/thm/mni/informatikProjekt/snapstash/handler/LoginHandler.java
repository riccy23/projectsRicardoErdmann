package de.thm.mni.informatikProjekt.snapstash.handler;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * This class handles the authentification process for the application,
 * like: validating the login, creating a session and logging out
 */
public class LoginHandler {
  JDBCPool sqlClient;

  /**
   * Constructor for the LoginHandler with the needed SQL client
   * @param sqlClient
   */
  public LoginHandler(JDBCPool sqlClient) {
    this.sqlClient = sqlClient;
  }

  /**
   * This Method validates the given credetials and creates a session for the user
   * @param routingContext
   */
  public void loginNEW(RoutingContext routingContext) {
    //opening a bodyHandler in order to process the request body (given credentials)
    routingContext.request().bodyHandler(body -> {
      JsonObject jsonObject;
      try {
        //Converting the content of the body into a JSON Object
        jsonObject = body.toJsonObject();

      } catch (Exception e) {
        //Case if the content of the body is not a valid JSON Object
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Invalid Input").encodePrettily());
        return;
      }

      //Getting both the username and the password from JSON Body
      String userName = jsonObject.getString("username", "").trim();
      String password = jsonObject.getString("password", "").trim();

      //Checking if username or password is empty
      if (userName.isEmpty() || password.isEmpty()) {
        routingContext.response()
          .setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("error", "Please enter a username AND a password").encodePrettily());
        return;
      }

      //SQL Query which gets us the correct result set for this request
      sqlClient.preparedQuery("SELECT user_id, password, isAdmin FROM users WHERE username = ?;")
        .execute(Tuple.of(userName))
        //case if there is a problem with the database request
        .onFailure(fail -> {
          routingContext.response()
            .setStatusCode(500)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", "Database failure").encodePrettily());
        })
        //Handle the request if query was successful
        .onSuccess(rows -> {
          //Checking if the user with the given username exists
          if (rows.size() > 0) {
            Row row = rows.iterator().next();
            //Checking if the given password from the JSON Body corresponds with the hashed password from the database
            if (BCrypt.checkpw(password, row.getString("password"))) {
              createUserSession(routingContext, userName, row.getInteger("user_id"), row.getBoolean("isAdmin"));
              routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("user_id", row.getInteger("user_id")).encodePrettily());
            } else {
              //Case if the passwords dont match
              routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Wrong credentials, pleas try again.").encodePrettily());
            }
          } else {
            //Case if there is no user with such username in the database
            routingContext.response()
              .setStatusCode(404)
              .putHeader("content-type", "application/json")
              .end(new JsonObject().put("error", "Wrong credentials, pleas try again.").encodePrettily());
          }
        });
    });
  }


  /**
   * This Method creates a session with the given params.
   * @param routingContext
   * @param username
   * @param userId
   * @param isAdmin
   */
  public void createUserSession(RoutingContext routingContext, String username, int userId, boolean isAdmin) {
    routingContext.setUser(User.fromName(username));
    routingContext.user().attributes().put("username", username);
    routingContext.user().attributes().put("user_id", userId);
    routingContext.user().attributes().put("isAdmin", isAdmin);
  }

  /**
   * This Method checks if there is currently loged in user,
   * if not then there is a redirect to the login page
   * @param routingContext
   */
  public void isAuth(RoutingContext routingContext) {
    if (routingContext.user() != null) {
      routingContext.next();
    } else {
      routingContext.response()
        .setStatusCode(303)
        .putHeader("Location", "/login")
        .end();
    }

  }

  /**
   * This Method logs out the current user and redirects him to the login page
   * @param routingContext
   */
  public void logout(RoutingContext routingContext) {
    routingContext.setUser(null);
    routingContext.response()
      .setStatusCode(303)
      .putHeader("Location", "/login")
      .end();
  }
}
