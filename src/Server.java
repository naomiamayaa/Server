import javax.xml.crypto.Data;
import java.net.*;
import java.util.Arrays;

public class Server implements Runnable{
    DatagramPacket in, out, request, confirmation;
    private DatagramSocket sendReceiveSocket;
    private static final int SERVER_PORT = 69;
    private DatagramParser parse = new DatagramParser();
    public Server() {
        try {
            this.sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }

    }

    private byte[] returnConfirmationCode(int type) {
        byte[] opcode = new byte[4];
        opcode[0] = 0;
        if (type == 1) {
            opcode[1] = 3;
            opcode[2] = 0;
            opcode[3] = 1;
        } else {
            if (type != 2) {
                throw new RuntimeException("Invalid packet received");
            }
            opcode[1] = 4;
            opcode[2] = 0;
            opcode[3] = 0;
        }

        return opcode;
    }

    public void sendRequest() throws UnknownHostException {
        byte[] requestData = new byte[4];
        byte[] receiveData = new byte[100];
        byte[] confirmData = new byte[0];

        requestData[0] = 0;
        requestData[1] = 1;
        requestData[2] = 0;
        requestData[3] = 1;

        this.request = new DatagramPacket(requestData, requestData.length, InetAddress.getLocalHost(), SERVER_PORT);
        this.in = new DatagramPacket(receiveData, receiveData.length);

        rpc_send(request, in);
    }

    public void processRequest() {
        byte[] receiveData = new byte[100];

        byte[] sendData = returnConfirmationCode(parse.getOpcode(in));
        this.confirmation = new DatagramPacket(sendData, sendData.length, in.getAddress(), in.getPort());
        in = new DatagramPacket(receiveData, receiveData.length);

        rpc_send(confirmation, in);
    }

    public void rpc_send(DatagramPacket out, DatagramPacket in) {
        try {
            System.out.println("\nSending a " + requestOrResponse(out) + " packet to host: ");
            this.sendReceiveSocket.send(out);
            System.out.println("Packet sent to host: ");

            for(int i = 0; i < 4; ++i) {
                System.out.print(String.format("%02X ", out.getData()[i]));
            }

            System.out.println("\n\nWaiting for packet from host...");
            this.sendReceiveSocket.receive(in);
            System.out.println("\nPacket received from host: ");
            parse.parseRequest(in);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String requestOrResponse(DatagramPacket packet){

        byte[] request = new byte[4];

        request[0] = 0;
        request[1] = 1;
        request[2] = 0;
        request[3] = 1;

        if (Arrays.equals(packet.getData(), request)) {
            return "REQUEST";
        }
        return "RESPONSE";
    }

    @Override
    public void run() {
        try {
            for(int i = 0; i < 5; i++){
                System.out.println("\n\nIteration: \u001b[32m\t" + i + "\u001b[0m");
                sendRequest();
                processRequest();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        Thread t = new Thread(server);
        t.start();
    }
}
