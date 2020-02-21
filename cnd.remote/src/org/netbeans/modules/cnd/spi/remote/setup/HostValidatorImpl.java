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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
