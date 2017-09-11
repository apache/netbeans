/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.providers;

import java.util.*;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "DESC_NBProfiler=NetBeans Profiler"
})
@StartupExtenderImplementation.Registration(displayName="#DESC_NBProfiler", position=1000, startMode={
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
                    args.add(agentArgs);
                    
                    String jvmargs = m.get("profiler.info.jvmargs"); // NOI18N // May not be set
                    if (jvmargs != null) {
                        jvmargs = jvmargs.replace(" -", "^"); // NOI18N
                        StringTokenizer st = new StringTokenizer(jvmargs, "^"); // NOI18N
                        while (st.hasMoreTokens()) {
                            String arg = st.nextToken();
                            if (!arg.isEmpty()) {
                                args.add((arg.startsWith("-") ? "" : "-") + arg); // NOI18N
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
