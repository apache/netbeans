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
package org.netbeans.modules.cnd.navigation.macroview.impl.services;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.navigation.hierarchy.ContextUtils;
import org.netbeans.modules.cnd.navigation.macroview.MacroExpansionTopComponent;
import org.netbeans.modules.cnd.navigation.macroview.MacroExpansionViewUtils;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionViewProvider;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Service that provides UI for macro expansion.
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionViewProvider.class)
public class MacroExpansionViewProviderImpl implements CsmMacroExpansionViewProvider {
    
    private static final RequestProcessor RP = new RequestProcessor("MacroExpansionViewProviderImpl", 1); // NOI18N

    /**
     * Expands document on specified position and shows Macro Expansion View panel.
     *
     * @param doc - document
     * @param offset - offset in document
     */
    @Override
    public void showMacroExpansionView(Document doc, final int offset) {
        final Document mainDoc = doc;
        if (mainDoc == null) {
            return;
        }
        
        // Create view
        Runnable createExpansionView = new Runnable() {

            @Override
            public void run() {
                final CsmFile csmFile = CsmUtilities.getCsmFile(mainDoc, true, false);
                if (csmFile == null) {
                    return;
                }

                // Get ofsets
                int startOffset = 0;
                int endOffset = mainDoc.getLength();
                if (MacroExpansionTopComponent.isLocalContext()) {
                    CsmScope scope = ContextUtils.findInnerFileScope(csmFile, offset);
                    if (CsmKindUtilities.isOffsetable(scope)) {
                        startOffset = ((CsmOffsetable) scope).getStartOffset();
                        endOffset = ((CsmOffsetable) scope).getEndOffset();
                    }
                }

                // Init expanded context field
                final Document expandedContextDoc = MacroExpansionViewUtils.createExpandedContextDocument(mainDoc, csmFile);
                if (expandedContextDoc == null) {
                    return;
                }
                final int expansionsNumber = CsmMacroExpansion.expand(mainDoc, startOffset, endOffset, expandedContextDoc, new AtomicBoolean(false));
                MacroExpansionViewUtils.setOffset(expandedContextDoc, startOffset, endOffset);
                MacroExpansionViewUtils.saveDocumentAndMarkAsReadOnly(expandedContextDoc);

                // Open view
                Runnable openView = new Runnable() {

                    @Override
                    public void run() {
                        MacroExpansionTopComponent view = MacroExpansionTopComponent.findInstance();
                        if (!view.isOpened()) {
                            view.open();
                        }
                        view.setDocuments(expandedContextDoc);
                        view.requestActive();
                        view.setDisplayName(NbBundle.getMessage(MacroExpansionTopComponent.class, "CTL_MacroExpansionViewTitle", MacroExpansionViewUtils.getDocumentName(mainDoc))); // NOI18N
                        view.setStatusBarText(NbBundle.getMessage(MacroExpansionTopComponent.class, "CTL_MacroExpansionStatusBarLine", expansionsNumber)); // NOI18N
                        int offset2 = MacroExpansionViewUtils.getDocumentOffset(expandedContextDoc, MacroExpansionViewUtils.getFileOffset(mainDoc, offset));
                        for(JTextComponent comp : EditorRegistry.componentList()) {
                            if (expandedContextDoc.equals(comp.getDocument())) {
                                int length = comp.getDocument().getLength();
                                if (offset2 > 0 && offset2 < length) {
                                    comp.setCaretPosition(offset2);
                                } else if (offset2 >= length && length > 0) {
                                    comp.setCaretPosition(length - 1);
                                } else {
                                    comp.setCaretPosition(0);
                                }
                                break;
                            }
                        }
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    openView.run();
                } else {
                    SwingUtilities.invokeLater(openView);
                }
            }
        };
        RP.execute(createExpansionView);
    }
}
