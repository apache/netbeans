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

    private final static String SINGLETON_FUNCTION_START = "(function(){"; // NOI18N
    private final static String SINGLETON_FUNCTION_END = "})();"; // NOI18N
    private final static String SHEBANG_START = "#!"; //NOI18N
    private final static String NODE = "node"; //NOI18N
    private final static String IMPORT = "import "; //NOI18N
    private final static String EXPORT = "export "; //NOI18N

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
                builder.emptyStatements(true).ecmacriptEdition(Integer.MAX_VALUE).jsx(true).build(),
                source,
                errorManager);
        FunctionNode node;
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
        boolean hasCorretSheBang = firstLine.startsWith(SHEBANG_START) && firstLine.contains(NODE) && SINGLETON_FUNCTION_START.length() < firstLine.length();
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
