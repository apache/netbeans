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

import org.netbeans.api.visual.anchor.AnchorShape;

import java.awt.geom.GeneralPath;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class TriangleAnchorShape implements AnchorShape {

    public static final Stroke STROKE = new BasicStroke (1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private int size;
    private boolean filled;
    private boolean hollow;
    private double cutDistance;

    private GeneralPath generalPath;

    /**
     * Creates a triangular anchor shape.
     * @param size the size of triangle
     * @param filled if true, then the triangle is filled
     * @param output if true, then it is output triangle
     * @param cutDistance the cut distance
     */
    public TriangleAnchorShape (int size, boolean filled, boolean output, boolean hollow, double cutDistance) {
        this.size = size;
        this.filled = filled;
        this.hollow = hollow;
        this.cutDistance = cutDistance;

        float side = size * 0.3f;
        generalPath = new GeneralPath ();
        if (output) {
            generalPath.moveTo (size, 0.0f);
            generalPath.lineTo (0.0f, -side);
            generalPath.lineTo (0.0f, +side);
            if (hollow)
                generalPath.lineTo (size, 0.0f);
        } else {
            generalPath.moveTo (0.0f, 0.0f);
            generalPath.lineTo (size, -side);
            generalPath.lineTo (size, +side);
            if (hollow)
                generalPath.lineTo (0.0f, 0.0f);
        }
    }

    public boolean isLineOriented () {
        return true;
    }

    public int getRadius () {
        return (int) Math.ceil (1.5f * size);
    }

    public double getCutDistance () {
        return cutDistance;
    }

    public void paint (Graphics2D graphics, boolean source) {
        if (filled)
            graphics.fill (generalPath);
        else {
            Stroke stroke = graphics.getStroke ();
            graphics.setStroke (STROKE);
            graphics.draw (generalPath);
            graphics.setStroke (stroke);
        }
    }

}
