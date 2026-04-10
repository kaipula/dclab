import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            TaskInterface stub = (TaskInterface) registry.lookup("TaskService");

            Scanner sc = new Scanner(System.in);

            System.out.println("1. Add Task");
            System.out.println("2. View Tasks");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                System.out.print("Enter Task Name: ");
                String task = sc.nextLine();
                System.out.println(stub.addTask(task));
            } 
            else if (choice == 2) {
                List<String> taskList = stub.viewTasks();

                System.out.println("Scheduled Tasks:");
                for (String t : taskList) {
                    System.out.println("- " + t);
                }
            }

            sc.close();
        } catch (Exception e) {
            System.out.println("Client Error: " + e);
        }
    }
}