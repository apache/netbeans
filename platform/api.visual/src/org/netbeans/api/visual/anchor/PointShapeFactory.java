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
package org.netbeans.api.visual.anchor;

import org.netbeans.modules.visual.anchor.ImagePointShape;
import org.netbeans.modules.visual.anchor.SquarePointShape;

import java.awt.*;

/**
 * The factory class of all built-in point shapes.
 * The instances of all built-in point shapes can be used multiple connection widgets.
 *
 * @author David Kaspar
 */
public class PointShapeFactory {

    private PointShapeFactory () {
    }

    /**
     * Creates a square shape.
     * @param size the size
     * @param filled if true, then the shape is filled
     */
    public static PointShape createPointShape (int size, boolean filled) {
        return new SquarePointShape (size, filled);
    }

    /**
     * Creates an image point shape.
     * @param image the image
     * @return the point shape
     */
    public static PointShape createImagePointShape (Image image) {
        return new ImagePointShape (image);
    }

}
