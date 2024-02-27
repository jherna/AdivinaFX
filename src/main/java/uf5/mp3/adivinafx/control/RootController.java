package uf5.mp3.adivinafx.control;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
    @FXML
    VBox vBoxLeft;
    @FXML
    VBox vBoxRight;
    @FXML
    Label lblEstatJoc;


    String resp = "";
    String nom;
    EstatJoc estatJoc = null;
    private Jugada jugada;;


    DatagramSocketClient client = new DatagramSocketClient() {
        @Override
        public void getResponse(byte[] data, int length) {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            try {
                ObjectInputStream ois = new ObjectInputStream(is);
                estatJoc = (EstatJoc) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            lblResponse.setText(estatJoc.getResponse());
            StringBuilder stringEstat = new StringBuilder();
            estatJoc.jugadors.forEach((j,i)->stringEstat.append(j + ":" + i + "\n"));
            lblEstatJoc.setText(stringEstat.toString());

        }

        @Override
        public byte[] getRequest() {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            //jugada = new Jugada();
            if(estatJoc == null) {
                jugada.setTirada(-2); //tirada d'inicialització
            }

            try {
                oos = new ObjectOutputStream(os);
                oos.writeObject(jugada);
                oos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return os.toByteArray();
        }

        @Override
        public boolean mustContinue(byte[] data) {
            return !resp.equals("Correcte");
        }
    };




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vBoxLeft.setMaxWidth((double) AdivinaApp.MAIN_WINDOW_WIDTH / 2);
        vBoxRight.setMaxWidth((double) AdivinaApp.MAIN_WINDOW_WIDTH / 2);

        lblResponse.setText("Welcome to Adivina!");
        //TODO Verificar si estàs o no connectat abans d'enviar un num
        //TODO Tractar els errors que puguin donar les connexions al servidor i la rx del client
        //TODO Automatitzar el sistea de torns (ara és manual i s'ha d'acualitzar fent click al botó update)
        jugada = new Jugada();;

    }

    private void enableControlsTurn() {
        if(estatJoc != null) {
            if(estatJoc.getTurn().equals(nom)) {
                btnSubmit.setDisable(false);
                txtNum.setDisable(false);
            } else {
                btnSubmit.setDisable(true);
                txtNum.setDisable(true);
            }
            txtNum.setText("");
        }
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
                jugada.setNom(nom);
                return new Pair<>(txtIp.getText(),Integer.parseInt(txtPort.getText()));
            }
            return null;
        });

        Optional<Pair<String,Integer>> result = dialog.showAndWait();

        if(result.isPresent()) {
            try {
                client.init(result.get().getKey(), result.get().getValue());
                client.runClient();
                Thread.sleep(500);
                circleClient.setFill(Color.BLUE);
                lblResponse.setText("Connectat, comença!");
                if(!estatJoc.getTurn().equals(nom)) {
                    timer.start();
                }

            } catch (SocketException | UnknownHostException | InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @FXML
    public void clickSubmit(MouseEvent mouseEvent) {
        if(!txtNum.getText().isEmpty() && !resp.equals("Correcte")) {
            try {
                jugada.setTirada(Integer.parseInt(txtNum.getText()));
                client.runClient();
                enableControlsTurn();
                timer.start();

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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thServer.start();
        }


    }


    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            try {
                jugada.setTirada(-1);
                client.runClient();
                if(estatJoc.getTurn().equals(nom)) {
                    stop();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            enableControlsTurn();
        }
    };
}