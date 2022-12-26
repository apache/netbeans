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
package org.netbeans.modules.languages.antlr.v4;

import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.antlr.v4.runtime.CharStreams;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;

import static org.antlr.parser.antlr4.ANTLRv4Lexer.*;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;

/**
 *
 * @author lkishalmi
 */
public class Antlr4Formatter implements Formatter {

    public Antlr4Formatter() {
    }

    @Override
    public void reformat(Context context, ParserResult compilationInfo) {
        LineDocument doc = LineDocumentUtils.as(context.document(), LineDocument.class);
        String text;
        int indentSize = getIndentSize(context.document());
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            return;
        }
        
        ANTLRv4Lexer lexer = new ANTLRv4Lexer(CharStreams.fromString(text));
        Token token = lexer.nextToken();
        if (token.getType() == EOF) {
            return;
        }
        
        final int cstart = context.startOffset();
        final int cend = context.endOffset();
        
        int tstart = token.getStartIndex();
        int tstop = token.getStopIndex();
        
        try {
            boolean inRule = false;
            int ruleLine = 0;
            int parenDepth = 0;
            int textDelta = 0;
            int prevTokenType = -1;
            while ((token.getType() != EOF) && (tstart < cend)) {
                switch (token.getType()) {
                    case RULE_REF:
                    case TOKEN_REF:
                        // @header , @member, etc, are not real rules;
                        if (!inRule && (prevTokenType != AT)) {
                            inRule = true;
                        }
                        break;
                    case LPAREN:
                        parenDepth++;
                        break;
                    case RPAREN:
                        parenDepth--;
                        break;
                    case SEMI:
                        inRule = false;
                        break;
                }
                if (tstop >= cstart) {
                    if (!context.isIndent()) {
                        //TODO: Do non-indent formatting
                    }
                    if (token.getChannel() == OFF_CHANNEL) {
                        String ttext = token.getText();
                        int nl = ttext.indexOf('\n');
                        while ((nl != -1) && (tstart + nl <= cend))  {
                            int lineStart = context.lineStartOffset(tstart + textDelta + nl);
                            if (inRule || ruleLine > 0) {
                                if (tstart + nl >= cstart) {
                                    // Indent the first rule line to 0 the rest to indentSize
                                    int originalIndent = context.lineIndent(lineStart);
                                    int newIndent = ruleLine > 0 ? indentSize : 0;
                                    context.modifyIndent(lineStart, newIndent);
                                    textDelta += newIndent - originalIndent;
                                }
                                ruleLine = inRule ? ruleLine + 1 : 0;
                            }
                            nl = ttext.indexOf('\n', nl + 1);
                        }
                    }
                }
                prevTokenType = token.getType();
                token = lexer.nextToken();
                tstart = token.getStartIndex();
                tstop = token.getStopIndex();
            }

            if ((cstart == cend) && (cstart == doc.getLength())) {
                // Pressed enter at the end of the file
                context.modifyIndent(cstart, inRule ? indentSize : 0);
            }
        } catch (BadLocationException ex) {}
    }

    @Override
    public void reindent(Context context) {
        reformat(context, null);
    }

    @Override
    public boolean needsParserResult() {
        return false;
    }

    @Override
    public int indentSize() {
        return 4;
    }

    @Override
    public int hangingIndentSize() {
        return 4;
    }
    
    static int getIndentSize(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        return prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 4);
    }
}
