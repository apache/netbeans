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
 * Reformatting of a block of code in a document.
 * <br>
 * The following pattern should be used:
 * <pre>
 * reformat.lock();
 * try {
 *     doc.atomicLock();
 *     try {
 *         reformat.reformat(...);
 *     } finally {
 *         doc.atomicUnlock();
 *     }
 * } finally {
 *     reformat.unlock();
 * }
 * </pre>
 *
 * @author Miloslav Metelka
 */
public final class Reformat {
    
    /**
     *  Get the reformatting for the given document.
     * 
     * @param doc non-null document.
     * @return non-null reformat object.
     */
    public static Reformat get(Document doc) {
        IndentImpl indentImpl = IndentImpl.get(doc);
        Reformat reformat = indentImpl.getReformat();
        if (reformat == null) {
            reformat = new Reformat(indentImpl);
            indentImpl.setReformat(reformat);
        }
        return reformat;
    }
    
    private final IndentImpl impl;
    
    private Reformat(IndentImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Clients should call this method before acquiring of document's write lock.
     * <br>
     * The following pattern should be used:
     * <pre>
     * reformat.lock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         reformat.reformat(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     reformat.unlock();
     * }
     * </pre>
     */
    public void lock() {
        impl.reformatLock();
    }
    
    /**
     * Clients should call this method after releasing of document's write lock.
     * <br>
     * The following pattern should be used:
     * <pre>
     * reformat.lock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         reformat.reformat(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     reformat.unlock();
     * }
     * </pre>
     */
    public void unlock() {
        impl.reformatUnlock();
    }
    
    /**
     * Reformat a given range of code in the given document.
     * <br>
     * It includes possible fixing of indentation and possible code beautification
     * dependent on the implementation of the reformatter.
     * 
     * <p>
     * If the reformatter implementation is not available the reindentation
     * will be performed (i.e. just the line indentation will be fixed)
     * as a closest match.
     * 
     * @param startOffset start offset of the area to be reformatted.
     * @param endOffset end offset of the area to be reformatted.
     */
    public void reformat(int startOffset, int endOffset) throws BadLocationException {
        impl.reformat(startOffset, endOffset, startOffset);
    }

}
