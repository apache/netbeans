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
package org.netbeans.modules.editor.structure.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentChange;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelTransactionCancelledException;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;


/** DocumentModel unit tests
 *
 * @author  Marek Fukala
 */
public class DocumentModelTest extends NbTestCase {
    
    private DocumentModelProvider dmProvider;
    
    public DocumentModelTest() {
        super("document-model-test");
    }
    
    @Override
    public void setUp() throws BadLocationException {
        dmProvider = new FakeDocumentModelProvider();
    }
    
    //--------- test methods -----------
    public void testModelBasis() throws DocumentModelException, BadLocationException {
        //set the document content
        Document doc = new BaseDocument(false, "text/plain");
        doc.insertString(0,"abcde|fgh|ij|k",null); //4 elements should be created
        
        DocumentModel model = new DocumentModel(doc, dmProvider);
        
        assertNotNull(model.getDocument());
        
        DocumentElement root = model.getRootElement();
        assertNotNull(root);
        
        assertNull(root.getParentElement());
        
        //test if the content of the root elemnt equals the document content
        assertTrue(root.getContent().equals(doc.getText(0, doc.getLength())));
        
        List children = root.getChildren();
        assertEquals(4, children.size());
        
        DocumentElement first = root.getElement(0);
        
        //check name and type
        assertEquals("element0", first.getName());
        assertEquals(FakeDocumentModelProvider.FAKE_ELEMENT_TYPE, first.getType());
        
        //check content and offsets
        assertEquals("abcde", first.getContent());
        assertEquals(0, first.getStartOffset());
        assertEquals(5, first.getEndOffset());
        
        //check has no children
        assertEquals(0, first.getElementCount());
        
    }
    
    public void testAddElementEvent() throws DocumentModelException, BadLocationException, InterruptedException {
        Document doc = new BaseDocument(false, "text/plain");
        final DocumentModel model = new DocumentModel(doc, dmProvider);
        
        
        //listen to model
        final List<DocumentElement> addedElements = new CopyOnWriteArrayList<DocumentElement>();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            @Override
            public void documentElementAdded(DocumentElement de) {
                addedElements.add(de);
            }
        });
        
        //listen to element
        final List<DocumentElement> addedElements2 = new CopyOnWriteArrayList<DocumentElement>();
        model.getRootElement().addDocumentElementListener(new DocumentElementListenerAdapter() {
            @Override
            public void elementAdded(DocumentElementEvent e) {
                addedElements2.add(e.getChangedChild());
            }
        });
        
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {
            @Override
            public void updateFinished() {
                assertEquals(4, addedElements.size());
                assertEquals(4, addedElements2.size());
                assertEquals(4, model.getRootElement().getElementCount());
            }
        });
        
        doc.insertString(0,"abcde|fgh|ij|k",null); //4 elements should be created
        model.forceUpdate();
    }
    
     public void testMoreDocumentElementListeners() throws DocumentModelException, BadLocationException, InterruptedException {
        Document doc = new BaseDocument(false, "text/plain");
        DocumentModel model = new DocumentModel(doc, dmProvider);
        
        DocumentElement root = model.getRootElement();
        
        assertNotNull(root);
        
        DocumentElementListener del1 = new DocumentElementListenerAdapter();
        DocumentElementListener del2 = new DocumentElementListenerAdapter();

        assertNull(root.deListener);
        assertNull(root.deListeners);
        
        root.addDocumentElementListener(del1);
        
        assertTrue(root.deListener == del1);
        assertNull(root.deListeners);
        
        root.addDocumentElementListener(del2);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(2, root.deListeners.size());
        
        //try to add twice
        root.addDocumentElementListener(del2);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(2, root.deListeners.size());
        
        root.removeDocumentElementListener(del2);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(1, root.deListeners.size());
        
        root.removeDocumentElementListener(del1);
        
        assertNull(root.deListener);
        assertNotNull(root.deListeners);
        assertEquals(0, root.deListeners.size());
        
    }
    
    public void testRemoveElementEvent() throws DocumentModelException, BadLocationException, InterruptedException {
        Document doc = new BaseDocument(false, "text/plain");
        doc.insertString(0,"abcde|fgh|ij|k",null); //4 elements should be created
        final DocumentModel model = new DocumentModel(doc, dmProvider);
        
        DocumentModelUtils.dumpModelElements(model);
        DocumentModelUtils.dumpElementStructure(model.getRootElement());
        
        //listen to model
        final List<DocumentElement> removedElements = new CopyOnWriteArrayList<DocumentElement>();
        model.addDocumentModelListener(new DocumentModelListenerAdapter() {
            @Override
            public void documentElementRemoved(DocumentElement de) {
                removedElements.add(de);
            }
        });
        
        //listen to element
        final List<DocumentElement> removedElements2 = new CopyOnWriteArrayList<DocumentElement>();
        model.getRootElement().addDocumentElementListener(new DocumentElementListenerAdapter() {
            @Override
            public void elementRemoved(DocumentElementEvent e) {
                removedElements2.add(e.getChangedChild());
            }
        });
        
        model.addDocumentModelStateListener(new DocumentModelStateListenerAdapter() {
            @Override
            public void updateFinished() {
                assertEquals(4,removedElements2.size());
                assertEquals(4, removedElements.size());
                assertEquals(0, model.getRootElement().getElementCount());
            }
        });
        
        doc.remove(0,doc.getLength());
        model.forceUpdate();
    }
    
    public void testDocumentModelStateListener() throws DocumentModelException, BadLocationException {
        Document doc = new BaseDocument(false, "text/plain");
        final DocumentModel model = new DocumentModel(doc, dmProvider);
        final State s = new State();
        s.setValue(-1);

        model.addDocumentModelStateListener(new DocumentModelStateListener() {
            @Override
            public void sourceChanged() {
                assertEquals(-1, s.getValue());
                s.setValue(0);
            }
            @Override
            public void scanningStarted() {
                assertEquals(0, s.getValue());
                s.setValue(1);
            }
            @Override
            public void updateStarted() {
                assertEquals(1, s.getValue());
                s.setValue(2);
            }
            @Override
            public void updateFinished() {
                assertEquals(2, s.getValue());
                s.setValue(3);
                synchronized (s) {
                    s.notifyAll();
                }
            }
        });
        
        doc.insertString(0, "blabla", null);
        model.forceUpdate();
        
        synchronized (s) {
            try {
                s.wait(10 * 1000); //10 sec 
            } catch (InterruptedException ex) {
                assert false : "DocumentModelStateListener doesn't work properly";
            }
        }
        
        assertEquals(3, s.getValue());
    }
    
    /**
     * A Simple testing implementation of DocumentModelProvider interface
     * used by DocumentModel unit tests.
     *
     * Creates elements according to the "|" separators: abcde|fghij|k|l|mnopqrstu|vwxyz
     *
     * @author Marek Fukala
     */
    private static class FakeDocumentModelProvider implements DocumentModelProvider {
        
        @Override
        public void updateModel(DocumentModel.DocumentModelModificationTransaction dtm,
                DocumentModel model, DocumentChange[] changes)
                throws DocumentModelException, DocumentModelTransactionCancelledException {
            try {
                String text = model.getDocument().getText(0, model.getDocument().getLength());
                int lastElementEnd = 0;
                int elCount = 0;
                ArrayList foundElements = new ArrayList();
                for(int i = 0; i < text.length(); i++) {
                    if(text.charAt(i) == '|' || i == text.length() - 1) {
                        //create element if doesn't exist
                        DocumentElement test = model.getDocumentElement(lastElementEnd, i);
                        if( test == null) {
                            foundElements.add(dtm.addDocumentElement("element" + elCount, FAKE_ELEMENT_TYPE, Collections.EMPTY_MAP, lastElementEnd, i));
                            elCount++;
                        } else {
                            foundElements.add(test);
                        }
                        lastElementEnd = i;
                    }
                }
                
                //delete unexisting elements
                ArrayList deleted = new ArrayList(model.getRootElement().getChildren());
                deleted.removeAll(foundElements);
                Iterator i = deleted.iterator();
                while(i.hasNext()) {
                    DocumentElement de = (DocumentElement)i.next();
                    dtm.removeDocumentElement(de, false);
                }
                
            }catch(BadLocationException e) {
                throw new DocumentModelException("error occured when creating elements",e);
            }
        }
        
        public static final String FAKE_ELEMENT_TYPE = "fake element";
        
    }
    
    private static class DocumentModelListenerAdapter implements DocumentModelListener {
        @Override
        public void documentElementAdded(DocumentElement de) {
        }
        @Override
        public void documentElementAttributesChanged(DocumentElement de) {
        }
        @Override
        public void documentElementChanged(DocumentElement de) {
        }
        @Override
        public void documentElementRemoved(DocumentElement de) {
        }
    }
    
    private static class DocumentElementListenerAdapter implements DocumentElementListener {
        @Override
        public void attributesChanged(DocumentElementEvent e) {
        }
        @Override
        public void childrenReordered(DocumentElementEvent e) {
        }
        @Override
        public void contentChanged(DocumentElementEvent e) {
        }
        @Override
        public void elementAdded(DocumentElementEvent e) {
        }
        @Override
        public void elementRemoved(DocumentElementEvent e) {
        }
    }
    
    private static class DocumentModelStateListenerAdapter implements DocumentModelStateListener {

        @Override
        public void sourceChanged() {
        }

        @Override
        public void scanningStarted() {
        }

        @Override
        public void updateStarted() {
        }

        @Override
        public void updateFinished() {
        }
        
    }
    
    private static class State {
        
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
    
}
