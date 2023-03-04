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

package org.netbeans.editor;

import java.util.EventListener;

/**
 * Listener for begining and end of the atomic
 * locking. It can be used to optimize the document
 * listeners if a large amounts of edits are performed
 * in an atomic change. For example if there's
 * a timer restarted after each document modification
 * to update an external pane showing the document structure
 * after 2000ms past the last modification occurred
 * then there could be a following listener used:<PRE>
 *  class MultiListener implements DocumentListener, AtomicLockListener {
 *
 *    private boolean atomic; // whether in atomic change
 *
 *    public void insertUpdate(DocumentEvent evt) {
 *      modified(evt);
 *    }
 *
 *    public void removeUpdate(DocumentEvent evt) {
 *      modified(evt);
 *    }
 *
 *    public void changedUpdate(DocumentEvent evt) {
 *    }
 *
 *    private void modified(DocumentEvent evt) {
 *      if (!atomic) {
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
 *    }
 *
 *  }
 *  <PRE>
 * @deprecated use {@link org.netbeans.api.editor.document.AtomicLockListener}
 */
@Deprecated
public interface AtomicLockListener extends EventListener {

    public void atomicLock(AtomicLockEvent evt);
    
    public void atomicUnlock(AtomicLockEvent evt);
    
}
