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

package org.netbeans.modules.editor.indent.api;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.editor.indent.IndentImpl;

/**
 * Reindentation of a single or multiple lines in the document
 * means fixing of the line's indent only but does not do any other
 * code beautification.
 * <br>
 * The following pattern should be used:
 * <pre>
 * indent.lock();
 * try {
 *     doc.atomicLock();
 *     try {
 *         indent.reindent(...);
 *     } finally {
 *         doc.atomicUnlock();
 *     }
 * } finally {
 *     indent.unlock();
 * }
 * </pre>
 *
 * @author Miloslav Metelka
 */
public final class Indent {
    
    /**
     *  Get the indentation for the given document.
     * 
     * @param doc non-null document.
     * @return non-null indentation.
     */
    public static Indent get(Document doc) {
        IndentImpl indentImpl = IndentImpl.get(doc);
        Indent indent = indentImpl.getIndent();
        if (indent == null) {
            indent = new Indent(indentImpl);
            indentImpl.setIndent(indent);
        }
        return indent;
    }
    
    private final IndentImpl impl;
    
    private Indent(IndentImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Clients should call this method before acquiring of document's write lock.
     * <br>
     * The following pattern should be used:
     * <pre>
     * indent.lock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         indent.reindent(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     indent.unlock();
     * }
     * </pre>
     */
    public void lock() {
        impl.indentLock();
    }
    
    /**
     * Clients should call this method after releasing of document's write lock.
     * <br>
     * The following pattern should be used:
     * <pre>
     * indent.lock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         indent.reindent(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     indent.unlock();
     * }
     * </pre>
     */
    public void unlock() {
        impl.indentUnlock();
    }
    
    /**
     * Correct indentation on a single line determined by the given offset.
     * <br>
     * Typically it is called after newline gets inserted
     * or when a line is reindented explicitly (e.g. by pressing TAB key in emacs mode).
     * <br>
     * This method will fallback to the editor formatting infrastructure
     * in case there are no registered indent or reformat factories.
     * 
     * @param offset &gt;=0 any offset on the line to be reformatted.
     * @throws BadLocationException in case the indenter attempted to insert/remove
     *  at an invalid offset or e.g. into a guarded section.
     */
    public void reindent(int offset) throws BadLocationException {
        reindent(offset, offset);
    }

    /**
     * Correct indentation of all lines in the given offset range.
     * <br>
     * This method will fallback to the editor formatting infrastructure
     * in case there are no registered indent or reformat factories.
     * 
     * @param startOffset &gt;=0 any offset on a first line to be reformatted.
     * @param endOffset &gt;=startOffset any offset (including end offset) 
     *   on a last line to be reformatted.
     * @throws BadLocationException in case the indenter attempted to insert/remove
     *  at an invalid offset or e.g. into a guarded section.
     */
    public void reindent(int startOffset, int endOffset) throws BadLocationException {
        impl.reindent(startOffset, endOffset, startOffset, false);
    }

    /**
     * Creates new line at <code>offset</code> and reindents it.
     *
     * <p>This method will insert a line break (ie EOL character) at the specified
     * offset and then reindent the newly created line. The method will return the
     * offset of the indented beginning of the new line. That is the offset where
     * the new text should appear when typing in the document.
     *
     * @param offset The document offset where the new line will be created.
     *
     * @return The offset of the first non-white character (or the EOL character)
     *   on the new line. This is basically where the caret should be moved to.
     * @throws javax.swing.text.BadLocationException
     * @since 1.10
     */
    public int indentNewLine(int offset) throws BadLocationException {
        return impl.reindent(offset, offset, offset, true);
    }

}
