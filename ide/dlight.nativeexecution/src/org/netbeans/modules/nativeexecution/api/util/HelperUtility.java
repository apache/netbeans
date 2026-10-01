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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.support.Encrypter;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MiscUtils;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author ak119685
 */
public class HelperUtility {

    protected static final java.util.logging.Logger log = Logger.getInstance();
    private final HashMap<ExecutionEnvironment, String> cache = new HashMap<>();
    private final String pattern;
    protected final String codeNameBase;

    public HelperUtility(String searchPattern) {
        this("org.netbeans.modules.dlight.nativeexecution", searchPattern); // NOI18N
    }

    public HelperUtility(String codeNameBase, String searchPattern) {
        this.codeNameBase = codeNameBase;
        pattern = searchPattern;
    }

    /**
     *
     * @param env
     * @return the ready-to-use remote path for the utility
     * @throws IOException
     */
    public final String getPath(final ExecutionEnvironment env) throws IOException {
        HostInfo hinfo;
        try {
            hinfo = HostInfoUtils.getHostInfo(env);
        } catch (CancellationException ex) {
            return null;
        }
        return getPath(env, hinfo);
    }

    public final String getPath(final ExecutionEnvironment env, final HostInfo hinfo) throws IOException {
        //one of the stacks when we come here is (see bz#239059):
        /*
        at org.netbeans.modules.nativeexecution.api.util.HelperUtility.getPath(HelperUtility.java:106)
        at org.netbeans.modules.nativeexecution.support.hostinfo.impl.UnixHostInfoProvider.getRemoteUserEnvironment(UnixHostInfoProvider.java:272)
        at org.netbeans.modules.nativeexecution.support.hostinfo.impl.UnixHostInfoProvider.getHostInfo(UnixHostInfoProvider.java:115)
        at org.netbeans.modules.nativeexecution.support.hostinfo.FetchHostInfoTask.compute(FetchHostInfoTask.java:64)
        at org.netbeans.modules.nativeexecution.api.util.HostInfoUtils.getHostInfo(HostInfoUtils.java:235)
        at org.netbeans.modules.nativeexecution.api.util.ConnectionManager.initiateConnection(ConnectionManager.java:407)
        Also the code below and comment (see 235998:3c427e2d4185) says that we can be here erlier then connection is
        */
        if (!ConnectionManager.getInstance().isConnectedTo(env, false)) {
            log.log(Level.FINE, env.toString() + " is not connected", //NOI18N
                    new IllegalStateException(env.toString() + " is not connected")); // NOI18N
        }

        String result;
        HostInfo localHostInfo;
        
        try {
            localHostInfo = env.isLocal() ? hinfo
                    : HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
        } catch (CancellationException ex) {
            throw new IOException(ex);
        }

        synchronized (cache) {
            result = cache.get(env);

            if (result == null) {
                try {
                    File localFile = getLocalFileFromSysProp(hinfo, env.isLocal());

                    if (localFile == null) {
                        localFile = getLocalFile(hinfo);
                    }

                    if (localFile == null) {
                        localFile = getLocalFile(env);
                    }

                    final String fileName = localFile.getName();
                    // Construct destination: {tmpbase}/{hash}/{name}
                    File localTmpBase = localHostInfo.getTempDirFile();
                    // hash - a string unique to pair: 
                    //        local file location and env
                    String key = localFile.getAbsolutePath().concat(env.getDisplayName());
                    String hash = Integer.toString(key.hashCode()).replace('-', '0');
                    File safeLocalDir = new File(localTmpBase, hash);
                    safeLocalDir.mkdirs();

                    File safeLocalFile = new File(safeLocalDir, fileName);
                    copyFile(localFile, safeLocalFile);

                    if (env.isLocal()) {
                        result = safeLocalFile.getAbsolutePath();
                    } else {
                        Logger.assertNonUiThread("Potentially long method " + getClass().getName() + ".getPath() is invoked in AWT thread"); // NOI18N

                        final String remoteDir = hinfo.getTempDir() + '/' + hash;
                        final String remoteFile = remoteDir + '/' + fileName;
                        // Helper utility could be needed at the early stages
                        // Should not use NPB here
                        ConnectionManagerAccessor cmAccess = ConnectionManagerAccessor.getDefault();
                        ChannelSftp channel = (ChannelSftp) cmAccess.openAndAcquireChannel(env, "sftp", true); // NOI18N
                        if (channel == null) {
                            return null;
                        }
                        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("UploadHelperUtility", localFile.getAbsolutePath()); // NOI18N
                        long remoteSize = -1;
                        try {
                            channel.connect();
                            // md5sum checking is not used for HelperUtilities
                            // it is assumed that comparing sizes is enough in
                            // this case
                            long localSize = safeLocalFile.length();
                            try {
                                SftpATTRS rstat = channel.stat(remoteFile);
                                remoteSize = rstat.getSize();
                            } catch (SftpException ex) {
                                // No such file ...
                            }

                            if (remoteSize >= 0 && localSize != remoteSize) {
                                // Remote file exists, but it has different size
                                // Remove it first (otherwise channel.put() will
                                // fail if this file is opened for reading.
                                // (Any better idea?)
                                channel.rm(remoteFile);
                                remoteSize = -1;
                            }
                            if (remoteSize < 0) {
                                try {
                                    channel.lstat(remoteDir);
                                } catch (SftpException ex) {
                                    channel.mkdir(remoteDir);
                                }

                                channel.put(safeLocalFile.getAbsolutePath(), remoteFile);
                                channel.chmod(0700, remoteFile);
                            }
                            result = remoteFile;
                        } catch (SftpException ex) {
                            log.log(Level.WARNING, "Failed to upload a file to a remote host: {0}", ex.toString()); // NOI18N
                            if (remoteSize >= 0) {
                                log.log(Level.WARNING, "File {0} exists, but cannot be updated. Used by other process?", remoteFile); // NOI18N
                            } else {
                                log.log(Level.WARNING, "File {0} doesn't exist, and cannot be uploaded. Do you have enough privileges? Is there enough space?", remoteFile); // NOI18N
                            }
                            log.log(Level.WARNING, "You could try to use -J-Dcnd.tmpbase=<other base location> to re-define default one."); // NOI18N
                        } finally {
                            RemoteStatistics.stopChannelActivity(activityID);
                            cmAccess.closeAndReleaseChannel(env, channel);
                        }
                    }
                    cache.put(env, result);
                } catch (MissingResourceException ex) {
                    return null;
                } catch (JSchException ex) {
                    if (MiscUtils.isJSCHTooLongException(ex)) {
                        MiscUtils.showJSCHTooLongNotification(env.getDisplayName());
                        log.log(Level.WARNING, "Handshaking process with a remote host failed. See IDE notifications. {0}", ex.toString());
                    } else {
                        throw new IOException(ex);
                    }
                } catch (ParseException | InterruptedException ex) {
                    if (ex.getCause() instanceof IOException ioException) {
                        throw ioException;
                    }
                    throw new IOException(ex);
                }
            }
        }

        return result;
    }

    protected File getLocalFile(final ExecutionEnvironment env, final HostInfo hinfo) throws MissingResourceException {
        return null;
    }

    protected File getLocalFile(final HostInfo hinfo) throws MissingResourceException {
        return null;
    }

    protected File getLocalFile(final ExecutionEnvironment env)
            throws ParseException, MissingResourceException {

        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        MacroExpander expander = MacroExpanderFactory.getExpander(env);
        String path = expander.expandPredefinedMacros(pattern);

        File file = fl.locate(path, codeNameBase, false);

        if (file == null || !file.exists()) {
            throw new MissingResourceException(path, null, null); //NOI18N
        }

        return file;
    }

    private static void copyFile(final File srcFile, final File dstFile) throws IOException {
        if (dstFile.exists()) {
            boolean wasRemoved = dstFile.delete();
            if (!wasRemoved) {
                long srcCRC = Encrypter.getFileChecksum(srcFile.getAbsolutePath());
                long dstCRC = Encrypter.getFileChecksum(dstFile.getAbsolutePath());
                if (srcCRC == dstCRC) {
                    // OK - file is busy, but it is just the same - just return
                    return;
                }
                log.log(Level.INFO, "Failed to copy {0} to {1}", new Object[]{srcFile, dstFile}); // NOI18N
            }
        }

        dstFile.getParentFile().mkdirs();
        dstFile.createNewFile();

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(srcFile).getChannel();
            destination = new FileOutputStream(dstFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            dstFile.setExecutable(true);
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private File getLocalFileFromSysProp(HostInfo hostInfo, boolean local) {
        String osname = hostInfo.getOS().getFamily().cname();
        if (local) {
            Shell activeShell = WindowsSupport.getInstance().getActiveShell();
            if (activeShell != null && activeShell.type == Shell.ShellType.WSL) {
                osname = "Linux";
            }
        }
        String platform = hostInfo.getCpuFamily().name().toLowerCase();
        String bitness = hostInfo.getOS().getBitness() == HostInfo.Bitness._64 ? "_64" : ""; // NOI18N
        StringBuilder propName = new StringBuilder(getClass().getSimpleName());
        propName.append('.').append(osname).append('-').append(platform).append(bitness).append(".exec"); // NOI18N
        String prop = System.getProperty(propName.toString());
        if (prop != null) {
            File res = new File(prop);
            if (res.canRead()) {
                log.log(Level.WARNING, "Using an executable specified by {0} system property for {1}: {2}", // NOI18N
                        new Object[]{propName, getClass().getSimpleName(), res.getAbsolutePath()});
                return res;
            }
        }
        return null;
    }
}
