import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CallbackImpl extends UnicastRemoteObject implements Callback {

    protected CallbackImpl() throws RemoteException {
        super();

    }

    @Override
    public void print(String name, String text) throws RemoteException {
        ChatClient.print(name, text);
    }

    @Override
    public void connect(String[] hostring, String[] info) throws RemoteException {
        ChatClient.receive(hostring,info);
    }
    
}
