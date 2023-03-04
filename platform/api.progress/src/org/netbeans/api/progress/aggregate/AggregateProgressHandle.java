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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;

/**
 * a progress handle that allows aggregation of progress indication from multiple
 * independant sources. All of the progress contributors are considered equal and are given
 * equal share of the global progress.
 * The task progress contributors can be added dynamically and
 * the progress bar adjusts accordingly, never stepping back though.
 *
 * For a more simple version of progress indication, see {@link org.netbeans.api.progress.ProgressHandle}
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public final class AggregateProgressHandle implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(AggregateProgressHandle.class.getName());

    private ProgressMonitor monitor;
    final ProgressHandle handle;
    static final int WORKUNITS = 10000;
    private boolean finished;
    private Collection<ProgressContributor> contributors;
    private int current;
    final String displayName;
    
    /** Creates a new instance of AggregateProgressHandle */
    AggregateProgressHandle(String displayName, ProgressContributor[] contribs, Cancellable cancellable, boolean systemtask,
            ProgressHandle hdl) {
        handle = hdl;
        finished = false;
        contributors = new ArrayList<ProgressContributor>();
        if (contribs != null) {
            for (int i = 0; i < contribs.length; i++) {
                addContributor(contribs[i]);
            }
        }
        this.displayName = displayName;
    }
    

    /**
     * start the progress indication for the task, shows the progress in the UI, events from the contributors are
     *  expected after this call.
     */
    public void start() {
        start(-1);
    }

    /**
     * start the progress indication for the task with an initial time estimate, shows the progress in the UI, events from the contributors are
     * expected after this call.
     * @param estimate estimated time to process the task in seconds
     */
    public synchronized void start(long estimate) {
        handle.start(WORKUNITS, estimate);
        current = 0;
    }  

    /**
     * Calls {@link #finish()}
     */
    @Override
    public void close() {
        finish();
    }
    
    /**
     * finish the task, remove the task's component from the progress bar UI, any additional incoming events from the 
     * contributors will be ignored.
     */
    public synchronized void finish() {
        if (finished) {
            return;
        }
        finished = true;
        handle.finish();
    }
    
    /**
     * Currently running task can switch to silent suspend mode where the progress bar 
     * stops moving, hides completely or partially. Useful to make progress in status bar less intrusive 
     * for very long running tasks, eg. running an ant script that executes user application, debugs user application etc.
     * Any incoming progress wakes up the progress bar to previous state.
     * @param message a message to display in the silent mode
     * @since org.netbeans.api.progress/1 1.10
     */
    public void suspend(String message) {
        LOG.log(Level.FINE, "{0}: {1}", new Object[] {displayName, message});
        handle.suspend(message);
    }    
    
    /**
     * allows to set a custom initial delay for the progress task to appear in the
     * status bar. This delay marks the time between starting of the progress handle
     * and it's appearance in the status bar. If it finishes earlier, it's not shown at all.
     * There is a default < 1s value for this. If you want to to appear earlier or later, 
     * call this method with the value you prefer before starting the handle.
     * <p> Progress bars that are placed in custom dialogs do always appear right away without a delay.
     * @param millis amount of miliseconds that shall pass before the progress appears in status bar.
     */
    public void setInitialDelay(int millis) {
       handle.setInitialDelay(millis); 
    }    
    
    /**
     * add a contributor to the global, aggregated progress.
     * Adding makes sense only if the task is still in progress.
     */
    public synchronized void addContributor(ProgressContributor contributor) {
        if (finished) {
            return;
        }
//        System.out.println("adding contributor=" + contributor.getTrackingId());
        int length = contributors.size();
        int remainingUnits = 0;
        double completedRatio = 0;
        Iterator<ProgressContributor> it;
        if (length > 0) {
            it = contributors.iterator();
            while (it.hasNext()) {
                ProgressContributor cont = it.next();
                remainingUnits = remainingUnits + cont.getRemainingParentWorkUnits();
                completedRatio = completedRatio + (1 - cont.getCompletedRatio());
            }
        } else {
            remainingUnits = WORKUNITS;
            completedRatio = 0;
        }

//        int idealShare = WORKUNITS / (length + 1);
        int currentShare = (int)(remainingUnits / (completedRatio + 1));
//        System.out.println("ideal share=" + idealShare);
//        System.out.println("current share=" + currentShare);
        it = contributors.iterator();
        while (it.hasNext()) {
            ProgressContributor cont = it.next();
            int newshare = (int)((1 - cont.getCompletedRatio()) * currentShare);
//            System.out.println(" new share for " + cont.getTrackingId() + " is " + newshare);
            remainingUnits = remainingUnits - newshare;
            cont.setAvailableParentWorkUnits(newshare);
        }
//        System.out.println("new contributor share is=" + remainingUnits);
        contributor.setAvailableParentWorkUnits(remainingUnits);
        contributors.add(contributor);
        contributor.setParent(this);
        
    }
    
    /**
     * @deprecated do, not use, for tests only
     */
    @Deprecated
    int getCurrentProgress() {
        return current;
    }
    
    
    void processContributorStep(ProgressContributor contributor, String message, int delta) {
        synchronized (this) {
            if (finished) {
                return;
            }
            current = current + delta;
            handle.progress(message, current);
        }
        //shall we sychronize the monitor calls? since it calls out to client code,
        // cannot guarantee how long it will last..
        if (monitor != null) {
            monitor.progressed(contributor);
        }
        
    }
    
    void processContributorStart(ProgressContributor contributor, String message) {
        synchronized (this) {
            if (finished) {
                return;
            }
            if (message != null) {
                handle.progress(message);
            }
        }
        //shall we sychronize the monitor calls? since it calls out to client code,
        // cannot guarantee how long it will last..
        if (monitor != null) {
            monitor.started(contributor);
        }
    }
    
    void processContributorFinish(ProgressContributor contributor) {
        synchronized (this) {
            if (finished) {
                return;
            }
            contributors.remove(contributor);
            if (contributors.size() == 0) {
                finish();
            }
        }
        //shall we sychronize the monitor calls? since it calls out to client code,
        // cannot guarantee how long it will last..
        if (monitor != null) {
            monitor.finished(contributor);
        }
    }
    
    
    /**
     * allow to watch the incoming events from the individual progress contributors.
     */
    public void setMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }
    
    /**
     * change the display name of the progress task. Use with care, please make sure the changed name is not completely different,
     * or otherwise it might appear to the user as a different task.
     * @since 1.5
     */
    public void setDisplayName(String newDisplayName) {
        handle.setDisplayName(newDisplayName);
    }
}
