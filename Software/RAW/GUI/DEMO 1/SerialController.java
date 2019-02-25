package sample;

import java.io.*;
import com.fazecast.jSerialComm.*;


public class SerialController{
  private static SerialPort commPort;
  private static InputStream dataInStream = null;
  private static OutputStream dataOutStream = null;
  private static SerialPort commPorts[] = SerialPort.getCommPorts();

  byte[] dataSent = new byte[1024];
  byte[] dataRead = new byte[1024];

  
  public static void see_available_ports(){
    System.out.println("The following COM ports are found:");
    for (int i = 0; i <commPorts.length ; i++) {
      int index =i+1;
      System.out.println(index+": "+ commPorts[i].getSystemPortName());
    }
  }


  public void select_port(String desc){
    commPort = SerialPort.getCommPort(desc);
    start_connection(commPort);
    if(!commPort.isOpen()){
      System.out.println("Serial error: Could not initialize connection to "+desc);
    }else{
      System.out.println("Serial: connected to "+desc);
    }
  }
  private void start_connection(SerialPort port){
    port.openPort();
  }

  public boolean check_connection(){
    return commPort.isOpen();
  }

  public void close_connection(){
    if(commPort.isOpen()){
      commPort.closePort();
    }
    try {
      dataInStream.close();
      dataOutStream.close();
    } catch (IOException err) {
      System.out.println("Error detected:"+err.getLocalizedMessage());
    }
  }

  public void init(){
    if(check_connection()){
      commPort.setBaudRate(9600);
      dataInStream = commPort.getInputStream();
      dataOutStream = commPort.getOutputStream();
    }else{
      System.out.println("You have not selected a serial port!!");
    }
  }

  public void read_data_n(int n){//Number of char to read
    ///////////////////////////////////////
    if(commPort.bytesAvailable()>0){
      for (int i = 0; i < n ; i++) {
        try {
          System.out.println((char)dataInStream.read());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }else{
      System.out.println("Could not find anything to read.");
    }

  }

  public void read_data(){//Number of char to read
    ///////////////////////////////////////
    if(commPort.bytesAvailable()>0){
      commPort.readBytes(dataRead,dataRead.length);
      //int    bytesRead = dataInStream.read(dataRead);
      int bytesRead = dataSent.length;
    }else{
      System.out.println("Could not find anything to read.");
    }
  }

  public void read_data_line(){
    BufferedReader br = new BufferedReader(new InputStreamReader(dataInStream));
    try {
      String line = br.readLine();
      if(line!=null){
        System.out.println(br.readLine());
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void write_data_line(String message){
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dataOutStream));
    try {
      bw.write(message);
      bw.newLine();
      bw.close();
    } catch (IOException e) {
      System.out.println("Data could not be written.");
    }
  }

  public static void main(String args[]) {
    //Testing

    /*see_available_ports();
    SerialController sc = new SerialController();
    sc.select_port("COM4");
    sc.init();
    System.out.println(sc.check_connection());*/

  }



}
