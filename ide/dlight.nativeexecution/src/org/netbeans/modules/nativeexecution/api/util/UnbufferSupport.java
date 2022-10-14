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
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.CpuFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/*
 * Used to unbuffer application's output in case OutputWindow is used.
 *
 */
public class UnbufferSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final boolean UNBUFFER_DISABLED = Boolean.getBoolean("execution.no_unbuffer"); // NOI18N
    private static final HashMap<ExecutionEnvironment, String> cache =
            new HashMap<>();

    private UnbufferSupport() {
    }

    public static void initUnbuffer(final ExecutionEnvironment execEnv, final MacroMap env) throws IOException {
        if (UNBUFFER_DISABLED) {
            return;
        }

        HostInfo hinfo;
        try {
            hinfo = HostInfoUtils.getHostInfo(execEnv);
        } catch (CancellationException ex) {
            // TODO:CancellationException error processing
            return;
        }

        boolean isWindows = false;

        switch (hinfo.getOSFamily()) {
            case MACOSX:
            case FREEBSD: // No unbuffer on FreeBSD
                // No unbuffer on MacOS - see IZ179172
                return;
            case LINUX:
                if (!hinfo.getCpuFamily().equals(CpuFamily.X86) && 
                        !hinfo.getCpuFamily().equals(CpuFamily.SPARC) &&
                        !hinfo.getCpuFamily().equals(CpuFamily.AARCH64)) {
                    // Unbuffer is available for x86 only
                    // and now for sparc and aarch64 linux
                    return;
                }
                break;
            case WINDOWS:
                isWindows = true;
                break;
        }

        final MacroExpander macroExpander = MacroExpanderFactory.getExpander(execEnv);
        // Setup LD_PRELOAD to load unbuffer library...

        String unbufferPath = null; // NOI18N
        String unbufferPath_64 = null; // NOI18N        
        String unbufferLib = null; // NOI18N

        try {
            unbufferPath = macroExpander.expandPredefinedMacros(
                    "bin/nativeexecution/$osname-$platform" + (isWindows ? "${_isa}" : "")); // NOI18N
            unbufferPath_64 = macroExpander.expandPredefinedMacros(
                    "bin/nativeexecution/$osname-$platform" + "${_isa}"); // NOI18N
            unbufferLib = macroExpander.expandPredefinedMacros(
                    "unbuffer.$soext"); // NOI18N
        } catch (ParseException ex) {
        }

        if (unbufferLib != null && unbufferPath != null) {
            InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
            File file = fl.locate(unbufferPath + "/" + unbufferLib, "org.netbeans.modules.dlight.nativeexecution", false); // NOI18N
            File file_64 = fl.locate(unbufferPath_64 + "/" + unbufferLib, "org.netbeans.modules.dlight.nativeexecution", false); // NOI18N

            log.log(Level.FINE, "Look for unbuffer library here: {0}/{1}", new Object[]{unbufferPath, unbufferLib}); // NOI18N
            log.log(Level.FINE, "Look for unbuffer library here: {0}/{1}", new Object[]{unbufferPath_64, unbufferLib}); // NOI18N

            if ((file != null && file.exists()) || 
                    (file_64 != null && file_64.exists())) {
                if (execEnv.isRemote()) {
                    String remotePath = null;

                    synchronized (cache) {
                        remotePath = cache.get(execEnv);

                        if (remotePath == null) {
                            remotePath = hinfo.getTempDir() + "/" + unbufferPath; // NOI18N
                            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                            npb.setExecutable("/bin/mkdir").setArguments("-p", remotePath, remotePath + "_64"); // NOI18N
                            ProcessUtils.execute(npb);
                            try {
                                String remoteLib_32 = remotePath + "/" + unbufferLib; // NOI18N
                                String remoteLib_64 = remotePath + "_64/" + unbufferLib; // NOI18N
                                Future<UploadStatus> copyTask;
                                if (file != null && file.exists()) {
                                    String fullLocalPath = file.getParentFile().getAbsolutePath(); // NOI18N                                
                                    copyTask = CommonTasksSupport.uploadFile(fullLocalPath + "/" + unbufferLib, execEnv, remoteLib_32, 0755, true); // NOI18N
                                    copyTask.get(); // is it OK not to check upload exit code?
                                }
                                if (file_64 != null && file_64.exists()) {//we have 64 bit version only (sparc-Linux)
                                    String fullLocalPath_64 = file_64.getParentFile().getAbsolutePath(); // NOI18N                                
                                    copyTask = CommonTasksSupport.uploadFile(fullLocalPath_64 + "/" + unbufferLib, execEnv, remoteLib_64, 0755, true); // NOI18N
                                    copyTask.get(); // is it OK not to check upload exit code?
                                }
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            cache.put(execEnv, remotePath);
                        }
                    }

                    unbufferPath = remotePath;
                } else {
                    if (file != null) {
                        unbufferPath = new File(file.getParent()).getAbsolutePath();
                    } else {
                        unbufferPath = new File(file_64.getParent()).getAbsolutePath();
                    }
                }

                String ldPreloadEnv;
                String ldLibraryPathEnv;

                if (isWindows) {
                    ldLibraryPathEnv = "PATH"; // NOI18N
                    ldPreloadEnv = "LD_PRELOAD"; // NOI18N
                } else {
                    ldLibraryPathEnv = "LD_LIBRARY_PATH"; // NOI18N
                    ldPreloadEnv = "LD_PRELOAD"; // NOI18N
                }

                String ldPreload = env.get(ldPreloadEnv);

                if (isWindows) {
                    // TODO: FIXME (?) For Mac and Windows just put unbuffer
                    // with path to it to LD_PRELOAD/DYLD_INSERT_LIBRARIES
                    // Reason: no luck to make it work using PATH ;(
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ";")) + // NOI18N
                            new File(unbufferPath, unbufferLib).getAbsolutePath(); // NOI18N

                    ldPreload = WindowsSupport.getInstance().convertToAllShellPaths(ldPreload);

                    if (ldPreload == null) {
                        // i.e. cannot convert [cygpath not found, for example]
                        // will not set LD_PRELOAD
                        return;
                    }
                } else {
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ":")) + // NOI18N
                            unbufferLib;
                }

                env.put(ldPreloadEnv, ldPreload);

                if (isWindows) {
//                    String ldLibPath = env.get(ldLibraryPathEnv);
//                    ldLibPath = ((ldLibPath == null) ? "" : (ldLibPath + ";")) + // NOI18N
//                            unbufferPath + ";" + unbufferPath + "_64"; // NOI18N
//                    ldLibPath = CommandLineHelper.getInstance(execEnv).toShellPaths(ldLibPath);
//                    env.put(ldLibraryPathEnv, ldLibPath); // NOI18N
                } else {
                    String ldLibPath = env.get(ldLibraryPathEnv);
                    ldLibPath = ((ldLibPath == null) ? "" : (ldLibPath + ":")) + // NOI18N
                            unbufferPath + ":" + unbufferPath + "_64"; // NOI18N
                    env.put(ldLibraryPathEnv, ldLibPath); // NOI18N
                }
            }
        }
    }
}
