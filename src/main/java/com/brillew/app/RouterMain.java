package com.brillew.app;

/*
 * This is the main class which contains the primary functionality of the application. It uses two helper classes to control the GUI
 * as well as interact with the excel file that functions as a customer database. Several of the methods used were borrowed from the ArcGIS
 * starter tutorials, which can be found at the following page: https://developers.arcgis.com/labs/browse/?topic=any&product=java
 * Some of these methods were modified to fit this specific application, and a few helper methods were introduced to display suitable addresses
 * and retrieve addresses from the helper class.
 */

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.OAuthConfiguration;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.networkanalysis.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RouterMain extends Application {

    private Point startPoint;
    private Point endPoint;
    private Point midPoint;

    private GeocodeParameters geocodeParameters;
    private LocatorTask locatorTask;

    private RouteTask solveRouteTask;
    private RouteParameters routeParameters;
    private DatabaseInterface distanceChecker;

    private String address;
    boolean withStop;
    private double twoStopLength;
    private Text location = new Text();
    ArrayList<String> addressList;
    int additionalDistance;
    Parent layout;

    public static void main(String[] args) {
        Application.launch(args);
    }

    //TODO: 1. Rewrite setupRouteTask, solveForRoute, geocodeQuery, and createLT (DONE)
    // 2. Try to reorganize some of the methods so everything is easy to follow - put everything into a newly made project (DONE)
    // 3. Perhaps before the other two, finish the GUI inside of LayoutController (DONE)
    // 4. Verify that GUI is adequate before licensing, then license the app
    // 5. Export everything as a single executable which is easy to use
    // 6. DELETE these comments when done

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        distanceChecker = new DatabaseInterface();
        addressList = new ArrayList<>();
        //Makes it so that showing a stage isn't necessary, in case a custom graphics library needs to be used
        //Platform.setImplicitExit(false);
        stage.setTitle("Have to change the name");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainLayout.fxml"));
        layout = loader.load();
        Scene scene = new Scene(layout);
        stage.setScene(scene);

        LayoutController controller = loader.getController();
        controller.setRouterMain(this);

        //setupAuthentication();
        //createLocatorTaskWithParameters();
    }

    // This method authenticates the developer as a user of the program, borrowed from the ArcGIS tutorial
    private void setupAuthentication() {
        String portalURL = "this is from one's account";
        String clientId = "have to remove stuff for privacy reasons";
        String redirectURI = "by viewing the tutorials at the top, you can find out how to fill in these parameters";
        try {
            OAuthConfiguration oAuthConfiguration = new OAuthConfiguration(portalURL, clientId, redirectURI);
            AuthenticationManager.setAuthenticationChallengeHandler(new DefaultAuthenticationChallengeHandler());
            AuthenticationManager.addOAuthConfiguration(oAuthConfiguration);
            final Portal portal = new Portal(portalURL, true);
            portal.addDoneLoadingListener(() -> {
                if (portal.getLoadStatus() == LoadStatus.LOADED) {
                    String routeServiceURI = "https://route.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World";
                    setupRouteTask(routeServiceURI);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Portal: " + portal.getLoadError().getMessage()).show();
                }
            });
            portal.loadAsync();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // This method was taken from the ArcGIS tutorial
    private void createLocatorTaskWithParameters() {
        locatorTask = new LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");
        geocodeParameters = new GeocodeParameters();
        geocodeParameters.getResultAttributeNames().add("*"); // return all attributes
        geocodeParameters.setMaxResults(1); // get closest match
    }

    // Another method borrowed from the starter tutorial
    private void setupRouteTask(String routeServiceURI) {
        solveRouteTask = new RouteTask(routeServiceURI);
        solveRouteTask.loadAsync();
        solveRouteTask.addDoneLoadingListener(() -> {
            if (solveRouteTask.getLoadStatus() == LoadStatus.LOADED) {
                final ListenableFuture<RouteParameters> routeParamsFuture = solveRouteTask.createDefaultParametersAsync();
                routeParamsFuture.addDoneListener(() -> {
                    try {
                        routeParameters = routeParamsFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        new Alert(Alert.AlertType.ERROR, "Cannot create RouteTask parameters " + e.getMessage()).show();
                    }
                });
            } else {
                new Alert(Alert.AlertType.ERROR, "Unable to load RouteTask " + solveRouteTask.getLoadStatus().toString()).show();
            }
        });
    }

    // Solves from point A to point B, then adds an additional stop and determines if the route is an acceptable length
    // This method was heavily modified from the starter example
    private void solveForRoute() throws InterruptedException {
        if (startPoint != null && endPoint != null) {
            if (withStop) {
                routeParameters.setStops(Arrays.asList(new Stop(startPoint), new Stop(midPoint), new Stop(endPoint)));
            } else {
                routeParameters.setStops(Arrays.asList(new Stop(startPoint), new Stop(endPoint)));
            }
            final ListenableFuture<RouteResult> routeResultFuture = solveRouteTask.solveRouteAsync(routeParameters);
            while (!routeResultFuture.isDone()) {
                Thread.sleep(10);
                location.setText("Inside SFR");
            }
            try {
                RouteResult routeResult = routeResultFuture.get();
                if (routeResult.getRoutes().size() >= 0) {
                    Route firstRoute = routeResult.getRoutes().get(0);
                    double length = firstRoute.getTotalLength();
                    length = length / 1000;
                    length = length * .621;
                    if (!withStop) {
                        twoStopLength = length;
                    }
                    if (length - twoStopLength <= additionalDistance && withStop) {
                        System.out.println(address);
                        addressList.add(address);
                    }
                } else {
                    new Alert(Alert.AlertType.WARNING, "No routes have been found.").show();
                }
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(address + " " + e.getMessage());
            }
        }
    }

    // Modified from the starter code given by ArcGIS
    void geocodeQuery(String query) throws InterruptedException {
        ListenableFuture<List<GeocodeResult>> geocode = locatorTask.geocodeAsync(query, geocodeParameters);
        while (!geocode.isDone()) {
            Thread.sleep(10);
        }
        try {
            List<GeocodeResult> results = geocode.get();
            if (results.size() > 0) {
                GeocodeResult result = results.get(0);
                if (startPoint == null) {
                    startPoint = result.getDisplayLocation();
                    endPoint = null;
                } else if (endPoint == null) {
                    endPoint = result.getDisplayLocation();
                    solveForRoute();
                } else {
                    midPoint = result.getDisplayLocation();
                    solveForRoute();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No results found.");
                alert.show();
            }
        } catch (InterruptedException | ExecutionException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error getting result.");
            alert.show();
        }
    }

    // This method is used to check all the addresses from the database that fall between A and B
    public void getAddress() throws IOException, InterruptedException {
        for (int i = 0; i < distanceChecker.addressList.size(); i++) {
            address = distanceChecker.getAddresses(i);
            if (!withStop) {
                endPoint = null;
            }
            geocodeQuery(address);
        }
    }

    // This method displays the addresses to the screen after the acceptable ones have been found
    void displayAddresses() {
        for (int i = 0; i < this.addressList.size(); i++) {
            Text thisLocation = new Text();
            thisLocation.setText(addressList.get(i));
            thisLocation.setX(200);
            thisLocation.setY(125 + 20 * i);
            Pane newPane = (Pane) layout.getChildrenUnmodifiable().get(1);
            newPane.getChildren().add(thisLocation);
        }
    }

    @Override
    // Stops the program from running
    public void stop() {
        System.exit(0);
    }
}