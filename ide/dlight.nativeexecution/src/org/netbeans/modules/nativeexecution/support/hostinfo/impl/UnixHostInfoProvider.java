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
package org.netbeans.modules.nativeexecution.support.hostinfo.impl;

import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.JschSupport;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelStreams;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.RemoteStatistics;
import org.netbeans.modules.nativeexecution.pty.NbStartUtility;
import org.netbeans.modules.nativeexecution.support.EnvReader;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MiscUtils;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider.class, position = 100)
public class UnixHostInfoProvider implements HostInfoProvider {

    private static final String TMPBASE = System.getProperty("cnd.tmpbase", null); // NOI18N
    private static final String PATH_VAR = "PATH"; // NOI18N
    private static final String PATH_TO_PREPEND = System.getProperty("hostinfo.prepend.path", null); // NOI18N
    private static final String ERROR_MESSAGE_PREFIX = "Error: TMPDIRBASE is not writable: "; // NOI18N
    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final File hostinfoScript;

    static {
        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        hostinfoScript = fl.locate("bin/nativeexecution/hostinfo.sh", "org.netbeans.modules.dlight.nativeexecution", false); // NOI18N

        if (hostinfoScript == null) {
            log.severe("Unable to find hostinfo.sh script!"); // NOI18N
        }
    }

    @Override
    public HostInfo getHostInfo(ExecutionEnvironment execEnv) throws IOException, InterruptedException {
        if (hostinfoScript == null) {
            return null;
        }

        boolean isLocal = execEnv.isLocal();

        if (isLocal && Utilities.isWindows()) {
            return null;
        }

        final Properties info = execEnv.isLocal()
                ? getLocalHostInfo()
                : getRemoteHostInfo(execEnv);

        final Map<String, String> environment = new HashMap<>();

        HostInfo result = HostInfoFactory.newHostInfo(execEnv, info, environment);

        if (execEnv.isLocal()) {
            getLocalUserEnvironment(result, environment);
        } else {
            getRemoteUserEnvironment(execEnv, result, environment);
        }

        // Add /bin:/usr/bin
        String path = PATH_TO_PREPEND;

        if (path != null && !path.isEmpty()) {
            if (environment.containsKey(PATH_VAR)) {
                path += ":" + environment.get(PATH_VAR); // NOI18N
            }

            environment.put(PATH_VAR, path); // NOI18N
        }

        return result;
    }

    private Properties getLocalHostInfo() throws IOException {
        Properties hostInfo = new Properties();

        ProcessBuilder pb = new ProcessBuilder("/bin/sh", // NOI18N
                hostinfoScript.getAbsolutePath());

        String tmpDirBase = null;
        if (TMPBASE != null) {
            if (pathIsOK(TMPBASE, false)) {
                tmpDirBase = TMPBASE;
            } else {
                log.log(Level.WARNING, "Ignoring cnd.tmpbase property [{0}] as it contains illegal characters", TMPBASE); // NOI18N
            }
        }

        if (tmpDirBase == null) {
            File tmpDirFile = new File(System.getProperty("java.io.tmpdir")); // NOI18N
            tmpDirBase = tmpDirFile.getCanonicalPath();
        }


        pb.environment().put("TMPBASE", tmpDirBase); // NOI18N
        pb.environment().put("NB_KEY", HostInfoFactory.getNBKey()); // NOI18N
        ProcessUtils.ExitStatus res = ProcessUtils.execute(pb);

        // In case of some error goes to stderr, waitFor() will not exit
        // until error stream is read/closed.
        // So this case sould be handled.

        // We safely can do this in the same thread (in this exact case)

        List<String> errorLines = res.getErrorLines();
        int result = res.exitCode;

        for (String errLine : errorLines) {
            log.log(Level.WARNING, "UnixHostInfoProvider: {0}", errLine); // NOI18N
            if (errLine.startsWith(ERROR_MESSAGE_PREFIX)) {
                String title = NbBundle.getMessage(UnixHostInfoProvider.class, "TITLE_PermissionDenied");
                String shortMsg = NbBundle.getMessage(UnixHostInfoProvider.class, "SHORTMSG_PermissionDenied", tmpDirBase, "localhost");
                String msg = NbBundle.getMessage(UnixHostInfoProvider.class, "MSG_PermissionDenied", tmpDirBase, "localhost");
                MiscUtils.showNotification(title, shortMsg, msg);
            }
        }

        if (result != 0) {
            throw new IOException(hostinfoScript + " rc == " + result); // NOI18N
        }

        fillProperties(hostInfo, res.getOutputLines());

        return hostInfo;
    }

    private Properties getRemoteHostInfo(final ExecutionEnvironment execEnv) throws IOException, InterruptedException {
        Properties hostInfo = new Properties();
        ChannelStreams sh_channels = null;

        try {
            log.log(Level.FINEST, "Getting remote host info for {0}", execEnv); // NOI18N
            sh_channels = JschSupport.startCommand(execEnv, "/bin/sh -s", null); // NOI18N

            long localStartTime = System.currentTimeMillis();

            OutputStream out = sh_channels.in;
            final InputStream err = sh_channels.err;
            final InputStream in = sh_channels.out;

            // echannel.setEnv() didn't work, so writing this directly
            out.write(("NB_KEY=" + HostInfoFactory.getNBKey() + '\n').getBytes()); // NOI18N
            if (TMPBASE != null) {
                if (pathIsOK(TMPBASE, true)) {
                    out.write(("TMPBASE=" + TMPBASE + '\n').getBytes()); // NOI18N
                } else {
                    log.log(Level.WARNING, "Ignoring cnd.tmpbase property [{0}] as it contains illegal characters", TMPBASE); // NOI18N
                }
            }
            out.flush();

            BufferedReader scriptReader = new BufferedReader(new FileReader(hostinfoScript));
            String scriptLine = scriptReader.readLine();

            while (scriptLine != null) {
                out.write((scriptLine + '\n').getBytes());
                out.flush();
                scriptLine = scriptReader.readLine();
            }

            scriptReader.close();

            NativeTaskExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
                        String errLine;
                        while ((errLine = errReader.readLine()) != null) {
                            log.log(Level.WARNING, "UnixHostInfoProvider: {0}", errLine); // NOI18N
                            if (errLine.startsWith(ERROR_MESSAGE_PREFIX)) {
                                errLine = errLine.replace(ERROR_MESSAGE_PREFIX, "");
                                String title = NbBundle.getMessage(UnixHostInfoProvider.class, "TITLE_PermissionDenied");
                                String shortMsg = NbBundle.getMessage(UnixHostInfoProvider.class, "SHORTMSG_PermissionDenied", errLine, execEnv);
                                String msg = NbBundle.getMessage(UnixHostInfoProvider.class, "MSG_PermissionDenied", errLine, execEnv);
                                MiscUtils.showNotification(title, shortMsg, msg);
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }, "reading hostInfo script error"); //NOPI18N // NOI18N

            fillProperties(hostInfo, readProcessStream(in, execEnv.isRemote()));

            long localEndTime = System.currentTimeMillis();

            hostInfo.put("LOCALTIME", Long.valueOf((localStartTime + localEndTime) / 2)); // NOI18N
        } catch (JSchException ex) {
            throw new IOException("Exception while receiving HostInfo for " + execEnv.toString() + ": " + ex); // NOI18N
        } finally {
            if (sh_channels != null) {
                if (sh_channels.channel != null) {
                    try {
                        ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(execEnv, sh_channels.channel);
                    } catch (JSchException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        return hostInfo;
    }

    private void fillProperties(Properties hostInfo, List<String> lines) {
        for(String s : lines) {
            String[] data = s.split("=", 2); // NOI18N
            if (data.length == 2) {
                hostInfo.put(data[0], data[1]);
            }
        }
    }

    private void getRemoteUserEnvironment(ExecutionEnvironment execEnv, HostInfo hostInfo, Map<String, String> environmentToFill) throws InterruptedException {
        // If NbStartUtility is available for target host will invoke it for
        // dumping environment to a file ...
        // 
        // The only thing - we cannot use builders at this point, so
        // need to do everything here... 

        String nbstart = null;

        try {
            nbstart = NbStartUtility.getInstance().getPath(execEnv, hostInfo);
        } catch (IOException ex) {
            log.log(Level.WARNING, "Failed to get remote path of NbStartUtility", ex); // NOI18N
            Exceptions.printStackTrace(ex);
        }

        String envPath = hostInfo.getEnvironmentFile();

        ChannelStreams login_shell_channels = null;

        RemoteStatistics.ActivityID activityID = null;
        try {
            login_shell_channels = JschSupport.startLoginShellSession(execEnv);
            activityID = RemoteStatistics.startChannelActivity("UnixHostInfoProvider", execEnv.getDisplayName()); // NOI18N
            if (nbstart != null && envPath != null) {
                // dumping environment to file, later we'll restore it for each newly created remote process
                login_shell_channels.in.write((nbstart + " --dumpenv " + envPath + "\n").getBytes()); // NOI18N
            }
            // printing evnironment to stdout to fill host info map
            login_shell_channels.in.write(("/usr/bin/env || /bin/env\n").getBytes()); // NOI18N
            login_shell_channels.in.flush();
            login_shell_channels.in.close();

            EnvReader reader = new EnvReader(login_shell_channels.out, true);
            environmentToFill.putAll(reader.call());
        } catch (Exception ex) {
            InterruptedException iex = toInterruptedException(ex);
            if (iex != null) {
                throw iex;
            }
            log.log(Level.WARNING, "Failed to get getRemoteUserEnvironment for " + execEnv.getDisplayName(), ex); // NOI18N
        } finally {
            RemoteStatistics.stopChannelActivity(activityID);
            if (login_shell_channels != null) {
                if (login_shell_channels.channel != null) {
                    try {
                        ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(execEnv, login_shell_channels.channel);
                    } catch (JSchException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    InterruptedException toInterruptedException(Exception ex) {
        if (ex instanceof InterruptedException) {
            return (InterruptedException) ex;
        } else if (ex.getCause() instanceof InterruptedException) {
            return (InterruptedException) ex.getCause();
        }
        InterruptedIOException iioe = null;
        if (ex instanceof InterruptedIOException) {
            iioe = (InterruptedIOException) ex;
        } else if (ex.getCause() instanceof InterruptedIOException) {
            iioe = (InterruptedIOException) ex.getCause();
        }
        if (iioe != null) {
            InterruptedException wrapper = new InterruptedException(ex.getMessage());
            wrapper.initCause(iioe);
            return wrapper;
        }
        return null;
    }
    
    private void getLocalUserEnvironment(HostInfo hostInfo, Map<String, String> environmentToFill) {
        environmentToFill.putAll(System.getenv());
    }

    private boolean pathIsOK(String path, boolean remote) {
        for (char c : path.toCharArray()) {
            if (c >= '0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'z') {
                continue;
            }
            if (c >= 'A' && c <= 'Z') {
                continue;
            }
            if (c == '_' || c == '=') {
                continue;
            }
            if (c == '\\' || c == '/') {
                continue;
            }
            if (c == ':' && !remote && Utilities.isWindows()) {
                continue;
            }

            return false;
        }

        return true;
    }

    private static List<String> readProcessStream(final InputStream stream, boolean remoteStream) throws IOException {
        if (stream == null) {
            return Collections.<String>emptyList();
        }
        final List<String> result = new LinkedList<>();
        final BufferedReader br = ProcessUtils.getReader(stream, remoteStream);

        try {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return result;
    }
}
