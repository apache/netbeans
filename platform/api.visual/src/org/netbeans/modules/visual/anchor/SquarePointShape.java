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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.anchor.PointShape;

import java.awt.*;

/**
 * Represents a square point shape.
 * @author David Kaspar
 */
public final class SquarePointShape implements PointShape {

    private int size;
    private boolean filled;

    /**
     * Creates a square shape.
     * @param size   the size
     * @param filled if true, then the shape is filled
     */
    public SquarePointShape (int size, boolean filled) {
        this.size = size;
        this.filled = filled;
    }

    public int getRadius () {
        return (int) Math.ceil (1.5f * size);
    }

    public void paint (Graphics2D graphics) {
        int size2 = size + size;
        Rectangle rect = new Rectangle (- size, - size, size2, size2);
        if (filled)
            graphics.fill (rect);
        else
            graphics.draw (rect);
    }

}
