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
package stopwatch;

import java.io.InputStream;
import java.text.DecimalFormat;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
/**
 * This sample is an animated stopwatch. Click the green button to start the
 * stopwatch and click the red button to stop it.
 *
 * @see javafx.scene.effect.DropShadow
 * @see javafx.scene.effect.GaussianBlur
 * @see javafx.scene.effect.Light
 * @see javafx.scene.effect.Lighting
 * @see javafx.scene.image.Image
 * @see javafx.scene.image.ImageView
 * @see javafx.scene.shape.Circle
 * @see javafx.scene.Group
 * @see javafx.scene.shape.Ellipse
 * @see javafx.scene.shape.Rectangle
 * @see javafx.scene.text.Font
 * @see javafx.scene.text.Text
 * @see javafx.scene.text.TextAlignment
 * @see javafx.scene.text.TextBoundsType
 * @see javafx.scene.transform.Rotate
 * @see javafx.util.Duration
 * 
 */
public class StopWatch extends Application {

    private Watch watch;

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root, 310, 320));
        watch = new Watch();
        myLayout();
        root.getChildren().add(watch);
    }

    private void myLayout() {
        watch.setLayoutX(15);
        watch.setLayoutY(20);
    }

    private class Watch extends Parent {
        //visual nodes

        private final Dial mainDial;
        private final Dial minutesDial;
        private final Dial tenthsDial;
        private final Group background = new Group();
        private final DigitalClock digitalClock = new DigitalClock();
        private final Button startButton;
        private final Button stopButton;
        /**
         * The number of milliseconds which have elapsed while the stopwatch has
         * been running. That is, it is the total time kept on the stopwatch.
         */
        private int elapsedMillis = 0;
        /**
         * Keeps track of the amount of the clock time (CPU clock) when the
         * stopwatch run plunger was pressed, or when the last tick even occurred.
         * This is used to calculate the elapsed time delta.
         */
        private int lastClockTime = 0;
        private DecimalFormat twoPlaces = new DecimalFormat("00");
        private Timeline time = new Timeline();

        public Watch() {
            startButton = new Button(Color.web("#8cc700"), Color.web("#71a000"));
            stopButton = new Button(Color.web("#AA0000"), Color.web("#660000"));

            mainDial = new Dial(117, true, 12, 60, Color.RED, true);
            minutesDial = new Dial(30, false, 12, 60, "minutes", Color.BLACK, false);
            tenthsDial = new Dial(30, false, 12, 60, "10ths", Color.BLACK, false);

            configureBackground();
            myLayout();
            configureListeners();
            configureTimeline();
            getChildren().addAll(background, minutesDial, tenthsDial, digitalClock, mainDial, startButton, stopButton);
        }

        private void configureTimeline() {
            time.setCycleCount(Timeline.INDEFINITE);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(47), new EventHandler<ActionEvent>() {

                public void handle(ActionEvent event) {
                    calculate();
                }
            });
            time.getKeyFrames().add(keyFrame);
        }

        private void configureBackground() {
            ImageView imageView = new ImageView();
            Image image = loadImage();
            imageView.setImage(image);

            Circle circle1 = new Circle();
            circle1.setCenterX(140);
            circle1.setCenterY(140);
            circle1.setRadius(120);
            circle1.setFill(Color.TRANSPARENT);
            circle1.setStroke(Color.web("#0A0A0A"));
            circle1.setStrokeWidth(0.3);

            Circle circle2 = new Circle();
            circle2.setCenterX(140);
            circle2.setCenterY(140);
            circle2.setRadius(118);
            circle2.setFill(Color.TRANSPARENT);
            circle2.setStroke(Color.web("#0A0A0A"));
            circle2.setStrokeWidth(0.3);

            Circle circle3 = new Circle();
            circle3.setCenterX(140);
            circle3.setCenterY(140);
            circle3.setRadius(140);
            circle3.setFill(Color.TRANSPARENT);
            circle3.setStroke(Color.web("#818a89"));
            circle3.setStrokeWidth(1);

            Ellipse ellipse = new Ellipse(140, 95, 180, 95);
            Circle ellipseClip = new Circle(140, 140, 140);
            ellipse.setFill(Color.web("#535450"));
            ellipse.setStrokeWidth(0);
            GaussianBlur ellipseEffect = new GaussianBlur();
            ellipseEffect.setRadius(10);
            ellipse.setEffect(ellipseEffect);
            ellipse.setOpacity(0.1);
            ellipse.setClip(ellipseClip);
            background.getChildren().addAll(imageView, circle1, circle2, circle3, ellipse);
        }

        private void myLayout() {
            mainDial.setLayoutX(140);
            mainDial.setLayoutY(140);

            minutesDial.setLayoutX(100);
            minutesDial.setLayoutY(100);

            tenthsDial.setLayoutX(180);
            tenthsDial.setLayoutY(100);

            digitalClock.setLayoutX(79);
            digitalClock.setLayoutY(195);

            startButton.setLayoutX(223);
            startButton.setLayoutY(1);
            Rotate rotateRight = new Rotate(360 / 12);
            startButton.getTransforms().add(rotateRight);

            stopButton.setLayoutX(59.5);
            stopButton.setLayoutY(0);
            Rotate rotateLeft = new Rotate(-360 / 12);
            stopButton.getTransforms().add(rotateLeft);
        }

        private void configureListeners() {
            startButton.setOnMousePressed(new EventHandler<MouseEvent>() {

                public void handle(MouseEvent me) {
                    startButton.moveDown();
                    me.consume();
                }
            });

            stopButton.setOnMousePressed(new EventHandler<MouseEvent>() {

                public void handle(MouseEvent me) {
                    stopButton.moveDown();
                    me.consume();
                }
            });

            startButton.setOnMouseReleased(new EventHandler<MouseEvent>() {

                public void handle(MouseEvent me) {
                    startButton.moveUp();
                    startStop();
                    me.consume();
                }
            });

            stopButton.setOnMouseReleased(new EventHandler<MouseEvent>() {

                public void handle(MouseEvent me) {
                    stopButton.moveUp();
                    stopReset();
                    me.consume();
                }
            });

            startButton.setOnMouseDragged(new EventHandler<MouseEvent>() {

                public void handle(MouseEvent me) {
                    me.consume();
                }
            });

            stopButton.setOnMouseDragged(new EventHandler<MouseEvent>() {

                public void handle(MouseEvent me) {
                    me.consume();
                }
            });
        }

        //MODEL
        private void calculate() {
            if (lastClockTime == 0) {
                lastClockTime = (int) System.currentTimeMillis();
            }

            int now = (int) System.currentTimeMillis();
            int delta = now - lastClockTime;

            elapsedMillis += delta;

            int tenths = (elapsedMillis / 10) % 100;
            int seconds = (elapsedMillis / 1000) % 60;
            int mins = (elapsedMillis / 60000) % 60;

            refreshTimeDisplay(mins, seconds, tenths);

            lastClockTime = now;
        }

        public void startStop() {
            if (time.getStatus() != Status.STOPPED) {
                // if started, stop it
                time.stop();
                lastClockTime = 0;
            } else {
                // if stopped, restart
                time.play();
            }
        }

        public void stopReset() {
            if (time.getStatus() != Status.STOPPED) {
                // if started, stop it
                time.stop();
                lastClockTime = 0;
            } else {
                // if stopped, reset it
                lastClockTime = 0;
                elapsedMillis = 0;
                refreshTimeDisplay(0, 0, 0);
            }
        }

        private void refreshTimeDisplay(int mins, int seconds, int tenths) {
            double handAngle = ((360 / 60) * seconds);
            mainDial.setAngle(handAngle);

            double tenthsHandAngle = ((360 / 100.0) * tenths);
            tenthsDial.setAngle(tenthsHandAngle);

            double minutesHandAngle = ((360 / 60.0) * mins);
            minutesDial.setAngle(minutesHandAngle);

            String timeString = twoPlaces.format(mins) + ":" + twoPlaces.format(seconds) + "." + twoPlaces.format(tenths);
            digitalClock.refreshDigits(timeString);
        }

        //IMAGE handling
        public Image loadImage() {
            InputStream is = Watch.class.getResourceAsStream("stopwatch.png");
            return new Image(is);
        }
    }

    private class Dial extends Parent {
        private final double radius;
        private final Color color;
        private final Color FILL_COLOR = Color.web("#0A0A0A");
        private final Font NUMBER_FONT = new Font(16);
        private final Text name = new Text();
        private final Group hand = new Group();
        private final Group handEffectGroup = new Group(hand);
        private final DropShadow handEffect = new DropShadow();
        private int numOfMarks;
        private int numOfMinorMarks;

        public Dial(double radius, boolean hasNumbers, int numOfMarks, int numOfMinorMarks, Color color, boolean hasEffect) {
            this.color = color;
            this.radius = radius;
            this.numOfMarks = numOfMarks;
            this.numOfMinorMarks = numOfMinorMarks;

            configureHand();
            if (hasEffect) {
                configureEffect();
            }
            if (hasNumbers) {
                getChildren().add(createNumbers());
            }
            getChildren().addAll(
                    createTickMarks(),
                    handEffectGroup);
        }

        public Dial(double radius, boolean hasNumbers, int numOfMarks, int numOfMinorMarks, String name, Color color, boolean hasEffect) {
            this(radius, hasNumbers, numOfMarks, numOfMinorMarks, color, hasEffect);
            configureName(name);
            getChildren().add(this.name);
        }

        private Group createTickMarks() {
            Group group = new Group();

            for (int i = 0; i < numOfMarks; i++) {
                double angle = (360 / numOfMarks) * (i);
                group.getChildren().add(createTic(angle, radius / 10, 1.5));
            }

            for (int i = 0; i < numOfMinorMarks; i++) {
                double angle = (360 / numOfMinorMarks) * i;
                group.getChildren().add(createTic(angle, radius / 20, 1));
            }
            return group;
        }

        private Rectangle createTic(double angle, double width, double height) {
            Rectangle rectangle = new Rectangle(-width / 2, -height / 2, width, height);
            rectangle.setFill(Color.rgb(10, 10, 10));
            rectangle.setRotate(angle);
            rectangle.setLayoutX(radius * Math.cos(Math.toRadians(angle)));
            rectangle.setLayoutY(radius * Math.sin(Math.toRadians(angle)));
            return rectangle;
        }

        private void configureName(String string) {
            Font font = new Font(9);
            name.setText(string);
            name.setBoundsType(TextBoundsType.VISUAL);
            name.setLayoutX(-name.getBoundsInLocal().getWidth() / 2 + 4.8);
            name.setLayoutY(radius * 1 / 2 + 4);
            name.setFill(FILL_COLOR);
            name.setFont(font);
        }

        private Group createNumbers() {
            return new Group(
                    createNumber("30", -9.5, radius - 16 + 4.5),
                    createNumber("0", -4.7, -radius + 22),
                    createNumber("45", -radius + 10, 5),
                    createNumber("15", radius - 30, 5));
        }

        private Text createNumber(String number, double layoutX, double layoutY) {
            Text text = new Text(number);
            text.setLayoutX(layoutX);
            text.setLayoutY(layoutY);
            text.setTextAlignment(TextAlignment.CENTER);
            text.setFill(FILL_COLOR);
            text.setFont(NUMBER_FONT);
            return text;
        }

        public void setAngle(double angle) {
            Rotate rotate = new Rotate(angle);
            hand.getTransforms().clear();
            hand.getTransforms().add(rotate);
        }

        private void configureHand() {
            Circle circle = new Circle(0, 0, radius / 18);
            circle.setFill(color);
            Rectangle rectangle1 = new Rectangle(-0.5 - radius / 140, +radius / 7 - radius / 1.08, radius / 70 + 1, radius / 1.08);
            Rectangle rectangle2 = new Rectangle(-0.5 - radius / 140, +radius / 3.5 - radius / 7, radius / 70 + 1, radius / 7);
            rectangle1.setFill(color);
            rectangle2.setFill(Color.BLACK);
            hand.getChildren().addAll(circle, rectangle1, rectangle2);
        }

        private void configureEffect() {
            handEffect.setOffsetX(radius / 40);
            handEffect.setOffsetY(radius / 40);
            handEffect.setRadius(6);
            handEffect.setColor(Color.web("#000000"));

            Lighting lighting = new Lighting();
            Light.Distant light = new Light.Distant();
            light.setAzimuth(225);
            lighting.setLight(light);
            handEffect.setInput(lighting);

            handEffectGroup.setEffect(handEffect);
        }
    }

    private class DigitalClock extends Parent {

        private final HBox hBox = new HBox();
        public final Font FONT = new Font(16);
        private Text[] digits = new Text[8];
        private Group[] digitsGroup = new Group[8];
        private int[] numbers = {0, 1, 3, 4, 6, 7};

        DigitalClock() {
            configureDigits();
            configureDots();
            configureHbox();
            getChildren().addAll(hBox);
        }

        private void configureDigits() {
            for (int i : numbers) {
                digits[i] = new Text("0");
                digits[i].setFont(FONT);
                digits[i].setTextOrigin(VPos.TOP);
                digits[i].setLayoutX(2.3);
                digits[i].setLayoutY(-1);
                Rectangle background;
                if (i < 6) {
                    background = createBackground(Color.web("#a39f91"), Color.web("#FFFFFF"));
                    digits[i].setFill(Color.web("#000000"));
                } else {
                    background = createBackground(Color.web("#bdbeb3"), Color.web("#FF0000"));
                    digits[i].setFill(Color.web("#FFFFFF"));
                }
                digitsGroup[i] = new Group(background, digits[i]);
            }
        }

        private void configureDots() {
            digits[2] = createDot(":");
            digitsGroup[2] = new Group(createDotBackground(), digits[2]);
            digits[5] = createDot(".");
            digitsGroup[5] = new Group(createDotBackground(), digits[5]);
        }

        private Rectangle createDotBackground() {
            Rectangle background = new Rectangle(8, 17, Color.TRANSPARENT);
            background.setStroke(Color.TRANSPARENT);
            background.setStrokeWidth(2);
            return background;
        }

        private Text createDot(String string) {
            Text text = new Text(string);
            text.setFill(Color.web("#000000"));
            text.setFont(FONT);
            text.setTextOrigin(VPos.TOP);
            text.setLayoutX(1);
            text.setLayoutY(-4);
            return text;
        }

        private Rectangle createBackground(Color stroke, Color fill) {
            Rectangle background = new Rectangle(14, 17, fill);
            background.setStroke(stroke);
            background.setStrokeWidth(2);
            background.setEffect(new Lighting());
            background.setCache(true);
            return background;
        }

        private void configureHbox() {
            hBox.getChildren().addAll(digitsGroup);
            hBox.setSpacing(1);
        }

        public void refreshDigits(String time) { //expecting time in format "xx:xx:xx"
            for (int i = 0; i < digits.length; i++) {
                digits[i].setText(time.substring(i, i + 1));
            }
        }
    }

    private class Button extends Parent {
        private final Color colorWeak;
        private final Color colorStrong;
        private final Rectangle rectangleSmall = new Rectangle(14, 7);
        private final Rectangle rectangleBig = new Rectangle(28, 5);
        private final Rectangle rectangleWatch = new Rectangle(24, 14);
        private final Rectangle rectangleVisual = new Rectangle(28, 7 + 5 + 14);

        Button(Color colorWeak, Color colorStrong) {
            this.colorStrong = colorStrong;
            this.colorWeak = colorWeak;
            configureDesign();
            setCursor(Cursor.HAND);
            getChildren().addAll(rectangleVisual, rectangleSmall, rectangleBig, rectangleWatch);
        }

        private void configureDesign() {
            rectangleVisual.setLayoutY(0f);
            rectangleVisual.setLayoutX(-14);
            rectangleVisual.setFill(Color.TRANSPARENT);

            rectangleSmall.setLayoutX(-7);
            rectangleSmall.setLayoutY(5);
            rectangleSmall.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop[]{
                        new Stop(0, colorWeak),
                        new Stop(0.5, colorStrong),
                        new Stop(1, colorWeak)}));

            rectangleBig.setLayoutX(-14);
            rectangleBig.setLayoutY(0);
            rectangleBig.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop[]{
                        new Stop(0, colorStrong),
                        new Stop(0.5, colorWeak),
                        new Stop(1, colorStrong)}));

            rectangleWatch.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop[]{
                        new Stop(0, Color.web("#4e605f")),
                        new Stop(0.2, Color.web("#c3d6d5")),
                        new Stop(0.5, Color.web("#f9ffff")),
                        new Stop(0.8, Color.web("#c3d6d5")),
                        new Stop(1, Color.web("#4e605f"))}));
            rectangleWatch.setLayoutX(-12);
            rectangleWatch.setLayoutY(12);
        }

        private void move(double smallRectHeight) {
            rectangleSmall.setHeight(smallRectHeight);
            rectangleSmall.setTranslateY(7 - smallRectHeight);
            rectangleBig.setTranslateY(7 - smallRectHeight);
        }

        public void moveDown() {
            move(0);
        }

        public void moveUp() {
            move(7);
        }
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
