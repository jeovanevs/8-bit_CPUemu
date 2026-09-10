/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication1;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafxapplication1.CPU.CPU;

/**
 * @author Jorge
 */
public class LittleEmuApp extends Application {  
    private static final double CONTENT_WIDTH = 1700;
    private static final double CONTENT_HEIGHT = 1020;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
    Group content = new Group(root);
    StackPane viewport = new StackPane(content);
    Scene scene = new Scene(viewport, CONTENT_WIDTH, CONTENT_HEIGHT);

    viewport.widthProperty().addListener((observable, oldValue, newValue) ->
        scaleContent(content, viewport));
    viewport.heightProperty().addListener((observable, oldValue, newValue) ->
        scaleContent(content, viewport));

        stage.setScene(scene);
    stage.setMinWidth(800);
    stage.setMinHeight(500);
        stage.setTitle("LittleEmu - Emulador");
        stage.getIcons().add(new Image(LittleEmuApp.class.getResourceAsStream("microchip.png")));
        stage.setOnCloseRequest(new EventHandler(){
            @Override
            public void handle(Event event) {
                CPU.safeInterrupt();
            }
        });
        
        stage.show();
        scaleContent(content, viewport);
        
    }

    private void scaleContent(Group content, StackPane viewport) {
        double scale = Math.min(
                viewport.getWidth() / CONTENT_WIDTH,
                viewport.getHeight() / CONTENT_HEIGHT);
        content.setScaleX(scale);
        content.setScaleY(scale);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {      
        launch(args);
    }
    
}
