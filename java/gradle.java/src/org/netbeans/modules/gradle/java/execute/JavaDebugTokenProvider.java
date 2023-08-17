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
package org.netbeans.modules.gradle.java.execute;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.actions.BeforeBuildActionHook;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(
        service = ReplaceTokenProvider.class,
        projectType = NbGradleProject.GRADLE_PROJECT_TYPE
)
public class JavaDebugTokenProvider implements ReplaceTokenProvider {
    /**
     * Replaceable token for debugging port.
     */
    public static String TOKEN_JAVAEXEC_DEBUG_PORT = "javaExec.debug.port"; // NOI18N

    private static final Set<String> TOKENS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            TOKEN_JAVAEXEC_DEBUG_PORT
    )));

    private static final Set<String> DEBUG_ACTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ActionProvider.COMMAND_DEBUG,
            ActionProvider.COMMAND_DEBUG_SINGLE,
            ActionProvider.COMMAND_DEBUG_TEST_SINGLE
    )));

    private final Project project;

    public JavaDebugTokenProvider(Project project) {
        this.project = project;
    }

    @Override
    public Set<String> getSupportedTokens() {
        return isEnabled() ? TOKENS : Collections.emptySet();
    }

    private boolean isEnabled() {
        Set<String> plugins = GradleBaseProject.get(project).getPlugins();
        return plugins.contains("java"); // NOI18N
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup context) {
        if (!isEnabled() || !DEBUG_ACTIONS.contains(action)) {
            return Collections.emptyMap();
        }

        OutputHolder output = context.lookup(OutputHolder.class);

        if (output == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();

        try {
            result.put(TOKEN_JAVAEXEC_DEBUG_PORT, "" + new JPDAStart(output.out, project).execute());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    private static final class OutputHolder {
        private final PrintWriter out;

        public OutputHolder(PrintWriter out) {
            this.out = out;
        }

    }

    public static final class DebugTokenHook implements BeforeBuildActionHook {

        @Override
        public Lookup beforeAction(String action, Lookup context, PrintWriter out) {
            if (!DEBUG_ACTIONS.contains(action))
                return context;
            return new ProxyLookup(context, Lookups.fixed(new OutputHolder(out)));
        }

    }
}
