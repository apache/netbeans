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
package org.netbeans.modules.javascript2.jade.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;

/**
 *
 * @author Petr Pisl    
 */
public class JadeStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        Map<String, List<OffsetRange>> folds = new HashMap<>();
        TokenHierarchy th = info.getSnapshot().getTokenHierarchy();
        TokenSequence<JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());

        List<TokenSequence<?>> list = th.tokenSequenceList(ts.languagePath(), 0, info.getSnapshot().getText().length());

        ts.moveStart();
        Token<JadeTokenId> token;
        JadeTokenId id;
        int indent = 0;
        int commentDelStart = 0;
        int commentDelLength = 0;
        List<FoldingItem> stack = new ArrayList<FoldingItem>();
        boolean afterEOL = true;
        while (ts.moveNext()) {
            token = ts.token();
            id = token.id();
            if (afterEOL) {
                if (id == JadeTokenId.WHITESPACE) {
                    indent = token.length();
                } else if (id == JadeTokenId.TAG || id == JadeTokenId.KEYWORD_BLOCK || id == JadeTokenId.KEYWORD_MIXIN) {
                    afterEOL = false;
                    stack.add(new FoldingItem(FoldType.TAG, indent, ts.offset(), ts.offset() + token.length()));
                } else  if (id != JadeTokenId.COMMENT_DELIMITER && id != JadeTokenId.UNBUFFERED_COMMENT_DELIMITER){
                    afterEOL = false;
                } else {
                    commentDelStart = ts.offset();
                    commentDelLength = token.length();
                }
                if (id == JadeTokenId.COMMENT || id == JadeTokenId.UNBUFFERED_COMMENT) {
                    String comment = token.text().toString();
                    while (!comment.isEmpty() && comment.charAt(comment.length() - 1) == '\n') {
                        comment = comment.substring(0, comment.length() - 1);
                    }
                    if (comment.indexOf('\n') >= 0) {
                        stack.add(new FoldingItem(FoldType.COMMENT, indent, commentDelStart, commentDelStart + commentDelLength));
                    }
                    afterEOL = true;
                }
            } 
            if (id == JadeTokenId.EOL) {
                afterEOL = true;
                indent = 0;
            } 
            
        }
        if (!stack.isEmpty()) {
            for(int i = 0; i < stack.size(); i++) {
                FoldingItem item1 = stack.get(i);
                boolean foldCreated = false;
                for (int j = i + 1; j < stack.size(); j++) {
                    FoldingItem item2 = stack.get(j);
                    if (item1.indent >= item2.indent) {
                        foldCreated = true;
                        appendFold(info, folds, item1.type.code(), item1.tagEnd, item2.tagStart - item2.indent - 1);
                        break;
                    }
                }
                if (!foldCreated) {
                    appendFold(info, folds, FoldType.TAG.code(), item1.tagEnd, ts.offset() + ts.token().length());
                }
            }
            
        }
        return folds;
    }

    private void appendFold(ParserResult info, Map<String, List<OffsetRange>> folds, String kind, int startOffset, int endOffset) {
        if (startOffset >= 0 && endOffset >= startOffset) {
            if (info.getSnapshot().getText().subSequence(startOffset, endOffset).toString().indexOf('\n') > -1) {
                getRanges(folds, kind).add(new OffsetRange(startOffset, endOffset));
            }   
        }
    }
    
    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }
    
    @Override
    public Configuration getConfiguration() {
        return null;
    }
    
    private static class FoldingItem {
        final int indent;
        final int tagStart;
        final int tagEnd;
        final FoldType type;

        public FoldingItem(FoldType type, int indent, int tagStart, int tagEnd) {
            this.indent = indent;
            this.tagStart = tagStart;
            this.tagEnd = tagEnd;
            this.type = type;
        }
        
    }
}
