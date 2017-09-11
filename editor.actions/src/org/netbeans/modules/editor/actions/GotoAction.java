/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
