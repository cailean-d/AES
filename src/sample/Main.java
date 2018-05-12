package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("AES");
        primaryStage.setScene(new Scene(root, 450, 400));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("aes.png")));
        primaryStage.show();

        List<String> parameters = getParameters().getUnnamed();

        if(parameters.size() > 0){

            Controller myController = loader.getController();
            File file = new File(parameters.get(0));

            String input = Util.readFile(file);
            String ext = Util.getFileExtension(file.getName());

            if (ext.equals("aes")) {
                myController.output.setText(input);
            } else {
                myController.text.setText(input);
            }

        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
