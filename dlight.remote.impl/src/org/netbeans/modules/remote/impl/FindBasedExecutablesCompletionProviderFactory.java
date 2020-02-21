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

package org.netbeans.modules.remote.impl;

import org.netbeans.modules.remote.api.ui.AutocompletionProvider;
import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */

//@ServiceProvider(service = AutocompletionProviderFactory.class)
// This provider is not, actualy, very useful, as it doesn't give the full path
// to the executable at the end... Plus in dialogs it is not a common practice
// to have completion for executables, taken from PATH...
//
//@ServiceProvider(service = AutocompletionProviderFactory.class)

public class FindBasedExecutablesCompletionProviderFactory implements AutocompletionProviderFactory {

    public AutocompletionProvider newInstance(ExecutionEnvironment env) {
        try {
            return new Provider(env);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public boolean supports(ExecutionEnvironment env) {
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    private final static class Provider implements AutocompletionProvider {

        private String[] executables = null;
        private final FutureTask<String[]> fetchTask;

        private Provider(ExecutionEnvironment env) throws IOException {
            fetchTask = new FutureTask<>(new Find(env));
            RequestProcessor.getDefault().post(fetchTask);
        }

        public List<String> autocomplete(String str) {
            if ("".equals(str)) { // NOI18N
                return Collections.<String>emptyList();
            }

            if (executables == null) {
                try {
                    executables = fetchTask.get();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            List<String> result = new ArrayList<>();

            boolean found = false;

            for (String exec : executables) {
                if (exec.startsWith(str)) {
                    result.add(exec);
                    found = true;
                } else if (found) {
                    break;
                }
            }

            return result;
        }

        private static final class Find implements Callable<String[]> {

            private final ExecutionEnvironment env;

            private Find(ExecutionEnvironment env) {
                this.env = env;
            }

            public String[] call() throws Exception {
                TreeSet<String> result = new TreeSet<>();

                try {
                    NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                    npb.setExecutable("/bin/sh").setArguments("-c", "find `echo $PATH|tr : ' '` -type f -perm -+x 2>/dev/null"); // NOI18N
                    ProcessUtils.ExitStatus rc = ProcessUtils.execute(npb);
                    for (String s : rc.getOutputLines()) {
                        int idx = s.lastIndexOf('/') + 1;
                        if (idx > 0) {
                            result.add(s.substring(idx));
                        }
                    }
                } catch (Exception ex) {
                }

                return result.toArray(new String[0]);
            }
        }
    }
}
