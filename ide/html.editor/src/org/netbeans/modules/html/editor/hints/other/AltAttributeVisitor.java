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
package org.netbeans.modules.html.editor.hints.other;

import java.io.IOException;
import java.util.*;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author Christian Lenz
 */
public class AltAttributeVisitor implements ElementVisitor {

    private static final String ALT_ATTR = "alt"; // NOI18N

    private final HtmlRuleContext context;
    private final List<Hint> hints;

    public AltAttributeVisitor(Rule rule, HtmlRuleContext context, List<Hint> hints) throws IOException {
        this.context = context;
        this.hints = hints;
    }

    @Override
    public void visit(Element node) {
        // We should only be invoked for opening tags
        if (!(node instanceof OpenTag)) {
            return;
        }

        // We are only interested in img, area, applet elements
        String lowerCaseTag = node.id().toString().toLowerCase();
        if (!(lowerCaseTag.equals("img") || lowerCaseTag.equals("area") || lowerCaseTag.equals("applet"))) { // NOI18N
            return;
        }

        OpenTag openTag = (OpenTag) node;

        // First try the parser's attribute check (works for simple cases)
        if (openTag.getAttribute(ALT_ATTR) != null) {
            return; // alt attribute exists
        }

        // For cases with embedded code (PHP, etc.), the parser may fail to detect attributes.
        // Use lexer tokens as fallback - they are more reliable with embedded code.
        if (hasAltAttributeViaLexer(openTag)) {
            return; // alt attribute found via lexer
        }

        hints.add(new AddMissingAltAttributeHint(context, openTag));
    }

    /**
     * Checks for the presence of an "alt" attribute using lexer tokens.
     * This is more reliable than the parser when PHP or other code is embedded in attribute values.
     */
    private boolean hasAltAttributeViaLexer(OpenTag openTag) {
        Document doc = context.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return false;
        }

        Snapshot snapshot = context.getSnapshot();

        // Convert snapshot offset to original document offset
        int originalTagStart = snapshot.getOriginalOffset(openTag.from());
        int originalTagEnd = snapshot.getOriginalOffset(openTag.to());
        if (originalTagStart == -1 || originalTagEnd == -1) {
            return false;
        }

        TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, originalTagStart);
        if (ts == null) {
            return false;
        }

        ts.move(originalTagStart);
        while (ts.moveNext()) {
            Token<HTMLTokenId> token = ts.token();
            if (token == null) {
                break;
            }

            // Stop if we've gone past the tag end
            if (ts.offset() >= originalTagEnd) {
                break;
            }

            HTMLTokenId id = token.id();

            // Found an attribute name - check if it's "alt"
            if (id == HTMLTokenId.ARGUMENT) {
                if (LexerUtils.equals(token.text(), ALT_ATTR, true, false)) {
                    return true; // Found alt attribute
                }
            }

            // Stop at tag close or next tag open
            if (id == HTMLTokenId.TAG_CLOSE_SYMBOL || id == HTMLTokenId.TAG_OPEN_SYMBOL) {
                break;
            }
        }

        return false;
    }
}
