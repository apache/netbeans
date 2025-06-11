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


package org.netbeans.modules.progress.spi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.progress.module.*;
import org.openide.modules.PatchedPublic;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 * Instances provided by the ProgressHandleFactory allow the users of the API to
 * notify the progress bar UI about changes in the state of the running task.
 * @author Milos Kleint (mkleint@netbeans.org)
 * @since org.netbeans.api.progress/1 1.18
 */
public class InternalHandle {

    private static final Logger LOG = Logger.getLogger(InternalHandle.class.getName());
    
    private String displayName;
    private int state;
    private int totalUnits;
    private int currentUnit;
    private long initialEstimate;
    private long timeStarted;
    private long timeLastProgress;
    private long timeSleepy = 0;
    private String lastMessage;
    private final Cancellable cancelable;
    private final boolean userInitiated;
    private int initialDelay = Controller.INITIAL_DELAY;
    private Controller controller;
    private ProgressHandle handle;
    private boolean customPlaced;
    
    public static final int STATE_INITIALIZED = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISHED = 2;
    public static final int STATE_REQUEST_STOP = 3;

    public static final int NO_INCREASE = -2;
    
    /** For compatibility only, not used for new clients, package access */ 
    InternalHandle del;
    
    /** Creates a new instance of ProgressHandle */
    @PatchedPublic
    protected InternalHandle(String displayName, 
                   Cancellable cancel,
                   boolean userInitiated) {
        this.displayName = displayName;
        this.userInitiated = userInitiated;
        state = STATE_INITIALIZED;
        totalUnits = 0;
        lastMessage = null;
        cancelable = cancel;
        
        compatInit();
    }
    
    /**
     * Creates a {@link ProgressHandle} instance, which works with this SPI.
     * @return a fresh instance of ProgressHandle
     * @since 1.40
     */
    public final ProgressHandle createProgressHandle() {
        synchronized (this) {
            if (handle != null) {
                return handle;
            }
        }
        ProgressHandle h;
        if (del != null) {
            h = ProgressApiAccessor.getInstance().create(del);
        } else {
            h = ProgressApiAccessor.getInstance().create(this);
        }
        synchronized (this) {
            if (handle == null) {
                handle = h;
            }
            return handle;
        }
    }
    
    public String getDisplayName() {
        if (del != null) {
            return del.getDisplayName();
        }
        return displayName;
    }

    /**
     * XXX - called from UI, threading
     */
    public synchronized int getState() {
        if (del != null) {
            return del.getState();
        }
        return state;
    }
    
    public boolean isAllowCancel() {
        if (del != null) {
            return del.isAllowCancel();
        }
        return cancelable != null;
    }
    
    public boolean isAllowView() {
        if (del != null) {
            return del.isAllowView();
        }
        return false;
    }
    
    public boolean isCustomPlaced() {
        if (del != null) {
            return del.isCustomPlaced();
        }
        return customPlaced;
    }
    
    public final boolean isUserInitialized() {
        return userInitiated;
    }
    
    private int getCurrentUnit() {
        return currentUnit;
    }
    
    public int getTotalUnits() {
        if (del != null) {
            return del.getTotalUnits();
        }
        return totalUnits;
    }
    
    public void setInitialDelay(int millis) {
        if (del != null) {
            del.setInitialDelay(millis);
            return;
        }
        if (state != STATE_INITIALIZED) {
            LOG.log(Level.WARNING, "Setting ProgressHandle.setInitialDelay() after the task is started has no effect at {0}", LoggingUtils.findCaller()); //NOI18N
            return;
        }
        initialDelay = millis;
    }
    
    public int getInitialDelay() {
        if (del != null) {
            return del.getInitialDelay();
        }
        return initialDelay;
    }
    
    public synchronized void toSilent(String message) {
        if (del != null) {
            del.toSilent(message);
            return;
        }
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            LOG.log(Level.WARNING, "Cannot switch to silent mode when not running at {0}", LoggingUtils.findCaller()); //NOI18N
            return;
        }
        timeLastProgress = System.currentTimeMillis();
        timeSleepy = timeLastProgress;
        if (message != null) {
            lastMessage = message;
        }
        controller.toSilent(this, message);
    }
    
    public boolean isInSleepMode() {
        if (del != null) {
            return del.isInSleepMode();
        }
        return timeSleepy == timeLastProgress;
    }
    
    public synchronized void toIndeterminate() {
        if (del != null) {
            del.toIndeterminate();
            return;
        }
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            LOG.log(Level.WARNING, "Cannot switch to indeterminate mode when not running at {0}", LoggingUtils.findCaller());
            return;
        }
        totalUnits = 0;
        currentUnit = 0;
        initialEstimate = -1;
        timeLastProgress = System.currentTimeMillis();
        controller.toIndeterminate(this);
    }
    
    public synchronized void toDeterminate(int workunits, long estimate) {
        if (del != null) {
            del.toDeterminate(workunits, estimate);
        }
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            LOG.log(Level.WARNING, "Cannot switch to determinate mode when not running at {0}", LoggingUtils.findCaller()); //NOI18N
            return;
        }
        if (workunits < 0) {
            throw new IllegalArgumentException("number of workunits cannot be negative");
        }        
        totalUnits = workunits;
        currentUnit = 0;
        initialEstimate = estimate;
        timeLastProgress = System.currentTimeMillis();
        controller.toDeterminate(this);
    }
    
    protected final void setController(Controller ctrl) {
        assert this.controller == null : "Controller can be set just once"; // NOI18N
        this.controller = ctrl;
    }
    
    /**
     * start the progress indication for a task with known number of steps and known
     * time estimate for completing the task.
     * 
     * @param message 
     * @param workunits 
     * @param estimate estimated time to process the task in seconds
     */
    public synchronized void start(String message, int workunits, long estimate) {
        if (del != null) {
            del.start(message, workunits, estimate);
            return;
        }
        if (state != STATE_INITIALIZED) {
            LOG.log(Level.WARNING, "Cannot call start twice on a handle at {0}", LoggingUtils.findCaller()); //NOI18N
            return;
        }
        if (workunits < 0) {
            throw new IllegalArgumentException("number of workunits cannot be negative");
        }
        totalUnits = workunits;
        currentUnit = 0;
        if (message != null) {
            lastMessage = message;
        }
        if (controller == null) {
            controller = Controller.getDefault();
        }
        state = STATE_RUNNING;
        initialEstimate = estimate;
        timeStarted = System.currentTimeMillis();
        timeLastProgress = timeStarted;

        
        controller.start(this);
    }

    /**
     * finish the task, remove the task's component from the progress bar UI.
     */
    public synchronized void finish() {
        if (del != null) {
            del.finish();
            return;
        }
        if (state == STATE_INITIALIZED) {
            LOG.log(Level.WARNING, "Cannot finish a task that was never started at {0}", LoggingUtils.findCaller()); //NOI18N
            return;
        }
        // handle is already finished, just return
        if (state == STATE_FINISHED) {
            return;
        }
        state = STATE_FINISHED;
        currentUnit = totalUnits;
        
        controller.finish(this);
    }
    
    
    /**
     * 
     * @param message 
     * @param workunit 
     */
    public synchronized void progress(String message, int workunit) {
        if (del != null) {
            del.progress(message, workunit);
            return;
        }
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            LOG.log(Level.WARNING, "Cannot call progress on a task that was never started at {0}", LoggingUtils.findCaller()); //NOI18N
            return;
        }

        if (workunit != NO_INCREASE) {
            if (workunit < currentUnit) {
                throw new IllegalArgumentException("Cannot decrease processed workunit count (" + workunit + ") to lower value than before (" + currentUnit + ")");
            }
            if (workunit > totalUnits) {
                // seems to be the by far most frequently abused contract. Record it to log file and safely handle the case
                //#96921 - WARNING -> INFO to prevent users reporting the problem automatically.
                LOG.log(Level.INFO,
                    "Cannot process more work than scheduled. " +
                    "Progress handle with name \"" + getDisplayName() + "\" has requested progress to workunit no." + workunit + 
                    " but the total number of workunits is " + totalUnits + ". That means the progress bar UI will not display real progress and will stay at 100%.",
                    new IllegalArgumentException()
                );
                workunit = totalUnits;
            }
            currentUnit = workunit;
        }
        if (message != null) {
            lastMessage = message;
        }
        timeLastProgress = System.currentTimeMillis();
        
        controller.progress(this, message, currentUnit, 
                            totalUnits > 0 ? getPercentageDone() : -1, 
                            (initialEstimate == -1 ? -1 : calculateFinishEstimate()));
    }
    
    
  // XXX - called from UI, threading

    public void requestCancel() {
        if (del != null) {
            del.requestCancel();
            return;
        }
        if (!isAllowCancel()) {
            return;
        }
        synchronized (this) {
            state = STATE_REQUEST_STOP;
        }
        // do not call in synchronized block because it can take a long time to process, 
        //  and it could slow down UI.
        //TODO - call in some other thread, not AWT? what is the cancel() contract?
        cancelable.cancel();
        synchronized (this) {
            requestStateSnapshot();
        }
    }
    
   //XXX - called from UI, threading
    public void requestView() {
        if (del != null) {
            del.requestView();
        }
    }
    
   // XXX - called from UI, threading
    public synchronized void requestExplicitSelection() {
        if (del != null) {
            del.requestExplicitSelection();
            return;
        }
        if (!isInSleepMode()) {
            timeLastProgress = System.currentTimeMillis();
        }
        controller.explicitSelection(this);
    }

    /**
     * Request a interaction callback to be attached to the Handle. The 
     * implementation decides if the callback is permitted and desirable. One command,
     * {@link ProgressHandle#ACTION_VIEW} is defined as a default command (action) for
     * the progress handle presentation. Implementations are free to ignore request
     * for adding actions.
     * @param actionCommand command to bind the action for.
     * @param action action instance
     * @return true, if the handle agrees to support the action.
     * @since 1.59
     */
    public boolean requestAction(String actionCommand, Action action) {
        return false;
    }
    
    public synchronized void requestDisplayNameChange(String newDisplayName) {
        if (del != null) {
            del.requestDisplayNameChange(newDisplayName);
            return;
        }
        displayName = newDisplayName;
        if (state == STATE_INITIALIZED) {
            return;
        }
        timeLastProgress = System.currentTimeMillis();
        controller.displayNameChange(this, currentUnit, 
                            totalUnits > 0 ? getPercentageDone() : -1, 
                            (initialEstimate == -1 ? -1 : calculateFinishEstimate()), newDisplayName);
    }
    
// XXX - called from UI, threading 
    public synchronized ProgressEvent requestStateSnapshot() {
        if (del != null) {
            // TODO - event.getSource() exposes the delegate InternalHandle
            return del.requestStateSnapshot();
        }
        if (!isInSleepMode()) {
            timeLastProgress = System.currentTimeMillis();
        }
        return controller.snapshot(this, lastMessage, currentUnit, 
                            totalUnits > 0 ? getPercentageDone() : -1, 
                            (initialEstimate == -1 ? -1 : calculateFinishEstimate()));
    }
    
    long calculateFinishEstimate() {
        
        // we are interested in seconds only
        double durationSoFar = ((double)(System.currentTimeMillis() - timeStarted)) / 1000;
        if (initialEstimate == -1) {
            // we don't have an initial estimate, calculate by real-life data only
            return (long)(durationSoFar *  (totalUnits - currentUnit) / totalUnits);
        } else {
            // in the begining give the initial estimate more weight than in the end.
            // should give us more smooth estimates 
            long remainingUnits = (totalUnits - currentUnit);
            double remainingPortion = (double)remainingUnits / (double)totalUnits;
            double currentEstimate = durationSoFar / (double)currentUnit * totalUnits;
            long retValue = (long)(((initialEstimate * remainingUnits * remainingPortion) 
                         + (currentEstimate * remainingUnits * (1 - remainingPortion)))
                       / totalUnits); 
            return retValue;
        }
    }
    /**
     *public because of tests.
     */
    public double getPercentageDone() {
        if (del != null) {
            return del.getPercentageDone();
        }
        return ((double)currentUnit * 100 / (double)totalUnits); 
    }
    
    /**
     * Returns the last time the progress was updated. he timestamp
     * is updated on start, stop, every progress report and determinate / indeterminate
     * switch. Generally every time the ProgressHandle client publishes some information.
     * 
     * @return timestamp of last update, in milliseconds.
     * @since 1.45
     */
    public long getLastPingTime() {
        if (del != null) {
            return del.getLastPingTime();
        }
        return timeLastProgress;
    }

    public long getTimeStampStarted() {
        if (del != null) {
            return del.getTimeStampStarted();
        }
        return timeStarted;
    }
    
    static final Method compatInit;
    
    private void compatInit() {
        if (compatInit == null) {
            return;
        }
        try {
            compatInit.invoke(this, displayName, cancelable, userInitiated);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    static {
        Method m = null;
        try {
            m = InternalHandle.class.getSuperclass().getDeclaredMethod("compatInit", 
                    String.class, Cancellable.class, Boolean.TYPE); // NOI18N
        } catch (NoSuchMethodException ex) {
            // OK
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        compatInit = m;
    }
    
    /**
     * Marks this handle as custom-placed. Handle should be marked as custom-placed
     * if some controller overtakes (part of) handle's presentation.
     * @since 1.59
     */
    protected final void markCustomPlaced() {
        if (getState() != STATE_INITIALIZED) {
            throw new IllegalStateException();
        }
        customPlaced = true;
    }
    
    @Override
    public String toString() {
        return "H@" + Integer.toHexString(System.identityHashCode(this)) + 
                "[\"" + getDisplayName() + "\", state: " + state + "]";
    }
}
