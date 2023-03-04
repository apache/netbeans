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

package org.netbeans.modules.editor.lib2;

import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.NbBundle;

/**
 * This class contains useful methods for working with documents.
 * 
 * @author Vita Stejskal
 */
public final class DocUtils {

    private static final Logger LOG = Logger.getLogger(DocUtils.class.getName());
    
    public static int getRowStart(Document doc, int offset, int lineShift)
    throws BadLocationException {
        
        checkOffsetValid(doc, offset);

        if (lineShift != 0) {
            Element lineRoot = doc.getDefaultRootElement();
            int line = lineRoot.getElementIndex(offset);
            line += lineShift;
            if (line < 0 || line >= lineRoot.getElementCount()) {
                return -1; // invalid line shift
            }
            return lineRoot.getElement(line).getStartOffset();

        } else { // no shift
            return doc.getDefaultRootElement().getElement(
                   doc.getDefaultRootElement().getElementIndex(offset)).getStartOffset();
        }
    }

    public static int getRowEnd(Document doc, int offset)
    throws BadLocationException {
        checkOffsetValid(doc, offset);

        return doc.getDefaultRootElement().getElement(
               doc.getDefaultRootElement().getElementIndex(offset)).getEndOffset() - 1;
    }
    
    /**
     * Return line index (line number - 1) for some offset in document.
     * @throws BadLocationException in case the offset is out of document.
     * @see {@link #getLineIndex(javax.swing.text.Document, int)} instead.
     */
    public static int getLineOffset(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength() + 1);
        return getLineIndex(doc, offset);
    }

    /** 
     * Return line index (line number - 1) for some offset in document.
     * 
     * @param doc document to operate on.
     * @param offset offset in document.
     * @return line index &gt;=0 since document always contains at least single '\n'.
     *  Returns 0 if offset &lt;=0. Returns last line index if offset is beyond document's end.
     */
    public static int getLineIndex(Document doc, int offset) {
        Element lineRoot = doc.getDefaultRootElement();
        return lineRoot.getElementIndex(offset);
    }

    public static String debugPosition(Document doc, int offset) {
        String ret;

        if (offset >= 0) {
            try {
                int line = getLineIndex(doc, offset) + 1;
                int col = getVisualColumn(doc, offset) + 1;
                ret = String.valueOf(line) + ":" + String.valueOf(col); // NOI18N
            } catch (BadLocationException e) {
                ret = NbBundle.getBundle(DocUtils.class).getString("wrong_position")
                      + ' ' + offset + " > " + doc.getLength(); // NOI18N
            }
        } else {
            ret = String.valueOf(offset);
        }

        return ret;
    }

    /** Return visual column (with expanded tabs) on the line.
    * @param doc document to operate on
    * @param offset position in document for which the visual column should be found
    * @return visual column on the line determined by position
    */
    public static int getVisualColumn(Document doc, int offset) throws BadLocationException {
        int docLen = doc.getLength();
        if (offset == docLen + 1) { // at ending extra '\n' => make docLen to proceed without BLE
            offset = docLen;
        }

        // TODO: fix this, do not use reflection
        try {
            Method m = findDeclaredMethod(doc.getClass(), "getVisColFromPos", Integer.TYPE); //NOI18N
            m.setAccessible(true);
            int col = (Integer) m.invoke(doc, offset);
            return col;
//            return doc.getVisColFromPos(offset);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Transpose letter at offset with the next one at offset+1.
     * @param doc non-null document that should be write-locked.
     * @param offset
     * @return true if succeeded or false when at end of doc.
     */
    public static boolean transposeLetters(@NonNull Document doc, int offset) {
        if (offset >= 0 && offset <= doc.getLength() - 2) {
            CharSequence text = DocumentUtilities.getText(doc);
            char ch = text.charAt(offset);
            try {
                doc.remove(offset, 1);
                doc.insertString(offset + 1, String.valueOf(ch), null);
            } catch (BadLocationException ex) {
                LOG.log(Level.FINE, null, ex);
                Toolkit.getDefaultToolkit().beep();
            }
            return true;
        }
        return false;
    }
    
    private static Method findDeclaredMethod(Class<?> clazz, String name, Class... parameters) throws NoSuchMethodException {
        while(clazz != null) {
            try {
                return clazz.getDeclaredMethod(name, parameters);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method: " + name); //NOI18N
    }
    
    public static boolean isIdentifierPart(Document doc, char ch) {
        // TODO: make this configurable
        return AcceptorFactory.UNICODE_IDENTIFIER.accept(ch);
    }
    
    public static boolean isWhitespace(char ch) {
        // TODO: make this configurable
        return AcceptorFactory.WHITESPACE.accept(ch);
    }
    
    public static void atomicLock(Document doc) {
        // TODO: fix this, do not use reflection
        try {
            Method lockMethod = doc.getClass().getMethod("atomicLock");
            lockMethod.invoke(doc);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    public static void atomicUnlock(Document doc) {
        // TODO: fix this, do not use reflection
        try {
            Method unlockMethod = doc.getClass().getMethod("atomicUnlock");
            unlockMethod.invoke(doc);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    public static void runAtomicAsUser(Document doc, Runnable r) {
        try {
            Method m = doc.getClass().getMethod("runAtomicAsUser", Runnable.class);
            m.invoke(doc, r);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    private static void checkOffsetValid(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength());
    }

    private static void checkOffsetValid(int offset, int limitOffset) throws BadLocationException {
        if (offset < 0 || offset > limitOffset) { 
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " not within <0, " + limitOffset + ">", // NOI18N
                offset);
        }
    }
    
    /** Creates a new instance of DocUtils */
    private DocUtils() {
    }
    
}
