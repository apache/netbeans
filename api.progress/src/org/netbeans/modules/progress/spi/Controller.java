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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.progress.spi;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.progress.module.DefaultHandleFactory;
import org.netbeans.progress.module.TrivialProgressBaseWorkerProvider;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 * @since org.netbeans.api.progress/1 1.18
 */
public class Controller {
    
    // non-private so that it can be accessed from the tests
    public static Controller defaultInstance;
    
    private ProgressUIWorker component;
    private TaskModel model;
    private List<ProgressEvent> eventQueue;
    private boolean dispatchRunning;
    private long timerStart = 0;
    private static final int TIMER_QUANTUM = 400;
    
    /**
     * initial delay for ading progress indication into the UI. if finishes earlier,
     * not shown at all, applies just to the status line (default) comtroller.
     */
    public static final int INITIAL_DELAY = 500;
    
    /** Creates a new instance of Controller */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Controller(ProgressUIWorker comp) {
        component = comp;
        model = new TaskModel(getEventExecutor());
        eventQueue = new LinkedList<ProgressEvent>();
        dispatchRunning = false;
    }

    public static synchronized Controller getDefault() {
        if (defaultInstance == null) {
            ProgressEnvironment f = DefaultHandleFactory.get();
            defaultInstance = f.getController();
        }
        return defaultInstance;
    }
    
    /**
     * Creates a worker. The default implementation creates a trivial no-op
     * worker. Subclasses should override to create a meaningful worker implementation.
     * @return worker instance 
     */
    protected ProgressUIWorkerWithModel createWorker() {
        Logger.getLogger(Controller.class.getName()).log(Level.CONFIG, "Using fallback trivial progress implementation");
        return new TrivialProgressBaseWorkerProvider().getDefaultWorker();
    }
    
    /**
     * Retrieves the ProgressUIWorker instance.
     * The instance is lazily created by calling {@link #createWorker}.
     * @return ProgressUIWorker instance
     */
    protected final ProgressUIWorker getProgressUIWorker()
    {
        if (component == null)
        {
            ProgressUIWorkerWithModel prgUIWorker = createWorker();
            prgUIWorker.setModel(getDefault().getModel());
            component = prgUIWorker;
        }
        return component;
    }

    public TaskModel getModel() {
        return model;
    }
    
    void start(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_START, isWatched(handle));
        if (this == getDefault() && handle.getInitialDelay() > 100) {
            // default controller
            postEvent(event, true);
        } else {
            runImmediately(Collections.singleton(event));
        }
    }
    
    void finish(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_FINISH, isWatched(handle));
        postEvent(event);
    }
    
    void toIndeterminate(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SWITCH, isWatched(handle));
        model.updateSelection();
        postEvent(event);
    }
    
    void toSilent(InternalHandle handle, String message) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SILENT, isWatched(handle), message);
        model.updateSelection();
        postEvent(event);
    }
    
    
    void toDeterminate(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SWITCH, isWatched(handle));
        model.updateSelection();
        postEvent(event);
    }    
    
    void progress(InternalHandle handle, String msg, 
                  int units, double percentage, long estimate) {
        ProgressEvent event = new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
        postEvent(event);
    }
    
    ProgressEvent snapshot(InternalHandle handle, String msg, 
                  int units, double percentage, long estimate) {
        if (handle.isInSleepMode()) {
            return new ProgressEvent(handle, ProgressEvent.TYPE_SILENT, isWatched(handle), msg);
        }
        return new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
    }
    
    
    void explicitSelection(InternalHandle handle) {
        InternalHandle old = model.getExplicitSelection();
        model.explicitlySelect(handle);
        Collection<ProgressEvent> evnts = new ArrayList<ProgressEvent>();
        evnts.add(handle.requestStateSnapshot());
        if (old != null && old != handle) {
            // refresh the old one, results in un-bodling the text.
            evnts.add(old.requestStateSnapshot());
        }
        runImmediately(evnts);
    }
    
    void displayNameChange(InternalHandle handle, int units, double percentage, long estimate, String display) {
        Collection<ProgressEvent> evnts = new ArrayList<ProgressEvent>();
        evnts.add(new ProgressEvent(handle, null, units, percentage, estimate, isWatched(handle), display));
        runImmediately(evnts);
    }
    
    private boolean isWatched(InternalHandle hndl) {
        return model.getExplicitSelection() == hndl;
    }
    
    void runImmediately(Collection<ProgressEvent> events) {
        synchronized (this) {
            // need to add to queue immediately in the current thread
            eventQueue.addAll(events);
            dispatchRunning = true;
        }
        // trigger ui update as fast as possible.
        runEvents();
    }
    
    void postEvent(final ProgressEvent event) {
        postEvent(event, false);
    }
    
    /**
     * Schedules a callback after `delay' milliseconds. If there's some initial
     * delay, and `shorten' is true, the delay is shortened to be minimum of the current
     * delay and the `delay'.
     * <p/>
     * The scheduler ticks can be disabled if the specified `delay' is 0.
     * <p/>
     * This method is to be implemented by environment-specific subclass, to use the target
     * platform scheduling support to invoke the 'runNow' method after the specified period.
     * <p/>
     * The default implementation uses a private RequestProcessor thread.
     * @param delay the delay after which the callback should be fired. -1 means no chage to the configured delay. 0 means to stop
     * @param shorten if the current delay is shorter than the `delay', keep the current one. If false, always configure the delay
     * @param activate restart ticks. If false, just configure the scheduler.
     */
    final void schedule(int delay, boolean shorten, boolean activate) {
        if (delay != -1) {
            if (shorten && taskDelay < delay) {
                delay = taskDelay;
            }
        } else {
            delay = taskDelay;
        }
        this.taskDelay = delay;
        
        resetTimer(delay, activate);
    }
    
    private int taskDelay = TIMER_QUANTUM;
    
    private static final RequestProcessor RQ = new RequestProcessor(Controller.class.getName());
    
    private final RequestProcessor.Task   task = RQ.create(new Runnable() {
        public void run() {
            runEvents();
        }
    });
    
    void postEvent(final ProgressEvent event, boolean shortenPeriod) {
        synchronized (this) {
            eventQueue.add(event);
            if (!dispatchRunning) {
                timerStart = System.currentTimeMillis();
                dispatchRunning = true;
                schedule(shortenPeriod ? event.getSource().getInitialDelay() : -1, shortenPeriod, true);
            } else if (shortenPeriod) {
                // time remaining is longer than required by the handle's initial delay.
                // restart with shorter time.
                if (System.currentTimeMillis() - timerStart > event.getSource().getInitialDelay()) {
                    schedule(event.getSource().getInitialDelay(), false, true);
                }
            }
        }
    }
    
    /**
     * The method is responsible to start or stop a timing service in the environment.
     * If "delay" is 0 or less, the method should stop the timer, or at least not call
     * the {@link #runEvents} if the timer ticks. 
     * <p/>
     * If "delay" is positive, the method should configure the timer to fire after
     * "delay" milliseconds. Depending on "restart" parameter, the timer should
     * be just configured (false), or activated (true) - potentially canceling the previous
     * schedule.
     * <p/>
     * The default implementation uses {@link RequestProcessor} for scheduling.
     * 
     * @param delay delay in milliseconds before the timer should fire 
     * @param activate if true, activate the changes immediately. 
     */
    protected void resetTimer(int delay, boolean activate) {
        if (delay > 0) {
            if (activate) {
                task.schedule(delay);
            }
        } else {
            assert activate;
            task.cancel();
        }
    }
    
    /**
     * Executes runnable synchronously with controller's event dispatch.
     * The method is used by Progress API to deliver other events which do not
     * therefore interfere with Controller's own events. If called from the thread
     * dispatching events, the executed Runnables will be delayed after the
     * and may be interleaved by dispatched events.
     * <p/>
     * The method <b>must</b> be overriden by a controller implementation which
     * changes the threading model from the default one.
     * 
     * @return the executor instance
     * @since 1.44
     */
    protected Executor getEventExecutor() {
        return RQ;
    }

    /**
     * Processes the queued events. Depending on environment, planning to a specific thread,
     * or passing some specific information may be necessary. 
     */
    protected void runEvents() {
        RQ.execute(new Runnable() {
            public void run() {
                runNow();
            }
        });
    }
     
    
    public void runNow() {
        // not true in tests: assert EventQueue.isDispatchThread();
        HashMap<InternalHandle, ProgressEvent> map = new HashMap<InternalHandle, ProgressEvent>();
        boolean hasShortOne = false;
        long minDiff = TIMER_QUANTUM;
        
        InternalHandle oldSelected = model.getSelectedHandle();
        long stamp = System.currentTimeMillis();
        synchronized (this) {
            Iterator<ProgressEvent> it = eventQueue.iterator();
            Collection<InternalHandle> justStarted = new ArrayList<InternalHandle>();
            while (it.hasNext()) {
                ProgressEvent event = it.next();
                boolean isShort = (stamp - event.getSource().getTimeStampStarted()) < event.getSource().getInitialDelay();
                if (event.getType() == ProgressEvent.TYPE_START) {
                    if (event.getSource().isCustomPlaced() || !isShort) {
                        model.addHandle(event.getSource());
                    } else {
                        justStarted.add(event.getSource());
                    }
                }
                else if (event.getType() == ProgressEvent.TYPE_FINISH &&
                       (! justStarted.contains(event.getSource()))) 
                {
                    model.removeHandle(event.getSource());
                }
                ProgressEvent lastEvent = map.get(event.getSource());
                if (lastEvent != null && event.getType() == ProgressEvent.TYPE_FINISH && 
                        justStarted.contains(event.getSource()) && isShort)
                {
                    // if task quits really fast, ignore..
                    // defined 'really fast' as being shorter than initial delay
                    map.remove(event.getSource());
                    justStarted.remove(event.getSource());
                } else {
                    if (lastEvent != null) {
                        // preserve last message
                        event.copyMessageFromEarlier(lastEvent);
                        // preserve the switched state
                        if (lastEvent.isSwitched()) {
                            event.markAsSwitched();
                        }
                    }
                    map.put(event.getSource(), event);
                }
                it.remove();
            }
            // now re-add the just started events into queue
            // if they don't last longer than the initial delay of the task.
            // applies just for status bar items
            Iterator<InternalHandle> startIt = justStarted.iterator();
            while (startIt.hasNext()) {
                InternalHandle hndl = startIt.next();
                long diff = stamp - hndl.getTimeStampStarted();
                if (diff >= hndl.getInitialDelay()) {
                    model.addHandle(hndl);
                } else {
                    eventQueue.add(new ProgressEvent(hndl, ProgressEvent.TYPE_START, isWatched(hndl)));
                    ProgressEvent evnt = map.remove(hndl);
                    if (evnt.getType() != ProgressEvent.TYPE_START) {
                        eventQueue.add(evnt);
                    }
                    hasShortOne = true;
                    minDiff = Math.min(minDiff, hndl.getInitialDelay() - diff);
                }
            }
        }
        InternalHandle selected = model.getSelectedHandle();
        selected = selected == null ? oldSelected : selected;
        Iterator<ProgressEvent> it = map.values().iterator();
        if (component == null) {
            getProgressUIWorker();
        }
        while (it.hasNext()) {
            ProgressEvent event = it.next();
            if (selected == event.getSource()) {
                component.processSelectedProgressEvent(event);
            }
            component.processProgressEvent(event);
        }
        synchronized (this) {
            schedule(0, false, true);
            if (hasShortOne) {
                timerStart = System.currentTimeMillis();
                schedule((int)Math.max(100, minDiff), false, true);
            } else {
                dispatchRunning = false;
                schedule(TIMER_QUANTUM, false, !eventQueue.isEmpty());
            }
        }
    }

}
