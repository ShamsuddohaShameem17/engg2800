package engg2800;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

  /*Variables used in the project*/
  private ObservableList<String> waves = FXCollections
          .observableArrayList("-", "Sine", "Square", "Sawtooth","Reverse Sawtooth", "Triangle");
  private GraphicsContext gc;
  private SerialController serialCo;
  private SerialPort defaultPort;
  private String dataSend;//DEBUG for now
  private String dataReceive;//Initial MCU communication
  private String ch1Amp, ch1Offset;
  private String ch2Amp, ch2Offset;
  private String ch1Freq, ch2Freq;

  @FXML
  void aboutMenuHandler(ActionEvent event) {
    Notification about = new Notification("About Project");
    String header = "GenBlock project details";
    Hyperlink gitLink = new Hyperlink("https://source.eait.uq.edu.au/gitlist/engg2800g43");
    gitLink.onActionProperty().set(e->{
      try {
        java.awt.Desktop.getDesktop().browse(new URI(gitLink.getText()));
      } catch (IOException|URISyntaxException netERR) {
        System.out.println(netERR);
      }});
    Label message = new Label("Visit: ");
    FlowPane fp = new FlowPane();
    fp.getChildren().addAll(message,gitLink);
    about.alert = new Alert(AlertType.INFORMATION);
    about.alert.setTitle(about.title);
    about.alert.setHeaderText(header);
    about.alert.getDialogPane().contentProperty().set(fp);
    about.alert.showAndWait();
  }

  @FXML
  void checkSerialConnection(ActionEvent event) {
    Notification serialNotif = new Notification("Serial Connection test");
    serialNotif.message="Comm port available: "+ SerialController.available_ports();
    if(SerialPort.getCommPorts().length !=0){
      if(defaultPort==null){
        if(serialCo.get_COMM_port()!=null){
          defaultPort = serialCo.default_port()==null?serialCo.get_last_COMM_port():serialCo.default_port();
        }
        defaultPort = serialCo.default_port();
      }
      serialNotif.message+="\nDefault port: "+defaultPort.getSystemPortName();
      String status = serialCo.check_connection()?"Connected :)":"Disconnected :(";
      serialNotif.message+="        Status: "+status;//TAB
      String MCUStatus = serialCo.MCU_connected()?"Connected :)":"Disconnected :(";
      serialNotif.message+="\nMCU connection: "+MCUStatus;
      serialNotif.showAlert("INFORMATION");
    }else{
      //Try again
      SerialController serialCoRetry = new SerialController();
      if(serialCoRetry.default_port()!=null){
        serialCo = serialCoRetry;
        serialCo.select_port(serialCoRetry.default_port().getSystemPortName());
        defaultPort = serialCo.get_COMM_port();
        serialCo.init();
      }else{
        serialNotif.message = "Could not find any available port.";
        serialNotif.showAlert("WARNING");
      }
    }

  }

  @FXML
  void closeMenuHandler(ActionEvent event) {
    Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
    confirmDialog.setTitle("Exiting...?");
    confirmDialog.setHeaderText("Do you want to close this application?");
    boolean confirmExit = (confirmDialog.showAndWait().get() == ButtonType.OK);
    if (confirmExit) {
      Platform.exit();
    }
  }

  void errors_notification(String type){
    Notification errorRead;
    String title = "", message ="";
    String alertType = "INFORMATION";
    switch (type){
      case "amplitude":
        title = "Amplitude is not valid!";
        message = "Choose a value between 0 to 6.";
        break;
      case "frequency":
        title = "Frequency is not valid!";
        message = "Choose a value between 0 to 10KHz";
        break;
      case "offset":
        title = "Offset is not valid!";
        message = "Choose a value between -3 to 3.";
        break;
    }
    errorRead = new Notification(title);
    errorRead.message=message;
    errorRead.showAlert(alertType);
  }


  void readCh1AmpInput(String amp) {
    ch1Amp=check_amp(amp)?amp:ch1Amp;
    if(!check_amp(amp)){
      ch1AmpInput.setText(ch1Amp);
      errors_notification("amplitude");
    }else{
      if(channel1Button.isSelected()&&(!ch1FreqInput.getText().isEmpty())){
        draw_channel_wave("1");
      }
    }
  }


  void readCh1FreqInput(String freqStr) {
    //Colour of the paint is black now
    if (gc.getFill().toString().compareTo("0x00ffffff") == 0) {
      draw_empty_canvas();
      if(channel2Button.isSelected()&&(!ch2FreqInput.getText().isEmpty())){
        draw_channel_wave("2");
      }
    }
    try {
      ch1Freq=check_frequency(freqStr)?freqStr:ch1Freq;
      if(!check_frequency(freqStr)){
        ch1FreqInput.setText(ch1Freq);
        errors_notification("frequency");
      }
      draw_channel_wave("1");
    } catch (NumberFormatException ne) {
      ch1FreqInput.clear();
    }
  }


  void readCh1OffsetInput(String offset) {
    ch1Offset=check_offset(offset)?offset:ch1Offset;
    if(!check_offset(offset)){
      if(offset.compareTo("-")!=0){
        ch1OffsetInput.setText(ch1Offset);
        errors_notification("offset");
      }
    }
  }


  void readCh2AmpInput(String amp) {
    ch2Amp=check_amp(amp)?amp:ch2Amp;
    if(!check_amp(amp)){
      ch2AmpInput.setText(ch1Amp);
      errors_notification("amplitude");
    }else{
      if(channel2Button.isSelected()&&(!ch2FreqInput.getText().isEmpty())){
        draw_channel_wave("2");
      }
    }
  }


  void readCh2FreqInput(String freqStr) {
    if (gc.getFill().toString().compareTo("0x00ffffff") != 0) {
      draw_empty_canvas();
      if(channel1Button.isSelected()&&(!ch1FreqInput.getText().isEmpty())){
        draw_channel_wave("1");
      }
    }

    try {
      ch2Freq=check_frequency(freqStr)?freqStr:ch2Freq;
      if(!check_frequency(freqStr)){
        ch2FreqInput.setText(ch2Freq);
        errors_notification("frequency");
      }
      draw_channel_wave("2");
    } catch (NumberFormatException ne) {
      ch2FreqInput.clear();
    }

  }


  void readCh2OffsetInput(String offset) {
    ch2Offset=check_offset(offset)?offset:ch2Offset;
    if(!check_offset(offset)){
      if(offset.compareTo("-")!=0){
        ch1OffsetInput.setText(ch1Offset);
        errors_notification("offset");
      }
    }
  }

  @FXML
  void resetButtonPressed(ActionEvent event) {
    default_states();
    //toggle the buttons
    channel1Button.setSelected(false);
    channel2Button.setSelected(false);
    //Reset the values
    ch1AmpInput.setText("0");
    ch1OffsetInput.setText("0");
    ch1FreqInput.setText("0");
    ch2AmpInput.setText("0");
    ch2OffsetInput.setText("0");
    ch2FreqInput.setText("0");
    //initialize();
  }

  @FXML
  void sendButtonPressed(ActionEvent event) {
    String ch1Send ="";
    String ch2Send ="";
    boolean isch1set = channel1Button.isSelected();
    boolean isch2set = channel2Button.isSelected();
    //Initialise dataSend
    dataSend.concat("....START Sending Data to MCU....\n");
    if(isch1set){
      boolean validWave=check_valid_wave(ch1WaveChoiceBox.getValue(),ch1Amp,ch1Offset,ch1Freq);
      if(validWave){
        double amp= Double.valueOf(ch1Amp)*100.0;
        double offset = Double.valueOf(ch1Offset)*100.0;
        int wave = waves.indexOf(ch1WaveChoiceBox.getValue());
        int freq = Integer.valueOf(ch1FreqInput.getText());
        ch1Send= serial_wave_send_format("ch1",wave,(int)Math.round(amp), (int)Math.round(offset),freq);
        dataSend.concat("Channel1:"+ch1Send+"\n");
      }
    }

    if(isch2set){
      boolean validWave=check_valid_wave(ch2WaveChoiceBox.getValue(),ch2Amp,ch2Offset,ch2Freq);
      if(validWave){
        double amp= Double.valueOf(ch2Amp)*100.0;
        double offset = Double.valueOf(ch2Offset)*100.0;
        int wave = waves.indexOf(ch2WaveChoiceBox.getValue());
        int freq = Integer.valueOf(ch2FreqInput.getText());
        ch2Send= serial_wave_send_format("ch2",wave,(int)Math.round(amp), (int)Math.round(offset),freq);
        dataSend.concat("Channel2:"+ch2Send+"\n");
      }
    }

    try{
      //Serial connection
      //Initiate connection
      if(serialCo.check_connection()){//&&serialCo.MCU_connected() -> Not implemented rnow due to CTS/RTS flow-control is not working
        //Send greetings to MCU
        serialCo.write_data_line("$");//sequence
        if(isch1set||isch2set){
          serialCo.write_data('$',"");
          if(isch1set&&isch2set){
            System.out.println("Sending ch1 & ch2 data to Serial port "+defaultPort);
            System.out.println("ch1Send:"+ch1Send);
            System.out.println("ch2Send"+ch2Send);
            serialCo.write_data_line(ch1Send+ch2Send+"&");
          }else if(isch1set){
            System.out.println("Sending ch1 data to Serial port "+defaultPort);
            serialCo.write_data_line(ch1Send+"&");
          }else if(isch2set){
            System.out.println("Sending ch1 data to Serial port "+defaultPort);
            serialCo.write_data_line(ch2Send+"&");
          }
          dataSend.concat("....COMPLETE Sending Data to MCU....\n");
        }

      }else{
        System.out.println("DEBUG:"+dataSend);
        System.out.println("Send Error:Serial communication is not possible!!");
      }

    }catch (Exception e){
      System.out.println("DEBUG:"+"ch1:"+ch1Send+"length:"+ch1Send.length()+" ch2:"+ch2Send);
      System.out.println("ERROR: " + e.getLocalizedMessage());
    }

  }


  void toggle_property(TextField txt){
    boolean toggledBit = txt.disabledProperty().getValue();
    txt.disableProperty().setValue(!toggledBit);
  }

  @FXML
  void toggleCh1(ActionEvent event) {
    toggle_property(ch1AmpInput);
    toggle_property(ch1OffsetInput);
    toggle_property(ch1FreqInput);
  }

  @FXML
  void toggleCh2(ActionEvent event) {
    toggle_property(ch2AmpInput);
    toggle_property(ch2OffsetInput);
    toggle_property(ch2FreqInput);
  }

  private boolean check_valid_wave(String wavename,String amp, String offset,
                                   String frequency){
    try{
      boolean wavecheck = (wavename.compareTo("-")!=0);
      boolean ampcheck = check_amp(amp);
      boolean offsetcheck = check_offset(offset);
      boolean frequencycheck = check_frequency(frequency);
      //Silently checking for parameters->cancelled
      return (wavecheck&&ampcheck&&offsetcheck&&frequencycheck);
    }catch (NullPointerException err){
      ;;//Do nothing
    }
    return false;
  }


  private void draw_channel_wave(String channel){
    Color channelCol;
    String wavename,freq;
    boolean validwave=false;
    int amp, offset, frequency;

    switch (channel){
      case "1":
        //Channel 1
        //Color: Black
        ///DO: some checking
        channelCol = Color.BLACK;
        wavename = ch1WaveChoiceBox.getValue();
        validwave=check_valid_wave(wavename,ch1Amp,ch1Offset,ch1Freq);

        if(validwave){
          draw_canvas(channelCol,wavename,(int)Double.parseDouble(ch1Amp) ,(int)Double.parseDouble(ch1Offset),
                  (int)Double.parseDouble(ch1Freq));
          break;
        }else{break;}
      case "2":
        //Channel 1
        //Color: Green
        channelCol = Color.GREEN;
        wavename = ch2WaveChoiceBox.getValue();
        validwave=check_valid_wave(wavename,ch2Amp,ch2Offset,ch2Freq);
        if(validwave){
          draw_canvas(channelCol,wavename,(int)Double.parseDouble(ch2Amp) ,(int)Double.parseDouble(ch2Offset),
                  (int)Double.parseDouble(ch2Freq));
          break;
        }else{break;}
      default: break;
    }
  }



  private void draw_canvas(Color color, String wavename, int amp, int offset,
                           int frequency) {
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
      case "Reverse Sawtooth":
        draw_reverse_sawtooth_wave(color, amp, frequency, offset);
        break;
      default:
        break;
    }
  }


  private boolean check_offset(String offsetStr){
    double offset;
    try{
      offset = Double.parseDouble(offsetStr);
      return (offset>=-3.0)&&(offset<=3.0);
    }catch (NumberFormatException|NullPointerException  ne){
      //Could show the user to enter something as a dialog window
      //For now do nothing
      Notification offsetRead = new Notification("Offset reading error!");
      offsetRead.message="Choose a value between -3 to 3.";
      offsetRead.showAlert("WARNING");
    }
    return false;
  }

  private boolean check_amp(String ampStr){
    double amp;
    try{
      amp = Double.parseDouble(ampStr);
      return (amp>=0.0)&&(amp<=6.0);
    }catch (NumberFormatException|NullPointerException ne){
      //Could show the user to enter something as a dialog window
      //For now do nothing
      Notification ampRead = new Notification("Amplitude reading error!");
      ampRead.message="Choose a value between 0 to 6.";
      ampRead.showAlert("WARNING");
    }
    return false;
  }

  private boolean check_frequency(String freqStr){
    int freq;
    try{
      freq = Integer.parseInt(freqStr);
      return (freq>=0)&&(freq<=10000); //freq. range: 0-10KHz
    }catch (NumberFormatException|NullPointerException ne){
      //Could show the user to enter something as a dialog window
      //For now do nothing
      Notification ampRead = new Notification("Frequency reading error!");
      ampRead.message="Choose a value between 0 to 10KHz";
      ampRead.showAlert("WARNING");
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

  private int period_calculator(int frequency) {
    //1500 is good for 20KHz
    //600 is good for 10KHz
    //*300 is good for 2000hz
    //*200 is good for 1000hz
    //*10 is good for 1 hz (with /24)
    //*500 is good for 10hz
    int p = 0;
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

  private void draw_reverse_sawtooth_wave(Color color, int amp, int frequency, int offset){
    //Fourier series
    gc.setStroke(color);
    double middleHalf = canvas.getHeight() / 2;
    double period = 5.9;
    if(amp!=0)amp=50;

    double xprev = 0.0;
    for (double t = 0.0; t <= canvas.getWidth(); t += period) {
      double x = 0.0;
      for (int k = 1; k <= 1000; k++) {
        x += Math.sin(k*t) / k;
      }
      x = x * 2 / Math.PI;
      gc.strokeLine(t-period, xprev+middleHalf, t, middleHalf+x*amp);
      xprev = x;
    }
    gc.setLineWidth(1);

  }

  private void draw_sinewave(Color color, int amp, int frequency, int offset) {
    double a = 0.0;
    gc.setStroke(color);
    double middleHalf = canvas.getHeight() / 2;
    double inc = (Math.PI * 2) / 24;//old:24
    double points = period_calculator(frequency);
    double period = (1 / (double) frequency) * points;

    if(amp!=0) amp = 50;
    for (double i = 0; i < canvas.getWidth(); i = i + period) {
      gc.strokeLine(i, middleHalf, i, middleHalf + Math.sin(a) * amp);
      a += inc;
    }
    gc.setLineWidth(1);
  }

  private void draw_square_wave(Color color, int amp, int frequency, int offset) {
    gc.setStroke(color);
    double a = 0.0;
    double middleHalf = canvas.getHeight() / 2;
    double inc = (Math.PI * 2) / 24;//old:24
    double p = (1 / (double) frequency) * period_calculator(frequency);
    if(amp!=0) amp = 50;

    for (double i = 0; i < canvas.getWidth(); i = i + p) {
      gc.strokeLine(i, middleHalf, i, middleHalf + Math.signum(Math.sin(a)) * amp);//Working
      a += inc;
    }
    gc.setLineWidth(1);

  }

  private void draw_sawtooth_wave(Color color, int amp, int frequency, int offset) {
    gc.setStroke(color);
    double middleHalf = canvas.getHeight() / 2;
    double period = 5.9;//6 seems to fit but 5.9 seems to fit better
    if(amp!=0)amp=50;

    //USING FOURIER SERIES
    double xprev = 0.0;
    for (double t = 0.0; t <= canvas.getWidth(); t += period) {
      double x = 0.0;
      for (int k = 1; k <= 1000; k++) {//n=1000
        x += Math.sin(k*t) / k;
      }
      x = x*(0.5 - (2/ Math.PI));
      gc.strokeLine(t-period, xprev+middleHalf, t, middleHalf+x*4*amp);//Four is for increasing the amplitude
      xprev = x;
    }
    gc.setLineWidth(1);

  }

  private void draw_triangle_wave(Color color, int amp, int frequency, int offset) {
    gc.setStroke(color);
    double middleHalf = canvas.getHeight() / 2;
    double period = 5.9;
    if(amp!=0)amp=50;

    double xprev = 0.0;
    for (double t = 0.0; t <= canvas.getWidth(); t += period) {
      double x = 0.0;
      for (int k = 1; k <= 1000; k++) {
        x +=(-1/Math.pow((double) (2*k-1),2))* Math.cos((2*k-1)*t);
      }
      x =x* (0.5+1/Math.pow(Math.PI,2));
      gc.strokeLine(t-period, xprev+middleHalf, t, middleHalf+x*amp);
      xprev = x;
    }
    gc.setLineWidth(1);

  }

  private String nbit_to_binary(String s, int n){
    String ret="";
    StringBuilder sb = new StringBuilder("");
    for(int i =0; i<n;i++){
      sb.append("0");
    }
    String nbitZeroes = sb.toString();
    if(s.isEmpty()){
      return ret;
    }else{
      String bin = Integer.toBinaryString(Integer.valueOf(s));
      int length = bin.length();
      if (length <= n){
        ret=nbitZeroes.substring(0, n-length).concat(bin);
      }else{
        //String binary conversion is higher nibbles than n
      }
    }
    return ret;
  }


  String serial_wave_send_format(String channel, int wave, int amp, int offset, int frequency){
    StringBuilder sb = new StringBuilder("");
    int newOffset = offset+300;

    switch (channel){
      case "ch1":
        //channel 2 bits
        sb.append("10");
        //Wave in binnary 3 bits
        sb.append(nbit_to_binary(String.valueOf(wave),3));
        //Amp in binary 9 bits between 0-300 (0v-3v)
        sb.append(nbit_to_binary(String.valueOf(amp),9));
        //Offset in binary 11 bits between 0-600 (-3v-3v)
        sb.append(nbit_to_binary(String.valueOf(newOffset),11));
        //frequency in binary 16 bits
        sb.append(nbit_to_binary(String.valueOf(frequency),16));
        break;
      case "ch2":
        //channel
        sb.append("01");
        //Wave in binnary 3 bits
        sb.append(nbit_to_binary(String.valueOf(wave),3));
        //Amp in binary 9 bits between 0-300 (0v-3v)
        sb.append(nbit_to_binary(String.valueOf(amp),9));
        //Offset in binary 11 bits between 0-600 (-3v-3v)
        sb.append(nbit_to_binary(String.valueOf(newOffset),11));
        //frequency in binary 16 bits
        sb.append(nbit_to_binary(String.valueOf(frequency),16));
        break;
    }

    return sb.toString();
  }

  private void update_temp() {
    //try check for temperature change.
    //If not connected, show '0'
    serialCo.add_data_listener(serialCo.get_COMM_port(),serialCo.get_serial_port_listener("read"));
  }

  private void default_states(){

    //Initial view
    ch1FreqInput.disableProperty().setValue(true);
    ch2FreqInput.disableProperty().setValue(true);
    ch1AmpInput.disableProperty().setValue(true);
    ch2AmpInput.disableProperty().setValue(true);
    ch1OffsetInput.disableProperty().setValue(true);
    ch2OffsetInput.disableProperty().setValue(true);
    //default values
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
    tempDisplay.setPromptText("°C");
    tempDisplay.setEditable(false);
    //Canvas defaults
    gc = canvas.getGraphicsContext2D();
    canvas.setStyle("-fx-border-color: blue;");
    draw_empty_canvas();
  }

  private void GUI_CSS(){
    channel1Button.setStyle("-fx-text-fill: black;"+
            "-fx-font-weight: bold;"+
            "-fx-border-color: black;");
    channel2Button.setStyle("-fx-text-fill: green;"+
            "-fx-font-weight: bold;"+
            "-fx-border-color: black;");
    resetButton.setStyle("-fx-text-fill: white;"+
            "-fx-background-color: red;"+
            "-fx-font-weight: bold;"+
            "-fx-border-color: black;");
    ch1Label.setTextFill(Color.BLACK);
    ch1Label.setStyle("-fx-font-size: 12.0;" +
            "-fx-font-weight: bold;"+
            "-fx-border-color: black;");
    ch2Label.setTextFill(Color.GREEN);
    ch2Label.setStyle("-fx-font-size: 12.0;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: black;");
  }


  @FXML
  void initialize() {
    default_states();
    //Initial input states
    ch1FreqInput.clear();
    ch2FreqInput.clear();
    ch1AmpInput.clear();
    ch2AmpInput.clear();
    ch1OffsetInput.clear();
    ch2OffsetInput.clear();
    resetButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12.0));
    sendButton.setFont(Font.font("Verdana", FontWeight.BOLD, 12.0));
    //GUI CSS
    GUI_CSS();

    //Add the listener parts from the inputs
    ch1AmpInput.textProperty().addListener((observable, oldValue, newValue) ->readCh1AmpInput(newValue));
    ch1OffsetInput.textProperty().addListener((observable, oldValue, newValue) ->readCh1OffsetInput(newValue));
    ch1FreqInput.textProperty().addListener((observable, oldValue, newValue)->readCh1FreqInput(newValue));

    ch2AmpInput.textProperty().addListener((observable, oldValue, newValue) ->readCh2AmpInput(newValue));
    ch2OffsetInput.textProperty().addListener((observable, oldValue, newValue) ->readCh2OffsetInput(newValue));
    ch2FreqInput.textProperty().addListener((observable, oldValue, newValue)->readCh2FreqInput(newValue));

    dataSend ="";
    ///////////////////////////
    ///Worker thread for checking connection
    Notification serialNotif = new Notification("Serial Communication");
    Runnable checkMCU = () -> {
      System.out.println("Reading Thread started..\n Thread name: "+Thread.currentThread().getName());
      try {
        while(true){
          if(!serialCo.check_connection()){
            serialNotif.message = "Cannot communicate with MCU. Use Help>Check connection" +
                    " for more information.";
            serialNotif.showAlert("ERROR");

            serialCo = new SerialController();
            defaultPort = serialCo.default_port();
            if(serialCo.default_port()!=null){
              serialCo.select_port(serialCo.default_port().getSystemPortName());
              serialCo.init();
            }
          }
          Thread.sleep(100);
        }
      } catch (InterruptedException e) {
        System.out.println(Thread.currentThread().getName()+" is interrupted.");
      }
    };
    //checkMCU.run();

    Thread connectionThread = new Thread(checkMCU);

    //////////////////////////
    //Serial parts
    serialCo = new SerialController();
    defaultPort = serialCo.default_port();
    if(serialCo.default_port()!=null){
      serialCo.select_port(serialCo.default_port().getSystemPortName());
      serialCo.init();
      update_temp();

      Platform.runLater(checkMCU);
    }else{
      serialNotif.message = "Cannot communicate with MCU. Use Help>Check connection" +
              " for more information.";
      serialNotif.showAlert("ERROR");
    }

  }

}


/**
 * Notification class for User
 * Handles any display operation or pop up alert
 */
class Notification {
  //Default values of these variable
  String message = ""; //contextText
  String title = ""; //title
  String header = ""; //header
  Alert alert;

  /**
   * Constructor which takes the window title
   *
   * @param title title
   */
  Notification(String title) {
    this.title = title;
  }

  /**
   * Show a pop up alert
   */
  void showAlert(String alertType) {
    alert = new Alert(AlertType.valueOf(alertType));
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(message);
    //Show and wait
    alert.showAndWait();
  }

}