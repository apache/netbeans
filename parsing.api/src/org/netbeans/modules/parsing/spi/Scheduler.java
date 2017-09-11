/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
     * @param sources       A collection of {@link Source}s.
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

