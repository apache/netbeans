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

package org.netbeans.libs.git.progress;

/**
 * Used to follow progress and control flow of git commands.
 * An instance of <code>GitClient</code> provides methods to start proper git 
 * commands and such a method accepts an instance if <code>ProgressMonitor</code>
 * as its argument.
 * Clients of the git API may extend this class and through its methods catch
 * error and warning messages produced by a git command, changes in the state 
 * of a command (when the command is started and when it's finished) and they may
 * cancel a running command by implementing the <code>isCanceled</code> method.
 */
public abstract class ProgressMonitor {
    
    /**
     * A constant indicating an unknown number of work units of a task.
     * @since 1.21
     */
    public static final int UNKNOWN_WORK_UNITS = org.eclipse.jgit.lib.ProgressMonitor.UNKNOWN;
    
    /**
     * Returns <code>true</code> if the progress should be canceled.
     * Git commands periodically check the result of the method and end their
     * progress immediately when the method returns <code>true</code>
     * @return true if the progress should be canceled
     */
    public abstract boolean isCanceled ();

    /**
     * Called when a git command is started.
     * Implement this method to catch the event.
     * @param command a string representing a commandline version of the started command
     */
    public abstract void started (String command);

    /**
     * Called by a git command when it finishes its progress.
     */
    public abstract void finished();

    /**
     * Called when a git command fails to start.
     * Implement this method to catch a description of an error occurred during
     * a git command initialization that prevents it from start running.
     * @param message error description
     */
    public abstract void preparationsFailed (String message);

    /**
     * Called when an error occurs during a git command's execution that however
     * does not prevent it from further actions.
     * Some commands for example operate with many files and if an error occurs 
     * while working (e.g. checkout) with one of them, this method is called and 
     * the command continues with other files.
     * @param message description of the error
     */
    public abstract void notifyError (String message);

    /**
     * Called when a non-fatal warning should be delivered to a git command's
     * caller.
     * @param message description of the warning 
     */
    public abstract void notifyWarning (String message);

    /**
     * Notifies about an informational message passed from the running command.
     * @param message informational message
     * @since 1.30
     */
    public void notifyMessage (String message) {
    }

    /**
     * Implementors may override this to be notified when a task is started
     * during a command execution. The task may or may not know its precise
     * number of steps. {@link #UNKNOWN_WORK_UNITS} as the value of
     * <code>totalWorkUnits</code> indicates the task cannot anticipate the
     * number of steps it will take.
     * <p>
     * This may be useful for long commands such as clone or fetch to update
     * progress bars etc.
     *
     * @param taskName name of the task
     * @param totalWorkUnits number of work units the task will take
     * @since 1.21
     */
    public void beginTask (String taskName, int totalWorkUnits) {
        
    }

    /**
     * Override to get notified when the currently running task finishes one or
     * more of its scheduled steps.
     *
     * @param completed number of completed steps since the last call. The value
     * is not a sum of all finished steps but an incremental value.
     * @since 1.21
     */
    public void updateTaskState (int completed) {
        
    }

    /**
     * Called when the currently running task ends.
     *
     * @since 1.21
     */
    public void endTask () {
        
    }

    /**
     * Basic implementation of the <code>ProgressMonitor</code> abstract class.
     * Provides no functionality except for canceling a running command.
     * To cancel a running command invoke the <code>cancel</code> method.
     */
    public static class DefaultProgressMonitor extends ProgressMonitor {
        private boolean canceled;

        /**
         * Cancels a currently running command.
         * @return <code>false</code> if the command has already been canceled
         * before. Otherwise returns <code>true</code>
         */
        public final synchronized boolean cancel () {
            boolean alreadyCanceled = canceled;
            canceled = true;
            return !alreadyCanceled;
        }

        @Override
        public final synchronized boolean isCanceled () {
            return canceled;
        }

        @Override
        public void started (String command) {
        }

        @Override
        public void finished() {
        }

        @Override
        public void preparationsFailed (String message) {
        }

        @Override
        public void notifyError (String message) {
        }

        @Override
        public void notifyWarning (String message) {
        }
    }
}
