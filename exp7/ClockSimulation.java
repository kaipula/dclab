import java.util.*;

public class ClockSimulation {

    static void logicalClock(Scanner sc) {
        System.out.println("\n--- LOGICAL CLOCK ---");

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int[] events = new int[n];
        int[] initialTime = new int[n];
        int[] dValue = new int[n];
        int[][] clock = new int[n][];

        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess P" + (i + 1));

            System.out.print("Number of events: ");
            events[i] = sc.nextInt();

            System.out.print("Initial time: ");
            initialTime[i] = sc.nextInt();

            System.out.print("D value: ");
            dValue[i] = sc.nextInt();

            clock[i] = new int[events[i]];
            clock[i][0] = initialTime[i];

            for (int j = 1; j < events[i]; j++) {
                clock[i][j] = clock[i][j - 1] + dValue[i];
            }
        }

        System.out.println("\nInitial Logical Clocks:");
        for (int i = 0; i < n; i++) {
            System.out.print("P" + (i + 1) + ": ");
            for (int val : clock[i]) {
                System.out.print(val + " ");
            }
            System.out.println();
        }

        System.out.print("\nEnter number of messages: ");
        int m = sc.nextInt();

        for (int k = 0; k < m; k++) {
            System.out.println("\nMessage " + (k + 1));

            System.out.print("Sender process: ");
            int sp = sc.nextInt() - 1;

            System.out.print("Sender event: ");
            int se = sc.nextInt() - 1;

            System.out.print("Receiver process: ");
            int rp = sc.nextInt() - 1;

            System.out.print("Receiver event: ");
            int re = sc.nextInt() - 1;

            int sendTime = clock[sp][se];

            clock[rp][re] = Math.max(clock[rp][re], sendTime + 1);

            for (int j = re + 1; j < events[rp]; j++) {
                clock[rp][j] = clock[rp][j - 1] + dValue[rp];
            }
        }

        System.out.println("\nFinal Logical Clocks:");
        for (int i = 0; i < n; i++) {
            System.out.print("P" + (i + 1) + ": ");
            for (int val : clock[i]) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    static void vectorClock(Scanner sc) {
        System.out.println("\n--- VECTOR CLOCK ---");

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int[][] vClock = new int[n][n];

        System.out.println("\nInitial vector clocks:");
        for (int i = 0; i < n; i++) {
            vClock[i][i] = 1;
        }

        for (int i = 0; i < n; i++) {
            System.out.print("P" + (i + 1) + ": ");
            for (int j = 0; j < n; j++) {
                System.out.print(vClock[i][j] + " ");
            }
            System.out.println();
        }

        System.out.print("\nEnter number of messages: ");
        int m = sc.nextInt();

        for (int k = 0; k < m; k++) {
            System.out.println("\nMessage " + (k + 1));

            System.out.print("Sender process: ");
            int sp = sc.nextInt() - 1;

            System.out.print("Receiver process: ");
            int rp = sc.nextInt() - 1;

            vClock[sp][sp]++;

            int[] sent = Arrays.copyOf(vClock[sp], n);

            for (int i = 0; i < n; i++) {
                vClock[rp][i] = Math.max(vClock[rp][i], sent[i]);
            }

            vClock[rp][rp]++;
        }

        System.out.println("\nFinal Vector Clocks:");
        for (int i = 0; i < n; i++) {
            System.out.print("P" + (i + 1) + ": ");
            for (int j = 0; j < n; j++) {
                System.out.print(vClock[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("1. Logical Clock");
        System.out.println("2. Vector Clock");
        System.out.print("Choose option: ");

        int choice = sc.nextInt();

        if (choice == 1) {
            logicalClock(sc);
        } else if (choice == 2) {
            vectorClock(sc);
        } else {
            System.out.println("Invalid choice.");
        }

        sc.close();
    }
}