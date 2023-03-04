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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.Constants;
import org.netbeans.modules.web.common.api.WebUtils;

/**
 * Creates CSS virtual source for html sources.
 *
 * @author Marek Fukala
 */
public class CssHtmlTranslator implements CssEmbeddingProvider.Translator {

    private static final Logger LOGGER = Logger.getLogger(CssHtmlTranslator.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    public static final String CSS_MIME_TYPE = "text/css"; //NOI18N
    public static final String HTML_MIME_TYPE = "text/html"; //NOI18N

    private static final Pattern CLASSES_LIST_PATTERN = Pattern.compile("[^\\s,]*"); //splits by whitespaces and comma //NOI18N
    static final Pattern CDATA_FILTER_PATTERN = Pattern.compile(".*<!\\[CDATA\\[\\s*(\\*/)?\\s*(<!--)?(.*?)(-->)?\\s*(/\\*)?\\s*]]>.*", Pattern.DOTALL | Pattern.MULTILINE);
    static final int CDATA_BODY_GROUP_INDEX = 3; //                                                  ^^^^

    static final Pattern ILLEGAL_CHARS_IN_SELECTOR = Pattern.compile("[#/{}\\.:\\[\\]]");

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (snapshot == null) {
            return Collections.emptyList();
        }
        TokenHierarchy th = snapshot.getTokenHierarchy();
        if(th == null) {
            //no lexer language for the snapshot's mimetype???
            return Collections.emptyList();
        }
        TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
        HashMap<String, Object> state = new HashMap<>(6);
        List<Embedding> embeddings = new ArrayList<>();
        extractCssFromHTML(snapshot, ts, state, embeddings);
        return embeddings;
    }
    //internal state names for the html code analyzer
    protected static final String END_OF_LAST_SEQUENCE = "end_of_last_sequence"; //NOI18N
    protected static final String IN_STYLE = "in_style"; //NOI18N
    protected static final String IN_INLINED_STYLE = "in_inlined_style"; //NOI18N
    protected static final String CURRENT_TAG = "current_tag"; //NOI18N
    protected static final String CURRENT_ATTR = "current_attr"; //NOI18N
    private static final String QUTE_CUT = "quote_cut"; //NOI18N
    //TODO rewrite the whole embedding provider to the parser based version so
    //we do not have to parse what's already parsed - like the <link ... /> tag
    private static final String LINK_TAG_NAME = "link"; //NOI18N
    private static final String HREF_ATTR_NAME = "href"; //NOI18N
    private static final String HREF_ATTR_REL = "rel"; //NOI18N
    private static final String HREF_ATTR_TYPE = "type"; //NOI18N

    /**
     * @param ts An HTML token sequence always positioned at the beginning.
     */
    protected void extractCssFromHTML(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, HashMap<String, Object> state, List<Embedding> embeddings) {
        while (ts.moveNext()) {
            Token<HTMLTokenId> htmlToken = ts.token();
            HTMLTokenId htmlId = htmlToken.id();
            if (htmlId == HTMLTokenId.STYLE) {
                state.put(IN_STYLE, Boolean.TRUE);
                //jumped into style
                int sourceStart = ts.offset();
                int length = htmlToken.length();

                //filter out <![CDATA ]]> tokens and <!-- --> comment tokens, the code may looks like:
                //    <head>
                //      <style type="text/css"><![CDATA[
                //      <!--
                //      body { }
                //      }
                //      -->
                //      ]]></style>
                //    </head><body/>
                //  </html>
                Matcher matcher = CDATA_FILTER_PATTERN.matcher(htmlToken.text());
                if (matcher.matches()) {
                    sourceStart += matcher.start(CDATA_BODY_GROUP_INDEX);
                    length = matcher.end(CDATA_BODY_GROUP_INDEX) - matcher.start(CDATA_BODY_GROUP_INDEX);
                }

                embeddings.add(snapshot.create(sourceStart, length, CSS_MIME_TYPE));
            } else {
                //jumped out of the style
                state.remove(IN_STYLE);

                if (state.get(IN_INLINED_STYLE) != null) {
                    switch (htmlId) {
                        case VALUE_CSS:
                            //continuation of the html style attribute value after templating
                            int sourceStart = ts.offset();
                            CharSequence text = htmlToken.text();
                            int tokenLength = htmlToken.length();
                            if (CharSequenceUtilities.endsWith(text, "\"") || CharSequenceUtilities.endsWith(text, "'")) {
                                tokenLength--;
                            }
                            embeddings.add(snapshot.create(sourceStart, tokenLength, CSS_MIME_TYPE));
                            break;
                        case EL_OPEN_DELIMITER:
                        case EL_CLOSE_DELIMITER:
                            //ignore
                            break;
                        case EL_CONTENT:
                            //generate templating mark
                            embeddings.add(snapshot.create(Constants.LANGUAGE_SNIPPET_SEPARATOR, CSS_MIME_TYPE));
                            break;
                        default:
                            //out of the css value -- close the virtual selector
                            state.remove(IN_INLINED_STYLE);
                            state.remove(QUTE_CUT);
                            embeddings.add(snapshot.create(";\n}\n", CSS_MIME_TYPE));
                    }
                    continue; //process next token
                }

                if (htmlId == HTMLTokenId.TAG_OPEN) {
                    //remember we are in a tag
                    state.put(CURRENT_TAG, htmlToken.text().toString());
                } else if (htmlId == HTMLTokenId.TAG_CLOSE_SYMBOL || htmlId == HTMLTokenId.TEXT) {
                    //out of a tag
                    state.remove(CURRENT_TAG);
                } else if (htmlId == HTMLTokenId.ARGUMENT) {
                    state.put(CURRENT_ATTR, htmlToken.text().toString());
                } else if (htmlId == HTMLTokenId.VALUE) {
                    String currentTag = (String) state.get(CURRENT_TAG);
                    String currentAttr = (String) state.get(CURRENT_ATTR);

                    if (currentTag == null || currentAttr == null) {
                        continue; //should not happen, if so ignore this token
                    }

                    boolean isLinkTag = LINK_TAG_NAME.equalsIgnoreCase(currentTag.toLowerCase(Locale.ENGLISH));
                    boolean isHrefAttr = HREF_ATTR_NAME.equals(currentAttr.toLowerCase(Locale.ENGLISH));

                    if (isLinkTag && isHrefAttr) {
                        String unquotedValue = WebUtils.unquotedValue(htmlToken.text().toString().toString());
                        //found href value, generate virtual css import
                        StringBuilder buf = new StringBuilder();
                        buf.append("@import \""); //NOI18N
                        buf.append(unquotedValue);
                        buf.append("\";"); //NOI18N
                        //insert the import at the beginning of the virtual source
                        embeddings.add(0, snapshot.create(buf, CSS_MIME_TYPE));
                    }
                } else if (htmlId == HTMLTokenId.VALUE_CSS) {
                    //found inlined css
                    int sourceStart = ts.offset();
                    String text = htmlToken.text().toString();

                    if (text.startsWith("\"") || text.startsWith("'")) {
                        sourceStart++;
                        text = text.substring(1);
                    }

                    int sourceEnd = sourceStart + text.length();
                    if (text.endsWith("\"") || text.endsWith("'")) {
                        sourceEnd--;
                        state.put(QUTE_CUT, Boolean.TRUE);
                        text = text.substring(0, text.length() - 1);
                    }

                    //determine the inlined css type
                    String valueCssType = (String) htmlToken.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                    if (valueCssType != null) {
                        //in ID or CLASS html attributes

                        //XXX we do not support templating code in the value!
                        //class or id attribute value - generate fake selector with # or . prefix
                        //#180576 - filter out "illegal" characters from the selector name
                        if (!ILLEGAL_CHARS_IN_SELECTOR.matcher(text).find()) {
                            embeddings.add(snapshot.create("\n ", CSS_MIME_TYPE)); //NOI18N

                            boolean isClassElement = HTMLTokenId.VALUE_CSS_TOKEN_TYPE_CLASS.equals(valueCssType);
                            String prefix = isClassElement ? " ." : " #";
                            Matcher matcher = CLASSES_LIST_PATTERN.matcher(text);
                            boolean elementExists = false;
                            int last_class_name_end_offset = -1;
                            while (matcher.find()) {
                                int start = matcher.start();
                                int end = matcher.end();
                                if (start != end) {
                                    embeddings.add(snapshot.create(prefix, CSS_MIME_TYPE)); //NOI18N

//                                    //escape slash char if present - Bug 216489 - Attribute 'id' shows an error with forward slash in it
//                                    int mark = start;
//                                    for(int i = start; i < end; i++) {
//                                        char c = text.charAt(i);
//                                        if(c == '/' || c == '$') {
//                                            //create document embedding for the prefix part
//                                            embeddings.add(snapshot.create(sourceStart + i, i - sourceStart, CSS_MIME_TYPE));
//
//                                            embeddings.add(snapshot.create("/", CSS_MIME_TYPE));
//
//                                        }
//                                    }
//
                                    //compute the token's document offset
                                    int start_in_document = sourceStart + start;
                                    int length = end - start;

                                    //create the real text embedding
                                    embeddings.add(snapshot.create(start_in_document, length, CSS_MIME_TYPE));

                                    elementExists = true;
                                    last_class_name_end_offset = start_in_document + length;
                                }
                            }

                            if (elementExists) {
                                if (isClassElement) {
                                    //check if there's a whitespace after the last class and if so, add it to the virtual
                                    //source so completion can work at <div class="foo |"/>
                                    //HOWEVER this fix will only work of the caret is exactly one char after the last
                                    //class name end, if further, completion for html element selectors will appear
                                    //To fix this I'd likely need to completely redone the class/id completion so it doesn't
                                    //use the normal css completion but some special html based completion taking the items
                                    //from css index.
                                    if (last_class_name_end_offset < sourceEnd) {
                                        embeddings.add(snapshot.create(prefix, CSS_MIME_TYPE)); //NOI18N
                                        embeddings.add(snapshot.create(last_class_name_end_offset + 1, 0, CSS_MIME_TYPE));
                                    }
                                }
                            } else {
                                //empty class attribute, we need to generate . {} so the completion can complete
                                //classes after the dot
                                embeddings.add(snapshot.create(prefix, CSS_MIME_TYPE)); //NOI18N
                                //+ empty real embedding for the empty value "" content
                                embeddings.add(snapshot.create(sourceStart, 0, CSS_MIME_TYPE));
                            }

                            embeddings.add(snapshot.create("{}", CSS_MIME_TYPE));
                        }

                    } else {
                        //style attribute value (inilined css code) - wrap with a fake selector
                        embeddings.add(snapshot.create("\n SELECTOR {\n\t", CSS_MIME_TYPE));
                        embeddings.add(snapshot.create(sourceStart, sourceEnd - sourceStart, CSS_MIME_TYPE));

                        state.put(IN_INLINED_STYLE, Boolean.TRUE);
                    }

                }
            }
        }
    }
}
