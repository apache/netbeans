/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.SftpIOException;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchParams;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchSupport;
import org.netbeans.modules.nativeexecution.support.hostinfo.FetchHostInfoTask;
import org.openide.util.RequestProcessor;

/**
 * Utility class that provides information about particular host.
 */
public final class HostInfoUtils {

    /**
     * String constant that can be used to identify a localhost.
     */
    public static final String LOCALHOST = "localhost"; // NOI18N
    private static final Future<List<String>> myAddresses;
    private static final ConcurrentHashMap<ExecutionEnvironment, HostInfo> cache =
            new ConcurrentHashMap<>();

    static {
        myAddresses = RequestProcessor.getDefault().submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                List<String> result = new ArrayList<>();
                NetworkInterface iface;
                try {
                    for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                            ifaces.hasMoreElements();) {
                        iface = ifaces.nextElement();
                        for (Enumeration<InetAddress> ips = iface.getInetAddresses();
                                ips.hasMoreElements();) {
                            result.add((ips.nextElement()).getHostAddress());
                        }
                    }
                } catch (Throwable th) {
                    Logger.getInstance().log(Level.INFO, "Exception while getting localhost IP", th); // NOI18N
                }
                return result;
            }
        });
    }

    private HostInfoUtils() {
    }

    /**
     * Utility method that dumps HostInfo to specified stream
     * @param hostinfo hostinfo that should be dumped
     * @param stream stream to dump to
     */
    public static void dumpInfo(HostInfo hostinfo, PrintStream stream) {
        stream.println("------------"); // NOI18N
        if (hostinfo == null) {
            stream.println("HostInfo is NULL"); // NOI18N
        } else {
            stream.println("Hostname      : " + hostinfo.getHostname()); // NOI18N
            stream.println("OS Family     : " + hostinfo.getOSFamily()); // NOI18N
            stream.println("OS            : " + hostinfo.getOS().getName()); // NOI18N
            stream.println("OS Version    : " + hostinfo.getOS().getVersion()); // NOI18N
            stream.println("OS Bitness    : " + hostinfo.getOS().getBitness()); // NOI18N
            stream.println("CPU Family    : " + hostinfo.getCpuFamily()); // NOI18N
            stream.println("CPU #         : " + hostinfo.getCpuNum()); // NOI18N
            stream.println("login shell   : " + hostinfo.getLoginShell()); // NOI18N
            stream.println("shell         : " + hostinfo.getShell()); // NOI18N
            stream.println("tmpdir to use : " + hostinfo.getTempDir()); // NOI18N
            stream.println("tmpdir (file) to use : " + hostinfo.getTempDirFile().toString()); // NOI18N
        }
        stream.println("------------"); // NOI18N
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt>.
     * If execEnv refers to a remote host that is not connected yet, a
     * <tt>ConnectException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     *
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     *
     * @throws ConnectException if host, identified by this execution
     * environment is not connected or operation was terminated.
     *
     * @throws InterruptedException if the thread was interrupted.
     *
     * @throws IOException if the process could not be created
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname)
            throws ConnectException, IOException, InterruptedException {

        if (execEnv.isLocal()) {
            return new File(fname).exists();
        } else {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                throw new ConnectException();
            }

            try {
                SftpSupport.getInstance(execEnv).lstat(fname).get();
                return true;
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof SftpIOException) {
                    SftpIOException e = (SftpIOException) ex.getCause();
                    if (SftpIOException.SSH_FX_NO_SUCH_FILE == e.getId()) {
                        return false;
                    }
                }
                if (ex.getCause() instanceof IOException) {
                    throw (IOException) ex.getCause();
                }
                throw new IOException(ex);
            }
        }
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt> and is a directory
     * (or is a link to dirctory).
     * If execEnv refers to a remote host that is not connected yet, a
     * <tt>ConnectException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     *
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     *
     * @throws ConnectException if host, identified by this execution
     * environment is not connected or operation was terminated.
     *
     * @throws InterruptedException if the thread was interrupted.
     *
     * @throws IOException if the process could not be created
     */
    public static boolean directoryExists(final ExecutionEnvironment execEnv,
            final String fname)
            throws ConnectException, IOException, InterruptedException {

        if (execEnv.isLocal()) {
            return new File(fname).exists();
        } else {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                throw new ConnectException();
            }

            try {
                FileInfoProvider.StatInfo statInfo = SftpSupport.getInstance(execEnv).stat(fname).get();
                return statInfo.isDirectory();
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof SftpIOException) {
                    SftpIOException e = (SftpIOException) ex.getCause();
                    if (SftpIOException.SSH_FX_NO_SUCH_FILE == e.getId()) {
                        return false;
                    }
                }
                if (ex.getCause() instanceof IOException) {
                    throw (IOException) ex.getCause();
                }
                throw new IOException(ex);
            }
        }
    }

    public static String searchFile(ExecutionEnvironment execEnv,
            List<String> searchPaths, String file, boolean searchInUserPaths) {
        String result = null;

        try {
            result = new FileSearchSupport().searchFile(new FileSearchParams(execEnv,
                    searchPaths, file, searchInUserPaths));
        } catch (InterruptedException ex) {
        }

        return result;
    }

    /**
     * Returns true if and only if <tt>host</tt> identifies a localhost.
     *
     * @param host host identification string. Either hostname or IP address.
     * @return true if and only if <tt>host</tt> identifies a localhost.
     */
    public static boolean isLocalhost(String host) {
        if (LOCALHOST.equals(host)) {
            return true;
        }

        boolean result = false;

        try {
            result = myAddresses.get().contains(InetAddress.getByName(host).getHostAddress());
        } catch (Throwable th) {
        }

        return result;
    }

    /**
     * Tests whether host info has been already fetched for the particular
     * execution environment.
     *
     * @param execEnv environment to perform test against
     * @return <tt>true</tt> if info is available and getHostInfo() could be
     * called without a risk to be blocked for a significant time.
     * <tt>false</tt> otherwise.
     */
    public static boolean isHostInfoAvailable(final ExecutionEnvironment execEnv) {
        return cache.containsKey(execEnv);
    }

    /**
     * Returns <tt>HostInfo</tt> with information about the host identified
     * by <tt>execEnv</tt>. Invocation of this method may block current thread
     * for rather significant amount of time. It can also initiate UI-user
     * interaction. This happens when execEnv represents remote host and no
     * active connection to that host is available.
     * An attempt to establish new connection will be performed. This may initiate
     * password prompt.
     *
     * One should avoid to call this method from within AWT thread without prior 
     * call to isHostInfoAvailable().
     *
     * @param execEnv execution environment to get information about.
     * It should never be null, otherwise IllegalArgumentException is thrown.
     * @return information about the host represented by execEnv.
     * The return value is never null; in the case it is impossible to get HostInfo,
     * IOException is thrown instead.
     * @throws IOException
     * @throws CancellationException 
     * @see #isHostInfoAvailable(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
     */
    public static HostInfo getHostInfo(final ExecutionEnvironment execEnv) throws IOException, CancellationException {
        return getHostInfo(execEnv, false);
    }

    /**
     * @param connecting is true if called from ConnectionManager that is is process of initiating connection right now
     */
    /*package*/ static HostInfo getHostInfo(final ExecutionEnvironment execEnv, boolean connecting) throws IOException, CancellationException {
        if (execEnv == null) {
            throw new IllegalArgumentException("ExecutionEnvironment should not be null"); //NOI18N
        }

        if (!isHostInfoAvailable(execEnv)) {
            Logger.assertNonUiThread("Don't call getHostInfo() from the UI thread while info is not known. " + // NOI18N
                    "Use quick isHostInfoAvailable() to detect whether info is available or not and go out of EDT if not"); // NOI18N
        }

        synchronized (HostInfoUtils.class) {

            HostInfo result = cache.get(execEnv);

            if (result == null) {
                try {
                    // Must be sure that host is connected.
                    // It will throw an exception in case if fails to connect
                    
                    // There was a very small time frame when host was in fact connected,
                    // but host info was not yet put into cache (issue #252922)
                    // Due to the issue #252922 we make ConnectionManager.isConnected() return FALSE while in this period.
                    // But here we need to check "real" connection status
                    if (!ConnectionManager.getInstance().isConnectedTo(execEnv, !connecting)) {
                        ConnectionManager.getInstance().connectTo(execEnv);
                        // connect will recursively call getHostInfo...
                        // so result will be available already.
                        result = cache.get(execEnv);

                        if (result != null) {
                            return result;
                        }
                    }

                    result = new FetchHostInfoTask().compute(execEnv);
                    if (result == null) {
                        throw new IOException("Error getting host info for " + execEnv); // NOI18N
                    }
                    cache.put(execEnv, result);
                } catch (InterruptedException ex) {
                    throw new CancellationException("getHostInfo(" + execEnv.getDisplayName() + ") cancelled."); // NOI18N
                }
            }
            return result;
        }

    }

    public static void updateHostInfo(ExecutionEnvironment execEnv) throws IOException, InterruptedException {
        HostInfo result = new FetchHostInfoTask().compute(execEnv);
        if (result != null) {
            cache.put(execEnv, result);
        }
    }

    /**
     * For testing purposes only!
     */
    protected static void resetHostsData() {
        cache.clear();
    }
}
