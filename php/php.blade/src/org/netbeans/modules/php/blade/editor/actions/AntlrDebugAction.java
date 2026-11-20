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
package org.netbeans.modules.php.blade.editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 *
 * TODO make it configurable in options
 */
@ActionID(id = "org.netbeans.modules.php.blade.editor.actions.AntlrDebug", category = "System")
@ActionRegistration(displayName = "Antlr Debug", lazy=false)
public class AntlrDebugAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    private Node node;

    public void setNode(Node node){
        this.node = node;
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        Node folderNode = lkp.lookup(Node.class);
        AntlrDebugAction act =  new AntlrDebugAction();
        act.setNode(folderNode);
        return act;
    }

    static final long serialVersionUID = 4906417339959070129L;

    @Override
    public void actionPerformed(ActionEvent ae) {
        assert false;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new AntlrDebugAction.Popup(this);
    }

    private List<JMenuItem> generate(Action gdlAction, boolean forMenu) {
        List<Action> actions = getAntlrDebugActions();
        List<JMenuItem> list = new ArrayList<>(actions.size());

        Lookup lookup;

        if (gdlAction instanceof Lookup.Provider) {
            lookup = ((Lookup.Provider) gdlAction).getLookup();
        } else {
            lookup = null;
        }

        for (Action a : actions) {

            // Retrieve context sensitive action instance if possible.
            if (lookup != null && a instanceof ContextAwareAction) {
                a = ((ContextAwareAction) a).createContextAwareInstance(lookup);
            }

            if (a != null && a.isEnabled()) {
                JMenuItem mi;
                mi = new JMenuItem();
                Actions.connect(mi, a, !forMenu);
                list.add(mi);
            }
        }

        return list;
    }

    List<Action> getAntlrDebugActions() {

        List<Action> arr = new ArrayList<>();
        if (node != null) {
            arr.add(new ViewAntlrLexerTokensAction(node));
            arr.add(new ViewAntlrColoringTokensAction(node));
            arr.add(new ViewAntlrFormatterTokensAction(node));
        }
//        List<? extends Action> actions = Utilities.actionsForPath("Actions/AntlrDebugActions");
//        arr.addAll(actions);
        //add the actions 
        return arr;
    }

    private final class Popup extends JMenuItem implements DynamicMenuContent {

        private final JMenu menu = new MyMenu();
        private JPopupMenu lastPopup = null;
        /**
         * Associated tools action.
         */
        private final Action antlrDebugAction;

        public Popup(Action antlrDebugAction) {
            super();
            this.antlrDebugAction = antlrDebugAction;
            HelpCtx.setHelpIDString(menu, AntlrDebugAction.class.getName());

        }

        @Override
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(new JComponent[0]);
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] jcs) {
            return new JMenuItem[]{menu};
        }

        private class MyMenu extends org.openide.awt.JMenuPlus implements PopupMenuListener {

            MyMenu() {
                super("Antlr Debug");
            }

            @Override
            public JPopupMenu getPopupMenu() {
                JPopupMenu popup = super.getPopupMenu();
                fillSubmenu(popup);

                return popup;
            }

            private void fillSubmenu(JPopupMenu pop) {
                if (lastPopup != null) {
                    return;
                }
                pop.addPopupMenuListener(this);
                lastPopup = pop;

                removeAll();
                Iterator<JMenuItem> it = generate(antlrDebugAction, false).iterator();

                while (it.hasNext()) {
                    java.awt.Component item = (java.awt.Component) it.next();

                    if (item == null) {
                        addSeparator();
                    } else {
                        add(item);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        }
    }

}
