import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TaskImplementation extends UnicastRemoteObject implements TaskInterface {
    private List<String> tasks;

    protected TaskImplementation() throws RemoteException {
        super();
        tasks = new ArrayList<>();
    }

    public synchronized String addTask(String taskName) throws RemoteException {
        tasks.add(taskName);
        return "Task Added Successfully!";
    }

    public synchronized List<String> viewTasks() throws RemoteException {
        return tasks;
    }
}