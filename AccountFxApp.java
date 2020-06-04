package de.cbw.jav.account.fx;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import de.cbw.jav.account.DataSources;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


/**
 * FX Application class. Note that FX application classes must feature
 * a public no-arg constructor because the FX launch() method uses the
 * former to create an instance of said class!
 * @author Sascha Baumeister
 */
public class AccountFxApp extends Application  {
	static private final String TITLE = "The Devil´s Bank (interest calculator!!!)";
	static private final double WIDTH = 480;
	static private final double HEIGHT = 360;
	static private final String PACKAGE_PATH = AccountFxApp.class.getPackage().getName().replace('.', '/');
	static private final String MAIN_FXML_PATH  = PACKAGE_PATH + "/main.fxml";
	static private final String MAIN_CSS_PATH  = PACKAGE_PATH + "/main.css";
	static private final String MAIN_ICON_PATH = PACKAGE_PATH + "/main.png";

	private MainController controller = null;


	/**
	 * Application entry point. Calls the launch() method to create an instance of
	 * this application class, and invoke the start() method.
	 * @param args the runtime arguments
	 */
	static public void main (String[] args) {
		AccountFxApp.launch(args);
	}


	/**
	 * Configures the primary window and it's components, and displays the former.
	 * @param primaryWindow the primary application window
	 * @throws IOException if there is an I/O related problem
	 * @throws SQLException if there is an SQL related problem
	 */
	@Override
	public void start (final Stage primaryWindow) throws IOException, SQLException {
		final Image icon = newImage(MAIN_ICON_PATH);
		final Connection connection = DataSources.ACCOUNT_SOURCE.getConnection();
		final BorderPane pane = newPane(MAIN_FXML_PATH);
		this.controller = new MainController(pane, connection);

		final Scene sceneGraph = new Scene(pane, WIDTH, HEIGHT);
		sceneGraph.getStylesheets().add(MAIN_CSS_PATH);

		primaryWindow.setTitle(TITLE);
		primaryWindow.getIcons().add(icon);
		primaryWindow.setScene(sceneGraph);
		primaryWindow.setWidth(sceneGraph.getWidth());
		primaryWindow.setHeight(sceneGraph.getHeight());
		primaryWindow.show();
	}


	/**
	 * Closes any resources upon closing the primary window.
	 * @throws SQLException if there is an SQL related problem
	 */
	@Override
	public void stop () throws SQLException {
		this.controller.close();
	}


	/**
	 * Creates a new image and returns it.
	 * @param imagePath the image path
	 * @return the image
	 * @throws IOException if there is an I/O related problem
	 */
	static public Image newImage (String imagePath) throws IOException {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream byteSource = classLoader.getResourceAsStream(imagePath)) {
			return new Image(byteSource);
		}
	}


	/**
	 * Creates a new pane and returns it.
	 * @param <T> the resulting pane type
	 * @param fxmlPath the FXML path
	 * @return the pane
	 * @throws IOException 
	 */
	static public <T extends Pane> T newPane (final String fxmlPath) throws IOException {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream byteSource = classLoader.getResourceAsStream(fxmlPath)) {
			return new FXMLLoader().load(byteSource);
		}
	}
}