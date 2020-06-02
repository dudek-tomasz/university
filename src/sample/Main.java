package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.utils.ReferencesContainer;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Kanban");
        primaryStage.setScene(new Scene(root, 675, 500));
        primaryStage.show();

        ReferencesContainer.getInstance();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
