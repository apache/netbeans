/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.makeproject.api;

import java.util.Collection;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.openide.windows.InputOutput;

/**
 */
public interface ProjectActionHandler {

    /**
     * Will be called to initialize newly created handler instance with
     * project action event.
     *
     * @param pae  project action event to be handled
     * @param paes the full chain of project action events to be handled
     */
    public abstract void init(ProjectActionEvent pae, ProjectActionEvent[] paes, Collection<OutputStreamHandler> outputHandlers);

    /**
     * Will be called to execute current project action event that was passed
     * to {@link #init(ProjectActionEvent)}.
     *
     * @param io  where to read input and write output
     */
    public void execute(InputOutput io);

    /**
     * Checks if handler execution can be cancelled. Will be called only
     * after {@link #init(ProjectActionEvent)}, so implementation can
     * take into accout current action.
     *
     * @return <code>true</code> if handler execution can be cancelled,
     *          <code>false</code> otherwise
     */
    public boolean canCancel();

    /**
     * Will be called to cancel handler execution.
     */
    public void cancel();

    /**
     * Adds execution listener.
     *
     * @param l  listener to be added
     */
    public void addExecutionListener(ExecutionListener l);

    /**
     * Removes execution listener.
     *
     * @param l  listener to be removed
     */
    public void removeExecutionListener(ExecutionListener l);

}
