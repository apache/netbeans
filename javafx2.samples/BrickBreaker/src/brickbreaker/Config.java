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
package brickbreaker;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.util.Duration;

public final class Config {

    public static final Duration ANIMATION_TIME = Duration.millis(40);
    public static final int MAX_LIVES = 9;
    // Screen info
    public static final int FIELD_BRICK_IN_ROW = 15;

    public static final String IMAGE_DIR = "images/desktop/";

    public static final int WINDOW_BORDER = 3; // on desktop platform
    public static final int TITLE_BAR_HEIGHT = 19; // on desktop platform
    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 720;

    public static final int INFO_TEXT_SPACE = 10;

    // Game field info
    public static final int BRICK_WIDTH = 48;
    public static final int BRICK_HEIGHT = 24;
    public static final int SHADOW_WIDTH = 10;
    public static final int SHADOW_HEIGHT = 16;

    public static final double BALL_MIN_SPEED = 6;
    public static final double BALL_MAX_SPEED = BRICK_HEIGHT;
    public static final double BALL_MIN_COORD_SPEED = 2;
    public static final double BALL_SPEED_INC = 0.5f;

    public static final int BAT_Y = SCREEN_HEIGHT - 40;
    public static final int BAT_SPEED = 8;

    public static final int BONUS_SPEED = 3;

    public static final int FIELD_WIDTH = FIELD_BRICK_IN_ROW * BRICK_WIDTH;
    public static final int FIELD_HEIGHT = FIELD_WIDTH;
    public static final int FIELD_Y = SCREEN_HEIGHT - FIELD_HEIGHT;

    private static final String[] BRICKS_IMAGES = new String[] {
        "blue.png",
        "broken1.png",
        "broken2.png",
        "brown.png",
        "cyan.png",
        "green.png",
        "grey.png",
        "magenta.png",
        "orange.png",
        "red.png",
        "violet.png",
        "white.png",
        "yellow.png",
    };

    private static ObservableList<Image> bricksImages = javafx.collections.FXCollections.<Image>observableArrayList();

    public static ObservableList<Image> getBricksImages() {
        return bricksImages;
    }

    private static final String[] BONUSES_IMAGES = new String[] {
        "ballslow.png",
        "ballfast.png",
        "catch.png",
        "batgrow.png",
        "batreduce.png",
        "ballgrow.png",
        "ballreduce.png",
        "strike.png",
        "extralife.png",
    };

    private static ObservableList<Image> bonusesImages = javafx.collections.FXCollections.<Image>observableArrayList();

    public static ObservableList<Image> getBonusesImages() {
        return bonusesImages;
    }

    public static final int IMAGE_BACKGROUND = 0;
    public static final int IMAGE_BAT_LEFT = 1;
    public static final int IMAGE_BAT_CENTER = 2;
    public static final int IMAGE_BAT_RIGHT = 3;
    public static final int IMAGE_BALL_0 = 4;
    public static final int IMAGE_BALL_1 = 5;
    public static final int IMAGE_BALL_2 = 6;
    public static final int IMAGE_BALL_3 = 7;
    public static final int IMAGE_BALL_4 = 8;
    public static final int IMAGE_BALL_5 = 9;
    public static final int IMAGE_LOGO = 10;
    public static final int IMAGE_SPLASH_BRICK = 11;
    public static final int IMAGE_SPLASH_BRICKSHADOW = 12;
    public static final int IMAGE_SPLASH_BREAKER = 13;
    public static final int IMAGE_SPLASH_BREAKERSHADOW = 14;
    public static final int IMAGE_SPLASH_PRESSANYKEY = 15;
    public static final int IMAGE_SPLASH_PRESSANYKEYSHADOW = 16;
    public static final int IMAGE_SPLASH_STRIKE = 17;
    public static final int IMAGE_SPLASH_STRIKESHADOW = 18;
    public static final int IMAGE_SPLASH_SUN = 19;
    public static final int IMAGE_READY = 20;
    public static final int IMAGE_GAMEOVER = 21;

    private static final String[] IMAGES_NAMES = new String[] {
        "background.png",
        "bat/left.png",
        "bat/center.png",
        "bat/right.png",
        "ball/ball0.png",
        "ball/ball1.png",
        "ball/ball2.png",
        "ball/ball3.png",
        "ball/ball4.png",
        "ball/ball5.png",
        "logo.png",
        "splash/brick.png",
        "splash/brickshadow.png",
        "splash/breaker.png",
        "splash/breakershadow.png",
        "splash/pressanykey.png",
        "splash/pressanykeyshadow.png",
        "splash/strike.png",
        "splash/strikeshadow.png",
        "splash/sun.png",
        "ready.png",
        "gameover.png",
    };

    private static ObservableList<Image> images = javafx.collections.FXCollections.<Image>observableArrayList();

    public static ObservableList<Image> getImages() {
        return images;
    }

    public static void initialize() {
        for (String imageName : IMAGES_NAMES) {
            Image image = new Image(Config.class.getResourceAsStream(IMAGE_DIR+imageName));
            if (image.isError()) {
                System.out.println("Image "+imageName+" not found");
            }
            images.add(image);
        }
        for (String imageName : BRICKS_IMAGES) {
            final String url = IMAGE_DIR+"brick/"+imageName;
            Image image = new Image(Config.class.getResourceAsStream(url));
            if (image.isError()) {
                System.out.println("Image "+url+" not found");
            }
            bricksImages.add(image);
        }
        for (String imageName : BONUSES_IMAGES) {
            final String url = IMAGE_DIR+"bonus/"+imageName;
            Image image = new Image(Config.class.getResourceAsStream(url));
            if (image.isError()) {
                System.out.println("Image "+url+" not found");
            }
            bonusesImages.add(image);
        }
    }

    private Config() {
        
    }

}

