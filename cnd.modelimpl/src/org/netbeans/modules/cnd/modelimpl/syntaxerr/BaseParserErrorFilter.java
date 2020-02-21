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

package org.netbeans.modules.cnd.modelimpl.syntaxerr;

import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ParserErrorFilter;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.openide.util.NbBundle;

/**
 * A common base class for ParserErrorFilter implementations
 */
public abstract class BaseParserErrorFilter extends ParserErrorFilter {

    // Mac OS X uses '\n' as line separator too
    private static final char LF = '\n'; // NOI18N

    protected Collection<CsmErrorInfo> toErrorInfo(Collection<CsmParserProvider.ParserError> errors, CsmFile file) {
        Collection<CsmErrorInfo> result = new ArrayList<>();
        if (!errors.isEmpty()){
            CharSequence text = file.getText();
            CsmParserProvider.ParserError prev = null;
            ContextCache prevLine = new ContextCache();
            for (CsmParserProvider.ParserError e : errors) {
                // Fix for IZ#143082: some syntax errors are reported twice.
                // We assume that equal recognition exceptions are next to each other.
                if (!equal(prev, e)) {
                    result.add(toErrorInfo(e, text, prevLine));
                }
                prev = e;
            }
        }
        return result;
    }

    private boolean equal(CsmParserProvider.ParserError e1, CsmParserProvider.ParserError e2) {
        if ((e1 == null) != (e2 == null)) {
            return false;
        }
        return e1.getLine() == e2.getLine() && e1.getColumn() == e2.getColumn();
    }

    private CsmErrorInfo toErrorInfo(CsmParserProvider.ParserError e, CharSequence text, ContextCache prevLine) {
        return toErrorInfo(getMessage(e), e.getLine(), e.getColumn(), text, prevLine, e.getTokenText());
    }

    private CsmErrorInfo toErrorInfo(String message, int line, int column, 
            CharSequence text, ContextCache prevLine, String tokenText) {
        int start = 0;
        int currLine = 1;
        if (prevLine.line <= line) {
            start = prevLine.offset;
            currLine = prevLine.line;
        }

        while (start < text.length() && currLine < line) {
            char c = text.charAt(start++);
            if (c == LF) {
                currLine++;
            }
        }
        prevLine.offset = start;
        prevLine.line = currLine;

        //start += column;
        int end = start + 1;
        while (end < text.length()) {
            if (text.charAt(end++) == LF) {
                break;
            }
        }
        end--;

        if (tokenText != null) {
            // if possible, highlight only single token
            int tokenStart = start + column - 1;
            int tokenEnd = tokenStart + tokenText.length();
            if (0 <= tokenStart && 0 <= tokenEnd && tokenEnd <= text.length()
                    && text.subSequence(tokenStart, tokenEnd).toString().equals(tokenText)) {
                start = tokenStart;
                end = tokenEnd;
            }
        }

        return new SimpleErrorInfo(start, end, message, getDefaultSeverity());
    }

    private String getMessage(CsmParserProvider.ParserError e) {        
        String tokenText = e.getTokenText();
        if (tokenText == null) {
            return NbBundle.getMessage(BaseParserErrorFilter.class, "MSG_PARSER_ERROR"); // NOI18N
        } else {
            if (e.isEof()) {
                return NbBundle.getMessage(BaseParserErrorFilter.class, "MSG_UNEXPECTED_EOF"); // NOI18N
            }
            return NbBundle.getMessage(BaseParserErrorFilter.class, "MSG_UNEXPECTED_TOKEN", tokenText); // NOI18N
        }
    }

    protected CsmErrorInfo.Severity getDefaultSeverity() {
        return CsmErrorInfo.Severity.ERROR;
    }
    private static class ContextCache{
        int offset,line=1;
    }
}
