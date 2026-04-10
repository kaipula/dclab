import java.net.*;
import java.util.*;

public class RicartAgrawala {

    static int MAX_NODES;
    static final int BASE_PORT = 8080;

    static int myId;
    static int logicalClock = 0;
    static int requestClock = 0;
    static int repliesReceived = 0;

    static boolean isRequesting = false;
    static boolean isInCS = false;

    static boolean[] replyDeferred;

    // Listener Thread
    static class Listener extends Thread {
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(BASE_PORT + myId);

                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msgStr = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = msgStr.split(",");

                    int type = Integer.parseInt(parts[0]); // 0=REQ, 1=REP
                    int timestamp = Integer.parseInt(parts[1]);
                    int sender = Integer.parseInt(parts[2]);

                    synchronized (RicartAgrawala.class) {

                        logicalClock = Math.max(logicalClock, timestamp) + 1;

                        if (type == 0) { // REQUEST
                            boolean priority = (timestamp < requestClock) ||
                                    (timestamp == requestClock && sender < myId);

                            if (isInCS || (isRequesting && !priority)) {
                                replyDeferred[sender] = true;
                                System.out.println("Node " + myId + ": Deferred reply to Node " + sender);
                            } else {
                                sendReply(sender);
                                System.out.println("Node " + myId + ": Sent REPLY to Node " + sender);
                            }

                        } else { // REPLY
                            repliesReceived++;
                            System.out.println("Node " + myId + ": Received REPLY from " + sender +
                                    " (Total: " + repliesReceived + ")");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Request access to CS
    static void requestAccess() throws Exception {
        synchronized (RicartAgrawala.class) {
            isRequesting = true;
            requestClock = ++logicalClock;
            repliesReceived = 0;
        }

        for (int i = 0; i < MAX_NODES; i++) {
            if (i == myId) continue;
            sendMessage(i, 0, requestClock);
        }

        // Wait for replies
        while (true) {
            synchronized (RicartAgrawala.class) {
                if (repliesReceived == MAX_NODES - 1) break;
            }
            Thread.sleep(10);
        }
    }

    // Send message
    static void sendMessage(int destId, int type, int timestamp) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        String msg = type + "," + timestamp + "," + myId;
        byte[] data = msg.getBytes();

        InetAddress address = InetAddress.getByName("127.0.0.1");

        DatagramPacket packet = new DatagramPacket(
                data, data.length, address, BASE_PORT + destId
        );

        socket.send(packet);
        socket.close();
    }

    // Send reply
    static void sendReply(int destId) throws Exception {
        sendMessage(destId, 1, logicalClock);
    }

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter total number of nodes: ");
        MAX_NODES = sc.nextInt();

        System.out.print("Enter your node ID (0 to " + (MAX_NODES - 1) + "): ");
        myId = sc.nextInt();
        sc.nextLine(); // clear buffer

        replyDeferred = new boolean[MAX_NODES];

        // Start listener
        Listener listener = new Listener();
        listener.start();

        while (true) {
            System.out.println("\nPress ENTER to request Critical Section...");
            sc.nextLine();

            requestAccess();

            System.out.println(">>> Node " + myId + " ENTERING CRITICAL SECTION <<<");
            isInCS = true;

            Thread.sleep(3000);

            System.out.println(">>> Node " + myId + " EXITING CRITICAL SECTION <<<");

            synchronized (RicartAgrawala.class) {
                isInCS = false;
                isRequesting = false;

                for (int i = 0; i < MAX_NODES; i++) {
                    if (replyDeferred[i]) {
                        sendReply(i);
                        replyDeferred[i] = false;
                    }
                }
            }
        }
    }
}