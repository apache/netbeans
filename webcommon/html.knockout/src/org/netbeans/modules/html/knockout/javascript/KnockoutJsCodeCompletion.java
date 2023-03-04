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
package org.netbeans.modules.html.knockout.javascript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import static org.netbeans.modules.html.knockout.javascript.KnockoutContext.COMPONENT_CONF_EMPTY;
import static org.netbeans.modules.html.knockout.javascript.KnockoutContext.COMPONENT_CONF_PARAMS;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement;
import org.netbeans.modules.javascript2.knockout.index.KnockoutIndex;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Roman Svitanic
 */
@CompletionProvider.Registration(priority = 7)
public class KnockoutJsCodeCompletion implements CompletionProvider {

    private static final String MIMETYPE = "text/html/text/javascript"; //NOI18N
    private static final String PROP_NAME = "name"; //NOI18N
    private static final String PROP_PARAMS = "params"; //NOI18N

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        if (!MIMETYPE.equals(ccContext.getParserResult().getSnapshot().getMimePath().getPath())) {
            // we are interested only in html documets
            return Collections.emptyList();
        }
        Document document = ccContext.getParserResult().getSnapshot().getSource().getDocument(true);
        int dOffset = ccContext.getCaretOffset();  // document offset
        ((AbstractDocument) document).readLock();
        try {
            List<CompletionProposal> result = new ArrayList<>();
            KnockoutContext koContext = KnockoutContext.findContext(document, dOffset);
            switch (koContext) {
                case COMPONENT_EMPTY:
                case COMPONENT_CONF_NAME:
                    result.addAll(findComponentNames(ccContext));
                    break;
                case COMPONENT_CONF_EMPTY:
                    if (prefix.isEmpty() || PROP_NAME.startsWith(prefix)) {
                        result.add(new KnockoutCodeCompletionItem.KOComponentOptionItem(
                                new KnockoutJsElement(PROP_NAME, ElementKind.PROPERTY), ccContext));
                    }
                    if (prefix.isEmpty() || PROP_PARAMS.startsWith(prefix)) {
                        result.add(new KnockoutCodeCompletionItem.KOComponentOptionConfigItem(
                                new KnockoutJsElement(PROP_PARAMS, ElementKind.PROPERTY), ccContext));
                    }
                    break;
                case COMPONENT_CONF_PARAMS:
                    if (prefix.isEmpty() || PROP_PARAMS.startsWith(prefix)) {
                        result.add(new KnockoutCodeCompletionItem.KOComponentOptionConfigItem(
                                new KnockoutJsElement(PROP_PARAMS, ElementKind.PROPERTY), ccContext));
                    }
                    break;
                case COMPONENT_CONF_PARAMS_VALUE:
                    String componentName = findComponentName(document, dOffset);
                    if (componentName != null) {
                        result.addAll(findComponentParameters(ccContext, componentName));
                    }
                    break;
            }
            return result;
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        return null;
    }

    private Collection<? extends CompletionProposal> findComponentNames(CodeCompletionContext ccContext) {
        FileObject fo = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return Collections.emptyList();
        }
        Collection<CompletionProposal> result = new ArrayList<>();
        KnockoutIndex knockoutIndex = null;
        try {
            knockoutIndex = KnockoutIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (knockoutIndex != null) {
            Collection<KnockoutCustomElement> customElements = knockoutIndex.getCustomElements(ccContext.getPrefix(), false);
            List<String> componentNames = new ArrayList<>(); //list of names of custom KO components, to prevent duplicates in CC
            if (customElements != null) {
                for (KnockoutCustomElement kce : customElements) {
                    String name = kce.getName();
                    ElementHandle element = new KnockoutJsElement(name, ElementKind.CLASS);
                    if ((ccContext.getPrefix().isEmpty() || name.startsWith(ccContext.getPrefix()))
                            && !componentNames.contains(name)) {
                        // we don't have this component yet, add it now
                        result.add(new KnockoutCodeCompletionItem.KOComponentItem(element, ccContext));
                        componentNames.add(name);
                    }
                }
            }
        }
        return result;
    }

    private Collection<? extends CompletionProposal> findComponentParameters(CodeCompletionContext ccContext, String componentName) {
        FileObject fo = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return Collections.emptyList();
        }
        Collection<CompletionProposal> result = new ArrayList<>();
        KnockoutIndex knockoutIndex = null;
        try {
            knockoutIndex = KnockoutIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (knockoutIndex != null) {
            Collection<String> parameters = knockoutIndex.getCustomElementParameters(componentName);
            for (String param : parameters) {
                if (param.startsWith(ccContext.getPrefix())) {
                    ElementHandle element = new KnockoutJsElement(param, ElementKind.PROPERTY);
                    result.add(new KnockoutCodeCompletionItem.KOComponentOptionItem(element, ccContext));
                }
            }
        }
        return result;
    }

    private String findComponentName(Document document, int offset) {
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<JsTokenId> jsTs = LexerUtils.getTokenSequence(th, offset, JsTokenId.javascriptLanguage(), false);
        if (jsTs != null) {
            int diff = jsTs.move(offset);
            if (diff == 0 && jsTs.movePrevious() || jsTs.moveNext()) {
                // move backwards and find the begining (char '{') of params configuration object
                Token<JsTokenId> jsToken = LexerUtils.followsToken(jsTs,
                        Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY),
                        true, false, true,
                        JsTokenId.WHITESPACE,
                        JsTokenId.STRING, JsTokenId.STRING_BEGIN, JsTokenId.STRING_END,
                        JsTokenId.NUMBER, JsTokenId.IDENTIFIER,
                        JsTokenId.OPERATOR_COLON, JsTokenId.OPERATOR_COMMA);
                if (jsToken != null && jsToken.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                    // we have found the begining, now let's find the 'params' identifier
                    jsToken = LexerUtils.followsToken(jsTs, JsTokenId.IDENTIFIER, true, false,
                            JsTokenId.WHITESPACE, JsTokenId.STRING, JsTokenId.STRING_BEGIN, JsTokenId.STRING_END, JsTokenId.OPERATOR_COLON);
                    if (jsToken != null && jsToken.id() == JsTokenId.IDENTIFIER && jsToken.text().toString().equals(PROP_PARAMS)) {
                        // now find 'name' identifier
                        jsToken = LexerUtils.followsToken(jsTs, JsTokenId.IDENTIFIER, true, false,
                                JsTokenId.WHITESPACE,
                                JsTokenId.STRING, JsTokenId.STRING_BEGIN, JsTokenId.STRING_END,
                                JsTokenId.OPERATOR_COLON, JsTokenId.OPERATOR_COMMA);
                        if (jsToken != null && jsToken.id() == JsTokenId.IDENTIFIER && jsToken.text().toString().equals(PROP_NAME)) {
                            // and now finally move forward to find the string value representing component name
                            jsToken = LexerUtils.followsToken(jsTs,
                                    JsTokenId.STRING,
                                    false, false,
                                    JsTokenId.WHITESPACE, JsTokenId.STRING_BEGIN, JsTokenId.OPERATOR_COLON);
                            return jsToken.text().toString();
                        }
                    }
                }
            }
        }
        return null;
    }
}
