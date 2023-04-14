package uf5.mp3.adivinafx.control;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;
import uf5.mp3.adivinafx.AdivinaApp;
import uf5.mp3.adivinafx.model.EstatJoc;
import uf5.mp3.adivinafx.model.Jugada;
import uf5.mp3.adivinafx.net.DatagramSocketClient;
import uf5.mp3.adivinafx.net.DatagramSocketServer;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.ResourceBundle;

public class RootController implements Initializable {
    @FXML
    private Label lblResponse;
    @FXML
    TextField txtNum;
    @FXML
    Button btnSubmit;
    @FXML
    Circle circleServer;
    @FXML
    Circle circleClient;

    String resp = "";
    String nom;


    DatagramSocketClient client = new DatagramSocketClient() {
        @Override
        public void getResponse(byte[] data, int length) {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            EstatJoc estatJoc = null;
            try {
                ObjectInputStream ois = new ObjectInputStream(is);
                estatJoc = (EstatJoc) ois.readObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            lblResponse.setText(estatJoc.getResponse());
        }

        @Override
        public byte[] getRequest() {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            Jugada jugada = new Jugada();
            jugada.setTirada(Integer.parseInt(txtNum.getText()));
            jugada.setNom(nom);
            try {
                oos = new ObjectOutputStream(os);
                oos.writeObject(jugada);
                oos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //return txtNum.getText().getBytes();
            return os.toByteArray();
        }

        @Override
        public boolean mustContinue(byte[] data) {
            return !resp.equals("Correcte");
        }
    };




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblResponse.setText("Welcome to Adivina!");
        //TODO Verificar si estàs o no connectat abans d'enviar un num
        //TODO Tractar els errors que puguin donar les connexions al servidor i la rx del client
    }

    @FXML
    public void menuItemConnection(ActionEvent actionEvent) {

        Dialog<Pair<String,Integer>> dialog = new Dialog<>();
        dialog.setTitle("Client configuration");
        dialog.setHeaderText("Dades per la connexió al servidor");
        ButtonType conButton = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(conButton,ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20,150,10,10));

        TextField txtIp = new TextField();
        txtIp.setPromptText("IP");
        TextField txtPort = new TextField();
        txtPort.setPromptText("Port");
        TextField txtNom = new TextField();
        txtNom.setPromptText("Nom");


        gridPane.add(new Label("IP:"), 0, 0);
        gridPane.add(txtIp, 1, 0);
        gridPane.add(new Label("Port:"), 0, 1);
        gridPane.add(txtPort, 1, 1);
        gridPane.add(new Label("Nom:"), 0, 2);
        gridPane.add(txtNom, 1, 2);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(txtIp::requestFocus);

        dialog.setResultConverter(dButton -> {
            if(dButton == conButton) {
                nom = txtNom.getText();
                return new Pair<>(txtIp.getText(),Integer.parseInt(txtPort.getText()));
            }
            return null;
        });

        Optional<Pair<String,Integer>> result = dialog.showAndWait();

        if(result.isPresent()) {
            try {
                client.init(result.get().getKey(), result.get().getValue());
                Thread.sleep(500);
                circleClient.setFill(Color.BLUE);
                lblResponse.setText("connectat. Comença!");
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void clickSubmit(MouseEvent mouseEvent) {


        if(!txtNum.getText().equals("") && !resp.equals("Correcte")) {
            try {
                client.runClient();
                txtNum.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickClose(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void menuItemActiveServer(ActionEvent actionEvent) {
        showConfigServer();
        circleServer.setFill(Color.BLUE);

    }

    public void showConfigServer() {
        TextInputDialog dialog = new TextInputDialog("5555");
        dialog.setTitle("Config Server");
        dialog.setHeaderText("Activació del servidor local");
        dialog.setContentText("Port");
        dialog.setGraphic(new ImageView(AdivinaApp.class.getResource("images/server.png").toString()));
        Optional<String> result = dialog.showAndWait();
        if(result.isPresent()) {
            DatagramSocketServer server = new DatagramSocketServer();
            Thread thServer = new Thread(() -> {
                try {
                    server.init(Integer.parseInt(result.get()));
                    server.runServer();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thServer.start();
        }

    }
}