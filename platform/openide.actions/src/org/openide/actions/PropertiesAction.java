/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            return c.toArray(new Node[0]);
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
