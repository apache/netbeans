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
package org.netbeans.modules.cnd.spi.remote.setup;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.Writer;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import static org.netbeans.modules.cnd.remote.server.RemoteServerList.TRACE_SETUP;
import static org.netbeans.modules.cnd.remote.server.RemoteServerList.TRACE_SETUP_PREFIX;
import org.netbeans.modules.cnd.remote.server.StopWatch;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
class HostValidatorImpl implements HostValidator {

    private final ToolsCacheManager cacheManager;
    private volatile Runnable runOnFinish;
    private volatile Thread compilerSearchThread;
    private volatile boolean compilerSearchCancelled;
    private volatile CompilerSetManager csm;
    
    public HostValidatorImpl(ToolsCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Runnable getRunOnFinish() {
        return runOnFinish;
    }

    // TODO: ToolsCacheManager FIXUP
    public ToolsCacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    public void cancelToolSearch() {
        compilerSearchCancelled = true;
        CompilerSetManager csmCopy = this.csm;
        if (csmCopy != null) {
            csmCopy.cancel();
        }
        Thread th = this.compilerSearchThread;
        if (th != null) {
            th.interrupt();
        }
    }

    @Override
    public boolean validate(final ExecutionEnvironment env, boolean searchForTools, final PrintWriter writer) {
        compilerSearchThread = null;
        compilerSearchCancelled = false;
        final RemoteServerRecord record = (RemoteServerRecord) ServerList.get(env);
        record.setNeedsValidationOnConnect(false);
        try {
            return validateImpl(record, searchForTools, writer);
        } finally {
            record.setNeedsValidationOnConnect(true);
        }
    }

    private boolean validateImpl(final RemoteServerRecord record, boolean searchForTools, final PrintWriter writer) {
        final ExecutionEnvironment env = record.getExecutionEnvironment();
        boolean result = false;
        final boolean alreadyOnline = record.isOnline();
        if (alreadyOnline) {
            String message = NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgAlreadyConnected1");
            message = String.format(message, env.toString());
            writer.printf("%s", message); // NOI18N
        } else {
            record.resetOfflineState(); // this is a do-over
        }
        // move expensive operation out of EDT

        if (!alreadyOnline) {
            writer.print(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgConnectingTo",
                    env.getHost()));
        }
        try {
//            if (password != null && password.length > 0) {
//                PasswordManager.getInstance().storePassword(env, password, rememberPassword);
//            }
            StopWatch sw = StopWatch.createAndStart(TRACE_SETUP, TRACE_SETUP_PREFIX, env, "connecting"); //NOI18N
            ConnectionManager.getInstance().connectTo(env);
            sw.stop();
        } catch (InterruptedIOException | CancellationException ex) {
            return false; // don't report InterruptedIOException and CancellationException
        } catch (IOException ex) {
            writer.print("\n" + RemoteUtil.getMessage(ex)); //NOI18N
            return false;
        }
        if (!alreadyOnline) {
            writer.print(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgDone") + '\n');
            writer.print(NbBundle.getMessage(getClass(), "CSM_ConfHost") + '\n');
            StopWatch sw = StopWatch.createAndStart(TRACE_SETUP, TRACE_SETUP_PREFIX, env, "record.init"); //NOI18N
            record.init(null);
            sw.stop();
        }
        if (record.isOnline()) {
            Writer reporter = new Writer() {

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    final String value = new String(cbuf, off, len);
                    writer.print(value);
                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public void close() throws IOException {
                }
            };          
            if (searchForTools && ! compilerSearchCancelled) {
                compilerSearchThread = Thread.currentThread();
                try {
                    csm = cacheManager.getCompilerSetManagerCopy(env, false);
                    StopWatch sw = StopWatch.createAndStart(TRACE_SETUP, TRACE_SETUP_PREFIX, env, "CompilerSetManager.initialize"); //NOI18N
                    csm.initialize(false, false, reporter);
                    sw.stop();
                    if (record.hasProblems()) {
                        try {
                            reporter.append(record.getProblems());
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    final CompilerSetManager csmCopy = csm;
                    runOnFinish = new Runnable() {
                        @Override
                        public void run() {
                            if (!compilerSearchCancelled) {
                                csmCopy.finishInitialization();
                            }
                        }
                    };
                } finally {
                    csm = null;
                    compilerSearchThread = null;
                }
                result = true;
            } else {
                writer.write(NbBundle.getMessage(getClass(), "HostValidator.Toolchain.Search.Skipped") + '\n');
                result = true;                
            }
        } else {
            writer.write(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.ErrConn")
                    + '\n' + record.getReason()); //NOI18N
        }
        if (alreadyOnline) {
            writer.write('\n' + NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgAlreadyConnected2"));
        } else {
            RemoteSyncFactoryDefaultProvider rsfdp = Lookup.getDefault().lookup(RemoteSyncFactoryDefaultProvider.class);
            if (rsfdp != null) {
                record.setSyncFactory(rsfdp.getDefaultFactory(env));
            }            
        }
        return result;
    }
}
