module org.lojaprodutos {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.lojaprodutos to javafx.fxml;
    exports org.lojaprodutos;
    exports org.lojaprodutos.controller;
    opens org.lojaprodutos.controller to javafx.fxml;
}