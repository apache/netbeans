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

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * @author Antonio
 */
public class ArrowAnchorShape implements AnchorShape {

    private static final Stroke STROKE = new BasicStroke (1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private GeneralPath path;
    private int size;

    public ArrowAnchorShape (int degrees, int size) {
        this.size = size;
        path = new GeneralPath ();

        double radians = Math.PI * degrees / 180.0;
        double cos = Math.cos (radians / 2.0);
        double sin = -size * Math.sqrt (1 - cos * cos);
        cos *= size;

        path.moveTo (0.0f, 0.0f);
        path.lineTo ((float) cos, (float) -sin);
        path.moveTo (0.0f, 0.0f);
        path.lineTo ((float) cos, (float) sin);
    }

    public boolean isLineOriented () {
        return true;
    }

    public int getRadius () {
        return size + 1;
    }

    public double getCutDistance () {
        return 0;
    }

    public void paint (Graphics2D graphics, boolean source) {
        Stroke previousStroke = graphics.getStroke ();
        graphics.setStroke (STROKE);
        graphics.draw (path);
        graphics.setStroke (previousStroke);
    }

}
