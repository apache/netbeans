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

package org.netbeans.modules.parsing.spi;

import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SchedulerAccessor;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceCache;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.implspi.SchedulerControl;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/**
 * Scheduler defines when tasks should be started. Some {@link SchedulerTask}s (like syntax
 * coloring) are current document sensitive only. It means that such {@link SchedulerTask} 
 * is automatically scheduled when currently edited document is changed.
 * Other tasks may listen on different events. Implementation of Scheduler
 * just listens on various IDE events, and call one of schedule() methods
 * when something interesting happens. Implementation of Parsing API just finds
 * all {@link SchedulerTask}s registered for this Scheduler and reschedules them.
 * Implementation of this class should be registered in your manifest.xml file
 * in "Editors/your mime type" folder.
 * 
 * @author Jan Jancura
 */
public abstract class Scheduler {
    
    /**
     * Default reparse delay
     */
    public static final int DEFAULT_REPARSE_DELAY = 500;

    private static final Logger LOG = Logger.getLogger(Scheduler.class.getName());

    /**
     * May be changed by unit test
     */
    int                     reparseDelay = DEFAULT_REPARSE_DELAY;

    //@GuardedBy("this")
    private Source          source;
    //@GuardedBy("this")
    private PropertyChangeListener wlistener;
    
    /**
     * This implementations of {@link Scheduler} reschedules all tasks when:
     * <ol>
     * <li>current document is changed (file opened, closed, editor tab switched), </li>
     * <li>text in the current document is changed, </li>
     * <li>cusor position is changed</li>
     * </ol>
     */
    public static final Class<? extends Scheduler>
                            CURSOR_SENSITIVE_TASK_SCHEDULER;
    
    /**
     * This implementations of {@link Scheduler} reschedules all tasks when:
     * <ol>
     * <li>current document is changed (file opened, closed, editor tab switched), </li>
     * <li>text in the current document is changed</li>
     * </ol>
     */
    public static final Class<? extends Scheduler>
                            EDITOR_SENSITIVE_TASK_SCHEDULER;
    
    /**
     * This implementations of {@link Scheduler} reschedules all tasks when
     * nodes selected in editor are changed.
     */
    public static final Class<? extends Scheduler>
                            SELECTED_NODES_SENSITIVE_TASK_SCHEDULER;

    static {
        CURSOR_SENSITIVE_TASK_SCHEDULER = Utilities.findDefaultScheduler("CURSOR_SENSITIVE_TASK_SCHEDULER");
        EDITOR_SENSITIVE_TASK_SCHEDULER = Utilities.findDefaultScheduler("EDITOR_SENSITIVE_TASK_SCHEDULER");
        SELECTED_NODES_SENSITIVE_TASK_SCHEDULER = Utilities.findDefaultScheduler("SELECTED_NODES_SENSITIVE_TASK_SCHEDULER");
    }
    
    /**
     * Reschedule all tasks registered for <code>this</code> Scheduler (see
     * {@link ParserResultTask#getSchedulerClass()}.
     */
    protected final synchronized void schedule (
        SchedulerEvent      event
    ) {
        if (source != null)
            schedule (source, event);
    }

    private RequestProcessor 
                            requestProcessor;
    private Task            task;
    
    private final SchedulerControl         ctrl = new Control(this);
    
    /**
     * Reschedule all tasks registered for <code>this</code> Scheduler (see
     * {@link ParserResultTask#getSchedulerClass()}, and sets new {@link Source}s for them.
     * 
     * @param source       A {@link Source}.
     */
    //tzezula: really unclear usages of sources field (synchronization, live cycle, may it be called twice with different set of sources?).
    //tzezula: should set CHANGE_EXPECTED flag on the sources.
    protected final synchronized void schedule (
        final Source        source,
        final SchedulerEvent event) {
        if (task != null) {
            task.cancel ();
        }
        task = null;
        if (requestProcessor == null) {
            requestProcessor = new RequestProcessor (
                    Scheduler.class.getName(),
                    1,
                    false,
                    false);
        }        
        boolean different = this.source != source;
        
        if (different) {
            if (this.source != null) {
                final SourceCache cache = SourceAccessor.getINSTANCE().getCache(this.source);
                cache.unscheduleTasks(Scheduler.this.getClass());
                SourceAccessor.getINSTANCE().attachScheduler(this.source, ctrl, false);
            }
            this.source = source;
        }
        if (source == null) {
            return;
        }
        if (different) {
            SourceAccessor.getINSTANCE().attachScheduler(source, ctrl, true);
        }
        task = requestProcessor.create (new Runnable () {
            @Override
            public void run () {
                SourceCache cache = SourceAccessor.getINSTANCE ().getCache (source);                
                SourceAccessor.getINSTANCE ().setSchedulerEvent (source, Scheduler.this, event);
                //S ystem.out.println ("\nSchedule tasks (" + Scheduler.this + "):");
                LOG.fine("Scheduling tasks for :" + source + " and scheduler " + this);
                cache.scheduleTasks (Scheduler.this.getClass ());
            }
        });
        task.schedule (reparseDelay);
    }

    /**
     * Returns active {@link Source}.
     * The {@link Scheduler} subclasses should use this method to obtain the active
     * {@link Source} rather than caching the {@link Source} them self.
     * @return the {@link Source} currently handled by scheduler.
     * @since 1.69
     */
    @CheckForNull
    protected final synchronized Source getSource() {
        return this.source;
    }

    protected abstract SchedulerEvent createSchedulerEvent (SourceModificationEvent event);

    static {
        SchedulerAccessor.set (new Accessor ());
    }
    
    private static class Accessor extends SchedulerAccessor {

        @Override
        public SchedulerEvent createSchedulerEvent (Scheduler scheduler, SourceModificationEvent event) {
            return scheduler.createSchedulerEvent (event);
        }
    }
    
    /**
     * Allows to control the scheduler and schedule tasks. An instance of Control
     * is passed to the {@link SourceEnvironment#attachScheduler} method when the
     * Source is bound to a Scheduler.
     * <p/>
     * The client is NOT expected to create instances of this class;
     */
    private static final class Control implements SchedulerControl {
        private final Scheduler scheduler;

        /**
         *
         * @param scheduler
         */
        public Control(final Scheduler scheduler) {
            super();
            this.scheduler = scheduler;
        }

        public Scheduler getScheduler() {
            return scheduler;
        }

        @Override
        public void sourceChanged(Source newSource) {
            scheduler.schedule(newSource, new SchedulerEvent(newSource));
        }
    }
    
}

