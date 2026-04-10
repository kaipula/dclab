import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TaskInterface extends Remote {
    String addTask(String taskName) throws RemoteException;
    List<String> viewTasks() throws RemoteException;
}