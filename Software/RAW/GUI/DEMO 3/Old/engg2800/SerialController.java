package engg2800;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Application;

import java.io.*;
import java.util.Scanner;

/**
 * Serial Controller for Using USART and I2C
 */
public class SerialController{

  private static final int BAUD_RATE = 9600;
  private static final int DATA_BIT = 8;
  private static final int STOP_BIT = 1;
  private static final int PARITY_BIT = 0;

  private static SerialPort commPort;//default port
  private static SerialPort lastPort = null;//last port accessed
  private static InputStream commPortIn = null; //Input Stream
  private static OutputStream commPortOut = null; //Output Stream
  private static SerialPort commPorts[] = SerialPort.getCommPorts(); //Lists of CPU Ports

  byte[] dataSent = new byte[1024];
  byte[] dataRead = new byte[1024];

  /**
   * DEBUG: Showing available ports
   */
  public static void print_available_ports(){
    System.out.println("The following COM ports are found:");
    for (int i = 0; i <commPorts.length ; i++) {
      int index =i+1;
      System.out.println(index+": "+ commPorts[i].getSystemPortName());
    }
  }

  /**
   * See the lists of available ports in CPU
   * @return string containing ports name
   */
  public static String available_ports(){
    StringBuilder ports = new StringBuilder("");
    if(commPorts.length<=0){
      return "null";
    }else{
      for (int i = 0; i <commPorts.length ; i++) {
        int index =i+1;
        ports.append(commPorts[i].getSystemPortName()+" ");
      }
    }
    return ports.toString();
  }

  public SerialPort get_COMM_port(){
    return commPort;
  }
  public SerialPort get_last_COMM_port(){
    if(commPort!=null && lastPort==null){
      lastPort = commPort;
    }
    return lastPort;
  }

  /**
   * Finds an open port for communication, if not found returns null
   * @return Serial port if found else null
   */
  public SerialPort default_port(){
    SerialPort defaultPort = null;
    if(commPorts.length<=0){
      return defaultPort;
    }else{
      for (int i = 0; i <commPorts.length ; i++) {
        start_connection(commPorts[i]);
        if(commPorts[i].isOpen()){
          defaultPort=commPorts[i];
          defaultPort.closePort();
          break;
        }
        commPorts[i].closePort();
      }
    }
    return defaultPort;
  }

  /**
   * Try to select serial port from their system-name
   * @param desc system name of COM port
   * @return connection status
   */

  /**
   * Try to select serial port from their system-name
   * @param desc system name of COM port
   * @return connection status
   * @throws NullPointerException if the Port does not exist
   */
  public boolean select_port(String desc) throws NullPointerException{
    commPort = SerialPort.getCommPort(desc);
    start_connection(commPort);
    //For debugging
    //todo:delete these debug codes
    if(!commPort.isOpen()){
      System.out.println("Serial error: Could not initialize connection to "+desc);
    }else{
      System.out.println("Serial: connected to "+desc);
    }
    //
    return commPort.isOpen();
  }

  private void start_connection(SerialPort port){
    port.openPort();
  }

  public boolean check_connection(){
    return commPort!=null && commPort.isOpen();
  }

  public boolean MCU_connected(){
    return commPort.isOpen();
  }

  public void close_connection(){
    if(commPort.isOpen()){
      commPort.closePort();
    }
    try {
      commPortIn.close();
      commPortOut.close();
    } catch (IOException err) {
      System.out.println("Error detected:"+err.getLocalizedMessage());
    }
  }

  public void init(){
    if(check_connection()){
      commPort.setComPortParameters(BAUD_RATE,DATA_BIT,STOP_BIT,PARITY_BIT);
      commPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
      //commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
      commPortIn = commPort.getInputStream();
      commPortOut = commPort.getOutputStream();
      lastPort = commPort;
    }else{
      System.out.println("You have not selected a serial port!!");
    }
  }

  /**
   * Try to read data from the serial
   * Clears any left-over data
   * @param n No. of bytes to be read
   * @return no. of bytes read
   */
  public byte[] read_data_n(int n){//Number of char to read
    byte[] readBuffer = new byte[n];
    int bytesAvailable = commPort.bytesAvailable();
    if(bytesAvailable==readBuffer.length){
      int bytesRead = commPort.readBytes(readBuffer, readBuffer.length);
      return (bytesRead>0)?readBuffer:null;
    }else{
      System.out.println("Less bytes are available than expected!");
    }
    return null;
  }

  /**
   * Try to read data from the serial
   * Clears any left-over data
   * @return ascii char decimal, -1 otherwise
   */
  public char read_data() throws IOException{
    return (char) new InputStreamReader(commPortIn).read();
  }


  public String read_data_line(){
    BufferedReader br=new BufferedReader(new InputStreamReader(commPortIn));
    try {
      if(br.ready())
        return br.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void write_data(char c, String message){
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(commPortOut));
    try {
      if(c!='\0'){//&& (c!='\r'||c!='\n')
        bw.write(c);
      }
      bw.write(message,0,message.length());
      bw.flush();
      bw.close();
    } catch (IOException e) {
      System.out.println("Data could not be written.");
    }
  }

  public void write_data_line(String message){
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(commPortOut));
    try {
      bw.write(message);
      bw.newLine();
      bw.flush();
      bw.close();
    } catch (IOException e) {
      System.out.println("Data could not be written.");

    }
  }

  public void add_data_listener(SerialPort port, SerialPortDataListener spdl){
    port.addDataListener(spdl);
  }

  /**
   *
   * @param type
   * @return
   */
  public SerialPortDataListener get_serial_port_listener(String type){
    SerialPortDataListener SPDLRead = new SerialPortDataListener() {
      @Override
      public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
      }

      @Override
      public void serialEvent(SerialPortEvent serialPortEvent) {
        serial_event(serialPortEvent);
      }
    };

    SerialPortDataListener SPDLReceived = new SerialPortDataListener() {
      @Override
      public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
      }

      @Override
      public void serialEvent(SerialPortEvent serialPortEvent) {
        serial_event(serialPortEvent);
      }
    };


    SerialPortDataListener SPDLWrite = new SerialPortDataListener() {
      @Override
      public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_WRITTEN;
      }

      @Override
      public void serialEvent(SerialPortEvent serialPortEvent) {
        serial_event(serialPortEvent);
      }
    };

    switch (type){
      case "read":
        return SPDLRead;
      case "write":
        return SPDLWrite;
      case "receive":
        return SPDLReceived;
      default:
        System.out.println("Choose between \"read\",\"write\" or \"received\"");
    }

    return null;
  }

  public void remove_data_listeners(){
    if(commPort!=null){
      commPort.removeDataListener();
    }
  }



  public void loopback(){
    //assumes that correctly configured/ init method is called
    if(check_connection()){//check connection
      InputStream in = commPortIn;
      try{
        this.write_data_line("......LoopBack.....");
        this.write_data('>',"");
        while(MCU_connected()){
          if(in.available()>0){
            if((char)in.read()!='\r'){
              char cRead =(char)in.read();
              System.out.print(cRead);
              write_data(cRead,"");
            }else{
              System.out.println();
              //DISPLAY TO MCU
              this.write_data('\0',System.lineSeparator());
              this.write_data('>',"");
            }
          }
          in.close();
        }
        if(!MCU_connected())
          System.out.println("Disconnected!!");
        in.close();
      }catch (Exception e){System.out.println("Loopback failed due to "+e.getLocalizedMessage());}

    }else{
      System.out.println("Loopback failed :(");
    }
  }



  //Asynchronous model of data transfer
  public void serial_event(SerialPortEvent event) {
    switch(event.getEventType()) {
      case SerialPort.FLOW_CONTROL_DISABLED:
        break;
      case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
        //byte[] readBuffer = new byte[1024];
        if(this.check_connection()){
//          Scanner scanner = new Scanner(commPortIn);
//          while(scanner.hasNext()){
//            System.out.print(scanner.next());
//          }
//
//          scanner.close();
          try {
            //System.out.println("\ncommPort.bytesAvailable()->"+commPort.bytesAvailable());
            byte[] readBytes = new byte[commPort.bytesAvailable()];
            int readAmount = commPortIn.read(readBytes);
            //System.out.println("readAmount:"+readAmount);
            for (int i = 0; i <readAmount ; i++) {
              char readChar = (char) readBytes[i];
              System.out.print(readChar);
            }
          } catch (IOException|NullPointerException e) {
            System.out.println(e.getLocalizedMessage());
          }
        }//End of check connection

        break;
      case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
        dataRead = event.getReceivedData();
        System.out.println("Received data of size: " + dataRead.length);
        for(byte b:dataRead){
          System.out.print((char)b);
        }
        System.out.println();
        break;
      case SerialPort.LISTENING_EVENT_DATA_WRITTEN:
        System.out.println("All bytes were successfully transmitted!");
        break;
    }
  }


  public static void mainCode(){
    //PUTS all the code to main function
    System.out.println("Running Serial Controller Main function::");
    SerialController sc = new SerialController();
    sc.select_port(sc.default_port().getSystemPortName());//sc.default_port().getSystemPortName()
    sc.init();

    //COMMENTED
    sc.get_COMM_port().addDataListener(sc.get_serial_port_listener("read"));
    sc.write_data_line("hello");
    sc.write_data_line("hello");

    System.out.println("..........................");
    if(!sc.check_connection()){
      sc.remove_data_listeners();
      sc.get_COMM_port().closePort();
    }

  }


  public static void main(String args[]) {
    //Testing
    if(available_ports().compareTo("null")!=0){
      print_available_ports();
      mainCode();
    }else{
      System.out.println("Nothing is found (COM ports)");
    }
  }
}
