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
package org.netbeans.modules.javascript2.requirejs.html;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.javascript2.requirejs.editor.FSCompletionUtils;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Pet Pisl
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/xhtml", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-jsp", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-tag", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-php5", service = HtmlExtension.class)
})
public class RequireJsHtmlExtension extends HtmlExtension {

    private static final String DATAMAIN = "data-main"; //NOI18N
    private static final String SCRIPT = "script"; //NOI18N
    private static final ValueCompletion<HtmlCompletionItem> FILE_NAME_SUPPORT = new FilenameSupport();

    @Override
    public List<CompletionItem> completeAttributes(HtmlExtension.CompletionContext context) {
        Element element = context.getCurrentNode();
        List<CompletionItem> items = new ArrayList<>();
        if (element != null) {
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    String name = ot.unqualifiedName().toString();
                    if (SCRIPT.equalsIgnoreCase(name)) {
                        Collection<CustomAttribute> customAttributes = RequireJsCustomAttribute.getCustomAttributes();
                        for (CustomAttribute ca : customAttributes) {
                            if (LexerUtils.startsWith(ca.getName(), context.getPrefix(), true, false)) {
                                items.add(new RequireJsAttributeCompletionItem(ca, context.getCCItemStartOffset()));
                            }
                        }
                    }
                    break;
            }
        }

        return items;
    }

    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        String attributeName = context.getAttributeName();
        if (attributeName.equals(DATAMAIN)) {
            FileObject fileObject = context.getResult().getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                List<CompletionItem> items = new ArrayList<>();
                items.addAll(FILE_NAME_SUPPORT.getItems(fileObject, context.getCCItemStartOffset(), context.getPrefix()));
                return items;
            }
        }

        return Collections.emptyList();
    }

    private static class FilenameSupport extends RequireJsFileReferenceCompletion<HtmlCompletionItem> {

        @Override
        public HtmlCompletionItem createFileItem(FileObject file, int anchor) {
            return RequireJsHtmlCompletionItem.createFileCompletionItem(file, anchor);
        }

        @Override
        public HtmlCompletionItem createGoUpItem(int anchor, Color color, ImageIcon icon) {
            return HtmlCompletionItem.createGoUpFileCompletionItem(anchor, color, icon); // NOI18N
        }
    }

    @Override
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        final TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, caretOffset);
        if (ts == null) {
            return super.getReferenceSpan(doc, caretOffset);
        }

        ts.move(caretOffset);
        if (ts.moveNext()) {
            Token<HTMLTokenId> token = ts.token();
            int offset = ts.offset();
            if (getDataMainValue(ts, caretOffset) != null) {
                return new OffsetRange(offset + 1, offset + token.length() - 1);
            }

        }
        return super.getReferenceSpan(doc, caretOffset);
    }

    @Override
    public DeclarationFinder.DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        final TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return super.findDeclaration(info, caretOffset);
        }
        final TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(th, caretOffset);
        if (ts == null) {
            return super.findDeclaration(info, caretOffset);
        }
        int eOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        String value = getDataMainValue(ts, eOffset);
        if (value != null) {
            FileObject fo = info.getSnapshot().getSource().getFileObject();
            if (fo != null) {
                String name = value.toLowerCase().endsWith(".js") ? value : value + ".js"; //NOI18N
                FileObject targetFO = FSCompletionUtils.findFileObject(fo, name, true);
                if (targetFO != null) {
                    return new DeclarationFinder.DeclarationLocation(targetFO, 0);
                }
                name = value + ".JS";
                targetFO = FSCompletionUtils.findFileObject(fo, name, true);
                if (targetFO != null) {
                    return new DeclarationFinder.DeclarationLocation(targetFO, 0);
                }
                name = value + ".Js";
                targetFO = FSCompletionUtils.findFileObject(fo, name, true);
                if (targetFO != null) {
                    return new DeclarationFinder.DeclarationLocation(targetFO, 0);
                }
            }
        }
        return super.findDeclaration(info, caretOffset);
    }

    private String getDataMainValue(TokenSequence<HTMLTokenId> ts, int offset) {
        ts.move(offset);
        if (ts.moveNext()) {
            Token<HTMLTokenId> token = ts.token();
            HTMLTokenId tokenId = token.id();
            if (tokenId == HTMLTokenId.VALUE) {
                String value = token.text().toString();
                token = LexerUtils.followsToken(ts, Arrays.asList(HTMLTokenId.ARGUMENT), true, false, HTMLTokenId.OPERATOR, HTMLTokenId.WS, HTMLTokenId.BLOCK_COMMENT);
                if (token != null && token.id() == HTMLTokenId.ARGUMENT && DATAMAIN.equals(token.text().toString())) {
                    return value.substring(1, value.length() - 1);
                }

            }
        }
        return null;
    }

}
