import java.rmi.Remote;
import java.rmi.RemoteException;
/*
 * Test javadoc
 * 
 */
public interface Callback extends Remote {

    public void print(String name,String text) throws RemoteException;
    public void connect(String[] hostring,String[] info) throws RemoteException;
}
