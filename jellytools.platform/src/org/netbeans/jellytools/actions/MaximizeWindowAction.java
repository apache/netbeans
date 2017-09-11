/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Used to call "Maximize Window" popup menu item, "Window|Maximize Window"
 * main menu item, shortcut or maximize window by IDE API.
 *
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri Skrivanek
 */
public class MaximizeWindowAction extends Action {

    /**
     * "Window" main menu item.
     */
    private static final String windowItem =
            Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window");
    private static final String configureWindowItem =
            Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window/ConfigureWindow");
    /**
     * "Window|Maximize Window"
     */
    private static final String windowMaximizePath =
            windowItem + "|"
            + configureWindowItem + "|"
            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
            "CTL_MaximizeWindowAction");
    /**
     * "Maximize Window"
     */
    private static final String popupPathMaximize =
            Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle", "CTL_MaximizeWindowAction");

    /** Creates new instance of MaximizeWindowAction. */
    public MaximizeWindowAction() {
        super(windowMaximizePath, popupPathMaximize, "org.netbeans.core.windows.actions.MaximizeWindowAction");
    }

    /** Performs popup action Maximize Window on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and maximized
     */
    @Override
    public void performPopup(ComponentOperator compOperator) {
        if (compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator) compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "MaximizeWindowAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action Maximize Window on given top component operator 
     * which is activated before the action.
     * @param tco top component operator which should be activated and maximized
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPathMaximize);
    }

    /** Maximize active top component by IDE API.*/
    @Override
    public void performAPI() {
        // run in dispatch thread
        new QueueTool().invokeSmoothly(new Runnable() {

            @Override
            public void run() {
                Mode activeMode = (Mode) AttachWindowAction.callWindowManager("getActiveMode");
                if (activeMode != null) {
                    AttachWindowAction.callWindowManager("switchMaximizedMode", activeMode);
                }
            }
        });
    }

    /** Performs Maximize Window by IDE API on given top component operator 
     * which is activated before the action.
     * @param tco top component operator which should be activated and maximized
     */
    public void performAPI(final TopComponentOperator tco) {
        tco.makeComponentVisible();
        // run in dispatch thread
        tco.getQueueTool().invokeSmoothly(new Runnable() {

            @Override
            public void run() {
                WindowManager wm = WindowManager.getDefault();
                TopComponent tc = (TopComponent) tco.getSource();
                Mode mode = (Mode) wm.findMode(tc);
                if (mode == null) {
                    throw new JemmyException("Mode not found for " + tc);
                }
                AttachWindowAction.callWindowManager("switchMaximizedMode", mode);
            }
        });
    }

    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                "MaximizeWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                "MaximizeWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                "MaximizeWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because MaximizeWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                "MaximizeWindowAction doesn't have popup representation on nodes.");
    }
}
