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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support.filesearch.impl;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchParams;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher.class, position = 100)
public class RemoteFileSearcherImpl implements FileSearcher {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public String searchFile(FileSearchParams fileSearchParams) {
        final ExecutionEnvironment execEnv = fileSearchParams.getExecEnv();

        if (execEnv.isLocal()) {
            return null;
        }

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);

            if (hostInfo == null) {
                return null;
            }

            List<String> sp = new ArrayList<>(fileSearchParams.getSearchPaths());

            if (fileSearchParams.isSearchInUserPaths()) {
                String path = hostInfo.getEnvironment().get("PATH"); // NOI18N
                if (path != null) {
                    sp.addAll(Arrays.asList(path.split(":"))); // NOI18N
                }
            }

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(hostInfo.getShell()).setArguments("-s"); // NOI18N

            Process p = npb.call();
            ProcessUtils.ignoreProcessError(p);

            OutputStreamWriter os = new OutputStreamWriter(p.getOutputStream());
            for (String path : sp) {
                if (path.indexOf('"') >= 0) { // Will not use paths with "
                    continue;
                }

                os.append("/bin/ls \"").append(path); // NOI18N
                os.append("/" + fileSearchParams.getFilename() + "\" 2>/dev/null || \\\n"); // NOI18N
            }

            os.append("(echo \"Not Found\" && exit 1)\n"); // NOI18N
            os.append("exit $?\n"); // NOI18N

            os.flush();
            os.close();

            String line = ProcessUtils.readProcessOutputLine(p);
            int result = p.waitFor();

            return (result != 0 || line == null || "".equals(line.trim())) ? null : line.trim(); // NOI18N
        } catch (Throwable th) {
            log.log(Level.FINE, "Execption in UnixFileSearcherImpl:", th); // NOI18N
        }

        return null;
    }
}
