/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package changelistener;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * A sample that demonstrates how to add or remove a change listener on a node
 * (for example, a Rectangle node) for some property (for example,
 * Rectangle.hover). Once you add a listener, the text field  shows the hover
 * property change.
 *
 * @see javafx.beans.value.ChangeListener
 * @see javafx.beans.InvalidationListener
 * @see javafx.beans.value.ObservableValue
 */
public class ChangeListener extends Application {

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 300,100));

        HBox outerHbox = new HBox();
        VBox vbox = new VBox(10);
        vbox.setPrefWidth(200);
        // create rectangle
        final Rectangle rect = new Rectangle(150,0,60,60);
        rect.setFill(Color.DODGERBLUE);
        rect.setEffect(new Lighting());
        //create text field for showing actual message
        final Text text = new Text(0,0, "Add a hover listener");
        text.setStyle("-fx-font-size: 22;");
        text.setTextOrigin(VPos.TOP);
        text.setTextAlignment(TextAlignment.CENTER);
        // create listener
        final InvalidationListener hoverListener = new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (rect.isHover()) {
                    text.setText("hovered");
                } else {
                    text.setText("not hovered");
                }
            }
        };
        //create button for adding listener
        Button buttonAdd = new Button("Add listener");
        buttonAdd.setPrefSize(140, 18);
        buttonAdd.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //add the listener on property hover
                rect.hoverProperty().addListener(hoverListener);
                text.setText("listener added");
            }
        });
        //create a button for removing the listener
        Button buttonRemove = new Button("Remove listener");
        buttonRemove.setPrefSize(140, 18);
        buttonRemove.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //remove the listener
                rect.hoverProperty().removeListener(hoverListener);
                text.setText("listener removed");
            }
        });
        // show all nodes
        vbox.getChildren().addAll(text, buttonAdd, buttonRemove);
        outerHbox.getChildren().addAll(vbox, rect);
        root.getChildren().add(outerHbox);
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX 
     * application. main() serves only as fallback in case the 
     * application can not be launched through deployment artifacts,
     * e.g., in IDEs with limited FX support. NetBeans ignores main().
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
