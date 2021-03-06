import javafx.util.Pair;

import java.io.RandomAccessFile;
import java.net.*;
/**
 * The DNS_UDP_Server implements a UDP server for simple DNS program.
 * Usage: open the server first, then the client and type in the Domain name. A request will be sent to the server
 * and the server will reply with IP of the domain. Sending zero as response will terminate the connection of the client.
 *
 * @author Abdulrahman Zaiter
 * @version 1.0
 * @since 2017-09-04
 */
public class DNS_UDP_Server {
    public static void main(String[] args) {
        System.out.printf("Domain Name Finder - UDP Server\n");
        // Initialize UDP socket for sending/receiving information from/to client
        try (DatagramSocket srvSocket = new DatagramSocket(DNS_Utils.UDP_SERVER_PORT)) {
            System.out.printf("Server is Listening on Port %d.\n\n", DNS_Utils.UDP_SERVER_PORT);
            // initialize RAF to store requests count.
            RandomAccessFile randomAccessFile = new RandomAccessFile(DNS_Utils.UDP_RAF_FILENAME, "rw");
            DNS_Utils.setClientCount(randomAccessFile, 0, 0);
            // keep serving clients until program terminations.
            while (true) {
                try {
                    // save client request into request string and UDP packet separately.
                    Pair<String, DatagramPacket> clientMsg = DNS_Utils.receiveUDPRequest(srvSocket, 1024);
                    String domainName = clientMsg.getKey();
                    DatagramPacket clientPacket = clientMsg.getValue();
                    // update the RAF
                    DNS_Utils.setClientCount(randomAccessFile, DNS_Utils.getClientCount(randomAccessFile, 0) + 1, 0);
                    System.out.printf("Client Connected\n");
                    // serve the client in a separate thread.
                    new Thread(()->{
                        try {
                            // keep serving simultaneously, terminating their threads when they send a command of "0"
                            if(!domainName.equals("0")) {
                                // compute ip address to send
                                System.out.printf("Recieved: %s\n", domainName);
                                String ipAddressToSend = InetAddress.getByName(domainName).getHostAddress()
                                        .concat(". Requests Served : ")
                                        .concat(Integer.toString(DNS_Utils.getClientCount(randomAccessFile, 0)));
                                System.out.printf("Sending %s ip: %s to %s.\n", domainName, ipAddressToSend,
                                        clientPacket.getAddress().getHostAddress());
                                // send ip address to client
                                DNS_Utils.sendUDPRequest(ipAddressToSend, clientPacket.getAddress().getHostAddress(), DNS_Utils.UDP_CLIENT_PORT, srvSocket);
                            }
                        }
                        catch (UnknownHostException e){
                            System.out.println("ERROR: Unknown host.");
                            // send ip address to client
                            DNS_Utils.sendUDPRequest("UNKNOWN".concat(". Requests Served : ")
                                    .concat(Integer.toString(DNS_Utils.getClientCount(randomAccessFile, 0)))
                                    , clientPacket.getAddress().getHostAddress(), DNS_Utils.UDP_CLIENT_PORT, srvSocket);
                        }
                        catch (Exception e){
                            System.out.println("ERROR: Can't Serve the Client!");
                        }
                    }).start();
                    } catch (Exception e) {
                        System.out.println("ERROR: Can't get client request.");
                        DNS_Utils.setClientCount(randomAccessFile, DNS_Utils.getClientCount(randomAccessFile, 0) - 1, 0);
                    }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Can't initialize the UDP socket!");
        }
    }
}
