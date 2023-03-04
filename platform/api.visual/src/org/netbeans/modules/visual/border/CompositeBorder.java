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
package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.border.Border;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class CompositeBorder implements Border {

    private Border[] borders;
    private Insets insets;

    public CompositeBorder (Border... borders) {
        this.borders = borders;

        Insets result = new Insets (0, 0, 0, 0);
        for (Border border : borders) {
            Insets insets = border.getInsets ();
            result.top += insets.top;
            result.left += insets.left;
            result.bottom += insets.bottom;
            result.right += insets.right;
        }
        this.insets = result;
    }

    public Insets getInsets () {
        return insets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        for (Border border : borders) {
            border.paint (gr, new Rectangle (bounds));
            Insets insets = border.getInsets ();
            bounds.x += insets.left;
            bounds.width -= insets.left + insets.right;
            bounds.y += insets.top;
            bounds.height -= insets.top + insets.bottom;
        }
    }

    public boolean isOpaque () {
        for (Border border : borders) {
            if (border.isOpaque ())
                return true;
        }
        return false;
    }

}
