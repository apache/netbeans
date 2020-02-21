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
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
