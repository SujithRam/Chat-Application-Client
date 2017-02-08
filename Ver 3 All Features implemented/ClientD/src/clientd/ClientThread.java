package clientd;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author Hunk501
 */
public class ClientThread implements Runnable{
    
    Socket socket;
    DataInputStream dis;
    DataOutputStream doos;
    MainForm main;
    StringTokenizer st;
    protected String file;
    protected String receiver;
    protected String sender;
    protected DecimalFormat df = new DecimalFormat("##,#00");
    private final int BUFFER_SIZE = 100;
    Socket soci;
    String host;
    int port;
    boolean status;
    
    public ClientThread(Socket socket, MainForm main, String host, int port){
        this.main = main;
        this.socket = socket;       
        this.host=host;
        this.port=port;
        status=false;
        try {
            soci=new Socket(host,port);
            dis = new DataInputStream(this.socket.getInputStream());
            doos = new DataOutputStream(soci.getOutputStream());
        } catch (IOException e) {
            main.appendMessage("[IOException]: "+ e.getMessage(), "Error", Color.RED, Color.RED);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if(status){
                try {
                    soci = new Socket(host, port);
                    dis = new DataInputStream(this.socket.getInputStream());
                    doos = new DataOutputStream(soci.getOutputStream());
                } catch (IOException e) {
                    main.appendMessage("[IOException]: Case2: " + e.getMessage(), "Error", Color.RED, Color.RED);
                }
                }
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                /** Get Message CMD **/
                String CMD = st.nextToken();
                switch(CMD){
                    case "CMD_MESSAGE":
                        SoundEffect.MessageReceive.play(); //  Play Audio clip
                        String msg = "";
                        String frm = st.nextToken();
                        msg=st.nextToken();
                        String dir=st.nextToken();//msg is nothing but the file to be searched 
                        main.appendMessage(" Request to download file "+msg, frm, Color.MAGENTA, Color.BLUE);
                        main.appendMessage(" Request Accepted ", main.getMyUsername(), Color.MAGENTA, Color.BLUE);
                        main.connectionpreparation(main.getMyUsername(),main.getMyHost(),main.getMyPort(),main);
                        String fil=dir+"/"+msg;
                        
                        this.file = fil;
                        this.receiver = frm;
                        this.sender = main.getMyUsername();
                        
                        System.out.println("Sending File..!");                       
                        File filename = new File(file);
                        System.out.println("File name: " + file);
                        int len = (int) filename.length();
                        int filesize = (int) Math.ceil(len / BUFFER_SIZE); // get the file size
                        String clean_filename = filename.getName();
                        doos.writeUTF("CMD_SENDFILE " + clean_filename.replace(" ", "_") + " " + filesize + " " + receiver + " " + sender);
                        //dos.close();
                        System.out.println("From: " + sender);
                        System.out.println("To: " + receiver);                        
                        InputStream input = new FileInputStream(filename);
                        //OutputStream output = socket.getOutputStream();                       
                        BufferedInputStream bis = new BufferedInputStream(input);                        
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int count;
                        while ((count = bis.read(buffer)) > 0) {                           
                            doos.write(buffer, 0, count);
                        } 
                        JOptionPane.showMessageDialog(main, "File successfully sent.!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
                        //bis.close();
                        //input.close();
                        doos.flush();
                        doos.close();
                        status=true;
                        //dos.close();
                        System.out.println("File was sent..!");
                        //Thread.currentThread().interrupt();               
                        //new Thread(new SendingFileThread(socket, fil, frm, main.getMyUsername())).start();                       
                        break;
                        
                    case "CMD_QUERY_RES":
                        String host=st.nextToken();
                        main.appendMyMessage(" File found at "+host, "Server");
                        break;                         
                        
                    default: 
                        main.appendMessage("[CMDException]: Unknown Command "+ CMD, "CMDException", Color.RED, Color.RED);
                    break;
                }
            }
        } catch(IOException e){
            main.appendMessage(e.getMessage(),"Cause",Color.RED, Color.RED);
            main.appendMessage(" Server Connection was lost, please try again later.!", "Error", Color.RED, Color.RED);
        }
    }
}
