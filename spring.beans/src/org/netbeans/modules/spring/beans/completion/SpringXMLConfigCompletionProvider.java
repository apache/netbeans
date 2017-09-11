/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.spring.beans.completion;

import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
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
                    if (!declaredNamespaces.values().contains(namespace)) {
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
