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
package org.openide.nodes;

import org.openide.util.Lookup;
import org.openide.util.UserCancelException;

import java.awt.Component;

import javax.swing.*;


/** Provides common operations on nodes.
 * Any component may
 * ask to open a customizer for, or explore, any node.
 * @since 3.14
 */
public abstract class NodeOperation {
    /** Subclass constructor. */
    protected NodeOperation() {
    }

    /** Get default instance from lookup.
     * @return some instance
     */
    public static NodeOperation getDefault() {
        NodeOperation no = Lookup.getDefault().lookup(NodeOperation.class);

        if (no == null) {
            throw new IllegalStateException(
                "To use NodeOperation you should have its implementation around. For example one from openide-explorer.jar" // NOI18N
            );
        }

        return no;
    }

    /** Tries to open a customization dialog for the specified node.
     * The dialog is
     * modal and the function returns only after
     * customization is finished, if it was possible.
     *
     * @param n the node to customize
     * @return <CODE>true</CODE> if the node had a customizer,
     * <CODE>false</CODE> if not
     * @see Node#hasCustomizer
     * @see Node#getCustomizer
     */
    public abstract boolean customize(Node n);

    /** Explore a node (and its subhierarchy).
     * It will be opened in a new Explorer view, as the root node of that window.
     * @param n the node to explore
     */
    public abstract void explore(Node n);

    /** Open a modal Property Sheet on a node.
     * @param n the node to show properties of
     */
    public abstract void showProperties(Node n);

    /** Open a modal Property Sheet on a set of nodes.
     * @param n the array of nodes to show properties of
     * @see #showProperties(Node)
     */
    public abstract void showProperties(Node[] n);

    /**
     * Shows a modal dialog with the custom editor of given property, just like
     * it would be invoked when clicking the [...] button next to a property in
     * the property sheet. The property value is updated if the dialog is
     * successfully closed via the OK button.
     * @param property The property to be edited (its property editor to be used).
     * @param beans The objects the property belongs to. Typically one item
     *   array with the Node of the property. The meaning is the same as in
     *   {@link org.openide.explorer.propertysheet.PropertyEnv#getBeans()}.
     * @since 7.24
     */
    public void showCustomEditorDialog(Node.Property<?> property, Object... beans) {
        throw new UnsupportedOperationException();
    }

    /** Open a modal Explorer on a root node, permitting a node selection to be returned.
     * <p>The acceptor
     * should be asked each time the set of selected nodes changes, whether to accept or
     * reject the current result. This will affect for example the
     * display of the "OK" button.
     *
     * @param title title of the dialog
     * @param rootTitle label at root of dialog. May use <code>&amp;</code> for a {@link javax.swing.JLabel#setDisplayedMnemonic(int) mnemonic}.
     * @param root root node to explore
     * @param acceptor class asked to accept or reject current selection
     * @param top an extra component to be placed on the dialog (may be <code>null</code>)
     * @return an array of selected (and accepted) nodes
     *
     * @exception UserCancelException if the selection is interrupted by the user
     */
    public abstract Node[] select(String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top)
    throws UserCancelException;

    /** Open a modal Explorer without any extra dialog component.
     * @param title title of the dialog
     * @param rootTitle label at root of dialog. May use <code>&amp;</code> for a {@link javax.swing.JLabel#setDisplayedMnemonic(int) mnemonic}.
     * @param root root node to explore
     * @param acceptor class asked to accept or reject current selection
     * @return an array of selected (and accepted) nodes
     *
     * @exception UserCancelException if the selection is interrupted by the user
     * @see #select(String, String, Node, NodeAcceptor, Component)
     */
    public Node[] select(String title, String rootTitle, Node root, NodeAcceptor acceptor)
    throws UserCancelException {
        return select(title, rootTitle, root, acceptor, null);
    }

    /** Open a modal Explorer accepting only a single node.
     * @param title title of the dialog
     * @param rootTitle label at root of dialog. May use <code>&amp;</code> for a {@link javax.swing.JLabel#setDisplayedMnemonic(int) mnemonic}.
     * @param root root node to explore
     * @return the selected node
     *
     * @exception UserCancelException if the selection is interrupted by the user
     * @see #select(String, String, Node, NodeAcceptor)
     */
    public final Node select(String title, String rootTitle, Node root)
    throws UserCancelException {
        return select(
            title, rootTitle, root,
            new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    return nodes.length == 1;
                }
            }
        )[0];
    }
}
