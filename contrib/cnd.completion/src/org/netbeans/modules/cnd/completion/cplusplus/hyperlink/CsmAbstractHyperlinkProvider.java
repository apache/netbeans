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

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 * base hyperlink provider for Csm elements
 */
public abstract class CsmAbstractHyperlinkProvider implements HyperlinkProviderExt {

    private TokenItem<TokenId> jumpToken = null;
    private Cancellable hyperLinkTask;

    protected CsmAbstractHyperlinkProvider() {
        DefaultCaret caret = new DefaultCaret();
        caret.setMagicCaretPosition(null);
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION, HyperlinkType.ALT_HYPERLINK);
    }

    protected abstract void performAction(final Document originalDoc, final JTextComponent target, final int offset, final HyperlinkType type);

    @Override
    public void performClickAction(Document originalDoc, final int offset, final HyperlinkType type) {
        if (originalDoc == null) {
            return;
        }

        final Document doc = originalDoc;
        final JTextComponent target = Utilities.getFocusedComponent();

        if (target == null || target.getDocument() != doc) {
            return;
        }

        Runnable run = new Runnable() {

            @Override
            public void run() {
                int[] span = CsmMacroExpansion.getMacroExpansionSpan(doc, offset, false);
                if (type == HyperlinkType.ALT_HYPERLINK && (span != null && span[0] != span[1])) {
                    // in this mode we open MacroView
                    CsmMacroExpansion.showMacroExpansionView(doc, offset);
                } else {
                    performAction(doc, target, offset, type);
                }
            }
        };
        if (hyperLinkTask != null) {
            hyperLinkTask.cancel();
        }
        hyperLinkTask = CsmModelAccessor.getModel().enqueue(run, "Following hyperlink");// NOI18N
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        TokenItem<TokenId> token = getToken(doc, offset);
        return isValidToken(token, type);
    }

    protected abstract boolean isValidToken(TokenItem<TokenId> token, HyperlinkType type);

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        TokenItem<TokenId> token = getToken(doc, offset);
        if (type == HyperlinkType.ALT_HYPERLINK) {
            int[] span = CsmMacroExpansion.getMacroExpansionSpan(doc, offset, false);
            if (span != null && span[0] != span[1]) {
                return span;
            }
        }
        if (isValidToken(token, type)) {
            jumpToken = token;
            return new int[]{token.offset(), token.offset() + token.length()};
        } else {
            return null;
        }
    }

    protected boolean preJump(Document doc, JTextComponent target, int offset, String msgKey, HyperlinkType type) {
        if (doc == null || target == null || offset < 0 || offset > doc.getLength()) {
            return false;
        }
        jumpToken = getToken(doc, offset);
        if (!isValidToken(jumpToken, type)) {
            return false;
        }
        //String name = jumpToken.text().toString();
        StringBuilder buf = new StringBuilder(jumpToken.text());
        String name = buf.toString();
        String msg = NbBundle.getBundle(CsmCompletionUtils.class).getString(msgKey); //NOI18N
        msg = MessageFormat.format(msg, new Object[]{name});
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);//NOI18N
        return true;
    }

    protected boolean postJump(CsmOffsetable item, String existItemKey, String noItemKey) {
        if (jumpToken == null) {
            return false;
        }
        if (item == null || !CsmUtilities.openSource(item)) {
            //nothing found (item == null) or no source found (itemDesc != null)
            //inform user, that we were not able to open the resource.
            Toolkit.getDefaultToolkit().beep();
            String key;
            String name;
            String itemDesc = CsmUtilities.getElementJumpName(item);
            if (itemDesc != null && itemDesc.length() > 0) {
                key = "goto_source_source_not_found"; // NOI18N
                name = itemDesc;
            } else {
                key = "cannot-open-csm-element";// NOI18N
                StringBuilder buf = new StringBuilder(jumpToken.text());
                name = buf.toString();
            }

            String msg = NbBundle.getBundle(CsmCompletionUtils.class).getString(key);

            org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object[]{name}));
            return false;
        }
        return true;
    }

    protected TokenItem<TokenId> getJumpToken() {
        return this.jumpToken;
    }

    static TokenItem<TokenId> getToken(final Document doc, final int offset) {
        final AtomicReference<TokenItem<TokenId>> out = new AtomicReference<TokenItem<TokenId>>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                out.set(CndTokenUtilities.getTokenCheckPrev(doc, offset));
            }
        });
        return out.get();
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        if (doc == null || offset < 0 || offset > doc.getLength()) {
            return null;
        }
        if (type == HyperlinkType.ALT_HYPERLINK) {
            int[] span = CsmMacroExpansion.getMacroExpansionSpan(doc, offset, true);
            if (span != null && span[0] != span[1]) {
                // macro expansion
                return getMacroExpandedText(doc, span[0], span[1]);
            }
        }
        TokenItem<TokenId> token = jumpToken;
        if (token == null || token.offset() > offset ||
                (token.offset() + token.length()) < offset) {
            token = getToken(doc, offset);
        }
        if (!isValidToken(token, type)) {
            return null;
        }
        return getTooltipText(doc, token, offset, type);
    }

    protected abstract String getTooltipText(Document doc, TokenItem<TokenId> token, int offset, HyperlinkType type);

    private static final int EXPANDED_TEXT_TOOLTIP_LIMIT = 2000;
    private String getMacroExpandedText(final Document doc, final int start, final int end) {
        String expandedText = CsmMacroExpansion.expand(doc, start, end);
        if (expandedText.length() > EXPANDED_TEXT_TOOLTIP_LIMIT) {
            expandedText = expandedText.substring(0, EXPANDED_TEXT_TOOLTIP_LIMIT) + " ..."; // NOI18N
        }
        final StringBuilder docText = new StringBuilder();
        doc.render(new Runnable() {

            @Override
            public void run() {
                try {
                    docText.append(doc.getText(start, end - start));
                } catch (BadLocationException ex) {
                    // skip
                }
            }
        });
        return NbBundle.getMessage(CsmAbstractHyperlinkProvider.class, "MacroExpansion", CsmDisplayUtilities.htmlize(docText), CsmDisplayUtilities.htmlize(expandedText)); // NOI18N
    }

    protected final CharSequence getAlternativeHyperlinkTip(Document doc, String altTextKey, CharSequence tooltip) {
        UIGesturesSupport.submit("USG_CND_HYPERLINK_TOOLTIP", altTextKey); //NOI18N
        Preferences prefs = MimeLookup.getLookup(NbEditorUtilities.getMimeType(doc)).lookup(Preferences.class);
        int shortCut = prefs.getInt(SimpleValueNames.ALT_HYPERLINK_ACTIVATION_MODIFIERS, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        return NbBundle.getMessage(CsmAbstractHyperlinkProvider.class, altTextKey, tooltip, InputEvent.getModifiersExText(shortCut)); // NOI18N
    }
}
