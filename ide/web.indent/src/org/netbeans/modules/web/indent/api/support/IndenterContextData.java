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

package org.netbeans.modules.web.indent.api.support;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;

/**
 *
 * @since org.netbeans.modules.css.editor/1 1.3
 */
public final class IndenterContextData<T1 extends TokenId> {

    private int lineStartOffset;
    private int lineEndOffset;
    private boolean blankLine;
    private int lineNonWhiteStartOffset;
    private JoinedTokenSequence<T1> joinedTS;
    private boolean languageBlockStart;
    private boolean languageBlockEnd;
    private int nextLineStartOffset;
    private boolean indentThisLine;

    public IndenterContextData(JoinedTokenSequence<T1> joinedTS,
            int lineStartOffset, int lineEndOffset, int lineNonWhiteStartOffset,
            int nextLineStartOffset, boolean blank, boolean indentThisLine) {
        this.lineStartOffset = lineStartOffset;
        this.lineEndOffset = lineEndOffset;
        this.lineNonWhiteStartOffset = lineNonWhiteStartOffset;
        this.joinedTS = joinedTS;
        this.nextLineStartOffset = nextLineStartOffset;
        this.blankLine = blank;
        this.indentThisLine = indentThisLine;
    }

    public int getNextLineStartOffset() {
        return nextLineStartOffset;
    }

    public int getLineEndOffset() {
        return lineEndOffset;
    }

    public int getLineStartOffset() {
        return lineStartOffset;
    }

    public int getLineNonWhiteStartOffset() {
        return lineNonWhiteStartOffset;
    }

    public JoinedTokenSequence<T1> getJoinedTokenSequences() {
        return joinedTS;
    }

    public boolean isBlankLine() {
        return blankLine;
    }

    public boolean isLanguageBlockStart() {
        return languageBlockStart;
    }

    void setLanguageBlockStart(boolean languageBlockStart) {
        this.languageBlockStart = languageBlockStart;
    }

    public boolean isLanguageBlockEnd() {
        return languageBlockEnd;
    }

    void setLanguageBlockEnd(boolean languageBlockEnd) {
        this.languageBlockEnd = languageBlockEnd;
    }

    public boolean isIndentThisLine() {
        return indentThisLine;
    }

    @Override
    public String toString() {
        return "FormatterContextData[lineStartOffset=" + lineStartOffset + "," +
                "lineEndOffset=" + lineEndOffset + "," + "joinedTS=" + joinedTS + "," +
                "blankLine=" + blankLine +"]";
    }

}
