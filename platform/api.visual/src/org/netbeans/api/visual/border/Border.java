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
package org.netbeans.api.visual.border;

import java.awt.*;

/**
 * The class is responsible for defining and rendering borders. Border size is defined by insets.
 * <p>
 * Borders can be opaque. If true, then the border has to care about painting all pixels in borders.
 * If false, then the widget background is painted under borders too.
 * <p>
 * This can be used for non-rectagular shapes of borders e.g. returning true from isOpaque and drawing a filled rounded rectangle.
 *
 * @author David Kaspar
 */
// TODO - change abstract class to assure immutable insets?
public interface Border {

    /**
     * Returns layout insets. Insets has to be the same during whole life-cycle of the border.
     * @return the insets
     */
    // WARNING - must be immutable during whole lifecycle
    public Insets getInsets ();

    /**
     * Paints the border to the Graphics2D instance within specific bounds.
     * Borders are always painted immediately after the widget background and before the widget painting itself.
     * @param gr the Graphics2D instance
     * @param bounds the boundary
     */
    public void paint (Graphics2D gr, Rectangle bounds);

    /**
     * Returns whether the border is opaque. The result of the method controls whether a widget background is painted
     * under the border insets too.
     * @return true, if background is painted under the border insets.
     */
    public boolean isOpaque ();

}
