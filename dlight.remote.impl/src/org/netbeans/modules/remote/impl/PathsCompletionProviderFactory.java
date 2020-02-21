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

import java.io.File;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.remote.api.ui.AutocompletionProvider;
import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = AutocompletionProviderFactory.class)
public class PathsCompletionProviderFactory implements AutocompletionProviderFactory {
    
    private static final RequestProcessor RP = new RequestProcessor("PathsCompletionProviderFactory", 1); // NOI18N

    @Override
    public AutocompletionProvider newInstance(ExecutionEnvironment env) {
        try {
            return new Provider(env);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    @Override
    public boolean supports(ExecutionEnvironment env) {
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    private final static class Provider implements AutocompletionProvider {

        private final static int cacheSizeLimit = 20;
        private final static int cacheLifetime = 1000 * 60 * 10; // 10 min
        private final ExecutionEnvironment env;
        private String homeDir = null;
        private final LinkedList<CachedValue> cache = new LinkedList<>();
        private final Task cleanUpTask;

        public Provider(final ExecutionEnvironment env) throws IOException {
            this.env = env;
            cleanUpTask = RP.post(new Runnable() {

                @Override
                public void run() {
                    synchronized (cache) {
                        cache.clear();
                    }
                }
            }, cacheLifetime);
        }

        @Override
        public List<String> autocomplete(String str) {
            cleanUpTask.schedule(cacheLifetime);
            boolean absolutePaths = false;

            if ("~".equals(str) || ".".equals(str)) { // NOI18N
                List<String> dir = new ArrayList<>();

                if (".".equals(str) && env.isLocal()) { // NOI18N
                    dir.add(new File("").getAbsolutePath() + '/'); // NOI18N
                } else {
                    try {
                        HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                        dir.add(hostInfo.getUserDir() + '/');
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (CancellationException ex) {
                        // don't report cancellatoin exception
                    }
                }

                return dir;
            }

            if (str.startsWith("~")) { // NOI18N
                str = str.replaceFirst("~", getHomeDir()); // NOI18N
                absolutePaths = true;
            }

            if (!str.startsWith(".") && !str.startsWith("/") && !str.startsWith("~")) { // NOI18N
                return Collections.<String>emptyList();
            }

            List<String> result = new ArrayList<>();

            int idx = str.lastIndexOf('/') + 1;
            String dir = str.substring(0, idx);
            String file = str.substring(idx);
            List<String> content = listDir(dir);

            for (String c : content) {
                if (c.startsWith(file)) {
                    if (absolutePaths) {
                        result.add(dir + c);
                    } else {
                        result.add(c);
                    }
                }
            }

            return result;
        }

        private List<String> listDir(String dir) {
            synchronized (cache) {
                for (int i = 0; i < cache.size(); i++) {
                    CachedValue cv = cache.get(i);
                    if (cv.key.equals(dir)) {
                        cache.remove(i); // touch
                        cache.add(cv);
                        return cv.value;
                    }
                }
            }

            List<String> content = new ArrayList<>();

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.setExecutable("/bin/ls").setArguments("-1FL", dir); // NOI18N

            ProcessUtils.ExitStatus result = ProcessUtils.execute(npb);
            if (result.isOK()) {
                for (String s : result.getOutputLines()) {
                    if (s.endsWith("*")) { // NOI18N
                        content.add(s.substring(0, s.length() - 1));
                    } else if (s.endsWith("/")) {// NOI18N
                        content.add(s);
                    }
                }
            }

            synchronized (cache) {
                cache.add(new CachedValue(dir, content));

                while (cache.size() > cacheSizeLimit) {
                    cache.removeFirst();
                }
            }

            return content;
        }

        private synchronized String getHomeDir() {
            if (homeDir == null) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    homeDir = hostInfo.getUserDir();
                } catch (Exception ex) {
                    // fallback... 
                    homeDir = "/home/" + env.getUser() + "/"; // NOI18N
                }
            }

            return homeDir;
        }
    }

    private final static class CachedValue {

        final String key;
        final List<String> value;

        public CachedValue(String key, List<String> value) {
            this.key = key;
            this.value = value;
        }
    }
}
