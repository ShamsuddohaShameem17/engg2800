package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

//Serial-Controller class


public class Controller {

  @FXML
  private ToggleButton channel1Button;

  @FXML
  private ToggleButton channel2Button;

  @FXML
  private Button resetButton;

  @FXML
  private MenuBar menubar;

  @FXML
  private Menu file;

  @FXML
  private MenuItem closeMenuItem;

  @FXML
  private Menu help;

  @FXML
  private MenuItem checkConnection;

  @FXML
  private MenuItem aboutMenuItem;

  @FXML
  private Button sendButton;

  @FXML
  private Label ch1Label;

  @FXML
  private Label waveLabel;

  @FXML
  private Label frequencyLabel;

  @FXML
  private ChoiceBox<String> ch1WaveChoiceBox;

  @FXML
  private TextField ch1FreqInput;

  @FXML
  private TextField ch1AmpInput;

  @FXML
  private TextField ch1OffsetInput;

  @FXML
  private Label ampLabel;

  @FXML
  private Label offsetLabel;

  @FXML
  private Label ch2Label;

  @FXML
  private ChoiceBox<String> ch2WaveChoiceBox;

  @FXML
  private TextField ch2FreqInput;

  @FXML
  private TextField ch2AmpInput;

  @FXML
  private TextField ch2OffsetInput;

  @FXML
  private Canvas canvas;

  @FXML
  private Label tempLabel;

  @FXML
  private TextField tempDisplay;

  private ObservableList<String> waves = FXCollections
      .observableArrayList("-", "Sine", "Square", "Sawtooth", "Triangle");
  private ObservableList<String> filters = FXCollections.observableArrayList("-", "LPF", "HPF");
  private GraphicsContext gc;
  private SerialController serialCo;
  private String dataSend;
  private String ch1Amp, ch1Offset;
  private String ch2Amp, ch2Offset;



  @FXML
  void aboutMenuHandler(ActionEvent event) {

  }

  @FXML
  void checkSerialConnection(ActionEvent event) {

  }

  @FXML
  void closeMenuHandler(ActionEvent event) {
    Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
    confirmDialog.setTitle("Exiting...?");
    confirmDialog.setHeaderText("Do you want to close this application?");
    //confirmDialog.setContentText("Select your choice.");
    boolean confirmExit = (confirmDialog.showAndWait().get() == ButtonType.OK);
    if (confirmExit) {
      Platform.exit();
    }
  }

  @FXML
  void readCh1AmpInput(ActionEvent event) {
    ch1Amp = ch1AmpInput.getText();

  }


  @FXML
  void readCh1FreqInput(ActionEvent event) {
    if (gc.getFill().toString().compareTo("0x00ffffff") == 0) {
      draw_empty_canvas();
    }

    try {
//      try {
//        double frequencyTwo = Double.valueOf(ch2FreqInput.getText());
//        draw_canvas(Color.WHITESMOKE,ch2WaveChoiceBox.getValue(),frequencyTwo);
//      }catch (Exception e){
//
//      }
      double frequency = Double.valueOf(ch1FreqInput.getText());
      draw_canvas(Color.BLACK, ch1WaveChoiceBox.getValue(), 0, 0, frequency);

    } catch (NumberFormatException ne) {
      ch1FreqInput.clear();
    }

  }

  @FXML
  void readCh1OffsetInput(ActionEvent event) {
    ch1Offset=ch1OffsetInput.getText();
  }

  @FXML
  void readCh2AmpInput(ActionEvent event) {
    ch2Amp=ch2AmpInput.getText();
  }

  @FXML
  void readCh2FreqInput(ActionEvent event) {
    if (gc.getFill().toString().compareTo("0x00ffffff") != 0) {
      draw_empty_canvas();
    }
    //draw_empty_canvas();
//    double frequencyOne = Double.valueOf(ch1FreqInput.getText());
//    draw_canvas(Color.BLACK,ch1WaveChoiceBox.getValue(),frequencyOne);

    try {
      double frequency = Double.valueOf(ch2FreqInput.getText());
      draw_canvas(Color.WHITESMOKE, ch2WaveChoiceBox.getValue(), 0, 0, frequency);

    } catch (NumberFormatException ne) {
      ch2FreqInput.clear();
    }

  }

  @FXML
  void readCh2OffsetInput(ActionEvent event) {

  }

  @FXML
  void resetButtonPressed(ActionEvent event) {
    channel1Button.setSelected(false);
    channel2Button.setSelected(false);

    initialize();
  }

  @FXML
  void sendButtonPressed(ActionEvent event) {
    String message="----------";
    if(serialCo.check_connection()){
      serialCo.write_data_line(message);
    }else{
      System.out.println("Send Error:Need to initiate serial communication!!");
    }

  }

  @FXML
  void toggleCh1(ActionEvent event) {
    boolean toggled = ch1FreqInput.isDisable();
    if (toggled) {
      ch1FreqInput.disableProperty().setValue(false);
      ch1AmpInput.disableProperty().setValue(false);
      ch1OffsetInput.disableProperty().setValue(false);
    } else {
      ch1FreqInput.disableProperty().setValue(true);
      ch1AmpInput.disableProperty().setValue(true);
      ch1OffsetInput.disableProperty().setValue(true);
    }


  }

  @FXML
  void toggleCh2(ActionEvent event) {
    boolean toggled = ch2FreqInput.isDisable();
    if(toggled){
      ch2AmpInput.disableProperty().setValue(false);
      ch2FreqInput.disableProperty().setValue(false);
      ch2OffsetInput.disableProperty().setValue(false);
    } else {
      ch2FreqInput.disableProperty().setValue(true);
      ch2AmpInput.disableProperty().setValue(true);
      ch2OffsetInput.disableProperty().setValue(true);
    }
  }

  @FXML//MADE THIS UP
  private void draw_canvas(Color color, String wavename, double amp, double offset,
      double frequency) {
    switch (wavename) {
      case "Sine":
        draw_sinewave(color, amp, frequency, offset);
        break;
      case "Square":
        draw_square_wave(color, amp, frequency, offset);
        break;
      case "Sawtooth":
        draw_sawtooth_wave(color, amp, frequency, offset);
        break;
      case "Triangle":
        draw_triangle_wave(color, amp, frequency, offset);
        break;
      default:
        break;
    }
  }


  private boolean check_offset(String offsetStr){
    double offset;
    try{
      offset = Double.valueOf(offsetStr);
      return (offset>=-3)&&(offset<=3);
    }catch (NumberFormatException ne){
      //Could show the user to enter something as a dialog window
      //For now do nothing
    }
    return false;
  }

  private boolean check_amp(String ampStr){
    double amp;
    try{
      amp = Double.valueOf(ampStr);
      return (amp>=-3)&&(amp<=3);
    }catch (NumberFormatException ne){
      //Could show the user to enter something as a dialog window
      //For now do nothing
    }
    return false;

  }

  private void draw_empty_canvas() {
    gc.setFill(Color.AQUA);
    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    gc.setStroke(Color.BLACK);
    gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
    gc.setStroke(Color.GOLD);
    gc.setLineWidth(2.0);
    gc.strokeLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);
    gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
  }

  private double period_calculator(double frequency) {
    //1500 is good for 20KHz
    //600 is good for 10KHz
    //450
    //*300 is good for 2000hz
    //*200 is good for 1000hz
    //*10 is good for 1 hz (with /24)
    //*500 is good for 10hz
    double p = 0;
    if (frequency >= 20000) {
      gc.setLineWidth(0.2);
      p = 1500;
    } else if (frequency >= 10) {
      gc.setLineWidth(0.9);
      p = 600;
    } else if (frequency >= 0) {
      gc.setLineWidth(2);
      p = 500;//50
    }
    return p;
  }

  private void draw_sinewave(Color color, double amp, double frequency, double offset) {//int A, int frequency
    double a = 0.0;
    gc.setStroke(color);
    double middleHalf = canvas.getHeight() / 2;
    double inc = (Math.PI * 2) / 24;//old:24
    //double A = 50.0; default
    //if A <0 or freq < 0 or >10Khz show a message
    double points = period_calculator(frequency);
    double period = (1 / frequency) * points;
    
    if (amp <= 0) {
      amp = 50.0;
    }
    for (double i = 0; i < canvas.getWidth(); i = i + period) {
      gc.strokeLine(i, middleHalf, i, middleHalf + Math.sin(a) * amp);
      a += inc;
    }
    gc.setLineWidth(1);
  }

  private void draw_square_wave(Color color, double amp, double frequency, double offset) {
    gc.setStroke(color);
    double a = 0.0;
    double middleHalf = canvas.getHeight() / 2;
    double inc = (Math.PI * 2) / 24;//old:24
    double p = (1 / frequency) * period_calculator(frequency);

    if (amp <= 0) {
      amp = 50.0;
    }
    for (double i = 0; i < canvas.getWidth(); i = i + p) {
      gc.strokeLine(i, middleHalf, i, middleHalf + Math.signum(Math.sin(a)) * amp);//Working
      a += inc;
    }
    gc.setLineWidth(1);
  }

  private void draw_sawtooth_wave(Color color, double amp, double frequency, double offset) {
    gc.setStroke(color);
    double a = 0.0;
    double inc = (Math.PI * 2) / 24;//old:24
    double middleHalf = canvas.getHeight() / 2;
    double period = (1 / frequency) * period_calculator(frequency);

    for (double i = 0; i < canvas.getWidth(); i = i + 2 * period) {
      if (Math.signum(Math.sin(a)) != 1) {
        gc.strokeLine(i, middleHalf, i, middleHalf + amp * (Math.abs(amp % 2 * period - amp)));
      }

      a += inc;
    }
    gc.setLineWidth(1);

  }

  private void draw_triangle_wave(Color color, double amp, double frequency, double offset) {

  }

  private void update_temp() {
    //try check for temperature change.
    //If not connected, show '0'
    tempDisplay.setPromptText("°C");
    tempDisplay.setEditable(false);
  }


  @FXML
  void initialize() {

    ch1FreqInput.disableProperty().setValue(true);
    ch2FreqInput.disableProperty().setValue(true);
    ch1AmpInput.disableProperty().setValue(true);
    ch2AmpInput.disableProperty().setValue(true);
    ch1OffsetInput.disableProperty().setValue(true);
    ch2OffsetInput.disableProperty().setValue(true);

    ch1FreqInput.clear();
    ch2FreqInput.clear();
    ch1AmpInput.clear();
    ch2AmpInput.clear();
    ch1OffsetInput.clear();
    ch2OffsetInput.clear();

    ch1WaveChoiceBox.setItems(waves);
    ch2WaveChoiceBox.setItems(waves);
    ch1WaveChoiceBox.setValue("-");
    ch2WaveChoiceBox.setValue("-");
    ch1AmpInput.setPromptText("V");
    ch2AmpInput.setPromptText("V");
    ch1FreqInput.setPromptText("Hz");
    ch2FreqInput.setPromptText("Hz");
    ch1OffsetInput.setPromptText("V");
    ch2OffsetInput.setPromptText("V");
    resetButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12.0));
    sendButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12.0));
    update_temp();
    gc = canvas.getGraphicsContext2D();
    dataSend ="";

    canvas.setStyle("-fx-border-color: blue;");

    draw_empty_canvas();
    //////////////////////////

    //draw_sinewave(Color.WHITESMOKE,70,10000);
    //draw_sinewave(Color.BLACK,70,5000);
    //draw_sinewave(Color.RED,70,2500);
    //draw_sinewave(Color.BLACK,70,9);

    //draw_sinewave(Color.GREENYELLOW,70,3000);
    ////////////////////////
    //Serial Parts
    SerialController.see_available_ports();
    serialCo = new SerialController();
    serialCo.select_port("COM4");
    serialCo.init();
  }

}