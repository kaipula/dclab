import java.net.*;
import java.util.*;

public class ChandyMisraOR {

    static int MAX_NODES;
    static final int BASE_PORT = 13000;

    static int myId;
    static int[] dependentNodes;
    static int numDependents = 0;

    static int repliesReceived = 0;

    // Message structure
    static class Message {
        int type; // 0 = QUERY, 1 = REPLY
        int initiator, sender, receiver;

        Message(int t, int i, int s, int r) {
            type = t;
            initiator = i;
            sender = s;
            receiver = r;
        }
    }

    // Send message
    static void sendMsg(int type, int initiator, int sender, int receiver) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        String msg = type + "," + initiator + "," + sender + "," + receiver;
        byte[] data = msg.getBytes();

        InetAddress address = InetAddress.getByName("127.0.0.1");

        DatagramPacket packet = new DatagramPacket(
                data, data.length, address, BASE_PORT + receiver
        );

        socket.send(packet);
        socket.close();
    }

    // Listener Thread
    static class Listener extends Thread {
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(BASE_PORT + myId);

                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = msg.split(",");

                    int type = Integer.parseInt(parts[0]);
                    int initiator = Integer.parseInt(parts[1]);
                    int sender = Integer.parseInt(parts[2]);

                    synchronized (ChandyMisraOR.class) {

                        if (type == 0) { // QUERY
                            System.out.println("Node " + myId +
                                    ": Received QUERY from " + sender +
                                    " (Initiator: " + initiator + ")");

                            if (numDependents == 0) {
                                // Not waiting → send REPLY
                                sendMsg(1, initiator, myId, sender);
                            } else {
                                // Forward QUERY
                                for (int i = 0; i < MAX_NODES; i++) {
                                    if (dependentNodes[i] == 1) {
                                        sendMsg(0, initiator, myId, i);
                                    }
                                }
                            }

                        } else if (type == 1) { // REPLY
                            System.out.println("Node " + myId +
                                    ": Received REPLY from " + sender +
                                    " for Initiator " + initiator);

                            if (initiator == myId) {
                                repliesReceived++;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter total number of nodes: ");
        MAX_NODES = sc.nextInt();

        dependentNodes = new int[MAX_NODES];

        System.out.print("Enter your node ID (0 to " + (MAX_NODES - 1) + "): ");
        myId = sc.nextInt();

        // Start listener
        Listener listener = new Listener();
        listener.start();

        while (true) {
            System.out.println("\n--- Node " + myId + " (OR Model) ---");
            System.out.println("1. Add Dependency");
            System.out.println("2. Check Deadlock");
            System.out.println("3. Clear Dependencies");
            System.out.print("Choice: ");

            int choice = sc.nextInt();

            if (choice == 1) {
                System.out.print("Enter Node ID: ");
                int dep = sc.nextInt();

                if (dep >= 0 && dep < MAX_NODES && dep != myId) {
                    dependentNodes[dep] = 1;
                    numDependents++;
                }

            } else if (choice == 2) {

                repliesReceived = 0;

                for (int i = 0; i < MAX_NODES; i++) {
                    if (dependentNodes[i] == 1) {
                        sendMsg(0, myId, myId, i);
                    }
                }

                Thread.sleep(2000); // wait for replies

                if (repliesReceived == 0 && numDependents > 0) {
                    System.out.println("!!! OR-MODEL DEADLOCK DETECTED !!!");
                } else {
                    System.out.println("No deadlock detected (Replies: " + repliesReceived + ")");
                }

            } else if (choice == 3) {
                Arrays.fill(dependentNodes, 0);
                numDependents = 0;
                System.out.println("Dependencies cleared.");
            }
        }
    }
}