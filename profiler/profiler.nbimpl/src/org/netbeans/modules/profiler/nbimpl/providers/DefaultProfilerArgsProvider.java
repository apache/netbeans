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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.util.*;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "DESC_NBProfiler=NetBeans Profiler"
})
@StartupExtenderImplementation.Registration(displayName="#DESC_NBProfiler", position=1000, argumentsQuoted = false, startMode={
    StartupExtender.StartMode.PROFILE,
    StartupExtender.StartMode.TEST_PROFILE
})
public class DefaultProfilerArgsProvider implements StartupExtenderImplementation {
    @Override
    public List<String> getArguments(Lookup context, StartMode mode) {
        Project p = context.lookup(Project.class);
        if (p != null) {
            ProfilerLauncher.Session s = ProfilerLauncher.getLastSession();
            if (s != null) {
                Map<String, String> m = ProfilerLauncher.getLastSession().getProperties();
                if (m != null) {
                    List<String> args = new ArrayList<String>();
                    
                    String agentArgs = m.get("agent.jvmargs"); // NOI18N // Always set
                    // remove quoting, expand params to array
                    args.addAll(Arrays.asList(BaseUtilities.parseParameters(agentArgs)[0]));
                    
                    String jvmargs = m.get("profiler.info.jvmargs"); // NOI18N // May not be set
                    if (jvmargs != null) {
                        jvmargs = jvmargs.replace(" -", "^"); // NOI18N
                        StringTokenizer st = new StringTokenizer(jvmargs, "^"); // NOI18N
                        while (st.hasMoreTokens()) {
                            String arg = st.nextToken();
                            if (!arg.isEmpty()) {
                                // remove any quoting etc, there should be just a single parameter.
                                for (String a : BaseUtilities.parseParameters(arg)) {
                                    args.add((arg.startsWith("-") ? "" : "-") + a); // NOI18N
                                }
                            }
                        }
                    }
                    return args;
                }
            }
        }
        return Collections.EMPTY_LIST;
    }
}
