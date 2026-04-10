import java.net.*;
import java.util.*;

public class ChandyMisraAND {

    static int MAX_NODES;
    static final int BASE_PORT = 12000;

    static int myId;
    static int[] dependentNodes; // 1 = waiting, 0 = not waiting

    // Probe structure
    static class Probe {
        int initiator, sender, receiver;

        Probe(int i, int s, int r) {
            initiator = i;
            sender = s;
            receiver = r;
        }
    }

    // Send probe
    static void sendProbe(int initiator, int sender, int receiver) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        String msg = initiator + "," + sender + "," + receiver;
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

                    int initiator = Integer.parseInt(parts[0]);
                    int sender = Integer.parseInt(parts[1]);
                    int receiver = Integer.parseInt(parts[2]);

                    System.out.println("\nNode " + myId +
                            ": Received Probe (" + initiator + ", " + sender + ", " + receiver + ")");

                    if (initiator == myId) {
                        System.out.println("********** DEADLOCK DETECTED (AND Model) **********");
                        System.out.println("Node " + myId + " is part of a deadlocked cycle.");
                    } else {
                        // Forward probe to all dependent nodes
                        for (int i = 0; i < MAX_NODES; i++) {
                            if (dependentNodes[i] == 1) {
                                System.out.println("Node " + myId +
                                        ": Forwarding probe to Node " + i);
                                sendProbe(initiator, myId, i);
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
            System.out.println("\n--- Node " + myId + " (AND Model) ---");
            System.out.println("1. Add Dependency");
            System.out.println("2. Initiate Deadlock Detection");
            System.out.println("3. Clear All Dependencies");
            System.out.print("Choice: ");

            int choice = sc.nextInt();

            if (choice == 1) {
                System.out.print("Enter Node ID to wait for: ");
                int dep = sc.nextInt();

                if (dep >= 0 && dep < MAX_NODES && dep != myId) {
                    dependentNodes[dep] = 1;
                }

            } else if (choice == 2) {
                for (int i = 0; i < MAX_NODES; i++) {
                    if (dependentNodes[i] == 1) {
                        sendProbe(myId, myId, i);
                    }
                }

            } else if (choice == 3) {
                Arrays.fill(dependentNodes, 0);
                System.out.println("Dependencies cleared.");
            }
        }
    }
}