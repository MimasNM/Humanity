package org.example.humanity;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage stage;

    /** Открывает окно по fxml из resources. Вызывается из контроллеров для перехода между экранами. */
    public static void showView(String fxml, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Scene scene = new Scene(loader.load(), width, height);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить " + fxml, e);
        }
    }

    /**
     * То же самое, но после завершения текущей загрузки fxml.
     * Нужно, когда переход инициируется из initialize(): иначе внешний вызов
     * showView успеет перезаписать сцену, установленную изнутри.
     */
    public static void showViewLater(String fxml, int width, int height) {
        Platform.runLater(() -> showView(fxml, width, height));
    }

    @Override
    public void start(Stage stage) {
        Main.stage = stage;
        stage.setTitle("Humanity");
        showView("auth-view.fxml", 380, 240);
        stage.show();
    }
}
