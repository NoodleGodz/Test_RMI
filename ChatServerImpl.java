import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    HashMap<String, Callback> active_user = new HashMap<String, Callback>();

    protected ChatServerImpl() throws RemoteException {
    }

    @Override
    public void enter(String name, Callback cb) throws RemoteException {
        if (active_user.containsKey(name)) {
            System.out.println("This user already logged in: " + name);
            throw new IllegalArgumentException("This user already logged in: " + name);
        };
        active_user.put(name, cb);
        System.out.println(Arrays.toString(who()));
        write("server", name+" entered");
    }

    @Override
    public void leave(String name) throws RemoteException {

        write("server", name+" left");
        active_user.remove(name);
        System.out.println(Arrays.toString(who()));
        

    }

    @Override
    public String[] who() throws RemoteException {
        Set<String> keySet = active_user.keySet();
        String[] keysArray = keySet.toArray(new String[keySet.size()]);
        return keysArray;

    }

    @Override
    public void write(String name, String text) throws RemoteException {

        for (Map.Entry<String,Callback> v : active_user.entrySet()) {
            v.getValue().print(name, text);
            
        }
    }
    


    public static void main(String[] args) {
        int port;
        try {
            port = Integer.valueOf(args[0]);
            }
        catch (Exception ex) {
            System.out.println(" Please enter: java PadImpl <port>"); return;
            }
        
        try {
            LocateRegistry.createRegistry(port);
            String URL = "//localhost:"+port+"/messenger";
            InetAddress ip = InetAddress.getLocalHost();
            String ipAddress = ip.getHostAddress();
            System.out.println("Server running on " + ipAddress + " on port " + port);
            ChatServer obj = new ChatServerImpl();
            Naming.rebind(URL, obj);
            System.out.println(" ChatServer is online...\n");

        } catch (RemoteException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    public void attach(String name,String[] hoststring, String[] info) {

        System.out.println(Arrays.toString(info));
        System.out.println(Arrays.toString(hoststring));
        for (Map.Entry<String,Callback> v : active_user.entrySet()) {
            if (v.getKey().equals(name)==false) {
                try {
                    v.getValue().print("server", name + " want to share file "+info[0]+" ("+info[1]+"KB)\n....Downloading file to your computer....\n");
                    v.getValue().connect(hoststring,info);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        System.out.println("Sending messenge done....");
        }
        //throw new UnsupportedOperationException("Unimplemented method 'attach'");
    }
}
