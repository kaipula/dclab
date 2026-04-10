import java.net.*;
import java.util.*;

public class TokenBasedME {

    static int MAX_NODES;
    static final int BASE_PORT = 9000;

    static int myId;
    static int[] RN;
    static boolean hasToken = false;
    static boolean isInCS = false;

    // Token Structure
    static class Token implements java.io.Serializable {
        int[] LN;
        Queue<Integer> queue;

        Token(int n) {
            LN = new int[n];
            queue = new LinkedList<>();
        }
    }

    static Token systemToken;

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

                    synchronized (TokenBasedME.class) {

                        if (type == 1) { // REQUEST
                            int sender = Integer.parseInt(parts[1]);
                            int seq = Integer.parseInt(parts[2]);

                            RN[sender] = Math.max(RN[sender], seq);

                            if (hasToken && !isInCS && RN[sender] == systemToken.LN[sender] + 1) {
                                sendToken(sender);
                            }

                        } else if (type == 2) { // TOKEN
                            systemToken = deserializeToken(parts[1]);
                            hasToken = true;
                            System.out.println("Node " + myId + ": *** RECEIVED TOKEN ***");
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Serialize token
    static String serializeToken(Token t) {
        StringBuilder sb = new StringBuilder();

        for (int val : t.LN) sb.append(val).append(":");
        sb.append("|");

        for (int q : t.queue) sb.append(q).append(":");

        return sb.toString();
    }

    // Deserialize token
    static Token deserializeToken(String data) {
        String[] parts = data.split("\\|");
        String[] lnParts = parts[0].split(":");

        Token t = new Token(MAX_NODES);

        for (int i = 0; i < MAX_NODES; i++) {
            t.LN[i] = Integer.parseInt(lnParts[i]);
        }

        if (parts.length > 1 && !parts[1].isEmpty()) {
            String[] qParts = parts[1].split(":");
            for (String q : qParts) {
                if (!q.isEmpty()) t.queue.add(Integer.parseInt(q));
            }
        }

        return t;
    }

    // Send request
    static void requestCS() throws Exception {
        synchronized (TokenBasedME.class) {
            if (hasToken) return;

            RN[myId]++;
        }

        for (int i = 0; i < MAX_NODES; i++) {
            if (i == myId) continue;

            sendMessage(i, "1," + myId + "," + RN[myId]);
        }

        while (!hasToken) Thread.sleep(10);
    }

    // Release CS
    static void releaseCS() throws Exception {
        synchronized (TokenBasedME.class) {
            systemToken.LN[myId] = RN[myId];
            isInCS = false;

            for (int i = 0; i < MAX_NODES; i++) {
                if (!systemToken.queue.contains(i) &&
                        RN[i] == systemToken.LN[i] + 1) {
                    systemToken.queue.add(i);
                }
            }

            if (!systemToken.queue.isEmpty()) {
                int next = systemToken.queue.poll();
                sendToken(next);
            }
        }
    }

    // Send token
    static void sendToken(int dest) throws Exception {
        String tokenData = serializeToken(systemToken);
        sendMessage(dest, "2," + tokenData);
        hasToken = false;

        System.out.println("Node " + myId + ": Token sent to Node " + dest);
    }

    // Send message
    static void sendMessage(int dest, String msg) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        byte[] data = msg.getBytes();
        InetAddress address = InetAddress.getByName("127.0.0.1");

        DatagramPacket packet = new DatagramPacket(
                data, data.length, address, BASE_PORT + dest
        );

        socket.send(packet);
        socket.close();
    }

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter total number of nodes: ");
        MAX_NODES = sc.nextInt();

        RN = new int[MAX_NODES];

        System.out.print("Enter your node ID (0 to " + (MAX_NODES - 1) + "): ");
        myId = sc.nextInt();

        System.out.print("Do you have initial token? (1=yes, 0=no): ");
        hasToken = sc.nextInt() == 1;

        systemToken = new Token(MAX_NODES);

        // Start listener
        Listener listener = new Listener();
        listener.start();

        sc.nextLine(); // clear buffer

        while (true) {
            System.out.println("\nPress ENTER to request Critical Section...");
            sc.nextLine();

            requestCS();

            isInCS = true;
            System.out.println(">>> Node " + myId + " ENTERING CS <<<");

            Thread.sleep(3000);

            System.out.println(">>> Node " + myId + " EXITING CS <<<");

            releaseCS();
        }
    }
}