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

package org.netbeans.test.web;

/**
 * A class taht may be used for waiting e.g. for a event.
 * <p>
 * For example:
 * <p><code><pre>
 * Observable obs;
 * final Waiter waiter = new Waiter();
 * final PropertyChangeListener pcl = new PropertyChangeListener() {
 *    public void propertyChange(PropertyChangeEvent evt) {
 *       if (evt.getPropertyName().equals("..")) {
 *          waiter.notifyFinished();
 *       }
 *    }
 * };
 * obs.addPropertyChangeListener(pcl);
 * // ...
 * waiter.waitFinished();
 * obs.removePropertyChangeListener(pcl);
 * </pre></code>
 * <p>
 *
 * @author ms113234
 */
public class Waiter {
    
    private boolean finished = false;
    
    /** Restarts Synchronizer.
     */
    public void init() {
        synchronized (this) {
            finished = false;
        }
    }
    
    /** Wait until the task is finished.
     */
    public void waitFinished() throws InterruptedException {
        synchronized (this) {
            while (!finished) {
                wait();
            }
        }
    }
    
    /** Wait until the task is finished, but only a given time.
     *  @param milliseconds time in milliseconds to wait
     *  @return true if the task is really finished, or false if the time out
     *     has been exceeded
     */
    public boolean waitFinished(long milliseconds) throws InterruptedException {
        synchronized (this) {
            if (finished) return true;
            long expectedEnd = System.currentTimeMillis() + milliseconds;
            for (;;) {
                wait(milliseconds);
                if (finished) return true;
                long now = System.currentTimeMillis();
                if (now >= expectedEnd) return false;
                milliseconds = expectedEnd - now;
            }
        }
    }
    
    /** Notify all waiters that this task has finished.
     */
    public void notifyFinished() {
        synchronized (this) {
            finished = true;
            notifyAll();
        }
    }
}
