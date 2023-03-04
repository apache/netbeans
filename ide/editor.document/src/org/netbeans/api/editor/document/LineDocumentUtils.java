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

package org.netbeans.api.editor.document;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.document.DocumentServices;
import org.netbeans.modules.editor.document.TextSearchUtils;
import org.netbeans.modules.editor.document.implspi.CharClassifier;
import org.netbeans.modules.editor.lib2.AcceptorFactory;
import org.netbeans.spi.editor.document.DocumentFactory;
import org.openide.util.Lookup;

/**
 * Line and word related utility methods.
 * <br/>
 * All the methods working with the document assume that the document is locked against
 * a parallel modification e.g. by including the call within
 * {@link Document#render(java.lang.Runnable)}.
 * <br/>
 * The utilities were moved from the former Editor Library 2's Utilities
 * and work with {@link LineDocument} only. The methods work only with document
 * data. Utilities that connect document with the UI elements (Swing) can be
 * still found in Editor Library (2).
 *
 * @author Miloslav Metelka
 */

public final class LineDocumentUtils {

    private static final String WRONG_POSITION_LOCALE = "wrong_position"; // NOI18N

    private LineDocumentUtils() {
        // instantiation has no sense
    }

    /**
     * Get start offset of a (newline character separated) line.
     * @param doc non-null document to operate on
     * @param offset position in document where to start searching
     * @return offset of character right above newline prior the given offset or zero.
     */
    public static int getLineStart(@NonNull LineDocument doc, int offset) {
        return doc.getParagraphElement(offset).getStartOffset();
    }

    public static int getLineEnd(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        return doc.getParagraphElement(offset).getEndOffset() - 1;
    }
    
    /**
     * Get the first non-whitespace character on a line represented by the given
     * offset.
     *
     * @param doc document to operate on
     * @param offset position in document anywhere on the line
     * @return position of the first non-white char on the line or -1 if there's
     * no non-white character on that line.
     */
    public static int getLineFirstNonWhitespace(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        Element lineElement = doc.getParagraphElement(offset);
        return TextSearchUtils.getNextNonWhitespace(docText, classifier,
            lineElement.getStartOffset(),
            lineElement.getEndOffset() - 1
        );
    }

    /**
     * Get the last non-white character on the line. The document.isWhitespace()
     * is used to test whether the particular character is white space or not.
     *
     * @param doc document to operate on
     * @param offset position in document anywhere on the line
     * @return position of the last non-white char on the line or -1 if there's
     * no non-white character on that line.
     */
    public static int getLineLastNonWhitespace(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        Element lineElement = doc.getParagraphElement(offset);
        return TextSearchUtils.getPreviousNonWhitespace(docText, classifier,
            lineElement.getEndOffset() - 1,
            lineElement.getStartOffset()
        );
    }

    public static int getWordStart(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getWordStart(docText, classifier, offset);
    }

    public static int getWordEnd(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getWordEnd(docText, classifier, offset);
    }

    /**
     * Get the word at given offset.
     * @param doc document to operate on
     * @param wordStartOffset offset of word start.
     * @return word starting at offset.
     */
    public static String getWord(@NonNull LineDocument doc, int wordStartOffset)
    throws BadLocationException
    {
        checkOffsetValid(doc, wordStartOffset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getWord(docText, classifier, wordStartOffset).toString();
    }

    public static int getNextWordStart(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getNextWordStart(docText, classifier, offset);
    }

    public static int getPreviousWordEnd(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getPreviousWordEnd(docText, classifier, offset);
    }

    /**
     * Get start of a previous word in backward direction.
     * 
     * @param doc non-null document.
     * @param offset >= 0 offset in document.
     * @return previous word boundary offset.
     * @since 1.4
     */
    public static int getPreviousWordStart(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getPreviousWordStart(docText, classifier, offset);
    }

    /**
     * Get first whitespace character in document in forward direction.
     *
     * @param doc document to operate on
     * @param offset position in document where to start searching
     * @return position of the next WS character or -1 if not found.
     */
    public static int getNextWhitespace(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        return getNextWhitespace(doc, offset, doc.getLength() + 1);
    }

    /**
     * Get first whitespace character in document in forward direction.
     *
     * @param doc document to operate on
     * @param offset position in document where to start searching
     * @param limitOffset offset above the last character to examine for WS.
     * @return position of the next WS character or -1 if not found.
     */
    public static int getNextWhitespace(@NonNull LineDocument doc, int offset, int limitOffset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getNextWhitespace(docText, classifier, offset, limitOffset);
    }

    /**
     * Get first whitespace character in document in backward direction.
     *
     * @param doc document to operate on
     * @param offset offset above first character to examine for WS.
     * @return position of the previous WS character or -1 if not found.
     */
    public static int getPreviousWhitespace(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        return getPreviousWhitespace(doc, offset, 0);
    }

    /**
     * Get first whitespace character in document in backward direction.
     *
     * @param doc document to operate on
     * @param offset offset above first character to examine for WS.
     * @param limitOffset offset of the last character (in backward direction) to examine for WS.
     * @return position of the previous WS character or -1 if not found.
     */
    public static int getPreviousWhitespace(@NonNull LineDocument doc, int offset, int limitOffset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getPreviousWhitespace(docText, classifier, offset, limitOffset);
    }

    /**
     * Get first non-whitespace character in document in forward direction.
     *
     * @param doc document to operate on
     * @param offset offset of first character to examine for WS.
     * @return position of the next non-WS character or -1 if not found.
     */
    public static int getNextNonWhitespace(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        return getNextNonWhitespace(doc, offset, doc.getLength() + 1);
    }

    /**
     * Get first non-whitespace character in document in forward direction.
     *
     * @param doc document to operate on
     * @param offset offset of first character to examine for WS.
     * @param limitOffset offset above the last character to examine for WS.
     * @return position of the next non-WS character or -1 if not found.
     */
    public static int getNextNonWhitespace(@NonNull LineDocument doc, int offset, int limitOffset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getNextNonWhitespace(docText, classifier, offset, limitOffset);
    }
    
    /**
     * Get first non-whitespace character in document in forward direction.
     *
     * @param doc document to operate on
     * @param offset offset of first character to examine for WS.
     * @return position of the next non-WS character or -1 if not found.
     */
    public static int getPreviousNonWhitespace(@NonNull LineDocument doc, int offset)
    throws BadLocationException {
        return getPreviousNonWhitespace(doc, offset, 0);
    }

    /**
     * Get first non-whitespace character in document in forward direction.
     *
     * @param doc document to operate on
     * @param offset offset of first character to examine for WS.
     * @param limitOffset the offset of the last character to examine for WS.
     * @return position of the next non-WS character or -1 if not found.
     */
    public static int getPreviousNonWhitespace(@NonNull LineDocument doc, int offset, int limitOffset)
    throws BadLocationException {
        checkOffsetValid(doc, offset);
        CharClassifier classifier = getValidClassifier(doc);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getPreviousNonWhitespace(docText, classifier, offset, limitOffset);
    }

    /**
     * Return line index (line number - 1) for the given offset in the document.
     *
     * @param doc document to operate on
     * @param offset position in document where to start searching
     */
    public static int getLineIndex(@NonNull LineDocument doc, int offset) throws BadLocationException {
        checkOffsetValid(doc, offset);
        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        return lineRoot.getElementIndex(offset);
    }

    /**
     * Return start offset of the line with the given index.
     *
     * @param lineIndex line index starting from 0
     * @return start offset of the line or -1 if lineIndex was invalid
     */
    public static int getLineStartFromIndex(@NonNull LineDocument doc, int lineIndex) {
        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        if (lineIndex < 0 || lineIndex >= lineRoot.getElementCount()) {
            return -1; // invalid line number
        } else {
            return lineRoot.getElement(lineIndex).getStartOffset();
        }
    }

    /**
     * Tests whether the line at the given offset contains no characters except the ending new-line.
     * @param doc document to operate on
     * @param offset position anywhere on the tested line
     * @return whether the line is empty or not
     */
    public static boolean isLineEmpty(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.isLineEmpty(docText, offset);
    }

    /** Tests whether the line contains only whitespace characters.
     * @param doc document to operate on
     * @param offset position anywhere on the tested line
     * @return whether the line is empty or not
     */
    public static boolean isLineWhitespace(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        return getLineFirstNonWhitespace(doc, offset) == -1;
    }

    public static int getNextNonNewline(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getNextNonNewline(docText, offset, doc.getLength() + 1);
    }

    public static int getPreviousNonNewline(@NonNull LineDocument doc, int offset)
    throws BadLocationException
    {
        checkOffsetValid(doc, offset);
        CharSequence docText = DocumentUtilities.getText(doc);
        return TextSearchUtils.getPreviousNonNewline(docText, offset, 0);
    }

    public static int getLineCount(@NonNull LineDocument doc) {
        return doc.getParagraphElement(0).getParentElement().getElementCount();
    }

    /** Count of rows between these two positions */
    public static int getLineCount(@NonNull LineDocument doc, int startOffset, int endOffset) {
        if (startOffset > endOffset) {
            return 0;
        }
        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        return lineRoot.getElementIndex(endOffset) - lineRoot.getElementIndex(startOffset) + 1;
    }

    private static void checkOffsetValid(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength() + 1);
    }

    private static void checkOffsetValid(int offset, int limitOffset) throws BadLocationException {
        if (offset < 0 || offset > limitOffset) { 
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " not within <0, " + limitOffset + ">", // NOI18N
                offset);
        }
    }
    
    private static CharClassifier getValidClassifier(Document doc) {
        CharClassifier cc = as(doc, CharClassifier.class);
        return (cc != null) ? cc : TextSearchUtils.DEFAULT_CLASSIFIER;
    }
    
    private static final Object NOT_FOUND = new Object();
    
    /**
     * Locates the appropriate service for the document. May return {@code null}
     * if the interface/service is not supported by the document.
     * <p/>
     * For example, if a code needs to perform an atomic action on the document,
     * it can do so as follows:
     * <code><pre>
     * Document d = ... ; // some parameter ?
     * AtomicLockDocument ald = LineDocumentUtils.as(d, AtomicLockDocument.class); // obtain the optional interface
     * Runnable r = new Runnable() {
     *      public void run() { /* ... the code to execute ... * / }
     * };
     * 
     * if (ald != null) {
     *    ald.runAtomic(r);
     * } else {
     *    r.run();
     * }
     * 
     * </pre></code>
     * @param <T> service type
     * @param d the document
     * @param documentService the service interface
     * @return the service implementation or {@code null} if the service is not available
     */
    public static @CheckForNull <T> T as(@NullAllowed Document d, Class<T> documentService) {
        if (d == null) {
            return null;
        }
        return as(d, documentService, false);
    }
    
    private static class V<T> {
        final T delegate;

        public V(T delegate) {
            this.delegate = delegate;
        }
        
    }
    
    /**
     * Locates the appropriate service for the document. 
     * A fallback (dummy) implementation may be returned if the document does not
     * support the service natively. An exception will be thrown
     * if the stub is not available.
     * 
     * @param d the document instance
     * @param documentService the requested service/interface
     */
    public static @NonNull <T> T asRequired(@NonNull Document d, Class<T> documentService) {
        return as(d, documentService, true);
    }
    
    private static @CheckForNull <T> T as(@NonNull Document d, Class<T> documentService, boolean useStub) {
        if (d == null) {
            throw new NullPointerException("null document");
        }
        
        if (documentService.isInstance(d)) {
            @SuppressWarnings("unchecked")
            T res = (T) d;
            return res;
        }
        Object serv = d.getProperty(documentService);
        if (serv != null) {
            if (serv instanceof V) {
                if (useStub) {
                    @SuppressWarnings("unchecked")
                    T res = ((V<T>)serv).delegate;
                    if (res == null) {
                        throw new IllegalArgumentException();
                    }
                    return res;
                } else {
                    return null;
                }
            }
            if (serv == NOT_FOUND) {
                if (!useStub) {
                    return null;
                }
                // fall through, make a wrapper
            } else {
                @SuppressWarnings("unchecked")
                T res = (T) serv;
                return res;
            }
        }
        
        Lookup lkp = DocumentServices.getInstance().getLookup(d);
        serv = lkp.lookup(documentService);
        if (serv == null) {
            if (useStub) {
                lkp = DocumentServices.getInstance().getStubLookup(d);
                serv = lkp.lookup(documentService);
                d.putProperty(documentService, new V<Object>(serv));
                if (serv == null) {
                    throw new IllegalArgumentException();
                }
            }
        } else {
            d.putProperty(documentService, serv == null ? NOT_FOUND : serv);
        }
        @SuppressWarnings("unchecked")
        T res = (T) serv;
        return res;
    }

    /**
     * Creates an empty document not attached to any environment, of the given
     * MIME type. The created document's type may differ for individual MIME types.
     * If the document cannot be created for the MIME type, an {@link IllegalArgumentException}
     * is thrown.
     * 
     * @param mimeType
     * @return LineDocument instance
     */
    public static @NonNull LineDocument  createDocument(String mimeType) {
        DocumentFactory f = MimeLookup.getLookup(mimeType).lookup(DocumentFactory.class);
        if (f == null) {
            throw new IllegalArgumentException("No document available for MIME type: " + mimeType);
        }
        Document doc = f.createDocument(mimeType);
        if (doc == null) {
            throw new IllegalArgumentException("Could not create document for MIME type: " + mimeType);
        }
        LineDocument ldoc = as(doc, LineDocument.class);
        if (ldoc == null) {
            throw new IllegalArgumentException("Could not create document for MIME type: " + mimeType);
        }
        return ldoc;
    }

}
