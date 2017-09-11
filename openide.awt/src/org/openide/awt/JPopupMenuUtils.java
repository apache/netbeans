/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.openide.awt;

import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

import java.awt.*;

import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/** A class that contains a set of utility classes and methods
 * around displaying and positioning popup menus.
 *
 * Popup menus sometimes need to be repositioned so that they
 * don't "fall off" the edges of the screen.
 *
 * Some of the menus have items that are added dynamically, that is,
 * after the menu is displayed.  These menus are often placed correctly
 * for their initial size, but will need to be repositioned as they
 * grow.
 *
 * @author   Evan Adams
 */
public class JPopupMenuUtils {
    private static boolean problemTested = false;
    private static boolean problem = false;
    private static RequestProcessor reqProc;
    private static RequestProcessor.Task task;
//    private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N

    /*
     * Called when a visible menu has dynamically changed.  Ensure that
     * it stays on the screen.  Compute its new location and,
     * if it differs from the current one, move the popup.
     *
     * @param popup the popup menu
     */
    public static void dynamicChange(final JPopupMenu popup, boolean usedToBeContained) {
        if (!popup.isShowing()) {
            return;
        }

        if (isProblemConfig()) {
            callRefreshLater(popup);

            return;
        }

        refreshPopup(popup);

        Point p = popup.getLocationOnScreen();
        Point newPt = getPopupMenuOrigin(popup, p);

        boolean willBeContained = willPopupBeContained(popup, newPt);

        if (usedToBeContained != willBeContained) {
            popup.setVisible(false);
        }

        if (!newPt.equals(p)) {
//            if (!NO_POPUP_PLACEMENT_HACK) {
//                popup.setLocation(newPt.x, newPt.y);
//            }
        }

        if (usedToBeContained != willBeContained) {
            popup.setVisible(true);
        }
    }

    /** Mysterious calls to pack(), invalidate() and validate() ;-) */
    private static void refreshPopup(JPopupMenu popup) {
        popup.pack();
        popup.invalidate();

        Component c = popup.getParent();

        if (c != null) {
            c.validate();
        }
    }

    /** Called from dynamicChange. Performs refresh of the popup
     * in task in reqProc.
     */
    private static void callRefreshLater(final JPopupMenu popup) {
        // this may cause the popup to flicker
        if (reqProc == null) {
            reqProc = new RequestProcessor();
        }

        if (task == null) {
            task = reqProc.create(
                    new Runnable() {
                        public void run() {
                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        task = null;

                                        // after the action is performed new task should be created
                                        // (probably for another instance of a popup)
                                        if (!popup.isShowing()) {
                                            return;
                                        }

                                        Point p = popup.getLocationOnScreen();
                                        Point newPt = getPopupMenuOrigin(popup, p);
                                        popup.setVisible(false);
                                        refreshPopup(popup);

                                        if (!newPt.equals(p)) {
//                                            if (!NO_POPUP_PLACEMENT_HACK) {
//                                                popup.setLocation(newPt.x, newPt.y);
//                                            }
                                        }

                                        popup.setVisible(true);
                                    }
                                }
                            );
                        }
                    }
                );
        }

        task.schedule(100);
    }

    /** Returns true when the popup has to be unconditioanlly
     * redisplayed when adding new items. Currently
     * this is true with JDK1.3 on Linux/Gnome. This
     * method checks for presence of system property
     * "netbeans.popup.linuxhack".
     */
    private static boolean isProblemConfig() {
        // we have already tested the need for the hack
        if (problemTested) {
            return problem;
        }

        problem = false;

        String needHack = System.getProperty("netbeans.popup.linuxhack");

        if (needHack != null) {
            problem = true;
        }

        return problem;
    }

    /*
     * Called when a visible submenu (pullright) has dynamically changed.
     * Ensure that it stays on the screen.  If it doesn't fit, then hide
     * the popup and redisplay it.  This causes JMenu's placement code
     * to get executed again which may change the submens to go up rather
     * than down.
     *
     * @param popup the popup menu
     */
    public static void dynamicChangeToSubmenu(JPopupMenu popup, boolean usedToBeContained) {
        Object invoker = popup.getInvoker();

        if (!(invoker instanceof JMenu)) {
            return;
        }

        JMenu menu = (JMenu) invoker;

        if (!popup.isShowing()) {
            return;
        }

        if (isProblemConfig()) {
            callRefreshLater2(popup, menu);

            return;
        }

        refreshPopup(popup);

        Point p = popup.getLocationOnScreen();
        Dimension popupSize = popup.getPreferredSize();
        Rectangle popupRect = new Rectangle(p, popupSize);
        Rectangle screenRect = getScreenRect();
        boolean willBeContained = isPopupContained(popup);

        if (!screenRect.contains(popupRect)) {
            /*
             * The menu grew off the edge of the screen.
             */
            menu.setPopupMenuVisible(false);
            menu.setPopupMenuVisible(true);
        } else if (usedToBeContained != willBeContained) {
            /*
             * The menu grew off the edge of the containing window.
             * Use the setVisible() hack to change the menu from
             * lightweight to heavyweight.
             */
            popup.setVisible(false);
            popup.setVisible(true);
        }
    }

    /** Called from dynamicChangeToSubmenu. Calls the popup refresh
     * in a task in the reqProc.
     */
    private static void callRefreshLater2(final JPopupMenu popup, final JMenu menu) {
        // this may cause the popup to flicker
        if (reqProc == null) {
            reqProc = new RequestProcessor();
        }

        if (task == null) {
            task = reqProc.create(
                    new Runnable() {
                        public void run() {
                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        task = null;

                                        // after the action is performed new task should be created
                                        // (probably for another instance of a popup)
                                        if (!popup.isShowing()) {
                                            return;
                                        }

                                        popup.setVisible(false);
                                        refreshPopup(popup);
                                        popup.setVisible(true);

                                        Point p = popup.getLocationOnScreen();
                                        Dimension popupSize = popup.getPreferredSize();
                                        Rectangle popupRect = new Rectangle(p, popupSize);
                                        Rectangle screenRect = getScreenRect();

                                        if (!screenRect.contains(popupRect)) {
                                            menu.setPopupMenuVisible(false);
                                            menu.setPopupMenuVisible(true);
                                        }
                                    }
                                }
                            );
                        }
                    }
                );
        }

        task.schedule(100);
    }

    /*
     * Return the point for the origin of this popup.
     * This is where the adjustments are made to ensure the
     * popup stays on the screen.
     *
     * @param popup the popup menu
     * @param p the popup menu's origin
     * @return the popup menu's new origin
     */
    static Point getPopupMenuOrigin(JPopupMenu popup, Point p) {
        Point newPt = new Point(p);
        Dimension popupSize = popup.getPreferredSize();
        Rectangle screenRect = getScreenRect();
        int popupRight = newPt.x + popupSize.width;
        int popupBottom = newPt.y + popupSize.height;
        int screenRight = screenRect.x + screenRect.width;
        int screenBottom = screenRect.y + screenRect.height;

        if (popupRight > screenRight) { // Are we off the right edge?
            newPt.x = screenRight - popupSize.width;
        }

        if (newPt.x < screenRect.x) { // Are we off the left edge?
            newPt.x = screenRect.x;
        }

        if (popupBottom > screenBottom) { // Are we off the bottom edge?
            newPt.y = screenBottom - popupSize.height;
        }

        if (newPt.y < screenRect.y) { // Are we off the top edge?
            newPt.y = screenRect.y;
        }

        return newPt;
    }

    /*
     * Return whether or not the given popup is contained by its
     * parent window.  Uses the current location and size of the popup.
     *
     * @return boolean indicating if the popup is contained
     */
    public static boolean isPopupContained(JPopupMenu popup) {
        if (!popup.isShowing()) {
            return false;
        }

        return willPopupBeContained(popup, popup.getLocationOnScreen());
    }

    /*
     * Return whether or not the given popup will be contained by
     * its parent window if it is moved to <code>origin</origin>.
     * Use its current size.
     *
     * @param <code>popup</code> the popup to be tested
     * @param <code>origin</code> location of the popup to be tested
     * @return boolean indicating if the popup will be contained
     */
    private static boolean willPopupBeContained(JPopupMenu popup, Point origin) {
        if (!popup.isShowing()) {
            return false;
        }

        Window w = SwingUtilities.windowForComponent(popup.getInvoker());
        Rectangle r = new Rectangle(origin, popup.getSize());

        return (w != null) && w.getBounds().contains(r);
    }

    /*
     * Return a rectange defining the usable portion of the screen.  Originally
     * designed to provide a way to account for the taskbar in Windows.  Didn't
     * work with multiple monitor configuration.  The new implementation
     * detects the current monitor and returns its bounds.  Never cache the
     * result of this method.
     *
     * @return a rectangle defining the usable area.
     */
    public static Rectangle getScreenRect() {
        return Utilities.getUsableScreenBounds();
    }
}
