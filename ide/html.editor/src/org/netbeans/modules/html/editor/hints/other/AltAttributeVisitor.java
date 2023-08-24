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
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.refactoring.InlinedStyleInfo;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Christian Lenz
 */
public class AltAttributeVisitor implements ElementVisitor {

    private static final CharSequence ALT_ATTR = "alt"; // NOI18N

    private final HtmlRuleContext context;
    private final List<Hint> hints;
    private final String tagToFindRegEx;

    public AltAttributeVisitor(Rule rule, HtmlRuleContext context, List<Hint> hints, String tagToFindRegEx) throws IOException {
        this.context = context;
        this.hints = hints;
        this.tagToFindRegEx = tagToFindRegEx;
    }

    @Override
    public void visit(Element node) {
        Source source = Source.create(context.getFile());
        Document doc = source.getDocument(false);
        final AtomicReference<List<InlinedStyleInfo>> result = new AtomicReference<>();

        doc.render(() -> {
            List<InlinedStyleInfo> found = new LinkedList<>();
            result.set(found);

            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(th, node.from());

            if (ts == null) {
                return;
            }

            OffsetRange range;
            CharSequence tag = null;
            CharSequence attr = null;
            int startTagOffset = 0;
            int endTagOffset = 0;

            do {
                Token<HTMLTokenId> t = ts.token();

                if (t == null) {
                    return;
                }

                if (t.id() == HTMLTokenId.TAG_OPEN) {
                    tag = t.text();
                    attr = null;

                    startTagOffset = ts.offset();
                } else if (t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    endTagOffset = ts.offset();

                    range = new OffsetRange(startTagOffset, endTagOffset);
                    //closing tag, produce the info
                    if (tag != null && Pattern.matches(tagToFindRegEx, tag) && attr == null) {
                        //alt attribute found
                        hints.add(new AddMissingAltAttributeHint(context, range));

                        tag = attr = null;
                    }
                } else if (t.id() == HTMLTokenId.ARGUMENT) {
                    if (TokenUtilities.textEquals(t.text(), ALT_ATTR)) {
                        attr = t.text();
                        range = null;
                    }
                }
            } while (ts.moveNext() && ts.offset() <= node.to());
        });
    }
}
