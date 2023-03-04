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
/*
 * WinVistaEditorTabCellRenderer.java
 *
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import javax.swing.Icon;

/**
 * Windows 8 implementation of tab renderer
 *
 * @author S. Aubrecht
 * @since 1.41
 */
class Windows8EditorTabCellRenderer extends WinVistaEditorTabCellRenderer {

    public Windows8EditorTabCellRenderer() {
    }

    @Override
    void paintTabGradient( Graphics g, Polygon poly ) {
        Rectangle rect = poly.getBounds(); 
        boolean selected = isSelected();
        boolean focused = selected && isActive();
        boolean attention = isAttention();
        boolean mouseOver = isArmed();
        Windows8ViewTabDisplayerUI.paintTabBackground( (Graphics2D)g, rect.x, rect.y, rect.width, rect.height, selected, focused, attention, mouseOver);
    }

    @Override
    Icon findIcon() {
        final String file;
        if( inCloseButton() && isPressed() ) {
            file = "org/openide/awt/resources/win8_bigclose_pressed.png"; // NOI18N
        } else if( inCloseButton() ) {
            file = "org/openide/awt/resources/win8_bigclose_rollover.png"; // NOI18N
        } else {
            file = "org/openide/awt/resources/win8_bigclose_enabled.png"; // NOI18N
        }
        return TabControlButtonFactory.getIcon(file);
    }
}
