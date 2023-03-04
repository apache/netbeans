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

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bonus extends Parent {

    public static final int TYPE_SLOW = 0;
    public static final int TYPE_FAST = 1;
    public static final int TYPE_CATCH = 2;
    public static final int TYPE_GROW_BAT = 3;
    public static final int TYPE_REDUCE_BAT = 4;
    public static final int TYPE_GROW_BALL = 5;
    public static final int TYPE_REDUCE_BALL = 6;
    public static final int TYPE_STRIKE = 7;
    public static final int TYPE_LIFE = 8;

    public static final int COUNT = 9;

    public static final String[] NAMES = new String[] {
        "SLOW",
        "FAST",
        "CATCH",
        "GROW BAT",
        "REDUCE BAT",
        "GROW BALL",
        "REDUCE BALL",
        "STRIKE",
        "LIFE",
    };

    private int type;
    private int width;
    private int height;
    private ImageView content;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getType() {
        return type;
    }

    public Bonus(int type) {
        content = new ImageView();
        getChildren().add(content);
        this.type = type;
        Image image = Config.getBonusesImages().get(type);
        width = (int)image.getWidth() - Config.SHADOW_WIDTH;
        height = (int)image.getHeight() - Config.SHADOW_HEIGHT;
        content.setImage(image);
        setMouseTransparent(true);
    }

}


