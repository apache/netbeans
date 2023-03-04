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

public class Brick extends Parent {

    public static final int TYPE_BLUE = 0;
    public static final int TYPE_BROKEN1 = 1;
    public static final int TYPE_BROKEN2 = 2;
    public static final int TYPE_BROWN = 3;
    public static final int TYPE_CYAN = 4;
    public static final int TYPE_GREEN = 5;
    public static final int TYPE_GREY = 6;
    public static final int TYPE_MAGENTA = 7;
    public static final int TYPE_ORANGE = 8;
    public static final int TYPE_RED = 9;
    public static final int TYPE_VIOLET = 10;
    public static final int TYPE_WHITE = 11;
    public static final int TYPE_YELLOW = 12;

    private int type;
    private ImageView content;

    public Brick(int type) {
        content = new ImageView();
        getChildren().add(content);
        changeType(type);
        setMouseTransparent(true);
    }

    public int getType() {
        return type;
    }

    public boolean kick() {
        if (type == TYPE_GREY) {
            return false;
        }
        if (type == TYPE_BROKEN1) {
            changeType(TYPE_BROKEN2);
            return false;
        }
        return true;
    }

    private void changeType(int newType) {
        this.type = newType;
        Image image = Config.getBricksImages().get(type);
        content.setImage(image);
        content.setFitWidth(Config.FIELD_WIDTH/15);
    }

    

    public static int getBrickType(String s) {
        if (s.equals("L")) {
            return TYPE_BLUE;
        } else if (s.equals("2")) {
            return TYPE_BROKEN1;
        } else if (s.equals("B")) {
            return TYPE_BROWN;
        } else if (s.equals("C")) {
            return TYPE_CYAN;
        } else if (s.equals("G")) {
            return TYPE_GREEN;
        } else if (s.equals("0")) {
            return TYPE_GREY;
        } else if (s.equals("M")) {
            return TYPE_MAGENTA;
        } else if (s.equals("O")) {
            return TYPE_ORANGE;
        } else if (s.equals("R")) {
            return TYPE_RED;
        } else if (s.equals("V")) {
            return TYPE_VIOLET;
        } else if (s.equals("W")) {
            return TYPE_WHITE;
        } else if (s.equals("Y")) {
            return TYPE_YELLOW;
        } else {
            System.out.println("Unknown brick type '{s}'");
            return TYPE_WHITE;
        }
    }

}


