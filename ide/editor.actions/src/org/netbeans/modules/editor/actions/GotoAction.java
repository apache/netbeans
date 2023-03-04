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

package org.netbeans.modules.editor.actions;

import org.netbeans.spi.editor.AbstractEditorAction;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.JumpList;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.openide.util.Lookup;

/**
 * Toggle toolbar/lines visibility.
 *
 * @author Miloslav Metelka
 * @since 1.13
 */

@EditorActionRegistrations({
    @EditorActionRegistration(
        name = EditorActionNames.gotoDeclaration,
        menuPath = "GoTo",
        menuPosition = 900,
        menuText = "#" + EditorActionNames.gotoDeclaration + "_menu_text"
    )
})
public final class GotoAction extends AbstractEditorAction {

    // -J-Dorg.netbeans.modules.editor.actions.GotoAction.level=FINEST
    private static final Logger LOG = Logger.getLogger(GotoAction.class.getName());
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        String actionName = actionName();
        if (EditorActionNames.gotoDeclaration.equals(actionName)) {
            resetCaretMagicPosition(target);
            if (target != null) {
                if (hyperlinkGoTo(target)) {
                    return;
                }
                
                BaseDocument doc = Utilities.getDocument(target);
                if (doc != null) {
                    try {
                        Caret caret = target.getCaret();
                        int dotPos = caret.getDot();
                        int[] idBlk = Utilities.getIdentifierBlock(doc, dotPos);
                        ExtSyntaxSupport extSup = (ExtSyntaxSupport) doc.getSyntaxSupport();
                        if (idBlk != null) {
                            int decPos = extSup.findDeclarationPosition(doc.getText(idBlk), idBlk[1]);
                            if (decPos >= 0) {
                                caret.setDot(decPos);
                            }
                        }
                    } catch (BadLocationException e) {
                    }
                }
            }
        }
    }
    
    private boolean hyperlinkGoTo(JTextComponent component) {
        Document doc = component.getDocument();
        Object mimeTypeObj = doc.getProperty(BaseDocument.MIME_TYPE_PROP);  //NOI18N
        String mimeType;
        
        if (mimeTypeObj instanceof String)
            mimeType = (String) mimeTypeObj;
        else {
            return false;
        }
        
        int position = component.getCaretPosition();
        Lookup lookup = MimeLookup.getLookup(mimeType);
        
        for (HyperlinkProviderExt provider : lookup.lookupAll(HyperlinkProviderExt.class)) {
            if (provider.getSupportedHyperlinkTypes().contains(HyperlinkType.GO_TO_DECLARATION) && provider.isHyperlinkPoint(doc, position, HyperlinkType.GO_TO_DECLARATION)) {
                //make sure the JumpList works:
                JumpList.checkAddEntry(component, position);
                
                provider.performClickAction(doc, position, HyperlinkType.GO_TO_DECLARATION);
                return true;
            }
        }
        
        for (final HyperlinkProvider provider : lookup.lookupAll(HyperlinkProvider.class)) {
            if (provider.isHyperlinkPoint(doc, position)) {
                //make sure the JumpList works:
                JumpList.checkAddEntry(component, position);
                
                provider.performClickAction(doc, position);
                return true;
            }
        }
        
        return false;
    }

}
