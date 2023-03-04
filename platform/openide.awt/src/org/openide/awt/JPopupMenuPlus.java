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
package org.openide.awt;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/** A subclass of JPopupMenu which ensures that the popup menus do
 * not stretch off the edges of the screen.
 * @deprecated - doesn't do anything special anymore. (since org.openide.awt 6.5)
 */
@Deprecated
public class JPopupMenuPlus extends JPopupMenu {
//    private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N

    public JPopupMenuPlus() {
    }

//    /*
//     * Override the show() method to ensure that the popup will be
//     * on the screen.
//     */
//    public void show(Component invoker, int x, int y) {
//        if (isVisible()) {
//            return;
//        }
//
////        // HACK[pnejedly]: Notify all the items in the menu we're going to show
////        JInlineMenu.prepareItemsInContainer(this);
//
//        // End of HACK
//        if (NO_POPUP_PLACEMENT_HACK) {
//            super.show(invoker, x, y);
//
//            return;
//        }
//
//        Point p = new Point(x, y);
//        SwingUtilities.convertPointToScreen(p, invoker);
//
//        Point newPt = JPopupMenuUtils.getPopupMenuOrigin(this, p);
//        SwingUtilities.convertPointFromScreen(newPt, invoker);
//        super.show(invoker, newPt.x, newPt.y);
//    }
}
