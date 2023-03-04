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

package org.netbeans.spi.editor.completion;

/**
 * The inteface of a task performing a code completion query.
 * <br>
 * The support class
 * {@link org.netbeans.spi.editor.completion.support.AsyncCompletionTask}
 * can be used for convenience when the task requires an asynchronous evaluation.
 *
 * @see CompletionProvider
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public interface CompletionTask {

    /**
     * Called by the code completion infrastructure to ask the task
     * to do a query and return the results through the given completion listener.
     * <br>
     * This method is called only once during the lifetime of the completion task
     * object.
     *
     * <p>
     * This method is always called in AWT thread but it may reschedule
     * its processing into another thread and fire the given listener
     * once the computing is finished.
     * 
     * @param resultSet non-null result set to which the results
     *  of the query must be added.
     */
    public void query(CompletionResultSet resultSet);

    /**
     * Called by the code completion infrastructure to inform the task about
     * changes in the corresponding document. The task should reflect these
     * changes while creating the query result.
     * <br>
     * This method can be called multiple times on a single task instance.
     * <br>
     * Typically it is called AFTER the <code>query()</code> was invoked
     * but it may also be invoked BEFORE the <code>query()</code> in case
     * the user types even before the <code>query()</code>
     * was called by the infrastructure. In such
     * case the <code>resultSet</code> parameter will be <code>null</code>.
     * <br>
     * It is guaranteed that this method will not be invoked in case
     * the document instance set in the component would change since the last invocation
     * of either the <code>query()</code> or <code>refresh()</code>.
     *
     * <p>
     * This method is always called in AWT thread but it may reschedule
     * its processing into another thread and fire the given listener
     * once the computing is finished.
     * 
     * @param resultSet non-null result set to which the results
     *  of the refreshing must be added.
     *  <br/>
     *  Null result set may be passed in case the <code>query()</code>
     *  was not invoked yet and user has typed a character. In this case
     *  the provider may hide the completion
     *  by using <code>Completion.get().hideAll()</code>
     *  if the typed character is inappropriate e.g. ";" for java completion.
     */
    public void refresh(CompletionResultSet resultSet);
    
    /**
     * Called by the code completion infrastructure to cancel the task.
     * <br>
     * Once the cancel is done on the task no more querying or refreshing
     * is done on it.
     *
     * <p>
     * This method may potentially be called from any thread.
     */
    public void cancel();

}
