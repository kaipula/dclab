import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            TaskImplementation obj = new TaskImplementation();

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("TaskService", obj);

            System.out.println("Task Scheduler Server Running...");
        } catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
    }
}