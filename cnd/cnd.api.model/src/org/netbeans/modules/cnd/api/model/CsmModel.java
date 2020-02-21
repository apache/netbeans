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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.util.Cancellable;

/**
 * Source model
 *
 */
public interface CsmModel {

    // TODO: write full description
    /** @param id Netbeans project */
    CsmProject getProject(Object id);

    Collection<CsmProject> projects();

    /**
     * Code model calls can be very expensive. 
     * Therefore one can never call code model from event dispatching thread.
     * Moreover, to make code model able to effectively solve synchronization issues,
     * all callers shall use not their own threads but call enqueue method instead.
     *
     * The method creates a thread and runs the given task in this thread.
     *
     * Whether or not the thread be created immediately or the task
     * will be just enqueued and runned later on, depends on implementation.
     *
     * We recommend using this method rather than one without <code>name</code> parameter.
     *
     * @param task task to run
     * @param name name that would be added to the thread name
     */
    Cancellable enqueue(Runnable task, CharSequence name);

    /**
     * Schedules complete projects reparse.
     * Does not wait until it is completed.
     */
    void scheduleReparse(Collection<CsmProject> projects);

    /**
     * Find project that contains file.
     * Returns CsmFile if project is found.
     *
     * This function might be costly (this depends on the model state).
     *
     * CAUTION: this method should never be called directly from the thread, 
     * in which model notificatios (either CsmModelListener or CsmProgressListener) come.
     * These notifications come directly in parser thread or project initialization thread.
     * Calling findFile from these threads may cause deadlock.
     *
     * @param absPath absolute file path
     * @since 1.13.2
     */
    CsmFile findFile(FSPath absPath, boolean createIfPossible, boolean snapShot);
    CsmFile[] findFiles(FSPath absPath, boolean createIfPossible, boolean snapShot);
    
    /**
     * Returns the state of the model
     */
    CsmModelState getState();

    /**
     * @param id NativeProject instance
     * @return Boolean.TRUE if the project is enabled Boolean.FALSE if the
     * project is disabled null if the project is being created
     */    
    Boolean isProjectEnabled(Object id);

    /**
     * 
     * @param p NativeProject instance
     */
    public void disableProject(Object p);

    /**
     * 
     * @param p NativeProject instance
     */
    public void enableProject(Object p);
}
