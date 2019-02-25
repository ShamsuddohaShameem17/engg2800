package engg2800;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.*;

/** Serial Controller class */
public class SerialController {

  //Serial data format
  private static final int BAUD_RATE = 9600;
  private static final int DATA_BIT = 8;
  private static final int STOP_BIT = 1;
  private static final int PARITY_BIT = 0;

  private static SerialPort commPort = null; // default port
  private static SerialPort lastPort = null; // last port accessed
  private static InputStream commPortIn = null; // Input Stream
  private static OutputStream commPortOut = null; // Output Stream
  private static SerialPort commPorts[] = SerialPort.getCommPorts(); // Lists of CPU Ports

  /** DEBUG: Showing available ports */
  public static void print_available_ports() {
    System.out.println("The following COM ports are found:");
    for (int i = 0; i < commPorts.length; i++) {
      int index = i + 1;
      System.out.println(index + ": " + commPorts[i].getSystemPortName());
    }
  }

  /**
   * See the lists of available ports in CPU
   *
   * @return string containing ports name
   */
  public static String available_ports() {
    StringBuilder ports = new StringBuilder();
    //Refresh the commPorts again
    commPorts = SerialPort.getCommPorts();
    if (commPorts.length <= 0) {
      return "null";
    } else {
      for (int i = 0; i < commPorts.length; i++) {
        int index = i + 1;
        ports.append(commPorts[i].getSystemPortName() + " ");
      }
    }
    return ports.toString();
  }

  /**
   * Returns the current comm port
   * @return
   */
  public SerialPort get_COMM_port() {
    return commPort;
  }

  /**
   * Returns the last comm port used
   * @return
   */
  public SerialPort get_last_COMM_port() {
    if (commPort != null && lastPort == null) {
      lastPort = commPort;
    }
    return lastPort;
  }

  /**
   * Finds an open port for communication, if not found returns null
   *
   * @return Serial port if found else null
   */
  public SerialPort default_port() {
    SerialPort defaultPort = null;
    String UARTDev="UMFT234XF";
    //Refresh the ports
    commPorts=SerialPort.getCommPorts();
    if (commPorts.length <= 0) {
      return defaultPort;
    } else {
      for (int i = 0; i < commPorts.length; i++) {
        if(commPorts[i].toString().compareTo(UARTDev)==0){
          start_connection(commPorts[i]);
          if (commPorts[i].isOpen()) {
            defaultPort = commPorts[i];
            defaultPort.closePort();
            break;
          }

        }
      }
    }
    return defaultPort;
  }

  /**
   * Try to select serial port from their system-name
   *
   * @param desc system name of COM port
   * @return connection status
   * @throws NullPointerException if the Port does not exist or null
   */
  public boolean select_port(String desc) throws NullPointerException {
    commPort = SerialPort.getCommPort(desc);
    start_connection(commPort);
    this.init();
    // For debugging
    // todo:delete these debug codes
    if (!commPort.isOpen()) {
      System.out.println("Serial error: Could not initialize connection to " + desc);
      //this.close_connection();
    } else {
      System.out.println("Serial: connected to " + desc);
    }
    //
    return commPort.isOpen();
  }

  /**
   * Start the Serial connection with given port
   * @param port communicating port
   */
  private void start_connection(SerialPort port) {
    port.openPort();
  }

  /**
   * Check connection of current serial port if valid
   * @return true if connected, false otherwise
   */
  public boolean check_connection() {
    if(commPort != null){
      return commPort.isOpen();
    }else{
      return false;
    }
  }

  /**
   * Check current serial port can communicate with MCU
   * @return true if open, false otherwise
   * @throws NullPointerException port is found / NULL
   */
  public boolean MCU_connected() throws NullPointerException {
    return commPort.isOpen();
  }

  /**
   * Close the serial connection
   */
  static void close_connection() {
    try {
      if(commPort!=null){
        if (commPort.isOpen()) {
          commPort.closePort();
        }
        commPortIn.close();
        commPortOut.close();
        commPort.removeDataListener();
      }
    } catch (IOException err) {
      System.out.println("Error detected:" + err.getLocalizedMessage());
    }
  }

  /**
   * Initialise the serial port with selected parameter.
   */
  void init() {
    if (check_connection()) {
      commPort.setComPortParameters(BAUD_RATE, DATA_BIT, STOP_BIT, PARITY_BIT);
      commPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
      commPortIn = commPort.getInputStream();
      commPortOut = commPort.getOutputStream();
      lastPort = commPort;
    } else {
      System.out.println("You have not selected a serial port!!");
    }
  }

  /**
   * Write to the Serial
   * @param c character
   * @param message String
   */
  void write_data(char c, String message) {
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(commPortOut));
    try {
      if (c != '\0') {
        bw.write(c);
      }
      bw.write(message, 0, message.length());
      bw.flush();
      bw.close();
    } catch (IOException e) {
      System.out.println("Data could not be written.");
    }
  }

  /**
   * Write to the Serial with line Separator
   * @param message String
   */
  void write_data_line(String message) {
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

  /**
   * Adds port-data-listener to the port
   * @param port port to set
   * @param spdl data-listener to set
   */
  void add_data_listener(SerialPort port, SerialPortDataListener spdl) {
    port.addDataListener(spdl);
  }

  /**
   * Creates data listener for serial port event
   * @param type read/write
   * @return data listener
   */
  SerialPortDataListener get_serial_port_listener(String type) {
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

    switch (type) {
      case "read":
        return SPDLRead;
      case "write":
        return SPDLWrite;
      case "receive":
        return SPDLReceived;
      default:
        System.out.println("Choose between \"read\",\"write\" or \"received\".");
    }
    return null;
  }

  /**
   * Removes all data listeners active on the port
   */
  public void remove_data_listeners() {
    if (commPort != null) {
      commPort.removeDataListener();
    }
  }

  //////////////////////////////////////
  // Asynchronous model of data transfer
  ///////////////////////////////////////
  /**
   * Performs task on specific serial event when data arrives
   * @param event Serial event
   */
  public void serial_event(SerialPortEvent event) {
    switch (event.getEventType()) {
      case SerialPort.FLOW_CONTROL_DISABLED:
        break;
      case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
        if (this.check_connection()) {
          try {
            byte[] readBytes = new byte[commPort.bytesAvailable()];
            int readAmount = commPortIn.read(readBytes);
            System.out.print("Reading: ");
            for (int i = 0; i < readAmount; i++) {
              char readChar = (char) readBytes[i];
              System.out.print(readChar);
            }
          } catch (IOException | NullPointerException e) {
            System.out.println(e.getLocalizedMessage());
          }
        } // End of check connection
        break;
      case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
        byte[] dataRead = event.getReceivedData();
        System.out.println("Received data of size: " + dataRead.length);
        for (byte b : dataRead) {
          System.out.print((char) b);
        }
        System.out.println();
        break;
      case SerialPort.LISTENING_EVENT_DATA_WRITTEN:
        System.out.println("All bytes were successfully transmitted!");
        break;
    }
  }
}
