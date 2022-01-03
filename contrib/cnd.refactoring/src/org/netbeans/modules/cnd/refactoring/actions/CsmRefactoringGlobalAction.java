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
package org.netbeans.modules.cnd.refactoring.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Hashtable;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * 
 * CsmRefactoringGlobalAction
 * This class is copy of RefactoringGlobalAction, which is not in public packages
 */
public abstract class CsmRefactoringGlobalAction extends NodeAction {

    /** Property identifier for menu text, neccessary for display in menu */
    protected static final String MENU_TEXT = "menuText"; //NOI18N
    /** Property identifier for popup textm, neccessary for display popup */
    protected static final String POPUP_TEXT = "popupText"; //NOI18N

    /** Creates a new JavaRefactoringGlobalActiongGlobalAction */
    public CsmRefactoringGlobalAction(String name, Icon icon) {
        setName(name);
        setIcon(icon);
    }

    @Override
    public final String getName() {
        return (String) getValue(Action.NAME);
    }

    protected final void setName(String name) {
        putValue(Action.NAME, name);
    }

    protected void setMnemonic(char m) {
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(m));
    }

    private static String trim(String arg) {//XXX unused
        arg = arg.replace("&", ""); // NOI18N
        return arg.replace("...", ""); // NOI18N
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected Lookup getLookup(Node[] n) {
        InstanceContent ic = new InstanceContent();
        for (Node node : n) {
            ic.add(node);
        }
        if (n.length > 0) {
            EditorCookie tc = getTextComponent(n[0]);
            if (tc != null) {
                ic.add(tc);
            }
        }
        ic.add(new Hashtable(0));
        return new AbstractLookup(ic);
    }

    protected static EditorCookie getTextComponent(Node n) {
        DataObject dobj = n.getLookup().lookup(DataObject.class);
        if (dobj != null) {
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                TopComponent activetc = TopComponent.getRegistry().getActivated();
                if (activetc instanceof Pane) {
                    return ec;
                }
            }
        }
        return null;
    }

    public abstract void performAction(Lookup context);

    protected boolean enable(Lookup context) {
        return true;
    }
    
    protected abstract boolean applicable(Lookup context);

    @Override
    public final void performAction(final Node[] activatedNodes) {
        performAction(getLookup(activatedNodes));
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return enable(getLookup(activatedNodes));
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext);
    }

    private class ContextAction implements Action, Presenter.Menu, Presenter.Popup {

        Lookup context;

        public ContextAction(Lookup context) {
            this.context = context;
        }

        @Override
        public Object getValue(String arg0) {
            if ("applicable".equals(arg0)) { //NOI18N
                return CsmRefactoringGlobalAction.this.applicable(context);
            }
            return CsmRefactoringGlobalAction.this.getValue(arg0);
        }

        @Override
        public void putValue(String arg0, Object arg1) {
            CsmRefactoringGlobalAction.this.putValue(arg0, arg1);
        }

        @Override
        public void setEnabled(boolean arg0) {
            CsmRefactoringGlobalAction.this.setEnabled(arg0);
        }

        @Override
        public boolean isEnabled() {
            return enable(context);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener arg0) {
            CsmRefactoringGlobalAction.this.addPropertyChangeListener(arg0);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener arg0) {
            CsmRefactoringGlobalAction.this.removePropertyChangeListener(arg0);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            CsmRefactoringGlobalAction.this.performAction(context);
        }

        @Override
        public JMenuItem getMenuPresenter() {
            if (isMethodOverridden(CsmRefactoringGlobalAction.this, "getMenuPresenter")) { // NOI18N

                return CsmRefactoringGlobalAction.this.getMenuPresenter();
            } else {
                return new Actions.MenuItem(this, true);
            }
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (isMethodOverridden(CsmRefactoringGlobalAction.this, "getPopupPresenter")) { // NOI18N

                return CsmRefactoringGlobalAction.this.getPopupPresenter();
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        private boolean isMethodOverridden(NodeAction d, String name) {
            try {
                Method m = d.getClass().getMethod(name, new Class[0]);

                return m.getDeclaringClass() != CallableSystemAction.class;
            } catch (java.lang.NoSuchMethodException ex) {
                ex.printStackTrace(System.err);
                throw new IllegalStateException("Error searching for method " + name + " in " + d); // NOI18N
            }
        }
    }
}
