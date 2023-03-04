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

import java.util.EventListener;

/**
 * Listener for begining and end of the atomic locking.
 * <br/>
 * Only outer atomic lock/unlock is being notified (nested locking is not notified).
 * <br/>
 * There may be an empty atomic section when the lock is acquired and released
 * but no modification is done inside it.
 * <br/>
 * Listener may be used to optimize regular document
 * listeners if a large amounts of edits are performed
 * in an atomic change. For example instead of restarting
 * a reparse timer after each document modification
 * inside a document reformatting section the timer could
 * only be restarted once when an atomic lock is being released:
 * <pre>
 *  class DocListener implements DocumentListener, AtomicLockListener {
 *
 *    private boolean atomicChange; // whether in atomic change
 * 
 *    private boolean modified; // any modification performed
 *
 *    public void insertUpdate(DocumentEvent evt) {
 *      modified = true;
 *      possiblyRestartTimer();
 *    }
 *
 *    public void removeUpdate(DocumentEvent evt) {
 *      modified = true;
 *      possiblyRestartTimer();
 *    }
 *
 *    public void changedUpdate(DocumentEvent evt) {
 *    }
 *
 *    private void modified() {
 *      if (modified && !atomic) {
 *        restartTimer(); // restart the timer
 *      }
 *    }
 *
 *    public void atomicLock(AtomicLockEvent evt) {
 *      atomic = true;
 *    }
 *
 *    public void atomicUnlock(AtomicLockEvent evt) {
 *      atomic = false;
 *      possiblyRestartTimer();
 *    }
 *
 *  }
 *  </pre>
 */
public interface AtomicLockListener extends EventListener {

    /**
     * Called once the outer atomic lock was acquired.
     *
     * @param evt non-null event
     */
    public void atomicLock(AtomicLockEvent evt);
    
    /**
     * Called right before the outer atomic lock will be released.
     * @param evt 
     */
    public void atomicUnlock(AtomicLockEvent evt);
    
}
