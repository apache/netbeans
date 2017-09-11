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
