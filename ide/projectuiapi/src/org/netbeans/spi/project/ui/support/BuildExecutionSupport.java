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

package org.netbeans.spi.project.ui.support;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.project.uiapi.BuildExecutionSupportImplementation;
import org.netbeans.modules.project.uiapi.Utilities;
import org.openide.filesystems.FileObject;

/**
 * Register running and finished build/run/debug tasks for use
 * by generic project UI, like "Repeat Build" action or "Stop Build" action
 * in main menu.
 *
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.32
 */
public final class BuildExecutionSupport {

    private BuildExecutionSupport() {
        
    }
    /**
     * Notify that the build job was started and pass a reference to it.
     * It is assumed that the given job instance is registered only once.
     * @param item
     */
    public static void registerRunningItem(BuildExecutionSupport.Item item) {
        Utilities.getBuildExecutionSupportImplementation().registerRunningItem(item);
    }

    /**
     * Notify that the build job finished. The instance passed shall be the same
     * to the one passed to registerRunningItem().
     * It is assumed that the given job instance is registered only once and only after the
     * registerRunningItem() method was called.
     * @param item
     */
    public static void registerFinishedItem(BuildExecutionSupport.Item item) {
        Utilities.getBuildExecutionSupportImplementation().registerFinishedItem(item);
    }

    /**
     * Returns the last registered finished item. 
     * 
     * @return item
     * @since 1.78
     */
    public static BuildExecutionSupport.Item getLastFinishedItem() {
        return Utilities.getBuildExecutionSupportImplementation().getLastItem();
    }
  
    /**
     * Registers a change listener on BuildExecutionSupport
     * 
     * @param listener 
     * @since 1.78
     */
    public static void addChangeListener(ChangeListener listener) {
        BuildExecutionSupportImplementation besi = Utilities.getBuildExecutionSupportImplementation();
        besi.addChangeListener(listener);
    }

    /**
     * Unregisters a change listener on BuildExecutionSupport
     * 
     * @param listener 
     * @since 1.78
     */
    public static void removeChangeListener(ChangeListener listener) {
        BuildExecutionSupportImplementation besi = Utilities.getBuildExecutionSupportImplementation();
        besi.removeChangeListener(listener);
    }
        
    /**
     * Wrapper for the build job used by <code>BuildExecutionSupport</code>
     */
    public static interface Item {
        /**
         * Display name of the build job
         * @return
         */
        String getDisplayName();
        /**
         * Trigger re-execution of the build job.
         */
        void repeatExecution();

        /**
         * Check wheather the build job has finished or not.
         * @return
         */
        boolean isRunning();

        /**
         * Request to stop the execution of the build job.
         */
        void stopRunning();

    }
    /**
     * Wrapper for the build job used by <code>BuildExecutionSupport</code>, extending <code>Item</code>,
     * allows collecting history of executions keyed by the <code>getAction()</code> values.
     * Implement <code>equals</code> and <code>hashcode</code> for advanced duplicate item resolution.
     * @since 1.69
     */
    public static interface ActionItem extends Item {
        /**
         * <code>ActionProvider</code> constants, used for grouping the history items, as of 1.69 only <code>ActionProvider.COMMAND_RUN</code> is supported in UI.
         * @return never null
         */
        String getAction();
        
        /**
         * used for memory releasing purposes, all items from given project will be removed when the project is no longer opened.
         * @return directory fileobject or null when not part of a project execution 
         */
        FileObject getProjectDirectory();
    }
}
