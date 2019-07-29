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

package org.netbeans.api.progress;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.progress.spi.UIInternalHandle;
import org.openide.util.Cancellable;

/**
 * Factory to create various ProgressHandle instances that allow long lasting
 * tasks to show their progress using various progress UIs. This class adds the 
 * ability to provide a custom Swing UI (JPanel) and manipulate with Swing components 
 * to the {@link org.netbeans.api.progress.ProgressHandle}.
 * @author Milos Kleint (mkleint@netbeans.org)
 * @author Svata Dedic
 */
public final class ProgressHandleFactory {

    /** Creates a new instance of ProgressIndicatorFactory */
    private ProgressHandleFactory() {
    }

    /**
     * Create a progress ui handle for a long lasting task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createHandle(java.lang.String)}
     */
    @Deprecated
    public static ProgressHandle createHandle(String displayName) {
        return createHandle(displayName, (Action)null);
    }
    
     /**
      * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createHandle(java.lang.String, org.openide.util.Cancellable)}.
     */
    @Deprecated
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel) {
        return createHandle(displayName, allowToCancel, null);
    }

    /**
     * Create a progress ui handle for a long lasting task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName, Action linkOutput) {
        return createHandle(displayName, null, linkOutput);
    }
    
    /**
     * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new UIInternalHandle(displayName, allowToCancel, true, linkOutput).createProgressHandle();
    }
    
    private static UIInternalHandle ih(ProgressHandle h) {
        return (UIInternalHandle)h.getInternalHandle();
    }

    /**
     * Get the progress bar component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.
     * @return the component to use in custom UI.
     */
    public static JComponent createProgressComponent(ProgressHandle handle) {
        return ih(handle).extractComponent();
    }

    /**
     * Get the task title component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to <code>ProgressHandle.setDisplayName()</code> method
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createMainLabelComponent(ProgressHandle handle) {
        return ih(handle).extractMainLabel();
    }
    
    /**
     * Get the detail messages component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to <code>ProgressHandle.progress(String)</code> method.
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createDetailLabelComponent(ProgressHandle handle) {
        return ih(handle).extractDetailLabel();
    }
    
    /**
     * Create a handle for a long lasting task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createSystemHandle(String displayName) {
        return createSystemHandle(displayName, null, null);
    }

    /**
     * Create a cancelable handle for a task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * @param displayName to be shown in the progress UI
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel) {
        return createSystemHandle(displayName, allowToCancel, null);
    }
    
    /**
     * Create a progress ui handle for a task that is not triggered by explicit user action.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     *
     */
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new UIInternalHandle(displayName, allowToCancel, false, linkOutput).createProgressHandle();
    }    
}
