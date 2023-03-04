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

package org.netbeans.core.execution;

import org.openide.execution.ExecutorTask;
import org.openide.windows.InputOutput;

/** Purpose ???
*
* @author Ales Novak
* @version 1.0, November 18, 1998
*/
final class ExecutorTaskImpl extends ExecutorTask {
    /** result */
    int result = -1;
    /** SysProcess ref */
    DefaultSysProcess proc;
    /** lock */
    Object lock = this;

    /** constructor */
    ExecutorTaskImpl() {
        super(new Runnable() {
                  public void run() {}
              }
             );
    }

    /** Stops the task. */
    public void stop() {
        try {
            synchronized (lock) {
                while (proc == null) lock.wait();
                proc.stop();
            }
        } catch (InterruptedException e) {
        }
    }
    /** @return result 0 means success. Blocking operation. */
    public int result() {
        waitFinished();
        proc.stop();
        return result;
    }
    // hack off
    final void finished() {
        notifyFinished();
    }
    public void run() {
        waitFinished();
    }

    public InputOutput getInputOutput() {
        return proc.getInputOutput();
    }
}
