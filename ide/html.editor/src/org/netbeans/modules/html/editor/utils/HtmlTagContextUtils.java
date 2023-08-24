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
package org.netbeans.modules.html.editor.utils;

import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.Utils;

/**
 *
 * @author Christian Lenz
 */
public final class HtmlTagContextUtils {

    private HtmlTagContextUtils() {
    }

    public static OffsetRange adjustContextRange(final Document doc, final int from, final int to, final boolean beforeClosingToken) {
        final AtomicReference<OffsetRange> ret = new AtomicReference<>();
        ret.set(new OffsetRange(from, to)); //return the same pair by default
        doc.render(() -> {
            TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, from);

            if (ts == null) {
                //no html token sequence at the offset, try to
                //TODO possibly try to travese the top level sequence backward
                //and try to find an html embedding.
                return;
            }

            Token<HTMLTokenId> openTag = Utils.findTagOpenToken(ts);

            if (openTag == null) {
                return;
            }

            int adjustedFrom = ts.offset();

            //now try to find the tag's end
            ts.move(to);
            int adjustedTo = -1;
            while (ts.moveNext()) {
                Token<HTMLTokenId> t = ts.token();

                if (t == null) {
                    return;
                }

                if (t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    adjustedTo = beforeClosingToken ? ts.offset() : ts.offset() + t.length();

                    break;
                } else if (t.id() == HTMLTokenId.TEXT) {
                    //do not go too far - out of the tag
                    break;
                }
            }

            if (adjustedTo == -1) {
                return;
            }

            //we found the adjusted range
            ret.set(new OffsetRange(adjustedFrom, adjustedTo));
        });
        return ret.get();
    }
}
