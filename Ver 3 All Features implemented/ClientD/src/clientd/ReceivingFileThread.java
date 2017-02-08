/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clientd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 *
 * @author Hunk501
 */
public class ReceivingFileThread implements Runnable {
    
    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected MainForm main;
    protected StringTokenizer st;
    protected DecimalFormat df = new DecimalFormat("##,#00");
    private final int BUFFER_SIZE = 100;
    protected DataOutputStream dres;
    Socket socres;
    
    public ReceivingFileThread(Socket soc, MainForm m, String host, int port){
        this.socket = soc;
        this.main = m;
        //System.out.println("Entered ReceivingFile Thread mode.");
        try {
            socres=new Socket(host,port); //socket to send succesful file download response to server
            dres=new DataOutputStream(socres.getOutputStream());   //output channel to send response
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("[ReceivingFileThread]: Error " +e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()){
                System.out.println(Thread.currentThread().isInterrupted());
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();
                //System.out.println("The Data is: "+data);
                switch(CMD){
                    
                    //   This will handle the recieving of a file in background process from other user
                    case "CMD_SENDFILE":
                        String consignee = null;
                            try {
                                String filename = st.nextToken();
                                int filesize = Integer.parseInt(st.nextToken());
                                consignee = st.nextToken(); // Get the Sender Username
                                //main.setMyTitle("Downloading File....");
                                System.out.println("Downloading File....");
                                System.out.println("From: "+ consignee);
                                String path = main.getMyDownloadFolder() + filename;                                
                                /*  Creat Stream   */
                                FileOutputStream fos = new FileOutputStream(path);
                                InputStream input = socket.getInputStream();                                                               
                                BufferedInputStream bis = new BufferedInputStream(input);   //in braces previously 'pmis'                                
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int count, percent = 0;
                                while((count = bis.read(buffer)) != -1){                                  
                                    fos.write(buffer, 0, count);
                                }
                                input.close();
                                bis.close();
                                fos.flush();
                                fos.close();
                                dres.writeUTF("CMD_SENDFILERESPONSE " +consignee+" "+filename+" Success");
                                //main.setMyTitle("you are logged in as: " + main.getMyUsername());
                                JOptionPane.showMessageDialog(main, "File has been downloaded to \n'"+ path +"'");
                                System.out.println("File was saved: "+ path);
                                Thread.currentThread().interrupt();
                            } catch (IOException e) {
                                /*
                                Send back an error message to sender
                                Format: CMD_SENDFILERESPONSE [username] [Message]
                                */
                                DataOutputStream eDos = new DataOutputStream(socket.getOutputStream());
                                eDos.writeUTF("CMD_SENDFILERESPONSE "+ consignee + " Connection was lost, please try again later.!");
                                
                                System.out.println(e.getMessage());
                                main.setMyTitle("you are logged in as: " + main.getMyUsername());
                                JOptionPane.showMessageDialog(main, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                                socket.close();
                            }
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[ReceivingFileThread]: " +e.getMessage());
        }
    }
}
