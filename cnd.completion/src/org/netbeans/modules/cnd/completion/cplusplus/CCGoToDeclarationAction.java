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
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.LineHyperlinkProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.NbBundle;

/**
 * Open CC source according to the given expression.
 *
 * @version 1.0
 */
@EditorActionRegistrations({
    @EditorActionRegistration(
        name = "goto-declaration", // NOI18N
        menuPath = "GoTo", // NOI18N
//        menuPosition = 900, Temporary fix
        menuText = "#goto-identifier-declaration", // NOI18N
        mimeType = MIMENames.C_MIME_TYPE
    ),
    @EditorActionRegistration(
        name = "goto-declaration", // NOI18N
        menuPath = "GoTo", // NOI18N
//        menuPosition = 900, Temporary fix
        menuText = "#goto-identifier-declaration", // NOI18N
        mimeType = MIMENames.CPLUSPLUS_MIME_TYPE
    ),
    @EditorActionRegistration(
        name = "goto-declaration", // NOI18N
        menuPath = "GoTo", // NOI18N
//        menuPosition = 900, Temporary fix
        menuText = "#goto-identifier-declaration", // NOI18N
        mimeType = MIMENames.HEADER_MIME_TYPE
    )
})
public class CCGoToDeclarationAction extends BaseAction {

    static final long serialVersionUID = 1L;

    public CCGoToDeclarationAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getBundle(CCGoToDeclarationAction.class).getString("NAME_GoToDeclarationAction"); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        /* If there are no paths in registry, the action shoud be disabled (#46632)*/
        //        Set sources = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        //        Set compile = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        //        Set boot = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
        //        return !(sources.isEmpty() && compile.isEmpty() && boot.isEmpty());
        final JTextComponent target = getFocusedComponent();
        if (target != null && (target.getDocument() instanceof BaseDocument)) {
            BaseDocument doc = (BaseDocument) target.getDocument();
            int offset = target.getSelectionStart();
            // first try include provider
            if (new CsmIncludeHyperlinkProvider().isHyperlinkPoint(doc, offset, HyperlinkType.GO_TO_DECLARATION)) {
                return true;
            } else if (new CsmHyperlinkProvider().isHyperlinkPoint(doc, offset, HyperlinkType.GO_TO_DECLARATION)) {
                return true;
            } else if (new LineHyperlinkProvider().isHyperlinkPoint(doc, offset, HyperlinkType.GO_TO_DECLARATION)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchonous() {
        return false;
    }

    public boolean gotoDeclaration(final JTextComponent target) {
        final String taskName = "Go to declaration"; //NOI18N
        Runnable run = new Runnable() {

            @Override
            public void run() {
                if (target != null && (target.getDocument() instanceof BaseDocument)) {
                    BaseDocument doc = (BaseDocument) target.getDocument();
                    int offset = target.getSelectionStart();
                    // first try include provider
                    if (!new CsmIncludeHyperlinkProvider().goToInclude(doc, target, offset, HyperlinkType.GO_TO_DECLARATION)) {
                        if (!new LineHyperlinkProvider().goToLine(doc, target, offset, HyperlinkType.GO_TO_DECLARATION)) {
                            // if failed => try identifier provider
                            new CsmHyperlinkProvider().goToDeclaration(doc, target, offset, HyperlinkType.GO_TO_DECLARATION);
                        }
                    }
                }
            }
        };
        //RequestProcessor.getDefault().post(run);
        CsmModelAccessor.getModel().enqueue(run, taskName);
        return false;
    }

    @Override
    public String getPopupMenuText(JTextComponent target) {
        String retValue;

        retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-identifier-declaration");
        if (target != null && (target.getDocument() instanceof BaseDocument)) {
            BaseDocument doc = (BaseDocument) target.getDocument();
            int offset = target.getSelectionStart();
            // don't need to lock document because we are in EQ
            TokenItem<TokenId> token = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            if (token != null) {
                if (CsmIncludeHyperlinkProvider.isSupportedToken(token, HyperlinkType.GO_TO_DECLARATION)) {
                    retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-included-file");
//                } else if (CsmHyperlinkProvider.isSupportedToken(token)) {
//                    // check if next token is '(' => it's possible to be functon
//                    Token next = TokenUtilities.getToken(doc, token.getEndOffset());
//                    if (next != null && next.getTokenID() == CCTokenContext.WHITESPACE) {
//                        // try next one
//                        next = TokenUtilities.getToken(doc, next.getEndOffset());
//                    }
//                    if (next != null && next.getTokenID() == CCTokenContext.LPAREN) {
//                        // this is function call or function definition or function declaration
//                        retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-definition-declaration");
//                    }
                }
            }
        }
        return retValue;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        gotoDeclaration(target);
    }
}
