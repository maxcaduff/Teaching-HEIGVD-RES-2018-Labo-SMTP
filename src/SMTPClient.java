import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
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



}
