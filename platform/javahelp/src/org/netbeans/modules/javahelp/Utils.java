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

package org.netbeans.modules.javahelp;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Utility class with a set of the static methods.
 *
 * @author Victor G. Vasilyev
 */
class Utils {

    /**
     * Shows the specified popup menu.
     * @param e - the mouse event
     * @param m - the popup menu
     * @param dst - the destination component
     */
    static void showPopupMenu(MouseEvent e, JPopupMenu m, Component dst) {
       Point pt = convertPoint(e.getComponent(), e.getPoint(), dst);
       m.show(dst, pt.x, pt.y);
    }

    private static Point convertPoint(Component src, Point p, Component dst) {
        return SwingUtilities.convertPoint(src, p, dst);
    }

    static boolean isMouseRightClick(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON3 &&
               e.getClickCount() == 1;
    }

}
