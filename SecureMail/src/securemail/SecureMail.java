/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securemail;

import com.sun.scenario.effect.impl.Renderer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Hochmoa
 */
public class SecureMail extends Application {

    int windowX = (int)(940);
    int windowY = (int)(700);
    int sideBarWidth = 100;
    int menuBarHeight = 25;
    int blueButtonHeight = 30;
    int separatorStrokeWidth = 4;
    String selectedFolder = "Inbox";
    final String username = "secomailtest@gmail.com";//wird noch in file geschrieben
    private double labelWidth = 100;
    LinkedList<MailDetails> mailList = new LinkedList<MailDetails>();
    VBox contentList=new VBox();
    Pane emailDetails=new Pane();
    private int xOffset=3;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SecureMail");

        Pane root = new Pane();

        createMenu(primaryStage, root);
        createSideBar(root);
        createOptionsBar(root);
        Scene scene = new Scene(root, windowX, windowY);
        primaryStage.setMinWidth(windowX + 10);
        primaryStage.setMinHeight(windowY + 10);
        scene.getStylesheets().add("resources/myStyle.css");
        primaryStage.setScene(scene);
        primaryStage.show();
        getMails();
        createList(root);
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {

                onResize();
            }

        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {

                onResize();
            }
        });
    }

    private void onResize() {
        System.out.println("LOOOL");
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void createList(Pane root) {
        //getMails

        contentList = new VBox();
        root.getChildren().remove(emailDetails);
        mailList.stream().forEach((e) -> {
            HBox entry = new HBox();
            CheckBox box = new CheckBox();

            Label labSender = new Label(e.sender);
            Label labHeaderText = new Label(e.subject);
            Label labDate = new Label(e.date);

            entry.getChildren().addAll(box, labSender, labHeaderText, labDate);

            contentList.getChildren().add(entry);
            contentList.getChildren().add(new Separator(Orientation.HORIZONTAL));

            labSender.setPrefWidth((windowX-sideBarWidth)/4);
            labHeaderText.setPrefWidth((windowX-sideBarWidth)/2);
            //labHeaderText.setAlignment(Pos.CENTER);
            labDate.setPrefWidth((windowX-sideBarWidth)/4-xOffset);
           // labDate.setAlignment(Pos.CENTER_RIGHT);

            entry.setUserData(e.id);

            entry.setOnMouseClicked(actionEvent -> showFullMail(entry.getUserData(),root));

        });
        contentList.setLayoutX(sideBarWidth + xOffset);
        contentList.setLayoutY(blueButtonHeight + menuBarHeight);

        if (mailList.isEmpty()) {
            Label labInfo=new Label("This Inbox is empty.");
            contentList.getChildren().add(labInfo);
        }
        root.getChildren().add(contentList);

    }

    public void sendMail() {

        final String password = "tlolzphg";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("secomailtest@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("mani.mair@gmail.com"));
            message.setSubject("Testing Subject");
            message.setText("Hi manuel, this mail was sent automatically. #nojoke");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void getMails() {
        mailList.clear();
        contentList.getChildren().clear();
        String defaultMsg = "Abnormal";
        Message[] msgs = null;
        try {
            //SOLL IN EIGENEN THREAD!!!!!!!

            // final String username = "secomailtest@gmail.com";
            //final String password = "tlolzphg";
            final String username = "secomailtest@gmail.com";
            final String password = "tlolzphg";
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", username, password);
            //get unread Messages
            Folder[] f = store.getDefaultFolder().list("*");
            for (Folder fd : f) {
                System.out.println(">> " + fd.getName());
            }
            Folder selectedFolderObject = null;
            switch (selectedFolder) {
                case "Inbox": {
                    selectedFolderObject = store.getFolder("Inbox");
                    break;
                }
                case "Sent": {
                    selectedFolderObject = store.getFolder("[Gmail]/Gesendet");
                    break;
                }

                case "Spam": {
                    selectedFolderObject = store.getFolder("[Gmail]/Spam");
                    break;
                }
                case "All Messages": {
                    selectedFolderObject = store.getFolder("[Gmail]/Alle Nachrichten");
                    break;
                }
            }
            /*>> INBOX
             >> [Gmail]
             >> Alle Nachrichten
             >> Entwürfe
             >> Gesendet
             >> Markiert
             >> Papierkorb
             >> Spam
             >> Wichtig*/
            selectedFolderObject.open(Folder.READ_ONLY);

            msgs = selectedFolderObject.getMessages();
            int i = 0;
            for (Message msg : msgs) {
                MailDetails le = new MailDetails();
                if (msg.getFrom()[0].toString().equals(username)) {
                    le.sender = msg.getRecipients(Message.RecipientType.TO)[0].toString();

                } else {
                    System.out.println("Sender: " + le.sender);
                    le.sender = msg.getFrom()[0].toString().split("<")[1];
                    le.sender = le.sender.substring(0, le.sender.length() - 1);

                }

                le.subject = msg.getSubject();

                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm, dd-MM-yyyy");
                le.date = formatter.format(msg.getSentDate());
                le.subject = msg.getSubject();

                try {
                    //le.message = manageMultipart(msg);
                    le.message = getText(msg);
                } catch (IOException ex) {
                    Logger.getLogger(SecureMail.class.getName()).log(Level.SEVERE, null, ex);
                }
                le.id = i;
                mailList.add(le);
                i++;
                if(i>1)break;
            }

        } catch (MessagingException ex) {
            Logger.getLogger(SecureMail.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (msgs == null) {
            createMessage(AlertType.ERROR, "Error", "Fetching", "Error occured while fetching your mails.");
            defaultMsg = "An Error occured. Please refresh.";
        }
        if (msgs.length == 0) {
            defaultMsg = "Nothing to show here.";
        }
    }
private String getText(Part p) throws
                MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            //textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
    public String manageMultipart(Message message) {
        String content = null;
        try {
            
            
            
            
            Multipart multipart = (Multipart) message.getContent();

            for (int j = 0; j < multipart.getCount(); j++) {

                BodyPart bodyPart = multipart.getBodyPart(j);

                String disposition = bodyPart.getDisposition();

                if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) { // BodyPart.ATTACHMENT doesn't work for gmail
                    System.out.println("Mail have some attachment");

                    DataHandler handler = bodyPart.getDataHandler();
                    System.out.println("file name : " + handler.getName());
                } else {
                    System.out.println("Body: " + bodyPart.getContent());
                    content = bodyPart.getContent().toString();
                }
            }

            return content;

        } catch (IOException ex) {
            Logger.getLogger(SecureMail.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(SecureMail.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;

    }

    private MenuBar createMenu(Stage primaryStage, Pane root) {
        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
       // root.setAlignment(menuBar, Pos.TOP_CENTER);

        // File menu - new, save, exit
        Menu fileMenu = new Menu("File");

        MenuItem newMenuItem = new MenuItem("New");
        MenuItem saveMenuItem = new MenuItem("Save");
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(actionEvent -> Platform.exit());

        fileMenu.getItems().addAll(newMenuItem, saveMenuItem,
                new SeparatorMenuItem(), exitMenuItem);

        Menu securityMenu = new Menu("Security");
        Menu asymmetricMenu = new Menu("Asymmetric Encryption");
        Menu symmetricMenu = new Menu("Symmetric Encryption");
        Menu hashMenu = new Menu("Hash Algorithm");

        ToggleGroup asToggleGroup = new ToggleGroup();
        ToggleGroup sToggleGroup = new ToggleGroup();
        ToggleGroup haToggleGroup = new ToggleGroup();

        RadioMenuItem as1radItem = new RadioMenuItem("RSA");
        RadioMenuItem as2radItem = new RadioMenuItem("DSA");
        RadioMenuItem as3radItem = new RadioMenuItem("Diffie Hellman");
        as1radItem.setToggleGroup(asToggleGroup);
        as2radItem.setToggleGroup(asToggleGroup);
        as3radItem.setToggleGroup(asToggleGroup);

        RadioMenuItem s1radItem = new RadioMenuItem("AES");
        RadioMenuItem s2radItem = new RadioMenuItem("Triple DES");
        RadioMenuItem s3radItem = new RadioMenuItem("IDEA");
        s1radItem.setToggleGroup(sToggleGroup);
        s2radItem.setToggleGroup(sToggleGroup);
        s3radItem.setToggleGroup(sToggleGroup);

        RadioMenuItem ha1radItem = new RadioMenuItem("MD5");
        RadioMenuItem ha2radItem = new RadioMenuItem("SHA-256");
        RadioMenuItem ha3radItem = new RadioMenuItem("SHA-512");
        ha1radItem.setToggleGroup(haToggleGroup);
        ha2radItem.setToggleGroup(haToggleGroup);
        ha3radItem.setToggleGroup(haToggleGroup);

        asymmetricMenu.getItems().addAll(as1radItem, as2radItem, as3radItem);
        symmetricMenu.getItems().addAll(s1radItem, s2radItem, s3radItem);
        hashMenu.getItems().addAll(ha1radItem, ha2radItem, ha3radItem);

        securityMenu.getItems().addAll(asymmetricMenu, symmetricMenu, hashMenu);

        menuBar.getMenus().addAll(fileMenu, securityMenu);

        root.getChildren().add(menuBar);
        return menuBar;
    }

    private void createSideBar(Pane root) {
        VBox sideMenu = new VBox();
        sideMenu.setPrefWidth(sideBarWidth);

        Button b1 = new Button("Inbox");
        Button b2 = new Button("Sent");
        Button b3 = new Button("Spam");
        Button b4 = new Button("All Messages");

        b1.getStyleClass().add("SideButton");
        b2.getStyleClass().add("SideButton");
        b3.getStyleClass().add("SideButton");

        b4.getStyleClass().add("SideButton");

        b1.setMinWidth(sideMenu.getPrefWidth());
        b2.setMinWidth(sideMenu.getPrefWidth());
        b3.setMinWidth(sideMenu.getPrefWidth());

        b4.setMinWidth(sideMenu.getPrefWidth());

        b1.setMinHeight(blueButtonHeight);
        b2.setMinHeight(blueButtonHeight);
        b3.setMinHeight(blueButtonHeight);

        b4.setMinHeight(blueButtonHeight);

        b1.setOnAction((ActionEvent e) -> {
            if ("Inbox".equals(selectedFolder)) {
                showList(root);
            } else {

                selectedFolder = "Inbox";
                getMails();
                createList(root);
            }
        });
        b2.setOnAction((ActionEvent e) -> {
            if ("Sent".equals(selectedFolder)) {
                showList(root);
            } else {
                selectedFolder = "Sent";
                getMails();
                createList(root);
            }
        });

        b3.setOnAction((ActionEvent e) -> {
            if ("Spam".equals(selectedFolder)) {
                showList(root);
            } else {
                selectedFolder = "Spam";
                getMails();
                createList(root);
            }
        });
        b4.setOnAction((ActionEvent e) -> {
            if ("All Messages".equals(selectedFolder)) {
                
                showList(root);
            } else {
                selectedFolder = "All Messages";
                getMails();
                createList(root);
            }
        });

        Button btnFake = new Button("");

        btnFake.getStyleClass().add("FakeButton");
        btnFake.setMinWidth(sideMenu.getPrefWidth());
        btnFake.setMinHeight(windowY);

        sideMenu.getChildren().addAll(b1, b2, b3, b4, btnFake);

        root.getChildren().add(sideMenu);
        sideMenu.setLayoutY(menuBarHeight);

    }

    private void createOptionsBar(Pane root) {
        HBox optionsBar = new HBox();

        Image imgRead = new Image(getClass().getResourceAsStream("/resources/ok.png"));
        Image imgDelete = new Image(getClass().getResourceAsStream("/resources/delete.png"));
        Image imgRefresh = new Image(getClass().getResourceAsStream("/resources/refresh.png"));

        Button btnRead = new Button("Read", new ImageView(imgRead));
        Button btnDelete = new Button("Delete", new ImageView(imgDelete));
        Button btnRefresh = new Button("Refresh", new ImageView(imgRefresh));

        btnRead.getStyleClass().add("OptionsButton");
        btnDelete.getStyleClass().add("OptionsButton");
        btnRefresh.getStyleClass().add("OptionsButton");

        btnRead.setMinHeight(blueButtonHeight);
        btnDelete.setMinHeight(blueButtonHeight);
        btnRefresh.setMinHeight(blueButtonHeight);

        Label labSearch = new Label("Search:");
        TextField txtSearch = new TextField();

        labSearch.setMinHeight(blueButtonHeight);
        txtSearch.setMinHeight(blueButtonHeight);

        labSearch.getStyleClass().add("OptionsButton");
        txtSearch.getStyleClass().add("SearchField");

        Button btnFake = new Button("");

        btnFake.getStyleClass().add("FakeButton");
        btnFake.setMinWidth(1000);
        btnFake.setMinHeight(blueButtonHeight);

        optionsBar.getChildren().addAll(btnRead, btnDelete, btnRefresh, labSearch, txtSearch, btnFake);
        optionsBar.setLayoutY(menuBarHeight);
        optionsBar.setLayoutX(sideBarWidth);
        root.getChildren().add(optionsBar);
        Line line = new Line(sideBarWidth, menuBarHeight + separatorStrokeWidth / 2, sideBarWidth, windowY);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(separatorStrokeWidth);
        root.getChildren().add(line);

    }

    private void createMessage(AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();

    }

    private void showFullMail(Object userData,Pane root) {
        root.getChildren().remove(emailDetails);
        System.out.println("Click on " + userData);
        contentList.setVisible(false);
        emailDetails=new Pane();
        MailDetails le=mailList.get((int)userData);
        
        Label labTo=new Label("Sent by: "+le.sender);
        
        Label date=new Label(le.date);
        labTo.setMinWidth(windowX-sideBarWidth*3);
        labTo.setMaxWidth(windowX-sideBarWidth*3);
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);
        webEngine.loadContent(le.message);
        
        root.getChildren().addAll(scrollPane);
        
        scrollPane.maxWidth(windowX-sideBarWidth-xOffset*10);
        scrollPane.prefHeight(windowY-menuBarHeight-blueButtonHeight);
        HBox hBox=new HBox();
        VBox vBox=new VBox();
        hBox.getChildren().add(labTo);
        hBox.getChildren().add(date);
        vBox.getChildren().add(hBox);
        vBox.getChildren().add(scrollPane);
        emailDetails.getChildren().add(vBox);
        root.getChildren().add(emailDetails);
        scrollPane.setLayoutY(blueButtonHeight);
        emailDetails.setLayoutX(sideBarWidth+separatorStrokeWidth);
        emailDetails.setLayoutY(menuBarHeight+blueButtonHeight);
       
    }

    private void showList(Pane root) {
        contentList.setVisible(true);
        root.getChildren().remove(emailDetails);
    }

}
