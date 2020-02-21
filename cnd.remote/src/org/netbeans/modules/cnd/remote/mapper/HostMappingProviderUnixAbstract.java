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
package org.netbeans.modules.cnd.remote.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 */
/*package*/ abstract class HostMappingProviderUnixAbstract implements HostMappingProvider {
    private static final Logger log = RemoteUtil.LOGGER;

    protected abstract String getShareCommand();

    protected abstract String fetchPath(String[] values);

    @Override
    public Map<String, String> findMappings(ExecutionEnvironment execEnv, ExecutionEnvironment otherExecEnv) {
        Map<String, String> mappings = new HashMap<>();
        String hostName = execEnv.isLocal() ? getLocalHostName() : execEnv.getHost();
        log.log(Level.FINE, "Find Mappings for {0}", execEnv);
        if (hostName != null) {
            ProcessUtils.ExitStatus exit = ProcessUtils.execute(execEnv, getShareCommand());
            if (exit.isOK()) { //NOI18N
                List<String> paths = parseOutput(execEnv, new StringReader(exit.getOutputString()));
                for (String path : paths) {
                    log.log(Level.FINE, "Path {0}", path);
                    assert path != null && path.length() > 0 && path.charAt(0) == '/';
                    String netPath = NET + hostName + path;
                    if (HostInfoProvider.fileExists(otherExecEnv, netPath)) {
                        if (execEnv.isLocal()) {
                            log.log(Level.FINE, "{0}->{1}", new Object[]{path, netPath});
                            mappings.put(path, netPath);
                        } else {
                            log.log(Level.FINE, "{0}->{1}", new Object[]{netPath, path});
                            mappings.put(netPath, path);
                        }
                    }
                    if (!mappings.containsKey(path) && execEnv.isLocal()) {
                        String host = getIP();
                        if (host != null && host.length()>0) {
                            log.log(Level.FINE, "IP={0}", host);
                            netPath = NET + host + path;
                            if (HostInfoProvider.fileExists(otherExecEnv, netPath)) {
                                mappings.put(path, netPath);
                                log.log(Level.FINE, "{0}->{1}", new Object[]{path, netPath});
                            }
                        }
                    }
                }
            }
        }
        if (execEnv.isRemote()) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                String userDir = hostInfo.getUserDir();
                if (RemotePathMap.isTheSame(execEnv, userDir, CndFileUtils.createLocalFile(userDir))) {
                    mappings.put(userDir, userDir);
                }            
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (CancellationException ex) {
                //don't report CancellationException
            } catch (InterruptedException ex) {
                //don't report InterruptedException
            }
        }
        return mappings;
    }

    private String getIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            RemoteUtil.LOGGER.log(Level.FINEST, "Exception when getting local host IP", ex); //NOI18N
            return null;
        }
    }

    private static final String NET = "/net/"; // NOI18N
    private static final Pattern pattern = Pattern.compile("\t+| +"); // NOI18N

    /**
     * This method parses lines like
     * -               /export1/sside   rw   "sside"
     * TODO: It assumes 2nd param is always path we want
     * @param outputReader
     * @return
     */
    private List<String> parseOutput(ExecutionEnvironment execEnv, Reader outputReader) {
        List<String> paths = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(outputReader);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String path = fetchPath(pattern.split(line));
                //if (path != null && HostInfoProvider.fileExists(execEnv, path)) {
                if (path != null) {
                    if (CndPathUtilities.isPathAbsolute(path)) {
                        FileObject fo = FileSystemProvider.getFileObject(execEnv, path);
                        if (fo != null && fo.isValid()) {
                            paths.add(path); // NOI18N
                        }
                    } else {
                        if (!"smb".equals(path)) { // NOI18N
                            log.fine("The command `" + getShareCommand() + "` listed non-absolute path: " + path); //NOI18N
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return paths;
    }

    private static String getLocalHostName() {
        String hostName = null;
        try {
            hostName = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal()).getHostname();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // don't report cancellation exception
        }
        return hostName;
    }
}
