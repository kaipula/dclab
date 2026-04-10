import java.net.*;
import java.util.*;

public class TokenRing {

    static int MAX_NODES;
    static final int BASE_PORT = 10000;

    static int myId;
    static int nextNodeId;

    static boolean hasToken = false;
    static boolean wantsCS = false;
    static boolean isInCS = false;

    // Listener Thread
    static class Listener extends Thread {
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(BASE_PORT + myId);

                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    synchronized (TokenRing.class) {
                        hasToken = true;
                        System.out.println("\nNode " + myId + ": *** RECEIVED TOKEN ***");
                    }

                    // If not requesting CS, pass token
                    if (!wantsCS) {
                        Thread.sleep(500); // simulate delay
                        sendToken();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Send token to next node
    static void sendToken() throws Exception {
        DatagramSocket socket = new DatagramSocket();

        byte[] data = "TOKEN".getBytes();
        InetAddress address = InetAddress.getByName("127.0.0.1");

        DatagramPacket packet = new DatagramPacket(
                data, data.length, address, BASE_PORT + nextNodeId
        );

        socket.send(packet);
        socket.close();

        synchronized (TokenRing.class) {
            hasToken = false;
        }

        System.out.println("Node " + myId + ": Passed token to Node " + nextNodeId);
    }

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter total number of nodes: ");
        MAX_NODES = sc.nextInt();

        System.out.print("Enter your node ID (0 to " + (MAX_NODES - 1) + "): ");
        myId = sc.nextInt();

        nextNodeId = (myId + 1) % MAX_NODES;

        System.out.print("Do you start with token? (1=yes, 0=no): ");
        hasToken = sc.nextInt() == 1;

        sc.nextLine(); // clear buffer

        // Start listener
        Listener listener = new Listener();
        listener.start();

        while (true) {
            System.out.println("Node " + myId +
                    ": Press ENTER to REQUEST Task (Token: " + (hasToken ? "YES" : "NO") + ")");
            sc.nextLine();

            wantsCS = true;

            // Wait for token
            while (!hasToken) {
                Thread.sleep(10);
            }

            isInCS = true;
            System.out.println("--- Node " + myId + ": EXECUTING TASK (CS) ---");

            Thread.sleep(3000);

            System.out.println("--- Node " + myId + ": TASK FINISHED ---");

            isInCS = false;
            wantsCS = false;

            sendToken();
        }
    }
}