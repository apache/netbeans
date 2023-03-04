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

package org.openide.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Performance helper class, allows to run post-init task for given component.
 * Can also handle cancel logic if contained in AsyncGUIJob.
 * Class is designed for one time use, can't be used to perform async init
 * more then once.
 * Restrictions: Note that for correct functionality given component must not
 * be showing at construction time of this class, however shouldn't stay hidden
 * forever as memory leak may occur.
 *
 * @author Dafe Simonek
 */
final class AsyncInitSupport implements HierarchyListener, Runnable, ActionListener {
    /** lock for access to wasCancelled flag */
    private static final Object CANCELLED_LOCK = new Object();
    private static final Logger LOG = Logger.getLogger(AsyncInitSupport.class.getName()); 

    /** task in which post init code from AsyncJob is executed */
    private Task initTask;

    /** true after cancel request came, false otherwise */
    private boolean wasCancelled;

    /** Component requesting asynchronous initialization */
    private Component comp4Init;

    /** Job that performs async init task */
    private AsyncGUIJob initJob;
    
    /** Timer for delaying asynchronous init job to enable some painting first */
    Timer timer = null;

    /** Creates a new instance of AsyncInitComponent
     * @param comp4Init Component to be initialized. Mustn't be showing at this
     * time. IllegalStateException is thrown if component is already showing.
     * @param initJob Instance of initialization job.
     */
    public AsyncInitSupport(Component comp4Init, AsyncGUIJob initJob) {
        this.comp4Init = comp4Init;
        this.initJob = initJob;
        if (comp4Init.isShowing()) {
            throw new IllegalStateException("Component already shown, can't be inited: " + comp4Init);
        }

        comp4Init.addHierarchyListener(this);
        LOG.log(Level.FINE, "addHierarchyListener for {0}", comp4Init);
    }
    
    /** Impl of HierarchyListener, starts init job with delay when component shown,
     * stops listening to asociated component it isn't showing anymore,
     * calls cancel if desirable.
     * @param evt hierarchy event
     */
    @Override
    public void hierarchyChanged(HierarchyEvent evt) {
        final boolean hierachyChanged = (evt.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;
        LOG.log(Level.FINE, "Hierarchy Changed {0}", hierachyChanged);
        if (hierachyChanged) {
            boolean isShowing = comp4Init.isShowing();
            if (timer == null && isShowing) {
                timer = new Timer(20, this);
                timer.setRepeats(false);
                timer.start();
                LOG.log(Level.FINE, "Timer started for {0}", comp4Init);
            } else if (!isShowing) {
                comp4Init.removeHierarchyListener(this);
                LOG.log(Level.FINE, "Not showing, cancling for {0}", comp4Init);
                cancel();
            }
        }
    }

    /** Impl of ActionListener, called from hierarchyChanged through a Timer,
     * starts the job */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (wasCancelled || (initTask != null)) {
            //If cancelled or already started, our job is done, go away.
            LOG.log(Level.FINE, "Detaching {0}", comp4Init);
            detach();
            return;
        }

        if ((comp4Init != null) && comp4Init.isDisplayable()) {
            //If the component has a parent onscreen, we're ready to run.
            LOG.log(Level.FINE, "Starting {0}", comp4Init);
            start();
        }
    }

    private void start() {
        detach();

        if (initTask == null) {
            initTask = RequestProcessor.getDefault().post(this);
        }
    }

    private void detach() {
        if (timer != null) {
            timer.stop();
        }
    }

    /** Body of task executed in RequestProcessor. Runs AsyncGUIJob's worker
     * method and after its completion posts AsyncJob's UI update method
     * to AWT thread.
     */
    @Override
    public void run() {
        if (!SwingUtilities.isEventDispatchThread()) {
            LOG.log(Level.FINE, "Prepare outside AWT for {0}", comp4Init);
            // first pass, executed in some of RP threads
            initJob.construct();
            comp4Init.removeHierarchyListener(this);
            LOG.log(Level.FINE, "No hierarchy listener for {0}", comp4Init); 

            // continue to invoke finished method only if hasn't been cancelled 
            boolean localCancel;

            synchronized (CANCELLED_LOCK) {
                localCancel = wasCancelled;
            }

            LOG.log(Level.FINE, "wasCancelled {0}", localCancel);
            if (!localCancel) {
                SwingUtilities.invokeLater(this);
            }
        } else {
            // second pass, executed in event dispatch thread
            initJob.finished();
            LOG.fine("Second pass finished");
        }
    }

    /** Delegates valid cancel requests to asociated AsyncGUIJob, in the case
     * job supports cancelling. */
    private void cancel() {
        if ((initTask != null) && !initTask.isFinished() && (initJob instanceof Cancellable)) {
            synchronized (CANCELLED_LOCK) {
                LOG.log(Level.FINE, "Cancelling for {0}", comp4Init);
                wasCancelled = true;
            }
            ((Cancellable) initJob).cancel();
            LOG.fine("Cancelling done");
        }
    }
    
}
