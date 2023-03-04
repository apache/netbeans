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
