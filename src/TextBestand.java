
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bddra
 */
public class TextBestand {
  public static void opslaanWachtLijst (School s, String wachtLijst) 
          throws TXTException {
            String bestandNaam = s.getID() + ".csv";
            File file = new File(bestandNaam);
            PrintWriter writer = null;
            try {
              if(file.exists())
                file.delete();
              file.createNewFile();
              writer = new PrintWriter(file);
              String[] lines = wachtLijst.split("\\r?\\n");
              for(String line : lines) 
                writer.println(line);
              // closing writer connection 
              writer.close(); 
            } catch (IOException e) {
              e.printStackTrace();
              throw new TXTException(e);
            }
  }
  
  public static ArrayList<String> laadWachtLijst (School s) throws TXTException {
    String bestandNaam = s.getID() + ".csv";
    ArrayList<String> lines = new ArrayList();
            File file = new File(bestandNaam);
            String csv = null;
            try {
              Scanner input = new Scanner(file);
              int i = 0;
              while(input.hasNextLine()) {
                if(i != 0) 
                  lines.add(input.nextLine());
                i++;
              }
              return lines;
            } catch (IOException e) {
              e.printStackTrace();
              throw new TXTException(e);
            }
  }
}
