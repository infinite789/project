
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
 
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
 
/**
 * @author Boris Dragnev, Victor Masscho, Jean Janssens, Edith Lust, Job van Lambalgen
 *
 */
public class Email {
    private final String host = "smtp.gmail.com"; 
    private final String port = "587";
    private final String ontvangers;
    private String subject;
    private StringBuffer body;
    private Map<String, String> mapInlineImages;
    private TypeBericht type;
    private String attachFile;
    
    private final String EMAIL_KLANTENDIENST = "klantendienstsct@gmail.com";
    private final String PASS_EMAIL = "centraletoewijzing";
  
    public Email(Ouder o, TypeBericht type, TijdSchema ts) {
      this.type = type;
      this.ontvangers = o.getEmail();
      if(null != type) switch (type) {
        case ACTIVATIE:
          this.subject = "Inloggegevens voor de dienst centrale toewijzing";
          this.body = new StringBuffer("<html>Beste " + o.getVoornaam() + ", <br>"
                                    + "<br>Je kan vanaf nu inloggen op onze website "
                                    + "met de volgende gegevens:<br>"
                                    + "<br>Gebruikersnaam: " + o.getGebruikersnaam()
                                    + "<br>Wachtwoord: " + o.getWachtwoord() + "<br>"
                                    + "<br>Met vriendelijke groeten,<br><br>"
                                    + "<br>Systeem Centrale Toewijzing"
                                    + "<br>Klantendienst</html>");
          break;
        case VOORKEUR:
          this.subject = "Nieuwe voorkeur ingeven";
          this.body = new StringBuffer("<html>Beste " + o.getVoornaam() + ", <br>"
                                    + "<br>Je kind werd afgewezen van de school waarvoor u gekozen heeft."
                                    + "<br>Indien je vergeten was een voorkeur in te geven, probeerden we je"
                                    + "<br>op de wachtlijst van een willekeurige school te zetten."
                                    + "<br>Ga naar onze website om uw volgende voorkeur in te geven. "
                                    + "<br>U heeft daarvoor de tijd tot: " + ts.getHuidigDeadline().toString().replace("T", " ") + "<br>"
                                    + "<br>Met vriendelijke groeten,<br><br>"
                                    + "<br>Systeem Centrale Toewijzing"
                                    + "<br>Klantendienst</html>");
          break;
          case EINDEOUDER:
          this.subject = "Einde toewijzingsperiode";
          this.body = new StringBuffer("<html>Beste " + o.getVoornaam() + ", <br>"
                                    + "<br>De periode voor de toewijzing is voorbij!"
                                    + "<br>Je kan de status van je aanvraag raadplegen in je profiel.<br>"
                                    + "<br>Met vriendelijke groeten,<br><br>"
                                    + "<br>Systeem Centrale Toewijzing"
                                    + "<br>Klantendienst</html>");
          break;
        default:
          break;
      }
      this.body.append("<img src=\"cid:image1\" width=\"30%\" height=\"30%\" /><br>");
      // inline images
      this.mapInlineImages = new HashMap<>();
      mapInlineImages.put("image1", "./logo.png"); 
    }
    
    public Email(String emails, TypeBericht type, TijdSchema ts) {
      this.ontvangers = emails;
      this.type = type;
      if(null != type) switch (type) {
        case EINDEOUDER:
          this.subject = "De toewijzingsperiode is afgelopen";
          this.body = new StringBuffer("<html>Beste ouder, <br>"
                  + "<br>Bedankt om gebruik te maken van onze dienst!<br>"
                  + "<br>Je kan je aanvraag online bekijken. Indien u vragen heeft "
                  + "<br>i.v.m. de beslissing kan je ze mailen naar klantendienstsct@gmail.com."
                  + "<br>Vermeld het aanvraagnummer a.u.b. <br>"
                  + "<br>Met vriendelijke groeten,<br><br>"
                  + "<br>Systeem Centrale Toewijzing"
                  + "<br>Klantendienst</html>");
          break;
        case AANBODUITBREIDING:
          this.subject = "Globaal aanbodtekort";
          this.body = new StringBuffer("<html>Beste school, <br>"
                  + "<br>Dit jaar stellen we een aanbodtekort vast!<br>"
                  + "<br>Indien u bereid bent om meer studenten op te nemen, gelieve ons"
                  + "<br>uw capaciteit te mailen naar klantendienstsct@gmail.com."
                  + "<br>Vemeld a.u.b uw id. U moet dit doen vóór: "
                  + "<br>" + ts.getCapaciteitDeadline().toString().replace("T", " ") + "<br>"
                          + "<br>Met vriendelijke groeten,<br><br>"
                          + "<br>Systeem Centrale Toewijzing"
                          + "<br>Klantendienst</html>");
          break;
        case TIJDSCHEMA:
          this.subject = "Aanpassing van onze tijdschema";
          this.body = new StringBuffer("<html>Beste, <br>"
                  + "<br>Bij deze willen we u verwittigen dat onze tijdsplanning"
                  + "<br>is aangepast. Hieronder de nieuwe planning: "
                  + "<br>Startdatum: " + ts.getStartDatum().toString().replace("T", " ")
                  + "<br>Deadline inschrijvingen: " + ts.getInschrijvingenDeadline().toString().replace("T", " ")
                  + "<br>Deadline capaciteitsuitbreiding: " + ts.getCapaciteitDeadline().toString().replace("T", " ") + "<br>"
                          + "<br>Einddatum: " + ts.getEindDatum().toString().replace("T", " ") + "<br>"
                                  + "<br>Met vriendelijke groeten,<br><br>"
                                  + "<br>Systeem Centrale Toewijzing"
                                  + "<br>Klantendienst</html>");
          break;
        case START:
          this.subject = "Start inschrijvingsperiode";
          this.body = new StringBuffer("<html>Beste ouder, <br>"
                  + "<br>De inschrijvingsperiode zal beginnen vanaf:"
                  + "<br>" + ts.getStartDatum().toString().replace("T", " ") + "<br>"
                          + "<br>Ga naar onze website om uw account te activeren."
                          + "<br>Dit is mogelijk tot: " + ts.getInschrijvingenDeadline().toString().replace("T", " ") + "<br>"
                                  + "<br>Met vriendelijke groeten,<br><br>"
                                  + "<br>Systeem Centrale Toewijzing"
                                  + "<br>Klantendienst</html>");
          break;
        default:
          break;
      }
      this.body.append("<img src=\"cid:image1\" width=\"30%\" height=\"30%\" /><br>");
      // inline images
      this.mapInlineImages = new HashMap<>();
      mapInlineImages.put("image1", "./logo.png"); 
    }
    
    public Email(School s, TypeBericht type) {
      this.ontvangers = s.getEmail();
      this.type = type;
      if(type == TypeBericht.EINDESCHOOL) {
        this.subject = "De toewijzingsperiode is afgelopen";
        this.body = new StringBuffer("<html>Beste school, <br>"
                                   + "<br>Bedankt om gebruik te maken van onze dienst!<br>"
                                   + "<br>In de bijlage vindt u de leerlingen die u toegewezen kreeg. " 
                                   + "<br>Met vriendelijke groeten,<br><br>"
                                   + "<br>Systeem Centrale Toewijzing"
                                   + "<br>Klantendienst</html>");
      }
      attachFile = s.getID() + ".csv";
      this.body.append("<img src=\"cid:image1\" width=\"30%\" height=\"30%\" /><br>");
      // inline images
      this.mapInlineImages = new HashMap<>();
      mapInlineImages.put("image1", "./logo.png"); 
    }
    
    public void send() throws AddressException, MessagingException, IOException {
        
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", EMAIL_KLANTENDIENST);
        properties.put("mail.password", PASS_EMAIL);
 
        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_KLANTENDIENST, PASS_EMAIL);
            }
        };
        Session session = Session.getInstance(properties, auth);
 
        // creates a new e-mail message
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(EMAIL_KLANTENDIENST));
        msg.setContent(body, "text/html");
        ArrayList<String> receivers = new ArrayList();
        String[] strs = ontvangers.split(",");
        for(String str : strs) {
          if(receivers.isEmpty())
            receivers.add(str);
          if(!receivers.contains(str))
            receivers.add(str);
        }
        String[] rec = new String[receivers.size()];
        int n = 0;
        for(String str : receivers) {
          rec[0] = str;
          n++;
        }
        InternetAddress[] addresses = new InternetAddress[rec.length];
        for(int i = 0; i < rec.length; i++) {
          addresses[i] = new InternetAddress(rec[i]);
        }
        msg.addRecipients(Message.RecipientType.CC, addresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
 
        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body.toString(), "text/html");
        
        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        
        // adds inline image attachments
        if (mapInlineImages != null && mapInlineImages.size() > 0) {
            Set<String> setImageID = mapInlineImages.keySet();
             
            for (String contentId : setImageID) {
                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.setHeader("Content-ID", "<" + contentId + ">");
                imagePart.setDisposition(MimeBodyPart.INLINE);
                 
                String imageFilePath = mapInlineImages.get(contentId);
                try {
                    imagePart.attachFile(imageFilePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
 
                multipart.addBodyPart(imagePart);
            }
        }
        
        //adds file
        if(attachFile != null) {
          MimeBodyPart attachPart = new MimeBodyPart();
          attachPart.attachFile(attachFile);
          multipart.addBodyPart(attachPart);
        }
        msg.setContent(multipart);
 
        Transport.send(msg);
    }
}