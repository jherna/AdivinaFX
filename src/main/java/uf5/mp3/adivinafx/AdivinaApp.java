package uf5.mp3.adivinafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A la branca master el joc és només amb integers i a la branca objectes amb Jugada i EstatJoc
 */

public class AdivinaApp extends Application {
    public static final int MAIN_WINDOW_WIDTH = 320;
    public static final int MAIN_WINDOW_HEIGHT = 240;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AdivinaApp.class.getResource("fxml/root-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
        stage.setTitle("Adivina");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}