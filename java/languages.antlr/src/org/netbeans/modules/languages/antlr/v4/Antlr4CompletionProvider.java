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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import org.netbeans.modules.languages.antlr.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;

import org.netbeans.api.annotations.common.StaticResource;

import static org.antlr.parser.antlr4.ANTLRv4Lexer.*;
import org.netbeans.modules.languages.antlr.AntlrParserResult.ReferenceType;
import static org.netbeans.modules.languages.antlr.AntlrTokenSequence.DEFAULT_CHANNEL;

/**
 *
 * @author Laszlo Kishalmi
 */
@MimeRegistration(mimeType = Antlr4Language.MIME_TYPE, service = CompletionProvider.class)
public class Antlr4CompletionProvider implements CompletionProvider {

    @StaticResource
    private static final String ANTLR_ICON = "org/netbeans/modules/languages/antlr/resources/antlr.png";

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new AntlrCompletionQuery(isCaseSensitive()), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static boolean isCaseSensitive() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false);
    }

    private class AntlrCompletionQuery extends AsyncCompletionQuery {

        final boolean caseSensitive;

        public AntlrCompletionQuery(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            AbstractDocument adoc = (AbstractDocument) doc;
            try {
                FileObject fo = EditorDocumentUtils.getFileObject(doc);
                if (fo == null) {
                    return;
                }

                String prefix = "";
                adoc.readLock();
                AntlrTokenSequence tokens;
                try {
                    String text = doc.getText(0, doc.getLength());
                    tokens = new AntlrTokenSequence(new ANTLRv4Lexer(CharStreams.fromString(text)));
                } catch (BadLocationException ex) {
                    return;
                } finally {
                    adoc.readUnlock();
                }

                if (!tokens.isEmpty()) {

                    tokens.seekTo(caretOffset);
                    // Check if we are in a comment (that is filtered out from the token stream)

                    int tokenOffset = tokens.getOffset();
                    if (tokens.hasNext()) {
                        Token nt = tokens.next().get();
                        if (caretOffset > tokenOffset) {
                            // Caret is in a token
                            if ((nt.getChannel() == COMMENT) || (nt.getType() == ACTION_CONTENT)) {
                                // We are in comment or action, no code completion
                                return;
                            }
                            if (nt.getChannel() == DEFAULT_TOKEN_CHANNEL) {
                                prefix = nt.getText().substring(0, caretOffset - tokenOffset);
                            }
                        } else if (nt.getType() == ACTION_CONTENT) {
                            //We are in action
                            return;
                        }
                        tokens.previous();
                        lookAround(fo, tokens, caretOffset, prefix, resultSet);
                    } else {
                        //Empty grammar so far offer lexer, parser and grammar
                        addTokens("", caretOffset, resultSet, "lexer", "parser", "grammar");
                    }
                }
            } finally {
                resultSet.finish();
            }
        }

        private void lookAround(FileObject fo, AntlrTokenSequence tokens, int caretOffset, String prefix, CompletionResultSet resultSet) {
            AntlrParserResult<?> result = AntlrParser.getParserResult(fo);
            AntlrParserResult.GrammarType grammarType = result != null ? result.getGrammarType(): AntlrParserResult.GrammarType.UNKNOWN;
            Optional<Token> opt = tokens.previous(DEFAULT_CHANNEL);
            if (!opt.isPresent()) {
                //At the start of the file;
                Optional<Token> t = tokens.next(DEFAULT_CHANNEL);
                if (t.isPresent() && t.get().getType() != LEXER) {
                    addTokens(prefix, caretOffset, resultSet, "lexer");
                }                
                if (t.isPresent() && t.get().getType() != PARSER) {
                    addTokens(prefix, caretOffset, resultSet, "parser");
                }                
                if (t.isPresent() && (t.get().getType() != LEXER) && (t.get().getType() != GRAMMAR)) {
                    addTokens(prefix, caretOffset, resultSet, "grammar");
                }
                return;
            } else {
                Token pt = opt.get();
                if (((pt.getType() == RULE_REF) || (pt.getType() == TOKEN_REF)) && (caretOffset == pt.getStopIndex() + 1)) {
                    // Could be start of some keywords
                    prefix = pt.getText();
                    opt = tokens.previous(DEFAULT_CHANNEL);
                }
                if (!opt.isPresent()) {
                    addTokens(prefix, caretOffset, resultSet, "lexer", "parser", "grammar");
                    return;
                } else {
                    pt = opt.get();
                    // chack our previous token
                    switch (pt.getType()) {
                        case PARSER:
                        case LEXER:
                            Optional<Token> t = tokens.next(DEFAULT_CHANNEL);
                            if (!t.isPresent() || t.get().getType() != GRAMMAR) {
                                addTokens(prefix, caretOffset, resultSet, "grammar");
                            }
                            return;

                        case SEMI:
                            //Could be the begining of a new rule def.
                            addTokens(prefix, caretOffset, resultSet, "mode", "fragment");
                            return;
                        case RARROW:
                            //Command: offer 'channel', 'skip', etc...
                            addTokens(prefix, caretOffset, resultSet, "skip", "more", "type", "channel", "mode", "pushMode", "popMode");
                            return;
                        case LPAREN:
                            Optional<Token> lexerCommand = tokens.previous(DEFAULT_CHANNEL);
                            // We are not necessary in a lexerCommand here, just taking chances
                            if (lexerCommand.isPresent()) {
                                switch (lexerCommand.get().getText()) {
                                    case "channel": 
                                        addReferences(fo, prefix, caretOffset, resultSet, EnumSet.of(ReferenceType.CHANNEL));
                                        return;
                                    case "mode":
                                    case "pushMode":
                                        addReferences(fo, prefix, caretOffset, resultSet, EnumSet.of(ReferenceType.MODE));
                                        return;
                                    case "type":
                                        addReferences(fo, prefix, caretOffset, resultSet, EnumSet.of(ReferenceType.TOKEN));
                                        return;
                                }
                            }
                            // the fall through is intentional, as of betting on lexerCommand did not come through
                        default:
                            tokens.seekTo(caretOffset);
                            Optional<Token> semi = tokens.previous(SEMI);
                            tokens.seekTo(caretOffset);
                            Optional<Token> colon = tokens.previous(COLON);
                            if (semi.isPresent() && colon.isPresent()
                                    && semi.get().getStartIndex() < colon.get().getStartIndex()) {
                                
                                EnumSet<ReferenceType> rtypes = EnumSet.noneOf(ReferenceType.class);
                                
                                // we are in lexer/parser ruledef
                                Optional<Token> ref = tokens.previous(DEFAULT_CHANNEL);
                                if (ref.isPresent() && (ref.get().getType() == RULE_REF || ref.get().getType() == TOKEN_REF)) {
                                    if (ref.get().getType() == TOKEN_REF) {
                                        rtypes.add(ReferenceType.FRAGMENT);
                                    } else {
                                        rtypes.add(ReferenceType.TOKEN);
                                        rtypes.add(ReferenceType.RULE);
                                        if (grammarType == AntlrParserResult.GrammarType.MIXED) {
                                            rtypes.add(ReferenceType.FRAGMENT);
                                        }
                                    }
                                } else {
                                    // A bit odd definition, let's rely on the grammarType
                                    if ((grammarType == AntlrParserResult.GrammarType.LEXER) || (grammarType == AntlrParserResult.GrammarType.MIXED)) {
                                        rtypes.add(ReferenceType.FRAGMENT);
                                    }
                                    if ((grammarType == AntlrParserResult.GrammarType.PARSER) || (grammarType == AntlrParserResult.GrammarType.MIXED)) {
                                        rtypes.add(ReferenceType.TOKEN);
                                        rtypes.add(ReferenceType.RULE);
                                    }
                                }
                                addReferences(fo, prefix, caretOffset, resultSet, rtypes);
                            }

                    }

                }
            }

        }

        public void addTokens(String prefix, int caretOffset, CompletionResultSet resultSet, String... tokens) {
            String uprefix = caseSensitive ? prefix : prefix.toUpperCase();
            for (String token : tokens) {
                String utoken = caseSensitive ? token : token.toUpperCase();
                if (utoken.startsWith(uprefix)) {
                    CompletionItem item = CompletionUtilities.newCompletionItemBuilder(token)
                            .iconResource(ANTLR_ICON)
                            .startOffset(caretOffset - prefix.length())
                            .leftHtmlText(token)
                            .build();
                    resultSet.addItem(item);
                }
            }
        }

        public void addReferences(FileObject fo, String prefix, int caretOffset, CompletionResultSet resultSet, Set<ReferenceType> rtypes) {
            Set<FileObject> scanned = new HashSet<>();
            Map<String,AntlrParserResult.Reference> matchingRefs = new HashMap<>();
            addReferencesForFile(fo, prefix, matchingRefs, scanned, rtypes);

            int startOffset = caretOffset - prefix.length();
            for (AntlrParserResult.Reference ref : matchingRefs.values()) {
                CompletionItem item = CompletionUtilities.newCompletionItemBuilder(ref.name)
                        .iconResource(getReferenceIcon(ref.type))
                        .startOffset(startOffset)
                        .leftHtmlText(ref.name)
                        .sortText(caseSensitive ? ref.name : ref.name.toUpperCase())
                        .build();
                resultSet.addItem(item);

            }            
        }
        
        public void addReferencesForFile(FileObject fo, String prefix, Map<String,AntlrParserResult.Reference> matching, Set<FileObject> scannedFiles, Set<ReferenceType> rtypes) {
            if (scannedFiles.contains(fo)) {
                return;
            }
            scannedFiles.add(fo);

            
            String mprefix = caseSensitive ? prefix : prefix.toUpperCase();

            AntlrParserResult<?> result = AntlrParser.getParserResult(fo);
            Map<String, AntlrParserResult.Reference> refs = result.references;
            for (AntlrParserResult.Reference ref : refs.values()) {
                String mref = caseSensitive ? ref.name : ref.name.toUpperCase();
                boolean match = mref.startsWith(mprefix);
                if (match && !matching.containsKey(ref) && rtypes.contains(ref.type)) {
                    matching.put(ref.name, ref);
                }
            }

            if (result instanceof Antlr4ParserResult) {
                for (String s : ((Antlr4ParserResult) result).getImports()) {
                    FileObject importedFo = fo.getParent().getFileObject(s, "g4");
                    if (importedFo != null) {
                        addReferencesForFile(importedFo, prefix, matching, scannedFiles, rtypes);
                    }
                }
            }
        }

    }
    
    //The folowing is an excrept of org.netbeans.modules.csl.navigation.Icons
    private static final String ICON_BASE = "org/netbeans/modules/csl/source/resources/icons/";    
    private static String getReferenceIcon(ReferenceType rtype) {
        switch (rtype) {
            case CHANNEL:
                return ICON_BASE + "database.gif";
            case FRAGMENT:
                return ICON_BASE + "constantPublic.png";
            case MODE: 
                return ICON_BASE + "class.png";
            case RULE:
                return ICON_BASE + "rule.png";
            case TOKEN:
                return ICON_BASE + "fieldPublic.png";
        }
        return null;
    }
}
