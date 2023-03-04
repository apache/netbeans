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
import javafx.scene.image.ImageView;

public class Ball extends Parent {
    
    public static final int DEFAULT_SIZE = 2;
    
    public static final int MAX_SIZE = 5;

    private int size;

    private int diameter;
    private ImageView imageView;

    public Ball() {
        imageView = new ImageView();
        getChildren().add(imageView);
        changeSize(DEFAULT_SIZE);
        setMouseTransparent(true);
    }

    public int getSize() {
        return size;
    }

    public int getDiameter() {
        return diameter;
    }

    public void changeSize(int newSize) {
        size = newSize;
        imageView.setImage(Config.getImages().get(Config.IMAGE_BALL_0 + size));
        diameter = (int) imageView.getImage().getWidth() - Config.SHADOW_WIDTH;
    }

}

