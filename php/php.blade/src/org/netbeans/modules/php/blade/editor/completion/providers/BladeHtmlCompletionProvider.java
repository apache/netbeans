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
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import static org.netbeans.modules.php.blade.editor.ResourceUtilities.CUSTOM_HTML_ICON;
import static org.netbeans.modules.php.blade.editor.ResourceUtilities.XML_ATTRIBUTE_ICON;
import org.netbeans.modules.php.blade.editor.components.ComponentsQueryService;
import org.netbeans.modules.php.blade.editor.indexing.PhpIndexResult;
import org.netbeans.modules.php.blade.project.ComponentsSupport;
import org.netbeans.modules.php.blade.syntax.BladeTagsUtils;
import org.netbeans.modules.php.blade.syntax.StringUtils;
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
