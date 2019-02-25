package engg2800;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
    primaryStage.setTitle("GenBlock GUI (g 43)");
    primaryStage.getIcons().add(new Image("engg2800/oscilloscope_icon.PNG"));
    Scene scene = new Scene(root, 600, 400);
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();

    //Close serial connection on close request
    primaryStage.setOnCloseRequest(we -> {
      if(SerialPort.getCommPorts().length!=0){
        SerialController.close_connection();
      }
      Platform.exit();
    });
  }

  public static void main(String[] args) {
    launch(args);
  }
}
