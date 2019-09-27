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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.structure.DocumentModelProviderFactory;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * DocumentModel represents a hirarchical structure of a {@link javax.swing.text.Document}.
 * It consists of a tree of DocumentElement objects which represents a various
 * pieces of the document.
 * <br>
 * The model content is created by an implementation of the SPI class
 * DocumentModelProvider responsible for parsing the text document a producing
 * appropriate DocumentElement-s.
 * For more information about how to create an implementation of the DocumentModelProvider
 * and how to register to a specific file-type look into
 * {@link org.netbeans.modules.editor.spi.DocumentModelProvider} documentation.
 * <br>
 * There can be only one instance of the model associated to a document
 * instance at the time. Clients can obtain the instance
 * through DocumentModel.getDocumentModel(Document doc) method.
 * The client will obtain an instance of the model immediately, if the model
 * already exists, or, when noone had asked for it, a new one will be created.
 * <br>
 * The model registers a DocumentListener to the associated Document and
 * listen on its changes. When there is a change in the document,
 * the model waits for a defined amount of time if another update happens,
 * and if not, then asks DocumentModelProvider to regenerate the model's
 * elements. If the document changes during the parsing process,
 * the process will be stopped, all model changes thrown away,
 * and a new update process will be started after the specified amount of time.
 * The DocumentModelProvider obtains a list of document changes which
 * happened during the 500ms interval and an instance of DocumentModelTransaction.
 * The provider is then responsible to decide what parts of the document
 * needs to be reparsed (based on what and where was changed in the document)
 * and update the elements accordingly. During the parsing process the
 * provider puts add, change and remove requests to the transaction.
 * Once the provider finishes the parsing all the changes stored in the
 * transactions are commited. Only during this process the content of the model
 * is really updated. All event's (both from the model itself and from elements)
 * are fired during the transaction commit, not during adding the requests
 * into the transaction.
 * <br>
 * The model is read-only. All DocumentElement instancies are immutable and clients
 * cannot modify them. Only DocumentModelProvider can modify the model.
 * <br>
 * DocumentElements cannot cross. There cannot be two elements with the same
 * boundaries.
 *
 * <br>
 * <b>Threading: Do not access the document model's element under the document writeLock()!!!</b>
 * This may cause a deadlock if you do a document change, do not do writeUnlock() and access the model.
 * In such case the model needs to resort it's elements which is done under document readLock().
 *
 *@author Marek Fukala
 *@version 1.0
 *
 *@see DocumentElement
 */
public final class DocumentModel {
    
    //after each document change update of the model is postponed for following timeout.
    //if another document change happens in the time interval the update is postponed again.
    private static int MODEL_UPDATE_TIMEOUT = 2000; //milliseconds
    
    //a Document instance which the model is build upon.
    private BaseDocument doc;
    private DocumentModelProvider provider;
    
    private DocumentChangesWatcher changesWatcher;
    
    private RequestProcessor requestProcessor;
    private RequestProcessor.Task task;
    
    private ElementsArray elements = new ElementsArray();
    
    //stores a default root element
    private DocumentElement rootElement;
    
    //the transaction is used to regenerate document model elements
    //its non-null value states that there is an already running model update
    private DocumentModel.DocumentModelModificationTransaction modelUpdateTransaction = null;
    //    private Object modelUpdateLock = new Object();
    
    //a semaphore signalling the state of synchronization between document and the elements
    //this is always se to true when the document is changed and is set back to false
    //when the elements are resorted.
    boolean documentDirty = true;
    
    //model synchronization
    private int numReaders = 0;
    private int numWriters = 0;
    private Thread currWriter = null;
    private Thread currReader = null;
    
    //stores DocumentModel listeners
    private HashSet<DocumentModelListener> dmListeners = new HashSet<DocumentModelListener>();
    private HashSet<DocumentModelStateListener> dmsListeners = new HashSet<DocumentModelStateListener>();
    
    private static final int ELEMENT_ADDED = 1;
    private static final int ELEMENT_REMOVED = 2;
    private static final int ELEMENT_CHANGED = 3;
    private static final int ELEMENT_ATTRS_CHANGED = 4;
    
    private static Map<Document, Object> locks = new WeakHashMap<Document, Object>();

    final Map<String,String> elementsNamesCache = new WeakHashMap<String,String>();
    final Map<String,String> elementsTypesCache = new WeakHashMap<String,String>();
    final Map<String,String> elementsAttrNamesCache = new WeakHashMap<String,String>();
    final Map<String,String> elementsAttrValueCache = new WeakHashMap<String,String>();
    
    private static final Map<String,String> EMPTY_ATTRS_MAP = Collections.emptyMap();
    private static final List<DocumentElement> EMPTY_ELEMENTS_LIST = Collections.emptyList();
    
    DocumentModel(Document doc, DocumentModelProvider provider) throws DocumentModelException {
        this.doc = (BaseDocument)doc; //type changed in DocumentModel.getDocumentModel(document);
        this.provider = provider;
        
        //init RP & RP task
        requestProcessor = new RequestProcessor(DocumentModel.class.getName());
        task = requestProcessor.create(new Runnable() {
            public void run() {
                updateModel();
            }
        });
        
        //create a new root element - this element comprises the entire document
        addRootElement();
        
        /*create a sorted set which sorts its elements according to their
        startoffsets and endoffsets.
        - lets have elements E1 and E2:
         
        if E1.startOffset > E2.startOffset then the E1 is before E2.
        if E1.startOffset == E2.startOffset then
           if E1.endOffset > E2.endOffset then the E1 is before E2
         */
        initDocumentModel();
        
        this.changesWatcher = new DocumentChangesWatcher();
        getDocument().addDocumentListener(WeakListeners.document(changesWatcher, doc));
        
    }
    
    /** Clients uses this method to obtain an instance of the model for a particullar text document.
     * The client will either obtain an instance of the model immediately, if the model
     * already exists, or, when noone had asked for the model, a new one will be created (which may take some time).
     *
     * @param doc the text document for which the client wants to create a model
     * @return an initialized DocumentModel instance containing the structural data got from DocumentModelProvider
     */
    public static DocumentModel getDocumentModel(Document doc) throws DocumentModelException {
        synchronized (getLock(doc)) {
            if(!(doc instanceof BaseDocument))
                throw new ClassCastException("Currently it is necessary to pass org.netbeans.editor.BaseDocument instance into the DocumentModel.getDocumentProvider(j.s.t.Document) method.");
            //first test if the document has already associated a document model
            @SuppressWarnings("unchecked")
            WeakReference<DocumentModel> modelWR = (WeakReference<DocumentModel>)doc.getProperty(DocumentModel.class);
            DocumentModel cachedInstance = modelWR == null ? null : modelWR.get();
            if(cachedInstance != null) {
                //            System.out.println("[document model] got from weak reference stored in editor document property");
                return cachedInstance;
            } else {
                //create a new modelx
                Class editorKitClass = ((BaseDocument)doc).getKitClass();
                BaseKit kit = BaseKit.getKit(editorKitClass);
                if (kit != null) {
                    String mimeType = kit.getContentType();
                    //get the provider instance (the provider is a singleton class)
                    DocumentModelProvider provider =
                            DocumentModelProviderFactory.getDefault().getDocumentModelProvider(mimeType);
                    if(provider != null) {
                        DocumentModel model = new DocumentModel(doc, provider);
                        //and put it as a document property
                        doc.putProperty(DocumentModel.class, new WeakReference<DocumentModel>(model));
                        //                    System.out.println("[document model] created a new instance");
                        return model;
                    } else
                        return null; //no provider ??? should not happen?!?!
                } else {
                    throw new IllegalStateException("No editor kit for document " + doc + "!");
                }
            }
        }
    }
    
    private static Object getLock(Document doc) {
        synchronized (locks) {
            Object lock = locks.get(doc);
            if(lock == null) {
                lock = new Object();
                locks.put(doc, lock);
            }
            return lock;
        }
    }
    
    /** @return the text document this model is based upon */
    public Document getDocument() {
        return doc;
    }
    
    /** Every model has at least one element - a root element.
     * This element cannot be removed or manipulated somehow.
     * All elements created by DocumentModelProvider are descendants of this element.
     * <br>
     * This is an entry point to the tree structure of the document.
     * Use element.getChildren() to traverse the tree of elements.
     *
     * @return the root DocumentElement
     */
    public DocumentElement getRootElement() {
        return rootElement;
    }
    
    /** Adds an instance of DocumentModelListener to the model.*/
    public void addDocumentModelListener(DocumentModelListener dml) {
        dmListeners.add(dml);
    }
    
    /** Removes an instance of DocumentModelListener from the model.*/
    public void removeDocumentModelListener(DocumentModelListener dml) {
        dmListeners.remove(dml);
    }
    
    /** Adds an instance of {@link DocumentModelStateListener} to the model.
     * 
     *  @since 1.14
     */
    public void addDocumentModelStateListener(DocumentModelStateListener dml) {
        dmsListeners.add(dml);
    }
    
    /** Removes an instance of {@link DocumentModelStateListener} from the model.
     * 
     *  @since 1.14
     */
    public void removeDocumentModelStateListener(DocumentModelStateListener dml) {
        dmsListeners.remove(dml);
    }
    
    /** Decides whether the elements are in ancestor - descendant relationship.
     * The relationship is defined as follows:
     * isDescendant = ((ancestor.getStartOffset() < descendant.getStartOffset()) &&
     *                (ancestor.getEndOffset() > descendant.getEndOffset()));
     * @return true if the ancestor element is an ancestor of the descendant element.
     */
    public boolean isDescendantOf(DocumentElement ancestor, DocumentElement descendant) {
        readLock();
        try {
            if(ancestor == descendant) {
                if(debug) System.out.println("ERROR in " + ancestor);
                debugElements();
                throw new IllegalArgumentException("ancestor == descendant!!!");
            }
            //there cannot normally by two elements with the same start or end offsets (boundaries)
            //the only exception where startoffset and endoffset can be the some is root elements =>
            if(isRootElement(ancestor)) return true;
            
            return isDescendantOf(ancestor.getStartOffset(), ancestor.getEndOffset(), 
                    descendant.getStartOffset(), descendant.getEndOffset());
            
        }finally{
            readUnlock();
        }
    }
    
    private static boolean isDescendantOf(int ancestorSO, int ancestorEO, int descendantSO, int descendantEO) {
        if(descendantEO != descendantSO) {
            if((ancestorSO == descendantSO && ancestorEO > descendantEO)
                    || (ancestorEO == descendantEO && ancestorSO < descendantSO))
                return true;
        }
        return (ancestorSO < descendantSO && ancestorEO > descendantEO);
    }
    
    /** Returns a leaf element from the hierarchy which contains the
     * given offset. If there is not such an element it returns root element.
     * The element is returned as a leaf if it contains the offset and there
     * isn't any children of the element containing the offset.
     *
     * @return the most top (leaf) element containing the offset.
     */
    public DocumentElement getLeafElementForOffset(int offset) {
        readLock();
        checkDocumentDirty();
        try{
            if(getDocument().getLength() == 0)  return getRootElement();
            DocumentElement leaf = null;
            for(int i = 0; i < elements.size(); i++) {
                DocumentElement de = elements.get(i);
                if(de.getStartOffset() <= offset) {
                    if(de.getEndOffset() >=offset) {
                        //a possible candidate found
                        if(de.getStartOffset() == de.getEndOffset() && de.getStartOffset() == offset) {
                            //empty tag on the offset => return nearest candidate (its parent)
                            break;
                        }
                        leaf = de;
                    }
                } else {
                    //we have crossed the 'offset' => there cannot be a suitable
                    //element in the rest of the 'elements' set.
                    break;
                }
            }
            
            //#69756 - there may be no found element in the case when the document content has been
            //changed during the model update - then the documentmodel element's may be in inconsistent
            //state so no element is found here. The correct approach to this is always lock the document
            //for reading when updating the model. However from performance reasons it is not possible now.
            if(leaf == null) leaf = getRootElement();
            
            return leaf;
            
        } finally {
            readUnlock();
        }
    }
    
    /** Forces the model to start update immediately. The call is non-blocking, client is supposed to listen
     * on DocumentModelStateListener.updateFinished() before using the model.
     * 
     *  @since 1.14
     */
    public void forceUpdate() {
        requestModelUpdate(true);
    }
    
    // ---- private methods -----
    static void setModelUpdateTimout(int timeout) {
        MODEL_UPDATE_TIMEOUT = timeout;
    }
    
    private boolean isRootElement(DocumentElement de) {
        return de == getRootElement();
    }
    
    /** Returns a DocumentElement instance if there is such one with given boundaries. */
    //noone except tests use this method so the linear complexity doesn't matter
    DocumentElement getDocumentElement(int startOffset, int endOffset) throws BadLocationException {
        readLock();
        checkDocumentDirty();
        try {
            for(int i = 0; i < elements.size(); i++) {
                DocumentElement de = elements.get(i);
                if(de.getStartOffset() == startOffset &&
                        de.getEndOffset() == endOffset)
                    return de;
                
                //we are far behind => there isn't such element => break the loop
                if(de.getStartOffset() > startOffset) break;
            }
            
            //nothing found
            return null;
            
        }finally{
            readUnlock();
        }
    }
    
    /** Finds all {@link DocumentElement}s whose start offset = given startOfset. 
     * @return List of {@link DocumentElement}s with start offset = startOffset
     */
    List<DocumentElement> getDocumentElements(int startOffset) throws BadLocationException {
        readLock();
        checkDocumentDirty();
        try {
            int elementIndex = elements.binarySearchForOffset(startOffset);
            if(elementIndex < 0) {
                return Collections.emptyList();
            } else {
                //we found an element with the startoffset
                //there might be some others just before or just after this one
                //in the elements array - find them.
                ArrayList<DocumentElement> found = new ArrayList<DocumentElement>();
                found.add(elements.get(elementIndex)); //add the found one
                
                //find previous
                int eli = elementIndex;
                while(--eli >= 0) {
                    DocumentElement previous = elements.get(eli);
                    if(previous.getStartOffset() == startOffset) {
                        //previous element has still the same start offset
                        found.add(0, previous);
                    } else {
                        break; //no more elements with the same startoffset
                    }
                }
                
                //find next
                while(++elementIndex < elements.size()) {
                    DocumentElement next = elements.get(elementIndex);
                    if(next.getStartOffset() == startOffset) {
                        //next element has still the same start offset
                        found.add(next);
                    } else {
                        break; //no more elements with the same startoffset
                    }
                }
                
                return found;
            }
            
        }finally{
            readUnlock();
        }
    }
    
    private DocumentModel.DocumentModelModificationTransaction createTransaction(boolean init) {
        return new DocumentModelModificationTransaction(init);
    }
    
    //generate elements for the entire document
    private void initDocumentModel() throws DocumentModelException {
        try {
            DocumentModel.DocumentModelModificationTransaction trans = createTransaction(true);
            provider.updateModel(trans, this, new DocumentChange[]{new DocumentChange(getDocument().getStartPosition(), getDocument().getLength(), DocumentChange.INSERT)});
            trans.commit();
        }catch(DocumentModelTransactionCancelledException e) {
            assert false : "We should never get here";
        }
    }
    
    private void requestModelUpdate(boolean updateImmediately) {
        //test whether there is an already running model update and if so cancel it
        if(modelUpdateTransaction != null) {
            modelUpdateTransaction.setTransactionCancelled();
        }
        task.schedule(updateImmediately ? 0 : MODEL_UPDATE_TIMEOUT);
    }
       
    private void updateModel() {
        fireScanningStarted();
        try {
            //create a new transaction
            modelUpdateTransaction = createTransaction(false);
            DocumentChange[] changes = changesWatcher.getDocumentChanges();

            if(debug) debugElements();
        
            //let the model provider to decide what has changed and what to regenerate
            provider.updateModel(modelUpdateTransaction, this, changes);
            //commit all changes => update the model and fire events
            
            //do that in EDT
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            writeLock(); //lock the model for reading
                            fireUpdateStarted();
                            DocumentModel.this.modelUpdateTransaction.commit();
                            //clear document changes cache -> if the transaction has been cancelled
                            //the cache is not cleared so next time the changes will be taken into account.
                            changesWatcher.clearChanges();
                            modelUpdateTransaction = null; //states that the model update has already finished
                        }catch(DocumentModelTransactionCancelledException dmte) {
                            //ignore
                        }catch(Exception e) {
                            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                        }finally{
                            writeUnlock(); //unlock the model
                            fireUpdateFinished();
                        }
                    }
                });
            }catch(Exception ie) {
                ie.printStackTrace(); //XXX handle somehow
            }
        }catch(DocumentModelException e) {
            if(debug) System.err.println("[DocumentModelUpdate] " + e.getMessage());
        }catch(DocumentModelTransactionCancelledException dmcte) {
            if(debug) System.out.println("[document model] update transaction cancelled.");
        }
        if(debug) DocumentModelUtils.dumpElementStructure(getRootElement());
    }
    
    
    /** AFAIK there isn't also any way how to explicitly resort a set so I need to use a list and resort it
     * manually after each elements change. This allows me to resort elements after a document change to
     * keep correct he eleements order. */
    private void checkDocumentDirty() {
        //the resort has to lock the model for access since it modifies the elements order
        //and the document for modifications
        if(documentDirty) {
            writeLock();
            try {
                doc.readLock();
                try {
                    elements.resort();
                } finally {
                    doc.readUnlock();
                }
            }finally {
                writeUnlock();
            }
            documentDirty = false;
        }
        
    }
    
    private void addRootElement() {
        try {
            DocumentModelModificationTransaction dmt = createTransaction(false);
            
            rootElement = dmt.addDocumentElement("root", DOCUMENT_ROOT_ELEMENT_TYPE, EMPTY_ATTRS_MAP,
                    0, getDocument().getLength());
            dmt.commit();
        }catch(BadLocationException e) {
            //this is very unlikely that the BLE will be thrown from this code
            throw new IllegalStateException("Adding of root document element failed - strange!");
        }catch(DocumentModelTransactionCancelledException dmtce) {
            assert false : "We should never get here";
        }
    }
    
    
    List<DocumentElement> getChildren(DocumentElement de) {
        readLock();
        checkDocumentDirty();
        try {
            //test whether the element has been removed - in such a case anyone can still have a reference to it
            //but the element is not held in the document structure elements list
            
            int index = elements.indexof(de);
            
            if(index < 0) {
                if(debug) System.out.println("Warning: DocumentModel.getChildren(...) called for " + de + " which has already been removed!");
                return Collections.emptyList();
            }
            
            //there is a problem with empty elements - if an element is removed its boundaries
            //are the some and the standart getParent/getChildren algorith fails.
            //the root element can be empty however it has to return children (also empty)
            if(de.isEmpty()) {
                //if the root element is empty the rest of elements is also empty and
                //has to be returned as children
                if(isRootElement(de)) {
                    //ommit the root element which is always first
                    if(elements.size() > 1) {
                        return Arrays.asList(elements.subarray(1, elements.size()));
                    } else {
                        //just root element exists
                        return Collections.emptyList();
                    }
                } else {
                    //empty element has no children
                    return Collections.emptyList();
                }
            }
            
            ArrayList<DocumentElement> children = new ArrayList<DocumentElement>();
            //get all elements with startOffset >= de.getStartOffset()

            index++;//skip the first element - this is the given element
            
            //is there any other elements behind the 'de' element?
            if(index < elements.size()) {
                //Since the elements are sorted acc. to their start and end offsets and elements cannot cross!!!
                //the next element must be the first child if its startOffset < the given element endOffset
                DocumentElement firstChild = elements.get(index);
                children.add(firstChild);
                if(!isDescendantOf(de, firstChild)) {
                    return Collections.emptyList();
                } else {
                    //the element is a child
                    //check the other elements - find first element which has startOffset > firstChild.endOffset
                    DocumentElement nextChild = firstChild;
                    for(int i = index + 1; i < elements.size(); i++) {
                        DocumentElement docel = elements.get(i);
                        int docel_so = docel.getStartOffset();
                        //test whether we didn't overpal the given 'de' endOffset
                        if(docel_so > de.getEndOffset()) break;
                        
                        //test if the element is the first next child which has startOffset > previous child endOffset
                        if(docel_so >= nextChild.getEndOffset() ) {
                            //found a next child
                            children.add(docel);
                            nextChild = docel;
                        }
                        
                    }
                }
            }
            
            return children;
            
        } finally {
            readUnlock();
        }
    }
    
    DocumentElement getParent(DocumentElement de) {
        int index = elements.indexof(de);
        if(index < 0) {
            //the element is not in the list
            throw new IllegalArgumentException("getParent() called for " + de + " which is not in the elements list!");
        } else {
            return getParent(index, de.getStartOffset(), de.getEndOffset());
        }
    }
    
    private DocumentElement getParent(int index, int de_so, int de_eo) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be positive!");
        }
        if (index == 0) {
            //root element has no parent
            return null;
        }
        readLock();
        checkDocumentDirty();
        try {
            //get all elements with startOffset <= de.getStartOffset()
            //scan the elements in reversed order
            for(int i = index - 1; i >= 0; i--) {
                DocumentElement el = elements.get(i);
                //test whether the element is empty - if so, get next one etc...
                //if(!isEmpty(el) && el.getStartOffset() < de.getStartOffset() && isDescendantOf(el,de)) return cacheParent(de, el);
                int el_so = el.getStartOffset();
                int el_eo = el.getEndOffset();
                if(el_so < de_so && el_so != el_eo && isDescendantOf(el_so, el_eo, de_so, de_eo)) {
//                    //measurement - average deepness of the find
//                    parent_count++;
//                    parent += index - i;
                    return el;
                }
            }
            
            //if not found (e.g. has the same startoffsets in case of root)
            //root is returned in all cases except parent of the root itself
            return getRootElement();
            
        }finally{
            readUnlock();
        }
    }
    
    /** This method should be owerrided by subclasses to return appropriate DocumentElement
     * instancies according to given DocumentElementType. */
    private DocumentElement createDocumentElement(String name, String type, Map<String, String> attributes,
            int startOffset, int endOffset) throws BadLocationException {
        //by default return DocumentElementBase
        return new DocumentElement(name, type, attributes, startOffset, endOffset, this );
    }
    
    private void fireDocumentModelEvent(DocumentElement de, int type) {
        for (DocumentModelListener cl: dmListeners) {
            switch(type) {
            case ELEMENT_ADDED: cl.documentElementAdded(de);break;
            case ELEMENT_REMOVED: cl.documentElementRemoved(de);break;
            case ELEMENT_CHANGED: cl.documentElementChanged(de);break;
            case ELEMENT_ATTRS_CHANGED: cl.documentElementAttributesChanged(de);break;
            }
        }
    }
    
    private void fireSourceChanged() {
        for (DocumentModelStateListener cl: dmsListeners) {
            cl.sourceChanged();
        }
    }

    private void fireScanningStarted() {
        for (DocumentModelStateListener cl: dmsListeners) {
            cl.scanningStarted();
        }
    }
    
    private void fireUpdateStarted() {
        for (DocumentModelStateListener cl: dmsListeners) {
            cl.updateStarted();
        }
    }
    
    private void fireUpdateFinished() {
        for (DocumentModelStateListener cl: dmsListeners) {
            cl.updateFinished();
        }
    }

    
    //-------------------------------------
    // ------ model synchronization -------
    //-------------------------------------
    
    public synchronized final void readLock() {
        try {
            while (currWriter != null) {
                if (currWriter == Thread.currentThread()) {
                    // writer has full read access.
                    return;
                }
                wait();
            }
            currReader = Thread.currentThread();
            numReaders += 1;
        } catch (InterruptedException e) {
            throw new Error("Interrupted attempt to aquire read lock");
        }
    }
    
    public synchronized final void readUnlock() {
        if (currWriter == Thread.currentThread()) {
            // writer has full read access.
            return;
        }
        assert numReaders > 0 : "Bad read lock state!";
        numReaders -= 1;
        if(numReaders == 0) currReader = null;
        notify();
    }
    
    private synchronized final void writeLock() {
        try {
            while ((numReaders > 0) || (currWriter != null)) {
                if (Thread.currentThread() == currWriter) {
                    numWriters++;
                    return;
                }
                if (Thread.currentThread() == currReader) {
                    //if this thread has readlock then we can write
                    return ;
                }
                wait();
            }
            currWriter = Thread.currentThread();
            numWriters = 1;
        } catch (InterruptedException e) {
            throw new Error("Interrupted attempt to aquire write lock");
        }
    }
    
    private synchronized final void writeUnlock() {
        if (--numWriters <= 0) {
            numWriters = 0;
            currWriter = null;
            notifyAll();
        }
    }
    
    //-------------------------------------
    // --------- debug methods ------------
    //-------------------------------------
    void debugElements() {
        System.out.println("DEBUG ELEMENTS:");
        for(int i = 0; i < elements.size(); i++) {
            System.out.println(elements.get(i));
        }
        System.out.println("*****\n");
    }
    
    DocumentElement[] elements() {
        readLock();
        try {
            DocumentElement[] clone = new DocumentElement[elements.size()];
            System.arraycopy(elements.elements, 0, clone, 0, clone.length);
            return clone;
        } finally {
            readUnlock();
        }
    }
    
    //-------------------------------------
    // --------- inner classes -------------
    //-------------------------------------
    
    /** Used by DocumentModelProvider to store planned changes in the model
     * (adds/removes/content changes) of elements and then commit them all together.
     * <br>
     * The transaction can be cancelled, then any attempt to add or remove document
     * element to/from the transaction causes the DocumentModelTransactionCancelledException
     * exception to be thrown.
     *
     */
    public final class DocumentModelModificationTransaction {
        
        private ArrayList<DocumentModelModification> modifications = new ArrayList<DocumentModelModification>();
        private boolean transactionCancelled = false;
        private boolean init;
        
        DocumentModelModificationTransaction(boolean init) {
            this.init = init;
        }
        
        /** Creates a new DocumentElement and adds it into the transaction.
         *
         * @param name the name of the DocumentElement
         * @param type the type of the elemenent
         * @param attributes the Map of element's attributes
         * @param startOffset, endOffset the element's boundaries
         * @throws DocumentModelTransactionCancelledException when the transaction has been cancelled and someone
         * calls this method.
         */
        public DocumentElement addDocumentElement(String name, String type, Map<String, String> attributes, int startOffset,
                int endOffset) throws BadLocationException, DocumentModelTransactionCancelledException {
            //test if the transaction has been cancelled and if co throw TransactionCancelledException
            if(transactionCancelled) throw new DocumentModelTransactionCancelledException();
            
            //create a new DocumentElement instance
            DocumentElement de = createDocumentElement(name, type, attributes, startOffset, endOffset);

            if(!elements.contains(de)) {
                if(debug) System.out.println("# ADD " + de + " adding into transaction");
                DocumentModelModification dmm = new DocumentModelModification(de, DocumentModelModification.ELEMENT_ADD);
                modifications.add(dmm);
            }
            
            return de;
        }
        
        /** Adds a remove request of an already existing DocumentElement to the transaction.
         *
         * @param de the document element to be removed
         * @param removeAllItsDescendants causes that all the element's descendants will be removed from the model as well.
         */
        public void removeDocumentElement(DocumentElement de, boolean removeAllItsDescendants) throws DocumentModelTransactionCancelledException {
            //test if the transaction has been cancelled and if co throw TransactionCancelledException
            if(transactionCancelled) throw new DocumentModelTransactionCancelledException();
            
            //we cannot remove root element
            if(isRootElement(de)) {
                if(debug) System.out.println("WARNING: root element cannot be removed!");
                return ;
            }
            if(debug) System.out.println("# REMOVE " + de + " adding into transaction ");
            
            //first remove children
            if(removeAllItsDescendants) {
                //remove all its descendants recursivelly
                Iterator/*<DocumentElement>*/ childrenIterator = getChildren(de).iterator();
                while(childrenIterator.hasNext()) {
                    DocumentElement child = (DocumentElement)childrenIterator.next();
                    removeDocumentElement(child, true);
                }
            }
            
            //and then myself
            DocumentModelModification dmm = new DocumentModelModification(de, DocumentModelModification.ELEMENT_REMOVED);
            modifications.add(dmm);
        }
        
        /** Adds a new text update request to the transaction.
         *
         * @param de the Document element which text content has been changed.
         */
        public void updateDocumentElementText(DocumentElement de) throws DocumentModelTransactionCancelledException {
            //test if the transaction has been cancelled and if co throw TransactionCancelledException
            if(transactionCancelled) throw new DocumentModelTransactionCancelledException();
            
            DocumentModelModification dmm = new DocumentModelModification(de, DocumentModelModification.ELEMENT_CHANGED);
            if(!modifications.contains(dmm)) modifications.add(dmm);
        }
        
        /** Adds a attribs update request to the transaction.
         *
         * @param de the Document element which text content has been changed.
         * @param attrs updated attributes
         */
        public void updateDocumentElementAttribs(DocumentElement de, Map<String, String> attrs) throws DocumentModelTransactionCancelledException {
            //test if the transaction has been cancelled and if co throw TransactionCancelledException
            if(transactionCancelled) throw new DocumentModelTransactionCancelledException();
            
            DocumentModelModification dmm = new DocumentModelModification(de, DocumentModelModification.ELEMENT_ATTRS_CHANGED, attrs);
            if(!modifications.contains(dmm)) modifications.add(dmm);
        }
        
        
        private void commit() throws DocumentModelTransactionCancelledException {
//            parent=0;
//            parent_count = 0;
            long a = System.currentTimeMillis();
            writeLock();
            try {
                //test if the transaction has been cancelled and if co throw TransactionCancelledException
                if(transactionCancelled) throw new DocumentModelTransactionCancelledException();
                
                //XXX not an ideal algorithm :-)
                //first remove all elements
                long r = System.currentTimeMillis();
                if(debug) System.out.println("\n# commiting REMOVEs");
                Iterator<DocumentModelModification> mods = modifications.iterator();
                int removes = 0;
                //reset last parent cache
                last_empty_element_start_offset = -1;
                last_parent = null;
                
                while(mods.hasNext()) {
                    DocumentModelModification dmm = mods.next();
                    if(dmm.type == DocumentModelModification.ELEMENT_REMOVED) {
                        if(removeDE(dmm.de)) {
                            removes++;
                        } else {
                            System.out.println("[DMT] cannot remove element " + dmm.de);
                        }
                    }
                }
                
                if(measure) System.out.println("[xmlmodel] "+ removes + " removes commited in " + (System.currentTimeMillis() - r));
                
                //if the entire document content has been removed the root element is marked as empty
                //to be able to add new elements inside we need to unmark it now
                
                long adds = System.currentTimeMillis();
                //then add all new elements
                //it is better to add the elements from roots to leafs
                if(debug) System.out.println("\n# commiting ADDs");
                mods = modifications.iterator();
                
                ArrayList<DocumentElement> added = new ArrayList<DocumentElement>();
                while(mods.hasNext()) {
                    DocumentModelModification dmm = mods.next();
                    if(dmm.type == DocumentModelModification.ELEMENT_ADD) {
                        added.add(dmm.de);
                        elements.fastAdd(dmm.de);
                    }
                }
                elements.resort();
                
                if(!init) { //do not fire events during model init
                    for (DocumentElement de: added) {
                        fireElementAddedEvent(de);
                    }
                }
                
                if(measure) System.out.println("[xmlmodel] " + added.size() + " adds commited in " + (System.currentTimeMillis() - adds));
                
                long upds = System.currentTimeMillis();
                if(debug) System.out.println("\n# commiting text UPDATESs");
                mods = modifications.iterator();
                while(mods.hasNext()) {
                    DocumentModelModification dmm = mods.next();
                    if(dmm.type == DocumentModelModification.ELEMENT_CHANGED) updateDEText(dmm.de);
                }
                
                if(debug) System.out.println("\n# commiting attribs UPDATESs");
                mods = modifications.iterator();
                while(mods.hasNext()) {
                    DocumentModelModification dmm = mods.next();
                    if(dmm.type == DocumentModelModification.ELEMENT_ATTRS_CHANGED) updateDEAttrs(dmm.de, dmm.attrs);
                }
                
                if(measure) System.out.println("[xmlmodel] updates commit done in " + (System.currentTimeMillis() - upds));
            } finally {
                writeUnlock();
            }
            if(debug) System.out.println("# commit finished\n");
            if(measure) System.out.println("[xmlmodel] commit done in " + (System.currentTimeMillis() - a));
//            System.out.println("parent root count = " + parent_count);
//            System.out.println("parent root average = " + (parent / (parent_count == 0 ? 1 : parent_count)));
            
        }
        
        private void updateDEText(DocumentElement de) {
            //notify model listeners
            fireDocumentModelEvent(de, ELEMENT_CHANGED);
            //notify element listeners
            ((DocumentElement)de).contentChanged();
        }
        
        private void updateDEAttrs(DocumentElement de, Map<String, String> attrs) {
            //set the new attributes
            de.setAttributes(attrs);
            
            //notify model listeners
            fireDocumentModelEvent(de, ELEMENT_ATTRS_CHANGED);
            //notify element listeners
            ((DocumentElement)de).attributesChanged();
        }
        
        private void fireElementAddedEvent(DocumentElement de) {
            DocumentElement parent = (DocumentElement)de.getParentElement();
            /* events firing:
             * If the added element has a children, we have to fire remove event
             * to their previous parents (it is the added element's parent)
             * and fire add event to the added element for all its children
             */
            if(parent != null) {//root element doesn't have any parent
                List children = de.getChildren();
                //fire add event for the new document element itself
                parent.childAdded(de);
                //fire events for all affected children
                Iterator/*<DocumentElement>*/ childrenIterator = children.iterator();
                while(childrenIterator.hasNext()) {
                    DocumentElement child = (DocumentElement)childrenIterator.next();
                    parent.childRemoved(child);
                    de.childAdded(child);
                }
            }
            fireDocumentModelEvent(de, ELEMENT_ADDED);
        }
        
        //note: document change events are fired from the leafs to root
        private boolean removeDE(DocumentElement de) {
            if(debug) System.out.println("[DTM] removing " + de);
            
            //I need to get the parent before removing from the list!
            DocumentElement parent = null;
            
            int index = elements.indexof(de);
            if(index <= 0) {
                return false; //element not found or root element (cannot be removed)
            }
            
            //empty element parent optimalization
            //when a bigger part of a document is deleted then a number of 
            //elements gets to the same start and end offsets, in such
            //case having a simple last empty element offset -> parent cache helps a lot.
            int de_so = de.getStartOffset();
            int de_eo = de.getEndOffset();
            boolean empty = (de_eo - de_so) == 0;
            if(empty) { //empty element
                if(last_empty_element_start_offset == de_so) {
                    //we already know the parent
                    parent = last_parent;
                }
            }

            if(parent == null) {
                parent = getParent(index, de_so, de_eo);
                //cache the parent if empty element
                if(empty) {
                    last_empty_element_start_offset = de_so;
                    last_parent = parent;
                }
            }
            
            //get children of the element to be removed
            List<DocumentElement> children = empty ? null : de.getChildren();
            
            /* events firing:
             * If the removed element had a children, we have to fire add event
             * to the parent of the removed element for each child.
             */
            elements.remove(index);
            
            if(!empty) {
                //fire events for all affected children
                for(DocumentElement child : children) {
                    if(debug) System.out.println("switching child " + child + "from removed " + de + "to parent " + parent);
                    de.childRemoved(child);
                    parent.childAdded(child);
                }
            }
            
            //notify the parent element that one of its children has been removed
            if(parent != null) parent.childRemoved(de);
            
            fireDocumentModelEvent(de, ELEMENT_REMOVED);
            
            if(debug) System.out.println("[DMT] removed element " + de + " ;parent = " + parent);
            
            return true;
        }
        
        /** called by the DocumentModel when the document changes during model update (the trans. lifetime) */
        private void setTransactionCancelled() {
            transactionCancelled = true;
        }
        
        private final class DocumentModelModification {
            public static final int ELEMENT_ADD = 1;
            public static final int ELEMENT_REMOVED = 2;
            public static final int ELEMENT_CHANGED = 3;
            public static final int ELEMENT_ATTRS_CHANGED = 4;
            
            public int type;
            public DocumentElement de;
            public Map<String, String> attrs = null;
            
            public DocumentModelModification(DocumentElement de, int type) {
                this.de = de;
                this.type = type;
            }
            
            public DocumentModelModification(DocumentElement de, int type, Map<String, String> attrs) {
                this(de, type);
                this.attrs = attrs;
            }
            
            public boolean equals(Object o) {
                if(!(o instanceof DocumentModelModification)) return false;
                DocumentModelModification dmm = (DocumentModelModification)o;
                return (dmm.type == this.type) && (dmm.de.equals(this.de));
            }
        }
    }

    /** This exception is thrown when someone tries to add a request to an already cancelled transaction.*/
    public final class DocumentModelTransactionCancelledException extends Exception {
        
        public DocumentModelTransactionCancelledException() {
            super();
        }
        
    }
    
    //compares elements according to their start offsets
    //XXX - design - this comparator should be defined in DocumentElement class in the compareTo method!!!!
    public static final Comparator<DocumentElement> ELEMENTS_COMPARATOR = new Comparator<DocumentElement>() {
        public int compare(DocumentElement de1, DocumentElement de2) {
            //fastly handle root element comparing
            DocumentModel model = de1.getDocumentModel();
            if(model.isRootElement(de1) && !model.isRootElement(de2)) return -1;
            if(!model.isRootElement(de1) && model.isRootElement(de2)) return +1;
            if(model.isRootElement(de2) && model.isRootElement(de1)) return 0;
            
            int startOffsetDelta = de1.getStartOffset() - de2.getStartOffset();
            if(startOffsetDelta != 0)
                //different startOffsets
                return startOffsetDelta;
            else {
                //the elements has the same startoffsets - so we need
                //to compare them according to their endoffsets
                int endOffsetDelta = de2.getEndOffset() - de1.getEndOffset();
                if(endOffsetDelta != 0) {
                    return (de1.isEmpty() || de2.isEmpty()) ? -endOffsetDelta : endOffsetDelta;
                } else {
                    //because of TreeSet operations seems to use the comparator to test equality of elements
                    int typesDelta = de1.getType().compareTo(de2.getType());
                    if(typesDelta != 0) return typesDelta;
                    else {
                        int namesDelta = de1.getName().compareTo(de2.getName());
                        if(namesDelta != 0) return namesDelta;
                        else {
                            //                            //equality acc. to attribs causes problems with readding of elements in XMLDocumentModelProvider when changing attributes.
                            int attrsComp = ((DocumentElement.Attributes)de1.getAttributes()).compareTo(de2.getAttributes());
                            if(attrsComp != 0) {
                                return attrsComp;
                            } else {
                                //Compare according to their hashCodes
                                //the same element have the same hashcode
                                //This is necessary when more elements with the same name is deleted
                                //then they have the same start-end offsets so they "seem" to be
                                //the same, thought they aren't.
                                return de1.isEmpty() ? System.identityHashCode(de2) - System.identityHashCode(de1) : 0;
                            }
                        }
                    }
                }
            }
        }
        public boolean equals(Object obj) {
            return obj.equals(DocumentModel.ELEMENTS_COMPARATOR);
        }
    };
    
    private final class DocumentChangesWatcher implements DocumentListener {
        
        private ArrayList<DocumentChange> documentChanges = new ArrayList<DocumentChange>();
        
        public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
            //no need to handle document attributes changes
        }
        
        public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
            documentChanged(documentEvent);
        }
        
        public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
            documentChanged(documentEvent);
        }
        
        //assumption: this method contains no locking AFAIK
        private void documentChanged(DocumentEvent documentEvent) {
            if(!documentDirty) {
                fireSourceChanged();
            }
            //indicate that the synchronization between document content and the element may be broken
            //not used by the model logic itself but by the "resortElements" method.
            documentDirty = true;
            
            try {
                //test whether a new text was inserted before or after the root element boundaries (positions)
                if(getRootElement().getStartOffset() > 0 || getRootElement().getEndOffset() < getDocument().getLength()) {
                    getRootElement().setStartPosition(0);
                    getRootElement().setEndPosition(getDocument().getLength());
                }
                
                //TODO: here we have to decide whether the document change affects
                //the model and how.
                int change_offset = documentEvent.getOffset();
                int change_length = documentEvent.getLength();
                
                int type = documentEvent.getType().equals(EventType.REMOVE) ? DocumentChange.REMOVE : DocumentChange.INSERT;
                DocumentChange dchi = new DocumentChange(getDocument().createPosition(change_offset), change_length, type);
                documentChanges.add(dchi);
                if(debug) System.out.println(dchi);
            }catch(BadLocationException e) {
                e.printStackTrace();
            }
            
            requestModelUpdate(false);
        }
        
        public DocumentChange[] getDocumentChanges() {
            @SuppressWarnings("unchecked")
            List<DocumentChange> changes = (List<DocumentChange>)documentChanges.clone();
            return changes.toArray(new DocumentChange[]{});
        }
        
        public void clearChanges() {
            documentChanges.clear();
        }
        
    }
    
    /** A text document change wrapper similar to DocumentEvent. It stores the change type (remove/insert), its offset and length.
     * An array of these objects is passed into the DocumentModelProvider.updateModel().
     *
     * @see DocumentModel overall description for more infermation
     */
    public class DocumentChange {
        
        /** document text insert */
        public static final int INSERT=0;
        
        /** removal of a text from text document */
        public static final int REMOVE=1;
        
        private Position changeStart;
        private int changeLength, type;
        
        DocumentChange(Position changeStart, int changeLength, int type) {
            this.changeStart = changeStart;
            this.changeLength = changeLength;
            this.type = type;
        }
        
        /** @return a Position in the text document when the change started. */
        public Position getChangeStart() {
            return changeStart;
        }
        /** @return the length of the change. */
        public int getChangeLength() {
            return changeLength;
        }
        
        /** @return either DocumentChange.INSERT or DocumentChange.REMOVE */
        public int getChangeType() {
            return type;
        }
        
        public String toString() {
            return "Change["+getChangeStart().getOffset() + "-" + (getChangeStart().getOffset() + getChangeLength())+ "-" + (type == INSERT ? "INSERT" : "REMOVE") + "] text: " + getChangeText();
        }
        
        private String getChangeText() {
            try {
                String text = getDocument().getText(getChangeStart().getOffset(), getChangeLength());
                if(type == INSERT) return text;
                else if(type == REMOVE) return "[cannot provide removed text]; the text on remove offset: " + text;
                
                assert false : "Wrong document change type!";
            }catch(BadLocationException e) {
                return "BadLocationException thrown: " + e.getMessage();
            }
            return null; //why do I need this???? :-)
        }
        
    }
    
    /** A sorted array wrapper.
     * Access to elements is done directly to the encapsulated array.
     * add() and remove() methods must be used instead of direct modification of the array.
     */
    private static class ElementsArray {
        
        private DocumentElement[] elements;
        //internal array size pointer
        private int size;
        private int increase_capacity_delta;
        private boolean needs_resort;
        
        ElementsArray() {
            elements = new DocumentElement[1000];
            size = 0;
            increase_capacity_delta = 100;
            needs_resort = false;
        }
        
        /* Elements are added to the end of the array,
         * not sorted, not checked for duplicity.
         * resort() needs to be called before any
         * subsequent access (except next fastAdd() call),
         * otherwise IllegalStateException will be called.
         */
        public void fastAdd(DocumentElement de) {
            checkCapacity();
            elements[size++] = de;
            needs_resort = true;
        }
        
        /* ~ log(n) peformance */
        public void add(DocumentElement de) {
            checkIntegrity();
            int index = indexof(de);
            if(index >= 0) {
                return ; //already added, no update
            }
            checkCapacity();
            
            index = -index - 1; //switch to insert index
            checkIndex(index);
            int move = size - index - 1;
            if(move > 0) {
                System.arraycopy(elements, index, elements, index + 1, move);
            }
            elements[index] = de;
            size++;
        }
        
        public void remove(int index) {
            checkIntegrity();
            if(index < 0) {
                return ; //nothing to remove, no update
            }
            checkIndex(index);
            
            int move = size - index - 1;
            if (move > 0) {
                System.arraycopy(elements, index+1, elements, index, move);
            }
            elements[--size] = null;
        } 
        
        /* ~ log(n) peformance */
        public void remove(DocumentElement de) {
            checkIntegrity();
            remove(indexof(de));
        }
        
        /* ~ log(n) peformance */
        public int indexof(DocumentElement de) {
            checkIntegrity();
            return binarySearch(de);
        }
        
        public DocumentElement get(int index) {
            checkIntegrity();
            checkIndex(index);
            return elements[index];
        }
        
        public boolean contains(DocumentElement de) {
            return indexof(de) >= 0;
        }
        
        public int size() {
            checkIntegrity();
            return size;
        }
        
        /* n*log(n) performance */
        public void resort() {
            Arrays.sort(elements, 0, size, ELEMENTS_COMPARATOR);
            needs_resort = false;
        }
        
        public DocumentElement[] subarray(int from, int to) {
            checkIntegrity();
            checkIndex(from);
            if (to < 0 || to > size) {
                throw new IllegalArgumentException("Index out of range: " + to + " (size = " + size + ")");
            }

            int len = to - from;
            if(len < 0) {
                throw new IllegalArgumentException("from > to: " + from + " > " + to);
            }
            DocumentElement[] sub = new DocumentElement[len];
            System.arraycopy(elements, from, sub, 0, len);
            return sub;
        }
        
        private void checkIntegrity() {
            if(needs_resort) {
                throw new IllegalStateException("Elements are inconsistent - fastAdd() called without subsequent resort() call!");
            }
        }
        
        private void checkIndex(int index) {
            if (index < 0 || index >= size) {
                throw new IllegalArgumentException("Index out of range: " + index + " (size = " + size + ")");
            }
        }
        
        private void checkCapacity() {
            int free = elements.length - size;
            assert free >= 0;
            if(free == 0) {
                increase_capacity_delta *= 5; //load factor
                DocumentElement[] old_elements = elements;
                elements = new DocumentElement[size + increase_capacity_delta];
                System.arraycopy(old_elements, 0, elements, 0, old_elements.length);
            }
        }
        
        private int binarySearch(DocumentElement key) {
            int low = 0;
            int high = size() - 1;
            
            while (low <= high) {
                int mid = (low + high) >> 1;
                DocumentElement midVal = elements[mid];
                int cmp = ELEMENTS_COMPARATOR.compare(midVal, key);
                
                if (cmp < 0)
                    low = mid + 1;
                else if (cmp > 0)
                    high = mid - 1;
                else
                    return mid; // key found
            }
            return -(low + 1);  // key not found.
        }
        
        /** finds a DocumentElement starting on the given offset. 
         * Note: there might be mode elements with the same startposition.
         * The method can return any of them.
         */
        public int binarySearchForOffset(int offset) {
            checkIntegrity();
            
            int low = 0;
            int high = size() - 1;
            
            while (low <= high) {
                int mid = (low + high) >> 1;
                DocumentElement midVal = elements[mid];
                int so = midVal.getStartOffset();
                int cmp = so - offset;
                
                if (cmp < 0)
                    low = mid + 1;
                else if (cmp > 0)
                    high = mid - 1;
                else
                    return mid; // key found
            }
            return -(low + 1);  // key not found.
        }
        
    }
    
    //some measurement
    private long parent = 0;
    private long parent_count = 0;
    
    //performance hack>>>
    private DocumentElement last_parent = null; 
    private int last_empty_element_start_offset = -1;
    //<<<
    
    //root document element - always present in the model - even in an empty one
    private static final String DOCUMENT_ROOT_ELEMENT_TYPE = "ROOT_ELEMENT";
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.editor.model.debug");
    private static final boolean measure = Boolean.getBoolean("org.netbeans.editor.model.measure");
    
    private static final String GENERATING_MODEL_PROPERTY = "generating_document_model";
    
}
