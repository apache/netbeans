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
