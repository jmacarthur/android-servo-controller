import java.io.*;
import java.net.*;

// Sends a datagram containing left and right servo information to a device
// listening on a known IP Address with the fixed port 6502.

// Usage: java Controller <ip addr> <left> <right> <duration>
// eg:    java Controller 1.2.3.4 80 80 5

class Controller
{
  public static void main(String[] args) throws SocketException, IOException
  {
    String addr = args[0];
    int left = Integer.parseInt(args[1]);
    int right = Integer.parseInt(args[2]);
    int duration = Integer.parseInt(args[3]);
    DatagramSocket clientSocket = new DatagramSocket();
    InetAddress IPAddress = null;
    try {
      IPAddress = InetAddress.getByName(addr);
    }
    catch(UnknownHostException e) {
      System.out.println("Unknown host "+addr);
      return;
    }
    
    System.out.println("Connecting to "+addr+" at "+IPAddress.toString());
    byte[] sendData = new byte[3];
    sendData[0] = (byte) left;
    sendData[1] = (byte) right;
    sendData[2] = (byte) duration;
    
    DatagramPacket sendPacket = new DatagramPacket(sendData, 3, IPAddress, 6502);
    clientSocket.send(sendPacket);
  }
}
