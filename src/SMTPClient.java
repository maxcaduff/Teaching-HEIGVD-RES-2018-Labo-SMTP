import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created on 24.04.18.
 *
 * @author Max
 */
public class SMTPClient {

    protected Socket clientSocket ;
    protected BufferedReader reader;
    protected PrintWriter writer;

    protected ArrayList<String> emails = new ArrayList<String>() ;
    protected ArrayList<String> texts = new ArrayList<String>() ;


    // when the client starts, it automatically parses the files in the folder, so it is directly usable.
    public SMTPClient() {
        parseFiles();

    }
    // some can call parseFiles() again to reload the content of files if it has changed.
    public void parseFiles() {

        try {

            BufferedReader emailsReader = new BufferedReader( new FileReader( new File("emails.txt" ))) ;
            BufferedReader textsReader = new BufferedReader( new FileReader( new File("texts.txt")));
            String line ;

            //reading emails list
            while ((line = emailsReader.readLine()) != null) {

                //emails should match regexp: alphanum + @ + alphanum + . + domain [not implemented]
                emails.add(line);
            }

            //parsing texts; they should be separated by "\n.\n"
            do {
                String message = "";
                while ((line = textsReader.readLine()) != null && !line.equals(".") ) {
                    message += line + "\r\n" ;
                }
                texts.add(message);

            } while (line != null) ;

            //shuffling emails and texts to get different results each time
            Collections.shuffle(emails);
            Collections.shuffle(texts);
        }
        catch (Exception e) {

            System.out.println("Error reading files.");
            e.printStackTrace();
        }

    }


    public void connect(String server, int port) throws IOException {

//        maybe: check if socket already exists, -> disconnect.
        clientSocket = new Socket(server, port) ;

        reader = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()) );
        writer = new PrintWriter( new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8" ) );

        System.out.println(reader.readLine());

    }


    public void sendMails (int numberOfGroups) {
        try {
            //groups size should be equal or bigger than 3 to have at least 2 different receivers.
            if (numberOfGroups * 3 > emails.size()) {
                numberOfGroups = emails.size() / 3;
                System.out.println("Groups should be made of at least 3 people. number of groups set to: " + numberOfGroups);
            }

            //being polite and reading a polite answer
            writer.print("EHLO server \r\n");
            writer.flush();

            String line ;
            do {
                System.out.println(line = reader.readLine());
            } while ( line.startsWith("250-"));

            //two loops to iterate over the different senders and receivers
            int emailNr = 0;
            for (int i = 0; i < numberOfGroups; i++) {

                //address "sending" the mail
                writer.print("MAIL FROM: " + emails.get(emailNr) + "\r\n");
                writer.flush();
                int saveSender = emailNr ++;
                System.out.println(reader.readLine());

                //trick to manage groups of different sizes
                for (int j = 0; j < Math.floorDiv(emails.size(), numberOfGroups) - (i < (emails.size() % numberOfGroups) ? 0 : 1); j++) {
                    writer.print("RCPT TO: " + emails.get(emailNr) + "\r\n");
                    writer.flush();
                    emailNr++;
                    System.out.println(reader.readLine());
                }

                //sending header of emails
                writer.print("DATA\r\n");
                writer.flush();
                System.out.println(reader.readLine());
                writer.print("Content-Type: text/html; charset=UTF-8\r\nfrom: " + emails.get(saveSender) + "\r\nto: ");
                saveSender++;

                while (saveSender < emailNr) {
                    writer.print(emails.get(saveSender) + (saveSender + 1 < emailNr ? ", " : "") );
                    saveSender ++;
                }
                writer.print("\r\n");

                //sending body
                writer.print(texts.get(i % texts.size()) + "\r\n");

//                System.out.println(texts.get(i % texts.size()));

                //finished!
                writer.println(".\r\n");
                writer.flush();
                System.out.println(reader.readLine());
            }
        }
        catch (Exception e) {
            System.out.println("Problem sending emails.");
        }
    }


    public void disconnect() throws IOException {

        if (clientSocket.isConnected()) {
            writer.print("QUIT\r\n");
            writer.flush();
            System.out.println(reader.readLine());
            clientSocket.close();
        }
    }




    //main program, creates a client, connects to the specified server and sends funny mails
    public static void main (String[] args) {

        SMTPClient client = new SMTPClient();

        try {
            String defaultAddr = "192.168.99.100";
            int defaultPort = 5555;

            client.connect( (args.length < 2) ? defaultAddr : args[1],
                            (args.length < 3) ? defaultPort : Integer.valueOf(args[2]));
        } catch (IOException e) {
            System.out.println("couldn't connect to server.\n");
            e.printStackTrace();
        }

        int defaultGroups = 3;
        client.sendMails( (args.length < 1) ? defaultGroups : Integer.valueOf(args[0]) );

        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
