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
package org.netbeans.modules.nativeexecution.support.windows;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.Shell.ShellType;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public final class SimpleConverter implements PathConverter {

    private String cygwinPrefix = null;
    private String shellRootWinPath = null;

    @Override
    public String convert(PathType srcType, PathType trgType, String path) {
        if (cygwinPrefix == null
                && (srcType == PathType.CYGWIN || trgType == PathType.CYGWIN)) {
            initCygwinPrefix();
        }

        if (shellRootWinPath == null) {
            final Shell shell = WindowsSupport.getInstance().getActiveShell();
            shellRootWinPath = shell == null ? "" : shell.bindir.getParent(); // NOI18N
        }

        String result = path;

        if (trgType == PathType.WINDOWS) {
            String prefix = srcType == PathType.CYGWIN ? cygwinPrefix : "/"; // NOI18N
            int plen = prefix.length();
            if (path.length() > plen && path.startsWith(prefix)) {
                result = path.charAt(plen) + ":"; // NOI18N
                result += path.substring(plen + 1);
            } else if (path.startsWith("/")) { // NOI18N
                result = shellRootWinPath + result;
            }
            return result.replace('/', '\\'); // NOI18N
        }

        String prefix = trgType == PathType.CYGWIN ? cygwinPrefix : "/"; // NOI18N

        if (path.length() > 2 && path.charAt(1) == ':') {
            result = prefix + result.replaceFirst(":", ""); // NOI18N
        }

        return result.replace('\\', '/'); // NOI18N
    }

    @Override
    public String convertAll(PathType srcType, PathType trgType, String path) {
        String srcDelim = srcType == PathType.WINDOWS ? ";" : ":"; // NOI18N
        String trgDelim = trgType == PathType.WINDOWS ? ";" : ":"; // NOI18N

        String[] elems = path.split(srcDelim);
        StringBuilder sb = new StringBuilder(path.length());

        for (String elem : elems) {
            sb.append(convert(srcType, trgType, elem)).append(trgDelim);
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private synchronized void initCygwinPrefix() {
        if (cygwinPrefix != null) {
            return;
        }

        cygwinPrefix = "/cygdrive/"; // NOI18N

        final Shell shell = WindowsSupport.getInstance().getActiveShell();

        if (shell == null || shell.type != ShellType.CYGWIN) {
            return;
        }

        final File cygpath = new File(shell.bindir, "cygpath.exe"); // NOI18N

        if (!cygpath.exists()) {
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(
                cygpath.getAbsolutePath(), "-u", "c:"); // NOI18N
        ProcessUtils.ExitStatus res = ProcessUtils.execute(pb);
        if (res.isOK()) {
            String output = res.getOutputString();
            if (output.length() > 1) {
                cygwinPrefix = output.substring(0, output.length() - 1);
            }
        }
    }
}
