/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
