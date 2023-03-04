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

package org.netbeans.modules.j2ee.core.api.support.progress;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.core.utilities.ProgressPanel;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * A class providing support for running event thread (in the event dispatching
 * thread) and background (in a background thread) actions. Multiple
 * actions can be posted at the same time, switching between event thread and
 * background ones as needed. The actions are run sequentially -- one at most one action
 * may be running at any moment in time. A progress panel is displayed for background actions.
 *
 * <p>A typical use case is running an background action with a progress dialog.
 * For that just create an {@link #BackgroundAction} and send it to the {@link #invoke} method.</p>
 *
 * <p>A more complex use case is mixing actions: first you need to run an background
 * action, then an event thread one (but in certain cases only) and then another
 * background one, showing and hiding the progress panel as necessary.</p>
 *
 * @author Andrei Badea
 */
public final class ProgressSupport {

    private static final Logger LOGGER = Logger.getLogger(ProgressSupport.class.getName()); // NOI18N

    private ProgressSupport() {
    }

    /**
     * Invokes the actions without allowing them to be cancelled.
     *
     * @param  actions the actions to invoke; never null.
     */
    public static void invoke(Collection<? extends Action> actions) {
        invoke(actions, false);
    }

    /**
     * Invokes the actions while possibly allowing them to be cancelled and returns
     * the cancellation status.
     *
     * @param  actions the action to invoke; never null.
     * @param  cancellable true whether to allow cancellable actions to be cancelled,
     *         false otherwise.
     * @return true if the actions were not cancelled, false otherwise.
     */
    public static boolean invoke(Collection<? extends Action> actions, boolean cancellable) {
        Parameters.notNull("actions", actions); // NOI18N
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method must be called in the event thread."); // NOI18N
        }

        return new ActionInvoker(new ArrayList<Action>(actions), cancellable).invoke();
    }

    /**
     * The class that actually invokes the actions.
     */
    private static final class ActionInvoker implements ActionListener {

        private final RequestProcessor rp = new RequestProcessor("ProgressSupport", 1); // NOI18N
        private final List<Action> actions;
        private final boolean cancellable;

        private volatile Context actionContext;
        private AtomicInteger nextActionIndex = new AtomicInteger();
        private volatile Action currentAction;
        private volatile boolean cancelled;

        public ActionInvoker(List<Action> actions, boolean cancellable) {
            this.actions = actions;
            this.cancellable = cancellable;
        }

        /**
         * Returns true if the invocation was not cancelled, false otherwise.
         */
        public boolean invoke() {
            assert SwingUtilities.isEventDispatchThread();

            final ProgressPanel progressPanel = new ProgressPanel();
            progressPanel.setCancelVisible(cancellable);
            progressPanel.addCancelActionListener(this);

            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(null);
            JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
            progressHandle.start();
            progressHandle.switchToIndeterminate();

            actionContext = new Context(progressPanel, progressHandle);

            // Contains the exception, if any, thrown by an action invocation
            // in either EDT or the RP thread
            final AtomicReference<Throwable> exceptionRef = new AtomicReference<Throwable>();

            // The RequestProcessor task for background actions
            RequestProcessor.Task task = rp.create(new Runnable() {
                public void run() {
                    try {
                        invokeNextActionsOfSameKind();
                    } catch (Throwable t) {
                        exceptionRef.set(t);
                    } finally {
                        // We are done running background actions, so we must close the progress panel
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                progressPanel.close();
                            }
                        });
                    }
                }
            });

            try {
                // True if we are running event thread actions in this round, false otherwise.
                boolean runInEDT = true;

                // Every round of the loop invokes a bunch of actions. The first
                // round invokes event thread ones, stopping at the first background one.
                // The second invokes background ones, stopping at the first event thread one.
                // The third invokes event thread ones, etc.
                // This avoids hiding/showing the progress panel after/before each
                // background action.
                while (nextActionIndex.get() < actions.size() && !cancelled) {
                    if (runInEDT) {
                        try {
                            invokeNextActionsOfSameKind();
                        } catch (Throwable t) {
                            exceptionRef.set(t);
                        }
                    } else {
                        // The equivalent of invokeNextActionsOfSameKind() above, but in the background
                        // and under the progress panel.

                        // Schedule the RP task for background actions. The task
                        // also sets exceptionRef if an exception occured.
                        task.schedule(0);

                        // Open the progress panel. It will be closed at the end of the task for
                        // background actions. Therefore the call will block and will return when
                        // the RP task's run() method returns.
                        progressPanel.open(progressComponent);

                        // The RP might be still running (e.g. preempted by the AWT thread
                        // thread just after the SW.invokeLater()).
                        task.waitFinished();
                    }

                    Throwable exception = exceptionRef.get();
                    if (exception != null) {
                        if (exception instanceof RuntimeException) {
                            throw (RuntimeException)exception;
                        } else {
                            RuntimeException re = new RuntimeException(exception.getMessage());
                            re.initCause(exception);
                            throw re;
                        }
                    }

                    runInEDT = !runInEDT;
                }
            } finally {
                progressHandle.finish();
            }

            return !cancelled;
        }

        /**
         * Invokes the next actions of the same kind (all event thread or all background),
         * starting with nextActionIndex, while skipping disabled actions. That is,
         * when called in the EDT it will run all enabled event thread actions,
         * stopping at the first background one. When called in a RP thread, it
         * will run all enabled background actions, stopping as the first event thread one.
         */
        private void invokeNextActionsOfSameKind() {
            boolean isBackground = !SwingUtilities.isEventDispatchThread();

            while (!cancelled) {
                int currentActionIndex = nextActionIndex.get();
                if (currentActionIndex >= actions.size()) {
                    break;
                }

                currentAction = actions.get(currentActionIndex);

                // Skip the action if disabled.
                if (!currentAction.isEnabled()) {
                    nextActionIndex.incrementAndGet();
                    LOGGER.log(Level.FINE, "Skipping " + currentAction);
                    continue;
                }

                // The current action is not of the current kind, finish.
                if (currentAction.isBackground() != isBackground) {
                    break;
                }

                LOGGER.log(Level.FINE, "Running " + currentAction);

                // Only enable/disable the cancel button for background actions.
                if (isBackground) {
                    final boolean cancelEnabled = currentAction.isCancellable();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            actionContext.getPanel().setCancelEnabled(cancelEnabled);
                        }
                    });
                }

                currentAction.run(actionContext);

                nextActionIndex.incrementAndGet();
            }

        }

        /**
         * Invoked when the Cancel button is pressed in the progress dialog.
         */
        public void actionPerformed(ActionEvent event) {
            // Just in case the user managed to click Cancel twice.
            if (cancelled) {
                return;
            }

            Action action = currentAction;

            // All actions could have been invoked by now.
            if (action == null) {
                return;
            }

            // There is no guarantee that the current action is a background one or that it
            // implements Cancellable (maybe the action before it did and the user clicked Cancel
            // just before it finished). If it doesn't we can't do better than
            // just ignore the Cancel request.
            if (!action.isEnabled() || !action.isBackground() || !action.isCancellable()) {
                return;
            }

            cancelled = action.cancel();
            if (cancelled) {
                actionContext.getPanel().setCancelEnabled(false);
            }
        }
    }

    /**
     * Encapsulates the "context" the action is it run under. Currently contains
     * methods for controlling the progress bar in the progress dialog
     * for background actions.
     */
    public static final class Context {

        private final ProgressPanel panel;
        private final ProgressHandle handle;

        private Context(ProgressPanel panel, ProgressHandle handle) {
            this.panel = panel;
            this.handle = handle;
        }

        /**
         * Switches the progress bar to a determinate one.
         *
         * @param workunits a definite number of complete units of work out of the total
         */
        public void switchToDeterminate(int workunits) {
            handle.switchToDeterminate(workunits);
        }

        /**
         *
         *
         * @param message
         */
        public void progress(final String message) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    panel.setText(message);
                }
            });
            handle.progress(message);
        }

        public void progress(int workunit) {
            handle.progress(workunit);
        }

        ProgressPanel getPanel() {
            return panel;
        }
    }

    /**
     * Describes an action. See also {@link EventThreadAction} and
     * {@link BackgroundAction}.
     */
    public abstract static class Action {

        /**
         * Constructs a new action, by default {@link #isEnabled enabled}.
         */
        public Action() {
        }

        /**
         * Returns true if the action is a background one.
         *
         * <p>This method is invoked in an unspecified thread.</p>
         *
         * @return true if the action should be run in the background,
         *         false otherwise.
         */
        protected abstract boolean isBackground();

        /**
         * Returns true if the action is enabled (should be run). The default
         * implementation of this method returns true.
         *
         * <p>This method is useful when having e.g. an event thread action between
         * two background actions, and the event thread action's enabled status depends on the
         * result of the first background action. If this result is such that the event
         * thread action should not run, the event thread action could implement
         * it's run() method as a no-op. But this would cause the progress dialog
         * to blink for a short time (being hidden after the first background action
         * and shown before the second one). This method helps remove that blinking.</p>
         *
         * <p>This method will be invoked in an unspecified thread.</p>
         *
         * @return true if the action is enabled, false otherwise.
         */
        protected boolean isEnabled() {
            return true;
        }

        /**
         * This method is invoked when the action should be run. It will
         * be invoked in the event dispatching thread or an unspecified
         * background thread depending on the result value of {@link #isBackground}.
         *
         * @param  actionContext the context in which this action is run.
         */
        protected abstract void run(Context actionContext);

        /**
         * Returns true if the action is cancellable. The default implementation
         * of this method returns false.
         *
         * @return true if the action is enabled, false otherwise.
         */
        protected boolean isCancellable() {
            return false;
        }

        /**
         * This method is invoked when the action should be cancelled. It is
         * invoked in an unspecified thread.
         *
         * @return true if the action could be cancelled successfully, false
         *         otherwise.
         */
        protected boolean cancel() {
            return true;
        }
    }

    /**
     * Describes an event thread action, that is, one that should be run
     * in the event dispatching thread.
     */
    public abstract static class EventThreadAction extends Action {

        private final boolean cancellable;

        public EventThreadAction() {
            this(false);
        }

        public EventThreadAction(boolean cancellable) {
            this.cancellable = cancellable;
        }

        public final boolean isBackground() {
            return false;
        }

        @Override
        protected final boolean isCancellable() {
            return cancellable;
        }
    }

    /**
     * Describes a background action, that is, one that should be run
     * in a background thread under a progress dialog.
     */
    public abstract static class BackgroundAction extends Action {

        private final boolean cancellable;

        public BackgroundAction() {
            this(false);
        }

        public BackgroundAction(boolean cancellable) {
            this.cancellable = cancellable;
        }

        public final boolean isBackground() {
            return true;
        }

        @Override
        protected final boolean isCancellable() {
            return cancellable;
        }
    }
}
