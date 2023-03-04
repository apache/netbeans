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
package brickbreaker;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bat extends Parent {

    public static final int DEFAULT_SIZE = 2;

    public static final int MAX_SIZE = 7;

    private static final Image LEFT = Config.getImages().get(Config.IMAGE_BAT_LEFT);
    private static final Image CENTER = Config.getImages().get(Config.IMAGE_BAT_CENTER);
    private static final Image RIGHT = Config.getImages().get(Config.IMAGE_BAT_RIGHT);

    private int size;
    private int width;
    private int height;

    private ImageView leftImageView;
    private ImageView centerImageView;
    private ImageView rightImageView;

    public int getSize() {
        return size;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void changeSize(int newSize) {
        this.size = newSize;
        width = size * 12 + 45;
        double rightWidth = RIGHT.getWidth() - Config.SHADOW_WIDTH;
        double centerWidth = width - LEFT.getWidth() - rightWidth;
        centerImageView.setViewport(new Rectangle2D(
            (CENTER.getWidth() - centerWidth) / 2, 0, centerWidth, CENTER.getHeight()));
        rightImageView.setTranslateX(width - rightWidth);
    }

    public Bat() {
        height = (int)CENTER.getHeight() - Config.SHADOW_HEIGHT; 
        Group group = new Group();
        leftImageView = new ImageView();
        leftImageView.setImage(LEFT);
        centerImageView = new ImageView();
        centerImageView.setImage(CENTER);
        centerImageView.setTranslateX(LEFT.getWidth());
        rightImageView = new ImageView();
        rightImageView.setImage(RIGHT);
        changeSize(DEFAULT_SIZE);
        group.getChildren().addAll(leftImageView, centerImageView, rightImageView);
        getChildren().add(group);
        setMouseTransparent(true);
    }

}

