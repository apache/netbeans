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

package org.netbeans.lib.editor.util.swing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.AbstractCharSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.CompactMap;
import org.openide.util.WeakListeners;

/**
 * Various utility methods related to swing text documents.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public final class DocumentUtilities {
    
    /** BaseDocument's version. */
    private static final String VERSION_PROP = "version"; //NOI18N
    private static final String LAST_MODIFICATION_TIMESTAMP_PROP = "last-modification-timestamp"; //NOI18N

    private static final Object TYPING_MODIFICATION_DOCUMENT_PROPERTY = new Object();
    
    private static final Object TYPING_MODIFICATION_KEY = new Object();
    
    private static volatile Field numReadersField;
    
    private static volatile Field currWriterField;
    
    
    private DocumentUtilities() {
        // No instances
    }

    /**
     * Add document listener to document with given priority
     * or default to using regular {@link Document#addDocumentListener(DocumentListener)}
     * if the given document is not listener priority aware.
     * 
     * @param doc document to which the listener should be added.
     * @param listener document listener to add.
     * @param priority priority with which the listener should be added.
     *  If the document does not support document listeners ordering
     *  then the listener is added in a regular way by using
     *  {@link javax.swing.text.Document#addDocumentListener(
     *  javax.swing.event.DocumentListener)} method.
     */
    public static void addDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        if (!addPriorityDocumentListener(doc, listener, priority))
            doc.addDocumentListener(listener);
    }
    
    /**
     * Suitable for document implementations - adds document listener
     * to document with given priority and does not do anything
     * if the given document is not listener priority aware.
     * <br>
     * Using this method in the document impls and defaulting
     * to super.addDocumentListener() in case it returns false
     * will ensure that there won't be an infinite loop in case the super constructors
     * would add some listeners prior initing of the priority listening.
     * 
     * @param doc document to which the listener should be added.
     * @param listener document listener to add.
     * @param priority priority with which the listener should be added.
     * @return true if the priority listener was added or false if the document
     *  does not support priority listening.
     */
    public static boolean addPriorityDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.add(listener, priority.getPriority());
            return true;
        } else
            return false;
    }

    /**
     * Remove document listener that was previously added to the document
     * with given priority or use default {@link Document#removeDocumentListener(DocumentListener)}
     * if the given document is not listener priority aware.
     * 
     * @param doc document from which the listener should be removed.
     * @param listener document listener to remove.
     * @param priority priority with which the listener should be removed.
     *  It should correspond to the priority with which the listener
     *  was added originally.
     */
    public static void removeDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        if (!removePriorityDocumentListener(doc, listener, priority))
            doc.removeDocumentListener(listener);
    }

    /**
     * Suitable for document implementations - removes document listener
     * from document with given priority and does not do anything
     * if the given document is not listener priority aware.
     * <br>
     * Using this method in the document impls and defaulting
     * to super.removeDocumentListener() in case it returns false
     * will ensure that there won't be an infinite loop in case the super constructors
     * would remove some listeners prior initing of the priority listening.
     * 
     * @param doc document from which the listener should be removed.
     * @param listener document listener to remove.
     * @param priority priority with which the listener should be removed.
     * @return true if the priority listener was removed or false if the document
     *  does not support priority listening.
     */
    public static boolean removePriorityDocumentListener(Document doc, DocumentListener listener,
    DocumentListenerPriority priority) {
        PriorityDocumentListenerList priorityDocumentListenerList
                = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class);
        if (priorityDocumentListenerList != null) {
            priorityDocumentListenerList.remove(listener, priority.getPriority());
            return true;
        } else
            return false;
    }

    /**
     * This method should be used by swing document implementations that
     * want to support document listeners prioritization.
     * <br>
     * It should be called from document's constructor in the following way:<pre>
     *
     * class MyDocument extends AbstractDocument {
     *
     *     MyDocument() {
     *         super.addDocumentListener(DocumentUtilities.initPriorityListening(this));
     *     }
     *
     *     public void addDocumentListener(DocumentListener listener) {
     *         if (!DocumentUtilities.addDocumentListener(this, listener, DocumentListenerPriority.DEFAULT))
     *             super.addDocumentListener(listener);
     *     }
     *
     *     public void removeDocumentListener(DocumentListener listener) {
     *         if (!DocumentUtilities.removeDocumentListener(this, listener, DocumentListenerPriority.DEFAULT))
     *             super.removeDocumentListener(listener);
     *     }
     *
     * }</pre>
     *
     *
     * @param doc document to be initialized.
     * @return the document listener instance that should be added as a document
     *   listener typically by using <code>super.addDocumentListener()</code>
     *   in document's constructor.
     * @throws IllegalStateException when the document already has
     *   the property initialized.
     */
    public static DocumentListener initPriorityListening(Document doc) {
        if (doc.getProperty(PriorityDocumentListenerList.class) != null) {
            throw new IllegalStateException(
                    "PriorityDocumentListenerList already initialized for doc=" + doc); // NOI18N
        }
        PriorityDocumentListenerList listener = new PriorityDocumentListenerList();
        doc.putProperty(PriorityDocumentListenerList.class, listener);
        return listener;
    }
    
    /**
     * Get total count of document listeners attached to a particular document
     * (useful e.g. for logging).
     * <br>
     * If the document uses priority listening then get the count of listeners
     * at all levels. If the document is not {@link AbstractDocument} the method
     * returns zero.
     * 
     * @param doc non-null document.
     * @return total count of document listeners attached to the document.
     */
    public static int getDocumentListenerCount(Document doc) {
        PriorityDocumentListenerList pdll;
        return (pdll = (PriorityDocumentListenerList)doc.getProperty(PriorityDocumentListenerList.class)) != null
                ? pdll.getListenerCount()
                : ((doc instanceof AbstractDocument)
                        ? ((AbstractDocument)doc).getListeners(DocumentListener.class).length
                        : 0);
    }

    /**
     * Mark that the ongoing document modification(s) will be caused
     * by user's typing.
     * It should be used by default-key-typed-action and the actions
     * for backspace and delete keys.
     * <br>
     * The document listeners being fired may
     * query it by using {@link #isTypingModification(Document)}.
     * This method should always be used in the following pattern:
     * <pre>
     * DocumentUtilities.setTypingModification(doc, true);
     * try {
     *     doc.insertString(offset, typedText, null);
     * } finally {
     *    DocumentUtilities.setTypingModification(doc, false);
     * }
     * </pre>
     *
     * @see #isTypingModification(Document)
     */
    public static void setTypingModification(Document doc, boolean typingModification) {
        doc.putProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY, Boolean.valueOf(typingModification));
    }
    
    /**
     * This method should be used by document listeners to check whether
     * the just performed document modification was caused by user's typing.
     * <br>
     * Certain functionality such as code completion or code templates
     * may benefit from that information. For example the java code completion
     * should only react to the typed "." but not if the same string was e.g.
     * pasted from the clipboard.
     *
     * @see #setTypingModification(Document, boolean)
     */
    public static boolean isTypingModification(Document doc) {
        Boolean b = (Boolean)doc.getProperty(TYPING_MODIFICATION_DOCUMENT_PROPERTY);
        return (b != null) ? b.booleanValue() : false;
    }

    /**
     * @deprecated
     * @see #isTypingModification(Document)
     */
    @Deprecated
    public static boolean isTypingModification(DocumentEvent evt) {
        return isTypingModification(evt.getDocument());
    }

    /**
     * Get text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @return non-null character sequence. Length of the character sequence
     *  is <code>doc.getLength() + 1</code> where the extra character is '\n'
     *  (it corresponds to AbstractDocument-based document implementations).
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc) {
        CharSequence text = (CharSequence)doc.getProperty(CharSequence.class);
        if (text == null) {
            text = new DocumentCharSequence(doc);
            doc.putProperty(CharSequence.class, text);
        }
        return text;
    }
    
    /**
     * Get a portion of text of the given document as char sequence.
     * <br>
     *
     * @param doc document for which the charsequence is being obtained.
     * @param offset starting offset of the charsequence to obtain.
     * @param length length of the charsequence to obtain. It must be <code>&gt;= 0</code>
     *   and <code>&lt;doc.getLength() + 1</code>.
     * @return non-null character sequence.
     * @exception BadLocationException some portion of the given range
     *   was not a valid part of the document. The location in the exception
     *   is the first bad position encountered.
     *  <br>
     *  The returned character sequence should only be accessed under
     *  document's readlock (or writelock).
     */
    public static CharSequence getText(Document doc, int offset, int length) throws BadLocationException {
        CharSequence text = getText(doc);
        try {
            return text.subSequence(offset, offset + length);
        } catch (IndexOutOfBoundsException e) {
            int badOffset = offset;
            if (offset >= 0 && offset + length > text.length()) {
                badOffset = length;
            }
            BadLocationException ble = new BadLocationException(e.getMessage(), badOffset);
            ble.initCause(e);
            throw ble;
        }
    }
    
    /**
     * Document provider should call this method to allow for document event
     * properties being stored in document events.
     *
     * @param evt document event to which the storage should be added.
     *   It must be an undoable edit allowing to add an edit.
     */
    public static void addEventPropertyStorage(DocumentEvent evt) {
        // Parameter is DocumentEvent because it's more logical
        if (!(evt instanceof UndoableEdit)) {
            throw new IllegalStateException("evt not instanceof UndoableEdit: " + evt); // NOI18N
        }
        ((UndoableEdit)evt).addEdit(new EventPropertiesElementChange());
    }
    
    /**
     * Get a property of a given document event.
     *
     * @param evt non-null document event from which the property should be retrieved.
     * @param key non-null key of the property.
     * @return value for the given property.
     */
    public static Object getEventProperty(DocumentEvent evt, Object key) {
        EventPropertiesElementChange change = (EventPropertiesElementChange)
                evt.getChange(EventPropertiesElement.INSTANCE);
        return (change != null) ? change.getProperty(key) : null;
    }
    
    /**
     * Sets a property for the document event, if the event supports the feature.
     * Returns true, if the property was actually set.
     * 
     * @param evt the event to modify
     * @param key property key
     * @param value property value
     * @return true, if the property was set
     * @since 1.59
     */
    public static boolean putEventPropertyIfSupported(DocumentEvent evt, Object key, Object value) {
        EventPropertiesElementChange change = (EventPropertiesElementChange)
                evt.getChange(EventPropertiesElement.INSTANCE);
        if (change != null) {
            putEventProperty(evt, key, value);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Set a property of a given document event.
     *
     * @param evt non-null document event to which the property should be stored.
     * @param key non-null key of the property.
     * @param value for the given property.
     */
    public static void putEventProperty(DocumentEvent evt, Object key, Object value) {
        EventPropertiesElementChange change = (EventPropertiesElementChange)
                evt.getChange(EventPropertiesElement.INSTANCE);
        if (change == null) {
            throw new IllegalStateException("addEventPropertyStorage() not called for evt=" + evt); // NOI18N
        }
        change.putProperty(key, value);
    }
    
    /**
     * Set a property of a given document event by using the given map entry.
     * <br>
     * The present implementation is able to directly store instances
     * of <code>CompactMap.MapEntry</code>. Other map entry implementations
     * will be delegated to {@link #putEventProperty(DocumentEvent, Object, Object)}.
     *
     * @param evt non-null document event to which the property should be stored.
     * @param mapEntry non-null map entry which should be stored.
     *  Generally after this method finishes the {@link #getEventProperty(DocumentEvent, Object)}
     *  will return <code>mapEntry.getValue()</code> for <code>mapEntry.getKey()</code> key.
     */
    public static void putEventProperty(DocumentEvent evt, Map.Entry mapEntry) {
        if (mapEntry instanceof CompactMap.MapEntry) {
            EventPropertiesElementChange change = (EventPropertiesElementChange)
                    evt.getChange(EventPropertiesElement.INSTANCE);
            if (change == null) {
                throw new IllegalStateException("addEventPropertyStorage() not called for evt=" + evt); // NOI18N
            }
            change.putEntry((CompactMap.MapEntry)mapEntry);

        } else {
            putEventProperty(evt, mapEntry.getKey(), mapEntry.getValue());
        }
    }
    
    /**
     * Fix the given offset according to the performed modification.
     * 
     * @param offset >=0 offset in a document.
     * @param evt document event describing change in the document.
     * @return offset updated by applying the document change to the offset.
     */
    public static int fixOffset(int offset, DocumentEvent evt) {
        int modOffset = evt.getOffset();
        if (evt.getType() == DocumentEvent.EventType.INSERT) {
            if (offset >= modOffset) {
                offset += evt.getLength();
            }
        } else if (evt.getType() == DocumentEvent.EventType.REMOVE) {
            if (offset > modOffset) {
                offset = Math.max(offset - evt.getLength(), modOffset);
            }
        }
        return offset;
    }
    
    /**
     * Get text of the given document modification.
     * <br>
     * It's implemented as retrieving of a <code>String.class</code>.
     *
     * @param evt document event describing either document insertion or removal
     *  (change event type events will produce null result).
     * @return text that was inserted/removed from the document by the given
     *  document modification or null if that information is not provided
     *  by that document event.
     */
    public static String getModificationText(DocumentEvent evt) {
        return (String)getEventProperty(evt, String.class);
    }
    
    /**
     * Check whether the given document is read-locked by at least one thread
     * or whether it was write-locked by the current thread (write-locking
     * grants the read-access automatically).
     * <br>
     * The method currently only works for {@link javax.swing.text.AbstractDocument}
     * based documents and it uses reflection.
     * <br>
     * Unfortunately the AbstractDocument only records number of read-lockers
     * but not the thread references that performed the read-locking. Thus it can't be verified
     * whether current thread has performed read locking or another thread.
     * 
     * @param doc non-null document instance.
     * @return true if the document was read-locked by some thread
     *   or false if not (or if doc not-instanceof AbstractDocument).
     * @since 1.17
     */
    public static boolean isReadLocked(Document doc) {
        if (checkAbstractDoc(doc)) {
            if (isWriteLocked(doc)) {
                return true;
            }
            Field f = numReadersField;
            if (f == null) {
                try {
                    f = AbstractDocument.class.getDeclaredField("numReaders"); // NOI18N
                } catch (NoSuchFieldException ex) {
                    throw new IllegalStateException(ex);
                }
                f.setAccessible(true);
                numReadersField = f;
            }
            try {
                return f.getInt(doc) > 0;
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Method m = lockMethods(doc)[0];
        try {
            return m == null ? false : (Boolean)m.invoke(doc);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DocumentUtilities.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * Check whether the given document is write-locked by the current thread.
     * <br>
     * The method currently only works for {@link javax.swing.text.AbstractDocument}
     * based documents and it uses reflection.
     * 
     * @param doc non-null document instance.
     * @return true if the document was write-locked by the current thread
     *   or false if not (or if doc not-instanceof AbstractDocument).
     * @since 1.17
     */
    public static boolean isWriteLocked(Document doc) {
        if (checkAbstractDoc(doc)) {
            Field f = currWriterField;
            if (f == null) {
                try {
                    f = AbstractDocument.class.getDeclaredField("currWriter"); // NOI18N
                } catch (NoSuchFieldException ex) {
                    throw new IllegalStateException(ex);
                }
                // TODO adds --add-opens=java.desktop/javax.swing.text=ALL-UNNAMED
                f.setAccessible(true);
                currWriterField = f;
            }
            try {
                return f.get(doc) == Thread.currentThread();
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Method m = lockMethods(doc)[1];
        try {
            return m == null ? false : (Boolean)m.invoke(doc);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DocumentUtilities.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private static final Map<Class, Method[]> documentClassAccessors = new HashMap<>();
    
    private static Method[] lockMethods(Document d) {
        Method[] res;
        synchronized(documentClassAccessors) {
            Class c = d.getClass();
            res = documentClassAccessors.get(c);
            if (res == null) {
                res = new Method[2];
                try {
                    res[0] = c.getMethod("isReadLocked");
                    if (res[0].getReturnType() != Boolean.TYPE) {
                        res[0] = null;
                    }
                    res[1] = c.getMethod("isWriteLocked");
                    if (res[1].getReturnType() != Boolean.TYPE) {
                        res[1] = null;
                    }
                } catch (NoSuchMethodException ex) {
                    // expected
                } catch (SecurityException ex) {
                    // expected
                }
                documentClassAccessors.put(c, res);
            }
        }
        return res;
    }
    
    private static boolean checkAbstractDoc(Document doc) {
        if (doc == null)
            throw new IllegalArgumentException("document is null"); // NOI18N
        return (doc instanceof AbstractDocument);
    }
    
    /**
     * Get the paragraph element for the given document.
     *
     * @param doc non-null document instance.
     * @param offset offset in the document >=0
     * @return paragraph element containing the given offset.
     */
    public static Element getParagraphElement(Document doc, int offset) {
        Element paragraph;
        if (doc instanceof StyledDocument) {
            paragraph = ((StyledDocument)doc).getParagraphElement(offset);
        } else {
            Element rootElem = doc.getDefaultRootElement();
            int index = rootElem.getElementIndex(offset);
            paragraph = rootElem.getElement(index);
            if ((offset < paragraph.getStartOffset()) || (offset >= paragraph.getEndOffset())) {
                paragraph = null;
            }
        }
        return paragraph;
    }
    
    /**
     * Get the root of the paragraph elements for the given document.
     *
     * @param doc non-null document instance.
     * @return root element of the paragraph elements.
     */
    public static Element getParagraphRootElement(Document doc) {
        if (doc instanceof StyledDocument) {
            return ((StyledDocument)doc).getParagraphElement(0).getParentElement();
        } else {
            return doc.getDefaultRootElement().getElement(0).getParentElement();
        }
    }

    /**
     * Get string representation of an offset for debugging purposes
     * in form "offset[line:column]". Both lines and columns start counting from 1
     * like in the editor's status bar. Tabs are expanded when counting the column.
     *
     * @param doc non-null document in which the offset is located.
     * @param offset offset in the document.
     * @return string representation of the offset.
     * @since 1.25
     */
    public static String debugOffset(Document doc, int offset) {
        return appendOffset(null, doc, offset).toString();
    }

    /**
     * Get string representation of an offset for debugging purposes
     * in form "offset[line:column]". Both lines and columns start counting from 1
     * like in the editor's status bar. Tabs are expanded when counting the column.
     *
     * @param sb valid string builder to which text will be appended or null in which case
     *  the method itself will create a string builder and it will return it.
     * @param doc non-null document in which the offset is located.
     * @param offset offset in the document.
     * @return non-null string builder to which the description was added.
     * @since 1.27
     */
    public static StringBuilder appendOffset(StringBuilder sb, Document doc, int offset) {
        if (sb == null) {
            sb = new StringBuilder(50);
        }
        sb.append(offset).append('[');
        if (offset < 0) { // Offset too low
            sb.append("<0");
        } else if (offset > doc.getLength() + 1) { // +1 for AbstractDocument-based docs
            sb.append(">").append(doc.getLength());
        } else { // Valid offset
            Element paragraphRoot = getParagraphRootElement(doc);
            int lineIndex = paragraphRoot.getElementIndex(offset);
            Element lineElem = paragraphRoot.getElement(lineIndex);
            sb.append(lineIndex + 1).append(':'); // Line
            sb.append(visualColumn(doc, lineElem.getStartOffset(), offset) + 1); // Column
        }
        sb.append(']');
        return sb;
    }

    /**
     * Get string representation of an offset for debugging purposes
     * in form "offset[line:column]". Both lines and columns start counting from 1
     * like in the editor's status bar. Tabs are expanded when counting the column.
     *
     * @param sb valid string builder to which text will be appended or null in which case
     *  the method itself will create a string builder and it will return it.
     * @param evt non-null document event.
     * @return non-null string builder to which the description was added.
     * @since 1.27
     */
    public static StringBuilder appendEvent(StringBuilder sb, DocumentEvent evt) {
        if (sb == null) {
            sb = new StringBuilder(100);
        }
        DocumentEvent.EventType type = evt.getType();
        sb.append(type).append(", ");
        appendOffset(sb, evt.getDocument(), evt.getOffset());
        sb.append(", l=").append(evt.getLength());
        // Possibly append the modification text
        String modText;
        if ((modText = getModificationText(evt)) != null) {
            sb.append(", modText=\"");
            CharSequenceUtilities.debugText(sb, modText);
            sb.append('"');
        }
        return sb;
    }

    private static int visualColumn(Document doc, int lineStartOffset, int offset) {
        Integer tabSizeInteger = (Integer) doc.getProperty(PlainDocument.tabSizeAttribute);
        int tabSize = (tabSizeInteger != null) ? tabSizeInteger : 8;
        CharSequence docText = getText(doc);
        // Expected that offset <= docText.length()
        int column = 0;
        for (int i = lineStartOffset; i < offset; i++) {
            char c = docText.charAt(i);
            if (c == '\t') {
                column = (column + tabSize) / tabSize * tabSize;
            } else {
                column++;
            }
        }
        return column;
    }

    /**
     * Implementation of the character sequence for a generic document
     * that does not provide its own implementation of character sequence.
     */
    private static final class DocumentCharSequence extends AbstractCharSequence.StringLike {
        
        private final Segment segment = new Segment();
        
        private final Document doc;
        
        DocumentCharSequence(Document doc) {
            this.doc = doc;
        }

        public int length() {
            // Assuming AbstractDocument-based contents which have mandatory extra '\n' at end
            return doc.getLength() + 1;
        }

        public synchronized char charAt(int index) {
            try {
                doc.getText(index, 1, segment);
            } catch (BadLocationException e) {
                IndexOutOfBoundsException ioobe = new IndexOutOfBoundsException(e.getMessage()
                    + " at offset=" + e.offsetRequested()); // NOI18N
                ioobe.initCause(e);
                throw ioobe;
            }
            char ch = segment.array[segment.offset];
            segment.array = null; // Allow GC of large char arrays
            return ch;
        }

    }
    
    /**
     * Helper element used as a key in searching for an element change
     * being a storage of the additional properties in a document event.
     */
    private static final class EventPropertiesElement implements Element {
        
        static final EventPropertiesElement INSTANCE = new EventPropertiesElement();
        
        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return 0;
        }

        public int getElementCount() {
            return 0;
        }

        public int getElementIndex(int offset) {
            return -1;
        }

        public Element getElement(int index) {
            return null;
        }

        public boolean isLeaf() {
            return true;
        }

        public Element getParentElement() {
            return null;
        }

        public String getName() {
            return "Helper element for modification text providing"; // NOI18N
        }

        public Document getDocument() {
            return null;
        }

        public javax.swing.text.AttributeSet getAttributes() {
            return null;
        }
        
        public @Override String toString() {
            return getName();
        }

    } // End of EventPropertiesElement class
    
    private static final class EventPropertiesElementChange
    implements DocumentEvent.ElementChange, UndoableEdit  {
        
        private CompactMap eventProperties = new CompactMap();
        
        public synchronized Object getProperty(Object key) {
            return (eventProperties != null) ? eventProperties.get(key) : null;
        }

        @SuppressWarnings("unchecked")
        public synchronized Object putProperty(Object key, Object value) {
            return eventProperties.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public synchronized CompactMap.MapEntry putEntry(CompactMap.MapEntry entry) {
            return eventProperties.putEntry(entry);
        }

        public int getIndex() {
            return -1;
        }

        public Element getElement() {
            return EventPropertiesElement.INSTANCE;
        }

        public Element[] getChildrenRemoved() {
            return null;
        }

        public Element[] getChildrenAdded() {
            return null;
        }

        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        public boolean addEdit(UndoableEdit anEdit) {
            return false;
        }

        public void undo() throws CannotUndoException {
            // do nothing
        }

        public void redo() throws CannotRedoException {
            // do nothing
        }

        public boolean isSignificant() {
            return false;
        }

        public String getUndoPresentationName() {
            return "";
        }

        public String getRedoPresentationName() {
            return "";
        }

        public String getPresentationName() {
            return "";
        }

        public void die() {
            // do nothing
        }

        public boolean canUndo() {
            return true;
        }

        public boolean canRedo() {
            return true;
        }

    }
    
    /**
     * Gets the mime type of a document. If the mime type can't be determined
     * this method will return <code>null</code>. This method should work reliably
     * for Netbeans documents that have their mime type stored in a special
     * property. For any other documents it will probably just return <code>null</code>.
     * 
     * @param doc The document to get the mime type for.
     * 
     * @return The mime type of the document or <code>null</code>.
     * @see <a href="@org-netbeans-modules-editor-lib@/org/netbeans/editor/BaseDocument.html#MIME_TYPE_PROP" >NbEditorDocument#MIME_TYPE_PROP</a>
     * @since 1.23
     */
    public static String getMimeType(Document doc) {
        return (String)doc.getProperty("mimeType"); //NOI18N
    }

    /**
     * Gets the mime type of a document in <code>JTextComponent</code>. If
     * the mime type can't be determined this method will return <code>null</code>.
     * It tries to determine the document's mime type first and if that does not
     * work it uses mime type from the <code>EditorKit</code> attached to the
     * component.
     * 
     * @param component The component to get the mime type for.
     * 
     * @return The mime type of a document opened in the component or <code>null</code>.
     * @since 1.23
     */
    public static String getMimeType(JTextComponent component) {
        Document doc = component.getDocument();
        String mimeType = getMimeType(doc);
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }

    /**
     * Attempts to get the version of a <code>Document</code>. Netbeans editor
     * documents are versioned, which means that every time a document is modified
     * its version is incremented. This method can be used to read the latest version
     * of a netbeans document.
     * 
     * @param doc The document to get a version for.
     *
     * @return The document's version or <code>0</code> if the document does not
     *   support versioning (ie. is not a netbeans editor document).
     *
     * @since 1.27
     */
    public static long getDocumentVersion(Document doc) {
        Object version = doc.getProperty(VERSION_PROP);
        return version instanceof AtomicLong ? ((AtomicLong) version).get() : 0;
    }

    /**
     * Attempts to get the timestamp of a <code>Document</code>. Netbeans editor
     * documents are versioned and timestamped whenever they are modified.
     * This method can be used to read the timestamp of the most recent modification.
     * The timestamp is a number of milliseconds returned from <code>System.currentTimeMillis()</code>
     * at the document modification.
     *
     * @param doc The document to get the timestamp for.
     *
     * @return The document's timestamp or <code>0</code> if the document does not
     *   support timestamps (ie. is not a netbeans editor document).
     *
     * @since 1.34
     */
    public static long getDocumentTimestamp(Document doc) {
        Object version = doc.getProperty(LAST_MODIFICATION_TIMESTAMP_PROP);
        return version instanceof AtomicLong ? ((AtomicLong) version).get() : 0;
    }

    /**
     * Adds <code>PropertyChangeListener</code> to a document.
     *
     * <p>In general, document properties are key-value pairs where both the key
     * and the value can be any <code>Object</code>. Contrary to that <code>PropertyChangeListener</code>s
     * can only handle named properties that can have an arbitrary value, but have <code>String</code> names.
     * Therefore the listenera attached to a document will only ever recieve document
     * properties, which keys are of <code>java.lang.String</code> type.
     *
     * <p>Additionally, the list of document properties that clients can listen on
     * is not part of this contract.
     *
     * <p>Note that this method does <em>not</em> work with {@code WeakListeners}.
     * Use {@link #addWeakPropertyChangeListener} for that purpose.
     *
     * @param doc The document to add the listener to.
     * @param l The listener to add to the document.
     *
     * @since 1.35
     */
    public static void addPropertyChangeListener(Document doc, PropertyChangeListener l) {
        PropertyChangeSupport pcs = (PropertyChangeSupport) doc.getProperty(PropertyChangeSupport.class);
        if (pcs != null) {
            pcs.addPropertyChangeListener(l);
        }
    }

    /**
     * Removes <code>PropertyChangeListener</code> from a document.
     *
     * @param doc The document to remove the listener from.
     * @param l The listener to remove from the document.
     *
     * @since 1.35
     */
    public static void removePropertyChangeListener(Document doc, PropertyChangeListener l) {
        PropertyChangeSupport pcs = (PropertyChangeSupport) doc.getProperty(PropertyChangeSupport.class);
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    /**
     * Adds a weak <code>PropertyChangeListener</code> to a document.
     *
     * <p>In general, document properties are key-value pairs where both the key
     * and the value can be any <code>Object</code>. Contrary to that <code>PropertyChangeListener</code>s
     * can only handle named properties that can have an arbitrary value, but have <code>String</code> names.
     * Therefore the listenera attached to a document will only ever recieve document
     * properties, which keys are of <code>java.lang.String</code> type.
     *
     * <p>Additionally, the list of document properties that clients can listen on
     * is not part of this contract.
     *
     * @param doc The document to add the listener to.
     * @param listenerImplementation The listener to be added weakly to the document.
     * @return the created weak listener - only the returned listener can be
     * used with {@link #removePropertyChangeListener}. If the document does not
     * support {@code PropertyChangeLister} {@code null} is returned.
     *
     * @since 1.68
     */
    public static PropertyChangeListener addWeakPropertyChangeListener(Document doc, PropertyChangeListener listenerImplementation) {
        PropertyChangeSupport pcs = (PropertyChangeSupport) doc.getProperty(PropertyChangeSupport.class);
        PropertyChangeListener weakListener = null;
        if (pcs != null) {
            weakListener = WeakListeners.propertyChange(listenerImplementation, pcs);
            pcs.addPropertyChangeListener(weakListener);
        }
        return weakListener;
    }
}
