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

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport.NotOwnerException;
import org.netbeans.modules.nativeexecution.spi.support.GrantPrivilegesProvider;
import org.netbeans.modules.nativeexecution.spi.support.GrantPrivilegesProviderFactory;
import org.netbeans.modules.nativeexecution.sps.impl.RequestPrivilegesTask.RequestPrivilegesTaskParams;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
//import org.netbeans.module.nativeexecution.ui.GrantPrivilegesDialog;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class RequestPrivilegesTask implements Computable<RequestPrivilegesTaskParams, Boolean> {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private WeakReference<GrantPrivilegesProvider> dialogRef = null;

    public Boolean compute(RequestPrivilegesTaskParams args) throws InterruptedException {
        final RequestPrivilegesTaskPerformer performer = new RequestPrivilegesTaskPerformer(args);
        final Future<Boolean> result = NativeTaskExecutorService.submit(performer, "RequestPrivilegesTask"); // NOI18N

        final ProgressHandle ph = ProgressHandle.createHandle(
                loc("TaskPrivilegesSupport_Progress_RequestPrivileges"), new Cancellable() { // NOI18N

            public boolean cancel() {
                return result.cancel(true);
            }
        });

        ph.start();

        try {
            return result.get();
        } catch (ExecutionException ex) {
            log.fine("ExecutionException in RequestPrivilegesTask : " + ex.toString()); // NOI18N
        } catch (CancellationException ex) {
            // skip. Will return false
        } finally {
            ph.finish();
        }

        return Boolean.FALSE;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SPSCommonImpl.class, key, params);
    }

    public static class RequestPrivilegesTaskParams {

        final Collection<String> requestedPrivileges;
        final boolean askForPassword;
        private final String privilegesString;
        final SPSCommonImpl support;

        public RequestPrivilegesTaskParams(
                SPSCommonImpl support,
                Collection<String> requestedPrivileges,
                boolean askForPassword) {
            this.support = support;
            this.requestedPrivileges = requestedPrivileges;
            this.askForPassword = askForPassword;

            StringBuffer sb = new StringBuffer();

            for (String priv : requestedPrivileges) {
                sb.append(priv).append(","); // NOI18N
            }

            privilegesString = sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RequestPrivilegesTaskParams)) {
                throw new IllegalArgumentException();
            }

            RequestPrivilegesTaskParams o = (RequestPrivilegesTaskParams) obj;

            return o.askForPassword == askForPassword &&
                    o.support == support &&
                    o.requestedPrivileges.containsAll(requestedPrivileges) &&
                    requestedPrivileges.containsAll(o.requestedPrivileges);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + (this.askForPassword ? 1 : 0);
            hash = 79 * hash + (this.support != null ? this.support.hashCode() : 0);
            return hash;
        }
    }

    private class RequestPrivilegesTaskPerformer implements Callable<Boolean> {

        private final RequestPrivilegesTaskParams args;

        public RequestPrivilegesTaskPerformer(RequestPrivilegesTaskParams args) {
            this.args = args;
        }

        public Boolean call() throws Exception {
            // An attempt to grant privileges using pfexec
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(args.support.getExecEnv());
            npb.setExecutable("/bin/pfexec").setArguments("/bin/ppriv", "-s", // NOI18N
                    "I+" + args.privilegesString, args.support.getPID()); // NOI18N

            ProcessUtils.ExitStatus res = ProcessUtils.execute(npb);

            if (res.isOK()) {
                // pfexec succeeded ...
                return Boolean.TRUE;
            }

            if (!args.askForPassword) {
                return Boolean.FALSE;
            }

            if (dialogRef == null || dialogRef.get() == null) {
                GrantPrivilegesProviderFactory factory = 
                        Lookup.getDefault().lookup(GrantPrivilegesProviderFactory.class);
                if (factory == null) {
                    return Boolean.FALSE;
                }
                dialogRef = new WeakReference<>(
                        factory.create());
            }

            GrantPrivilegesProvider provider = dialogRef.get();

            while (true) {
                if (provider.askPassword()) {
                    try {
                        char[] clearPassword = provider.getPassword();
                        args.support.requestPrivileges(args.requestedPrivileges, provider.getUser(), clearPassword);
                        Arrays.fill(clearPassword, (char) 0);
                        provider.clearPassword();
                        return Boolean.TRUE;
                    } catch (NotOwnerException ex) {
                        // wrong password or not enough privileges...
                        // Continue with password requests...
                    }
                } else {
                    throw new CancellationException();
                }
            }
        }
    }
}
