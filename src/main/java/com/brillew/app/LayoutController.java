package com.brillew.app;

/*
 * This class is used as the controller for the FXML files that make up the layout. Most of the methods are named based on their
 * functionality within the context of the GUI. This class is a controller to two FXML files: it is responsible for the main layout,
 * but it also has two methods (setupAddCustomer() and addCustomer()) which are used to control a secondary FXML that is just for adding
 * new customers to the database.
 */

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class LayoutController {

    @FXML
    public TextField startAddress;
    @FXML
    public TextField endAddress;
    @FXML
    public TextField distanceFromRoute;
    private RouterMain routerMain;
    @FXML
    public TextField customerName;
    @FXML
    public TextField customerAddress;
    @FXML
    public TextField customerCity;
    @FXML
    public TextField customerState;
    @FXML
    public TextField customerZIP;
    public static Stage stage;


    public void setRouterMain(RouterMain myRouterMain) {
        this.routerMain = myRouterMain;
    }

    @FXML
    public void getTextFromFields() throws InterruptedException, IOException {
        clearScreen();
        if (startAddress.getText().equals("") || endAddress.getText().equals("") || distanceFromRoute.getText().equals("")) {
            System.out.println("Please fill in all parameters");
            routerMain.addressList.add(startAddress.getText());
            routerMain.addressList.add(endAddress.getText());
            routerMain.addressList.add(distanceFromRoute.getText());
            routerMain.displayAddresses();
        } else {
            routerMain.withStop = false;
            routerMain.geocodeQuery(startAddress.getText());
            routerMain.geocodeQuery(endAddress.getText());
            routerMain.additionalDistance = Integer.parseInt(distanceFromRoute.getText());
            routerMain.withStop = true;
            routerMain.getAddress();
        }
    }

    @FXML
    public void stop() {
        routerMain.stop();
    }

    @FXML
    public void aboutText() {
        clearScreen();
        Text aboutText = new Text();
        aboutText.setText("Developed by Evan Brill. For help, please contact evanwb50@gmail.com");
        aboutText.setX(200);
        aboutText.setY(350);
        Pane pane = (Pane) routerMain.layout.getChildrenUnmodifiable().get(1);
        pane.getChildren().add(aboutText);
    }

    @FXML
    public void clearScreen() {
        Pane pane = (Pane) routerMain.layout.getChildrenUnmodifiable().get(1);
        ArrayList<Node> toRemove = new ArrayList<>();
        for (int i = 0; i < pane.getChildren().size(); i++) {
            if (pane.getChildren().get(i) instanceof Text) {
                if (!((Text) pane.getChildren().get(i)).getText().equals("Welcome to H.E.L.P.")) {
                    toRemove.add(pane.getChildren().get(i));
                }
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            pane.getChildren().remove(toRemove.get(i));
        }
        routerMain.addressList.clear();
    }

    @FXML
    public void setupAddCustomer() throws IOException {
        stage = new Stage();
        stage.setTitle("Add a new customer");
        stage.setWidth(400);
        stage.setHeight(350);
        stage.show();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/newCustomerWindow.fxml"));
        Parent layout = loader.load();
        Scene scene = new Scene(layout);
        stage.setScene(scene);
    }

    @FXML
    public void addCustomer() throws IOException {
        String name = customerName.getText();
        String address = customerAddress.getText();
        String city = customerCity.getText();
        String state = customerState.getText();
        int zip = Integer.parseInt(customerZIP.getText());
        DatabaseInterface db = new DatabaseInterface();
        if (db.addCustomer(name, address, city, state, zip)) {
            stage.close();
        }
    }

}