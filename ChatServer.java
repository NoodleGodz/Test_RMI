import java.rmi.Remote;
import java.rmi.RemoteException;





public interface ChatServer extends Remote {
    public void enter(String name, Callback cb) throws RemoteException;
    public void leave(String name) throws RemoteException;
    public String[] who() throws RemoteException;
    public void write(String name, String text) throws RemoteException;
    public void attach(String name,String[] hoststring, String[] info) throws RemoteException; 
    }