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
package org.netbeans.modules.php.blade.editor.completion.providers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.blade.editor.FileSystemUtils;
import static org.netbeans.modules.php.blade.editor.ResourceUtilities.CUSTOM_HTML_ICON;
import static org.netbeans.modules.php.blade.editor.ResourceUtilities.XML_ATTRIBUTE_ICON;
import org.netbeans.modules.php.blade.editor.components.ComponentModel;
import org.netbeans.modules.php.blade.editor.components.ComponentsQueryService;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.project.ComponentsSupport;
import org.netbeans.modules.php.blade.syntax.BladeTagsUtils;
import org.netbeans.modules.php.blade.syntax.StringUtils;
import org.netbeans.modules.php.blade.syntax.antlr4.html_components.BladeHtmlAntlrLexer;
import org.netbeans.modules.php.blade.syntax.antlr4.html_components.BladeHtmlAntlrUtils;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bhaidu
 */
@MimeRegistrations(value = {
    @MimeRegistration(mimeType = "text/html", service = CompletionProvider.class),
    @MimeRegistration(mimeType = BladeLanguage.MIME_TYPE, service = CompletionProvider.class)
})
public class BladeHtmlCompletionProvider implements CompletionProvider {

    private static final Logger LOGGER = Logger.getLogger(BladeHtmlCompletionProvider.class.getName());

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new BladeCompletionQuery(), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        FileObject fo = EditorDocumentUtils.getFileObject(component.getDocument());
        if (fo == null || !fo.getMIMEType().equals(BladeLanguage.MIME_TYPE)) {
            return 0;
        }

        if (typedText.length() == 0) {
            return 0;
        }

        //don't autocomplete on space, \n, )
        if (typedText.trim().isEmpty()) {
            return 0;
        }

        char lastChar = typedText.charAt(typedText.length() - 1);
        switch (lastChar) {
            case ')':
            case '\n':
            case '<':
            case '>':
                return 0;
        }
        return COMPLETION_QUERY_TYPE;
    }

    private class BladeCompletionQuery extends AsyncCompletionQuery {

        public BladeCompletionQuery() {
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            long startTime = System.currentTimeMillis();
            AbstractDocument adoc = (AbstractDocument) doc;
            try {
                FileObject fo = EditorDocumentUtils.getFileObject(doc);

                if (fo == null || !fo.getMIMEType().equals(BladeLanguage.MIME_TYPE)) {
                    return;
                }

                adoc.readLock();
                AntlrTokenSequence tokens;
                String typedText;
                try {
                    String text = doc.getText(0, doc.getLength());
                    typedText = doc.getText(caretOffset - 1, 1);
                    tokens = new AntlrTokenSequence(new BladeHtmlAntlrLexer(CharStreams.fromString(text)));
                } catch (BadLocationException ex) {
                    return;
                } finally {
                    adoc.readUnlock();
                }

                if (!tokens.isEmpty()) {
                    tokens.seekTo(caretOffset);
                    Token queryToken;

                    if (tokens.hasNext()) {
                        queryToken = tokens.next().get();
                    } else if (tokens.hasPrevious()) {
                        queryToken = tokens.previous().get();
                    } else {
                        return;
                    }

                    int queryTokenOffset = queryToken.getStartIndex();

                    //check for context where we have inline css code or offseted blade tags
                    if (queryTokenOffset > caretOffset) {
                        int correction = typedText.equals("!") //NOI18N
                                || typedText.equals("}")  ? 0 : 1; //NOI18N
                        tokens.seekTo(caretOffset - correction);
                        queryToken = tokens.next().get();
                        queryTokenOffset = queryToken.getStartIndex();
                    }

                    String queryText = queryToken.getText();
                    int textLength = queryText.length();
                    int endOffset = queryTokenOffset + textLength;

                    if (endOffset < caretOffset){
                        //out of range
                        return;
                    }

                    switch (queryToken.getType()) {
                        case BladeHtmlAntlrLexer.TAG_PART: {
                            tokens.seekTo(queryToken.getStartIndex() - 1);
                            if (tokens.hasNext()){
                                Token previousToken = tokens.next().get();
                                if (previousToken.getType() == BladeHtmlAntlrLexer.RAW_TAG_OPEN && queryText.equals("!")){ //NOI18N
                                    addBladeTagCompletionItem(BladeTagsUtils.RAW_TAG_CLOSE, caretOffset, resultSet); //NOI18N
                                }
                            }
                            break;
                        }
                        case BladeHtmlAntlrLexer.HTML_COMPONENT_OPEN_TAG: {
                            String identifier = ComponentsSupport.tag2ClassName(queryToken.getText());
                            completeComponents(identifier, fo, caretOffset, resultSet);
                            break;
                        }
                        case BladeHtmlAntlrLexer.COMPONENT_ATTRIBUTE: {
                            Set<Integer> stopTokens = new HashSet<>();
                            stopTokens.add(BladeHtmlAntlrLexer.HTML_COMPONENT_OPEN_TAG);
                            stopTokens.add(BladeHtmlAntlrLexer.GT);
                            String attributeIdentifier = queryText.startsWith(":") ? queryText.substring(1) : queryText; //NOI18N
                            Token componentToken = BladeHtmlAntlrUtils.findBackwardWithStop(tokens, BladeHtmlAntlrLexer.HTML_COMPONENT_OPEN_TAG, stopTokens);
                            if (componentToken != null && componentToken.getType() == BladeHtmlAntlrLexer.HTML_COMPONENT_OPEN_TAG) {
                                ComponentsQueryService componentComplervice = new ComponentsQueryService();
                                String identifier = ComponentsSupport.tag2ClassName(componentToken.getText());
                                Collection<PhpIndexResult> indexedReferences = componentComplervice.findComponentClass(identifier, fo);
                                Project projectOwner = FileSystemUtils.getProjectOwner(doc);
                                ComponentsSupport componentSupport = ComponentsSupport.getInstance(projectOwner);
                                
                                if (componentSupport == null){
                                    break;
                                }

                                for (PhpIndexResult indexReference : indexedReferences) {
                                    ComponentModel componentModel = componentSupport.findComponentClass(indexReference.declarationFile);
                                    if (componentModel == null){
                                        continue;
                                    }
                                    for (FormalParameter parameter : componentModel.getConstructorProperties()){
                                        String parameterName = parameter.getParameterName().toString().substring(1);
                                        if (parameterName.startsWith(attributeIdentifier)){
                                            addSimplAttributeItem(attributeIdentifier, parameterName, caretOffset, resultSet);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }

                }
            } finally {
                long time = System.currentTimeMillis() - startTime;
                if (time > 2000) {
                    LOGGER.log(Level.INFO, "Slow completion time detected. {0}ms", time); //NOI18N
                }
                resultSet.finish();
            }
        }
    }

    private void completeComponents(String prefixIdentifier, FileObject fo,
            int caretOffset, CompletionResultSet resultSet) {

        int insertOffset = caretOffset - prefixIdentifier.length();
        ComponentsQueryService componentComplervice = new ComponentsQueryService();
        Collection<PhpIndexResult> indexedReferences = componentComplervice.queryComponents(prefixIdentifier, fo);

        for (PhpIndexResult indexReference : indexedReferences) {
            addComponentIdCompletionItem(indexReference,
                    insertOffset, resultSet);
        }

    }

    private void addSimplAttributeItem(String prefix, String attributeName, int caretOffset, CompletionResultSet resultSet) {
        int insertOffset = caretOffset - prefix.length();
        CompletionItem item = CompletionUtilities.newCompletionItemBuilder(attributeName)
                .iconResource(XML_ATTRIBUTE_ICON)
                .startOffset(insertOffset)
                .leftHtmlText(attributeName)
                .rightHtmlText("    component attribute") //NOI18N
                .sortPriority(1)
                .build();
        resultSet.addItem(item);
    }

    private void addComponentIdCompletionItem(PhpIndexResult indexReference,
            int caretOffset, CompletionResultSet resultSet) {

        String tagName = StringUtils.toKebabCase(indexReference.name);
        CompletionItem item = CompletionUtilities.newCompletionItemBuilder(tagName)
                .iconResource(CUSTOM_HTML_ICON)
                .startOffset(caretOffset)
                .leftHtmlText(tagName)
                .rightHtmlText(indexReference.namespace)
                .sortPriority(1)
                .build();
        resultSet.addItem(item);
    }
    
    
    private void addBladeTagCompletionItem(String tag,
            int caretOffset, CompletionResultSet resultSet) {

        String tagName = StringUtils.toKebabCase(tag);
        CompletionItem item = CompletionUtilities.newCompletionItemBuilder(tagName)
                .iconResource(CUSTOM_HTML_ICON)
                .startOffset(caretOffset)
                .leftHtmlText(tagName)
                .rightHtmlText(tag)
                .sortPriority(1)
                .build();
        resultSet.addItem(item);
    }

}
