/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.api.progress;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.progress.module.DefaultHandleFactory;
import org.netbeans.progress.module.ProgressApiAccessor;
import org.openide.modules.PatchedPublic;
import org.openide.util.Cancellable;

/**
 * Instances provided by the ProgressHandleFactory allow the users of the API to
 * notify the progress bar UI about changes in the state of the running task.
 * Progress component will be visualized only after one of the start() methods.
 * <p/>
 * This class was adapted to contain only non-Swing part of the Progress API. If you need
 * to use progress UI components, please work with {@code org.netbeans.api.progres.ProgressUIHandle} 
 * class from {@code org.netbeans.api.progress.nb} module.
 * 
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public final class ProgressHandle {

    private static final Logger LOG = Logger.getLogger(ProgressHandle.class.getName());

    private InternalHandle internal;
    
    /**
     * Create a progress ui handle for a long lasting task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName) {
        return createHandle(displayName, null);
    }
    
    /**
     * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel) {
        return DefaultHandleFactory.get().createHandle(displayName, allowToCancel, true);
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
        return DefaultHandleFactory.get().createHandle(displayName, allowToCancel, false);
    }

    /** Creates a new instance of ProgressHandle */
    ProgressHandle(InternalHandle internal) {
        LOG.fine(internal.getDisplayName());
        this.internal = internal;
    }

    
    /**
     * start the progress indication for indeterminate task. 
     * it will be visualized by a progress bar in indeterminate mode.
     * <code>start</code> method can be called just once.
     */
    public final void start() {
        start(0, -1);
    }
    
    /**
     * start the progress indication for a task with known number of steps.
     * <code>start</code> method can be called just once.
     * @param workunits total number of workunits that will be processed
     */
    public final void start(int workunits) {
       start(workunits, -1); 
    }
    
    
    /**
     * start the progress indication for a task with known number of steps and known
     * time estimate for completing the task.
     * <code>start</code> method can be called just once.
     * @param workunits total number of workunits that will be processed
     * @param estimate estimated time to process the task in seconds
     */
    
    public final void start(int workunits, long estimate) {
       internal.start("", workunits, estimate);
    }


    /**
     * Currently determinate task (with percentage or time estimate) can be 
     * switched to indeterminate mode.
     * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
     */
    public final void switchToIndeterminate() {
        internal.toIndeterminate();
    }
    
    /**
     * Current task can switch to silent suspend mode where the progress bar stops moving, hides completely or partially. 
     * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
     * Useful to make progress in status bar less intrusive. 
     * for very long running tasks, eg. running an ant script that executes user application, debugs user application etc.
     * Any incoming progress wakes up the progress bar to previous state.
     * @param message a message to display in the silent mode
     * @since org.netbeans.api.progress/1 1.9
     */
    public final void suspend(String message) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0}: {1}", new Object[] {internal.getDisplayName(), message});
        }
        internal.toSilent(message);
    }
    
    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
     * A common usecase is to calculate the amount of work in the beginning showing 
     * in indeterminate mode and later switch to the progress with known steps
     * @param workunits a definite number of complete units of work out of the total
     */
    public final void switchToDeterminate(int workunits) {
        internal.toDeterminate(workunits, -1);
    }
    
    /**
     * Currently indeterminate task can be switched to show the time estimate til completion.
     * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
     * A common usecase is to calculate the amount of work in the beginning 
     * in indeterminate mode and later switch to the progress with the calculated estimate.
     * @param workunits a definite number of complete units of work out of the total
     * @param estimate estimated time to process the task, in seconds
     */
    public final void switchToDeterminate(int workunits, long estimate) {
        internal.toDeterminate(workunits, estimate);
    }
    
    /**
     * Finish the task, remove the task's component from the progress bar UI.
     * This method has to be called after calling <code>start</code> method (the task has to be running).
     */
    public final void finish() {
        internal.finish();
    }
    
    
    /**
     * Notify the user about completed workunits.
     * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
     * @param workunit a cumulative number of workunits completed so far
     */
    public final void progress(int workunit) {
        progress(null, workunit);
    }
    
    /**
     * Notify the user about progress by showing message with details.
     * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
     * @param message details about the status of the task
     */
    public final void progress(String message) {
        progress(message, InternalHandle.NO_INCREASE);
    }
    
    /**
     * Notify the user about completed workunits and show additional detailed message.
     * This method has to be called after calling <code>start</code> method and before calling <code>finish</code> method (the task has to be running).
     * @param message details about the status of the task
     * @param workunit a cumulative number of workunits completed so far
     */
    public final void progress(String message, int workunit) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "{0}: {1}", new Object[] {internal.getDisplayName(), message});
        }
        internal.progress(message, workunit);
    }
    

    /**
     * Set a custom initial delay for the progress task to appear in the status bar.
     * This delay marks the time between starting of the progress handle
     * and its appearance in the status bar. If it finishes earlier, it's not shown at all.
     * There is a default &lt; 1s value for this. If you want it to appear earlier or later,
     * call this method with the value you prefer <strong>before {@linkplain #start() starting}</strong> the handle.
     * (Has no effect if called after the handle is started.)
     * <p> Progress bars that are placed in custom dialogs do always appear right away without a delay.
     * @param millis number of miliseconds to wait before the progress appears in status bar.
     * @since org.netbeans.api.progress/1 1.2
     */
    public final void setInitialDelay(int millis) {
       internal.setInitialDelay(millis); 
    }
    
    /**
     * Change the display name of the progress task. Use with care, please make sure the changed name is not completely different,
     * or otherwise it might appear to the user as a different task.
     * @param newDisplayName a new name to set for the task
     * @since org.netbeans.api.progress 1.5
     */
    public final void setDisplayName(String newDisplayName) {
        LOG.fine(newDisplayName);
        internal.requestDisplayNameChange(newDisplayName);
    }
    
    /**
     * for unit testing only..
     */
    @PatchedPublic
    final InternalHandle getInternalHandle() {
        return internal;
    }

    String getDisplayName() {
        //XXX internal.getDisplayName() improperly synchronized
        synchronized (internal) {
            return internal.getDisplayName();
        }
    }
    
    static class Accessor extends ProgressApiAccessor {

        @Override
        public ProgressHandle create(InternalHandle h) {
            return new ProgressHandle(h);
        }

        @Override
        public InternalHandle getInternalHandle(ProgressHandle h) {
            return h.getInternalHandle();
        }
    }
    
    static {
        ProgressApiAccessor.setInstance(new Accessor());
    }
}
