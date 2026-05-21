module com.plasticstock {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;

    opens com.plasticstock to javafx.fxml;
    opens com.plasticstock.controllers to javafx.fxml;
    opens com.plasticstock.models to javafx.base;

    exports com.plasticstock;
    exports com.plasticstock.controllers;
    exports com.plasticstock.models;
}
