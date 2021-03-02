/*
 *
 * TCPServer from Kurose and Ross
 * Compile: javac TCPServer.java
 * Run: java TCPServer
 */

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.ArrayList;

public class Server {
    
	public static void main(String[] args)throws Exception {
        /* define socket parameters, Address + PortNo, Address will default to localhost */
		int serverPort = Integer.parseInt(args[0]);
		/* change above port number if required */
		
		/*create server socket that is assigned the serverPort (6789)
        We will listen on this port for connection request from clients */
		ServerSocket welcomeSocket = new ServerSocket(serverPort);

        /* We have to read the credentials.txt file
        */
        ArrayList<String> creds = new ArrayList<>();
        ArrayList<String> users = new ArrayList<>();
        BufferedReader credReader = new BufferedReader(new FileReader("credentials.txt"));
        String credLine = credReader.readLine();
        while (credLine != null) {
            creds.add(credLine);
            credLine =  credReader.readLine();
        } 
        ArrayList<String> threads = new ArrayList<>();
		while (true){
            System.out.println("Waiting for clients");
		    // accept connection from connection queue
		    Socket connectionSocket = welcomeSocket.accept();
            /*When a client knocks on this door, the program invokes the accept( ) method for welcomeSocket, which creates a new socket in the server, called connectionSocket, dedicated to this particular client. The client and server then complete the handshaking, creating a TCP connection between the client?s clientSocket and the server?s connectionSocket. With the TCP connection established, the client and server can now send bytes to each other over the connection. With TCP, all bytes sent from one side not are not only guaranteed to arrive at the other side but also guaranteed to arrive in order*/     
           
		    // create read stream to get input
		    DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
		    String clientSentence;
		    // output stream created 
		    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		    
		    // We use this to check that the user has inputted a username that is foudn in the credentials.txt
		    // There are two outputs the server can send to the client (NO MATCH, MATCH)
            
            // check username 
            
            // the username is read from the client
            
            boolean logged = false;
            boolean user;
            boolean pass;
            String username = "not logged";
            String password = "not logged";

            while (logged == false) {
                System.out.println("waiting for user");
                clientSentence = inFromClient.readUTF();
                username = clientSentence;
                user = checkUser(clientSentence, creds);
                if (user) {
                    System.out.println("User found");
                    outToClient.writeUTF("MATCH");
                    clientSentence = inFromClient.readUTF();
                    pass = checkPass(clientSentence, creds);
                    
                    if (pass) {
                        password = clientSentence;
                        outToClient.writeUTF("MATCH");
                        logged = true;
                        users.add(username);
                    } else {
                        outToClient.writeUTF("NO MATCH");
                    }
                } else {
                    // We then receive a password from the client and we put into credentials.txt
                    // We first check if there are any spaces in the username
                    System.out.println("User not found");
                    outToClient.writeUTF("NO MATCH");
                    String newname = clientSentence;
                    clientSentence = inFromClient.readUTF();
                    String newpass = clientSentence;
                    
                    if (checkSpace(newname) || checkSpace(newpass)) {
                        outToClient.writeUTF("SPACE");
                    } else {
                        outToClient.writeUTF("");
                        password = clientSentence;
                        String newLine = newname + " " + newpass + '\n';
                        BufferedWriter credWriter = new BufferedWriter(new FileWriter("credentials.txt", true));
                        credWriter.write(newLine);
                        credWriter.close();
                        logged = true;
                        users.add(username);
                    }
                }
            }
            
            // An arraylist to keep track of all threads made by the server
            
		    while ((clientSentence = inFromClient.readUTF()) != null) {
                //data from client is stored in clientSentence
                
		        // We check what the command is equal to 
		        System.out.println(clientSentence);
		        String[] line = clientSentence.split(" ");
		        
		        if (line[0].equals("CRT")) { 
		            		        
		            //thread titles (argument 2) must be a single word
		            if (line.length > 2) {
		                outToClient.writeUTF("Thread titles can only be one word long");
		            } else if (line.length < 2) {
		                outToClient.writeUTF("You must input a thread title");
		            } else {
		                // check that the file already exists or not 
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (exists) {
                            outToClient.writeUTF("Thread title already exists");
                        } else {
                            // write in the username
                            FileWriter threadWriter = new FileWriter(line[1]);
                            threadWriter.write(username + "\n");
                            threadWriter.close();
                            threads.add(line[1]);
                            outToClient.writeUTF("Thread has been created");
                        }
		            }
		            
		        } else if (line[0].equals("LST")) {
		            // this is not necessary but it is done for the sake of consistency
		            if (line.length != 1) {
		                outToClient.writeUTF("ERROR");
		            } else {
		                String threadlist = "";
                        for (int i = 0; i < threads.size(); i++) {
                            
                            if (i == 0) {
                                threadlist = threadlist + threads.get(i);
                            } else {
                                threadlist = threadlist + " " + threads.get(i);
                            }
                        }
                        if (threadlist.equals("")) {
                            outToClient.writeUTF("NO MATCH");
                        } else {
                            outToClient.writeUTF(threadlist + "\n");
                        }
                    }
		        } else if (line[0].equals("MSG")) {
		             		        
		            //thread titles (argument 2) must be a single word
		            if (line.length < 3) {
		                outToClient.writeUTF("You must input a thread title and a message");
		            } else {
		             // check that the file already exists or not 
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (!exists) {
                            outToClient.writeUTF("Thread does not exist");
                        } else {
                            // count number of lines in thread
                            BufferedReader threadReader = new BufferedReader(new FileReader(line[1]));
                            int lines = 0;
                            String threadLines;
                            while ((threadLines = threadReader.readLine()) != null) {
                                String[] splitted = threadLines.split(" ");
                                if (splitted.length > 2) {
                                    if (!splitted[1].equals("uploaded")) {
                                        lines++;
                                    }
                                }
                                else {
                                    lines++;
                                }
                            }
                            threadReader.close();
                            // create the start of the line 
                            
                            String newLine = lines + " " + username + ":";  
                            // write in the line (starts from third argument)
                            FileWriter threadWriter = new FileWriter(line[1], true);
                            // write each word in the line                   
                            for (int i = 2; i < line.length; i++) {
                                newLine = newLine + " " + line[i];
                            }
                            newLine = newLine + " " + "\n";
                            threadWriter.write(newLine);
                            threadWriter.close();
                            outToClient.writeUTF("Message has been posted");
                        }
		            }
		        } else if (line[0].equals("DLT")) {
		             //thread titles (argument 2) must be a single word
		            if (line.length != 3) {
		                outToClient.writeUTF("You must input a thread title and a number");
		            } else {
		             // check that the file already exists or not 
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (!exists) {
                            outToClient.writeUTF("Thread does not exist");
                        } else {
                            // Read all lines into the file 
                            
                            ArrayList<String> threadMessages = new ArrayList<>();
                            BufferedReader threadReader = new BufferedReader(new FileReader(line[1]));
                            String threadLine = threadReader.readLine();
                            while (threadLine != null) {
                                threadMessages.add(threadLine);
                                threadLine =  threadReader.readLine();
                            } 
                            threadReader.close();
                            
                            // check if message number exists (line[2]) and if user has posted name
                            
                            String removingLine = "";
                            
                            boolean numberFound = false;
                            boolean rightUser = false;
                            for (int i = 0; i < threadMessages.size(); i++) {
                                String[] splitted = threadMessages.get(i).split(" ");
                                if (line[2].equals(splitted[0])) {
                                    numberFound = true;
                                    removingLine = threadMessages.get(i);
                                    if ((username + ":").equals(splitted[1])) {
                                        rightUser = true;
                                    }
                                }
                            }
                            
                            if (!numberFound) {
                                outToClient.writeUTF("Message number does not exist");
                            } else if (!rightUser) {
                                outToClient.writeUTF("You do not have permission to delete the message");
                            } else {
                                // we begin removing the line. Solution thanks to https://stackoverflow.com/questions/1377279/find-a-line-in-a-file-and-remove-it
                                File tempFile = new File("tempFile.txt");

                                BufferedReader reader = new BufferedReader(new FileReader(line[1]));
                                FileWriter writer = new FileWriter(tempFile, true);
                                String threadLine1 = "";
                                
                                int inc = 0;
                                
                                while((threadLine1 = reader.readLine()) != null) {
                                    // compare line with linetoremove (we dont print it out)
                                    System.out.println(threadLine1);
                                    if (threadLine1.equals(removingLine)) {
                                        System.out.println("DELETE");
                                        continue;
                                    }
                                    String[] splitted = threadLine1.split(" ");
                                    
                                    // the author name
                                    if (inc == 0) {
                                        writer.write(threadLine1 + "\n");
                                        // the other messages in the thread
                                    } else {    
                                        String newLine = "";
                                        // if its an uploaded line we print it out exactly as it was 
                                        if (splitted.length > 2) {
                                            newLine = Integer.toString(inc);
                                            for (int i = 1; i < splitted.length; i++) {
                                                newLine = newLine + " " + splitted[i];
                                                inc++;
                                            }
                                        } else if (splitted[1].equals("uploaded")) {
                                            for (int i = 0; i < splitted.length; i++) {
                                                if (i == 0) {
                                                    newLine = splitted[i];
                                                } else {
                                                    newLine = newLine + " " + splitted[i];
                                                }
                                            }
                                        } else {
                                            newLine = Integer.toString(inc);
                                            for (int i = 1; i < splitted.length; i++) {
                                                newLine = newLine + " " + splitted[i];
                                                inc++;
                                            }
                                        }
                                        newLine = newLine + "\n";
                                        writer.write(newLine);
                                    }
                                }
                                writer.close(); 
                                reader.close(); 
                                File f1 = new File(line[1]);
                                boolean successful = tempFile.renameTo(f1);
                                outToClient.writeUTF("Message has been deleted");
                            }
                        }
		            }
		        } else if (line[0].equals("RDT")) {
                     if (line.length != 2) {
		                outToClient.writeUTF("ERROR");
		            } else {
		             // check that the file already exists or not 
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (!exists) {
                            outToClient.writeUTF("NO MATCH");
                        } else {
                            BufferedReader reader2 = new BufferedReader(new FileReader(line[1]));
                            String threadlines = "";
                            String threadLine3 = "";
                            while ((threadLine3 = reader2.readLine()) != null) {
                                threadlines = threadlines + threadLine3 + "@newline@";
                            }
                            outToClient.writeUTF(threadlines);
                        }
                    }
		        } else if (line[0].equals("EDT")) {
		            if (line.length < 4) {
		                outToClient.writeUTF("You must input a thread title, a message number and a message");
		            } else {
		             // check that the file already exists or not 
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (!exists) {
                            outToClient.writeUTF("Thread does not exist");
                        } else {
                            // Read all lines into the file 
                            
                            ArrayList<String> threadMessages = new ArrayList<>();
                            BufferedReader threadReader = new BufferedReader(new FileReader(line[1]));
                            String threadLine = threadReader.readLine();
                            while (threadLine != null) {
                                threadMessages.add(threadLine);
                                threadLine =  threadReader.readLine();
                            } 
                            threadReader.close();
                            
                            // check if message number exists (line[2]) and if user has posted name
                            
                            String changingLine = "";
                            
                            boolean numberFound = false;
                            boolean rightUser = false;
                            for (int i = 0; i < threadMessages.size(); i++) {
                                String[] splitted = threadMessages.get(i).split(" ");
                                if (line[2].equals(splitted[0])) {
                                    numberFound = true;
                                    changingLine = threadMessages.get(i);
                                    if ((username + ":").equals(splitted[1])) {
                                        rightUser = true;
                                    }
                                }
                            }
                            
                            if (!numberFound) {
                                outToClient.writeUTF("Message number does not exist");
                            } else if (!rightUser) {
                                outToClient.writeUTF("You do not have permission to edit the message");
                            } else {
                                File tempFile1 = new File("tempFile.txt");
                                BufferedReader reader1 = new BufferedReader(new FileReader(line[1]));
                                FileWriter writer1 = new FileWriter(tempFile1, true);
                                String threadLine2 = "";
                                while((threadLine2 = reader1.readLine()) != null) {
                                    // compare line with changingline 
                                    System.out.println(threadLine2);
                                    
                                    // we construct the line
                                    if (threadLine2.equals(changingLine)) {
                                        System.out.println("CHANGE");
                                        String[] splitted = threadLine2.split(" "); 
                                        String newline2 = splitted[0] + " " + splitted[1];
                                        for (int i = 3; i < line.length; i++ ) {
                                            newline2 = newline2 + " " + line[i];
                                        }
                                        newline2 = newline2 + "\n";
                                        writer1.write(newline2);
                                    } else {
                                        writer1.write(threadLine2 + "\n");
                                    }
                                }
                                writer1.close(); 
                                reader1.close(); 
                                File f2 = new File(line[1]);
                                boolean successful1 = tempFile1.renameTo(f2);
                                outToClient.writeUTF("Message has been editted");
                                
                            }
                        }
                    }
		        } else if (line[0].equals("UPD")) { //https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
		            if (line.length != 3) {
		                outToClient.writeUTF("ERROR");
		            } else {
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (!exists) {
                            outToClient.writeUTF("NO MATCH");
                        } else {
                            outToClient.writeUTF("MATCH");
                            File f1 = new File(line[1] + "-" + line[2]);
                            FileOutputStream upl = new FileOutputStream(f1, true);
                            
                            byte[] buffer = new byte[8192];
                            int count;
                            count = inFromClient.read(buffer);
                            upl.write(buffer, 0, count);
                            upl.close();
                            //write the new entry in the file
                            String newupl = username + " uploaded " + line[2];
                            System.out.println(newupl);
                            FileWriter threadWriter22 = new FileWriter(line[1], true);               
                            threadWriter22.write(newupl + "\n");
                            threadWriter22.close();
                            outToClient.writeUTF("SUCCESS");
                        }
                    }
                    
		        } else if (line[0].equals("DWN")) {
		             if (line.length != 3) {
		                outToClient.writeUTF("ERROR");
		            } else {
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (!exists) {
                            outToClient.writeUTF("NO MATCH");
                        } else {
                        
                        // store the contents of the file into an array 
                            ArrayList<String> threadMessages23 = new ArrayList<>();
                            BufferedReader threadReader23 = new BufferedReader(new FileReader(line[1]));
                            String threadLine23 = threadReader23.readLine();
                            while (threadLine23 != null) {
                                threadMessages23.add(threadLine23);
                                threadLine23 =  threadReader23.readLine();
                            } 
                            threadReader23.close();
                            
                            boolean found = false;
                            for (int i = 0; i < threadMessages23.size(); i++) {
                                String[] splitted = threadMessages23.get(i).split(" ");
                                // prvents out of bound errors
                                if (splitted.length > 2) {
                                    if (line[2].equals(splitted[1])) {
                                        found = true;
                                    }
                                }
                            }
                            
                            if (found == false) {
                                outToClient.writeUTF("NO MATCH");
                            } else {
                                outToClient.writeUTF("MATCH");
                                File f1 = new File(line[1] + "-" + line[2]);
		                        InputStream fileReader = new FileInputStream(f1);

		                        byte[] buffers = new byte[8192];
		                        int counts;
                                counts = fileReader.read(buffers);
                                outToClient.write(buffers, 0, counts);
                                
		                        fileReader.close();
		                        outToClient.writeUTF("SUCCESS");
                            }
                        }
                    }
		        } else if (line[0].equals("RMV")) {
		            if (line.length != 2) {
		                outToClient.writeUTF("You must input a thread title");
		            } else {
		             // check that the file already exists or not 
                        File newThread = new File(line[1]);
                        boolean exists = newThread.exists();
                        if (!exists) {
                            outToClient.writeUTF("Thread does not exist");
                        } else {
                            // REad first line into file
                            BufferedReader threadReader1 = new BufferedReader(new FileReader(line[1]));
                            String threadLine1 = threadReader1.readLine();
                            threadReader1.close();
                            
                            // check if first line is same as user
                            System.out.println(threadLine1);
                            if (threadLine1.equals(username)) {
                                
                                // removing associated files with the thread
                                ArrayList<String> threadMessages24 = new ArrayList<>();
                                BufferedReader threadReader24 = new BufferedReader(new FileReader(line[1]));
                                String threadLine24 = threadReader24.readLine();
                                while (threadLine24 != null) {
                                    threadMessages24.add(threadLine24);
                                    threadLine24 =  threadReader24.readLine();
                                } 
                                threadReader24.close();
                                
                                for (int i = 0; i < threadMessages24.size(); i++) {
                                   String[] splitted = threadMessages24.get(i).split(" ");
                                   if (splitted.length > 1) {
                                       if (splitted[1].equals("uploaded")) {
                                           File upldelete1 = new File(line[1] + "-" + splitted[2]);
                                           upldelete1.delete();
                                       }
                                   }
                                }
                                File fff = new File(line[1]);
                                fff.delete();
                                outToClient.writeUTF("Thread removed");
                                threads.remove(line[1]);
                            } else {
                                outToClient.writeUTF("You do not have sufficient permissions");
                            }
                        }
                    }
		        } else if (line[0].equals("XIT")) {
		            outToClient.writeUTF("Goodbye");
		            connectionSocket.close();
		            logged = false;
		            username = "not logged";
		            password = "not logged";
		            users.remove(username);
		            break;
		        } else if (line[0].equals("SHT")) {
		            if (line.length != 2) {
		                outToClient.writeUTF("You must input the admin password");
		            } else {
		                if (!line[1].equals(args[1])) {
		                    outToClient.writeUTF("Admin password is wrong");
		                } else {
		                    // delete everything 
		                    outToClient.writeUTF("Server shutting down");
		                    for (int i = 0; i < threads.size(); i++ ) {
		                        // delete all files in threads array 
		                        // start by deleteing all files associated with a thread
                                ArrayList<String> threadMessages23 = new ArrayList<>();
                                BufferedReader threadReader23 = new BufferedReader(new FileReader(threads.get(i)));
                                String threadLine23 = threadReader23.readLine();
                                while (threadLine23 != null) {
                                    threadMessages23.add(threadLine23);
                                    threadLine23 =  threadReader23.readLine();
                                } 
                                threadReader23.close();
                                
                                for (int j = 0; j < threadMessages23.size(); j++) {
                                   String[] splitted = threadMessages23.get(j).split(" ");
                                   if (splitted.length > 1) {
                                       if (splitted[1].equals("uploaded")) {
                                           File upldelete = new File(threads.get(i) + "-" + splitted[2]);
                                           upldelete.delete();
                                       }
                                   }
                                }
		                        File delete = new File(threads.get(i));
		                        delete.delete();
		                    }
		                    // shut down the server 
		                    System.exit(0);
		                }
		            }
		        } else {
		            outToClient.writeUTF("Invalid Command");
		        }
	        }
            
            /*In this program, after sending the capitalized sentence to the client, we close the connection socket. But since welcomeSocket remains open, another client can now knock on the door and send the server a sentence to modify.
             */
             connectionSocket.close();
		} // end of while (true)

	} // end of main()

    // checks if the password exists in creds
    private static boolean checkPass(String pass, ArrayList<String> creds) {
         for (int i = 0; i < creds.size(); i++) {
            String[] splitted = creds.get(i).split(" ");
            if (pass.equals(splitted[1])) {
                return true;
            }
        }
        return false;
    }
    
    // checks if the username exists in creds
    private static boolean checkUser(String user, ArrayList<String> creds) {
        for (int i = 0; i < creds.size(); i++) {
            String[] splitted = creds.get(i).split(" ");
            if (user.equals(splitted[0])) {
                return true;
            }
        }
        return false;
    }
    
    // checks if theres a space in the string
    private static boolean checkSpace(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
} // end of class TCPServer
