/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package xylophone;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * A sample that demonstrates the basics of AudioClips.
 *
 * @see javafx.scene.media.AudioClip
 * @resource Note1.wav
 * @resource Note2.wav
 * @resource Note3.wav
 * @resource Note4.wav
 * @resource Note5.wav
 * @resource Note6.wav
 * @resource Note7.wav
 * @resource Note8.wav
 */
public final class Xylophone extends Application {
    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 480, 350));

        final AudioClip bar1Note = 
            new AudioClip(Xylophone.class.getResource("Note1.wav").toString());
        final AudioClip bar2Note = 
            new AudioClip(Xylophone.class.getResource("Note2.wav").toString());
        final AudioClip bar3Note = 
            new AudioClip(Xylophone.class.getResource("Note3.wav").toString());
        final AudioClip bar4Note = 
            new AudioClip(Xylophone.class.getResource("Note4.wav").toString());
        final AudioClip bar5Note = 
            new AudioClip(Xylophone.class.getResource("Note5.wav").toString());
        final AudioClip bar6Note = 
            new AudioClip(Xylophone.class.getResource("Note6.wav").toString());
        final AudioClip bar7Note = 
            new AudioClip(Xylophone.class.getResource("Note7.wav").toString());
        final AudioClip bar8Note = 
            new AudioClip(Xylophone.class.getResource("Note8.wav").toString());

        Group rectangleGroup = new Group();

        double xStart = 50.0;
        double xOffset = 30.0;
        double yPos = 180.0;
        double barWidth = 22.0;
        double barDepth = 7.0;

        Group base1Group = createRectangle(new Color(0.2, 0.12, 0.1, 1.0), 
                                           xStart + 135, yPos + 20.0, barWidth*11.5, 10.0);
        Group base2Group = createRectangle(new Color(0.2, 0.12, 0.1, 1.0), 
                                           xStart + 135, yPos - 20.0, barWidth*11.5, 10.0);
        Group bar1Group = createRectangle(Color.PURPLE,
                                          xStart + 1*xOffset, yPos, barWidth, 100.0);
        Group bar2Group = createRectangle(Color.BLUEVIOLET,
                                          xStart + 2*xOffset, yPos, barWidth, 95.0);
        Group bar3Group = createRectangle(Color.BLUE,
                                          xStart + 3*xOffset, yPos, barWidth, 90.0);
        Group bar4Group = createRectangle(Color.GREEN,
                                          xStart + 4*xOffset, yPos, barWidth, 85.0);
        Group bar5Group = createRectangle(Color.GREENYELLOW,
                                          xStart + 5*xOffset, yPos, barWidth, 80.0);
        Group bar6Group = createRectangle(Color.YELLOW,
                                          xStart + 6*xOffset, yPos, barWidth, 75.0);
        Group bar7Group = createRectangle(Color.ORANGE,
                                          xStart + 7*xOffset, yPos, barWidth, 70.0);
        Group bar8Group = createRectangle(Color.RED,
                                          xStart + 8*xOffset, yPos, barWidth, 65.0);

        bar1Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar1Note.play(); }
        });
        bar2Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar2Note.play(); }
        });
        bar3Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar3Note.play(); }
        });
        bar4Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar4Note.play(); }
        });
        bar5Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar5Note.play(); }
        });
        bar6Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar6Note.play(); }
        });
        bar7Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar7Note.play(); }
        });
        bar8Group.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) { bar8Note.play(); }
        });

        Light.Point light = new Light.Point();
        light.setX(-20);
        light.setY(-20);
        light.setZ(100);

        Lighting l = new Lighting();
        l.setLight(light);
        l.setSurfaceScale(1.0f);

        bar1Group.setEffect(l);
        bar2Group.setEffect(l);
        bar3Group.setEffect(l);
        bar4Group.setEffect(l);
        bar5Group.setEffect(l);
        bar6Group.setEffect(l);
        bar7Group.setEffect(l);
        bar8Group.setEffect(l);

        rectangleGroup.getChildren().add(base1Group);
        rectangleGroup.getChildren().add(base2Group);
        rectangleGroup.getChildren().add(bar1Group);
        rectangleGroup.getChildren().add(bar2Group);
        rectangleGroup.getChildren().add(bar3Group);
        rectangleGroup.getChildren().add(bar4Group);
        rectangleGroup.getChildren().add(bar5Group);
        rectangleGroup.getChildren().add(bar6Group);
        rectangleGroup.getChildren().add(bar7Group);
        rectangleGroup.getChildren().add(bar8Group);
        rectangleGroup.setScaleX(1.8);
        rectangleGroup.setScaleY(1.8);
        rectangleGroup.setTranslateX(55.0);

        Pane pane = new Pane(); 
        pane.getChildren().add(rectangleGroup);

        root.getChildren().add(pane);

    }

    public static Group createRectangle(Color color, double tx, double ty, double sx, double sy) {
        Group squareGroup = new Group();
        Rectangle squareShape = new Rectangle(1.0, 1.0);
        squareShape.setFill(color);
        squareShape.setTranslateX(-0.5);
        squareShape.setTranslateY(-0.5);
        squareGroup.getChildren().add(squareShape);
        squareGroup.setTranslateX(tx);
        squareGroup.setTranslateY(ty);
        squareGroup.setScaleX(sx);
        squareGroup.setScaleY(sy);
        return squareGroup;
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
