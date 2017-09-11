/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
