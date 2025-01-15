package de.thm.mni.informatikProjekt.snapstash;

import de.thm.mni.informatikProjekt.snapstash.handler.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.io.File;
import java.sql.*;

/**
 * The MainVerticle file is the entry point to our application, from here you can start the server
 * It also contains the needed routes and the SQL client
 */

public class MainVerticle extends AbstractVerticle {

  //Information about the Database
  private static final int PORT = 8888;
  private static final String PHOTOS_DIR = "photos";
  private static final String DB_PW = "4WdcDoDQQoYUihQyg";
  private static final String DB_USER = "snapUser";
  private static final String DB_URL = "jdbc:mariadb://5.181.49.182:3306/snapStashDB";

  private JDBCPool sqlClient;

  /**
   * A dedicated Method to start the Server, so you dont have to manually do it.
   * @param args
   */
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }

  /**
   * This Method initializes the HTTP Server
   * @param startPromise  a promise which should be called when verticle start-up is complete.
   * @throws Exception which is thrown when any errors occur while setting up the HTTP Server
   */
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    initSqlClient();

    Router router = Router.router(vertx);
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    router.route("/*").handler(StaticHandler.create().setCachingEnabled(false));

    //Redirect tto the login page
    router.route("/").handler(routingContext -> {
      routingContext.response().setStatusCode(302).putHeader("Location", "/login").end();
    });

    router.route("/photos/*").handler(StaticHandler.create().setWebRoot("photos"));

    //Handlers for every Structure used by the application
    UserHandler userHandler = new UserHandler(sqlClient);
    KeywordHandler keywordHandler = new KeywordHandler(sqlClient);
    PhotoHandler photoHandler = new PhotoHandler(sqlClient);
    LoginHandler loginHandler = new LoginHandler(sqlClient);
    AlbumHandler albumHandler = new AlbumHandler(sqlClient);


    /**
     * Route used for checking if a login was valid or not.
     * Possible statuscodes: 400,500,200,404
     */
    router.post("/login").handler(loginHandler::loginNEW);
    /**
     * Route used for logging out the current user
     * Possible statuscodes: 303
     */
    router.get("/logout").handler(loginHandler::logout);

    /**
     * This route makes sure that the user is loged in before being allowed to use certain methods.
     */
    router.route("/home/*").handler(loginHandler::isAuth).handler(StaticHandler.create("home").setCachingEnabled(false));


    //Routes used for managing users by admin
    router.route("/users/*").handler(loginHandler::isAuth);
    router.get("/users").handler(userHandler::isAdmin).handler(userHandler::getUsers);
    router.post("/users").handler(userHandler::createUser);
    router.patch("/users/:user_id").handler(userHandler::updateUser);
    router.delete("/users/:user_id").handler(userHandler::isAdmin).handler(userHandler::deleteUser);
    router.get("/users/search/:searched_string").handler(userHandler::isAdmin).handler(userHandler::getUserByName);
    router.get("/users/curUser").handler(userHandler::getCurrentUserData);


    //Routes for managing albums
    router.get("/home/albums").handler(albumHandler::getAllAlbums);
    router.get("/home/albums/search/:searched_string").handler(albumHandler::getAlbumByTitleOrKeyword);
    router.post("/home/albums").handler(albumHandler::createAlbum);
    router.put("/home/albums/:album_id").handler(albumHandler::updateAlbumTitle);
    router.delete("/home/albums/:album_id").handler(albumHandler::deleteAlbum);
    router.post("/home/albums/:album_id/keywords/:keyword").handler(keywordHandler::createKeyword).handler(keywordHandler::assignKeywordToAlbum);
    router.delete("/home/albums/:album_id/keywords/:keyword").handler(keywordHandler::deleteKeywordFromAlbum);
    router.get("/home/albums/:album_id/keywords").handler(keywordHandler::getKeywordsFromAlbum);

    //Routes for managing photos
    router.get("/home/albums/:album_id/photos").handler(loginHandler::isAuth).handler(photoHandler::checkAlbumsUsers).handler(photoHandler::getAllPhotosFromAlbum);
    router.get("/home/albums/:album_id/photos/:photo_id").handler(photoHandler::getPhotoById);
    router.get("/home/albums/:album_id/photos/search/:searched_string").handler(photoHandler::getPhotoByTitleOrKeyword);
    router.post("/home/albums/:album_id/photos").handler(BodyHandler.create().setUploadsDirectory("photos")).handler(photoHandler::createPhoto);
    router.patch("/home/albums/:album_id/photos/:photo_id").handler(photoHandler::updatePhoto);
    router.delete("/home/albums/:album_id/photos/:photo_id").handler(photoHandler::deletePhoto);
    router.post("/home/albums/:album_id/photos/:photo_id/keywords/:keyword").handler(keywordHandler::createKeyword).handler(keywordHandler::assignKeywordToPhoto);
    router.delete("/home/albums/:album_id/photos/:photo_id/keywords/:keyword").handler(keywordHandler::deleteKeywordFromPhoto);
    router.get("/home/albums/:album_id/photos/:photo_id/keywords/").handler(keywordHandler::getKeywordsFromPhoto);

    //Route to get all photos
    router.get("/home/albums/allPhotos/").handler(photoHandler::getAllPhotosFromUser);


    //Start HTTP Server
    vertx.createHttpServer().requestHandler(router).listen(PORT, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port " + PORT);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  /**
   * Method being used to redirect to the login page
   * @param routingContext
   */
  private void getToLoginPage(RoutingContext routingContext) {
    String path = "webroot/login";

  }

  /**
   * Method for initializing the SQL client for database use
   */
  private void initSqlClient() {
    final JsonObject config = new JsonObject()
      .put("url", DB_URL)
      .put("driver_class", "org.mariadb.jdbc.Driver")
      .put("user", DB_USER)
      .put("password", DB_PW);

    this.sqlClient = JDBCPool.pool(vertx,config);

  }

}
