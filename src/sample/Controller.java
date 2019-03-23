package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;

public class Controller {

    @FXML private TextInputControl key;
    @FXML public TextArea text;
    @FXML public TextArea output;

    private AES aes = new AES();

    @FXML private void encodeMessage() throws Exception {
        String _key = this.key.getText();
        String message = this.text.getText();

        try{
            byte[] key = aes.init(_key);
            String encrypted = aes.encrypt(message,key);
            this.output.setText(encrypted);
        } catch (Exception e) {
            Util.showAlert("Ошибка", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML private void decodeMessage() throws Exception {
        String _key = this.key.getText();
        String cipherText = this.output.getText();

        try{
            byte[] key = aes.init(_key);
            String decrypted = aes.decrypt(cipherText, key);
            this.output.setText(decrypted);
        } catch (Exception e) {
            Util.showAlert("Ошибка", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML private void save(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для сохранения");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("aes files(*.aes)", "*.aes"));
        fileChooser.setInitialFileName("*.aes");

        File file = fileChooser.showSaveDialog(((Node)event.getTarget()).getScene().getWindow());

        if(file != null) {
            Util.writeFile(file, output.getText().getBytes());
            Util.showAlert(null, "Файл успешно создан", AlertType.INFO);
        }

    }

    @FXML private void load(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("aes, txt files(*.aes, *.txt)", "*.aes", "*.txt"));
        fileChooser.setTitle("Выберите файл");
        File file = fileChooser.showOpenDialog(((Node)event.getTarget()).getScene().getWindow());

        if(file != null) {

            String input = Util.readFile(file);
            String ext = Util.getFileExtension(file.getName());

            if (ext.equals("aes")) {
                output.setText(input);
            } else {
                text.setText(input);
            }

        }
    }
}
