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
package org.netbeans.modules.html.editor.indexing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.indexing.api.CssIndexModel;
import org.netbeans.modules.css.indexing.api.CssIndexModelFactory;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.web.common.api.Constants;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * HtmlCssIndexContributor uses the CssIndexModel construct to hook into the
 * css indexer process and add custom elements to the index.
 *
 * From the HTML class and id attributes are added.
 */
public class HtmlCssIndexContributor extends CssIndexModel {

    // Identical to CssIndexer#CLASSES_KEY, CssIndexer#IDS_KEY and
    // CssIndexer#VIRTUAL_ELEMENT_MARKER copied as defining class is not
    // exported
    public static final String CLASSES_KEY = "classes"; //NOI18N
    public static final String IDS_KEY = "ids"; //NOI18N
    public static final char VIRTUAL_ELEMENT_MARKER = '!'; //NOI18N

    private static final Collection<String> INDEX_KEYS = Arrays.asList(new String[]{});

    private Set<String> classes = new HashSet<>();
    private Set<String> ids = new HashSet<>();

    public HtmlCssIndexContributor(CssParserResult crp) {
        try {
            ParserManager.parse(Collections.singleton(crp.getSnapshot().getSource()), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    indexHTML(resultIterator);
                }

                private void indexHTML(ResultIterator resultIterator) {
                    try {
                        Result pr = resultIterator.getParserResult();
                        // HtmlParserResult is a lookup provider - a bit strange
                        if(pr instanceof Lookup.Provider &&  ((Lookup.Provider) pr).getLookup().lookup(SyntaxAnalyzerResult.class) != null) {
                            TokenHierarchy<?> th = resultIterator.getSnapshot().getTokenHierarchy();
                            TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
                            while(ts.moveNext()) {
                                Token t = ts.token();
                                if (t.id() == HTMLTokenId.VALUE_CSS) {
                                    String cssTokenType = (String) t.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                                    String value = WebUtils.unquotedValue(t.text())
                                            .replace(Constants.LANGUAGE_SNIPPET_SEPARATOR, " ");
                                    if (Objects.equals(cssTokenType, HTMLTokenId.VALUE_CSS_TOKEN_TYPE_ID)) {
                                        if(value != null && (! value.trim().isEmpty())) {
                                            ids.add(value);
                                        }
                                    } else if(Objects.equals(cssTokenType, HTMLTokenId.VALUE_CSS_TOKEN_TYPE_CLASS)) {
                                        for(String clazz: value.split("\\s+")) {
                                            if(clazz != null && (! clazz.trim().isEmpty())) {
                                                classes.add(clazz);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    for(Embedding e: resultIterator.getEmbeddings()) {
                        indexHTML(resultIterator.getResultIterator(e));
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    @Override
    public void storeToIndex(IndexDocument document) {
        for(String id: ids) {
            document.addPair(IDS_KEY, id + VIRTUAL_ELEMENT_MARKER, true, true);
        }
        for(String clazz: classes) {
            document.addPair(CLASSES_KEY, clazz + VIRTUAL_ELEMENT_MARKER, true, true);
        }
    }

    @ServiceProvider(service = CssIndexModelFactory.class)
    public static final class Factory extends CssIndexModelFactory{

        @Override
        public HtmlCssIndexContributor getModel(CssParserResult result) {
            return new HtmlCssIndexContributor(result);
        }

        @Override
        public HtmlCssIndexContributor loadFromIndex(IndexResult result) {
            return new HtmlCssIndexContributor(null);
        }

        @Override
        public Collection<String> getIndexKeys() {
            return INDEX_KEYS;
        }

    }

}
