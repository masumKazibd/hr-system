module com.hrsystem.hrsystem {
    requires javafx.controls;
    requires javafx.fxml;

//    requires org.kordamp.bootstrapfx.core;
    opens com.hrsystem.hrsystem.model to javafx.base;
    requires java.sql;

    opens com.hrsystem.hrsystem.controller to javafx.fxml;
    exports com.hrsystem.hrsystem;
}