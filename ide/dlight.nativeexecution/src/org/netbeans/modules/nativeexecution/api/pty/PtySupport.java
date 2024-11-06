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
package org.netbeans.modules.nativeexecution.api.pty;

import java.io.IOException;
import org.netbeans.modules.nativeexecution.ExProcessInfoProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.CpuFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.IOConnector;
import org.netbeans.modules.nativeexecution.pty.PtyAllocator;
import org.netbeans.modules.nativeexecution.pty.SttySupport;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 * An utility class for pty-related stuff...
 *
 * @author ak119685
 */
public final class PtySupport {

    private PtySupport() {
    }

    /**
     * This method returns a Pty that is currently associated with the process
     * (if any).
     *
     * @param process - process to get pty of
     * @return Pty that is currently associated with the process or {@code null}
     * if ptocess was started in non-pty mode.
     */
    public static String getTTY(NativeProcess process) {
        if (process instanceof ExProcessInfoProvider exProcessInfoProvider) {
            return exProcessInfoProvider.getTTY();
        }

        return null;
    }

    /**
     * Connects process' IO streams with the specified InputOutput.
     *
     * @param io - {@code InputOutput} to connect process' IO with
     * @param process - the process which should be connected with the io
     *
     * @return {@code true} if operation was successfull. {@code false} otherwise.
     */
    public static boolean connect(InputOutput io, NativeProcess process) {
        return IOConnector.getInstance().connect(io, process);
    }

    /**
     * Connects process' IO streams with the specified InputOutput.
     *
     * @param io {@link InputOutput} to connect process' IO with
     * @param process the process which should be connected with the io
     * @param postConnectRunnabel the task to be executed *after* the connection is established
     *
     * @return {@code true} if operation was successfull. {@code false} otherwise.
     */
    public static boolean connect(InputOutput io, NativeProcess process, Runnable postConnectRunnabel) {
        return IOConnector.getInstance().connect(io, process, postConnectRunnabel);
    }

    /**
     * Connects pty's IO streams (master side) with the specified {@link InputOutput}.
     * So that IO of the process that will do input/output to the specified pty'
     * slave will go to the specified {@link InputOutput}.
     *
     * @param io {@link InputOutput} to connect pty's IO with
     * @param pty the pty to connect InputOutput with
     *
     * @return {@code true} if operation was successfull. {@code false} otherwise.
     */
    public static boolean connect(InputOutput io, Pty pty) {
        return IOConnector.getInstance().connect(io, pty);
    }

    /**
     * Allocates a new 'unconnected' pty
     * @param env environmant in which a pty should be allocated
     * @return newly allocated pty or {@code null} if allocation failed
     * @throws java.io.IOException
     */
    public static Pty allocate(ExecutionEnvironment env) throws IOException {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IllegalStateException();
        }

        return PtyAllocator.getInstance().allocate(env);
    }

    public static void deallocate(Pty pty) throws IOException {
        pty.close();
    }

    /**
     * Disables echo in the tty (stty -echo)
     * @param env - environmant in which a tty is used
     * @param tty - tty name
     */
    public static void disableEcho(ExecutionEnvironment env, String tty) {
        SttySupport.apply(env, tty, "-echo"); //NOI18N
    }

    /**
     * Sets 'erase' key of a terminal to ^H.
     *
     * There are some bugs in Solaris (7009510, 7045666, 7164170) that make this
     * approach unreliable. So it is better not to use it.
     *
     * (it is recommended not to use stty utility for controlling any terminal
     * other than the one in which it is running in.)
     *
     * There is an option in pty utility - --set-erase-key that could be used to
     * achieve needed effect.
     *
     * @param exEnv
     * @param tty
     */
    @Deprecated
    public static void setBackspaceAsEraseChar(ExecutionEnvironment exEnv, String tty) {
        SttySupport.apply(exEnv, tty, "erase \\^H"); // NOI18N
    }

    public static boolean isSupportedFor(ExecutionEnvironment executionEnvironment) {
        if (!HostInfoUtils.isHostInfoAvailable(executionEnvironment)) {
            return false;
        }

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);

            switch (hostInfo.getOSFamily()) {
                case WINDOWS -> {
                    // for now pty mode only supported with Cygwin
                    Shell shell = WindowsSupport.getInstance().getActiveShell();

                    if (shell == null) {
                        return false;
                    }

                    return shell.type == Shell.ShellType.CYGWIN || shell.type == Shell.ShellType.WSL;
                }
                case MACOSX -> {
                    return true;
                }
                case LINUX -> {
                    return hostInfo.getCpuFamily().equals(CpuFamily.X86)
                            || hostInfo.getCpuFamily().equals(CpuFamily.SPARC)
                            || (hostInfo.getCpuFamily().equals(CpuFamily.ARM) && Boolean.getBoolean("cnd.pty.arm.support"))
                            || hostInfo.getCpuFamily().equals(CpuFamily.AARCH64);
                }
                case SUNOS -> {
                    return true;
                }
                case FREEBSD -> {
                    return false;
                }
                default -> {
                    return false;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // don't report CancellationException
        }

        return false;
    }
}
