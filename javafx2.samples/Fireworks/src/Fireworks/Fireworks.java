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
package Fireworks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Reflection;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * A sample that demonstrates how to draw and paint shapes, apply visual
 * effects, blend colors in overlapping objects, and animate objects.
 *
 * @see javafx.scene.canvas.Canvas
 * @see javafx.scene.canvas.GraphicsContext
 * @see javafx.scene.effect.BlendMode
 * @see javafx.scene.effect.BoxBlur
 * @see javafx.scene.shape.Circle
 * @see javafx.scene.Group
 * @see javafx.scene.paint.LinearGradient
 * @see javafx.animation.Timeline
 */
public class Fireworks extends Application {
    private final SanFranciscoFireworks sanFranciscoFireworks = new SanFranciscoFireworks();
    
    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        root.getChildren().add(sanFranciscoFireworks);
    }

    @Override public void stop() {
        sanFranciscoFireworks.stop();
    }

    public void play() {
        sanFranciscoFireworks.start();
    }
    
    public static class SanFranciscoFireworks extends Pane {
        private final AnimationTimer timer;
        private final Canvas canvas;
        private final ImageView background;
        private final List<Particle> particles = new ArrayList<Particle>();
        private final Paint[] colors;
        private int countDownTillNextFirework = 40;
        
        public SanFranciscoFireworks() {
            // create a color palette of 180 colors
            colors = new Paint[181];
            colors[0] = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, 
                        new Stop(0, Color.WHITE),
                        new Stop(0.2, Color.hsb(59, 0.38, 1)),
                        new Stop(0.6, Color.hsb(59, 0.38, 1,0.1)),
                        new Stop(1, Color.hsb(59, 0.38, 1,0))
                        );
            for (int h=0;h<360;h+=2) {
                colors[1+(h/2)] = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, 
                        new Stop(0, Color.WHITE),
                        new Stop(0.2, Color.hsb(h, 1, 1)),
                        new Stop(0.6, Color.hsb(h, 1, 1,0.1)),
                        new Stop(1, Color.hsb(h, 1, 1,0))
                        );
            }
            // create canvas
            canvas = new Canvas(1024, 500);

            canvas.setBlendMode(BlendMode.ADD);
            canvas.setEffect(new Reflection(0,0.4,0.15,0));
            background = new ImageView(getClass().getResource("sf.jpg").toExternalForm());
            getChildren().addAll(background,canvas);
            // create animation timer that will be called every frame
            // final AnimationTimer timer = new AnimationTimer() {
            timer = new AnimationTimer() {

                @Override public void handle(long now) {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    // clear area with transparent black
                    gc.setFill(Color.rgb(0, 0, 0, 0.2));
                    gc.fillRect(0, 0, 1024, 708);
                    // draw fireworks
                    drawFireworks(gc);
                    // countdown to launching the next firework
                    if (countDownTillNextFirework == 0) {
                        countDownTillNextFirework = 10 + (int)(Math.random()*30);
                        fireParticle();
                    }
                    countDownTillNextFirework --;
                }
            };
        }
        
        public void start() { timer.start(); }
        public void stop() { timer.stop(); }

        /**
         * Make resizable and keep background image proportions and center.
         */
        @Override protected void layoutChildren() {
            // final double w = 480.0;
            // final double h = 360.0;
            final double w = getWidth();
            final double h = getHeight();
            final double scale = Math.min(w/1024d, h/708d);
            final int width = (int)(1024*scale);
            final int height = (int)(708*scale);
            final int x = (int)((w-width)/2);
            final int y = (int)((h-height)/2);
            background.relocate(x, y);
            background.setFitWidth(width);
            background.setFitHeight(height);
            canvas.relocate(x, y);
            canvas.setWidth(width);
            canvas.setHeight(height * 0.706);
        }
    
        private void drawFireworks(GraphicsContext gc) {
            Iterator<Particle> iter = particles.iterator();
            List<Particle> newParticles = new ArrayList<Particle>();
            while(iter.hasNext()) {
                Particle firework = iter.next();
                // if the update returns true then particle has expired
                if(firework.update()) {
                    // remove particle from those drawn
                    iter.remove();
                    // check if it should be exploded
                    if(firework.shouldExplodeChildren) {
                        if(firework.size == 9) {
                            explodeCircle(firework, newParticles);
                        } else if(firework.size == 8) {
                            explodeSmallCircle(firework, newParticles);
                        }
                    }
                }
                firework.draw(gc);
            }
            particles.addAll(newParticles);
        }
        
        private void fireParticle() {
            particles.add(new Particle(
                canvas.getWidth()*0.5, canvas.getHeight()+10,
                Math.random() * 5 - 2.5, 0, 
                0, 150 + Math.random() * 100,
                colors[0], 9,
                false, true, true));
        }


        private void explodeCircle(Particle firework, List<Particle> newParticles) {
            final int count = 20 + (int)(60*Math.random());
            final boolean shouldExplodeChildren = Math.random() > 0.5;
            final double angle = (Math.PI * 2) / count;
            final int color = (int)(Math.random()*colors.length);
            for(int i=count; i>0; i--) {
                double randomVelocity = 4 + Math.random() * 4;
                double particleAngle = i * angle;
                newParticles.add(
                    new Particle(
                        firework.posX, firework.posY, 
                        Math.cos(particleAngle) * randomVelocity, Math.sin(particleAngle) * randomVelocity,
                        0, 0, 
                        colors[color], 
                        8,
                        true, shouldExplodeChildren, true));
            }
        }

        private void explodeSmallCircle(Particle firework, List<Particle> newParticles) {
            final double angle = (Math.PI * 2) / 12;
            for(int count=12; count>0; count--) {
                double randomVelocity = 2 + Math.random() * 2;
                double particleAngle = count * angle;
                newParticles.add(
                    new Particle(
                        firework.posX, firework.posY, 
                        Math.cos(particleAngle) * randomVelocity, Math.sin(particleAngle) * randomVelocity,
                        0, 0, 
                        firework.color, 
                        4,
                        true, false, false));
            }
        }
    }


    /**
     * A Simple Particle that draws its self as a circle.
     */
    public static class Particle {
        private static final double GRAVITY = 0.06;
        // properties for animation
        // and colouring
        double alpha;
        final double easing;
        double fade;
        double posX;
        double posY;
        double velX;
        double velY;
        final double targetX;
        final double targetY;
        final Paint color;
        final int size;
        final boolean usePhysics;
        final boolean shouldExplodeChildren;
        final boolean hasTail;
        double lastPosX;
        double lastPosY;
        
        public Particle(double posX, double posY, double velX, double velY, double targetX, double targetY, 
                Paint color,int size, boolean usePhysics, boolean shouldExplodeChildren, boolean hasTail) {
            this.posX = posX;
            this.posY = posY;
            this.velX = velX;
            this.velY = velY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.color = color;
            this.size = size;
            this.usePhysics = usePhysics;
            this.shouldExplodeChildren = shouldExplodeChildren;
            this.hasTail = hasTail;
            this.alpha    = 1;
            this.easing   = Math.random() * 0.02;
            this.fade     = Math.random() * 0.1;
        }

        public boolean update() {
            lastPosX = posX;
            lastPosY = posY;
            if(this.usePhysics) { // on way down
                velY += GRAVITY;
                posY += velY;
                this.alpha -= this.fade; // fade out particle
            } else { // on way up
                double distance = (targetY - posY);
                // ease the position
                posY += distance * (0.03 + easing);
                // cap to 1
                alpha = Math.min(distance * distance * 0.00005, 1);
            }
            posX += velX;
            return alpha < 0.005;
        }

        public void draw(GraphicsContext context) {
            final double x = Math.round(posX);
            final double y = Math.round(posY);
            final double xVel = (x - lastPosX) * -5;
            final double yVel = (y - lastPosY) * -5;
            // set the opacity for all drawing of this particle
            context.setGlobalAlpha(Math.random() * this.alpha);
            // draw particle
            context.setFill(color);
            context.fillOval(x-size, y-size, size+size, size+size);
            // draw the arrow triangle from where we were to where we are now
            if (hasTail) {
                context.setFill(Color.rgb(255,255,255,0.3));
                context.fillPolygon(new double[]{posX + 1.5,posX + xVel,posX - 1.5}, 
                        new double[]{posY,posY + yVel,posY}, 3);
            }
        }
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
