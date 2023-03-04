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

package org.netbeans.modules.spring.beans.completion;

import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.beans.completion.CompletionContext.CompletionType;
import org.netbeans.modules.spring.beans.editor.DocumentContext;
import org.netbeans.modules.spring.beans.index.SpringIndex;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.TagElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
@MimeRegistration(mimeType = "x-springconfig+xml", service = CompletionProvider.class)
public class SpringXMLConfigCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) == COMPLETION_QUERY_TYPE) {
            return new AsyncCompletionTask(new SpringXMLConfigCompletionQuery(queryType), component);
        }

        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0; // XXX: Return something more specific
    }

    private static class SpringXMLConfigCompletionQuery extends AsyncCompletionQuery {
        
        private final int queryType;
        private JTextComponent component;
        private volatile Completor completor;

        public SpringXMLConfigCompletionQuery(int queryType) {
            this.queryType = queryType;
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            //XXX: look for invalidation conditions
            this.component = component;
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CompletionContext context = new CompletionContext(doc, caretOffset, queryType);
            if (context.getCompletionType() == CompletionType.NONE) {
                resultSet.finish();
                return;
            }
            SpringXMLConfigDocumentListener listener = SpringXMLConfigDocumentListener.getListener(context.getDocumentContext());
            doc.removeDocumentListener(listener);
            doc.addDocumentListener(listener);

            completor = CompletorRegistry.getDefault().getCompletor(context);
            if(completor != null) {
                SpringCompletionResult springCompletionResult = completor.complete(context);
                populateResultSet(resultSet, springCompletionResult);
            }

            resultSet.finish();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            if(completor == null) {
                return false;
            }
            
            boolean retVal = completor.canFilter(new CompletionContext(component.getDocument(), 
                    component.getCaretPosition(), queryType));
            if( (!retVal) && (completor != null) ) {
                completor.cancel();
        }

            return retVal;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            CompletionContext context = new CompletionContext(component.getDocument(), 
                    component.getCaretPosition(), queryType);
            SpringCompletionResult springCompletionResult = completor.filter(context);
            populateResultSet(resultSet, springCompletionResult);
            resultSet.finish();
        }
        
        private void populateResultSet(CompletionResultSet resultSet, SpringCompletionResult springCompletionResult) {
            if(springCompletionResult == SpringCompletionResult.NONE) {
                return;
            }
            
            resultSet.addAllItems(springCompletionResult.getItems());
            if (completor.getAnchorOffset() != -1) {
                resultSet.setAnchorOffset(completor.getAnchorOffset());
            }
            
            if(springCompletionResult.hasAdditionalItems()) {
                resultSet.setHasAdditionalItems(true);
                resultSet.setHasAdditionalItemsText(springCompletionResult.getAdditionalItemsText());
            }
        }
    }

    private static class SpringXMLConfigDocumentListener implements DocumentListener {

        private static Map<String, String> declaredNamespaces;
        private static SpringXMLConfigDocumentListener listener;

        private SpringXMLConfigDocumentListener(DocumentContext docContext) {
            updateDeclaredNamespaces(docContext);
        }
        
        public static SpringXMLConfigDocumentListener getListener(DocumentContext docContext) {
            if (listener == null) {
                listener = new SpringXMLConfigDocumentListener(docContext);
            } else {
                updateDeclaredNamespaces(docContext);
            }

            return listener;
        }

        private static void updateDeclaredNamespaces(DocumentContext docContext) {
            declaredNamespaces = docContext.getDeclaredNamespacesMap();
        }

        @Override
        public void insertUpdate(DocumentEvent evt) {
            int length = evt.getLength();
            int offset = evt.getOffset();
            try {
                Document doc = evt.getDocument();
                String text = evt.getDocument().getText(offset, length).trim();
                if (text.startsWith("xmlns:")) {    //NOI18N
                    String namespace = parseNamespace(text);
                    if (!declaredNamespaces.containsValue(namespace)) {
                        String schemaLocation = findSchemaLocation(doc, namespace);
                        updateSchemaLocation(doc, offset, namespace, schemaLocation);
                    }
                    evt.getDocument().removeDocumentListener(this);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent evt) {
            evt.getDocument().removeDocumentListener(this);
        }

        @Override
        public void changedUpdate(DocumentEvent evt) {
            evt.getDocument().removeDocumentListener(this);
        }

        void updateSchemaLocation(Document doc, final int offset, final String namespace, final String schemaLocation) {
                BaseDocument baseDoc = (BaseDocument) doc;
                final XMLSyntaxSupport syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(doc);

                baseDoc.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            SyntaxElement element = syntaxSupport.getElementChain(offset);
                            if (element.getType() == Node.ELEMENT_NODE &&
                                ((TagElement)element).isStart()) {
                                NamedNodeMap nnm = element.getNode().getAttributes();
                                Attr attr = (Attr)nnm.getNamedItem("xsi:schemaLocation");    //NOI18N
                                if (attr != null) {
                                    String val = attr.getValue();
                                    if (!val.contains(namespace)) {
                                        attr.setValue(val + "\n       " + namespace + " " + schemaLocation);    //NOI18N
                                    }
                                }
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
        }

        String parseNamespace(String text) {
            String namespace = text.substring(text.indexOf('"')+1,text.length()-1); //NOI18N
            return namespace;
        }

        String findSchemaLocation(Document document, String namespace) {
            FileObject fo = NbEditorUtilities.getFileObject(document);
            Map<String, FileObject> map = new SpringIndex(fo).getAllSpringLibraryDescriptors();
            for (String ns: map.keySet()) {
                if (ns.equals(namespace)) {
                    FileObject file = map.get(ns);
                    return namespace+"/"+file.getNameExt(); //NOI18N
                }
            }
            throw new UnsupportedOperationException("schemaLocation not found");
        }
    }
}
