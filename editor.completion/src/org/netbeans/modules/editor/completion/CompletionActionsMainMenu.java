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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.completion;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.MainMenuAction;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public abstract class CompletionActionsMainMenu extends MainMenuAction implements Action {

    private AbstractAction delegate;
        
    public CompletionActionsMainMenu() {
        super();
        delegate = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // Does nothing;
            }
        };
        putValue(NAME, getActionName());
        setMenu();
    }
    
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }

    public void putValue(String key, Object newValue) {
        delegate.putValue(key, newValue);
    }

    public Object getValue(String key) {
        return delegate.getValue(key);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    public void setEnabled(boolean newValue) {
        delegate.setEnabled(newValue);
    }

    public @Override boolean isEnabled() {
        return delegate.isEnabled();
    }

    /** Sets the state of JMenuItem*/
    protected @Override void setMenu(){
        
        ActionMap am = getContextActionMap();
        Action action = null;
        if (am != null) {
            action = am.get(getActionName());
        }
        
        JMenuItem presenter = getMenuPresenter();
        Action presenterAction = presenter.getAction();
        if (presenterAction == null){
            presenter.setAction(this);
            presenter.setToolTipText(null); /* bugfix #62872 */ 
            menuInitialized = false;
        } 
        else {
            if (!this.equals(presenterAction)){
                presenter.setAction(this);
                presenter.setToolTipText(null); /* bugfix #62872 */
                menuInitialized = false;
            }
        }

        if (!menuInitialized){
            Mnemonics.setLocalizedText(presenter, getMenuItemText());
            menuInitialized = true;
        }

        presenter.setEnabled(action != null);
        JTextComponent comp = Utilities.getFocusedComponent();
        if (comp != null && comp instanceof JEditorPane){
            addAccelerators(this, presenter, comp);
        } else {
            presenter.setAccelerator(getDefaultAccelerator());
        }

    }
    
    
    public static final class CompletionShow extends CompletionActionsMainMenu {


        protected String getMenuItemText() {
            return NbBundle.getBundle(CompletionActionsMainMenu.class).getString(ExtKit.completionShowAction + "-main_menu_item"); //NOI18N
        }

        
        protected String getActionName() {
            return ExtKit.completionShowAction;
        }

        public void actionPerformed(ActionEvent e) {
            Completion.get().showCompletion();
        }
        
        
        
        
    } 
    
    public static final class DocumentationShow extends CompletionActionsMainMenu {

        protected String getMenuItemText() {
            return NbBundle.getBundle(CompletionActionsMainMenu.class).getString(ExtKit.documentationShowAction + "-main_menu_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.documentationShowAction;
        }

        public void actionPerformed(ActionEvent e) {
            Completion.get().showDocumentation();
        }
                
    }
    
    public static final class ToolTipShow extends CompletionActionsMainMenu {

        protected String getMenuItemText() {
            return NbBundle.getBundle(CompletionActionsMainMenu.class).getString(ExtKit.completionTooltipShowAction + "-main_menu_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.completionTooltipShowAction;
        }

        public void actionPerformed(ActionEvent e) {
            Completion.get().showToolTip();
        }
                
    }
    
}
