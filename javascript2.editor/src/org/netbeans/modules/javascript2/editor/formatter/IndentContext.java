/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
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
    private final Stack<BlockDescription> blocks = new Stack<BlockDescription>();

    private final List<Indentation> indentations = new ArrayList<Indentation>();

    private int embeddedIndent;

    private final int caretLineStart;

    private final int caretLineEnd;

    public IndentContext(Context context, Defaults.Provider provider) {
        this.context = context;
        this.provider = provider;

        this.embedded = !JsTokenId.JAVASCRIPT_MIME_TYPE.equals(context.mimePath())
                && !JsTokenId.JSON_MIME_TYPE.equals(context.mimePath());

        int lineStart;
        try {
            lineStart = Utilities.getRowStart((BaseDocument) context.document(),
                    context.caretOffset());
        } catch (BadLocationException ex) {
            lineStart = context.caretOffset();
        }
        this.caretLineStart = lineStart;

        int lineEnd;
        try {
            lineEnd = Utilities.getRowEnd((BaseDocument) context.document(),
                    context.caretOffset());
        } catch (BadLocationException ex) {
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
