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

package org.netbeans.api.progress.aggregate;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;

/**
 * Factory for creation of aggregate progress indication handles and individual contributor instances.
 * For a more simple version of progress indication, see {@link org.netbeans.api.progress.ProgressHandleFactory}
 *
 * @author mkleint (mkleint@netbeans.org)
 */
public final class AggregateProgressFactory extends BasicAggregateProgressFactory {

    /** Creates a new instance of AggregateProgressFactory */
    private AggregateProgressFactory() {
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
     * @deprecated use {@link BasicAggregateProgressFactory#createHandle(java.lang.String, org.netbeans.api.progress.aggregate.ProgressContributor[], org.openide.util.Cancellable, javax.swing.Action)}.
     */
    @Deprecated
    public static AggregateProgressHandle createHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel, Action linkOutput) {
        return BasicAggregateProgressFactory.createHandle(displayName, contributors, allowToCancel, linkOutput);
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
     * @deprecated use {@link BasicAggregateProgressFactory#createSystemHandle(java.lang.String, org.netbeans.api.progress.aggregate.ProgressContributor[], org.openide.util.Cancellable, javax.swing.Action)}.
     */
    @Deprecated
    public static AggregateProgressHandle createSystemHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel, Action linkOutput) {
        return BasicAggregateProgressFactory.createSystemHandle(displayName, contributors, allowToCancel, linkOutput);
    }  
    
    /**
     * Get the progress bar component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.
     * @since org.netbeans.api.progress 1.3
     * @return the component to use in custom UI.
     */
    public static JComponent createProgressComponent(AggregateProgressHandle handle) {
        return ProgressHandleFactory.createProgressComponent(getProgressHandle(handle));
    }    
    
    /**
     * Get the task title component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to handle's <code>setDisplayName()</code> method.
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createMainLabelComponent(AggregateProgressHandle handle) {
        return ProgressHandleFactory.createMainLabelComponent(getProgressHandle(handle));
    }
    
    /**
     * Get the detail messages component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.The text of the label is changed by calls to contributors' <code>progress(String)</code> methods.
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createDetailLabelComponent(AggregateProgressHandle handle) {
        return ProgressHandleFactory.createDetailLabelComponent(getProgressHandle(handle));
    }
}
