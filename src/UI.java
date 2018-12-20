
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.time.Year;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.Timer;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

/*
 * User Interface Klasse
 * De gebruiker activeert zijn account door zijn rijksregisternummer
 * in te geven en ontvangt per email zijn gegevens. (simulatie van de werking
 * van een websitedienst van de overheid)
 * Na inloggen met de juiste gegevens krijgt de ouder toegang tot zijn
 * elektronische documenten 
 * Onder de 'Aanmeldingsformulier'-tab wordt een aanvraag ingediend,
 * een voorkeur kan aangepast worden onder de 'Voorkeurformulier'-tab 
 * en onder de 'Aanvragen raadplegen'-tab wordt informatie gegeven
 * over de lopende aanvragen
 *
 * Een admin mag ook inloggen en krijgt een andere interface te zien.
 * Hij heeft toegang tot 'Beheer scholen'-tab, 'Beheer wachtlijsten'-tab en 'Beheer data'-tab
 *
 * @author Boris Dragnev, Victor Masscho, Jean Janssens, Edith Lust, Job van Lambalgen
 */
public class UI extends javax.swing.JFrame  {
  
    private Main main;
    private Ouder gebruiker;
    private DefaultTableModel dtm;
    private School[] scholenData;
    
    public final int delayHuidigDL = 10;  //huidig deadline wordt met die waarde aangepast na een sorteerronde
    
    public UI(Main main) {
      this.main = main; 
      initComponents(); //componenten van NetBeans GuiBuilder initializeren
      createJDatePickers();
      datePickerLabel1.setVisible(false);
      datePickerLabel2.setVisible(false);
      datePickerLabel3.setVisible(false);
      datePickerLabel4.setVisible(false);
      getContentPane().setBackground(Color.white); //wit achtergrond van de Pane
      InlogScherm.getRootPane().setDefaultButton(inlogKnopIS);
      Timer timer = new Timer( 5000, new ActionListener() {
        @Override
        public void actionPerformed( ActionEvent e ) {
          voorwaardelijkOpmaak();
        }
      } );
      timer.setRepeats( true );
      timer.start();
    }
    
    /*
     * Methode dat bepaalt wat wordt getoond op het scherm naargelang de periode.
     * 
     */
    public final void voorwaardelijkOpmaak() {
      String inschrNietBegonnen = "<html>De inschrijvingsperiode<br>is nog niet begonnen!</html>";
      String capNietContr = "<html>Je mag de capaciteit<br>niet controleren.</html>";
      String geenAanvragen = "<html>Er zijn nog geen<br>aanvragen!</html>";
      String inschrBezig = "<html>De inschrijvingsperiode<br>is nog bezig!</html>";
      String inshcrVoorbij = "De inschrijvingsperiode is voorbij!";
      String exportEnVerzend = "<html>De periode is voorbij!"
                              + "<br>Exporteer de wachtlijsten en "
                              + "<br>verzend ze naar de scholen.</html>";
      Periode periode = main.huidigPeriode();
      TijdSchema ts = main.ophalenTijdSchema(LocalDateTime.now().getYear());
      setDataLabelsText(ts);
      if(null != periode) switch (periode) {
        case NULL:
          activeerKnopAS.setEnabled(false);
          indienenKnopAFT.setEnabled(false);
          indienenKnopVFT.setEnabled(false);
          boodschapLabelAS.setText(inschrNietBegonnen);
          boodschapCapAdmin.setText(capNietContr);
          boodschapLabelAdminWTab.setText(inschrNietBegonnen);
          exporteerKnopAdmin.setEnabled(false);
          sorteerKnopAdmin.setEnabled(false);
          laadLijstKnop.setEnabled(false);
          controleerCapKnop.setEnabled(false);
          //beheer data
          if(JTPAdmin.getSelectedIndex()== 1) {
            datePickerLabel1.setVisible(false);
            datePickerLabel2.setVisible(false);
            datePickerLabel3.setVisible(false);
            datePickerLabel4.setVisible(false);
            timeSD.setEnabled(true);      
            timeIDL.setEnabled(true);
            timeCDL.setEnabled(true);      
            timeED.setEnabled(true);
            datePickerSD.setVisible(true);
            datePickerIDL.setVisible(true);
            datePickerCDL.setVisible(true);
            datePickerED.setVisible(true);
          }
          break;
        case INSCHR:
          activeerKnopAS.setEnabled(true);
          indienenKnopAFT.setEnabled(true);
          if(boodschapLabelAS.getText().equals(inschrNietBegonnen))
            boodschapLabelAS.setText("");
          if(boodschapCapAdmin.getText().equals(capNietContr))
            boodschapCapAdmin.setText("");
          boodschapLabelAdminWTab.setText(inschrBezig);
          exporteerKnopAdmin.setEnabled(false);
          sorteerKnopAdmin.setEnabled(false);
          indienenKnopVFT.setEnabled(false);
          controleerCapKnop.setEnabled(false);
          //beheer data
          if(JTPAdmin.getSelectedIndex()== 1) {
            datePickerLabel1.setVisible(true);
            datePickerLabel2.setVisible(false);
            datePickerLabel3.setVisible(false);
            datePickerLabel4.setVisible(false);
            timeSD.setEnabled(false);      
            timeIDL.setEnabled(true);
            timeCDL.setEnabled(true);      
            timeED.setEnabled(true);
            datePickerSD.setVisible(false);
            datePickerIDL.setVisible(true);
            datePickerCDL.setVisible(true);
            datePickerED.setVisible(true);
          }
          break;
        case CAP:
          indienenKnopAFT.setEnabled(false);
          activeerKnopAS.setEnabled(false);
          exporteerKnopAdmin.setEnabled(false);
          boodschapLabelAS.setText(inshcrVoorbij);
          if(boodschapCapAdmin.getText().equals(capNietContr))
            boodschapCapAdmin.setText("");
          boodschapLabelAdminWTab.setText(geenAanvragen);
          sorteerKnopAdmin.setEnabled(false);
          indienenKnopVFT.setEnabled(false);
          controleerCapKnop.setEnabled(true);
          //beheer data
          if(JTPAdmin.getSelectedIndex()== 1) {
            datePickerLabel1.setVisible(true);
            datePickerLabel2.setVisible(true);
            datePickerLabel3.setVisible(false);
            datePickerLabel4.setVisible(false);
            timeSD.setEnabled(false);      
            timeIDL.setEnabled(false);
            timeCDL.setEnabled(true);      
            timeED.setEnabled(true);
            datePickerSD.setVisible(false);
            datePickerIDL.setVisible(false);
            datePickerCDL.setVisible(true);
            datePickerED.setVisible(true);
          }
          break;
        case VOORKEUR:
          indienenKnopAFT.setEnabled(false);
          activeerKnopAS.setEnabled(false);
          boodschapLabelAS.setText(inshcrVoorbij);
          boodschapCapAdmin.setText(capNietContr);
          if(boodschapLabelAdminWTab.getText().equals(geenAanvragen)) {
            boodschapLabelAdminWTab.setText("");
          }
          exporteerKnopAdmin.setEnabled(false);
          sorteerKnopAdmin.setEnabled(true);
          indienenKnopVFT.setEnabled(true);
          controleerCapKnop.setEnabled(false);
          //beheer data
          if(JTPAdmin.getSelectedIndex()== 1) {
            datePickerLabel1.setVisible(true);
            datePickerLabel2.setVisible(true);
            datePickerLabel3.setVisible(true);
            datePickerLabel4.setVisible(false);
            timeSD.setEnabled(false);      
            timeIDL.setEnabled(false);
            timeCDL.setEnabled(false);      
            timeED.setEnabled(true);
            datePickerSD.setVisible(false);
            datePickerIDL.setVisible(false);
            datePickerCDL.setVisible(false);
            datePickerED.setVisible(true);
          }
          break;
        case EINDE:
          indienenKnopAFT.setEnabled(false);
          activeerKnopAS.setEnabled(false);
          boodschapLabelAS.setText("");
          exporteerKnopAdmin.setEnabled(true);
          boodschapLabelAS.setText(inshcrVoorbij);
          boodschapCapAdmin.setText(capNietContr);
          if(boodschapLabelAdminWTab.getText().equals(""))
            boodschapLabelAdminWTab.setText(exportEnVerzend);
          sorteerKnopAdmin.setEnabled(false);
          indienenKnopVFT.setEnabled(false);
          controleerCapKnop.setEnabled(false);
          //beheer data
          if(JTPAdmin.getSelectedIndex()== 1) {
            datePickerLabel1.setVisible(true);
            datePickerLabel2.setVisible(true);
            datePickerLabel3.setVisible(true);
            datePickerLabel4.setVisible(true);
            timeSD.setEnabled(false);      
            timeIDL.setEnabled(false);
            timeCDL.setEnabled(false);      
            timeED.setEnabled(false);
            datePickerSD.setVisible(false);
            datePickerIDL.setVisible(false);
            datePickerCDL.setVisible(false);
            datePickerED.setVisible(false);
          }
          break;
        default:
          break;
      }
    }
   
    /*
     * Hulpmethode voor bovenstaande methode, bepaalt de tekst van de labels in tab 'data beheer'
     * 
     */
    public void setDataLabelsText(TijdSchema ts) {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
      if(ts.getStartDatum() == null) {
        deadlinesBoodschap.setText("<html>Er is nog geen<br>tijdschema ingesteld.</html>");
        startDatumLabel.setText("");
        inschrijvingenDLLabel.setText("");
        capaciteitDLLabel.setText("");
        huidigDLLabel.setText("");
        eindDatumLabel.setText("");
      } else {
        deadlinesBoodschap.setText("");
        startDatumLabel.setText(dtf.format(ts.getStartDatum()));
        inschrijvingenDLLabel.setText(dtf.format(ts.getInschrijvingenDeadline()));
        capaciteitDLLabel.setText(dtf.format(ts.getCapaciteitDeadline()));
        huidigDLLabel.setText(dtf.format(ts.getHuidigDeadline()));
        eindDatumLabel.setText(dtf.format(ts.getEindDatum()));
      }
    }
    
    /*
     * Methode voor de datumkiezers in tab 'beheer data"
     * 
     */
    public final void createJDatePickers() {
      Properties p = new Properties();
      p.put("text.today", "today");
      p.put("text.month", "month");
      p.put("text.year", "year");
      datePickerSD.removeAll();
      datePickerIDL.removeAll();
      datePickerCDL.removeAll();
      datePickerED.removeAll();
      //datumPicker voor de startDatum
      UtilDateModel model1 = new UtilDateModel();
      JDatePanelImpl datePanel1 = new JDatePanelImpl(model1, p);
      JDatePickerImpl startDatumPicker = new JDatePickerImpl(datePanel1, new DateLabelFormatter());
      datePickerSD.add((JComponent)startDatumPicker);
      //datumPicker voor de inschrijvingen
      UtilDateModel model2 = new UtilDateModel();
      JDatePanelImpl datePanel2 = new JDatePanelImpl(model2, p);
      JDatePickerImpl inschrijvingenDL = new JDatePickerImpl(datePanel2, new DateLabelFormatter());
      datePickerIDL.add((JComponent)inschrijvingenDL);
      //datumPicker voor de capaciteit 
      UtilDateModel model3 = new UtilDateModel();
      JDatePanelImpl datePanel3 = new JDatePanelImpl(model3, p);  
      JDatePickerImpl capaciteitDL = new JDatePickerImpl(datePanel3, new DateLabelFormatter());
      datePickerCDL.add((JComponent)capaciteitDL);
      //datumPicker voor de einddatum
      UtilDateModel model4 = new UtilDateModel();
      JDatePanelImpl datePanel4 = new JDatePanelImpl(model4, p);
      JDatePickerImpl eindDatumPicker = new JDatePickerImpl(datePanel4, new DateLabelFormatter());
      datePickerED.add((JComponent)eindDatumPicker);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    MainPanel = new javax.swing.JPanel();
    InlogScherm = new javax.swing.JPanel();
    gebrLabelAS = new javax.swing.JLabel();
    gebrVeldIS = new javax.swing.JTextField();
    passVeldIS = new javax.swing.JPasswordField();
    passLabelIS = new javax.swing.JLabel();
    inlogKnopIS = new javax.swing.JButton();
    activeerLinkLabelIS = new javax.swing.JLabel();
    boodschapLabelIS = new javax.swing.JLabel();
    doorgaanKnopIS = new javax.swing.JButton();
    logoLabel = new javax.swing.JLabel();
    ActiveerScherm = new javax.swing.JPanel();
    gegevensOuderAS = new javax.swing.JPanel();
    naamLabelAS = new javax.swing.JLabel();
    voornaamLabelAS = new javax.swing.JLabel();
    rijksnumLabelAS = new javax.swing.JLabel();
    emailLabelAS = new javax.swing.JLabel();
    adresLabelAS = new javax.swing.JLabel();
    rijksnumVeldAS = new javax.swing.JTextField();
    naamVeldAS = new javax.swing.JTextField();
    voornaamVeldAS = new javax.swing.JTextField();
    emailVeldAS = new javax.swing.JTextField();
    adresVeldAS = new javax.swing.JTextField();
    terugLinkLabelAS = new javax.swing.JLabel();
    activeerKnopAS = new javax.swing.JButton();
    boodschapLabelAS = new javax.swing.JLabel();
    logoLabel1 = new javax.swing.JLabel();
    FormulierScherm = new javax.swing.JTabbedPane();
    HomeTab = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    jPanel4 = new javax.swing.JPanel();
    AlgemeneTekstOuders = new javax.swing.JLabel();
    PersoonlijkeJlabel = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    AanmeldingsFormulierTab = new javax.swing.JPanel();
    indienenKnopAFT = new javax.swing.JButton();
    gegevensStudentAFT = new javax.swing.JPanel();
    naamStudentLabelAFT = new javax.swing.JLabel();
    voornaamStudentLabelAFT = new javax.swing.JLabel();
    rijksnumStudentLabelAFT = new javax.swing.JLabel();
    telnumLabelAFT = new javax.swing.JLabel();
    naamStudentVeldAFT = new javax.swing.JTextField();
    voornaamStudentVeldAFT = new javax.swing.JTextField();
    telnumVeldAFT = new javax.swing.JTextField();
    studentenDropBoxAFT = new javax.swing.JComboBox<>();
    gegevensOuderAFT = new javax.swing.JPanel();
    naamOuderLabelAFT = new javax.swing.JLabel();
    voornaamOuderLabelAFT = new javax.swing.JLabel();
    rijksnumOuderLabelAFT = new javax.swing.JLabel();
    emailLabelAFT = new javax.swing.JLabel();
    adresLabelAFT = new javax.swing.JLabel();
    rijksnumOuderVeldAFT = new javax.swing.JTextField();
    naamOuderVeldAFT = new javax.swing.JTextField();
    voornaamOuderVeldAFT = new javax.swing.JTextField();
    emailVeldAFT = new javax.swing.JTextField();
    adresVeldAFT = new javax.swing.JTextField();
    boodschapLabelAFT = new javax.swing.JLabel();
    waarschuwingLabelAFT = new javax.swing.JLabel();
    VoorkeurFormulierTab = new javax.swing.JPanel();
    zoekwoordLabel = new javax.swing.JLabel();
    zoekwoordVeld = new javax.swing.JTextField();
    indienenKnopVFT = new javax.swing.JButton();
    selectieBoodschapLabel = new javax.swing.JLabel();
    rijksnumStudentLabelVFT = new javax.swing.JLabel();
    infoLabelVFT = new javax.swing.JLabel();
    scholenScrollPane = new javax.swing.JScrollPane();
    scholenTabel = new javax.swing.JTable();
    boodschapLabelVFT = new javax.swing.JLabel();
    studentenDropBoxVFT = new javax.swing.JComboBox<>();
    aanvraagNummerLabelVFT = new javax.swing.JLabel();
    aanvraagnummerVeldVFT = new javax.swing.JTextField();
    ZoekAanvraagTab = new javax.swing.JPanel();
    rijksnumStudentLabelART = new javax.swing.JLabel();
    infoLabelART = new javax.swing.JLabel();
    gegevensPanelART = new javax.swing.JPanel();
    eersteVoorkeurLabel = new javax.swing.JLabel();
    aanvraagnummerLabelOut = new javax.swing.JLabel();
    statusLabelOut = new javax.swing.JLabel();
    tijdstipLabelOut = new javax.swing.JLabel();
    eersteVoorkeurLabelOut = new javax.swing.JLabel();
    aanvraagnummerLabelART = new javax.swing.JLabel();
    statusLabel = new javax.swing.JLabel();
    tijdstipLabel = new javax.swing.JLabel();
    studentenDropBoxART = new javax.swing.JComboBox<>();
    verwijderLinkLabelART = new javax.swing.JLabel();
    boodschapLabelART = new javax.swing.JLabel();
    UitloggenTab = new javax.swing.JPanel();
    uitloggenLinkLabel = new javax.swing.JLabel();
    AdminScherm = new javax.swing.JPanel();
    JTPAdmin = new javax.swing.JTabbedPane();
    scholenTab = new javax.swing.JPanel();
    scholenScrollPaneAdmin = new javax.swing.JScrollPane();
    scholenTabelAdmin = new javax.swing.JTable();
    instructieLabelAdminScholenTab = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    zoekwoordVeldAdmin = new javax.swing.JTextField();
    nieuweCapaciteitVeld = new javax.swing.JTextField();
    veranderCapKnop = new javax.swing.JButton();
    zoekwoordLabelAdmin = new javax.swing.JLabel();
    nieuweCapaciteitLabel = new javax.swing.JLabel();
    jLabel10 = new javax.swing.JLabel();
    controleerCapKnop = new javax.swing.JButton();
    jLabel9 = new javax.swing.JLabel();
    boodschapCapAdmin = new javax.swing.JLabel();
    schoolGegevensPanel = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    jLabel8 = new javax.swing.JLabel();
    jTextField2 = new javax.swing.JTextField();
    jTextField3 = new javax.swing.JTextField();
    jTextField4 = new javax.swing.JTextField();
    jTextField5 = new javax.swing.JTextField();
    jTextField6 = new javax.swing.JTextField();
    tijdSchemaTab = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    toepassenTijdSchemaKnop = new javax.swing.JButton();
    timeED = new javax.swing.JComboBox<>();
    timeCDL = new javax.swing.JComboBox<>();
    datePickerED = new javax.swing.JButton();
    datePickerCDL = new javax.swing.JButton();
    datePickerIDL = new javax.swing.JButton();
    datePickerSD = new javax.swing.JButton();
    timeIDL = new javax.swing.JComboBox<>();
    timeSD = new javax.swing.JComboBox<>();
    jLabel16 = new javax.swing.JLabel();
    jLabel14 = new javax.swing.JLabel();
    jLabel13 = new javax.swing.JLabel();
    jLabel12 = new javax.swing.JLabel();
    deadlinesBoodschap2 = new javax.swing.JLabel();
    datePickerLabel1 = new javax.swing.JLabel();
    datePickerLabel2 = new javax.swing.JLabel();
    datePickerLabel3 = new javax.swing.JLabel();
    datePickerLabel4 = new javax.swing.JLabel();
    jPanel3 = new javax.swing.JPanel();
    jLabel15 = new javax.swing.JLabel();
    jLabel17 = new javax.swing.JLabel();
    jLabel18 = new javax.swing.JLabel();
    jLabel19 = new javax.swing.JLabel();
    jLabel20 = new javax.swing.JLabel();
    startDatumLabel = new javax.swing.JLabel();
    inschrijvingenDLLabel = new javax.swing.JLabel();
    capaciteitDLLabel = new javax.swing.JLabel();
    eindDatumLabel = new javax.swing.JLabel();
    deadlinesBoodschap = new javax.swing.JLabel();
    jLabel21 = new javax.swing.JLabel();
    huidigDLLabel = new javax.swing.JLabel();
    wachtLijstenTab = new javax.swing.JPanel();
    editPanel = new javax.swing.JPanel();
    jPanel5 = new javax.swing.JPanel();
    exporteerKnopAdmin = new javax.swing.JButton();
    sorteerKnopAdmin = new javax.swing.JButton();
    boodschapLabelAdminWTab = new javax.swing.JLabel();
    leesPanel = new javax.swing.JPanel();
    lijstScrollPane = new javax.swing.JScrollPane();
    jList1 = new javax.swing.JList<>();
    laadLijstKnop = new javax.swing.JButton();
    idVeldLaadLijst = new javax.swing.JTextField();
    jLabel1 = new javax.swing.JLabel();
    TerugKnop2 = new javax.swing.JButton();
    uitlogAdminTab = new javax.swing.JPanel();
    uitloggenLinkLabel1 = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Centrale toewijzing leerlingen");
    setBackground(new java.awt.Color(255, 255, 255));
    setExtendedState(MAXIMIZED_BOTH);
    setSize(new java.awt.Dimension(0, 0));
    getContentPane().setLayout(new java.awt.GridBagLayout());

    MainPanel.setBackground(new java.awt.Color(255, 255, 255));
    MainPanel.setPreferredSize(new java.awt.Dimension(800, 800));
    MainPanel.setLayout(new java.awt.CardLayout());

    InlogScherm.setBackground(new java.awt.Color(255, 255, 255));
    InlogScherm.setPreferredSize(new java.awt.Dimension(800, 800));

    gebrLabelAS.setText("Gebruikersnaam:");

    passLabelIS.setBackground(new java.awt.Color(0, 0, 0));
    passLabelIS.setText("Wachtwoord:");

    inlogKnopIS.setText("Inloggen");
    inlogKnopIS.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        inlogKnopISActionPerformed(evt);
      }
    });

    activeerLinkLabelIS.setForeground(new java.awt.Color(0, 102, 255));
    activeerLinkLabelIS.setText("Uw account activeren");
    activeerLinkLabelIS.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        activeerLinkLabelISMouseClicked(evt);
      }
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        activeerLinkLabelISMouseEntered(evt);
      }
      public void mouseExited(java.awt.event.MouseEvent evt) {
        activeerLinkLabelISMouseExited(evt);
      }
    });

    boodschapLabelIS.setForeground(new java.awt.Color(153, 0, 0));
    boodschapLabelIS.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

    doorgaanKnopIS.setText("Doorgaan");
    doorgaanKnopIS.setEnabled(false);
    doorgaanKnopIS.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        doorgaanKnopISActionPerformed(evt);
      }
    });

    logoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo.png"))); // NOI18N

    javax.swing.GroupLayout InlogSchermLayout = new javax.swing.GroupLayout(InlogScherm);
    InlogScherm.setLayout(InlogSchermLayout);
    InlogSchermLayout.setHorizontalGroup(
      InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(InlogSchermLayout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(InlogSchermLayout.createSequentialGroup()
            .addComponent(boodschapLabelIS, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(doorgaanKnopIS))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, InlogSchermLayout.createSequentialGroup()
            .addComponent(activeerLinkLabelIS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(inlogKnopIS, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, InlogSchermLayout.createSequentialGroup()
            .addGroup(InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(gebrLabelAS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(passLabelIS, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(passVeldIS, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
              .addComponent(gebrVeldIS))))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      .addComponent(logoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    InlogSchermLayout.setVerticalGroup(
      InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(InlogSchermLayout.createSequentialGroup()
        .addGap(32, 32, 32)
        .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(26, 26, 26)
        .addGroup(InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(gebrLabelAS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(gebrVeldIS))
        .addGap(9, 9, 9)
        .addGroup(InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(passLabelIS, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(passVeldIS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(activeerLinkLabelIS)
          .addComponent(inlogKnopIS))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(InlogSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(boodschapLabelIS, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(doorgaanKnopIS))
        .addGap(263, 263, 263))
    );

    MainPanel.add(InlogScherm, "card5");

    ActiveerScherm.setBackground(new java.awt.Color(255, 255, 255));
    ActiveerScherm.setPreferredSize(new java.awt.Dimension(800, 800));

    gegevensOuderAS.setBackground(new java.awt.Color(255, 255, 255));
    gegevensOuderAS.setBorder(javax.swing.BorderFactory.createTitledBorder("Ouder"));

    naamLabelAS.setText("Naam:");

    voornaamLabelAS.setText("Voornaam:");

    rijksnumLabelAS.setText("Rijksregisternummer:");

    emailLabelAS.setText("E-mail:");

    adresLabelAS.setText("Adres:");

    rijksnumVeldAS.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        rijksnumVeldASKeyReleased(evt);
      }
    });

    naamVeldAS.setFocusable(false);

    voornaamVeldAS.setFocusable(false);

    emailVeldAS.setFocusable(false);

    adresVeldAS.setFocusable(false);

    javax.swing.GroupLayout gegevensOuderASLayout = new javax.swing.GroupLayout(gegevensOuderAS);
    gegevensOuderAS.setLayout(gegevensOuderASLayout);
    gegevensOuderASLayout.setHorizontalGroup(
      gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensOuderASLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(gegevensOuderASLayout.createSequentialGroup()
            .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(naamLabelAS)
              .addComponent(emailLabelAS)
              .addComponent(adresLabelAS))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(naamVeldAS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(emailVeldAS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(adresVeldAS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addGroup(gegevensOuderASLayout.createSequentialGroup()
            .addComponent(voornaamLabelAS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(voornaamVeldAS, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(gegevensOuderASLayout.createSequentialGroup()
            .addComponent(rijksnumLabelAS)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(rijksnumVeldAS, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    gegevensOuderASLayout.setVerticalGroup(
      gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensOuderASLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(rijksnumVeldAS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gegevensOuderASLayout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(rijksnumLabelAS)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(naamLabelAS)
          .addComponent(naamVeldAS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(voornaamLabelAS)
          .addComponent(voornaamVeldAS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(emailLabelAS)
          .addComponent(emailVeldAS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(gegevensOuderASLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(adresLabelAS)
          .addComponent(adresVeldAS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(35, Short.MAX_VALUE))
    );

    terugLinkLabelAS.setForeground(new java.awt.Color(0, 102, 255));
    terugLinkLabelAS.setText("Terug");
    terugLinkLabelAS.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        terugLinkLabelASMouseClicked(evt);
      }
      public void mouseExited(java.awt.event.MouseEvent evt) {
        terugLinkLabelASMouseExited(evt);
      }
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        terugLinkLabelASMouseEntered(evt);
      }
    });

    activeerKnopAS.setText("Activeren");
    activeerKnopAS.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        activeerKnopASActionPerformed(evt);
      }
    });

    boodschapLabelAS.setForeground(new java.awt.Color(153, 0, 0));

    logoLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    logoLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo.png"))); // NOI18N

    javax.swing.GroupLayout ActiveerSchermLayout = new javax.swing.GroupLayout(ActiveerScherm);
    ActiveerScherm.setLayout(ActiveerSchermLayout);
    ActiveerSchermLayout.setHorizontalGroup(
      ActiveerSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(ActiveerSchermLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(ActiveerSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(ActiveerSchermLayout.createSequentialGroup()
            .addGap(0, 244, Short.MAX_VALUE)
            .addGroup(ActiveerSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
              .addComponent(boodschapLabelAS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ActiveerSchermLayout.createSequentialGroup()
                .addComponent(terugLinkLabelAS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(activeerKnopAS))
              .addComponent(gegevensOuderAS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(0, 244, Short.MAX_VALUE))
          .addComponent(logoLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    ActiveerSchermLayout.setVerticalGroup(
      ActiveerSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ActiveerSchermLayout.createSequentialGroup()
        .addGap(38, 38, 38)
        .addComponent(logoLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(27, 27, 27)
        .addComponent(gegevensOuderAS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(boodschapLabelAS, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(ActiveerSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(terugLinkLabelAS)
          .addComponent(activeerKnopAS))
        .addContainerGap())
    );

    MainPanel.add(ActiveerScherm, "card4");

    FormulierScherm.setBackground(new java.awt.Color(255, 255, 255));
    FormulierScherm.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
    FormulierScherm.setPreferredSize(new java.awt.Dimension(800, 800));
    FormulierScherm.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        FormulierSchermStateChanged(evt);
      }
    });

    jPanel4.setBackground(new java.awt.Color(255, 255, 255));

    AlgemeneTekstOuders.setText("<html>Sinds een aantal jaren komt de vraag naar plaatsen voor het basisonderwijs<br/> \nniet overeen met het aanbod. Recent ziet men ook hetzelfde probleem bij<br/> \nplaatsen voor het middelbaar onderwijs. Beide problemen manifesteren<br/>\nzich voornamelijk in de grotere steden. Bijgevolg hebben we een online<br/> \ninformatiesysteem ontwikkeld om de centrale toewijzing van kinderen aan<br/>\nscholen te regelen. <br/>\n<br/>\nDe werking van ons informatiesysteem is grotendeels gebaseerd op de tijd<br/> \nvan aanmelding in het systeem, de school die geprefereerd wordt en of er<br/> \nal dan niet broers of zussen reeds op die school zitten. <br/>\n<br/>\nIn het volgende tabblad kan u uw aanmeldingsformulier invullen en<br/>\nvervolgens ook een voorkeursformulier. \n</html>");

    PersoonlijkeJlabel.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
    PersoonlijkeJlabel.setText("jLabel4");

    jButton1.setText("Volgende");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addGap(40, 40, 40)
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(jButton1)
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addComponent(PersoonlijkeJlabel)
            .addGap(316, 316, 316))
          .addComponent(AlgemeneTekstOuders))
        .addGap(0, 0, 0))
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addGap(12, 12, 12)
        .addComponent(PersoonlijkeJlabel)
        .addGap(18, 18, 18)
        .addComponent(AlgemeneTekstOuders, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addComponent(jButton1)
        .addContainerGap(889, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout HomeTabLayout = new javax.swing.GroupLayout(HomeTab);
    HomeTab.setLayout(HomeTabLayout);
    HomeTabLayout.setHorizontalGroup(
      HomeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(HomeTabLayout.createSequentialGroup()
        .addGap(67, 67, 67)
        .addComponent(jLabel2)
        .addContainerGap(724, Short.MAX_VALUE))
      .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    HomeTabLayout.setVerticalGroup(
      HomeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(HomeTabLayout.createSequentialGroup()
        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel2)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    FormulierScherm.addTab("Home", HomeTab);

    AanmeldingsFormulierTab.setBackground(new java.awt.Color(255, 255, 255));
    AanmeldingsFormulierTab.setBorder(javax.swing.BorderFactory.createTitledBorder("Student"));

    indienenKnopAFT.setText("Indienen");
    indienenKnopAFT.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        indienenKnopAFTActionPerformed(evt);
      }
    });

    gegevensStudentAFT.setBackground(new java.awt.Color(255, 255, 255));
    gegevensStudentAFT.setBorder(javax.swing.BorderFactory.createTitledBorder("Student"));

    naamStudentLabelAFT.setText("Naam:");

    voornaamStudentLabelAFT.setText("Voornaam:");

    rijksnumStudentLabelAFT.setText("Rijksregisternummer:");

    telnumLabelAFT.setText("Telefoonnummer:");

    naamStudentVeldAFT.setFocusable(false);

    voornaamStudentVeldAFT.setFocusable(false);

    telnumVeldAFT.setFocusable(false);

    studentenDropBoxAFT.setModel(new DefaultComboBoxModel());
    studentenDropBoxAFT.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        studentenDropBoxAFTItemStateChanged(evt);
      }
    });

    javax.swing.GroupLayout gegevensStudentAFTLayout = new javax.swing.GroupLayout(gegevensStudentAFT);
    gegevensStudentAFT.setLayout(gegevensStudentAFTLayout);
    gegevensStudentAFTLayout.setHorizontalGroup(
      gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensStudentAFTLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(naamStudentLabelAFT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(voornaamStudentLabelAFT, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(telnumLabelAFT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(rijksnumStudentLabelAFT))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(voornaamStudentVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(studentenDropBoxAFT, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(naamStudentVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(telnumVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(209, Short.MAX_VALUE))
    );
    gegevensStudentAFTLayout.setVerticalGroup(
      gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensStudentAFTLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(rijksnumStudentLabelAFT)
          .addComponent(studentenDropBoxAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(voornaamStudentLabelAFT)
          .addComponent(voornaamStudentVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(naamStudentLabelAFT)
          .addComponent(naamStudentVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensStudentAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(telnumLabelAFT)
          .addComponent(telnumVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    gegevensOuderAFT.setBackground(new java.awt.Color(255, 255, 255));
    gegevensOuderAFT.setBorder(javax.swing.BorderFactory.createTitledBorder("Ouder"));

    naamOuderLabelAFT.setText("Naam:");

    voornaamOuderLabelAFT.setText("Voornaam:");

    rijksnumOuderLabelAFT.setText("Rijksregisternummer:");

    emailLabelAFT.setText("E-mail:");

    adresLabelAFT.setText("Adres:");

    rijksnumOuderVeldAFT.setFocusable(false);

    naamOuderVeldAFT.setFocusable(false);

    voornaamOuderVeldAFT.setFocusable(false);

    emailVeldAFT.setFocusable(false);

    adresVeldAFT.setFocusable(false);

    javax.swing.GroupLayout gegevensOuderAFTLayout = new javax.swing.GroupLayout(gegevensOuderAFT);
    gegevensOuderAFT.setLayout(gegevensOuderAFTLayout);
    gegevensOuderAFTLayout.setHorizontalGroup(
      gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensOuderAFTLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
            .addComponent(emailLabelAFT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(naamOuderLabelAFT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(voornaamOuderLabelAFT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rijksnumOuderLabelAFT, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
          .addComponent(adresLabelAFT, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(voornaamOuderVeldAFT, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
          .addComponent(naamOuderVeldAFT)
          .addComponent(rijksnumOuderVeldAFT)
          .addComponent(emailVeldAFT)
          .addComponent(adresVeldAFT))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    gegevensOuderAFTLayout.setVerticalGroup(
      gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensOuderAFTLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(voornaamOuderLabelAFT)
          .addComponent(voornaamOuderVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(naamOuderVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(naamOuderLabelAFT))
        .addGap(18, 18, 18)
        .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(rijksnumOuderLabelAFT)
          .addComponent(rijksnumOuderVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(20, 20, 20)
        .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(emailLabelAFT)
          .addComponent(emailVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensOuderAFTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(adresLabelAFT)
          .addComponent(adresVeldAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 10, Short.MAX_VALUE))
    );

    boodschapLabelAFT.setForeground(new java.awt.Color(153, 0, 0));
    boodschapLabelAFT.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    waarschuwingLabelAFT.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
    waarschuwingLabelAFT.setText("Alle aanvragen dienen vóór 15 juli ingediend te worden!");

    javax.swing.GroupLayout AanmeldingsFormulierTabLayout = new javax.swing.GroupLayout(AanmeldingsFormulierTab);
    AanmeldingsFormulierTab.setLayout(AanmeldingsFormulierTabLayout);
    AanmeldingsFormulierTabLayout.setHorizontalGroup(
      AanmeldingsFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(AanmeldingsFormulierTabLayout.createSequentialGroup()
        .addGroup(AanmeldingsFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(AanmeldingsFormulierTabLayout.createSequentialGroup()
            .addGap(12, 12, 12)
            .addComponent(waarschuwingLabelAFT))
          .addGroup(AanmeldingsFormulierTabLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(AanmeldingsFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
              .addComponent(gegevensStudentAFT, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(indienenKnopAFT)
              .addComponent(gegevensOuderAFT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(boodschapLabelAFT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        .addContainerGap())
    );
    AanmeldingsFormulierTabLayout.setVerticalGroup(
      AanmeldingsFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(AanmeldingsFormulierTabLayout.createSequentialGroup()
        .addComponent(waarschuwingLabelAFT)
        .addGap(6, 6, 6)
        .addComponent(gegevensStudentAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(12, 12, 12)
        .addComponent(gegevensOuderAFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(boodschapLabelAFT, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(indienenKnopAFT)
        .addContainerGap())
    );

    FormulierScherm.addTab("Aanmeldingsformulier", AanmeldingsFormulierTab);

    VoorkeurFormulierTab.setBackground(new java.awt.Color(255, 255, 255));
    VoorkeurFormulierTab.setBorder(javax.swing.BorderFactory.createTitledBorder("School"));

    zoekwoordLabel.setText("Zoekwoord:");

    zoekwoordVeld.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        zoekwoordVeldKeyReleased(evt);
      }
    });

    indienenKnopVFT.setText("Indienen");
    indienenKnopVFT.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        indienenKnopVFTActionPerformed(evt);
      }
    });

    selectieBoodschapLabel.setForeground(new java.awt.Color(153, 0, 0));
    selectieBoodschapLabel.setText("Gelieve een school uit de bovenstaande lijst te selecteren.");
    selectieBoodschapLabel.setToolTipText("");
    selectieBoodschapLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    rijksnumStudentLabelVFT.setText("Rijksregisternummer (kind):");

    infoLabelVFT.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
    infoLabelVFT.setText("<html>Deze formulier past uw keuze aan voor de lopende periode.<br/> U kunt uw eerste voorkeur aanpassen vóór 15 juli.</html>");

    scholenTabel.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Naam", "Adres", "Capaciteit", "ID"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
      };
      boolean[] canEdit = new boolean [] {
        false, false, false, false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    scholenScrollPane.setViewportView(scholenTabel);

    boodschapLabelVFT.setForeground(new java.awt.Color(153, 0, 0));
    boodschapLabelVFT.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    studentenDropBoxVFT.setModel(new DefaultComboBoxModel());
    studentenDropBoxVFT.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        studentenDropBoxVFTItemStateChanged(evt);
      }
    });

    aanvraagNummerLabelVFT.setText("Toewijzingsaanvraagnummer:");

    aanvraagnummerVeldVFT.setFocusable(false);

    javax.swing.GroupLayout VoorkeurFormulierTabLayout = new javax.swing.GroupLayout(VoorkeurFormulierTab);
    VoorkeurFormulierTab.setLayout(VoorkeurFormulierTabLayout);
    VoorkeurFormulierTabLayout.setHorizontalGroup(
      VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(VoorkeurFormulierTabLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(selectieBoodschapLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(infoLabelVFT, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(VoorkeurFormulierTabLayout.createSequentialGroup()
            .addComponent(boodschapLabelVFT, javax.swing.GroupLayout.PREFERRED_SIZE, 577, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(indienenKnopVFT))
          .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, VoorkeurFormulierTabLayout.createSequentialGroup()
              .addComponent(zoekwoordLabel)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(zoekwoordVeld))
            .addComponent(scholenScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 678, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, VoorkeurFormulierTabLayout.createSequentialGroup()
              .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(aanvraagNummerLabelVFT)
                .addComponent(rijksnumStudentLabelVFT))
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(studentenDropBoxVFT, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(aanvraagnummerVeldVFT))))))
    );
    VoorkeurFormulierTabLayout.setVerticalGroup(
      VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(VoorkeurFormulierTabLayout.createSequentialGroup()
        .addComponent(infoLabelVFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(rijksnumStudentLabelVFT)
          .addComponent(studentenDropBoxVFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(27, 27, 27)
        .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(aanvraagNummerLabelVFT)
          .addComponent(aanvraagnummerVeldVFT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(22, 22, 22)
        .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(zoekwoordLabel)
          .addComponent(zoekwoordVeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addComponent(scholenScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(selectieBoodschapLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(14, 14, 14)
        .addGroup(VoorkeurFormulierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(boodschapLabelVFT, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(indienenKnopVFT))
        .addContainerGap(390, Short.MAX_VALUE))
    );

    FormulierScherm.addTab("Voorkeurformulier", VoorkeurFormulierTab);

    ZoekAanvraagTab.setBackground(new java.awt.Color(255, 255, 255));

    rijksnumStudentLabelART.setText("Rijksregisternummer (kind): ");

    infoLabelART.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
    infoLabelART.setText("<html>Je kan je aanvragen onderaan raadplegen door het rijksregisternummer van uw kind in te\n <br/>geven. Mocht u niet tevreden zijn met uw opgegeven voorkeur, kunt u deze nog aanpassen\n<br/>door opnieuw de 'Voorkeurformulier' in te vullen.\n<br/>(Let op: u kan enkel de voorkeur voor de lopende periode aanpassen!)</html>");

    gegevensPanelART.setBackground(new java.awt.Color(255, 255, 255));
    gegevensPanelART.setBorder(javax.swing.BorderFactory.createTitledBorder("Uw gegevens"));

    eersteVoorkeurLabel.setText("Voorkeurschool:");

    aanvraagnummerLabelART.setText("Aanvraagnummer:");

    statusLabel.setText("Status:");

    tijdstipLabel.setText("Tijdstip van aanmelding:");

    javax.swing.GroupLayout gegevensPanelARTLayout = new javax.swing.GroupLayout(gegevensPanelART);
    gegevensPanelART.setLayout(gegevensPanelARTLayout);
    gegevensPanelARTLayout.setHorizontalGroup(
      gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensPanelARTLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(aanvraagnummerLabelART)
          .addComponent(tijdstipLabel)
          .addComponent(eersteVoorkeurLabel)
          .addComponent(statusLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(aanvraagnummerLabelOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(statusLabelOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(tijdstipLabelOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(eersteVoorkeurLabelOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    gegevensPanelARTLayout.setVerticalGroup(
      gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(gegevensPanelARTLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(aanvraagnummerLabelART)
          .addComponent(aanvraagnummerLabelOut, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(statusLabel)
          .addComponent(statusLabelOut, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(tijdstipLabel)
          .addComponent(tijdstipLabelOut, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(gegevensPanelARTLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(eersteVoorkeurLabel)
          .addComponent(eersteVoorkeurLabelOut, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    studentenDropBoxART.setModel(new DefaultComboBoxModel());
    studentenDropBoxART.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        studentenDropBoxARTItemStateChanged(evt);
      }
    });

    verwijderLinkLabelART.setFont(new java.awt.Font("Dialog", 2, 11)); // NOI18N
    verwijderLinkLabelART.setForeground(java.awt.Color.blue);
    verwijderLinkLabelART.setText("<html><u>VERWIJDEREN</u></html>");
    verwijderLinkLabelART.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        verwijderLinkLabelARTMouseClicked(evt);
      }
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        verwijderLinkLabelARTMouseEntered(evt);
      }
      public void mouseExited(java.awt.event.MouseEvent evt) {
        verwijderLinkLabelARTMouseExited(evt);
      }
    });

    boodschapLabelART.setForeground(new java.awt.Color(153, 0, 0));
    boodschapLabelART.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    boodschapLabelART.setText("boodschapLabelART");

    javax.swing.GroupLayout ZoekAanvraagTabLayout = new javax.swing.GroupLayout(ZoekAanvraagTab);
    ZoekAanvraagTab.setLayout(ZoekAanvraagTabLayout);
    ZoekAanvraagTabLayout.setHorizontalGroup(
      ZoekAanvraagTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(ZoekAanvraagTabLayout.createSequentialGroup()
        .addGroup(ZoekAanvraagTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(boodschapLabelART, javax.swing.GroupLayout.PREFERRED_SIZE, 645, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(ZoekAanvraagTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ZoekAanvraagTabLayout.createSequentialGroup()
              .addContainerGap()
              .addGroup(ZoekAanvraagTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(infoLabelART, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
                .addGroup(ZoekAanvraagTabLayout.createSequentialGroup()
                  .addComponent(rijksnumStudentLabelART)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(studentenDropBoxART, 0, 514, Short.MAX_VALUE))
                .addComponent(gegevensPanelART, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(ZoekAanvraagTabLayout.createSequentialGroup()
              .addGap(26, 26, 26)
              .addComponent(verwijderLinkLabelART, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        .addContainerGap(122, Short.MAX_VALUE))
    );
    ZoekAanvraagTabLayout.setVerticalGroup(
      ZoekAanvraagTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(ZoekAanvraagTabLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(infoLabelART, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addGroup(ZoekAanvraagTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(rijksnumStudentLabelART)
          .addComponent(studentenDropBoxART, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addComponent(gegevensPanelART, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(verwijderLinkLabelART, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(boodschapLabelART, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(445, Short.MAX_VALUE))
    );

    FormulierScherm.addTab("Aanvragen raadplegen", ZoekAanvraagTab);

    UitloggenTab.setBackground(new java.awt.Color(255, 255, 255));

    uitloggenLinkLabel.setForeground(java.awt.Color.blue);
    uitloggenLinkLabel.setText("<html><u>Log me uit</u></html>");
    uitloggenLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        uitloggenLinkLabelMouseClicked(evt);
      }
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        uitloggenLinkLabelMouseEntered(evt);
      }
      public void mouseExited(java.awt.event.MouseEvent evt) {
        uitloggenLinkLabelMouseExited(evt);
      }
    });

    javax.swing.GroupLayout UitloggenTabLayout = new javax.swing.GroupLayout(UitloggenTab);
    UitloggenTab.setLayout(UitloggenTabLayout);
    UitloggenTabLayout.setHorizontalGroup(
      UitloggenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(UitloggenTabLayout.createSequentialGroup()
        .addGap(30, 30, 30)
        .addComponent(uitloggenLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(712, Short.MAX_VALUE))
    );
    UitloggenTabLayout.setVerticalGroup(
      UitloggenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(UitloggenTabLayout.createSequentialGroup()
        .addGap(26, 26, 26)
        .addComponent(uitloggenLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(731, Short.MAX_VALUE))
    );

    FormulierScherm.addTab("Uitloggen", UitloggenTab);

    MainPanel.add(FormulierScherm, "card2");

    AdminScherm.setBackground(new java.awt.Color(255, 255, 255));
    AdminScherm.setPreferredSize(new java.awt.Dimension(800, 800));

    JTPAdmin.setBackground(new java.awt.Color(255, 255, 255));
    JTPAdmin.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        JTPAdminStateChanged(evt);
      }
    });

    scholenTab.setBackground(new java.awt.Color(255, 255, 255));

    scholenTabelAdmin.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Naam", "Adres", "Capaciteit", "ID"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
      };
      boolean[] canEdit = new boolean [] {
        false, false, false, false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    scholenScrollPaneAdmin.setViewportView(scholenTabelAdmin);

    instructieLabelAdminScholenTab.setForeground(new java.awt.Color(153, 0, 0));
    instructieLabelAdminScholenTab.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    instructieLabelAdminScholenTab.setText("Selecteer een school en voer de nieuwe capaciteit in.");

    zoekwoordVeldAdmin.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyReleased(java.awt.event.KeyEvent evt) {
        zoekwoordVeldKeyReleased(evt);
      }
    });

    veranderCapKnop.setText("Verander");
    veranderCapKnop.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        veranderCapKnopActionPerformed(evt);
      }
    });

    zoekwoordLabelAdmin.setText("Zoekwoord:");

    nieuweCapaciteitLabel.setText("Nieuwe Capaciteit:");

    jLabel10.setText("<html>Hier wordt de controle uitgevoerd op het aanbod.<br>Indien er  een tekort is aan plaatsen,<br> wordt een email verstuurd naar<br>alle scholen.</html>");
    jLabel10.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    controleerCapKnop.setText("Controle uitvoeren");
    controleerCapKnop.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        controleerCapKnopActionPerformed(evt);
      }
    });

    boodschapCapAdmin.setForeground(new java.awt.Color(153, 0, 0));
    boodschapCapAdmin.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    boodschapCapAdmin.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(boodschapCapAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(28, 28, 28)
            .addComponent(controleerCapKnop, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
          .addComponent(jLabel10)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                  .addComponent(zoekwoordLabelAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(nieuweCapaciteitLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                  .addComponent(zoekwoordVeldAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                  .addComponent(nieuweCapaciteitVeld)))
              .addComponent(veranderCapKnop, javax.swing.GroupLayout.Alignment.TRAILING))))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(zoekwoordLabelAdmin)
              .addComponent(zoekwoordVeldAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(nieuweCapaciteitLabel)
              .addComponent(nieuweCapaciteitVeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(7, 7, 7)
            .addComponent(veranderCapKnop)
            .addGap(18, 18, 18)
            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(controleerCapKnop)
              .addComponent(boodschapCapAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))))
        .addContainerGap(16, Short.MAX_VALUE))
    );

    jLabel3.setText("Schoolgegevens:");

    jLabel4.setText("Naam:");

    jLabel5.setText("Adres:");

    jLabel6.setText("Capaciteit:");

    jLabel7.setText("ID:");

    jLabel8.setText("Email:");

    jTextField2.setFocusable(false);

    jTextField3.setFocusable(false);

    jTextField4.setFocusable(false);

    jTextField5.setFocusable(false);

    jTextField6.setFocusable(false);

    javax.swing.GroupLayout schoolGegevensPanelLayout = new javax.swing.GroupLayout(schoolGegevensPanel);
    schoolGegevensPanel.setLayout(schoolGegevensPanelLayout);
    schoolGegevensPanelLayout.setHorizontalGroup(
      schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(schoolGegevensPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(schoolGegevensPanelLayout.createSequentialGroup()
            .addComponent(jLabel3)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(schoolGegevensPanelLayout.createSequentialGroup()
            .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, schoolGegevensPanelLayout.createSequentialGroup()
                .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                  .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)))
              .addGroup(schoolGegevensPanelLayout.createSequentialGroup()
                .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                  .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addComponent(jTextField6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
    );
    schoolGegevensPanelLayout.setVerticalGroup(
      schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(schoolGegevensPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel6)
          .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel7)
          .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(schoolGegevensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel8)
          .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(106, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout scholenTabLayout = new javax.swing.GroupLayout(scholenTab);
    scholenTab.setLayout(scholenTabLayout);
    scholenTabLayout.setHorizontalGroup(
      scholenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(scholenTabLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(scholenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(instructieLabelAdminScholenTab)
          .addComponent(scholenScrollPaneAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 737, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(scholenTabLayout.createSequentialGroup()
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(34, 34, 34)
            .addComponent(schoolGegevensPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addContainerGap(210, Short.MAX_VALUE))
    );
    scholenTabLayout.setVerticalGroup(
      scholenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(scholenTabLayout.createSequentialGroup()
        .addGap(18, 18, 18)
        .addComponent(instructieLabelAdminScholenTab)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(scholenScrollPaneAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addGroup(scholenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(scholenTabLayout.createSequentialGroup()
            .addComponent(schoolGegevensPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(1243, 1243, 1243))
          .addGroup(scholenTabLayout.createSequentialGroup()
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
    );

    JTPAdmin.addTab("Beheer Scholen", scholenTab);

    tijdSchemaTab.setBackground(new java.awt.Color(255, 255, 255));

    toepassenTijdSchemaKnop.setText("Toepassen");
    toepassenTijdSchemaKnop.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toepassenTijdSchemaKnopActionPerformed(evt);
      }
    });

    timeED.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "09:00", "09:10", "09:20", "09:30", "09:40", "09:50", "10:00", "10:10", "10:20", "10:30", "10:40", "10:50", "11:00", "11:10", "11:20", "11:30", "11:40", "11:50", "12:00", "12:10", "12:20", "12:30", "12:40", "12:50", "13:00", "13:10", "13:20", "13:30", "13:40", "13:50", "14:00", "14:10", "14:20", "14:30", "14:40", "14:50", "15:00", "15:10", "15:20", "15:30", "15:40", "15:50", "16:00", "16:10", "16:20", "16:30", "16:40", "16:50", "17:00", "17:10", "17:20", "17:30", "17:40", "17:50", "18:00", "18:10", "18:20", "18:30", "18:40", "18:50", "19:00", "19:10", "19:20", "19:30", "19:40", "19:50", "20:00", "20:10", "20:20", "20:30", "20:40", "20:50", "21:00", "21:10", "21:20", "21:30", "21:40", "21:50", "22:00", "22:10", "22:20", "22:30", "22:40", "22:50", "23:00", "23:10", "23:20", "23:30", "23:40", "23:50", "00:00", "00:10", "00:20", "00:30", "00:40", "00:50", "01:00", "01:10", "01:20", "01:30", "01:40", "01:50", "02:00", "02:10", "02:20", "02:30", "02:40", "02:50", "03:00", "03:10", "03:20", "03:30", "03:40", "03:50", "04:00", "04:10", "04:20", "04:30", "04:40", "04:50", "05:00", "05:10", "05:20", "05:30", "05:40", "05:50", "06:00", "06:10", "06:20", "06:30", "06:40", "06:50", "07:00", "07:10", "07:20", "07:30", "07:40", "07:50", "08:00", "08:10", "08:20", "08:30", "08:40", "08:50", " " }));

    timeCDL.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "09:00", "09:10", "09:20", "09:30", "09:40", "09:50", "10:00", "10:10", "10:20", "10:30", "10:40", "10:50", "11:00", "11:10", "11:20", "11:30", "11:40", "11:50", "12:00", "12:10", "12:20", "12:30", "12:40", "12:50", "13:00", "13:10", "13:20", "13:30", "13:40", "13:50", "14:00", "14:10", "14:20", "14:30", "14:40", "14:50", "15:00", "15:10", "15:20", "15:30", "15:40", "15:50", "16:00", "16:10", "16:20", "16:30", "16:40", "16:50", "17:00", "17:10", "17:20", "17:30", "17:40", "17:50", "18:00", "18:10", "18:20", "18:30", "18:40", "18:50", "19:00", "19:10", "19:20", "19:30", "19:40", "19:50", "20:00", "20:10", "20:20", "20:30", "20:40", "20:50", "21:00", "21:10", "21:20", "21:30", "21:40", "21:50", "22:00", "22:10", "22:20", "22:30", "22:40", "22:50", "23:00", "23:10", "23:20", "23:30", "23:40", "23:50", "00:00", "00:10", "00:20", "00:30", "00:40", "00:50", "01:00", "01:10", "01:20", "01:30", "01:40", "01:50", "02:00", "02:10", "02:20", "02:30", "02:40", "02:50", "03:00", "03:10", "03:20", "03:30", "03:40", "03:50", "04:00", "04:10", "04:20", "04:30", "04:40", "04:50", "05:00", "05:10", "05:20", "05:30", "05:40", "05:50", "06:00", "06:10", "06:20", "06:30", "06:40", "06:50", "07:00", "07:10", "07:20", "07:30", "07:40", "07:50", "08:00", "08:10", "08:20", "08:30", "08:40", "08:50", " " }));

    datePickerED.setText("jButton6");

    datePickerCDL.setText("jButton5");

    datePickerIDL.setText("jButton4");

    datePickerSD.setText("jButton3");

    timeIDL.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "09:00", "09:10", "09:20", "09:30", "09:40", "09:50", "10:00", "10:10", "10:20", "10:30", "10:40", "10:50", "11:00", "11:10", "11:20", "11:30", "11:40", "11:50", "12:00", "12:10", "12:20", "12:30", "12:40", "12:50", "13:00", "13:10", "13:20", "13:30", "13:40", "13:50", "14:00", "14:10", "14:20", "14:30", "14:40", "14:50", "15:00", "15:10", "15:20", "15:30", "15:40", "15:50", "16:00", "16:10", "16:20", "16:30", "16:40", "16:50", "17:00", "17:10", "17:20", "17:30", "17:40", "17:50", "18:00", "18:10", "18:20", "18:30", "18:40", "18:50", "19:00", "19:10", "19:20", "19:30", "19:40", "19:50", "20:00", "20:10", "20:20", "20:30", "20:40", "20:50", "21:00", "21:10", "21:20", "21:30", "21:40", "21:50", "22:00", "22:10", "22:20", "22:30", "22:40", "22:50", "23:00", "23:10", "23:20", "23:30", "23:40", "23:50", "00:00", "00:10", "00:20", "00:30", "00:40", "00:50", "01:00", "01:10", "01:20", "01:30", "01:40", "01:50", "02:00", "02:10", "02:20", "02:30", "02:40", "02:50", "03:00", "03:10", "03:20", "03:30", "03:40", "03:50", "04:00", "04:10", "04:20", "04:30", "04:40", "04:50", "05:00", "05:10", "05:20", "05:30", "05:40", "05:50", "06:00", "06:10", "06:20", "06:30", "06:40", "06:50", "07:00", "07:10", "07:20", "07:30", "07:40", "07:50", "08:00", "08:10", "08:20", "08:30", "08:40", "08:50", " " }));

    timeSD.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "09:00", "09:10", "09:20", "09:30", "09:40", "09:50", "10:00", "10:10", "10:20", "10:30", "10:40", "10:50", "11:00", "11:10", "11:20", "11:30", "11:40", "11:50", "12:00", "12:10", "12:20", "12:30", "12:40", "12:50", "13:00", "13:10", "13:20", "13:30", "13:40", "13:50", "14:00", "14:10", "14:20", "14:30", "14:40", "14:50", "15:00", "15:10", "15:20", "15:30", "15:40", "15:50", "16:00", "16:10", "16:20", "16:30", "16:40", "16:50", "17:00", "17:10", "17:20", "17:30", "17:40", "17:50", "18:00", "18:10", "18:20", "18:30", "18:40", "18:50", "19:00", "19:10", "19:20", "19:30", "19:40", "19:50", "20:00", "20:10", "20:20", "20:30", "20:40", "20:50", "21:00", "21:10", "21:20", "21:30", "21:40", "21:50", "22:00", "22:10", "22:20", "22:30", "22:40", "22:50", "23:00", "23:10", "23:20", "23:30", "23:40", "23:50", "00:00", "00:10", "00:20", "00:30", "00:40", "00:50", "01:00", "01:10", "01:20", "01:30", "01:40", "01:50", "02:00", "02:10", "02:20", "02:30", "02:40", "02:50", "03:00", "03:10", "03:20", "03:30", "03:40", "03:50", "04:00", "04:10", "04:20", "04:30", "04:40", "04:50", "05:00", "05:10", "05:20", "05:30", "05:40", "05:50", "06:00", "06:10", "06:20", "06:30", "06:40", "06:50", "07:00", "07:10", "07:20", "07:30", "07:40", "07:50", "08:00", "08:10", "08:20", "08:30", "08:40", "08:50", " " }));

    jLabel16.setText("Einddatum:");

    jLabel14.setText("<html>Deadline<br>capaciteitsuitbreiding:</html>");

    jLabel13.setText("<html>Deadline<br>inschrijvingen:</html>");

    jLabel12.setText("Startdatum:");

    deadlinesBoodschap2.setForeground(new java.awt.Color(153, 0, 0));
    deadlinesBoodschap2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    deadlinesBoodschap2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    datePickerLabel1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
    datePickerLabel1.setForeground(new java.awt.Color(153, 0, 0));
    datePickerLabel1.setText("<html>DATUM<br>OVERSCHREDEN</html>");
    datePickerLabel1.setToolTipText("");

    datePickerLabel2.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
    datePickerLabel2.setForeground(new java.awt.Color(153, 0, 0));
    datePickerLabel2.setText("<html>DATUM<br>OVERSCHREDEN</html>");
    datePickerLabel2.setToolTipText("");

    datePickerLabel3.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
    datePickerLabel3.setForeground(new java.awt.Color(153, 0, 0));
    datePickerLabel3.setText("<html>DATUM<br>OVERSCHREDEN</html>.");
    datePickerLabel3.setToolTipText("");

    datePickerLabel4.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
    datePickerLabel4.setForeground(new java.awt.Color(153, 0, 0));
    datePickerLabel4.setText("<html>DATUM<br>OVERSCHREDEN</html>");
    datePickerLabel4.setToolTipText("");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGap(6, 6, 6)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(deadlinesBoodschap2, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel16)
              .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel12))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(datePickerLabel2)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(datePickerLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(datePickerLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(datePickerLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE)))
            .addGap(38, 38, 38)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(toepassenTijdSchemaKnop, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                  .addComponent(datePickerCDL, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                  .addComponent(datePickerIDL, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(datePickerSD, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(datePickerED, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(timeSD, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(timeIDL, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeED, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeCDL, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
        .addContainerGap(146, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGap(17, 17, 17)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(datePickerSD)
            .addComponent(timeSD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(datePickerLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
          .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(7, 7, 7)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(datePickerIDL))
              .addComponent(timeIDL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(datePickerLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(8, 8, 8)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(datePickerLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(datePickerCDL)
              .addComponent(timeCDL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(16, 16, 16))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)))
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(datePickerLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(timeED, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(datePickerED))
          .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
        .addComponent(deadlinesBoodschap2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(29, 29, 29)
        .addComponent(toepassenTijdSchemaKnop)
        .addGap(19, 19, 19))
    );

    jLabel15.setText("Huidig schema:");

    jLabel17.setText("Startdatum:");
    jLabel17.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    jLabel18.setText("Deadline inschrijvingen:");
    jLabel18.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    jLabel19.setText("Deadline capaciteitsuitbreiding:");
    jLabel19.setToolTipText("");
    jLabel19.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    jLabel20.setText("Einddatum:");
    jLabel20.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    startDatumLabel.setForeground(new java.awt.Color(153, 0, 0));
    startDatumLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    inschrijvingenDLLabel.setForeground(new java.awt.Color(153, 0, 0));
    inschrijvingenDLLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    capaciteitDLLabel.setForeground(new java.awt.Color(153, 0, 0));
    capaciteitDLLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    eindDatumLabel.setForeground(new java.awt.Color(153, 0, 0));
    eindDatumLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    deadlinesBoodschap.setForeground(new java.awt.Color(153, 0, 0));
    deadlinesBoodschap.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    deadlinesBoodschap.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

    jLabel21.setText("Huidig deadline:");

    huidigDLLabel.setForeground(new java.awt.Color(153, 0, 0));
    huidigDLLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(startDatumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(inschrijvingenDLLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(capaciteitDLLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(138, 138, 138))
          .addComponent(eindDatumLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(huidigDLLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel15)
              .addComponent(deadlinesBoodschap, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addGap(7, 7, 7)
        .addComponent(jLabel15)
        .addGap(18, 18, 18)
        .addComponent(jLabel17)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(startDatumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel18)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(inschrijvingenDLLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel19)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(capaciteitDLLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel21)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(huidigDLLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel20)
        .addGap(1, 1, 1)
        .addComponent(eindDatumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(deadlinesBoodschap, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    javax.swing.GroupLayout tijdSchemaTabLayout = new javax.swing.GroupLayout(tijdSchemaTab);
    tijdSchemaTab.setLayout(tijdSchemaTabLayout);
    tijdSchemaTabLayout.setHorizontalGroup(
      tijdSchemaTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tijdSchemaTabLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(174, 174, 174))
    );
    tijdSchemaTabLayout.setVerticalGroup(
      tijdSchemaTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(tijdSchemaTabLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(tijdSchemaTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(1267, 1267, 1267))
    );

    JTPAdmin.addTab("Beheer Data", tijdSchemaTab);

    wachtLijstenTab.setBackground(new java.awt.Color(255, 255, 255));
    wachtLijstenTab.setPreferredSize(new java.awt.Dimension(500, 394));

    editPanel.setBackground(new java.awt.Color(255, 255, 255));

    exporteerKnopAdmin.setText("Exporteer wachtlijsten");
    exporteerKnopAdmin.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exporteerKnopAdminActionPerformed(evt);
      }
    });

    sorteerKnopAdmin.setText("Sorteer wachtlijsten");
    sorteerKnopAdmin.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sorteerKnopAdminActionPerformed(evt);
      }
    });

    boodschapLabelAdminWTab.setForeground(new java.awt.Color(153, 0, 0));
    boodschapLabelAdminWTab.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    boodschapLabelAdminWTab.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
            .addComponent(exporteerKnopAdmin, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
            .addComponent(sorteerKnopAdmin, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addComponent(boodschapLabelAdminWTab, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(41, Short.MAX_VALUE))
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(exporteerKnopAdmin)
        .addGap(27, 27, 27)
        .addComponent(sorteerKnopAdmin)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(boodschapLabelAdminWTab, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(20, Short.MAX_VALUE))
    );

    lijstScrollPane.setViewportView(jList1);

    laadLijstKnop.setText("Laden");
    laadLijstKnop.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        laadLijstKnopActionPerformed(evt);
      }
    });

    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel1.setText("School ID:");

    TerugKnop2.setText("Terug");
    TerugKnop2.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        TerugKnop2Clicked(evt);
      }
    });

    javax.swing.GroupLayout leesPanelLayout = new javax.swing.GroupLayout(leesPanel);
    leesPanel.setLayout(leesPanelLayout);
    leesPanelLayout.setHorizontalGroup(
      leesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(leesPanelLayout.createSequentialGroup()
        .addGroup(leesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leesPanelLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(TerugKnop2))
          .addGroup(leesPanelLayout.createSequentialGroup()
            .addGroup(leesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addGroup(leesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(idVeldLaadLijst)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(laadLijstKnop))
              .addGroup(leesPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(lijstScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(0, 21, Short.MAX_VALUE)))
        .addContainerGap())
    );
    leesPanelLayout.setVerticalGroup(
      leesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(leesPanelLayout.createSequentialGroup()
        .addGap(13, 13, 13)
        .addGroup(leesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(idVeldLaadLijst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(laadLijstKnop))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(lijstScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(34, 34, 34)
        .addComponent(TerugKnop2)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
    editPanel.setLayout(editPanelLayout);
    editPanelLayout.setHorizontalGroup(
      editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(editPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(leesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    editPanelLayout.setVerticalGroup(
      editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(editPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(leesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout wachtLijstenTabLayout = new javax.swing.GroupLayout(wachtLijstenTab);
    wachtLijstenTab.setLayout(wachtLijstenTabLayout);
    wachtLijstenTabLayout.setHorizontalGroup(
      wachtLijstenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(wachtLijstenTabLayout.createSequentialGroup()
        .addComponent(editPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0))
    );
    wachtLijstenTabLayout.setVerticalGroup(
      wachtLijstenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(wachtLijstenTabLayout.createSequentialGroup()
        .addComponent(editPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0))
    );

    JTPAdmin.addTab("Beheer Wachtlijsten", wachtLijstenTab);

    uitloggenLinkLabel1.setForeground(java.awt.Color.blue);
    uitloggenLinkLabel1.setText("<html><u>Log me uit</u></html>");
    uitloggenLinkLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        uitloggenLinkLabelMouseClicked(evt);
      }
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        uitloggenLinkLabelMouseEntered(evt);
      }
      public void mouseExited(java.awt.event.MouseEvent evt) {
        uitloggenLinkLabelMouseExited(evt);
      }
    });

    javax.swing.GroupLayout uitlogAdminTabLayout = new javax.swing.GroupLayout(uitlogAdminTab);
    uitlogAdminTab.setLayout(uitlogAdminTabLayout);
    uitlogAdminTabLayout.setHorizontalGroup(
      uitlogAdminTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(uitlogAdminTabLayout.createSequentialGroup()
        .addGap(30, 30, 30)
        .addComponent(uitloggenLinkLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(878, Short.MAX_VALUE))
    );
    uitlogAdminTabLayout.setVerticalGroup(
      uitlogAdminTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(uitlogAdminTabLayout.createSequentialGroup()
        .addGap(26, 26, 26)
        .addComponent(uitloggenLinkLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(1639, Short.MAX_VALUE))
    );

    JTPAdmin.addTab("Uitloggen", uitlogAdminTab);

    javax.swing.GroupLayout AdminSchermLayout = new javax.swing.GroupLayout(AdminScherm);
    AdminScherm.setLayout(AdminSchermLayout);
    AdminSchermLayout.setHorizontalGroup(
      AdminSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
      .addGroup(AdminSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(AdminSchermLayout.createSequentialGroup()
          .addGap(0, 0, Short.MAX_VALUE)
          .addComponent(JTPAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGap(0, 0, Short.MAX_VALUE)))
    );
    AdminSchermLayout.setVerticalGroup(
      AdminSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
      .addGroup(AdminSchermLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(AdminSchermLayout.createSequentialGroup()
          .addGap(0, 0, Short.MAX_VALUE)
          .addComponent(JTPAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGap(0, 0, Short.MAX_VALUE)))
    );

    MainPanel.add(AdminScherm, "card5");

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    getContentPane().add(MainPanel, gridBagConstraints);

    pack();
  }// </editor-fold>//GEN-END:initComponents
 
    /*
     * Methode voor het updaten/maken van de tabel(len) met scholen.
     * 
     */
  public void updateTabel(JTable tabel) {
      scholenData = main.ophalenScholen();
      
      this.dtm = (DefaultTableModel)tabel.getModel();
      this.dtm.setRowCount(0);
      for (School s : scholenData) {
	  Object[] o = new Object[4];
	  o[0] = s.getNaam();
	  o[1] = s.getAdres();
	  o[2] = s.getPlaatsen();
	  o[3] = s.getID();
	  dtm.addRow(o);
      }

      /*
       * Methode dat de actie bepaalt bij het selecteren van een item uit
       * tabel met scholen
       */
      tabel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
	@Override
	public void valueChanged(ListSelectionEvent e) {
	  if(tabel.getSelectedRow() > -1) {
	      selectieBoodschapLabel.setText("U heeft een school "
		      + "geselecteerd.");
              for(School s : scholenData) {
                if(s.getID() == (int)tabel.getValueAt(tabel.getSelectedRow(), 3)) {
                  jTextField2.setText(s.getNaam());
                  jTextField3.setText(s.getAdres());
                  jTextField4.setText(String.valueOf(s.getPlaatsen()));
                  jTextField5.setText(String.valueOf(s.getID()));
                  jTextField6.setText(s.getEmail());
                  schoolGegevensPanel.setVisible(true);
                }
              }
	  } else {
	      selectieBoodschapLabel.setText("Gelieve een school uit de "
		      + "bovenstaande lijst te "
		      + "selecteren.");
	  }
	}
      });
  }
  
    /*
     * Methode dat de actie bepaalt bij het selecteren van een item uit
     * tabel met scholen
     */
    private void indienenKnopVFTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indienenKnopVFTActionPerformed
        if(studentenDropBoxVFT.getSelectedItem() == null 
             || scholenTabel.getSelectedRow() < 0
             || aanvraagnummerVeldVFT.getText().equals("")) {
                      boodschapLabelVFT.setText("Maak een selectie!");
          } else {
              int aanvraagnummer = Integer.parseInt(aanvraagnummerVeldVFT.getText());
              Student student = main.ophalenStudent(studentenDropBoxVFT.getSelectedItem().toString());
              int schoolID = (int)scholenTabel.getValueAt(scholenTabel.getSelectedRow(), 3);
              switch(main.indienenVoorkeur(aanvraagnummer, student, schoolID)) {
                case -1:  boodschapLabelVFT.setText("<html>U hoeft momenteel niets te doen! U ontvangt van ons een email"
                                                  + "<br/>van zondra u uw voorkeur dient aan te passen.</html>");
                          break;
                case  0:  boodschapLabelVFT.setText("<html>U werd al afgewezen voor deze school! "
                                            + "<br/>Gelieve een andere school te selecteren.</html>");
                          break;
                case  1:  boodschapLabelVFT.setText("<html>U heeft uw voorkeur succesvol ingediend! "
                                   + "<br/>Je kan je aanvragen raadplegen onder 'Aanvragen'.</html>");
                          break;
                case  2:  boodschapLabelVFT.setText("U heeft al voor deze school gekozen!");
                          break;
                case  3:  boodschapLabelVFT.setText("Uw aanvraag werd definitief toegewezen!!! Geen aanpassingen meer mogelijk..");
                          break;
                default:  boodschapLabelVFT.setText("Fout!");
                          break;
              }
        }
    }//GEN-LAST:event_indienenKnopVFTActionPerformed
    
    /*
     * Methode dat de actie bepaalt bij klikken op 'Indienen' onder de tab
     * 'Aanmeldingsformulier'
     */
    private void indienenKnopAFTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indienenKnopAFTActionPerformed
        if(naamStudentVeldAFT.getText().equals("")){
          boodschapLabelAFT.setText("Maak een selectie!");
        }
        else {
          Student s = main.ophalenStudent(studentenDropBoxAFT.getSelectedItem().toString());
          ToewijzingsAanvraag ta = new ToewijzingsAanvraag(main.keyNieuweAanvraag(), 
                                                           s.getRijksregisterNummer(), 
                                                           s.getRijksregisterNummerOuder());
          if(main.addAanvraag(ta)){
              boodschapLabelAFT.setText("<html>U heef zich succesvol aangemeld! "
                  + "<br/> Je kan je aanvragen raadplegen onder 'Aanvragen'.</html>");
          } else {
              boodschapLabelAFT.setText("U heeft al een aanvraag gedaan.");
          }
        }
    }//GEN-LAST:event_indienenKnopAFTActionPerformed

    /*
     * Methode dat de actie bepaalt bij het tijpen van een zoekwoord onder 
     * de tab 'Voorkeurformulier'
     */
    private void zoekwoordVeldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_zoekwoordVeldKeyReleased
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(((DefaultTableModel) scholenTabel.getModel())); 
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + zoekwoordVeld.getText()));
        scholenTabel.setRowSorter(sorter);
        TableRowSorter<TableModel> sorter1 = new TableRowSorter<>(((DefaultTableModel) scholenTabelAdmin.getModel())); 
        sorter1.setRowFilter(RowFilter.regexFilter("(?i)" + zoekwoordVeldAdmin.getText()));
        scholenTabelAdmin.setRowSorter(sorter1);
    }//GEN-LAST:event_zoekwoordVeldKeyReleased
    
    /*
     * Methode dat de actie bepaalt bij het zweven over 'Activeer' in het
     * aanmeldingsscherm
     */
    private void activeerLinkLabelISMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_activeerLinkLabelISMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_activeerLinkLabelISMouseEntered
    
    /*
     * Methode dat de actie bepaalt bij het niet zweven over 'Activeer' in het
     * aanmeldingsscherm
     */
    private void activeerLinkLabelISMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_activeerLinkLabelISMouseExited
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_activeerLinkLabelISMouseExited
    
    /*
     * Methode dat de actie bepaalt bij klikken op 'Activeer' in het
     * aanmeldingsscherm
     */
    private void activeerLinkLabelISMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_activeerLinkLabelISMouseClicked
        ActiveerScherm.setVisible(true);
        InlogScherm.setVisible(false);
        rijksnumVeldAS.setText("");
        naamVeldAS.setText("");
        voornaamVeldAS.setText("");
        emailVeldAS.setText("");
        adresVeldAS.setText("");
        boodschapLabelAS.setText("");
        voorwaardelijkOpmaak();
    }//GEN-LAST:event_activeerLinkLabelISMouseClicked
    
    /*
     * Methode dat de actie bepaalt bij het zweven over 'Terug' in het
     * aanmeldingsscherm
     */
    private void terugLinkLabelASMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_terugLinkLabelASMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_terugLinkLabelASMouseEntered

    /*
     * Methode dat de actie bepaalt bij het zweven over 'Terug' in het
     * activeerscherm
     */
    private void terugLinkLabelASMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_terugLinkLabelASMouseExited
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_terugLinkLabelASMouseExited

    /*
     * Methode dat de actie bepaalt bij het niet zweven over 'Terug' in het
     * activeerscherm
     */
    private void terugLinkLabelASMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_terugLinkLabelASMouseClicked
        ActiveerScherm.setVisible(false);
        InlogScherm.setVisible(true);
        gebrVeldIS.setText("");
        passVeldIS.setText("");
        boodschapLabelIS.setText("");
    }//GEN-LAST:event_terugLinkLabelASMouseClicked

    /*
     * Methode dat de actie bepaalt bij het klikken op 'Terug' in het
     * activeerscherm
     */
    private void activeerKnopASActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeerKnopASActionPerformed
       
            String rnouder = rijksnumVeldAS.getText();
            if(main.activeren(rnouder)) 
                boodschapLabelAS.setText("<html>Account succesvol aangemaakt. U ontangt van ons "
                                + "<br/>binnenkort een email met uw login gegevens.</html>");
            else if (naamVeldAS.getText().equals("")) 
                boodschapLabelAS.setText("<html>U moet eerst een rijksregisternummer ingeven!</html>");
            else 
                boodschapLabelAS.setText("<html>U heeft al een account aangemaakt!</html>");
    }//GEN-LAST:event_activeerKnopASActionPerformed
    
    /*
     * Methode dat de actie bepaalt bij klikken op 'Inloggen' in het
     * aanmeldingsscherm
     */

    private void inlogKnopISActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inlogKnopISActionPerformed
        /*
         * Gegevens vergelijken met de gegevens van de databank
         * Indien de juiste gebruikersnaam en wachtwoord werden ingegeven, wordt
         * de knop 'Doorgaan' beschikbaar en een groene boodschaap 'U bent
         * ingelogd!' verscijnt
         * Indien de gegevens niet kloppen wordt de boodschap 'Verkeerde 
         * gegevens.' weergegeven
         * Bij succes worden de gegevens van de gebruiker ingeladen en gebruikt
         * om de interface te bepalen
         */
        String gebrnaam = gebrVeldIS.getText();
        char[] passArray = passVeldIS.getPassword();
        //inloggen met verkeerde gegevens
	TypeGebruiker type = main.inloggen(gebrnaam, passArray);
	switch(type) {
	  case ADMIN: boodschapLabelIS.setText("<html>Ingelogd als <br>administrator.</html>");
		      doorgaanKnopIS.setEnabled(true);
		      InlogScherm.getRootPane().setDefaultButton(doorgaanKnopIS);
                      schoolGegevensPanel.setVisible(false);
                      updateTabel(scholenTabelAdmin);
		      break;
	  case OUDER: boodschapLabelIS.setText("U bent ingelolgd.");
		      doorgaanKnopIS.setEnabled(true);
		      InlogScherm.getRootPane().setDefaultButton(doorgaanKnopIS);

		      /* 
		       * Gegevens van ouder automatisch aanvullen in de 
		       * 'Aanmeldingsformulier'-tab
		       */
		      gebruiker = main.ophalenOuder(gebrnaam, passArray);
		      naamOuderVeldAFT.setText(gebruiker.getNaam());
		      voornaamOuderVeldAFT.setText(gebruiker.getVoornaam());
		      emailVeldAFT.setText(gebruiker.getEmail());
		      adresVeldAFT.setText(gebruiker.getStraat() + ", " + gebruiker.getGemeente());
		      rijksnumOuderVeldAFT.setText(gebruiker.getRijksregisterNummer());

		      /* 
		       * Dropbox items toevoegen in 'Voorkeurformulier'-tab en 
		       * 'Aanvragen raadplegen'-tab en in 'Aanmeldigsformulier'-tab
		       */
		      studentenDropBoxAFT.insertItemAt("", 0);
		      studentenDropBoxART.insertItemAt("", 0);
		      studentenDropBoxVFT.insertItemAt("", 0);           
		      main.ophalenKinderen(gebruiker.getRijksregisterNummer()).stream().map((s) -> {
			  studentenDropBoxAFT.addItem(s.getRijksregisterNummer());
			  return s;
		      }).map((s) -> {
			  studentenDropBoxART.addItem(s.getRijksregisterNummer());
			  return s;
		      }).forEachOrdered((s) -> {
			  studentenDropBoxVFT.addItem(s.getRijksregisterNummer());
		      });
                      updateTabel(scholenTabel);
		      break;
	    default:  boodschapLabelIS.setText("Verkeerde gegevens.");
		      doorgaanKnopIS.setEnabled(false);	
		      break;
	}
    }//GEN-LAST:event_inlogKnopISActionPerformed

    /*
     * Methode dat de actie bepaalt bij klikken op 'Doorgaan' in het
     * aanmeldingsscherm
     */
    private void doorgaanKnopISActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doorgaanKnopISActionPerformed
        if(gebruiker == null) {
	  AdminScherm.setVisible(true);
	  InlogScherm.setVisible(false);
	  ActiveerScherm.setVisible(false);
	  FormulierScherm.setVisible(false);
          JTPAdmin.setSelectedComponent(scholenTab);
	} else {
	  ActiveerScherm.setVisible(false);
	  InlogScherm.setVisible(false);
	  FormulierScherm.setVisible(true);
	  FormulierScherm.setSelectedComponent(HomeTab);
	  PersoonlijkeJlabel.setText("Hallo " + gebruiker.getVoornaam() + " " 
				      + gebruiker.getNaam() + ".");
	}
    }//GEN-LAST:event_doorgaanKnopISActionPerformed

   
    /*
     * Methode dat de actie bepaalt bij het opzoeken van een rijksregisternummer
     * in het activeerscherm
     */
    private void rijksnumVeldASKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rijksnumVeldASKeyReleased
        String rnouder = rijksnumVeldAS.getText();
        Ouder o = main.ophalenOuder(rnouder);
        if(o != null) {
            naamVeldAS.setText(o.getVoornaam());
            voornaamVeldAS.setText(o.getNaam());
            emailVeldAS.setText(o.getEmail());
            adresVeldAS.setText(o.getStraat() + ", " + o.getGemeente());
        } else {
          naamVeldAS.setText("");
          voornaamVeldAS.setText("");
          emailVeldAS.setText("");
          adresVeldAS.setText("");
        }
    }//GEN-LAST:event_rijksnumVeldASKeyReleased

    /*
     * Methode dat de actie bepaalt bij het maken van een selectie 
     * van een student onder de tab 'Aanmeldingsformulier'
     */
    private void studentenDropBoxAFTItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_studentenDropBoxAFTItemStateChanged
        Student s = null;
        if(studentenDropBoxAFT.getItemCount() > 0) {
            s = main.ophalenStudent(
                    studentenDropBoxAFT.getSelectedItem().toString()
            );
        }
        if(s != null) {
            naamStudentVeldAFT.setText(s.getNaam());
            voornaamStudentVeldAFT.setText(s.getVoornaam());
            telnumVeldAFT.setText(s.getTelefoonnummer());
        } else {
            naamStudentVeldAFT.setText("");
            voornaamStudentVeldAFT.setText("");
            telnumVeldAFT.setText("");
        }
        boodschapLabelAFT.setText("");
    }//GEN-LAST:event_studentenDropBoxAFTItemStateChanged

    /*
     * Methode dat de actie bepaalt bij het maken van een selectie 
     * van een student onder de tab 'Aanvragen raadplegen'
     */
    private void studentenDropBoxARTItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_studentenDropBoxARTItemStateChanged
        ToewijzingsAanvraag ta = null;
        TijdSchema ts = main.ophalenTijdSchema(LocalDateTime.now().getYear());
        if(studentenDropBoxART.getItemCount() > 0)
            ta = main.ophalenAanvraag(studentenDropBoxART.getSelectedItem().toString());
        if(ta == null && studentenDropBoxART.getSelectedIndex() > 0) {
            aanvraagnummerLabelOut.setText("");
            statusLabelOut.setText("");
            tijdstipLabelOut.setText("");
            eersteVoorkeurLabelOut.setText("");
            boodschapLabelART.setText("De aanvraag werd niet gevonden!");
        } else if (ta == null || studentenDropBoxART.getSelectedIndex() == 0) {
            aanvraagnummerLabelOut.setText("");
            statusLabelOut.setText("");
            tijdstipLabelOut.setText("");
            eersteVoorkeurLabelOut.setText("");
            boodschapLabelART.setText("");
        } else {
            boodschapLabelART.setText("");
            aanvraagnummerLabelOut.setText(String.valueOf(ta.getToewijzingsAanvraagNummer()));
            statusLabelOut.setText(String.valueOf(ta.getStatus()));
            DateTimeFormatter df
                    = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String date = df.format(ta.getAanmeldingsTijdstip());
            tijdstipLabelOut.setText(date);
            int voorkeur = ta.getVoorkeur();
            if(voorkeur == 0)
                eersteVoorkeurLabelOut.setText("Je kan je voorkeur "
                                         + "aanpassen vóór " + ts.getHuidigDeadline());
            else
                eersteVoorkeurLabelOut.setText(main.ophalenSchool(voorkeur).toString());
        }
        
    }//GEN-LAST:event_studentenDropBoxARTItemStateChanged

    /*
     * Methode dat de actie bepaalt bij het maken van een selectie 
     * van een student onder de tab 'Voorkeurformulier'
     */
    private void studentenDropBoxVFTItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_studentenDropBoxVFTItemStateChanged
        ToewijzingsAanvraag ta = null;
        if(studentenDropBoxVFT.getItemCount() > 0) {
            ta = main.ophalenAanvraag(
                studentenDropBoxVFT.getSelectedItem().toString()
            );
        } 
        if(ta == null) {
            aanvraagnummerVeldVFT.setText("");
        } else {
            aanvraagnummerVeldVFT.setText(
                    String.valueOf(ta.getToewijzingsAanvraagNummer())
            );
        }
    }//GEN-LAST:event_studentenDropBoxVFTItemStateChanged

    /*
     * Methode dat de actie bepaalt bij het selecteren van een tab
     */
    private void FormulierSchermStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_FormulierSchermStateChanged
      if(gebruiker != null) {
            studentenDropBoxAFT.setSelectedIndex(0);
            studentenDropBoxVFT.setSelectedIndex(0);
            studentenDropBoxART.setSelectedIndex(0);
            scholenTabel.clearSelection();
            boodschapLabelAFT.setText("");
            boodschapLabelVFT.setText("");
            boodschapLabelART.setText("");
        }
    }//GEN-LAST:event_FormulierSchermStateChanged

    /*
     * Methode dat de actie bepaalt bij het zweven over 'VERWIJDER' onder de
     * tab 'Aanvragen raadplegen'
     */
    private void verwijderLinkLabelARTMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_verwijderLinkLabelARTMouseEntered
         setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_verwijderLinkLabelARTMouseEntered

    /*
     * Methode dat de actie bepaalt bij het niet zweven over 'VERWIJDER' onder de
     * tab 'Aanvragen raadplegen'
     */
    private void verwijderLinkLabelARTMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_verwijderLinkLabelARTMouseExited
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_verwijderLinkLabelARTMouseExited

    /*
     * Methode dat de actie bepaalt bij klikken op 'VERWIJDER' onder de tab
     * 'Aanvragen raadplegen'
     */
    private void verwijderLinkLabelARTMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_verwijderLinkLabelARTMouseClicked
        int aanvraagnummer = Integer.parseInt(aanvraagnummerLabelOut.getText());
        if(main.verwijderAanvraag(aanvraagnummer)) {
            studentenDropBoxART.setSelectedIndex(0);
            boodschapLabelART.setText("Aanvraag verwijderd!");
        } 
    }//GEN-LAST:event_verwijderLinkLabelARTMouseClicked
    
    /*
     * Methode dat de actie bepaalt bij klikken op 'Exporteer wachtlijsten' onder de tab
     * 'Aanvragen raadplegen'
     */
    private void exporteerKnopAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exporteerKnopAdminActionPerformed
        if(main.exporteerWachtlijsten())
          boodschapLabelAdminWTab.setText("Exporteren gelukt!");
        else {
          JOptionPane optionPane = new JOptionPane("<html>Het exporteren is mislukt.. "
                                        + "</br>Hou ermee rekening dat deze functie "
                                        + "</br>enkel na het aflopen van alle toewijzingen "
                                        + "</br>toegankelijk is!</html>", JOptionPane.ERROR_MESSAGE);    
          JDialog dialog = optionPane.createDialog("Fout");
          dialog.setAlwaysOnTop(true);
          dialog.setVisible(true);
        }    
    }//GEN-LAST:event_exporteerKnopAdminActionPerformed

    /*
     * Methode dat de actie bepaalt bij het zweven over 'Uitloggen'
     */
    private void uitloggenLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uitloggenLinkLabelMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_uitloggenLinkLabelMouseEntered

    /*
     * Methode dat de actie bepaalt bij het zweven over 'Uitloggen'
     */
    private void uitloggenLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uitloggenLinkLabelMouseExited
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_uitloggenLinkLabelMouseExited

    /*
     * Methode dat de actie bepaalt bij het klikken op 'Uitloggen'
     */
    private void uitloggenLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uitloggenLinkLabelMouseClicked
        AdminScherm.setVisible(false);
        FormulierScherm.setVisible(false);
        ActiveerScherm.setVisible(false);
        InlogScherm.setVisible(true);
        doorgaanKnopIS.setEnabled(false);
        InlogScherm.getRootPane().setDefaultButton(inlogKnopIS);
        gebrVeldIS.setText("");
        passVeldIS.setText("");
        boodschapLabelIS.setText("");
        studentenDropBoxVFT.removeAllItems();
        studentenDropBoxAFT.removeAllItems();
        studentenDropBoxART.removeAllItems();
        this.main = new Main();
        this.gebruiker = null;
    }//GEN-LAST:event_uitloggenLinkLabelMouseClicked

    /*
     * Methode dat de actie bepaalt bij het klikken op 'Sorteer wachtlijsten' in 
     * de tab 'Beheer wachtlijsten'
     */
    private void sorteerKnopAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sorteerKnopAdminActionPerformed
        if(main.toewijzen()) {
          boodschapLabelAdminWTab.setText("<html>De recentste wachtlijsten werden gesorteerd."
                                        + "<br>Indien er aanvragen werden afgewezen, worden emails"
                                        + "<br>verstuurd naar de ouders.</html>");
        } else {
          boodschapLabelAdminWTab.setText("<html>De toewijzing is definitief.");
        }
    }//GEN-LAST:event_sorteerKnopAdminActionPerformed

    /*
     * Methode dat de actie bepaalt bij het klikken op 'Laden' in 
     * de tab 'Beheer wachtlijsten'
     */
    private void laadLijstKnopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_laadLijstKnopActionPerformed
      ArrayList<String> csvWachtLijst = main.laadWachtLijst(Integer.parseInt(idVeldLaadLijst.getText()));
      String[] lines = new String[csvWachtLijst.size()];
      int i = 0;
      for(String str : csvWachtLijst) {
        lines[i] = str;
        i++;
      }
      jList1.setListData(lines);
    }//GEN-LAST:event_laadLijstKnopActionPerformed

    /*
     * Methode dat de actie bepaalt bij het klikken op 'Laden' in 
     * de tab 'Home' van de Formulierscherm.
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FormulierScherm.setSelectedComponent(AanmeldingsFormulierTab);
    }//GEN-LAST:event_jButton1ActionPerformed

    /*
     * Methode dat de actie bepaalt bij het klikken op 'Terug' 
     */
    private void TerugKnop2Clicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TerugKnop2Clicked
        AdminScherm.setVisible(false);
        InlogScherm.setVisible(true);
        gebrVeldIS.setText("");
        passVeldIS.setText("");
        boodschapLabelIS.setText("");
        doorgaanKnopIS.setEnabled(false);
        InlogScherm.getRootPane().setDefaultButton(inlogKnopIS);
        this.main = new Main();
        this.gebruiker = null;
    }//GEN-LAST:event_TerugKnop2Clicked

  /*
   * Methode dat de actie bepaalt bij het klikken op 'Verander' in 'Beheer scholen'-tab van
   * het adminscherm.
   */
  private void veranderCapKnopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_veranderCapKnopActionPerformed
      if(scholenTabelAdmin.getSelectedRow() > -1) {
        if(nieuweCapaciteitVeld.getText().matches("[0-9]+")) {
          main.veranderCapaciteit((int)scholenTabelAdmin.getValueAt(scholenTabelAdmin.getSelectedRow(), 3), Integer.parseInt(nieuweCapaciteitVeld.getText()));
          updateTabel(scholenTabelAdmin);
          scholenTabelAdmin.clearSelection();
          nieuweCapaciteitVeld.setText("");
          schoolGegevensPanel.setVisible(false);
          jLabel9.setText("");
        } else {
          jLabel9.setText("Voer een geldig getal in!");
        }
      } else {
        jLabel9.setText("Je moet een school selecteren!");
      }
  }//GEN-LAST:event_veranderCapKnopActionPerformed

  /*
   * Methode dat de actie bepaalt bij het veranderen van een tab in het adminscherm
   */
  private void JTPAdminStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_JTPAdminStateChanged
    schoolGegevensPanel.setVisible(false);
    scholenTabelAdmin.clearSelection();
    boodschapLabelAdminWTab.setText("");
    boodschapCapAdmin.setText("");
    deadlinesBoodschap2.setText("");
    if(JTPAdmin.getSelectedComponent().equals(tijdSchemaTab)) {
      TijdSchema ts = main.ophalenTijdSchema(LocalDateTime.now().getYear());
      createJDatePickers();
    }
    voorwaardelijkOpmaak();
  }//GEN-LAST:event_JTPAdminStateChanged

  /*
   * Methode dat de actie bepaalt bij het klikken op 'Controle uitvoeren' in de 'Beheer scholen'-tab
   */
  private void controleerCapKnopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controleerCapKnopActionPerformed
        switch(main.controleerCapaciteit()) {
          case  2:  boodschapCapAdmin.setText("Er zijn voldoende plaatsen.");
                    break;

          case  1:  boodschapCapAdmin.setText("<html>Er waren niet voldoende plaatsen."
                                 + "<br>Een email werd versuurd naar alle scholen.</html>");
                    break;

          case  0:  boodschapCapAdmin.setText("<html>Fout met server...</html>");
                    break;

          case  3:  boodschapCapAdmin.setText("<html>Deadline verstreken..</html>");
                    break;
          default:  boodschapCapAdmin.setText("<html>Onbekende fout..</html>");
                    break;          
        }
  }//GEN-LAST:event_controleerCapKnopActionPerformed

  /*
   * Methode dat de actie bepaalt bij het veranderen van het tijdschema door te klikken op 
   * 'Toepassen' in de 'Beheer data'-tab.
   */
  private void toepassenTijdSchemaKnopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toepassenTijdSchemaKnopActionPerformed
        LocalDateTime now = LocalDateTime.now();
        Year jaar = Year.of(now.getYear());
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Periode periode = main.huidigPeriode();
        TijdSchema schema = main.ophalenTijdSchema(now.getYear());
        JDatePickerImpl sd = null, idl = null, cdl = null, ed = null;
        String SD, IDL, CDL, ED;
        if(datePickerSD.isVisible())
          sd = (JDatePickerImpl)datePickerSD.getComponent(0);
        if(datePickerIDL.isVisible())
          idl = (JDatePickerImpl)datePickerIDL.getComponent(0);
        if(datePickerCDL.isVisible())
          cdl = (JDatePickerImpl)datePickerCDL.getComponent(0);
        if(datePickerED.isVisible())
          ed = (JDatePickerImpl)datePickerED.getComponent(0);
        
        try {
          if(sd != null)
            SD = sf.format(sd.getModel().getValue()) + "T" + timeSD.getSelectedItem() + ":00";
           else 
            SD = startDatumLabel.getText().replaceAll(" ", "T");
          if(idl != null)
            IDL = sf.format(idl.getModel().getValue()) + "T" + timeIDL.getSelectedItem() + ":00";
          else
            IDL = inschrijvingenDLLabel.getText().replaceAll(" ", "T");
          if(cdl != null)
            CDL = sf.format(cdl.getModel().getValue()) + "T" + timeCDL.getSelectedItem() + ":00";
          else
            CDL = capaciteitDLLabel.getText().replaceAll(" ", "T");
          if(ed != null)
            ED = sf.format(ed.getModel().getValue()) + "T" + timeED.getSelectedItem() + ":00";
          else
            ED = eindDatumLabel.getText().replaceAll(" ", "T");
          
          LocalDateTime startDatum = LocalDateTime.parse(SD);
          LocalDateTime inschrDL = LocalDateTime.parse(IDL);
          LocalDateTime capDL = LocalDateTime.parse(CDL);
          LocalDateTime eindDatum = LocalDateTime.parse(ED);
          LocalDateTime huidigDL;
          if(huidigDLLabel.getText().isEmpty()) 
            huidigDL = capDL.plusMinutes(delayHuidigDL);
          else 
            huidigDL = LocalDateTime.parse(huidigDLLabel.getText().replace(" ", "T"));

          boolean inputCheck = false;
          switch(periode) {
            case NULL:      if(startDatum.isAfter(now) && inschrDL.isAfter(startDatum) 
                                && capDL.isAfter(inschrDL) && eindDatum.isAfter(capDL)) {
                                  huidigDL = capDL.plusMinutes(delayHuidigDL);
                                  TijdSchema ts = new TijdSchema(jaar, startDatum, inschrDL, 
                                                                 capDL, huidigDL, eindDatum);
                                  if(huidigDL.isBefore(eindDatum)) {
                                      main.veranderTijdSchema(ts);
                                      inputCheck = true;
                                  }
                            }
                            break;
            case INSCHR:    if(inschrDL.isAfter(now) && capDL.isAfter(inschrDL) 
                                && eindDatum.isAfter(capDL)) {                                    
                                    huidigDL = capDL.plusMinutes(delayHuidigDL);
                                    TijdSchema ts = new TijdSchema(jaar, schema.getStartDatum(), 
                                                              inschrDL, capDL, huidigDL, eindDatum);
                                    if(huidigDL.isBefore(eindDatum)) {
                                      main.veranderTijdSchema(ts);
                                      inputCheck = true;
                                    }
                            }
                            break;
            case CAP:       if(capDL.isAfter(now) && eindDatum.isAfter(capDL)) {
                              huidigDL = capDL.plusMinutes(delayHuidigDL);
                              TijdSchema ts = new TijdSchema(jaar, schema.getStartDatum(), 
                                                             schema.getInschrijvingenDeadline(), 
                                                             capDL, huidigDL, eindDatum);
                              if(huidigDL.isBefore(eindDatum)) {
                                main.veranderTijdSchema(ts);
                                inputCheck = true;
                              }
                            }
                            break;
            case VOORKEUR:  if(eindDatum.isAfter(now)) {
                              TijdSchema ts = new TijdSchema(jaar, schema.getStartDatum(), 
                                                             schema.getInschrijvingenDeadline(), 
                                                             schema.getCapaciteitDeadline(), 
                                                             huidigDL, eindDatum);
                              if(huidigDL.isBefore(eindDatum)) {
                                main.veranderTijdSchema(ts);
                                inputCheck = true;
                              }
                            }
                            break;
            default:        System.exit(0);
          }
          if(inputCheck) {
            JTPAdmin.setSelectedIndex(0);
            JTPAdmin.setSelectedComponent(tijdSchemaTab);
            deadlinesBoodschap2.setText("<html>Het tijdschema is aangepast.</html>");
          }
          else {
            deadlinesBoodschap2.setText("<html>Controleer je input!"
                                        + "<br>Je kan enkel toekomstige datums ingeven!"
                                        + "<br>De elkaar opvolgende deadlines moeten"
                                        + "<br>logisch zijn. Tussen het deadline voor de "
                                        + "<br>capaciteit en het einddatum moet er minstens"
                                        + "<br>een verschil zijn van vier dagen.</html>");
          }
        } catch (Exception e) {
          deadlinesBoodschap2.setText("Vul alle waarden in!");
        }
        
  }//GEN-LAST:event_toepassenTijdSchemaKnopActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel AanmeldingsFormulierTab;
  private javax.swing.JPanel ActiveerScherm;
  private javax.swing.JPanel AdminScherm;
  private javax.swing.JLabel AlgemeneTekstOuders;
  private javax.swing.JTabbedPane FormulierScherm;
  private javax.swing.JPanel HomeTab;
  private javax.swing.JPanel InlogScherm;
  private javax.swing.JTabbedPane JTPAdmin;
  private javax.swing.JPanel MainPanel;
  private javax.swing.JLabel PersoonlijkeJlabel;
  private javax.swing.JButton TerugKnop2;
  private javax.swing.JPanel UitloggenTab;
  private javax.swing.JPanel VoorkeurFormulierTab;
  private javax.swing.JPanel ZoekAanvraagTab;
  private javax.swing.JLabel aanvraagNummerLabelVFT;
  private javax.swing.JLabel aanvraagnummerLabelART;
  private javax.swing.JLabel aanvraagnummerLabelOut;
  private javax.swing.JTextField aanvraagnummerVeldVFT;
  private javax.swing.JButton activeerKnopAS;
  private javax.swing.JLabel activeerLinkLabelIS;
  private javax.swing.JLabel adresLabelAFT;
  private javax.swing.JLabel adresLabelAS;
  private javax.swing.JTextField adresVeldAFT;
  private javax.swing.JTextField adresVeldAS;
  private javax.swing.JLabel boodschapCapAdmin;
  private javax.swing.JLabel boodschapLabelAFT;
  private javax.swing.JLabel boodschapLabelART;
  private javax.swing.JLabel boodschapLabelAS;
  private javax.swing.JLabel boodschapLabelAdminWTab;
  private javax.swing.JLabel boodschapLabelIS;
  private javax.swing.JLabel boodschapLabelVFT;
  private javax.swing.JLabel capaciteitDLLabel;
  private javax.swing.JButton controleerCapKnop;
  private javax.swing.JButton datePickerCDL;
  private javax.swing.JButton datePickerED;
  private javax.swing.JButton datePickerIDL;
  private javax.swing.JLabel datePickerLabel1;
  private javax.swing.JLabel datePickerLabel2;
  private javax.swing.JLabel datePickerLabel3;
  private javax.swing.JLabel datePickerLabel4;
  private javax.swing.JButton datePickerSD;
  private javax.swing.JLabel deadlinesBoodschap;
  private javax.swing.JLabel deadlinesBoodschap2;
  private javax.swing.JButton doorgaanKnopIS;
  private javax.swing.JPanel editPanel;
  private javax.swing.JLabel eersteVoorkeurLabel;
  private javax.swing.JLabel eersteVoorkeurLabelOut;
  private javax.swing.JLabel eindDatumLabel;
  private javax.swing.JLabel emailLabelAFT;
  private javax.swing.JLabel emailLabelAS;
  private javax.swing.JTextField emailVeldAFT;
  private javax.swing.JTextField emailVeldAS;
  private javax.swing.JButton exporteerKnopAdmin;
  private javax.swing.JLabel gebrLabelAS;
  private javax.swing.JTextField gebrVeldIS;
  private javax.swing.JPanel gegevensOuderAFT;
  private javax.swing.JPanel gegevensOuderAS;
  private javax.swing.JPanel gegevensPanelART;
  private javax.swing.JPanel gegevensStudentAFT;
  private javax.swing.JLabel huidigDLLabel;
  private javax.swing.JTextField idVeldLaadLijst;
  private javax.swing.JButton indienenKnopAFT;
  private javax.swing.JButton indienenKnopVFT;
  private javax.swing.JLabel infoLabelART;
  private javax.swing.JLabel infoLabelVFT;
  private javax.swing.JButton inlogKnopIS;
  private javax.swing.JLabel inschrijvingenDLLabel;
  private javax.swing.JLabel instructieLabelAdminScholenTab;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel12;
  private javax.swing.JLabel jLabel13;
  private javax.swing.JLabel jLabel14;
  private javax.swing.JLabel jLabel15;
  private javax.swing.JLabel jLabel16;
  private javax.swing.JLabel jLabel17;
  private javax.swing.JLabel jLabel18;
  private javax.swing.JLabel jLabel19;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel20;
  private javax.swing.JLabel jLabel21;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JList<String> jList1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JTextField jTextField2;
  private javax.swing.JTextField jTextField3;
  private javax.swing.JTextField jTextField4;
  private javax.swing.JTextField jTextField5;
  private javax.swing.JTextField jTextField6;
  private javax.swing.JButton laadLijstKnop;
  private javax.swing.JPanel leesPanel;
  private javax.swing.JScrollPane lijstScrollPane;
  private javax.swing.JLabel logoLabel;
  private javax.swing.JLabel logoLabel1;
  private javax.swing.JLabel naamLabelAS;
  private javax.swing.JLabel naamOuderLabelAFT;
  private javax.swing.JTextField naamOuderVeldAFT;
  private javax.swing.JLabel naamStudentLabelAFT;
  private javax.swing.JTextField naamStudentVeldAFT;
  private javax.swing.JTextField naamVeldAS;
  private javax.swing.JLabel nieuweCapaciteitLabel;
  private javax.swing.JTextField nieuweCapaciteitVeld;
  private javax.swing.JLabel passLabelIS;
  private javax.swing.JPasswordField passVeldIS;
  private javax.swing.JLabel rijksnumLabelAS;
  private javax.swing.JLabel rijksnumOuderLabelAFT;
  private javax.swing.JTextField rijksnumOuderVeldAFT;
  private javax.swing.JLabel rijksnumStudentLabelAFT;
  private javax.swing.JLabel rijksnumStudentLabelART;
  private javax.swing.JLabel rijksnumStudentLabelVFT;
  private javax.swing.JTextField rijksnumVeldAS;
  private javax.swing.JScrollPane scholenScrollPane;
  private javax.swing.JScrollPane scholenScrollPaneAdmin;
  private javax.swing.JPanel scholenTab;
  private javax.swing.JTable scholenTabel;
  private javax.swing.JTable scholenTabelAdmin;
  private javax.swing.JPanel schoolGegevensPanel;
  private javax.swing.JLabel selectieBoodschapLabel;
  private javax.swing.JButton sorteerKnopAdmin;
  private javax.swing.JLabel startDatumLabel;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JLabel statusLabelOut;
  private javax.swing.JComboBox<String> studentenDropBoxAFT;
  private javax.swing.JComboBox<String> studentenDropBoxART;
  private javax.swing.JComboBox<String> studentenDropBoxVFT;
  private javax.swing.JLabel telnumLabelAFT;
  private javax.swing.JTextField telnumVeldAFT;
  private javax.swing.JLabel terugLinkLabelAS;
  private javax.swing.JPanel tijdSchemaTab;
  private javax.swing.JLabel tijdstipLabel;
  private javax.swing.JLabel tijdstipLabelOut;
  private javax.swing.JComboBox<String> timeCDL;
  private javax.swing.JComboBox<String> timeED;
  private javax.swing.JComboBox<String> timeIDL;
  private javax.swing.JComboBox<String> timeSD;
  private javax.swing.JButton toepassenTijdSchemaKnop;
  private javax.swing.JPanel uitlogAdminTab;
  private javax.swing.JLabel uitloggenLinkLabel;
  private javax.swing.JLabel uitloggenLinkLabel1;
  private javax.swing.JButton veranderCapKnop;
  private javax.swing.JLabel verwijderLinkLabelART;
  private javax.swing.JLabel voornaamLabelAS;
  private javax.swing.JLabel voornaamOuderLabelAFT;
  private javax.swing.JTextField voornaamOuderVeldAFT;
  private javax.swing.JLabel voornaamStudentLabelAFT;
  private javax.swing.JTextField voornaamStudentVeldAFT;
  private javax.swing.JTextField voornaamVeldAS;
  private javax.swing.JLabel waarschuwingLabelAFT;
  private javax.swing.JPanel wachtLijstenTab;
  private javax.swing.JLabel zoekwoordLabel;
  private javax.swing.JLabel zoekwoordLabelAdmin;
  private javax.swing.JTextField zoekwoordVeld;
  private javax.swing.JTextField zoekwoordVeldAdmin;
  // End of variables declaration//GEN-END:variables
}
