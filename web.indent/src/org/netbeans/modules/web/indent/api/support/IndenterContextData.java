/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
