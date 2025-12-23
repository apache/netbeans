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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Alexander Simon
 */
public class HelperLibraryUtility {

    private final HashMap<ExecutionEnvironment, List<String>> cache = new HashMap<>();
    private final String pattern;
    private final String codeNameBase;

    public HelperLibraryUtility(String searchPattern) {
        this("org.netbeans.modules.dlight.nativeexecution", searchPattern); // NOI18N
    }

    public HelperLibraryUtility(String codeNameBase, String searchPattern) {
        this.codeNameBase = codeNameBase;
        pattern = searchPattern;
    }

    /**
     *
     * @param env
     * @return the ready-to-use remote path for the utility
     * @throws IOException
     */
    public final List<String> getPaths(final ExecutionEnvironment env) throws IOException {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IllegalStateException(env.toString() + " is not connected"); // NOI18N
        }

        List<String> result = null;

        synchronized (cache) {
            result = cache.get(env);

            if (result == null) {
                try {
                    HostInfo hinfo = HostInfoUtils.getHostInfo(env);
                    List<String> localFile = getLocalFileLocationFor(env);

                    if (env.isLocal()) {
                        result = localFile;
                    } else {
                        result = new ArrayList<>();
                        for(String lf : localFile) {
                            Logger.assertNonUiThread("Potentially long method " + getClass().getName() + ".getPath() is invoked in AWT thread"); // NOI18N
                            final File file = new File(lf);
                            final String fileName = file.getName();
                            final String fileFolder = file.getParentFile().getName();
                            final String remoteFile = hinfo.getTempDir() + '/' + fileFolder +'/' +fileName;

                            Future<CommonTasksSupport.UploadStatus> uploadTask = CommonTasksSupport.uploadFile(lf, env, remoteFile, 0755, true);
                            CommonTasksSupport.UploadStatus status = uploadTask.get();
                            if (!status.isOK()) {
                                throw new IOException("Unable to upload " + fileName + " to " + env.getDisplayName() + ':' + remoteFile // NOI18N
                                        + " rc=" + status.getExitCode() + ' ' + status.getError()); // NOI18N
                            }
                            result.add(remoteFile);
                        }
                    }
                    cache.put(env, result);
                } catch (IOException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
        }

        return result;
    }

    public static String getLDPathEnvName(ExecutionEnvironment execEnv) {
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            switch(hostInfo.getOSFamily()) {
                case MACOSX:
                   return "DYLD_LIBRARY_PATH"; // NOI18N
                case LINUX:
                case FREEBSD:
                   return "LD_LIBRARY_PATH"; // NOI18N
                case WINDOWS:
                   return "PATH"; // NOI18N
            }
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }
        return null;
    }

    public final String getLDPaths(final ExecutionEnvironment env) throws IOException {
        List<String> paths = getPaths(env);
        StringBuilder buf = new StringBuilder();
        for(String p : paths) {
            if (buf.length()>0) {
                buf.append(':'); // NOI18N
            }
            p = p.replace('\\', '/'); // NOI18N
            int i = p.lastIndexOf('/'); // NOI18N
            String parent;
            if (i >= 0) {
                parent = p.substring(0,i);
            } else {
                parent = p;
            }
            buf.append(parent);
        }
        return buf.toString();
    }

    public static String getLDPreloadEnvName(ExecutionEnvironment execEnv) {
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            switch(hostInfo.getOSFamily()) {
                case MACOSX:
                   return "DYLD_INSERT_LIBRARIES"; // NOI18N
                case LINUX:
                case FREEBSD:
                   return "LD_PRELOAD"; // NOI18N
                case WINDOWS:
                   return "LD_PRELOAD"; // NOI18N
            }
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }
        return null;
    }
    
    public final String getLibraryName(final ExecutionEnvironment env) throws IOException {
        List<String> paths = getPaths(env);
        String name = null;
        for(String p : paths) {
            final File file = new File(p);
            if (name == null) {
                name = file.getName();
            } else {
                assert name.equals(file.getName());
            }
        }
        return name;
    }
    
    public static boolean isMac(ExecutionEnvironment execEnv) {
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
            switch(hostInfo.getOSFamily()) {
                case MACOSX:
                    return true;
            }
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }
        return false;
    }
    
    private List<String> getLocalFileLocationFor(final ExecutionEnvironment env)
            throws ParseException, MissingResourceException {

        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        MacroExpander expander = MacroExpanderFactory.getExpander(env);
        String aPattern = pattern.replace("${_isa}", "${_my_isa}"); // NOI18N
        String path = expander.expandPredefinedMacros(aPattern);
        int indexOf = path.indexOf("${_my_isa}"); // NOI18N
        List<String> paths = new ArrayList<>();
        if (indexOf > 0) {
            paths.add(path.replace("${_my_isa}", "")); // NOI18N
            paths.add(path.replace("${_my_isa}", "_64")); // NOI18N
        } else {
            paths.add(path);
        }
        List<String> res = new ArrayList<>();
        MissingResourceException ex = null;
        for(String p : paths) {
            File file = fl.locate(p, codeNameBase, false);
            if (file == null || !file.exists()) {
                ex = new MissingResourceException(p, null, null);
                continue;
            }
            res.add(file.getAbsolutePath());
        }
        if (res.isEmpty() && ex != null) {
            throw ex;
        }
        return res;
    }
}
