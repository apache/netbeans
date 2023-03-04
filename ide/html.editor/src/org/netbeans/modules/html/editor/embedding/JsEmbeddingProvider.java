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
package org.netbeans.modules.html.editor.embedding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;

/**
 *
 * @author marekfukala
 */
@EmbeddingProvider.Registration(
        mimeType = "text/html",
        targetMimeType = "text/javascript")
public class JsEmbeddingProvider extends EmbeddingProvider {

    private static final Logger LOGGER = Logger.getLogger(JsEmbeddingProvider.class.getSimpleName());
    private static final String JS_MIMETYPE = "text/javascript"; //NOI18N
    private static final String BABEL_MIMETYPE = "text/babel"; //NOI18N
    private static final String SCRIPT_TYPE_MODULE = "module"; //NOI18N
    private static final String NETBEANS_IMPORT_FILE = "__netbeans_import__"; // NOI18N
    private boolean cancelled = true;
    private final Language JS_LANGUAGE;
    private final JsEPPluginQuery PLUGINS;
    
    private static final Pattern GENERIC_MARK_PATTERN = Pattern.compile("@@@"); //NOI18N
    private static final String GENERATED_JS_IDENTIFIER = "__UNKNOWN__"; // NOI18N
    
    /** Files with mime types defined in this collection will use transitional
     * javascript embedded source creation. 
     * 
     * This means that first html embedded source will be created from the top level
     * language and then from this embedded html source an embedded javascript source
     * will be created.
     */
    private static final Collection<String> TEMPLATING_LANGUAGES_USING_TRANSITIONAL_EMBEDDING_CREATION 
            = new HashSet<>(Arrays.asList(new String[]{
                "text/x-jsp", "text/x-tag", "text/xhtml", "text/x-php5" //NOI18N
            }));

    public JsEmbeddingProvider() {
        JS_LANGUAGE = Language.find(JS_MIMETYPE); //NOI18N
        PLUGINS = JsEPPluginQuery.getDefault();
    }
    
    @Override
    public List<Embedding> getEmbeddings(final Snapshot snapshot) {
        String rootMimeType = snapshot.getMimePath().getMimeType(0);
        if (snapshot.getMimePath().size() > 1 
                && !TEMPLATING_LANGUAGES_USING_TRANSITIONAL_EMBEDDING_CREATION.contains(rootMimeType)) {
            //do not create any js embeddings in already embedded html code
            //another js embedding provider for such cases exists in 
            //javascript2.editor module.
            return Collections.emptyList();
        }

        cancelled = false; //resume
        final List<Embedding> embeddings = new ArrayList<>();
        final TokenSequence<HTMLTokenId> tokenSequence = snapshot.getTokenHierarchy().tokenSequence(HTMLTokenId.language());
        final JsAnalyzerState state = new JsAnalyzerState();
        
        try {
        ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator htmlRI = WebUtils.getResultIterator(resultIterator, "text/html");
                if(htmlRI != null) {
                    HtmlParserResult result = (HtmlParserResult)htmlRI.getParserResult();
                    if(result != null) {
                        process(result, snapshot, tokenSequence, state, embeddings);
                    } else {
                        //likely a bug in parsing.api
                        //https://netbeans.org/bugzilla/show_bug.cgi?id=233926
                    }
                }
            }
        });
        
        } catch (ParseException pe) {
            LOGGER.log(Level.WARNING, null, pe);
        }
        
        if (embeddings.isEmpty()) {
            LOGGER.log(Level.FINE, "No javascript embedding created for source {0}", //NOI18N
                    snapshot.getSource().toString());
            return Collections.<Embedding>emptyList();
        } else {
            Embedding embedding = Embedding.create(embeddings);
            LOGGER.log(Level.FINE, "Javascript embedding for source {0}:\n{1}",
                    new Object[]{snapshot.getSource().toString(), embedding.getSnapshot().getText().toString()});
            return Collections.singletonList(embedding);

        }
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    private void process(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> ts, JsAnalyzerState state, List<Embedding> embeddings) {
        assert parserResult != null;
        
        JsEPPluginQuery.Session session = PLUGINS.createSession();
        session.startProcessing(parserResult, snapshot, ts, embeddings);
        try {
            ts.moveStart();

            while (ts.moveNext()) {
                if (cancelled) {
                    embeddings.clear();
                    return;
                }

                //plugins
                if (session.processToken()) {
                    //the plugin already processed the token so we should? not? process it anymore ... that's a question
                    continue;
                }

                Token<HTMLTokenId> token = ts.token();
                switch (token.id()) {
                    case SCRIPT:
                        handleScript(snapshot, ts, state, embeddings);
                        break;
                    case TAG_OPEN:
                        handleOpenTag(snapshot, ts, embeddings);
                        break;
                    case TEXT:
                        if (state.in_javascript) {
                            embeddings.addAll(createEmbedding(snapshot, ts.offset(), token.length()));
                        }
                        break;
                    case VALUE_JAVASCRIPT:
                    case VALUE:
                        handleValue(snapshot, ts, embeddings);
                        break;
                    case TAG_CLOSE:
                        if (LexerUtils.equals("script", token.text(), true, true)) {
                            embeddings.addAll(createEmbedding(snapshot, "\n")); //NOI18N
                        }
                        break;
                    default:
                        state.in_javascript = false;
                        break;
                }
            }
        } finally {
            session.endProcessing();
        }
    }

    //VALUE_JAVASCRIPT token always has text/javascript embedding
    //VALUE token MAY have text/javascript embedding (provided by HtmlLexerPlugin) or by dynamic embedding creation
    private void handleValue(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
        if (ts.embedded(JS_LANGUAGE) != null) {
            //has javascript embedding
            embeddings.addAll(createEmbedding(snapshot, "(function(){\n")); //NOI18N
            int diff = Utils.isAttributeValueQuoted(ts.token().text()) ? 1 : 0;
            embeddings.addAll(createEmbedding(snapshot, ts.offset() + diff, ts.token().length() - diff * 2));
            embeddings.addAll(createEmbedding(snapshot, ";\n});\n")); //NOI18N
        }
    }

    private void handleScript(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, JsAnalyzerState state, List<Embedding> embeddings) {
        String scriptType = (String) ts.token().getProperty(HTMLTokenId.SCRIPT_TYPE_TOKEN_PROPERTY);
        if (isValidScriptTypeAttributeValue(scriptType)) {
            state.in_javascript = true;
            // Emit the block verbatim
            int sourceStart = ts.offset();
            String text = ts.token().text().toString();
            List<EmbeddingPosition> jsEmbeddings = extractJsEmbeddings(text, sourceStart);
            for (EmbeddingPosition embedding : jsEmbeddings) {
                embeddings.addAll(createEmbedding(snapshot, embedding.getOffset(), embedding.getLength()));
            }
        }
    }

    private boolean isValidScriptTypeAttributeValue(String scriptType) {
        return scriptType == null
                || JS_MIMETYPE.equals(scriptType)
                || BABEL_MIMETYPE.equals(scriptType)
                || SCRIPT_TYPE_MODULE.equals(scriptType);
    }

    private void handleOpenTag(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
        // TODO - if we see a <script src="someurl"> block that also
        // has a nonempty body, warn - the body will be ignored!!
        // (This should be a quickfix)
        if (LexerUtils.equals("script", ts.token().text(), false, false)) {
            // Look for "<script src=" and if found, locate any includes.
            // Quit when I find TAG_CLOSE or run out of tokens
            // (for files with errors)
            TokenSequence<? extends HTMLTokenId> ets = ts.subSequence(ts.offset());
            ets.moveStart();
            boolean foundSrc = false;
            boolean foundType = false;
            String type = null;
            String src = null;
            while (ets.moveNext()) {
                Token<? extends HTMLTokenId> t = ets.token();
                HTMLTokenId id = t.id();
                // TODO - if we see a DEFER attribute here record that somehow
                // such that I can have a quickfix look to make sure you don't try
                // to mess with the document!
                if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    break;
                } else if (foundSrc || foundType) {
                    if (id == HTMLTokenId.ARGUMENT) {
                        break;
                    } else if (id == HTMLTokenId.VALUE) {
                        // Found a script src
                        if (foundSrc) {
                            src = t.text().toString();
                        } else {
                            assert foundType;
                            type = t.text().toString();
                        }
                        foundSrc = false;
                        foundType = false;
                    }
                } else if (id == HTMLTokenId.ARGUMENT) {
                    String val = t.text().toString();
                    switch (val) {
                        case "src":
                            foundSrc = true;
                            break;
                        case "type":
                            foundType = true;
                            break;
                    }
                }
            }
            if (src != null) {
                if (type == null || type.toLowerCase().indexOf("javascript") != -1) {
                    if (src.length() > 2 && src.startsWith("\"") && src.endsWith("\"")) {
                        src = src.substring(1, src.length() - 1);
                    }
                    if (src.length() > 2 && src.startsWith("'") && src.endsWith("'")) {
                        src = src.substring(1, src.length() - 1);
                    }

                    // Insert a file link
                    String insertText = NETBEANS_IMPORT_FILE + "('" + src + "');\n"; // NOI18N
                    embeddings.addAll(createEmbedding(snapshot, insertText));
                }
            }
        }

    }

    private List<EmbeddingPosition> extractJsEmbeddings(String text, int sourceStart) {
        List<EmbeddingPosition> embeddings = new LinkedList<>();
        // beginning comment around the script
        int start = 0;
        for (; start < text.length(); start++) {
            char c = text.charAt(start);
            if (!Character.isWhitespace(c)) {
                break;
            }
        }
        if (start < text.length() && text.startsWith("<!--", start)) { //NOI18N
            int lineEnd = text.indexOf('\n', start); //NOI18N
            if (isHtmlCommentStartToSkip(text, start, lineEnd)) {
                if (start > 0) {
                    embeddings.add(new EmbeddingPosition(sourceStart, start));
                }
                lineEnd++; //skip the \n
                sourceStart += lineEnd;
                text = text.substring(lineEnd);
                // need to look at the end of the text, whether there is no -->
                int end = text.length() - 1;
                while(end > -1 && Character.isWhitespace(text.charAt(end))) {
                    end--;
                }
                if (end > 4) {
                    int index = text.indexOf("-->", end - 4);
                    if (index != -1) { //NOI18N
                        String helpText = text.substring(0, index);
                        if (helpText.lastIndexOf("<!--") <= helpText.lastIndexOf("-->")) { //NOI18N
                            text = helpText;
                        }
                    }
                }
            }
        }
        // inline comments inside script
        Scanner scanner = new Scanner(text).useDelimiter("(<!--).*(-->)"); //NOI18N
        while (scanner.hasNext()) {
            scanner.next();
            MatchResult match = scanner.match();
            embeddings.add(new EmbeddingPosition(sourceStart + match.start(), match.group().length()));
        }
        return embeddings;
    }

    private boolean isHtmlCommentStartToSkip(String text, int start, int lineEnd) {
        if (lineEnd != -1) {
            // issue #223883 - one of suggested constructs: http://lachy.id.au/log/2005/05/script-comments (Example 4)
            if (text.startsWith("<!--//-->", start)) { //NOI18N
                return true;
            } else {
                //    embedded delimiter - issue #217081 || one line comment - issue #223883
                return (text.indexOf("-->", start) == -1 || lineEnd < text.indexOf("-->", start)); //NOI18N
            }
        } else {
            return false;
        }
    }
    
    /* replace all @@@ marks by the fake javascript ident */
    private static Collection<Embedding> createEmbedding(Snapshot snapshot, CharSequence text) {
        String replaced = GENERIC_MARK_PATTERN.matcher(text).replaceAll(GENERATED_JS_IDENTIFIER);
        return Collections.singleton(snapshot.create(replaced, JS_MIMETYPE));
    }
    
    /* replace all @@@ marks by the fake javascript ident */
    private Collection<Embedding> createEmbedding(Snapshot snapshot, int offset, int len) {
        Collection<Embedding> es = new ArrayList<>();
        CharSequence text = snapshot.getText().subSequence(offset, offset + len);
        Matcher matcher = GENERIC_MARK_PATTERN.matcher(text);
        int tmpOffset = 0;
        while(matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if(start != end) {
                //create embedding from the original
                es.add(snapshot.create(offset + tmpOffset, start - tmpOffset, JS_MIMETYPE));
                tmpOffset = end;
                if(!matcher.hitEnd()) {
                    //follows the delimiter - @@@ - convert it to the GENERATED_JS_IDENTIFIER
                    es.add(snapshot.create(GENERATED_JS_IDENTIFIER, JS_MIMETYPE));
                }
            }
        }
        es.add(snapshot.create(offset + tmpOffset, text.length() - tmpOffset, JS_MIMETYPE));
        return es;
    }
    
    private static final class JsAnalyzerState {

        boolean in_javascript = false;
    }

    protected static final class EmbeddingPosition {

        private final int offset;
        private final int length;

        public EmbeddingPosition(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return offset;
        }
    }
}
