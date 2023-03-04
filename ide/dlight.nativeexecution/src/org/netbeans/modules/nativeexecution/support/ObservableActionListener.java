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
package org.netbeans.modules.nativeexecution.support;

import javax.swing.Action;

/**
 * The listener interface for receiving <tt>actionStarted</tt>/
 * <tt>actionCompleted</tt> events from an {@link ObservableAction}. The class
 * that is interested in processing an action event implements
 * this interface, and the object created with that class is registered with an
 * <tt>ObservableAction</tt>, using it's <tt>addObservableActionListener</tt>
 * method. When the action starts, that object's <tt>actionStarted</tt> method
 * is invoked. On action completion <tt>actionCompleted</tt> is invoked.
 * 
 * @param <T> type of action's result.
 */
public interface ObservableActionListener<T> {

    /**
     * Notifies listeners that action started.
     * @param source the Action that has been started.
     */
    public void actionStarted(Action source);

    /**
     * Notifies listeners that action completed.
     * @param source the Action that has been completed.
     * @param result the result of the action.
     */
    public void actionCompleted(Action source, T result);
}
