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
package org.netbeans.modules.cnd.makefile.editor;

import java.io.IOException;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.cnd.api.makefile.MakefileElement;
import org.netbeans.modules.cnd.api.makefile.MakefileMacro;
import org.netbeans.modules.cnd.api.makefile.MakefileRule;
import org.netbeans.modules.cnd.api.makefile.MakefileSupport;
import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/**
 */
public class MakefileHyperlinkProvider implements HyperlinkProvider {

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        return tokenHierarchy != null && findHyperlinkToken(tokenHierarchy, offset) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        if (tokenHierarchy != null) {
            HyperlinkToken token = findHyperlinkToken(tokenHierarchy, offset);
            if (token != null) {
                return new int[] {token.offset, token.offset + token.text.length()};
            }
        }
        return null;
    }

    @Override
    public void performClickAction(final Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        if (tokenHierarchy != null) {
            final HyperlinkToken token = findHyperlinkToken(tokenHierarchy, offset);
            if (token != null) {
                switch (token.kind) {
                    case RULE:
                    case MACRO:
                        findAndOpenElement(doc, token);
                        break;

                    case INCLUDE:
                        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
                        if (fileObject != null) {
                            findAndOpenFile(fileObject.getParent(), token.text);
                        }
                        break;
                }
            }
        }
    }

    private static HyperlinkToken findHyperlinkToken(TokenHierarchy<?> tokenHierarchy, int offset) {
         TokenSequence<MakefileTokenId> tokenSequence = tokenHierarchy.tokenSequence(MakefileTokenId.language());
         if (tokenSequence != null) {
             tokenSequence.move(offset);
             if (tokenSequence.moveNext()) {
                Token<MakefileTokenId> token = tokenSequence.token();
                if (token != null) {
                    switch (token.id()) {
                        case BARE:
                            return analyzeBareToken(tokenSequence, token, offset);
                        case MACRO:
                            return analyzeMacroToken(tokenSequence, token, offset);
                    }
                }
             }
         }
         return null;
    }

    private static HyperlinkToken analyzeBareToken(TokenSequence<MakefileTokenId> tokenSequence, Token<MakefileTokenId> token, int offset) {
        String text = token.text().toString();
        int tokenOffset = tokenSequence.offset();
        MakefileElement.Kind kind = MakefileElement.Kind.RULE;

        // check for "include" keyword behind current token
        PREV_LOOP:
        while (tokenSequence.movePrevious()) {
            Token<MakefileTokenId> prevToken = tokenSequence.token();
            switch (prevToken.id()) {
                case BARE:
                case WHITESPACE:
                case MACRO:
                    // ok, just skip them
                    break;

                case INCLUDE:
                    kind = MakefileElement.Kind.INCLUDE;
                    break PREV_LOOP;

                default:
                    break PREV_LOOP;
            }
        }

        tokenSequence.move(offset);

        // check for some assignment token ahead of current token
        NEXT_LOOP:
        while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> nextToken = tokenSequence.token();
            switch (nextToken.id()) {
                case BARE:
                case WHITESPACE:
                case MACRO:
                    // ok, just skip them
                    break;

                case EQUALS:
                case COLON_EQUALS:
                case PLUS_EQUALS:
                    kind = MakefileElement.Kind.MACRO;
                    break NEXT_LOOP;

                default:
                    break NEXT_LOOP;
            }
        }

        return new HyperlinkToken(text, tokenOffset, kind);
    }

    private static HyperlinkToken analyzeMacroToken(TokenSequence<MakefileTokenId> tokenSequence, Token<MakefileTokenId> token, int offset) {
        String text = token.text().toString();
        int tokenOffset = tokenSequence.offset();
        if ((text.startsWith("$(") && text.endsWith(")") || text.startsWith("${") && text.endsWith("}")) && // NOI18N
                tokenOffset + 2 <= offset && offset < tokenOffset + text.length() - 1) {
            text = text.substring(2, text.length() - 1);
            return new HyperlinkToken(text, tokenOffset + 2, MakefileElement.Kind.MACRO);
        }
        return null;
    }

    private static void findAndOpenFile(FileObject baseDir, final String targetPath) {
        while (baseDir != null) {
            FileObject fileObject = baseDir.getFileObject(targetPath);
            if (fileObject != null) {
                asyncOpenInEditor(fileObject, 0);
                break;
            }
            baseDir = baseDir.getParent();
        }
    }

    private static void findAndOpenElement(final Document doc, final HyperlinkToken token) {
        try {
            List<MakefileElement> elements = MakefileSupport.parseDocument(doc);
            for (MakefileElement element : elements) {
                if (element.getKind() == MakefileElement.Kind.RULE && token.kind == MakefileElement.Kind.RULE) {
                    MakefileRule rule = (MakefileRule) element;
                    if (rule.getTargets().contains(token.text)) {
                        asyncOpenInEditor(doc, rule.getStartOffset());
                        break;
                    }
                } else if (element.getKind() == MakefileElement.Kind.MACRO && token.kind == MakefileElement.Kind.MACRO) {
                    MakefileMacro macro = (MakefileMacro) element;
                    if (macro.getName().equals(token.text)) {
                        asyncOpenInEditor(doc, macro.getStartOffset());
                        break;
                    }
                }
            }
        } catch (ParseException ex) {
            // tsss, don't tell anybody
        }
    }

    private static final class HyperlinkToken {

        private final String text;
        private final int offset;
        private final MakefileElement.Kind kind;

        private HyperlinkToken(String text, int offset, MakefileElement.Kind kind) {
            this.text = text;
            this.offset = offset;
            this.kind = kind;
        }
    }

    private static void asyncOpenInEditor(final Document doc, final int offset) {
        DataObject dataObject = NbEditorUtilities.getDataObject(doc);
        if (dataObject != null) {
            asyncOpenIdEditor(dataObject, doc, offset);
        }
    }

    private static void asyncOpenInEditor(final FileObject fileObject, final int offset) {
        try {
            DataObject dataObject = DataObject.find(fileObject);
            asyncOpenInEditor(dataObject, offset);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void asyncOpenInEditor(final DataObject dataObject, final int offset) {
        EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
        if (editorCookie != null) {
            try {
                Document doc = editorCookie.openDocument();
                asyncOpenIdEditor(dataObject, doc, offset);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void asyncOpenIdEditor(final DataObject dataObject, final Document doc, final int offset) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
                if (lineCookie != null) {
                    try {
                        int lineIdx = Utilities.getLineOffset((BaseDocument) doc, offset);
                        Line line = lineCookie.getLineSet().getCurrent(lineIdx);
                        line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                    } catch (BadLocationException ex) {
                    }
                }
            }
        });
    }
}
