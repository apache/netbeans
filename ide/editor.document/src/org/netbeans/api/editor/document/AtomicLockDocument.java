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

import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
/**
 * Document that supports atomic locking allows
 * for transactional modifications.
 * The document is write-locked during the whole atomic
 * operation. All the operations since
 * the begining of the atomic operation
 * can be undone by using atomicUndo().
 * <p>Clients may watch for atomic operations
 * by registering an listener through
 * {@link addAtomicLockListener(AtomicLockListener)}
 * <p/>
 * The infrastructure registers a stub implementation for all documents;
 * the stub does not perform any locking, except that execution of {@link #runAtomic}
 * Runnable is synchronized on the document object.
 * 
 * Also see the predecessor, {@link org.netbeans.editor.AtomicLockDocument}.
 */
public interface AtomicLockDocument {

    /**
     * Provides access to the underlying Document
     * @return Document instance
     */
    public @NonNull Document getDocument();
    
    /**
     * Reverts modifications done during the atomic operation.
     */
    public void atomicUndo();
    
    /**
     * Runs the Runnable under atomic lock.
     * The runnable is executed while holding an atomic lock. If the Runnable
     * throws an Exception, the changes are undone as if {@link #atomicUndo} was
     * called.
     * @param r the executable to run.
     */
    public void runAtomic(@NonNull Runnable r);
    
    /**
     * Runs the Runnable under atomic lock, respecting document protection.
     * The runnable is executed while holding an atomic lock. If the Runnable
     * throws an Exception, the changes are undone as if {@link #atomicUndo} was
     * called. If an operation executed by the Runnable attempts to alter a protected 
     * document area, an exception will be thrown and all changes will be rolled back.
     * 
     * @param r the executable to run.
     */
    public void runAtomicAsUser(@NonNull Runnable r);
    
    /**
     * Attaches a Listener to receive start/end atomic lock events.
     * @param l the listener
     */
    public void addAtomicLockListener(@NonNull AtomicLockListener l);
    
    /**
     * Detaches a Listener for start/end atomic lock events.
     * @param l the listener
     */
    public void removeAtomicLockListener(@NonNull AtomicLockListener l);

}
