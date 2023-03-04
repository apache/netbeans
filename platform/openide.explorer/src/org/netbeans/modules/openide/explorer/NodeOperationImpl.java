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

package org.netbeans.modules.openide.explorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.*;

import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;
import org.openide.util.UserCancelException;

/**
 * Default implementation of node operations like show properties, etc.
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.nodes.NodeOperation.class)
public final class NodeOperationImpl extends org.openide.nodes.NodeOperation {

    public boolean customize(Node node) {
        Component customizer = node.getCustomizer();
        if (customizer == null) {
            return false;
        }
        final JDialog d = new JDialog();
        d.setModal(false);
        d.setTitle(node.getDisplayName());
        d.getContentPane().setLayout(new BorderLayout());
        d.getContentPane().add(customizer, BorderLayout.CENTER);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.pack();
        d.setVisible(true);
        return true;
    }

    public void explore(Node n) {
        JDialog d = new JDialog();
        d.setTitle(n.getDisplayName());
        d.setModal(false);
        d.getContentPane().setLayout(new BorderLayout());
        EP p = new EP();
        p.getExplorerManager().setRootContext(n);
        p.setLayout(new BorderLayout());
        p.add(new BeanTreeView(), BorderLayout.CENTER);
        d.getContentPane().add(p, BorderLayout.CENTER);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.pack();
        d.setVisible(true);
    }

    public Node[] select(String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top) throws UserCancelException {
        // XXX rootTitle and acceptor currently ignored
        JDialog d = new JDialog();
        d.setTitle(title);
        d.setModal(true);
        d.getContentPane().setLayout(new BorderLayout());
        EP p = new EP();
        p.getExplorerManager().setRootContext(root);
        p.setLayout(new BorderLayout());
        p.add(new BeanTreeView(), BorderLayout.CENTER);
        d.getContentPane().add(p, BorderLayout.CENTER);
        if (top != null) {
            d.getContentPane().add(top, BorderLayout.NORTH);
        }
        d.pack();
        d.setVisible(true);
        Node[] nodes = p.getExplorerManager().getSelectedNodes();
        d.dispose();
        return nodes;
    }

    public void showProperties(Node n) {
        showProperties(new Node[] {n});
    }

    public void showProperties(Node[] nodes) {
        PropertySheet ps = new PropertySheet();
        ps.setNodes(nodes);
        JDialog d = new JDialog();
        d.setTitle("Properties"); // XXX I18N
        d.setModal(true);
        d.getContentPane().setLayout(new BorderLayout());
        d.getContentPane().add(ps, BorderLayout.CENTER);
        d.pack();
        d.setVisible(true);
        d.dispose();
    }
    
    private static final class EP extends JPanel 
    implements org.openide.explorer.ExplorerManager.Provider {
        private org.openide.explorer.ExplorerManager em = new org.openide.explorer.ExplorerManager ();
        
        public org.openide.explorer.ExplorerManager getExplorerManager () {
            return em;
        }
    }

    /**
     * Shows a modal dialog with the custom editor of given property, just like
     * it would be invoked when clicking the [...] button next to a property in
     * the property sheet. The property value is updated if the dialog is
     * successfully closed via the OK button.
     * @param property The property to be edited (its property editor to be used).
     * @param beans The objects the property belongs to. Typically one item
     *   array with the Node of the property. The meaning is the same as in
     *   {@link org.openide.explorer.propertysheet.PropertyEnv#getBeans()}.
     */
    @Override
    public void showCustomEditorDialog(Node.Property<?> property, Object... beans) {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException();
        }
        if (accessor == null) {
            try {
                // hack to make sure the accessor initialized from PropertyEnv static init
                Class.forName(PropertyEnv.class.getName(), true, getClass().getClassLoader());
            } catch (ClassNotFoundException ex) {
            }
        }
        accessor.showDialog(property, beans);
    }

    private static CustomEditorAccessor accessor;

    public static void registerCustomEditorAccessor(CustomEditorAccessor acc) {
        accessor = acc;
    }

    public interface CustomEditorAccessor {
        void showDialog(Node.Property property, Object[] beans);
    }
}
