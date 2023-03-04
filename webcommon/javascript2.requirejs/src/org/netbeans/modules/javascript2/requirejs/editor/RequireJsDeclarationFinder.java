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
package org.netbeans.modules.javascript2.requirejs.editor;

import java.util.Arrays;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.DeclarationFinder;
import org.netbeans.modules.javascript2.requirejs.RequireJsPreferences;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@DeclarationFinder.Registration(priority = 12)
public class RequireJsDeclarationFinder implements DeclarationFinder {

    private boolean isSupportEnabled(FileObject fo) {
        if (fo == null) {
            return false;
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return false;
        }
        return RequireJsPreferences.getBoolean(project, RequireJsPreferences.ENABLED);
    }
    
    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        if (!isSupportEnabled(fo)) {
            return DeclarationLocation.NONE;
        }
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(info.getSnapshot().getTokenHierarchy(), caretOffset);
        int jsOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        if (ts != null) {
            ts.move(jsOffset);
            if (ts.moveNext() && ts.token().id() == JsTokenId.STRING && EditorUtils.isFileReference(info.getSnapshot(), ts.offset())) {
                ts.move(jsOffset);
                ts.moveNext();
                String path = FSCompletionUtils.removePlugin(ts.token().text().toString());
                FileObject targetFO = FSCompletionUtils.findMappedFileObject(path, fo);
                if (targetFO != null) {
                    return new DeclarationLocation(targetFO, 0);
                }
            } else if (ts != null && ts.token() != null && ts.token().id() == JsTokenId.IDENTIFIER) {
                int commaNumber = 0;
                Token<? extends JsTokenId> token;
                do {
                    token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.IDENTIFIER, JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT));
                    if (token.id() == JsTokenId.OPERATOR_COMMA) {
                        commaNumber++;
                    } else if (token.id() == JsTokenId.OPERATOR_DOT) {
                        break;
                    }
                } while (ts.movePrevious() && token.id() != JsTokenId.BRACKET_LEFT_BRACKET
                        && token.id() != JsTokenId.BRACKET_LEFT_PAREN);
                if (token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
                    token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
                    if (token.id() == JsTokenId.IDENTIFIER
                            && (EditorUtils.DEFINE.equals(token.text().toString()) || EditorUtils.REQUIRE.equals(token.text().toString()) || EditorUtils.REQUIREJS.equals(token.text().toString()))) {
                        // we found define method
                        token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.BRACKET_LEFT_BRACKET, JsTokenId.KEYWORD_FUNCTION, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_PAREN));
                        if (token.id() == JsTokenId.BRACKET_LEFT_BRACKET) {
                            int commaInArray = 0;
                            String path = null;
                            do {
                               token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.STRING, JsTokenId.OPERATOR_COMMA, JsTokenId.BRACKET_RIGHT_PAREN));
                               if (token.id() == JsTokenId.OPERATOR_COMMA) {
                                   commaInArray++;
                               } else if (token.id() == JsTokenId.STRING && commaInArray == commaNumber) {
                                   path = token.text().toString();
                               }
                            } while (path == null && ts.moveNext() && token.id() != JsTokenId.BRACKET_RIGHT_PAREN);
                            if (path != null) {
                                path = FSCompletionUtils.removePlugin(path);
                                FileObject targetFO = FSCompletionUtils.findMappedFileObject(path, fo);
                                if (targetFO != null) {
                                    return new DeclarationLocation(targetFO, 0);
                                }
                            }
                        }
                    }
                }
            }
        }

        return DeclarationLocation.NONE;
    }

    

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final OffsetRange[] value = new OffsetRange[1];
        value[0] = OffsetRange.NONE;
        
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
                if (ts != null) {
                    ts.move(caretOffset);
                    if (ts.moveNext() && ts.token().id() == JsTokenId.STRING && EditorUtils.isFileReference(Source.create(doc).createSnapshot(), ts.offset())) {
                        ts.move(caretOffset);
                        ts.moveNext();
                        String path = ts.token().text().toString();
                        int start = ts.offset();
                        int end = ts.offset() + path.length();
                        if (FSCompletionUtils.containsPlugin(path)) {
                            String withoutPlugin = FSCompletionUtils.removePlugin(path);
                            start = start + path.length() - withoutPlugin.length();
                        }
                        value[0] = new OffsetRange(start, end);
                    }
                }
            }
        });
        return value[0];
    }

    

}
