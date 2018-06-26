/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.Parser;
import com.oracle.js.parser.ScriptEnvironment;
import com.oracle.js.parser.Source;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public class JsParser extends SanitizingParser<JsParserResult> {
    
    private static String SINGLETON_FUNCTION_START = "(function(){"; // NOI18N
    private static String SINGLETON_FUNCTION_END = "})();"; // NOI18N
    private static String SHEBANG_START = "#!"; //NOI18N
    private static String NODE = "node"; //NOI18N
    private static String IMPORT = "import "; //NOI18N
    private static String EXPORT = "export "; //NOI18N
    
    public JsParser() {
        super(JsTokenId.javascriptLanguage());
    }

    @Override
    protected String getDefaultScriptName() {
        return "javascript.js"; // NOI18N
    }

    @Override
    protected JsParserResult parseSource(SanitizingParser.Context context, JsErrorManager errorManager) throws Exception {
        final Snapshot snapshot = context.getSnapshot();
        final String name = context.getName();
        final String text = context.getSource();
        final int caretOffset = context.getCaretOffset();
        final boolean isModule = context.isModule();
        String parsableText = text;
//        System.out.println(text);
//        System.out.println("----------------");
        // handle shebang
        if (parsableText.startsWith(SHEBANG_START)) { // NOI18N
            StringBuilder sb = new StringBuilder(parsableText);
            int index = parsableText.indexOf("\n"); // NOI18N
            if (index < 0) {
                index = parsableText.length();
            }

            sb.delete(0, index);
            for (int i = 0; i < index; i++) {
                sb.insert(i, ' ');
            }
             if (isNodeSource(text.substring(0, index), text)) {
                // we are expecting a node file like #!/usr/bin/env node
                // such files are in runtime wrapped with a function, so the files
                // can contain a return statements in global context.
                // -> we need wrap the source to a function as well. 
                sb.delete(0, SINGLETON_FUNCTION_START.length());
                sb.insert(0, SINGLETON_FUNCTION_START);
                sb.append(SINGLETON_FUNCTION_END);
            }
            parsableText = sb.toString();
        }
        if (caretOffset > 0 && parsableText.charAt(caretOffset - 1) == '.' 
                && (parsableText.length() > caretOffset)
                && Character.isWhitespace(parsableText.charAt(caretOffset))) {
            // we are expecting that the dot was just written. See issue #246006
            StringBuilder sb = new StringBuilder(parsableText);
            sb.delete(caretOffset - 1, caretOffset);
            sb.insert(caretOffset - 1, ' ');
            parsableText = sb.toString();
        }
        
        Source source = Source.sourceFor(name, parsableText);
        errorManager.setLimit(0);

        ScriptEnvironment.Builder builder = ScriptEnvironment.builder();
        Parser parser = new Parser(
                builder.emptyStatements(true).es6(true).es7(true).jsx(true).build(),
                source,
                errorManager);
        FunctionNode node = null;
        if (isModule) {
            node = parser.parseModule(name);
        } else {
            node = parser.parse();
        }
        return new JsParserResult(snapshot, node);
    }

    @Override
    protected JsParserResult createErrorResult(Snapshot snapshot) {
        return new JsParserResult(snapshot, null);
     }

    @Override
    protected String getMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }
    
    private boolean isNodeSource(String firstLine, String text) {
        boolean hasCorretSheBang = firstLine.startsWith(SHEBANG_START) && firstLine.indexOf(NODE) > -1 && SINGLETON_FUNCTION_START.length() < firstLine.length();
        if (hasCorretSheBang) {
            int lineOffsetBegin = firstLine.length() + 1;
            int lineOffsetEnd = text.indexOf('\n', lineOffsetBegin);
            while (lineOffsetEnd >= lineOffsetBegin) {
                String line = text.substring(lineOffsetBegin, lineOffsetEnd).trim();
                if (line.startsWith(IMPORT) || line.startsWith(EXPORT)) {
                    // if contains import or exports, it's module
                    hasCorretSheBang = false;
                    break;
                }
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("*") || line.startsWith("/*")) { //NOI18N
                    // skip lines with comments and emppty lines
                    lineOffsetBegin = lineOffsetEnd + 1;
                    lineOffsetEnd = text.indexOf('\n', lineOffsetBegin);
                } else {
                    // there is no import or export expression -> it can be wrapped
                    break;
                }
            }
            
        }
        
        return hasCorretSheBang;
    }
}
