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
package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JTree;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

/**
 * Provides access to Navigator TopComponent belonging to form editor.
 */
public class ComponentInspectorOperator extends NavigatorOperator {

    private PropertySheetOperator _properties;

    /** Getter for component tree.
     * @return JTreeOperator instance
     */
    public JTreeOperator treeComponents() {
        new FormDesignerOperator(null).makeComponentVisible();
        return new JTreeOperator(this, new FormTreeComponentChooser());
    }

    /** Getter for PropertySheetOperator. It returns first found property
     * sheet within IDE. It is not guaranteed that it is the global property
     * placed next to Component Inspector by default.
     * @return PropertySheetOperator instance
     */
    public PropertySheetOperator properties() {
        if (_properties == null) {
            _properties = new PropertySheetOperator();
        }
        return (_properties);
    }

    /** Opens property sheet and returns PropertySheetOperator instance for
     * given component path.
     * @param componentPath path in component tree (e.g. "[JFrame]|jPanel1")
     * @return instance of PropertySheetOperator
     */
    public PropertySheetOperator properties(final String componentPath) {
        final AtomicReference<String> nodeText = new AtomicReference<String>();
        freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                Node node = new Node(treeComponents(), componentPath);
                nodeText.set(node.getText());
                new PropertiesAction().perform(node);
            }
        });
        return new PropertySheetOperator(nodeText.get());
    }

    /** Selects component in the tree.
     * @param componentPath path in component tree (e.g. "[JFrame]|jPanel1")
     */
    public void selectComponent(final String componentPath) {
        freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                Node node = new Node(treeComponents(), componentPath);
                node.select();
            }
        });
    }

    /** Performs action on given component path. It is guaranteed that Navigator
     * doesn't change context while executing an action.
     * @param action action to be called
     * @param componentPath path in component tree (e.g. "[JFrame]|jPanel1")
     */
    public void performAction(final Action action, final String componentPath) {
        freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                action.perform(new Node(treeComponents(), componentPath));
            }
        });
    }

    /** Performs verification by accessing all sub-components */
    public void verify() {
        treeComponents();
        properties().verify();
    }

    /** Changes context to show form hierarchy in navigator and prevents
     * other context changes while code in given Runnable is being executed.
     * @param runnable runnable to be executed
     */
    public void freezeNavigatorAndRun(Runnable runnable) {
        new FormDesignerOperator(null).makeComponentVisible();
        Object navigatorController;
        Lookup.Result<Node> curNodesRes;
        Lookup.Result<Node> curHintsRes;
        try {
            // remove LookupListeners in NavigatorController
            Class navigatorTCClass = Class.forName("org.netbeans.modules.navigator.NavigatorTC", true, Thread.currentThread().getContextClassLoader());
            Method getControllerMethod = navigatorTCClass.getMethod("getController");
            navigatorController = getControllerMethod.invoke(getSource());
            Field curNodesResField = navigatorController.getClass().getDeclaredField("curNodesRes");
            curNodesResField.setAccessible(true);
            curNodesRes = (Lookup.Result<Node>) curNodesResField.get(navigatorController);
            curNodesRes.removeLookupListener((LookupListener) navigatorController);
            Field curHintsResField = navigatorController.getClass().getDeclaredField("curHintsRes");
            curHintsResField.setAccessible(true);
            curHintsRes = (Lookup.Result<Node>) curHintsResField.get(navigatorController);
            curHintsRes.removeLookupListener((LookupListener) navigatorController);
            // let pending postponed events proceed
            new EventTool().waitNoEvent(500);
        } catch (Exception e) {
            throw new JemmyException("Failed when freezing navigator.", e);
        }
        // run our code
        runnable.run();
        // add listeners back
        curNodesRes.addLookupListener((LookupListener) navigatorController);
        curHintsRes.addLookupListener((LookupListener) navigatorController);
    }

    private static final class FormTreeComponentChooser implements ComponentChooser {

        @Override
        public boolean checkComponent(Component comp) {
            Object root = ((JTree) comp).getModel().getRoot();
            if (root != null) {
                String name = root.toString();
                return name != null && name.startsWith("Form");
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "Form Tree";  //NOI18N
        }
    }
}
