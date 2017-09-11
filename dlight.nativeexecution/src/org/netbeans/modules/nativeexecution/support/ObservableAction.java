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

        for (ObservableActionListener l : ll) {
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
