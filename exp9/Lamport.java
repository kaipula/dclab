import java.net.*;
import java.util.*;

public class Lamport {

    static int MAX_NODES;
    static final int BASE_PORT = 11000;

    static int myId;
    static int logicalClock = 0;

    static class Request {
        int timestamp, nodeId;

        Request(int t, int id) {
            timestamp = t;
            nodeId = id;
        }
    }

    static List<Request> queue = new ArrayList<>();
    static int ackCount = 0;

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
                    int ts = Integer.parseInt(parts[1]);
                    int sender = Integer.parseInt(parts[2]);

                    synchronized (Lamport.class) {

                        logicalClock = Math.max(logicalClock, ts) + 1;

                        if (type == 0) { // REQUEST
                            queue.add(new Request(ts, sender));
                            System.out.println("Node " + myId +
                                    ": Received REQ from " + sender + " at T=" + ts);

                            sendMsg(sender, 1, logicalClock); // ACK

                        } else if (type == 1) { // ACK
                            ackCount++;

                        } else if (type == 2) { // RELEASE
                            queue.removeIf(r -> r.nodeId == sender);
                            System.out.println("Node " + myId +
                                    ": Node " + sender + " Released CS");
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Send message
    static void sendMsg(int toId, int type, int ts) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        String msg = type + "," + ts + "," + myId;
        byte[] data = msg.getBytes();

        InetAddress address = InetAddress.getByName("127.0.0.1");

        DatagramPacket packet = new DatagramPacket(
                data, data.length, address, BASE_PORT + toId
        );

        socket.send(packet);
        socket.close();
    }

    // Check if it's my turn
    static boolean isMyTurn(int myTs) {
        Request oldest = null;

        for (Request r : queue) {
            if (oldest == null ||
                    r.timestamp < oldest.timestamp ||
                    (r.timestamp == oldest.timestamp && r.nodeId < oldest.nodeId)) {
                oldest = r;
            }
        }

        return oldest != null &&
                oldest.nodeId == myId &&
                oldest.timestamp == myTs;
    }

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter total number of nodes: ");
        MAX_NODES = sc.nextInt();

        System.out.print("Enter your node ID (0 to " + (MAX_NODES - 1) + "): ");
        myId = sc.nextInt();
        sc.nextLine(); // clear buffer

        // Start listener
        Listener listener = new Listener();
        listener.start();

        while (true) {
            System.out.println("\nNode " + myId + ": Press ENTER to Request CS...");
            sc.nextLine();

            int myReqTs;

            synchronized (Lamport.class) {
                logicalClock++;
                myReqTs = logicalClock;
                queue.add(new Request(myReqTs, myId));
                ackCount = 0;
            }

            // Send REQUEST to all
            for (int i = 0; i < MAX_NODES; i++) {
                if (i != myId) {
                    sendMsg(i, 0, myReqTs);
                }
            }

            // Wait for ACKs and turn
            while (true) {
                synchronized (Lamport.class) {
                    if (ackCount >= MAX_NODES - 1 && isMyTurn(myReqTs)) {
                        break;
                    }
                }
                Thread.sleep(10);
            }

            System.out.println("--- Node " + myId + ": IN CRITICAL SECTION ---");
            Thread.sleep(3000);
            System.out.println("--- Node " + myId + ": LEAVING CS ---");

            synchronized (Lamport.class) {
                queue.removeIf(r -> r.nodeId == myId);
                logicalClock++;
            }

            // Send RELEASE
            for (int i = 0; i < MAX_NODES; i++) {
                if (i != myId) {
                    sendMsg(i, 2, logicalClock);
                }
            }
        }
    }
}