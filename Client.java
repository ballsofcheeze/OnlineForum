/*
 *
 *  TCPClient from Kurose and Ross
 *  * Compile: java TCPClient.java
 *  * Run: java TCPClient
 */
import java.io.*;
import java.net.*;
import java.lang.*;
import java.lang.Object.*;

public class Client {

	public static void main(String[] args) throws Exception {

		// Define socket parameters, address and Port No
        
        InetAddress ipAddress = InetAddress.getByName(args[0]);
		int serverPort = Integer.parseInt(args[1]); 
		//change above port number if required
		
		// create socket which connects to server
		Socket clientSocket = new Socket(ipAddress, serverPort);
/*This line creates the client?s socket, called clientSocket. The first parameter indicates the server address and the second parameter indicates the port number of the Server. In Java, this also initiates the TCP 3 way handshake*/
        boolean active = true;
        
        // Set up stuff 
	    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromUser =
	        new BufferedReader(new InputStreamReader(System.in));
	    DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
	    String sentence;
	    String sentenceFromServer;
	    boolean logged = false;
        while (active) {

            // Flow chart series of actions
            while (logged == false) {
                //TODO comment on this 
                System.out.println("Enter username: ");
                sentence = inFromUser.readLine();
                outToServer.writeUTF(sentence);
                // Client sends, server catches
                sentenceFromServer = inFromServer.readUTF();
                if (sentenceFromServer.equals("NO MATCH")) { // username not found in txt, make new user
                    System.out.println("Username not found");
                    System.out.println("New username made. Enter password: ");
                    sentence = inFromUser.readLine();
                    outToServer.writeUTF(sentence);
                    sentenceFromServer = inFromServer.readUTF();
                    if (sentenceFromServer.equals("SPACE")) { // spaces not allwoed in username or password
                        System.out.println("No spaces allowed in username or password");
                    } else {
                        System.out.println("Password created");
                        logged = true;
                        System.out.println("Welcome to the forum"); // success!
                    }
                } else if (sentenceFromServer.equals("MATCH")) { // username foudn in txt, check for password now 
                    System.out.println("Enter password: ");
                    sentence = inFromUser.readLine();
                    outToServer.writeUTF(sentence);
                    sentenceFromServer = inFromServer.readUTF();
                    if (sentenceFromServer.equals("MATCH")) {
                        logged = true;
                        System.out.println("Welcome to the forum"); // success!
                    } else if (sentenceFromServer.equals("NO MATCH")) {
                        System.out.println("Incorrect password"); // password not found, user goes back to start of loop
                    }
                    
                    
                }
                
            }
            
            System.out.println("Enter one of the following commands: CRT, MSG, DLT, EDT, LST, RDT, UPD, DWN, RMV, XIT, SHT: ");
		    sentence = inFromUser.readLine();

		    // write to server
		    outToServer.writeUTF(sentence);
		    // create read stream and receive from server
		    
		    sentenceFromServer = inFromServer.readUTF();
            
            String[] splitted = sentence.split(" ");
            
		    if (splitted[0].equals("LST")) {
		        if (sentenceFromServer.equals("NO MATCH")) { 
		            System.out.println("No threads have been made");
		        } else if (sentenceFromServer.equals("ERROR")) {
		            System.out.println("No arguments required");
		        } else {
		            String[] sep = sentenceFromServer.split(" ");
		            for (int i = 0; i < sep.length; i++) {
		                System.out.println(sep[i]);
		            }
	            }
		    } else if (splitted[0].equals("RDT")) {
		        if (sentenceFromServer.equals("ERROR")) {
		            System.out.println("You must input a thread title and a filename");
		        } else if (sentenceFromServer.equals("NO MATCH")) {
		            System.out.println("Thread not found");
		        } else {
		            String[] sep = sentenceFromServer.split("@newline@");
		            for (int i = 1; i < sep.length; i++) {
		                System.out.println(sep[i]);
		            }
		            if (sep.length == 1) {
		                System.out.println("Thread is empty");
		            }
		        }
		    } else if (splitted[0].equals("UPD")) { //https://www.rgagnon.com/javadetails/java-0542.html CREDIT
		        if (sentenceFromServer.equals("ERROR")) {
		            System.out.println("You must input a thread title and a filename");
		        } else if (sentenceFromServer.equals("NO MATCH")) {
		            System.out.println("Thread not found");
		        } else if (sentenceFromServer.equals("MATCH")) {
		            // reads file argument into f1
		            File f1 = new File(splitted[2]);
		            InputStream fileReader = new FileInputStream(f1);


		            byte[] buffer = new byte[8192];
		            int count;
                    count = fileReader.read(buffer);
                    outToServer.write(buffer, 0, count);
                    
		            fileReader.close();
		            sentenceFromServer = inFromServer.readUTF();
		            if (sentenceFromServer.equals("SUCCESS")) {
		                System.out.println(splitted[2] + " uploaded to server");
		            }
		        }
		    
		    } else if (splitted[0].equals("DWN")) { //https://www.rgagnon.com/javadetails/java-0542.html CREDIT
		        if (sentenceFromServer.equals("ERROR")) {
		            System.out.println("You must input a thread title");
		        } else if (sentenceFromServer.equals("NO MATCH")) {
		            System.out.println("Thread or file not found");
		        } else if (sentenceFromServer.equals("MATCH")) {
		            File f1 = new File(splitted[1]);
                    FileOutputStream dwn = new FileOutputStream(f1);
                    
                    byte[] buffer = new byte[8192];
                    int count;
                    count = inFromServer.read(buffer);
                    dwn.write(buffer, 0, count);
                    dwn.close();
		            sentenceFromServer = inFromServer.readUTF();
		            if (sentenceFromServer.equals("SUCCESS")) {
		                System.out.println(splitted[2] + " downloaded from server");
		            }
		        }
		    } else if (splitted[0].equals("XIT")) {
		        System.out.println(sentenceFromServer);
		        System.out.println("Client shutting down");
		        inFromServer.close();
		        System.exit(0);
		    } else {
		        // print output
		        System.out.println(sentenceFromServer);
		    }

        } // end of while loop
        
		// close client socket
		clientSocket.close();

	} // end of main

} // end of class TCPClient
