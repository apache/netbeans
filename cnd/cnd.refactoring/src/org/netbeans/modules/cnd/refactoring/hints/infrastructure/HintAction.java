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
package org.netbeans.modules.cnd.refactoring.hints.infrastructure;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * based on org.netbeans.modules.java.hints.infrastructure.HintAction
 */
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
        setEnabled(getCurrentDocument(new int[] {0,0,0}) != null);
    }

    @Override
    public boolean isEnabled() {
        updateEnabled();
        return super.isEnabled();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String error = doPerform();

        if (error != null) {
            String errorText = NbBundle.getMessage(HintAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    private String doPerform() {
        int[] span = new int[3];
        Document doc = getCurrentDocument(span);

        if (doc == null) {
            if (span[0] != span[1]) {
                return "ERR_Not_Selected"; //NOI18N
            } else {
                return "ERR_No_Selection"; //NOI18N
            }
        }

        CsmContext editorContext = CsmContext.create(doc, span[0], span[1], span[2]);
        if (editorContext == null) {
            return "ERR_Not_Supported"; //NOI18N
        }
        perform(editorContext);

        return null;
    }

    protected abstract void perform(CsmContext context);

    private Document getCurrentDocument(int[] span) {
        JTextComponent pane = EditorRegistry.lastFocusedComponent();

        if (pane == null) {
            return null;
        }
        if (span != null) {
            if (pane.getCaret() == null) {
                return null;
            }
            span[0] = pane.getSelectionStart();
            span[1] = pane.getSelectionEnd();
            span[2] = pane.getCaretPosition();
            if (span[0] == span[1] && requiresSelection()) {
                return null;
            }
        }

        Document doc = pane.getDocument();
        Object stream = doc != null ? doc.getProperty(Document.StreamDescriptionProperty) : null;

        if (!(stream instanceof DataObject)) {
            return null;
        }

        DataObject dObj = (DataObject) stream;
        FileObject result = dObj.getPrimaryFile();

        if (MIMENames.isHeaderOrCppOrC(FileUtil.getMIMEType(result))) {
            return doc;
        } else {
            return null;
        }
    }

    protected boolean requiresSelection() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateEnabled();
    }
}
