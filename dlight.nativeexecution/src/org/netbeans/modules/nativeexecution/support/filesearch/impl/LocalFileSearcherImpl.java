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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchParams;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher.class, position = 70)
/**
 * In case of Windows use WINDOWS paths (c:\\mypath)... Also it requires exact
 * names (programm.exe and not just programm)
 */
public final class LocalFileSearcherImpl implements FileSearcher {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public final String searchFile(FileSearchParams fileSearchParams) {
        final ExecutionEnvironment execEnv = fileSearchParams.getExecEnv();

        if (!execEnv.isLocal()) {
            return null;
        }

        log.log(Level.FINE, "File Searching Task: {0}...", fileSearchParams.toString()); // NOI18N

        List<String> sp = new ArrayList<>(fileSearchParams.getSearchPaths());

        if (fileSearchParams.isSearchInUserPaths()) {
            try {
                Map<String, String> environment = HostInfoUtils.getHostInfo(execEnv).getEnvironment();
                String path = null;
                if (environment.containsKey("Path")) { // NOI18N
                    path = environment.get("Path"); // NOI18N
                } else if (environment.containsKey("PATH")) { // NOI18N
                    path = environment.get("PATH"); // NOI18N
                }
                if (path != null) {
                    sp.addAll(Arrays.asList(path.split(File.pathSeparator)));
                }
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }

        String file = fileSearchParams.getFilename();

        for (String path : sp) {
            try {
                File f = new File(path, file);
                log.log(Level.FINE, "   Test ''{0}''", f.toString()); // NOI18N
                if (f.canRead()) {
                    log.log(Level.FINE, "   FOUND ''{0}''", f.toString()); // NOI18N
                    return f.getCanonicalPath();
                }
            } catch (Throwable th) {
                log.log(Level.FINE, "Execption in LocalFileSearcherImpl:", th); // NOI18N
            }
        }

        return null;
    }
}
