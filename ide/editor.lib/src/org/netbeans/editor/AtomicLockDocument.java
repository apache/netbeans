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

package org.netbeans.editor;

import javax.swing.text.Document;

/**
 * Document that supports atomic locking allows
 * for transactional modifications.
 * The document is write-locked during the whole atomic
 * operation. All the operations since
 * the begining of the atomic operation
 * can be undone by using atomicUndo().
 * Typical scenario of the operation
 * is the following: <PRE>
 *   doc.atomicLock();
 *   try {
 *     ...
 *     modification1
 *     modification2
 *     ...
 *   } catch (BadLocationException e) {
 *     // something went wrong - undo till begining
 *     doc.atomicUndo();
 *   } finally {
 *     doc.atomicUnlock();
 *   }
 *   </PRE>
 *   <P>The external clients can watch for atomic operations
 *   by registering an listener through
 *   {@link #addAtomicLockListener(AtomicLockListener)}
 * @deprecated Use {@link org.netbeans.api.editor.document.AtomicLockDocument}
 */
@Deprecated
public interface AtomicLockDocument extends Document {

    public void atomicLock();
    
    public void atomicUnlock();
    
    public void atomicUndo();
    
    public void addAtomicLockListener(AtomicLockListener l);
    
    public void removeAtomicLockListener(AtomicLockListener l);

}
