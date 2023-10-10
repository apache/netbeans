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
package org.netbeans.modules.j2ee.persistence.editor.completion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.j2ee.persistence.editor.CompletionContext;
import org.netbeans.modules.j2ee.persistence.editor.completion.db.DBCompletionContextResolver;
import org.netbeans.modules.j2ee.persistence.unit.PUDataLoader;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

/**
 * 
 * @author sp153251
 */
@MimeRegistration(mimeType = PUDataLoader.REQUIRED_MIME, service = CompletionProvider.class)//NOI18N
public class PUCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE && queryType !=CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new PUCompletionQuery(queryType, component, component.getSelectionStart(), true), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;//will not appear automatically
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    static class PUCompletionQuery extends AsyncCompletionQuery {

        private ArrayList<CompletionContextResolver> resolvers;
        private byte hasAdditionalItems = 0; //no additional items
        private int anchorOffset;
        private int queryType;

        public PUCompletionQuery(int queryType, JTextComponent component, int caretOffset, boolean hasTask) {
            this.queryType = queryType;
            initResolvers();
        }

        private void initResolvers() {
            //XXX temporary - should be registered somehow better
            resolvers = new ArrayList<CompletionContextResolver>();
            resolvers.add(new DBCompletionContextResolver());
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            List<JPACompletionItem> completionItems = new ArrayList<>();

            int anchorOffset = getCompletionItems(doc, caretOffset, completionItems);
            resultSet.addAllItems(completionItems);
            if (anchorOffset != -1) {
                resultSet.setAnchorOffset(anchorOffset);
            }

            resultSet.finish();
        }

        // This method is here for Unit testing purpose
        int getCompletionItems(Document doc, int caretOffset, List<JPACompletionItem> completionItems) {

            int anchorOffset = -1;
            CompletionContext context = new CompletionContext(doc, caretOffset);

            if (context.getCompletionType() == CompletionContext.CompletionType.NONE) {
                return anchorOffset;
            }

            switch (context.getCompletionType()) {
                case ATTRIBUTE_VALUE:
                    anchorOffset = PUCompletionManager.getDefault().completeAttributeValues(context, completionItems);
                    break;
                case ATTRIBUTE:
                    anchorOffset = PUCompletionManager.getDefault().completeAttributes(context, completionItems);
                    break;
                case TAG:
                    anchorOffset = PUCompletionManager.getDefault().completeElements(context, completionItems);
                    break;
                case VALUE:
                    anchorOffset = PUCompletionManager.getDefault().completeValues(context, completionItems);
                    break;
            }

            return anchorOffset;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            return false;
        }
        
        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            resultSet.finish();
        }
    }
}