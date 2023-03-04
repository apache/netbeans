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
package timelineinterpolator;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A sample that shows various types of  interpolation  between key frames in a
 * timeline. There are five circles, each animated with a different
 * interpolation method.  The Linear interpolator is the default. Use the
 * controls to reduce opacity to zero for some circles to compare with others,
 * or change circle color to distinguish between individual interpolators.
 *
 * @see javafx.animation.Interpolator
 * @see javafx.animation.KeyFrame
 * @see javafx.animation.KeyValue
 * @see javafx.animation.Timeline
 * @see javafx.util.Duration
 */
public class TimelineInterpolator extends Application {
    private final Timeline timeline = new Timeline();
    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 250, 90));

        //create circles by method createMovingCircle listed below
        Circle circle1 = createMovingCircle(Interpolator.LINEAR); //default interpolator
        circle1.setOpacity(0.7);
        Circle circle2 = createMovingCircle(Interpolator.EASE_BOTH); //circle slows down when reached both ends of trajectory
        circle2.setOpacity(0.45);
        Circle circle3 = createMovingCircle(Interpolator.EASE_IN);
        Circle circle4 = createMovingCircle(Interpolator.EASE_OUT);
        Circle circle5 = createMovingCircle(Interpolator.SPLINE(0.5, 0.1, 0.1, 0.5)); //one can define own behaviour of interpolator by spline method
        
        root.getChildren().addAll(
                circle1,
                circle2,
                circle3,
                circle4,
                circle5
        );
    }

    private Circle createMovingCircle(Interpolator interpolator) {
        //create a transparent circle
        Circle circle = new Circle(45,45, 40,  Color.web("1c89f4"));
        circle.setOpacity(0);
        //add effect
        circle.setEffect(new Lighting());

        //create a timeline for moving the circle
       
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);

        //create a keyValue for horizontal translation of circle to the position 155px with given interpolator
        KeyValue keyValue = new KeyValue(circle.translateXProperty(), 155, interpolator);

        //create a keyFrame with duration 4s
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(4), keyValue);

        //add the keyframe to the timeline
        timeline.getKeyFrames().add(keyFrame);

        return circle;
    }

   public void play() {
        timeline.play();      
    }
 
    @Override public void stop() {
        timeline.stop();
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
        play();
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
