
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author bddra
 */
public class Main {

  private HashMap<String, Ouder> ouders;
  private HashMap<String, Student> studenten;
  private HashMap<Integer, School> scholen;
  private HashMap<Integer, ToewijzingsAanvraag> toewijzingsaanvragen;
  private TijdSchema tijdschema;
  private TypeGebruiker typeGebruiker;

  private final String ADMIN_ACCOUNT = "admin";
  private final String ADMIN_PASS = "admin";
  private final LocalDateTime START_DATUM = LocalDateTime.of(
          2018, Month.DECEMBER, 1, 0, 0, 0);//start inschrijvingen
  private final LocalDateTime CAPACITEIT_DEADLINE = LocalDateTime.of(
          2018, Month.DECEMBER, 20, 0, 0, 0);//start inschrijvingen
  private LocalDateTime huidigDeadline = LocalDateTime.of(
          2018, Month.DECEMBER, 30, 0, 0, 0);//dynamische deadline, die na elke 'sorteerronde' een andere waarde aanneemt 
  private final LocalDateTime EIND_DATUM = LocalDateTime.of(
          2019, Month.JANUARY, 30, 0, 0, 0);//einde periode 

  public Main() {
    try {
      studenten = DBStudent.getStudenten();
      ouders = DBOuder.getOuders();
      scholen = DBSchool.getScholen();
      toewijzingsaanvragen = DBToewijzingsAanvraag.getToewijzingsAanvragen();
    } catch(DBException dbe) {
      
    } finally {
      this.typeGebruiker = TypeGebruiker.NULL;
    }
  }

  public static void main(String[] args) throws Exception {
    UI ui = new UI(new Main());
    ui.setVisible(true);
  }  
    
  public TypeGebruiker inloggen(String gebrnaam, char[] wachtwoord) {
    String wwoord = "";
    for(char c : wachtwoord) {
      wwoord += c;
    }
    
    try {
    ouders = DBOuder.getOuders();
    Ouder ouder = null;
    for(Ouder o : ouders.values()) {
      if(o.getGebruikersnaam().equals(gebrnaam) && o.getWachtwoord().equals(wwoord)) {
        ouder = o;
        break;
      }
    }
      if(ouder != null) {
        typeGebruiker = TypeGebruiker.OUDER;
      }
      if(gebrnaam.equals(ADMIN_ACCOUNT) && wwoord.equals(ADMIN_PASS)) 
	typeGebruiker = TypeGebruiker.ADMIN;
    } catch (DBException dbe) {
      typeGebruiker = TypeGebruiker.NULL;
      dbe.getMessage();
      return typeGebruiker;
    } 
    return typeGebruiker;
  }

  /*
   * Methode voor het activeren van een account
   */
  public boolean activeren(String rnouder) {
    boolean geactiveerd = false;
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                 + "abcdefghijklmnopqrstuvwxyz"
                 + "0123456789";
    String wachtwoord = new Random().ints(8, 0, chars.length())
                .mapToObj(i -> "" + chars.charAt(i))
                .collect(Collectors.joining());
    try {
      Ouder ouder = DBOuder.getOuder(rnouder);
      tijdschema = DBTijdSchema.getTijdSchema(Year.of(LocalDateTime.now().getYear()));
      ouders.put(ouder.getRijksregisterNummer(), ouder);
      if (ouder.getWachtwoord().equals("")) {
        ouder.setWachtwoord(wachtwoord);
        String[] ontvangers = {ouder.getEmail()};
        Email email = new Email(ouder, TypeBericht.ACTIVATIE, tijdschema);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        executor.execute(() -> {
          try {
            DBOuder.bewaarOuder(ouders.get(rnouder));
            email.send();
          } catch (DBException | MessagingException e) {
            e.getMessage();
          } catch (IOException ex) {
            ex.getMessage();
          }
        });
        geactiveerd = true;
      }
    } catch (DBException dbe) {
      dbe.getMessage();
    } catch (Exception e) {
      e.getMessage();
    }
    return geactiveerd;
  }

  /*
   * Methode voor het indienen van een aanvraag 
   */
  public boolean addAanvraag(ToewijzingsAanvraag ta) {
    try {
      if(DBToewijzingsAanvraag.voegNieuweAanvraagToe(ta)) {
        toewijzingsaanvragen.put(ta.getToewijzingsAanvraagNummer(), ta);
        return true;
      } else {
        return false;
      }
    } catch(DBException dbe) {
      dbe.getMessage();
      return false;
    }
  }
  
  /*
   * Methode voor het verwijderen van een aanvraag 
   */
  public boolean verwijderAanvraag(int nummer) {
    ToewijzingsAanvraag ta = toewijzingsaanvragen.remove(nummer);
    if(ta != null) {
      try {
	DBToewijzingsAanvraag.verwijder(nummer);
      } catch (DBException dbe) {
	dbe.getMessage();
      }
    }
    return ta != null;
  }
  
  /*
   * Methode voor het berekenen van de key van een nieuwe aanvraag
   */
  public int keyNieuweAanvraag() {
    try {
      return DBToewijzingsAanvraag.getNextKey();
    } catch (DBException dbe) {
      dbe.getMessage();
      return 0;
    }
  }

  public int controleerCapaciteit() {
    
    try {
      tijdschema = DBTijdSchema.getTijdSchema(Year.of(LocalDateTime.now().getYear()));
      if(LocalDateTime.now().isBefore(tijdschema.getCapaciteitDeadline())) {
        toewijzingsaanvragen = DBToewijzingsAanvraag.getToewijzingsAanvragen();
        scholen = DBSchool.getScholen();
        int globaleCapaciteit = 0;
        for(School s : scholen.values()) {
          globaleCapaciteit += s.getPlaatsen();
        }
        if(toewijzingsaanvragen.size() > globaleCapaciteit) {
          String emails = "";
          for(School s : scholen.values()) {
            emails += s.getEmail() + ",";
          }
          emails = emails.substring(0, emails.length()-1);
          Email email = new Email(emails, TypeBericht.AANBODUITBREIDING, tijdschema);
          sendEmail(email);
          return 1;
        } else {
          return 2;
        }
      } else {
        return 3;
      }
    } catch(DBException dbe) {
      dbe.getMessage();
      return 0;
    }
  }
  /*
   * Methode voor het ophalen van een ouder op basis van 
   * zijn inloggegevens
   */
  public Ouder ophalenOuder(String gebrnaam, char[] pass) {
    Ouder o = null;
    String wachtwoord = "";
    for (char c : pass) {
      wachtwoord += c;
    }
    for (Ouder ouder : ouders.values()) {
      if (ouder.getGebruikersnaam().equals(gebrnaam)
              && wachtwoord.equals(ouder.getWachtwoord())) {
        o = ouder;
      }
    }
    return o;
  }

  public Ouder ophalenOuder(String rnouder) {
    try {
      return DBOuder.getOuder(rnouder);
    } catch (DBException dbe) {
      dbe.getMessage();
      return null;
    }
  }
  
  /*
     * Methode voor het ophalen van een student op basis van 
     * zijn rijksregisternummer
   */
  public Student ophalenStudent(String rnstudent) {
    Student s;
    try {
      s = DBStudent.getStudent(rnstudent);
      return s;
    } catch (DBException dbe) {
      dbe.getMessage();
      return null;
    }
  }

  /*
     * Methode voor het ophalen van de studenten die bij een ouder 
     * horen en niet op een school zitten op basis van zijn rijksregisternummer
   */
  public ArrayList<Student> ophalenKinderen(String rnouder) {
    ArrayList<Student> studentenVanOuder;
    try {
      studentenVanOuder = DBOuder.getStudentenVanOuder(rnouder);
      return studentenVanOuder;
    } catch (DBException dbe) {
      dbe.getMessage();
      return null;
    }
  }
  
  /*
   * Methode voor het ophalen van een school op basis van
   * zijn ID
   */
  public School ophalenSchool(int ID) {
    School s = null;
    try {
      s = DBSchool.getSchool(ID);
      return s;
    } catch (DBException dbe) {
      dbe.getMessage();
      return s;     
    }
  }
  
  public School[] ophalenScholen() {
    try {
      scholen = DBSchool.getScholen();
      School[] scholenArray = new School[scholen.size()];
      int i = 0;
      for (School s : scholen.values()) {
        scholenArray[i] = s;
        i++;
      }
      return scholenArray;
    } catch (DBException dbe) {
      dbe.getMessage();
      return null;
    }
  }
  
  public ToewijzingsAanvraag ophalenAanvraag(String rnstudent) {
    try {
      return DBToewijzingsAanvraag.getAanvraag(rnstudent);
    } catch (DBException dbe) {
      dbe.getMessage();
    }
    return null;
  }
  
  public TijdSchema ophalenTijdSchema(int jaar) {
    try {
      tijdschema = DBTijdSchema.getTijdSchema(Year.of(jaar));
      return tijdschema;
    } catch (DBException dbe) {
      dbe.getMessage();
    }
    return null;
  }
  
  public void veranderTijdSchema(TijdSchema ts) {
    try {
      tijdschema = DBTijdSchema.getTijdSchema(Year.of(LocalDateTime.now().getYear()));
      DBTijdSchema.setTijdSchema(ts);
      if(tijdschema.getStartDatum() == null) {
        ouders = DBOuder.getOuders();
        String emails = "";
        for(Ouder o : ouders.values()) {
          emails += o.getEmail() + ",";
        }
        emails = emails.substring(0, emails.length()-1);
        Email email = new Email(emails,TypeBericht.START,ts);
        sendEmail(email);
      } else {
        ouders = DBOuder.getOuders();
        scholen = DBSchool.getScholen();
        String emails = "";
        for(Ouder o : ouders.values())
          emails += o.getEmail() + ",";
        for(School s : scholen.values())
          emails += s.getEmail() + ",";
        Email email = new Email(emails, TypeBericht.TIJDSCHEMA, ts);
        sendEmail(email);
      }
    } catch (DBException dbe) {
      dbe.getMessage();
    }
    try {
      ouders = DBOuder.getOuders();
      scholen = DBSchool.getScholen();
      String emails = "";
      for(Ouder o : ouders.values())
        emails += o.getEmail() + ";";
      for(School s : scholen.values())
        emails += s.getEmail() + ";";
      Email email = new Email(emails, TypeBericht.TIJDSCHEMA, ts);
      sendEmail(email);
    } catch (DBException dbe) {
      dbe.getMessage();
    }
  }
  
  public void veranderCapaciteit(int ID, int nieuweCap){
      try {
          DBSchool.setCapaciteit(ID, nieuweCap);
      }
      catch (DBException dbe){
          dbe.getMessage();
      }
  }
  
  /*
   * Methode voor het indienen of aanpassen van een voorkeur
   * De methode past zijn gedrag door de datum te vergelelijken
   * met de deadlines voor het indienen 
   */
   public int indienenVoorkeur(int nummer, Student student, int schoolID) {
    try {
      toewijzingsaanvragen = DBToewijzingsAanvraag.getToewijzingsAanvragen();
      scholen = DBSchool.getScholen();
      ToewijzingsAanvraag ta = toewijzingsaanvragen.get(nummer);
      int vorigVoorkeur = 0;
      if(ta != null)
        vorigVoorkeur = ta.getVoorkeur();
      if(ta.getStatus() == Status.VOORLOPIG) {
        return -1;
      } else if (ta.schoolIsAfgewezen(schoolID)) {
        return 0;
      } else if(vorigVoorkeur != schoolID) {
        ta.setVoorkeur(schoolID);
        ta.setStatus(Status.INGEDIEND);
        scholen.get(schoolID).getWachtLijst().add(ta);
        if(vorigVoorkeur != 0) 
          scholen.get(vorigVoorkeur).getWachtLijst().remove(ta);
        int aantal = getBroersEnZussen(nummer, student, schoolID);
        ta.setBroersOfZussen(aantal);
        long preferentie = bepaalPreferentie(ta);
        ta.setPreferentie(preferentie);
        for(ToewijzingsAanvraag twa : toewijzingsaanvragen.values()) {
          if(twa.getRijksregisterNummerOuder().equals(student.getRijksregisterNummerOuder())
             && !twa.getRijksregisterNummerStudent().equals(student.getRijksregisterNummer())
             && twa.getVoorkeur() == schoolID) {
            twa.setBroersOfZussen(twa.getBroersOfZussen()+1);
            long pref = bepaalPreferentie(twa);
            twa.setPreferentie(pref);
          }
        }
        DBToewijzingsAanvraag.bewaarToewijzingsAanvragen(toewijzingsaanvragen);
        return 1;
      } else if (vorigVoorkeur == schoolID) {
        return 2;
      } else if (ta.getStatus() == Status.DEFINITIEF) {
        return 3;
      }
    } catch (DBException dbe) {
      dbe.getMessage();
    } catch (Exception e) {
      e.getMessage();
    } 
    return -2;
  }
   
  public int getBroersEnZussen(int aanvraagnummer, Student student, int voorkeurschool) {
    int aantal = 0;
      //hashmaps zijn al geladen in methode indienenVoorkeur()
      for(Student s : studenten.values()) {
        if(!s.getRijksregisterNummer().equals(student.getRijksregisterNummer())
          && s.getRijksregisterNummerOuder().equals(student.getRijksregisterNummerOuder())
          && s.getHuidigeSchool() == voorkeurschool) {
              aantal++;
        }
      }
      for(ToewijzingsAanvraag ta : toewijzingsaanvragen.values()) {
        if(!ta.getRijksregisterNummerStudent().equals(student.getRijksregisterNummer())
           && ta.getRijksregisterNummerOuder().equals(student.getRijksregisterNummerOuder())
           && ta.getVoorkeur() == voorkeurschool) {
              aantal++;
        }
      }
      return aantal;
  }
  
  public boolean exporteerWachtlijsten() {
    try {
      TijdSchema ts = ophalenTijdSchema(LocalDateTime.now().getYear());
      if(!toewijzen()) {
        toewijzingsaanvragen = DBToewijzingsAanvraag.getToewijzingsAanvragen();
        scholen = DBSchool.getScholen();
        ouders = DBOuder.getOuders();
        studenten = DBStudent.getStudenten();
        String emails = "";
        for (School s : scholen.values()) {
          String csvWachtLijst = "Toewijzingsaanvraagnummer,Volledige naam,Rijksregisternummer student,Email ouder\n";
          if(!s.getWachtLijst().isEmpty()){
            String[] twnums = s.csvFormatLijst().split(";");
            for(String num : twnums) {
              String rnstudent = toewijzingsaanvragen.get(Integer.parseInt(num)).getRijksregisterNummerStudent();
              String rnouder = toewijzingsaanvragen.get(Integer.parseInt(num)).getRijksregisterNummerOuder();
              String naamSt = studenten.get(rnstudent).getNaam() + " " + studenten.get(rnstudent).getVoornaam();
              String emailOuder = ouders.get(rnouder).getEmail();
              csvWachtLijst += num + "," + naamSt + "," + rnstudent + "," + emailOuder + "\n";
            }
          }
          TextBestand.opslaanWachtLijst(s, csvWachtLijst);
          Email email = new Email(s, TypeBericht.EINDESCHOOL);
          sendEmail(email);
        }
      } else {
        throw new DBException("Probleem met toewijzen!");
      }
      return true;
    } catch (DBException dbe) {
      dbe.getMessage();
      return false;
    } catch (TXTException txte) {
      txte.getMessage();
      return false;
    } 
  }
  
  public ArrayList<String> laadWachtLijst(int ID) {
    try {
      ArrayList<String> csv = TextBestand.laadWachtLijst(DBSchool.getSchool(ID));
      
    return csv;
    } catch (TXTException | DBException txte) {
      txte.getMessage();
      return null;
    }
  }
  
  public void sorteerWachtLijst(ArrayList<ToewijzingsAanvraag> wachtLijst) {
      for (int i = 0; i < wachtLijst.size(); i++) {
	  ToewijzingsAanvraag lagerePref = wachtLijst.get(i);
	  ToewijzingsAanvraag hogerePref = null;
	  int lagePrefIndex = i;
	  for (int j = i + 1; j < wachtLijst.size(); j++) {
	      long preferentie;
	      if (hogerePref == null) {
		  preferentie = bepaalPreferentie(wachtLijst.get(i));
	      } else {
		  preferentie = bepaalPreferentie(hogerePref);
	      }
	      long preferentie2 = bepaalPreferentie(wachtLijst.get(j));
	      if (preferentie < preferentie2) {
		  hogerePref = wachtLijst.get(j);
		  lagePrefIndex = j;
	      }
	  }
	  if (hogerePref != null) {
	      wachtLijst.set(i, hogerePref);
	      wachtLijst.set(lagePrefIndex, lagerePref);
	  }
      }
  }
  
  public boolean toewijzen() {
      int delayHuidigDeadline = 10;
      int afgewezenStudenten = 0;
      boolean succes = false;
      try {
        tijdschema = DBTijdSchema.getTijdSchema(Year.of(LocalDateTime.now().getYear()));
        toewijzingsaanvragen = DBToewijzingsAanvraag.getToewijzingsAanvragen();
        studenten = DBStudent.getStudenten();
        scholen = DBSchool.getScholen();
        LocalDateTime deadlineInBegin = tijdschema.getHuidigDeadline();
        tijdschema.setHuidigDeadline(tijdschema.getHuidigDeadline().plusMinutes(delayHuidigDeadline));
        if(tijdschema.getHuidigDeadline().isAfter(tijdschema.getEindDatum()))
          tijdschema.setHuidigDeadline(tijdschema.getEindDatum());
        
        
        succes = true;
        for(ToewijzingsAanvraag ta : toewijzingsaanvragen.values()) {
          if(ta.getStatus() != Status.DEFINITIEF) {
            HashMap<Integer,School> legeScholen = new HashMap();
            for(School s : scholen.values()) {
              if(s.getWachtLijst().size() < s.getPlaatsen()) {
                legeScholen.put(s.getID(), s);
              }
            }
            if(ta.getVoorkeur() == 0 && legeScholen.size() > 0) {
              Random r = new Random();
              ArrayList<Integer> keys = new ArrayList(legeScholen.keySet());
              int i = keys.get(r.nextInt(legeScholen.size()));
              indienenVoorkeur(ta.getToewijzingsAanvraagNummer(), studenten.get(ta.getRijksregisterNummerStudent()), i);
            }
          } 
        }
        for (School s : scholen.values()) {
          ArrayList<ToewijzingsAanvraag> wachtLijst = s.getWachtLijst();
          boolean isDefinitief = false;
          for(ToewijzingsAanvraag ta : wachtLijst) {
            if(ta.getStatus().equals(Status.DEFINITIEF))
              isDefinitief = true;
          }
          sorteerWachtLijst(wachtLijst);
          if(isDefinitief) {
            succes = false;
          } else {
            //studenten afwijzen indien de school vol zit en emails sturen
            while (wachtLijst.size() > s.getPlaatsen()) {
              ToewijzingsAanvraag twa = wachtLijst.get(wachtLijst.size() - 1); 
              twa = toewijzingsaanvragen.get(twa.getToewijzingsAanvraagNummer());
              Ouder ouder = ouders.get(twa.getRijksregisterNummerOuder());
              wachtLijst.remove(twa);
              if(twa.getAfgewezenScholen().equals(""))
                twa.setAfgewezenScholen(String.valueOf(twa.getVoorkeur()));
              else
                twa.setAfgewezenScholen(twa.getAfgewezenScholen() + ";" + twa.getVoorkeur());
              twa.setStatus(Status.ONTWERP);
              twa.setVoorkeur(0);
              twa.setPreferentie(0);
              afgewezenStudenten++;
              if(LocalDateTime.now().isBefore(tijdschema.getEindDatum())) {
                Email email = new Email(ouder, TypeBericht.VOORKEUR, tijdschema);
                sendEmail(email);
              }
            }
          }
          s.setWachtLijst(wachtLijst);
        }
        if(afgewezenStudenten > 0 && LocalDateTime.now().isBefore(tijdschema.getEindDatum())){
          updateStatus(Status.VOORLOPIG);
        } else {
          tijdschema.setEindDatum(deadlineInBegin);
          tijdschema.setHuidigDeadline(deadlineInBegin);
          for (ToewijzingsAanvraag ta : toewijzingsaanvragen.values()) 
	    ta.setStatus(Status.DEFINITIEF);
          String emails = "";
          for(Ouder o : ouders.values()) {
            emails += o.getEmail() + ",";
          }
          emails = emails.substring(0, emails.length()-1);
          Email email = new Email(emails, TypeBericht.EINDEOUDER, tijdschema);
          sendEmail(email);
        }
        for(ToewijzingsAanvraag ta : toewijzingsaanvragen.values()) {
          ta.setPreferentie(this.bepaalPreferentie(ta));
        }
        DBTijdSchema.setTijdSchema(tijdschema);
        DBToewijzingsAanvraag.bewaarToewijzingsAanvragen(toewijzingsaanvragen);
      } catch(DBException dbe) {
        dbe.getMessage();
      }
      return succes;        
  }
  
  public void sendEmail(Email email) {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
              executor.execute(() -> {
                try {
                  email.send();
                } catch (MessagingException e) {
                  e.getMessage();
                } catch (IOException ex) {
                  ex.getMessage();
                }
              });
  }
  public void updateStatus(Status s) {
    for (ToewijzingsAanvraag ta : toewijzingsaanvragen.values()) {
	  if (!ta.getStatus().equals(Status.ONTWERP))
	    ta.setStatus(s);
    }
  }
  
  public long bepaalPreferentie(ToewijzingsAanvraag ta) {
      long preferentie = huidigDeadline.toEpochSecond(ZoneOffset.UTC)
	      - ta.getAanmeldingsTijdstip().toEpochSecond(ZoneOffset.UTC);
      long bonusPunten = huidigDeadline.toEpochSecond(ZoneOffset.UTC)
	      - START_DATUM.toEpochSecond(ZoneOffset.UTC);
      if (ta.getBroersOfZussen() > 0) {
	  preferentie += bonusPunten*ta.getBroersOfZussen();
      }
      return preferentie/100000;

  }

  public boolean schoolIsAfgewezen(int schoolID, int nummer) {
      try {
        toewijzingsaanvragen = DBToewijzingsAanvraag.getToewijzingsAanvragen();
        String afgScholen = toewijzingsaanvragen.get(nummer).getAfgewezenScholen();
        String[] afgewezenScholen = afgScholen.split(";");
        if(afgScholen.contains(String.valueOf(schoolID)))
          return true;
      } catch (Exception ex) {
        System.out.println("Error: " + ex);
        return false;
      }
      return false;
  }

  public Periode huidigPeriode() {
    try {
      TijdSchema ts = DBTijdSchema.getTijdSchema(Year.of(LocalDateTime.now().getYear()));
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime sd = ts.getStartDatum();
      LocalDateTime idl = ts.getInschrijvingenDeadline();
      LocalDateTime cpd = ts.getCapaciteitDeadline();
      LocalDateTime ed = ts.getEindDatum();
      if(now.isAfter(sd) && now.isBefore(idl))
        return Periode.INSCHR;
      else if (now.isAfter(idl) && now.isBefore(cpd))
        return Periode.CAP;
      else if (now.isAfter(cpd) && now.isBefore(ed))
        return Periode.VOORKEUR;
      else if (now.isAfter(ed) && now.isBefore(ed.plusDays(15)))
        return Periode.EINDE;
      else
        return Periode.NULL;
    } catch (DBException dbe) {
      dbe.getMessage();
      return Periode.NULL;
    } catch (Exception e) {
      return Periode.NULL;
    }
  }
  
}

