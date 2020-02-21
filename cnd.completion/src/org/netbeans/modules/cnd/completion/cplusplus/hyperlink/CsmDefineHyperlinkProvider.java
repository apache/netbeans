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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
