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

package org.netbeans.api.progress.aggregate;

import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;

/**
 * Factory for creation of aggregate progress indication handles and individual contributor instances.
 * For a more simple version of progress indication, see {@link org.netbeans.api.progress.ProgressHandle}
 *
 * @author mkleint (mkleint@netbeans.org)
 */
public class BasicAggregateProgressFactory {
    /** Creates a new instance of AggregateProgressFactory */
    
    /**
     * Create an aggregating progress ui handle for a long lasting task.
     * @param contributors the initial set of progress indication contributors that are aggregated in the UI.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param displayName to be shown in the progress UI
     * @return an instance of <code>ProgressHandle</code>, initialized but not started.
     *
     */
    public static AggregateProgressHandle createHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel) {
        return doCreateHandle(displayName, contributors, allowToCancel, false, 
                ProgressHandle.createHandle(displayName, allowToCancel));
    }
    
    public static ProgressContributor createProgressContributor(String trackingId) {
        return new ProgressContributor(trackingId);
    }
    
    /**
     * Create an aggregating progress ui handle for a long lasting task.
     * @param contributors the initial set of progress indication contributors that are aggregated in the UI.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of <code>ProgressHandle</code>, initialized but not started.
     * @since 1.59
     */
    public static AggregateProgressHandle createSystemHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel, Action linkOutput) {
        return doCreateHandle(displayName, contributors, allowToCancel, true,
                ProgressHandle.createSystemHandle(displayName, allowToCancel, linkOutput));
    }  
    
    /**
     * Create an aggregating progress ui handle for a long lasting task.
     * @param contributors the initial set of progress indication contributors that are aggregated in the UI.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of <code>ProgressHandle</code>, initialized but not started.
     * @since 1.59
     */
    public static AggregateProgressHandle createHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel, Action linkOutput) {
        return doCreateHandle(displayName, contributors, allowToCancel, false,
                ProgressHandle.createHandle(displayName, allowToCancel, linkOutput));
    }
    
    protected static AggregateProgressHandle doCreateHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel, boolean systemHandle, ProgressHandle h) {
        return new AggregateProgressHandle(displayName, contributors, allowToCancel, systemHandle,
                h);
    }
    
    protected static ProgressHandle getProgressHandle(AggregateProgressHandle ah) {
        return ah.handle;
    }
}
