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

package org.openide.actions;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/** Get properties of a node.
*
* @see NodeOperation#showProperties(Node[])
* @author   Ian Formanek, Jan Jancura
*/
public class PropertiesAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            NodeOperation.getDefault().showProperties(activatedNodes[0]);
        } else {
            NodeOperation.getDefault().showProperties(activatedNodes);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes != null;
    }

    public JMenuItem getPopupPresenter() {
        JMenuItem prop = new Actions.MenuItem(this, false);

        CustomizeAction customizeAction = SystemAction.get(CustomizeAction.class);

        if (customizeAction.isEnabled()) {
            JInlineMenu mi = new JInlineMenu();
            mi.setMenuItems(new JMenuItem[] { new Actions.MenuItem(customizeAction, false), prop });

            return mi;
        } else {
            return prop;
        }
    }

    public String getName() {
        return NbBundle.getMessage(PropertiesAction.class, "Properties");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PropertiesAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/properties.gif"; // NOI18N
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }

    /** Delegate action for clonned context. Used to provide a special
     * support for getPopupPresenter.
     */
    private static final class DelegateAction implements Action, Presenter.Menu, Presenter.Toolbar, Presenter.Popup {
        /** action to delegate to */
        private PropertiesAction delegate;

        /** lookup we try to work in */
        private Lookup lookup;

        public DelegateAction(PropertiesAction a, Lookup actionContext) {
            this.delegate = a;
            this.lookup = actionContext;
        }

        private Node[] nodes() {
            Collection<? extends Node> c = lookup.lookupAll(Node.class);
            return c.toArray(new Node[c.size()]);
        }

        /** Overrides superclass method, adds delegate description. */
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            delegate.performAction(nodes());
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // ignore
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // ignore
        }

        public void putValue(String key, Object o) {
        }

        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        public boolean isEnabled() {
            return delegate.enable(nodes());
        }

        public void setEnabled(boolean b) {
            assert false;
        }

        public JMenuItem getMenuPresenter() {
            return new Actions.MenuItem(this, true);
        }

        public JMenuItem getPopupPresenter() {
            JMenuItem prop = new Actions.MenuItem(this, false);

            Action customizeAction = SystemAction.get(CustomizeAction.class);

            // Retrieve context sensitive action instance if possible.
            if (lookup != null) {
                customizeAction = ((ContextAwareAction) customizeAction).createContextAwareInstance(lookup);
            }

            if (customizeAction.isEnabled()) {
                JInlineMenu mi = new JInlineMenu();
                mi.setMenuItems(new JMenuItem[] { new Actions.MenuItem(customizeAction, false), prop });

                return mi;
            } else {
                for (Node n : nodes()) {
                    for (Node.PropertySet ps : n.getPropertySets()) {
                        if (ps.getProperties().length > 0) {
                            // OK, we have something to show!
                            return prop;
                        }
                    }
                }
                // else nothing to show, so show nothing
                return new JInlineMenu();
            }
        }

        public Component getToolbarPresenter() {
            return new Actions.ToolbarButton(this);
        }
    }
     // end of DelegateAction
}
