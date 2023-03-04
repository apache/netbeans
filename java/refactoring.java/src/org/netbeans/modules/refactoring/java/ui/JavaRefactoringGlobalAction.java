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
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Hashtable;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
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
 * JavaRefactoringGlobalAction
 * This class is copy of RefactoringGlobalAction, which is not in public packages
 * @author Jan Becicka
 */
public abstract class JavaRefactoringGlobalAction extends NodeAction {

    /** Creates a new JavaRefactoringGlobalActiongGlobalAction */
    public JavaRefactoringGlobalAction(String name, Icon icon) {
        setName(name);
        setIcon(icon);
    }
    
    @Override
    public final String getName() {
        return (String) getValue(Action.NAME);
    }
    
    protected void setName(String name) {
        putValue(Action.NAME, name);
    }
    
    protected void setMnemonic(char m) {
        putValue(Action.MNEMONIC_KEY, m);
    }
    
    private static String trim(String arg) {
        arg = arg.replace("&", ""); // NOI18N
        return arg.replace("...", ""); // NOI18N
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected Lookup getLookup(Node[] n) {
        InstanceContent ic = new InstanceContent();
        for (Node node:n) {
            ic.add(node);
        }
        if (n.length>0) {
            EditorCookie tc = getTextComponent(n[0]);
            if (tc != null) {
                ic.add(tc);
            }
        }
        ic.add(new Hashtable(0));
        return new AbstractLookup(ic);
    }

    
    protected static EditorCookie getTextComponent(Node n) {
        DataObject dobj = n.getCookie(DataObject.class);
        if (dobj != null) {
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
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
    
    protected abstract boolean enable(Lookup context);
    
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
    
    public class ContextAction implements Action, Presenter.Menu, Presenter.Popup, Presenter.Toolbar {

        Lookup context;

        public ContextAction(Lookup context) {
            this.context=context;
        }
        
        @Override
        public Object getValue(String arg0) {
            return JavaRefactoringGlobalAction.this.getValue(arg0);
        }
        
        @Override
        public void putValue(String arg0, Object arg1) {
            JavaRefactoringGlobalAction.this.putValue(arg0, arg1);
        }
        
        @Override
        public void setEnabled(boolean arg0) {
            JavaRefactoringGlobalAction.this.setEnabled(arg0);
        }
        
        @Override
        public boolean isEnabled() {
            return enable(context);
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener arg0) {
            JavaRefactoringGlobalAction.this.addPropertyChangeListener(arg0);
        }
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener arg0) {
            JavaRefactoringGlobalAction.this.removePropertyChangeListener(arg0);
        }
        
        @Override
        public void actionPerformed(ActionEvent arg0) {
            JavaRefactoringGlobalAction.this.performAction(context);
        }
        @Override
        public JMenuItem getMenuPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getMenuPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getMenuPresenter();
            } else {
                return new Actions.MenuItem(this, true);
            }
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getPopupPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getPopupPresenter();
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        @Override
        public Component getToolbarPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getToolbarPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getToolbarPresenter();
            } else {
                final JButton button = new JButton();
                Actions.connect(button, this);
                return button;
            }
        }

        private boolean isMethodOverridden(NodeAction d, String name) {
            try {
                Method m = d.getClass().getMethod(name, new Class[0]);

                return m.getDeclaringClass() != CallableSystemAction.class;
            } catch (java.lang.NoSuchMethodException ex) {
                ex.printStackTrace();
                throw new IllegalStateException("Error searching for method " + name + " in " + d); // NOI18N
            }
        }        
    }
}
