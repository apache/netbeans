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
