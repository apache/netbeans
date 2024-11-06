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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport;
import org.netbeans.modules.nativeexecution.sps.impl.RequestPrivilegesTask.RequestPrivilegesTaskParams;
import org.netbeans.modules.nativeexecution.support.ObservableActionListener;
import org.netbeans.modules.nativeexecution.support.TasksCachedProcessor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public abstract class SPSCommonImpl implements SolarisPrivilegesSupport {

    private static final TasksCachedProcessor<ExecutionEnvironment, List<String>> cachedPrivilegesFetcher =
            new TasksCachedProcessor<>(new FetchPrivilegesTask(), false);
    private static final TasksCachedProcessor<RequestPrivilegesTaskParams, Boolean> cachedPrivilegesRequestor =
            new TasksCachedProcessor<>(new RequestPrivilegesTask(), true);
    private final ExecutionEnvironment execEnv;
    private volatile boolean cancelled = false;
    private final ConnectionListener connectionListener;

    protected SPSCommonImpl(final ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        connectionListener = new ConnectionListener() {

            @Override
            public void connected(ExecutionEnvironment env) {
            }

            @Override
            public void disconnected(ExecutionEnvironment env) {
                if (execEnv.equals(env)) {
                    invalidate();
                }
            }
        };

        ConnectionManager cm = ConnectionManager.getInstance();
        cm.addConnectionListener(WeakListeners.create(
                ConnectionListener.class, connectionListener, cm));
    }

    ExecutionEnvironment getExecEnv() {
        return execEnv;
    }

    abstract String getPID();

    @Override
    public abstract boolean requestPrivileges(
            Collection<String> requestedPrivileges,
            String root, char[] passwd) throws NotOwnerException, CancellationException, InterruptedException;

    @Override
    public void requestPrivileges(
            final Collection<String> requestedPrivileges,
            boolean askForPassword) throws NotOwnerException, CancellationException {

        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("requestExecutionPrivileges " + // NOI18N
                    "should never be called in AWT thread"); // NOI18N
        }

        if (askForPassword && cancelled) {
            return;
        }

        if (hasPrivileges(requestedPrivileges)) {
            return;
        }

        try {
            if (cachedPrivilegesRequestor.compute(
                    new RequestPrivilegesTaskParams(this, requestedPrivileges, askForPassword)).booleanValue() == true) {
                invalidateCache();
            } else {
                throw new NotOwnerException();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    // No synchronization, because cancelled is volatile
    public boolean isCanceled() {
        return cancelled;
    }

    @Override
    public boolean hasPrivileges(
            final Collection<String> privs) {
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            invalidate();
            return false;
        }
        List<String> real_privs = getExecutionPrivileges();
        if (real_privs == null) {
            return false;
        }

        boolean status = true;
        for (String priv : privs) {
            if (!status) {
                break;
            }
            status &= real_privs.contains(priv);
        }

        return status;
    }

    @Override
    public List<String> getExecutionPrivileges() {
        List<String> result = null;

        try {
            result = cachedPrivilegesFetcher.compute(execEnv);
        } catch (InterruptedException ex) {
        }

        return result;
    }

    /**
     * Provides action to request privileges.
     * The SAME action is returned for requesting same privileges.
     * @param requestedPrivileges
     * @param onPrivilegesGranted
     * @return
     */
    @Override
    public AsynchronousAction getRequestPrivilegesAction(
            final Collection<String> requestedPrivileges,
            final Runnable onPrivilegesGranted) {
        final RequestPrivilegesAction action =
                RequestPrivilegesAction.getInstance(this, requestedPrivileges);

        if (onPrivilegesGranted != null) {
            action.addObservableActionListener(new ObservableActionListener<Boolean>() {

                @Override
                public void actionStarted(Action source) {
                }

                @Override
                public void actionCompleted(Action source, Boolean result) {
                    if (result != null && result.booleanValue() == true) {
                        onPrivilegesGranted.run();
                    }
                }
            });
        }

        return action;
    }

    private void invalidateCache() {
        cachedPrivilegesFetcher.remove(execEnv);
    }

    @Override
    public void invalidate() {
        invalidateCache();
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SPSCommonImpl.class, key, params);
    }
}
