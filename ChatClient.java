import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.awt.FileDialog;




public class ChatClient {
	public static TextArea		text;
	public static TextField		data;
	public static Frame 		frame;
	public static String serverhost,myhost;
	public static boolean login;
	public static Button write_button,who_button,leave_button,enter_button,attach_button;

	public static Vector<String> users = new Vector<String>();
	public static String myName;
	public static String port;
	
	public static void main(String argv[]) {
	
		if (argv.length != 2) {
			System.out.println(" Please enter: java ChatClient <server_port> <name>");
			return;
		}
		port = argv[0];
		myName = argv[1];
		serverhost= "192.168.1.3";
		myhost= "192.168.1.3";
		// creation of the GUI 
		frame=new Frame();
		FlowLayout fLayout = new FlowLayout();
		fLayout.setVgap(20);
		fLayout.setHgap(20);
		frame.setLayout(fLayout);
		

		text=new TextArea(10,55);
		text.setEditable(false);
		text.setFocusable(false);
		text.setForeground(Color.red);
		text.setBounds(0, 0 ,30, 30);
		frame.add(text);
		login = false;

		data=new TextField(55);
		data.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (ChatClient.login)) {
					ChatClient.write(ChatClient.myName, ChatClient.data.getText());;
				}

			}
		});
		frame.add(data);

		//Label usernamLabel = new Label("Username : "+myName);
		//frame.add(usernamLabel);

		frame.setTitle("Username : "+myName);

		write_button = new Button("write");
		write_button.addActionListener(new WriteListener());
		write_button.setEnabled(false);
		frame.add(write_button);

		attach_button = new Button("attach");
		attach_button.addActionListener(new AttachListener());
		attach_button.setEnabled(false);
		frame.add(attach_button);


		enter_button = new Button("enter");
		enter_button.addActionListener(new EnterListener());
		frame.add(enter_button);

		who_button = new Button("who");
		who_button.addActionListener(new WhoListener());
		who_button.setEnabled(false);
		frame.add(who_button);

		leave_button = new Button("leave");
		leave_button.addActionListener(new LeaveListener());
		leave_button.setEnabled(false);
		frame.add(leave_button);

		frame.setSize(470,300);
		text.setBackground(Color.black); 
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
    	public void windowClosing(WindowEvent e) {	
			ChatClient.leave(myName);
			frame.dispose();
			System.exit(0);
    	}
		});
		

	}	


	public static void enter(String username) {
		try {
			ChatServer cServer = (ChatServer) Naming.lookup("//"+serverhost+":"+port+"/messenger");
			Callback callback = new CallbackImpl();
			cServer.enter(username,callback);
			login=true;
			write_button.setEnabled(true);
			enter_button.setEnabled(false);
			who_button.setEnabled(true);
			leave_button.setEnabled(true);
			attach_button.setEnabled(login);
		} catch (IllegalArgumentException e) {
			System.out.println("This username is taken: " + myName+ "\nPlease choose another username");
			System.exit(0);

		} catch (RemoteException e) {
			System.out.println("Can't reach the server.\nPlease check your URL and port.\n\n...Maybe u forgot to run the server ?");
			System.exit(0);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

	}
	
	public static void leave(String username) {
		try {
			ChatServer cServer = (ChatServer) Naming.lookup("//"+serverhost+":"+port+"/messenger");
			cServer.leave(username);
			login=false;
			write_button.setEnabled(false);
			enter_button.setEnabled(true);
			who_button.setEnabled(false);
			leave_button.setEnabled(false);
			attach_button.setEnabled(login);
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}
	public static String combineString(String[] strings) {
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            combined.append(" -"+strings[i]);
            if (i < strings.length - 1) {
                combined.append("\n");
            }
        }

        return combined.toString();
    }
	public static void who() {
		String[] aStrings;
		try {
			ChatServer cServer = (ChatServer) Naming.lookup("//"+serverhost+":"+port+"/messenger");
			aStrings = cServer.who();
			String text = "active users: \n"+combineString(aStrings);
			print("server", text);

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static void write(String username, String text) {
		try {
			data.setText("");
			ChatServer cServer = (ChatServer) Naming.lookup("//"+serverhost+":"+port+"/messenger");
			cServer.write(username,text);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public static void print(String username, String text) {
		try {
			ChatClient.text.append(username+" says : "+text+"\n");
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	static class SlaveGet extends Thread {
		String[] host;
		String[] info;
		public SlaveGet(String[] hoStrings, String[] info)
		{
			this.host=hoStrings;
			this.info = info;
		}
		public void run() {
			try {

			Socket socket = new Socket(host[0],Integer.parseInt(host[1])); 
            InputStream is = socket.getInputStream();
			byte[] buffer = new byte[1024*8];
			String directoryPath = "Received_file/" + myName + "/";
			File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs(); 
            }
			FileOutputStream fos = new FileOutputStream(directoryPath+info[0]);
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

			print("server", "Received "+ info[0]+"\n");
            fos.close();
            is.close();
            socket.close();
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}

	static class SlaveSend extends Thread {

		Socket csocket;
		File file;
		public SlaveSend(Socket cSocket,File f) {
			this.csocket = cSocket;
			this.file = f;
		}

		@Override
		public void run() {

			try {
				byte[] buffer = new byte[(int) file.length()];
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				bis.read(buffer, 0,(int) file.length());

				OutputStream os = csocket.getOutputStream();
				os.write(buffer);
				os.flush();

				System.out.println("File sent to " + csocket.getInetAddress());
				bis.close();
				os.close();
				csocket.close();


			} catch (Exception e) {
				// TODO: handle exception
			}



		}


	}	

	public static void receive(String[] hosts,String[] info){
		
			/* 
			FileDialog fileDialog = new FileDialog(frame, "Choose a File", FileDialog.LOAD);
			fileDialog.setVisible(true);
			String sDirectory = fileDialog.getDirectory();
			*/
			SlaveGet sg = new SlaveGet(hosts, info);
			System.out.println(Arrays.toString(hosts));
			sg.start();

	}

	static class SlaveAttach extends Thread
	{
		String name;
		String direct;
		String filename;

		public SlaveAttach(String name,String direct, String filename)
		{
			this.name = name;
			this.direct = direct;
			this.filename = filename;

		}

		@Override
		public void run() {
			try {
				attach_button.setEnabled(false);
				ChatServer cServer = (ChatServer) Naming.lookup("//"+serverhost+":"+port+"/messenger");
				ServerSocket sSocket = new ServerSocket(0);
				String sport = String.valueOf(sSocket.getLocalPort()); 
				int numofclient = cServer.who().length-1;
				File fileToSend = new File(direct+filename);
				print("server", "Uploading "+filename+"\n");
				System.out.println("TCP started on port " + sport);
				String[] socketinfo = {myhost,sport};
				String[] info= {filename, String.valueOf(fileToSend.length()/ 1024)};
				cServer.attach(ChatClient.myName,socketinfo,info);
	
				List<SlaveSend> threads = new ArrayList<>(); 
				while (numofclient!=0) {
					Socket cSocket = sSocket.accept();
					System.out.println("Client " + String.valueOf(numofclient)+ " connected: " + cSocket.getInetAddress());
					SlaveSend ss = new SlaveSend(cSocket,fileToSend);
					ss.start();
					threads.add(ss);
					numofclient--;
				}
				
				for (SlaveSend t: threads) {
					t.join();
				}
				sSocket.close(); 
				print("server", "sending done....");
				attach_button.setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public static void attach(String name,String direct, String filename){
		SlaveAttach sa = new SlaveAttach(name, direct, filename);
		sa.start();
	}


}


	class WriteListener implements ActionListener {
		public void actionPerformed (ActionEvent ae) {
			try {
				ChatClient.write(ChatClient.myName, ChatClient.data.getText());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	class EnterListener implements ActionListener {
		public void actionPerformed (ActionEvent ae) {
			try {  
				ChatClient.enter(ChatClient.myName);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}  

	class WhoListener implements ActionListener {
		public void actionPerformed (ActionEvent ae) {
			try {
				ChatClient.who();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	class LeaveListener implements ActionListener {
		public void actionPerformed (ActionEvent ae) {
			try {
				ChatClient.leave(ChatClient.myName);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	class AttachListener implements ActionListener {
		public void actionPerformed (ActionEvent ae) {
			try {
				FileDialog fileDialog = new FileDialog(ChatClient.frame, "Select File to Attach", FileDialog.LOAD);
                fileDialog.setVisible(true);
				String filename = fileDialog.getFile();
                if (filename != null) {
                    String directory = fileDialog.getDirectory();
					System.out.println(directory);
					System.out.println(filename);
                    ChatClient.attach(ChatClient.myName,directory,filename);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


