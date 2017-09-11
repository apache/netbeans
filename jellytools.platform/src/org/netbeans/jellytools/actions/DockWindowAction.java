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
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/** Used to call "Dock Window" popup menu item, "Window|Dock Window" main menu item or
 * "org.netbeans.core.windows.actions.UnDockWindowAction".
 *
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Vojtech.Sigler@sun.com
 */
public class DockWindowAction extends Action {

    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
                                                                    "Menu/Window");
    private static final String configureWindowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
            "Menu/Window/ConfigureWindow");
    /** "Window|Dock Window"" main menu item. */
    private static final String menuPath = windowItem
                                            + "|"
                                            + configureWindowItem
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                            "CTL_UndockWindowAction_Dock");

    /** "Dock Window" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                            "CTL_UndockWindowAction_Dock");

    /** Create new DockWindowAction instance. */
    public DockWindowAction() {
        super(menuPath, popupPath, "org.netbeans.core.windows.actions.UndockWindowAction");
    }

    /** Performs popup action "Dock Window" on given component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and docked
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "DockWindowAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action "Dock Window" on given top component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param tco top component operator which should be activated and docked
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

}
