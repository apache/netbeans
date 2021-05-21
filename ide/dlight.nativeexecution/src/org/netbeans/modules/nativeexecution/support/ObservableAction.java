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
package org.netbeans.modules.nativeexecution.support;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;

/**
 * Extension of the {@link AbstractAction} implementation that allows to attach
 * listeners to get notifications on task start / completion events.
 *
 * @param <T> result type of the action
 */
public abstract class ObservableAction<T>
        extends AbstractAction
        implements AsynchronousAction {

    private final List<ObservableActionListener<T>> listeners =
            Collections.synchronizedList(new ArrayList<ObservableActionListener<T>>());
    private volatile Future<T> taskFutureResult = null;
    private final Object lock = ObservableAction.class.getName() + "Lock"; // NOI18N

    /**
     * Constructor
     * @param name name of the action that is passed to super constructor.
     */
    public ObservableAction(String name) {
        super(name);
    }

    /**
     * Adds an <tt>ObservableAction</tt> listener. Listener should be specified
     * with the same type parameter as <tt>ObservableAction</tt> does.
     *
     * It is guarantied that the same listener will not be added more than once.
     *
     * @param listener a <tt>ObservableActionListener</tt> object
     */
    public final void addObservableActionListener(
            ObservableActionListener<T> listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes an <tt>ObservableAction</tt> listener. Removing not previously
     * added listener has no effect.
     *
     * @param listener a <tt>ObservableActionListener</tt> object
     */
    public final void removeObservableActionListener(
            ObservableActionListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Must be implemented in descendant class to perform an action.
     * Normally it should not be invoked directly.
     *
     * @return result of <tt>ObservableAction</tt> execution.
     *
     * @see #invoke()
     * @see #actionPerformed(java.awt.event.ActionEvent)
     */
    abstract protected T performAction();

    /**
     * Invoked when an action occurs. <p>
     * Action is submitted for an execution in a separate thread and
     * current thread is not blocked. If this method is invoked again before
     * previously submitted task completion, it returns immideately without new
     * task submition.
     *
     * @param e event that causes the action. May be <tt>NULL</tt>
     */
    public final void actionPerformed(final ActionEvent e) {
        // Will not start the task if it is already started.
        synchronized (lock) {

            if (e == null) {
            }

            if (taskFutureResult == null || taskFutureResult.isDone()) {
                // Will execute task unsynchronously ... Post the task
                taskFutureResult = NativeTaskExecutorService.submit(new Callable<T>() {

                    public T call() throws Exception {
                        fireStarted();
                        T result = performAction();
                        fireCompleted(result);

                        return result;
                    }
                }, "Performing observable action " + getValue(Action.NAME)); // NOI18N
            }
        }
    }

    /**
     * Performs synchronous execution of the action. <p>
     * If the action is executed at the moment of <tt>invoke</tt> call,
     * the current thread is blocked until the action is completed and
     * a result of <b>that</b> invocation in returned. Otherwise new task is
     * submitted and result of it's execution is returned.
     *
     * @return a result of ths action execution.
     */
    public final void invoke() {
        synchronized (lock) {
            actionPerformed(null);

            try {
                taskFutureResult.get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
        }

    }

    private void fireStarted() {
        List<ObservableActionListener<T>> ll =
                new ArrayList<>(listeners);

        for (ObservableActionListener<T> l : ll) {
            l.actionStarted(this);
        }
    }

    private void fireCompleted(T result) {
        List<ObservableActionListener<T>> ll =
                new ArrayList<>(listeners);

        for (ObservableActionListener<T> l : ll) {
            l.actionCompleted(this, result);
        }
    }
}
