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
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public final class IndentContext {

    private final Context context;

    private final Defaults.Provider provider;

    private final boolean embedded;

    /**
     * <p>
     * Stack describing indentation of blocks defined by '{', '[' and blocks
     * with missing optional curly braces '{'. See also getBracketBalanceDelta()
     * </p>
     * For example:
     * <pre>
     * if (true)        // [ StackItem[block=true] ]
     *   if (true) {    // [ StackItem[block=true], StackItem[block=false] ]
     *     if (true)    // [ StackItem[block=true], StackItem[block=false], StackItem[block=true] ]
     *       foo();     // [ StackItem[block=true], StackItem[block=false] ]
     *     bar();       // [ StackItem[block=true], StackItem[block=false] ]
     *   }              // [ StackItem[block=true] ]
     * fooBar();        // [ ]
     * </pre>
     */
    private final Stack<BlockDescription> blocks = new Stack<>();

    private final List<Indentation> indentations = new ArrayList<>();

    private int embeddedIndent;

    private final int caretLineStart;

    private final int caretLineEnd;

    public IndentContext(Context context, Defaults.Provider provider) {
        this.context = context;
        this.provider = provider;

        this.embedded = !JsTokenId.JAVASCRIPT_MIME_TYPE.equals(context.mimePath())
                && !JsTokenId.JSON_MIME_TYPE.equals(context.mimePath());
        LineDocument doc = LineDocumentUtils.as(context.document(), LineDocument.class);

        int lineStart = -1;
        if (doc != null) {
            try {
                lineStart = LineDocumentUtils.getLineStart(doc,
                        context.caretOffset());
            } catch (IndexOutOfBoundsException ex) {
            }
        }
        if (lineStart == -1) {
            lineStart = context.caretOffset();
        }
        this.caretLineStart = lineStart;

        int lineEnd = -1;
        if (doc != null) {
            try {
                lineEnd = LineDocumentUtils.getLineEnd(doc,
                        context.caretOffset());
            } catch (BadLocationException | IndexOutOfBoundsException ex) {
            }
        }
        if (lineEnd == -1) {
             lineEnd = context.caretOffset();
        }
        this.caretLineEnd = lineEnd;
    }

    public Defaults.Provider getDefaultsProvider() {
        return provider;
    }

    public BaseDocument getDocument() {
        return (BaseDocument) context.document();
    }

    public Context getContext() {
        return context;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public int getEmbeddedIndent() {
        return embeddedIndent;
    }

    public void setEmbeddedIndent(int embeddedIndent) {
        this.embeddedIndent = embeddedIndent;
    }

    public Stack<BlockDescription> getBlocks() {
        return blocks;
    }

    public void addIndentation(Indentation indentation) {
        indentations.add(indentation);
    }

    public List<Indentation> getIndentations() {
        return Collections.unmodifiableList(indentations);
    }

    public int getCaretLineStart() {
        return caretLineStart;
    }

    public int getCaretLineEnd() {
        return caretLineEnd;
    }

    public static final class Indentation {

        private final int offset;

        private final int size;

        private final boolean continuation;

        public Indentation(int offset, int size, boolean continuation) {
            this.offset = offset;
            this.size = size;
            this.continuation = continuation;
        }

        public int getOffset() {
            return offset;
        }

        public int getSize() {
            return size;
        }

        public boolean isContinuation() {
            return continuation;
        }
    }

    public static final class BlockDescription {

        /**
         * Marks block without optional curly braces.
         */
        private final boolean braceless;

        private final boolean object;

        /**
         * For braceless blocks it is range from statement beginning (e.g. |if...)
         * to end of line where curly brace would be (e.g. if(...) |\n )<br>
         * For braces and brackets blocks it is offset of beginning of token for
         * both - beginning and end of range (e.g. OffsetRange[ts.token(), ts.token()])
         */
        private final OffsetRange range;

        public BlockDescription(boolean braceless, boolean object, OffsetRange range) {
            assert !object || !braceless;
            this.braceless = braceless;
            this.object = object;
            this.range = range;
        }

        public boolean isBraceless() {
            return braceless;
        }

        public boolean isObject() {
            return object;
        }

        public OffsetRange getRange() {
            return range;
        }

    }
}
