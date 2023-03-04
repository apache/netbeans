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
package paginationdemo;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * A sample that demonstrates pagination
 *
 * @see javafx.scene.control.Pagination 
 * @resource animal1.jpg
 * @resource animal2.jpg
 * @resource animal3.jpg
 * @resource animal4.jpg
 * @resource animal5.jpg
 * @resource animal6.jpg
 * @resource animal7.jpg
 * @resource animal8.jpg
 */
public class PaginationDemo extends Application {

    private Pagination pagination;
    private Image[] images = new Image[7];

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        VBox outerBox = new VBox();
        outerBox.setAlignment(Pos.CENTER);
        //Images for our pages
        for (int i = 0; i < 7; i++) {
            images[i] = new Image(PaginationDemo.class.getResource("animal" + (i + 1) + ".jpg").toExternalForm(), false);
        }

        pagination = PaginationBuilder.create().pageCount(7).pageFactory(new Callback<Integer, Node>() {           
            @Override public Node call(Integer pageIndex) {
                return createAnimalPage(pageIndex);
            }
        }).build();
        //Style can be numeric page indicators or bullet indicators
        Button styleButton = ButtonBuilder.create().text("Toggle pagination style").onAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent me) {
                if (!pagination.getStyleClass().contains(Pagination.STYLE_CLASS_BULLET)) {
                    pagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
                } else {
                    pagination.getStyleClass().remove(Pagination.STYLE_CLASS_BULLET);
                }
            }
        }).build();

        outerBox.getChildren().addAll(pagination, styleButton);
        root.getChildren().add(outerBox);
    }
    //Creates the page content
    private VBox createAnimalPage(int pageIndex) {
        VBox box = new VBox();
        ImageView iv = new ImageView(images[pageIndex]);
        box.setAlignment(Pos.CENTER);
        Label desc = new Label("PAGE " + (pageIndex + 1));
        box.getChildren().addAll(iv, desc);
        return box;
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
