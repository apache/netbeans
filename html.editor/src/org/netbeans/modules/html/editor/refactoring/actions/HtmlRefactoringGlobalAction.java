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
package org.netbeans.modules.html.editor.refactoring.actions;

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
public abstract class HtmlRefactoringGlobalAction extends NodeAction {

    /** Creates a new JavaRefactoringGlobalActiongGlobalAction */
    public HtmlRefactoringGlobalAction(String name, Icon icon) {
        setName(name);
        setIcon(icon);
    }
    
    public final String getName() {
        return (String) getValue(Action.NAME);
    }
    
    protected void setName(String name) {
        putValue(Action.NAME, name);
    }
    
    protected void setMnemonic(char m) {
        putValue(Action.MNEMONIC_KEY, new Integer(m));
    }
    
    private static String trim(String arg) {
        arg = arg.replace("&", ""); // NOI18N
        return arg.replace("...", ""); // NOI18N
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected Lookup getLookup(Node[] n) {
        InstanceContent ic = new InstanceContent();
        for (Node node:n)
            ic.add(node);
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
    
    public final void performAction(final Node[] activatedNodes) {
        performAction(getLookup(activatedNodes));
    }

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
        
        public Object getValue(String arg0) {
            return HtmlRefactoringGlobalAction.this.getValue(arg0);
        }
        
        public void putValue(String arg0, Object arg1) {
            HtmlRefactoringGlobalAction.this.putValue(arg0, arg1);
        }
        
        public void setEnabled(boolean arg0) {
            HtmlRefactoringGlobalAction.this.setEnabled(arg0);
        }
        
        public boolean isEnabled() {
            return enable(context);
        }
        
        public void addPropertyChangeListener(PropertyChangeListener arg0) {
            HtmlRefactoringGlobalAction.this.addPropertyChangeListener(arg0);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener arg0) {
            HtmlRefactoringGlobalAction.this.removePropertyChangeListener(arg0);
        }
        
        public void actionPerformed(ActionEvent arg0) {
            HtmlRefactoringGlobalAction.this.performAction(context);
        }
        public JMenuItem getMenuPresenter() {
            if (isMethodOverridden(HtmlRefactoringGlobalAction.this, "getMenuPresenter")) { // NOI18N

                return HtmlRefactoringGlobalAction.this.getMenuPresenter();
            } else {
                return new Actions.MenuItem(this, true);
            }
        }

        public JMenuItem getPopupPresenter() {
            if (isMethodOverridden(HtmlRefactoringGlobalAction.this, "getPopupPresenter")) { // NOI18N

                return HtmlRefactoringGlobalAction.this.getPopupPresenter();
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        public Component getToolbarPresenter() {
            if (isMethodOverridden(HtmlRefactoringGlobalAction.this, "getToolbarPresenter")) { // NOI18N

                return HtmlRefactoringGlobalAction.this.getToolbarPresenter();
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
