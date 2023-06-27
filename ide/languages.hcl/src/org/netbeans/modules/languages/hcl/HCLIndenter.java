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
package org.netbeans.modules.languages.hcl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.indent.spi.support.AutomatedIndenting;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

import static org.netbeans.modules.languages.hcl.HCLTokenId.*;

/**
 *
 * @author lkishalmi
 */
public class HCLIndenter implements IndentTask {

    private static final MimePath MIME_HCL = MimePath.get("text/x-hcl");
    private final Context context;
    private final int indentSize;

    private TokenSequence<HCLTokenId> ts;

    public HCLIndenter(Context context) {
        this.context = context;
        Preferences prefs = CodeStylePreferences.get(context.document()).getPreferences();
        indentSize = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 2);
    }

    @Override
    public void reindent() throws BadLocationException {
        TokenSequence<?> tseq = TokenHierarchy.get(context.document()).tokenSequence();

        if (MimePath.parse(context.mimePath()).getIncludedPaths().contains(MIME_HCL)) {
            ts = (TokenSequence<HCLTokenId>) tseq;
        }

        ArrayList<Context.Region> regions = new ArrayList<>(context.indentRegions());
        Collections.reverse(regions);

        if (ts != null) {
            for (Context.Region region : regions) {
                int rstart = region.getStartOffset();
                ts.move(rstart);
                int prevLineIndent = 0;
                if (ts.movePrevious()) {
                    int prevLineStart = context.lineStartOffset(ts.offset());
                    prevLineIndent = context.lineIndent(prevLineStart);
                    while (ts.offset() > prevLineStart && ts.movePrevious());
                }
                int delta = 0;

                LinkedList<Integer> startOffsets = getStartOffsets(region);
                Map<Integer, Integer> newIndents = new HashMap<>();

                for (Integer startOffset : startOffsets) {
                    delta = depthScan(startOffset);

                    int newIndent = prevLineIndent + (delta * indentSize);
                    newIndent = Math.max(newIndent, 0);

                    Token<HCLTokenId> token = ts.token();
                    // Do not indent block comments and heredoc contents
                    if (token != null && token.id() != BLOCK_COMMENT && token.id() != HEREDOC) {
                        newIndents.put(startOffset, newIndent);
                    }
                    // TODO: Add support for indented HEREDOC <<~
                    prevLineIndent = newIndent;
                }

                while (!startOffsets.isEmpty()) {
                    Integer startOffset = startOffsets.removeLast();
                    Integer newIndent = newIndents.get(startOffset);
                    if (newIndent != null) {
                        context.modifyIndent(startOffset, newIndent);
                    }
                }
            }
        }
    }

    private int depthScan(int offset) {
        if (offset == 0) {
            return 0;
        }
        int ret = 0;
        Token<HCLTokenId> token = ts.token();
        while ((token == null) || (ts.offset() + token.length() < offset)) {
            if (ts.moveNext()) {
                token = ts.token();
                if (isGroupOpen(token.id())) {
                    ret++;
                }
                if (isGroupClose(token.id())) {
                    ret--;
                }
            } else {
                break;
            }
        }
        // Check immediate closures after the offset + WS
        while (ts.moveNext() && ((ts.token().id() == WS) || isGroupClose(ts.token().id()))) {
            if (isGroupClose(ts.token().id())) {
                ret--;
            }
        }
        if (ts.offset() > offset) { // Move TS mack to the previously read non-group closing token.
            ts.movePrevious();
        }
        return ret;
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }

    private LinkedList<Integer> getStartOffsets(Context.Region region) throws BadLocationException {
        LinkedList<Integer> offsets = new LinkedList<>();
        int offset = region.getEndOffset();
        int lso;
        while (offset > 0 && (lso = context.lineStartOffset(offset)) >= region.getStartOffset()) {
            offsets.addFirst(lso);
            offset = lso - 1;
        }
        return offsets;
    }

    @MimeRegistration(mimeType = "text/x-hcl", service = IndentTask.Factory.class)
    public static class Factory implements IndentTask.Factory {

        @Override
        public IndentTask createTask(Context context) {
            return new HCLIndenter(context);
        }

    }

    @MimeRegistration(mimeType = "text/x-hcl", service = TypedTextInterceptor.Factory.class)
    public static class AutoIndentFactory implements TypedTextInterceptor.Factory {

        private static final TypedTextInterceptor AUTO_INDENT  = AutomatedIndenting.createHotCharsIndenter(
                Pattern.compile("\\s*\\}"),
                Pattern.compile("\\s*\\]"),
                Pattern.compile("\\s*\\)")
        );

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return AUTO_INDENT;
        }

    }
}
