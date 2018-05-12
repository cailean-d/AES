package sample;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

enum AlertType { WARNING, INFO }

public class Util {

    public static void showAlert(String title, String text, AlertType type){

        Alert.AlertType alertType;

        if (type == AlertType.WARNING) {
            alertType = Alert.AlertType.WARNING;
        } else {
            alertType = Alert.AlertType.INFORMATION;
        }

        Alert dialog = new Alert(alertType);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Controller.class.getResourceAsStream("aes.png")));
        dialog.setContentText(text);
        dialog.showAndWait();
    }

    public static String readFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] inputBytes = new byte[(int) file.length()];
        inputStream.read(inputBytes);
        return new String(inputBytes);
    }

    public static void writeFile(File filename, byte[] textBytes){
        try{

            FileOutputStream outputStream = new FileOutputStream(filename);
            outputStream.write(textBytes);
            outputStream.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String getFileExtension(String filename) {

        String extension = "";

        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i+1);
        }

        return extension;
    }

}
