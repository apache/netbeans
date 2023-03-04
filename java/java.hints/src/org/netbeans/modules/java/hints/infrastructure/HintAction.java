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
package org.netbeans.modules.java.hints.infrastructure;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

public abstract class HintAction extends TextAction implements PropertyChangeListener {
    
    protected HintAction() {
        this(null);
    }

    protected HintAction(String key) {
        super(key);
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        
        EditorRegistry.addPropertyChangeListener(WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        
        setEnabled(false);
        updateEnabled();
    }
    
    private void updateEnabled() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doUpdateEnabled();
                }
            });
        } else {
            doUpdateEnabled();
        }
    }

    private void doUpdateEnabled() {
        setEnabled(getCurrentFile(null) != null);
    }

    public void actionPerformed(ActionEvent e) {
        String error = doPerform();
        
        if (error != null) {
            String errorText = NbBundle.getMessage(HintAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }
    
    private String doPerform() {
        int[] span = new int[2];
        JTextComponent pane = getCurrentFile(span);
        Document doc = pane != null ? pane.getDocument() : null;
        
        if (doc == null) {
            if (span[0] != span[1])
                return "ERR_Not_Selected"; //NOI18N
            else
                return "ERR_No_Selection"; //NOI18N
        }
        
        JavaSource js = JavaSource.forDocument(doc);
        
        if (js == null)
            return  "ERR_Not_Supported"; //NOI18N
        
        perform(js, pane, span);
        
        return null;
    }
    
    protected abstract void perform(JavaSource js, JTextComponent pane, int[] selection);

    private JTextComponent getCurrentFile(int[] span) {
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        
        if (pane == null) return null;
        
        Document doc = pane.getDocument();
        Object stream = doc != null ? doc.getProperty(Document.StreamDescriptionProperty) : null;
        
        if (!(stream instanceof DataObject))
            return null;
        
        if (!"text/x-java".equals(NbEditorUtilities.getMimeType(doc))) //NOI18N
            return null;
        
        TopComponent tc = TopComponent.getRegistry().getActivated();
        Component c = pane;
        
        while (c != null) {
            if (c == tc) break;
            c = c.getParent();
        }
        
        if (c == null) return null;
        
        if (span != null) {
            span[0] = pane.getSelectionStart();
            span[1] = pane.getSelectionEnd();
            
            if (span[0] == span[1] && requiresSelection())
                return null;
        }
        
        return pane;
    }
    
    protected boolean requiresSelection() {
        return true;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        updateEnabled();
    }

}
