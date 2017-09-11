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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
