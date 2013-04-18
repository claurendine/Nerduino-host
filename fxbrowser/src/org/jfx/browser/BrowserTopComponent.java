/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jfx.browser;

import java.awt.BorderLayout;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.jfx.browser//Browser//EN",
autostore = false)
@TopComponent.Description(preferredID = "BrowserTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.jfx.browser.BrowserTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "Browser",
preferredID = "BrowserTopComponent")
public final class BrowserTopComponent extends TopComponent {

    private Pane browser;
    private static final int PANEL_WIDTH_INT = 675;
    private static final int PANEL_HEIGHT_INT = 400;

    public BrowserTopComponent() {

        initComponents();
        setName("Browser");
        setLayout(new BorderLayout());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initAndShowGUI();
            }
        });

    }

    private void initAndShowGUI() {
        final JFXPanel fxPanel = new JFXPanel();
		
		add(fxPanel, BorderLayout.CENTER);
        
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                browser = createBrowser();
                fxPanel.setScene(new Scene(browser));
            }
        });
    }

    private Pane createBrowser() {
        Double widthDouble = new Integer(PANEL_WIDTH_INT).doubleValue();
        Double heightDouble = new Integer(PANEL_HEIGHT_INT).doubleValue();
        WebView view = new WebView();
        view.setMinSize(widthDouble, heightDouble);
        view.setPrefSize(widthDouble, heightDouble);
        final WebEngine eng = view.getEngine();
        final Label warningLabel = new Label("Do you need to specify web proxy information?");
        eng.load("http://www.oracle.com/us/index.html");
        ChangeListener handler = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (warningLabel.isVisible()) {
                    warningLabel.setVisible(false);
                }
            }
        };
        eng.getLoadWorker().progressProperty().addListener(handler);
        final TextField locationField = new TextField("http://www.oracle.com/us/index.html");
        locationField.setMaxHeight(Double.MAX_VALUE);
        Button goButton = new Button("Go");
        goButton.setDefaultButton(true);
        EventHandler<ActionEvent> goAction = new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                eng.load(locationField.getText().startsWith("http://") ? locationField.getText()
                        : "http://" + locationField.getText());
            }
        };
        goButton.setOnAction(goAction);
        locationField.setOnAction(goAction);
        eng.locationProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                locationField.setText(newValue);
            }
        });
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5));
        grid.setVgap(5);
        grid.setHgap(5);
        GridPane.setConstraints(locationField, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
        GridPane.setConstraints(goButton, 1, 0);
        GridPane.setConstraints(view, 0, 1, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
        GridPane.setConstraints(warningLabel, 0, 2, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(widthDouble - 200, widthDouble - 200, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true),
                new ColumnConstraints(40, 40, 40, Priority.NEVER, HPos.CENTER, true));
        grid.getChildren().addAll(locationField, goButton, warningLabel, view);
        return grid;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
