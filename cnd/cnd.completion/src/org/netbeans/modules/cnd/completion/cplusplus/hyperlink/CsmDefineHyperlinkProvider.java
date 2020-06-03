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
package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.util.NbBundle;

/**
 * Implementation of the hyperlink provider for #define preprocessor directives.
 * <br>
 * The hyperlinks are constructed for identifiers.
 * <br>
 * The click action corresponds to performing the goto-declaration action.
 *
 */
public final class CsmDefineHyperlinkProvider extends CsmHyperlinkProvider {

    public CsmDefineHyperlinkProvider() {
    }

    @Override
    protected void performAction(final Document doc, final JTextComponent target, final int offset, final HyperlinkType type) {
        goToDeclaration(doc, target, offset, type);
    }

    @Override
    protected boolean isValidToken(TokenItem<TokenId> token, HyperlinkType type) {
        return isSupportedToken(token, type);
    }

    public static boolean isSupportedToken(TokenItem<TokenId> token, HyperlinkType type) {
        if (token != null) {
            if (type == HyperlinkType.ALT_HYPERLINK) {
                if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory()) ||
                        CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                    return false;
                }
            }
            if(token.id() instanceof CppTokenId) {
                switch ((CppTokenId)token.id()) {
                    case PREPROCESSOR_IDENTIFIER:
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean goToDeclaration(Document doc, JTextComponent target, int offset, HyperlinkType type) {
        if (!preJump(doc, target, offset, "opening-csm-element", type)) { //NOI18N
            return false;
        }
        try {
            CsmCacheManager.enter();
            TokenItem<TokenId> jumpToken = getJumpToken();
            CsmFile csmFile = CsmUtilities.getCsmFile(doc, true, false);
            CsmObject primary = findTargetObject(doc, jumpToken, offset, false);
            CsmOffsetable item = toJumpObject(primary, csmFile, offset);
            UIGesturesSupport.submit("USG_CND_HYPERLINK", type); //NOI18N
            return postJump(item, "goto_source_source_not_found", "cannot-open-csm-element"); //NOI18N
        } finally {
            CsmCacheManager.leave();
        }
    }

    @Override
    /*package*/ CsmObject findTargetObject(final Document doc, final TokenItem<TokenId> jumpToken, final int offset, boolean toOffsetable) {
        assert jumpToken != null;
        CsmFile file = CsmUtilities.getCsmFile(doc, true, false);
        CsmObject csmObject = file == null ? null : ReferencesSupport.findDefine(doc, file, jumpToken, offset);
        if (csmObject != null) {
            // convert to jump object
            return toOffsetable ? toJumpObject(csmObject, file, offset) : csmObject;
        }
        return super.findTargetObject(doc, jumpToken, offset, toOffsetable);
    }

    @Override
    protected String getTooltipText(Document doc, TokenItem<TokenId> token, int offset, HyperlinkType type) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true, false);
        if(file != null) {
            CsmObject item = ReferencesSupport.findDefine(doc, file, token, offset);
            if (item != null) {
                return NbBundle.getMessage(CsmDefineHyperlinkProvider.class, "DSC_MacroParameterTooltip", ((CsmOffsetable)item).getText());
            }
        }
        return super.getTooltipText(doc, token, offset, type);
    }
}
