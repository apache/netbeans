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
package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * An implementation of SetupProvider that nandles RFS related binaries
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.api.remote.SetupProvider.class)
public class RfsSetupProvider implements SetupProvider {

    public static final String POSTFIX_64 = "_64"; // NOI18N
    private final Map<String, File> binarySetupMap;
    private static final String CONTROLLER = "rfs_controller"; // NOI18N
    private static final String PRELOAD = "rfs_preload.so"; // NOI18N

    public RfsSetupProvider() {
        String[] dirs = new String[]{
            "SunOS-x86" // NOI18N
            , "SunOS-x86_64" // NOI18N
            , "Linux-x86" // NOI18N
            , "Linux-x86_64" // NOI18N
            , "Linux-sparc_64" // NOI18N
            , "SunOS-sparc" // NOI18N
            , "SunOS-sparc_64" // NOI18N
        };
        binarySetupMap = new HashMap<>();
        for (String dir : dirs) {
            binarySetupMap.put(dir + "/" + PRELOAD, InstalledFileLocator.getDefault().locate("bin/" + dir + "/" + PRELOAD, "org.netbeans.modules.cnd.remote", false)); // NOI18N
            binarySetupMap.put(dir + "/" + CONTROLLER, InstalledFileLocator.getDefault().locate("bin/" + dir + "/" + CONTROLLER, "org.netbeans.modules.cnd.remote", false)); // NOI18N
        }
    }

    @Override
    public Map<String, File> getBinaryFiles(ExecutionEnvironment env) {
        Map<String, File> result = new LinkedHashMap<>();
        Boolean applicable = isApplicable(env);
        if (applicable == null) {
            RemoteUtil.LOGGER.log(Level.WARNING, "Can not determine whether RFS is applicable for {0}", env.getDisplayName());
            return result;
        }
        if (!applicable.booleanValue()) {
            RemoteUtil.LOGGER.log(Level.WARNING, "RFS not applicable for {0}", env.getDisplayName());
            return result;
        }
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            String osName = getOsName(env);
            String dir32 = osName + '/';
            String dir64 = getOsName(env) + POSTFIX_64 + '/';
            for (Map.Entry<String, File> entry : binarySetupMap.entrySet()) {
                boolean add = false;
                if (entry.getKey().startsWith(dir32)) {
                    add = true;
                } else if (entry.getKey().startsWith(dir64)) {
                    add = hostInfo.getOS().getBitness() == HostInfo.Bitness._64;
                }
                if (add) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (InterruptedIOException ex) {
            // don't report
        } catch (CancellationException ex) {
            // don't report
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    @Override
    public void failed(Collection<File> files, StringBuilder describeProblem) {
        describeProblem.append(NbBundle.getMessage(RfsSetupProvider.class, "ErrorUploadingBinaries"));
    }

    public static String getPreloadName(ExecutionEnvironment execEnv) {
        return PRELOAD;
    }

    /** Never returns null, throws instead */
    public static String getControllerPath(ExecutionEnvironment execEnv) throws ParseException, CancellationException, IOException {
        String result = getLibDir(execEnv);
        HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
        if (hostInfo.getOS().getBitness() == HostInfo.Bitness._64) {
            result += POSTFIX_64;
        }
        result += '/' + CONTROLLER; // NOI18N;
        return result;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // three state
    public static Boolean isApplicable(ExecutionEnvironment env) {
        if (env == null) {
            throw new NullPointerException();
        }

        HostInfo.OSFamily osFamily = null;
        HostInfo.CpuFamily cpuFamily = null;
        String osVersion = null;

        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                osFamily = hostInfo.getOSFamily();
                cpuFamily = hostInfo.getCpuFamily();
                osVersion = hostInfo.getOS().getVersion();
            } catch (IOException ex) {
                RemoteUtil.LOGGER.log(Level.WARNING, "Exception when getting host info:", ex);
            } catch (CancellationException ex) {
                // don't log CancellationException
            }
        }

        if (osFamily == null || cpuFamily == null || osVersion == null) { // in fact either all or none is null
            RemoteServerRecord record = RemoteServerList.getInstance().get(env, false);
            if (record != null) {
                osFamily = record.getOsFamily();
                cpuFamily = record.getCpuFamily();
                osVersion = record.getOsVersion();
            }
        }

        if (osFamily == null || cpuFamily == null) {
            RemoteUtil.LOGGER.log(Level.WARNING, "RFS: can not determine host OS and CPU for {0}", env.getDisplayName());
            return null;
        }

        switch (osFamily) {
            case LINUX:
                return (cpuFamily == HostInfo.CpuFamily.X86 || cpuFamily == HostInfo.CpuFamily.SPARC) ?
                        Boolean.TRUE : Boolean.FALSE;
            case SUNOS:
                //BZ #189231 Smart secure copy does not work on (remote) Solaris 8
                //Disable Automatic copying on Solaris 8 as it is not supported platform
                //NFS file sharing can be used in this case
                if (osVersion == null || getSolarisOSVersionNumber(osVersion) <= 8) {
                    return Boolean.FALSE;
                }
                return (cpuFamily == HostInfo.CpuFamily.X86 || cpuFamily == HostInfo.CpuFamily.SPARC) ? Boolean.TRUE : Boolean.FALSE;
            case MACOSX:
            case FREEBSD:
            case WINDOWS:
            case UNKNOWN:
            default:
                return Boolean.FALSE;
        }
    }

    private static int getSolarisOSVersionNumber(String versionString) {
        String prefixToStrip = "Oracle "; // NOI18N
        if (versionString.startsWith(prefixToStrip)) {
            versionString = versionString.substring(prefixToStrip.length());
        }

        Pattern p = Pattern.compile("[a-zA-Z]+[ ]([\\d]+).*"); // NOI18N
        Matcher m = p.matcher(versionString);
        String result = "-1"; // NOI18N
        if (m.matches()) {
            result = m.group(1);
        }

        int version = -1;
        try {
            version = Integer.parseInt(result);
        } catch (NumberFormatException e) {
            return -1;
        }
        return version;
    }

    public static String getLdLibraryPath(ExecutionEnvironment execEnv) throws ParseException {
        String libDir = getLibDir(execEnv);
        return libDir + ':' + libDir + POSTFIX_64; // NOI18N
    }

    private static String getLibDir(ExecutionEnvironment execEnv) throws ParseException {
        String libDir = HostInfoProvider.getLibDir(execEnv); //NB: should contain trailing '/'
        String osname = getOsName(execEnv); // NOI18N
        return libDir + '/' + osname;
    }

    private static String getOsName(ExecutionEnvironment execEnv) throws ParseException {
        //NB: should contain trailing '/'
        MacroExpander mef = MacroExpanderFactory.getExpander(execEnv);
        String osname = mef.expandPredefinedMacros("${osname}-${platform}"); // NOI18N
        return osname;
    }
}
