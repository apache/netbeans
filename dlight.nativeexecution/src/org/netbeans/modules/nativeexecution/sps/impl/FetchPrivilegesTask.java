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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;

public final class FetchPrivilegesTask implements Computable<ExecutionEnvironment, List<String>> {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public List<String> compute(ExecutionEnvironment execEnv) {
        /*
         * To find out actual privileges that tasks will have use
         * > ppriv -v $$ | grep [IL]
         *
         * and return intersection of list of I (inherit) and L (limit)
         * privileges...
         */

        ProcessUtils.ExitStatus res = null;
        try {
            String command = "/usr/bin/ppriv -v $$ | grep [IL]"; // NOI18N

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable("/bin/sh").setArguments("-c", command); // NOI18N

            res = ProcessUtils.execute(npb);

            if (res.exitCode != 0) {
                throw new IOException("Unable to get current privileges. Command " + // NOI18N
                        command + " failed with code " + res.exitCode); // NOI18N
            }

            List<String> iprivs = new ArrayList<>();
            List<String> lprivs = new ArrayList<>();

            List<String> out = res.getOutputLines();

            for (String str : out) {
                if (str.contains("I:")) { // NOI18N
                    String[] privs = str.substring(
                            str.indexOf(": ") + 2).split(","); // NOI18N
                    iprivs = Arrays.asList(privs);
                } else if (str.contains("L:")) { // NOI18N
                    String[] privs = str.substring(
                            str.indexOf(": ") + 2).split(","); // NOI18N
                    lprivs = Arrays.asList(privs);
                }
            }

            if (iprivs == null || lprivs == null) {
                return Collections.emptyList();
            }

            List<String> real_privs = new ArrayList<>();

            for (String ipriv : iprivs) {
                if (lprivs.contains(ipriv)) {
                    real_privs.add(ipriv);
                }
            }

            return real_privs;
        } catch (ConnectException ex) {
            return Collections.emptyList();
        } catch (IOException ex) {
            log.fine(ex.getMessage());
            if (res != null) {
                try {
                    ProcessUtils.logError(Level.FINE, log, res);
                } catch (IOException ioex) {
                }
            }
        }

        return Collections.emptyList();
    }
}
