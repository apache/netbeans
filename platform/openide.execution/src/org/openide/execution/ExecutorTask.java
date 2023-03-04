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

package org.openide.execution;

import org.openide.util.Task;
import org.openide.windows.InputOutput;

/** A task object that represents an asynchronously
* running execution task.
* Module authors do not typically need to subclass this.
* @author Jaroslav Tulach
*/
public abstract class ExecutorTask extends Task {
    /** Create the task.
    * @param run runnable to run that computes the task
    */
    protected ExecutorTask(Runnable run) {
        super (run);
    }

    /** Stop the computation.
    */
    public abstract void stop ();

    /** Check the result of execution. If the execution
    * is not finished, the calling thread is blocked until it is.
    *
    * @return the result of execution. Zero means successful execution; other numbers may indicate various error conditions.
    */
    public abstract int result ();

    /**
    * @return InputOutput assigned to this process
    */
    public abstract InputOutput getInputOutput();
}
