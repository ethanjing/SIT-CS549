package edu.stevens.cs549.ftpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Stack;
import java.util.logging.Logger;

import edu.stevens.cs549.ftpinterface.IServer;

/**
 *
 * @author dduggan
 */
public class Server extends UnicastRemoteObject
        implements IServer {
	
	static final long serialVersionUID = 0L;
	
	public static Logger log = Logger.getLogger("edu.stevens.cs.cs549.ftpserver");
    
	/*
	 * For multi-homed hosts, must specify IP address on which to 
	 * bind a server socket for file transfers.  See the constructor
	 * for ServerSocket that allows an explicit IP address as one
	 * of its arguments.
	 */
	private InetAddress host;
	
	final static int backlog = 5;
	
	/*
	 *********************************************************************************************
	 * Current working directory.
	 */
    static final int MAX_PATH_LEN = 1024;
    private Stack<String> cwd = new Stack<String>();
    
    /*
     *********************************************************************************************
     * Data connection.
     */
    
    enum Mode { NONE, PASSIVE, ACTIVE };
    
    private Mode mode = Mode.NONE;
    
    /*
     * If passive mode, remember the server socket.
     */
    
    private ServerSocket dataChan = null;
    
    private InetSocketAddress makePassive () throws IOException {
    	dataChan = new ServerSocket(0, backlog, host);
    	mode = Mode.PASSIVE;
    	return (InetSocketAddress)(dataChan.getLocalSocketAddress());
    }
    
    /*
     * If active mode, remember the client socket address.
     */
    private InetSocketAddress clientSocket = null;
    
    private void makeActive (InetSocketAddress s) {
    	clientSocket = s;
    	mode = Mode.ACTIVE;
    }
    
    /*
     **********************************************************************************************
     */
            
    /*
     * The server can be initialized to only provide subdirectories
     * of a directory specified at start-up.
     */
    private final String pathPrefix;

    public Server(InetAddress host, int port, String prefix) throws RemoteException {
    	super(port);
    	this.host = host;
    	this.pathPrefix = prefix + "/";
        log.info("A client has bound to a server instance.");
    }
    
    public Server(InetAddress host, int port) throws RemoteException {
        this(host, port, "/");
    }
    
    private boolean valid (String s) {
        // File names should not contain "/".
        return (s.indexOf('/')<0);
    }
    
    static void msg(String m) {
		System.out.print(m);
	}

	static void msgln(String m) {
		System.out.println(m);
	}
    
    private static class GetThread implements Runnable {
    	private ServerSocket dataChan = null;
    	private FileInputStream file = null;
    	public GetThread (ServerSocket s, FileInputStream f) { dataChan = s; file = f; }
    	public void run () {
    		/*
    		 * TODO: Process a client request to transfer a file.
    		 */
    		try {
    			Socket xfer = dataChan.accept();
    			
    			OutputStream out = xfer.getOutputStream();
    			byte[] buf = new byte[512];
    			int nbytes = file.read(buf, 0, 512);
    			
    			while (nbytes > 0) {
    				out.write(buf, 0, nbytes);
    				out.flush();
    				nbytes = file.read(buf, 0, 512);
    			}
    			
    			out.close();
    			file.close();
    			xfer.close();
    		} catch (IOException e) {
				msg("Exception: " + e);
				e.printStackTrace();
    		}
    	}
    }
    
    public void get (String file) throws IOException, FileNotFoundException, RemoteException {
        if (!valid(file)) {
            throw new IOException("Bad file name: " + file);
        } else if (mode == Mode.ACTIVE) {
        	Socket xfer = new Socket (clientSocket.getAddress(), clientSocket.getPort());
        	/*
        	 * TODO: connect to client socket to transfer file.
        	 */
        	FileInputStream in = new FileInputStream(path()+file);
        	OutputStream out = xfer.getOutputStream();
        	
        	byte[] buf = new byte[512];
			int nbytes = in.read(buf, 0, 512);
			
			while (nbytes > 0) {
				out.write(buf, 0, nbytes);
				out.flush();
				nbytes = in.read(buf, 0, 512);
			}
			
			out.close();
			in.close();
			xfer.close();
        	/*
			 * End TODO.
			 */
        } else if (mode == Mode.PASSIVE) {
            FileInputStream f = new FileInputStream(path()+file);
            new Thread (new GetThread(dataChan, f)).start();
        } else {
        	msgln("GET: No mode set--use port or pasv command.");
        }
    }
    
    private static class PutThread implements Runnable {
		/*
		 * This client-side thread runs when the server is active mode and a
		 * file download is initiated. This thread listens for a connection
		 * request from the server. The client-side server socket (...)
		 * should have been created when the port command put the server in
		 * active mode.
		 */
		private ServerSocket dataChan = null;
		private FileOutputStream file = null;

		public PutThread(ServerSocket s, FileOutputStream f) {
			dataChan = s;
			file = f;
		}

		public void run() {
			try {
				Socket xfer = dataChan.accept();
				InputStream in = xfer.getInputStream();
				byte[] buf = new byte[512];
				int nbytes = in.read(buf, 0, 512);
				
				while (nbytes > 0) {
					file.write(buf, 0, nbytes);
					file.flush();
					nbytes = in.read(buf, 0, 512);
				}
				
				in.close();
				file.close();
				xfer.close();
			} catch (IOException e) {
				msg("Exception: " + e);
				e.printStackTrace();
			}
		}
	}
    
    public void put (String file) throws IOException, FileNotFoundException, RemoteException {
    	/*
    	 * TODO: Finish put (both ACTIVE and PASSIVE).
    	 */
    	if (!valid(file)) {
    		throw new IOException("Bad file name: " + file);
    	}
    	else if (mode == Mode.ACTIVE) {
    		Socket xfer = new Socket(clientSocket.getAddress(), clientSocket.getPort());
    		
    		FileOutputStream out = new FileOutputStream(path()+file);
    		InputStream in = xfer.getInputStream();
    		
    		byte[] buf = new byte[512];
			int nbytes = in.read(buf, 0, 512);
			
			while (nbytes > 0) {
				out.write(buf, 0, nbytes);
				out.flush();
				nbytes = in.read(buf, 0, 512);
			}
			
			out.close();
			in.close();
			xfer.close();
    	} else if (mode == Mode.PASSIVE) {
    		FileOutputStream f = new FileOutputStream(path() + file);
    		new Thread(new PutThread(dataChan, f)).start();
    	} else {
    		msgln("PUT: No mode set--use port or pasv command.");
    	}
    }
    
    public String[] dir () throws RemoteException {
        // List the contents of the current directory.
        return new File(path()).list();
    }

	public void cd(String dir) throws IOException, RemoteException {
		// Change current working directory (".." is parent directory)
		if (!valid(dir)) {
			throw new IOException("Bad file name: " + dir);
		} else {
			if ("..".equals(dir)) {
				if (cwd.size() > 0)
					cwd.pop();
				else
					throw new IOException("Already in root directory!");
			} else if (".".equals(dir)) {
				;
			} else {
				File f = new File(path());
				if (!f.exists())
					throw new IOException("Directory does not exist: " + dir);
				else if (!f.isDirectory())
					throw new IOException("Not a directory: " + dir);
				else
					cwd.push(dir);
			}
		}
	}

    public String pwd () throws RemoteException {
        // List the current working directory.
        String p = "/";
        for (Enumeration<String> e = cwd.elements(); e.hasMoreElements(); ) {
            p = p + e.nextElement() + "/";
        }
        return p;
    }
    
    private String path () throws RemoteException {
    	return pathPrefix+pwd();
    }
    
    public void port (InetSocketAddress s) {
    	makeActive(s);
    }
    
    public InetSocketAddress pasv () throws IOException {
    	return makePassive();
    }

}
