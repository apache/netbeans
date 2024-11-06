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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SignatureException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.support.Encrypter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

public final class SPSLocalImpl extends SPSCommonImpl {

    private static final Map<String, Long> csums = new HashMap<>();
    private final String privp;
    private String pid = null;

    static {
        csums.put("SunOS-x86", 4216904528L); // NOI18N
        csums.put("SunOS-sparc", 186118468L); // NOI18N
    }

    private SPSLocalImpl(ExecutionEnvironment execEnv, String privp) {
        super(execEnv);
        this.privp = privp;
    }

    public static SPSLocalImpl getNewInstance(ExecutionEnvironment execEnv)
            throws SignatureException, MissingResourceException {
        String privpCmd = null;

        MacroExpander macroExpander = MacroExpanderFactory.getExpander(execEnv);
        String path = "$osname-$platform"; // NOI18N
        try {
            path = macroExpander.expandPredefinedMacros(path); // NOI18N
        } catch (ParseException ex) {
        }

        privpCmd = "bin/nativeexecution/" + path + "/privp"; // NOI18N
        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        File file = fl.locate(privpCmd, "org.netbeans.modules.dlight.nativeexecution", false); //NOI18N

        if (file == null || !file.exists()) {
            throw new MissingResourceException(privpCmd, null, null);
        }

        privpCmd = file.getAbsolutePath();

        // Will not pass any password to unknown program...
        if (!Encrypter.checkCRC32(privpCmd, csums.get(path))) {
            throw new SignatureException("Wrong privp executable! CRC check failed!"); // NOI18N
        }

        // Set execution privileges ...
        Future<Integer> chmod = CommonTasksSupport.chmod(execEnv, privpCmd, 0755, null);
        try {
            chmod.get();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        return new SPSLocalImpl(execEnv, privpCmd);
    }

    @Override
    public boolean requestPrivileges(Collection<String> requestedPrivileges, String user, char[] passwd) throws NotOwnerException {
        PrintWriter w = null;

        try {
            // Construct privileges list
            StringBuilder sb = new StringBuilder();

            for (String priv : requestedPrivileges) {
                sb.append(priv).append(","); // NOI18N
            }

            Process process = new ProcessBuilder(privp, user, sb.toString(), getPID()).start();
            ProcessUtils.ignoreProcessOutputAndError(process);

            w = new PrintWriter(process.getOutputStream());
            w.println(passwd);
            w.flush();

            int result = process.waitFor();

            if (result != 0) {
                Logger.getInstance().log(Level.FINE, "privp returned {0}", result); // NOI18N
                throw new NotOwnerException();
            }

            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            Logger.getInstance().log(Level.FINE, "IOException in requestPrivileges : {0}", ex); // NOI18N
        } finally {
            if (w != null) {
                w.close();
            }
        }

        return false;
    }

    @Override
    synchronized String getPID() {
        if (pid != null) {
            return pid;
        }

        try {
            File self = new File("/proc/self"); // NOI18N
            pid = self.getCanonicalFile().getName();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return pid;
    }
}
