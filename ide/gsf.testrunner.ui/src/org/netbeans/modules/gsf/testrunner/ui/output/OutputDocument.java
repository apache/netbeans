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

package org.netbeans.modules.gsf.testrunner.ui.output;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

/**
 * Simple document for displaying textual output produced by JUnit tests.
 *
 * @author  Marian Petras
 */
public final class OutputDocument implements Document {

    public static final AttributeSet attrs = new TrivialAttributeSet();

    final RootElement rootElement;

    private DocElement[] docElements = new DocElement[100];
    private int docElementsCount;
    private final DocElement lastDocElement
                             = new DocElement(0, "", false);            //NOI18N
    private int length = 0;

    private final Position startPosition = new SimplePosition(0);
    private final Position endPosition = new EndPosition();
    private final Element[] rootElements;

    private Map<Object,Object> properties;
    private DocumentListener[] docListeners;

    OutputDocument() {
        rootElement = new RootElement();
        rootElements = new Element[] { rootElement };

        docElements[0] = lastDocElement;
        docElementsCount = 1;
    }

    public int getLength() {
        return length;
    }

    public void addDocumentListener(DocumentListener listener) {
        if (listener == null) {
            return;
        }

        if (docListeners == null) {
            docListeners = new DocumentListener[1];
            docListeners[0] = listener;
        } else {
            DocumentListener[] oldArr = docListeners;
            docListeners = new DocumentListener[oldArr.length + 1];
            System.arraycopy(oldArr, 0,
                             docListeners, 0,
                             oldArr.length);
            docListeners[oldArr.length] = listener;
        }
    }

    public void removeDocumentListener(DocumentListener listener) {
        if (listener == null) {
            return;
        }

        if (docListeners == null) {
            return;
        }

        int index = -1;
        for (int i = 0; i < docListeners.length; i++) {
            if (docListeners[i] == listener) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            if (docListeners.length == 1) {
                docListeners = null;
            } else {
                DocumentListener[] oldArr = docListeners;
                docListeners = new DocumentListener[oldArr.length - 1];
                if (index != 0) {
                    System.arraycopy(oldArr, 0,
                                     docListeners, 0,
                                     index);
                }
                if (index != oldArr.length - 1) {
                    System.arraycopy(oldArr, index + 1,
                                     docListeners, index,
                                     oldArr.length - (index + 1));
                }
            }
        }
    }

    public void addUndoableEditListener(UndoableEditListener listener) {
        //no-op
    }

    public void removeUndoableEditListener(UndoableEditListener listener) {
        //no-op
    }

    public Object getProperty(Object key) {
        return (properties != null) ? properties.get(key) : null;
    }

    public void putProperty(Object key, Object value) {
        if (properties == null) {
            properties = new HashMap<Object,Object>(7);
        }
        properties.put(key, value);
    }

    public void remove(int offset, int len) throws BadLocationException {
        checkLocation(offset);
        assert false : "modification is not supported";                 //NOI18N
    }

    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        checkLocation(offset);
        if (offset != getLength()) {
            assert false : "modification is not supported";             //NOI18N
            return;
        }

        final int strLen = str.length();
        if (strLen == 0) {
            return;
        }

        if (docElementsCount == docElements.length) {
            DocElement[] oldElems = docElements;
            int oldCapacity = oldElems.length;
            int newCapacity = (oldCapacity < 100000) ? oldCapacity * 2
                                                     : oldCapacity * 14 / 10;
            docElements = new DocElement[newCapacity];
            System.arraycopy(oldElems, 0,
                             docElements, 0,
                             docElementsCount - 1);     //minus 'lastElement'
        }

        DocElement newElem = new DocElement(offset, str, a != null);
        int index = docElementsCount - 1;
        docElements[index] = newElem;
        docElements[docElementsCount++] = lastDocElement;

        lastDocElement.offset = offset + strLen;

        length += strLen;

        fireTextAppended(newElem, index);
    }

    final class DocElement implements Element {

        int offset;
        final int length;
        String text;
        char[] chars;
        final boolean isError;

        DocElement(int offset, String text, boolean isError) {
            this.offset = offset;
            this.text = text;
            this.chars = null;
            this.length = text.length();
            this.isError = isError;
        }

        char[] getChars() {
            if (chars == null) {
                chars = text.toCharArray();
                text = null;
            }
            return chars;
        }

        String getString() {
            if (text == null) {
                text = new String(chars);
                chars = null;
            }
            return text;
        }

        void appendToBuf(StringBuilder buf) {
            if (text != null) {
                buf.append(text);
            } else {
                assert (chars != null);
                buf.append(chars);
            }
        }

        void appendToBuf(StringBuilder buf, int offset) {
            int innerOffset = offset - this.offset;
            if (text != null) {
                buf.append(text, innerOffset, length);
            } else {
                assert (chars != null);
                buf.append(chars, innerOffset, length - innerOffset);
            }
        }

        void appendToBuf(StringBuilder buf, int startOffset, int endOffset) {
            if (text != null) {
                buf.append(text, startOffset - offset, endOffset - offset);
            } else {
                assert (chars != null);
                buf.append(chars, startOffset - offset, endOffset - startOffset);
            }
        }

        public Document getDocument() {
            return OutputDocument.this;
        }

        public Element getParentElement() {
            return rootElement;
        }

        public String getName() {
            return "DocElement";
        }

        public AttributeSet getAttributes() {
            return attrs;
        }

        public int getStartOffset() {
            return offset;
        }

        public int getEndOffset() {
            return offset + length;
        }

        public int getElementIndex(int offset) {
             throw new UnsupportedOperationException("Not supported.");
        }

        public int getElementCount() {
            return 0;
        }

        public Element getElement(int index) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean isLeaf() {
            return true;
        }

    }

    /* element index information cache: */
    int cachedOffsetStart = -1;
    int cachedOffsetEnd = -1;
    int cachedIndex = -1;

    int getElementIndex(int offset) {
        if (cachedIndex != -1) {

            /* First, try the cache: */
            if ((offset == cachedOffsetStart) 
                   || (offset > cachedOffsetStart) && (offset < cachedOffsetEnd)) {
                return cachedIndex;
            }

            /*
             * OK, the cached did not hit the offset.
             * Before going to the CPU-intensive cache, try whether element
             * following the cached one is not the one we are looking for:
             */
            if (offset == cachedOffsetEnd) {
                DocElement docElement = docElements[cachedIndex + 1];
                assert docElement.offset == offset;
                cachedIndex++;
                cachedOffsetStart = cachedOffsetEnd;
                cachedOffsetEnd = cachedOffsetStart + docElement.length;
                return cachedIndex;
            }
        }

        if (offset == 0) {
            return 0;
        }
        if (offset >= length) {
            return docElementsCount - 1;
        }

        /* We did not get the index from the cache. So do some actual work: */
        int startIndex;
        int endIndex;
        if (cachedIndex == -1) {
            startIndex = 0;
            endIndex = docElementsCount - 1;
        } else if (offset > cachedOffsetEnd) {
            startIndex = cachedIndex + 1;
            endIndex = docElementsCount - 1;
        } else {
            startIndex = 0;
            endIndex = cachedIndex;
        }
        int cycles = 0;
        while ((endIndex - startIndex) > 3) {
            cycles++;
            int middle = (startIndex + endIndex + 1) / 2;
            if (docElements[middle].offset >= offset) {
                endIndex = middle;
            } else {
                startIndex = middle;
            }
        }
        int index = startIndex;
        while (docElements[index].offset < offset) {
            cycles++;
            index++;
        }
        if (docElements[index].offset != offset) {
            index--;
        }

        DocElement docElem = docElements[index];

        if ((cachedIndex == -1) || (index >= cachedIndex)) {
            cachedIndex = index;
            cachedOffsetStart = docElem.offset;
            cachedOffsetEnd = docElem.offset + docElem.length;
        }
        return index;
    }

    /**
     * Notifies document listeners that a text has been appended.
     * 
     * @param  length  length of the appended text
     */
    private void fireTextAppended(DocElement newElem, int index) {
        if (docListeners != null) {
            DocumentEvent e = new DocInsertEvent(newElem, index);
            for (int i = 0; i < docListeners.length; i++) {
                docListeners[i].insertUpdate(e);
            }
        }
    }

    public String getText(int offset, int length) throws BadLocationException {
        checkLocation(offset);
        if (length < 0) {
            throw new BadLocationException("negative length", offset);  //NOI18N
        }
        if (offset + length > getLength()) {
            throw new BadLocationException(
                    "(offset[" + offset + "] + length[" + length        //NOI18N
                    + "]) go beyond total length[" + getLength() + ']', //NOI18N
                    getLength());
        }

        if (length == 0) {
            return "";                                                  //NOI18N
        }

        int elemIndex = getElementIndex(offset);
        DocElement docElem = docElements[elemIndex];
        if ((offset == docElem.offset) && (length == docElem.length)) {
            return docElem.getString();
        } else if (offset + length <= docElem.offset + docElem.length) {
            return docElem.getString().substring(offset - docElem.offset,
                                                 offset - docElem.offset + length);
        } else {
            int finalOffset = offset + length;
            StringBuilder buf = new StringBuilder(length);

            /* append the first line: */
            if (docElem.offset == offset) {
                docElem.appendToBuf(buf);
            } else {
                docElem.appendToBuf(buf, offset);
            }

            /* append whole lines: */
            while ((docElem = docElements[++elemIndex]).offset < finalOffset) {
                docElem.appendToBuf(buf);
            }

            /* append the last line: */
            if (finalOffset > docElem.offset) {
                docElem.appendToBuf(buf, docElem.offset, finalOffset);
            }

            return buf.toString();
        }
    }

    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        checkLocation(offset);
        if (length < 0) {
            throw new BadLocationException("negative length", offset);  //NOI18N
        }
        if (length == 0) {
            txt.array = new char[0];
            txt.offset = 0;
            txt.count = 0;
            return;
        }
        if (offset + length > getLength()) {
            throw new BadLocationException("too long text requested",   //NOI18N
                                           getLength());
        }

        int elemIndex = getElementIndex(offset);
        DocElement docElem = docElements[elemIndex];
        if ((offset == docElem.offset) && (length == docElem.length)) {
            txt.array = docElem.getChars();
            txt.offset = 0;
            txt.count = length;
        } else if (offset + length <= docElem.offset + docElem.length) {
            txt.array = docElem.getChars();
            txt.offset = offset - docElem.offset;
            txt.count = length;
        } else if (txt.isPartialReturn()) {
            txt.array = docElem.getChars();
            txt.offset = offset - docElem.offset;
            txt.count = docElem.offset + docElem.length - offset;
        } else {
            int finalOffset = offset + length;
            int charsStoredCount;
            char[] result = new char[length];

            /* append the first line: */
            System.arraycopy(docElem.getChars(), offset - docElem.offset,
                             result, 0,
                             charsStoredCount = docElem.offset + docElem.length - offset);

            /* append whole lines: */
            while ((docElem = docElements[++elemIndex]).offset < finalOffset) {
                System.arraycopy(docElem.getChars(), 0,
                                 result, charsStoredCount,
                                 docElem.length);
                charsStoredCount += docElem.length;
            }

            /* append the last line: */
            if (docElem.offset < finalOffset) {
                System.arraycopy(docElem.getChars(), 0,
                                 result, charsStoredCount,
                                 finalOffset - docElem.offset);
            }

            txt.array = result;
            txt.offset = 0;
            txt.count = length;
        }
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getEndPosition() {
        return endPosition;
    }

    public Position createPosition(int offset) throws BadLocationException {
        checkLocation(offset);
        return (offset != length) ? new SimplePosition(offset)
                                  : new EndPosition();
    }

    private void checkLocation(int offset) throws BadLocationException {
        if (offset < 0) {
            throw new BadLocationException(
                    "negative offset",                                  //NOI18N
                    offset);
        }
        if (offset > length) {
            throw new BadLocationException(
                    "offset > length (" + length + ')',                 //NOI18N
                    offset);
        }
    }

    public Element[] getRootElements() {
        return rootElements;
    }

    public Element getDefaultRootElement() {
        return rootElement;
    }

    public void render(Runnable r) {
        assert EventQueue.isDispatchThread();
        r.run();
    }

    private static final class SimplePosition implements Position {

        private final int offset;

        SimplePosition(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            assert EventQueue.isDispatchThread();
            return offset;
        }

    }

    private final class EndPosition implements Position {

        public int getOffset() {
            assert EventQueue.isDispatchThread();
            return getLength();
        }

    }

    private final class DocInsertEvent implements DocumentEvent, DocumentEvent.ElementChange {
        private final int index;
        private final DocElement docElem;
        private Element[] childrenAdded;
        private DocInsertEvent(DocElement docElem, int index) {
            this.docElem = docElem;
            this.index = index;
        }
        public int getOffset() { return docElem.offset; }
        public int getLength() { return docElem.length; }
        public Document getDocument() { return OutputDocument.this; }
        public EventType getType() { return EventType.INSERT; }
        public ElementChange getChange(Element elem) {
            return (elem == OutputDocument.this.rootElement) ? this : null;
        }

        public Element getElement() {
            return OutputDocument.this.rootElement;
        }

        public int getIndex() {
            return index;
        }

        public Element[] getChildrenRemoved() {
            return null;
        }

        public Element[] getChildrenAdded() {
            if (childrenAdded == null) {
                childrenAdded = new Element[] { docElem };
            }
            return childrenAdded;
        }
    }

    final class RootElement implements Element {

        private static final String ROOT_NAME = "root element";

        RootElement() {
        }

        public Document getDocument() {
            return OutputDocument.this;
        }

        public Element getParentElement() {
            return null;
        }

        public String getName() {
            return ROOT_NAME;
        }

        public AttributeSet getAttributes() {
            return attrs;
        }

        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return OutputDocument.this.getLength();
        }

        public int getElementIndex(int offset) {
            return OutputDocument.this.getElementIndex(offset);
        }

        public int getElementCount() {
            return OutputDocument.this.docElementsCount;
        }

        public Element getElement(int index) {
            return OutputDocument.this.docElements[index];
        }

        DocElement getDocElement(int index) {
            return OutputDocument.this.docElements[index];
        }

        public boolean isLeaf() {
            return false;
        }

    }

    private static final class TrivialAttributeSet implements AttributeSet {

        private static final String NAME = "Trivial Attribute Set";     //NOI18N

        public int getAttributeCount() {
            return 1;
        }

        public boolean isDefined(Object attrName) {
            return attrName.equals(NameAttribute);
        }

        public boolean isEqual(AttributeSet attr) {
            return (attr.getAttributeCount() == 1)
                   && NAME.equals(attr.getAttribute(NameAttribute));
        }

        public AttributeSet copyAttributes() {
            return this;
        }

        public Object getAttribute(Object key) {
            return NameAttribute.equals(key) ? NAME : null;
        }

        public Enumeration<?> getAttributeNames() {
            return Collections.enumeration(Collections.singleton(NameAttribute));
        }

        public boolean containsAttribute(Object name, Object value) {
            if (name == null) {
                throw new IllegalArgumentException();
            }
            return NameAttribute.equals(name) && NAME.equals(value);
        }

        public boolean containsAttributes(AttributeSet attributes) {
            int attrCount = attributes.getAttributeCount();
            return (attrCount == 0)
                   || (attrCount == 1)
                      && (NAME.equals(attributes.getAttribute(NameAttribute)));
        }

        public AttributeSet getResolveParent() {
            return null;
        }

    }

}
