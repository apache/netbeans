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

package org.netbeans.modules.cnd.completion.cplusplus;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.utils.MIMENames;

/**
 *
 */
@EditorActionRegistrations({
    @EditorActionRegistration(
    name = "goto-implementation",// NOI18N
    popupPath = "goto", // NOI18N
    popupText = "#goto-implementation-text", // NOI18N
    mimeType = MIMENames.CPLUSPLUS_MIME_TYPE
    ),
    @EditorActionRegistration(
    name = "goto-implementation",// NOI18N
    popupPath = "goto", // NOI18N
    popupText = "#goto-implementation-text", // NOI18N
    mimeType = MIMENames.C_MIME_TYPE
    ),
    @EditorActionRegistration(
    name = "goto-implementation",// NOI18N
    popupPath = "goto", // NOI18N
    popupText = "#goto-implementation-text", // NOI18N
    mimeType = MIMENames.HEADER_MIME_TYPE
    )
})
public class CCGoToImplementationAction extends BaseAction {

    static final long serialVersionUID = 1L;

    public CCGoToImplementationAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        final JTextComponent target = getFocusedComponent();
        if (target != null && (target.getDocument() instanceof BaseDocument)) {
            BaseDocument doc = (BaseDocument) target.getDocument();
            int offset = target.getSelectionStart();
            // first try include provider
            if (new CsmHyperlinkProvider().isHyperlinkPoint(doc, offset, HyperlinkType.GO_TO_DECLARATION)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchonous() {
        return false;
    }

    public boolean gotoImplementation(final JTextComponent target) {
        final String taskName = "Go to implementation"; //NOI18N
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (target != null && (target.getDocument() instanceof BaseDocument)) {
                    BaseDocument doc = (BaseDocument) target.getDocument();
                    int offset = target.getSelectionStart();
                    new CsmHyperlinkProvider().goToDeclaration(doc, target, offset, HyperlinkType.ALT_HYPERLINK);
                }
            }
        };
        CsmModelAccessor.getModel().enqueue(run, taskName);
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        gotoImplementation(target);
    }
}
