module uf5.mp3.adivinafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens uf5.mp3.adivinafx to javafx.fxml;
    exports uf5.mp3.adivinafx;
    exports uf5.mp3.adivinafx.control;
    opens uf5.mp3.adivinafx.control to javafx.fxml;
    exports uf5.mp3.adivinafx.net;
    opens uf5.mp3.adivinafx.net to javafx.fxml;
}