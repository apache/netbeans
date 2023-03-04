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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class OverlayLayout implements Layout {

    public void layout (Widget widget) {
        Dimension total = new Dimension ();
        for (Widget child : widget.getChildren ()) {
            if (! child.isVisible ())
                continue;
            Dimension size = child.getPreferredBounds ().getSize ();
            if (size.width > total.width)
                total.width = size.width;
            if (size.height > total.height)
                total.height = size.height;
        }
        for (Widget child : widget.getChildren ()) {
            Point location = child.getPreferredBounds ().getLocation ();
            child.resolveBounds (new Point (- location.x, - location.y), new Rectangle (location, total));
        }
    }

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget widget) {
        Rectangle clientArea = widget.getClientArea ();
        for (Widget child : widget.getChildren ()) {
            if (child.isVisible ()) {
                Point location = child.getPreferredBounds ().getLocation ();
                child.resolveBounds (new Point (clientArea.x - location.x, clientArea.y - location.y), new Rectangle (location, clientArea.getSize ()));
            } else {
                child.resolveBounds (clientArea.getLocation (), new Rectangle ());
            }
        }
    }

}
