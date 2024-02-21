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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.ProjectActions;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(
        service = ReplaceTokenProvider.class, 
        projectType = NbGradleProject.GRADLE_PROJECT_TYPE
)
public class JavaExecTokenProvider implements ReplaceTokenProvider {
    /**
     * Replaceable token for JVM arguments project property. Generates project property for NB Tooling Gradle plugin, if the extra JVM arguments are present, otherwise
     * generates an empty String.
     * @see #TOKEN_JAVA_JVMARGS
     */
    public static String TOKEN_JAVAEXEC_JVMARGS = ProjectActions.TOKEN_JAVAEXEC_JVMARGS;

    /**
     * Replaceable token for program parameters as a commandline option. Generates --args <i>&lt;parameter-list></i>, if the extra parameters are present, otherwise
     * generates an empty String.
     * @see #TOKEN_JAVA_ARGS
     */
    public static String TOKEN_JAVAEXEC_ARGS = ProjectActions.TOKEN_JAVAEXEC_ARGS;

    /**
     * Replaceable token for working directory project property. Generates project property for NB Tooling Gradle plugin, if the working directory is present, otherwise
     * generates an empty String.
     * @see #TOKEN_JAVA_CWD
     */
    public static String TOKEN_JAVAEXEC_CWD = ProjectActions.TOKEN_JAVAEXEC_CWD;

    /**
     * Replaceable token for environment variables project property. Generates project property for NB Tooling Gradle plugin, if the environment variables are present,
     * otherwise generates an empty String.
     * @see #TOKEN_JAVA_ENV
     */
    public static String TOKEN_JAVAEXEC_ENV = ProjectActions.TOKEN_JAVAEXEC_ENV;

    /**
     * Replaceable token for JVM arguments. Generates escaped / quoted arguments as a single String.
     */
    public static String TOKEN_JAVA_ARGS = "java.args"; // NOI18N

    /**
     * Replaceable token for program parameters. Parameters will be escaped and quoted and collected to 
     * a space-delimited String.
     */
    public static String TOKEN_JAVA_JVMARGS = "java.jvmArgs"; // NOI18N

    /**
     * Replaceable token for application working directory.
     */
    public static String TOKEN_JAVA_CWD = "java.workingDir"; // NOI18N

    /**
     * Replaceable token for environment variables. Generates escaped / quoted variable assignments as a single String.
     * Expression <code>VAR_NAME=VAR_VALUE</code> sets environment variable of name <code>VAR_NAME</code> to value
     * <code>VAR_VALUE</code>, expression <code>!VAR_NAME</code> unsets environment variable <code>VAR_NAME</code>.
     */
    public static String TOKEN_JAVA_ENV = "java.environment"; // NOI18N

    private static final Set<String> TOKENS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            TOKEN_JAVAEXEC_ARGS, TOKEN_JAVAEXEC_JVMARGS,
            TOKEN_JAVA_ARGS, TOKEN_JAVA_JVMARGS
    )));
    
    private final Project project;
    
    public JavaExecTokenProvider(Project project) {
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
        if (!isEnabled()) {
            return Collections.emptyMap();
        }
        StartupExtender.StartMode mode;
        
        switch (action) {
            case ActionProvider.COMMAND_RUN:
            case ActionProvider.COMMAND_RUN_SINGLE:
                mode = StartupExtender.StartMode.NORMAL;
                break;
            case ActionProvider.COMMAND_DEBUG:
            case ActionProvider.COMMAND_DEBUG_SINGLE:
                mode = StartupExtender.StartMode.DEBUG;
                break;
            case ActionProvider.COMMAND_PROFILE:
            case ActionProvider.COMMAND_PROFILE_SINGLE:
                mode = StartupExtender.StartMode.PROFILE;
                break;
            case ActionProvider.COMMAND_TEST:
            case ActionProvider.COMMAND_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_NORMAL;
                break;
            case ActionProvider.COMMAND_DEBUG_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_DEBUG;
                break;
            case ActionProvider.COMMAND_PROFILE_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_PROFILE;
                break;
            default:
                mode = null;
        }
        InstanceContent ic = new InstanceContent();
        if (project != null) {
            ic.add(project);
        }
        
        List<String> extraArgs = new ArrayList<>();
        if (mode != null) {
            for (StartupExtender group : StartupExtender.getExtenders(new AbstractLookup(ic), mode)) {
                extraArgs.addAll(group.getRawArguments());
            }
        }
        
        ExplicitProcessParameters contextParams = ExplicitProcessParameters.buildExplicitParameters(context);
        Map<String, String> result = new HashMap<>();
        result.put(TOKEN_JAVAEXEC_ARGS, ""); // NOI18N
        result.put(TOKEN_JAVAEXEC_JVMARGS, ""); // NOI18N
        result.put(TOKEN_JAVAEXEC_CWD, ""); // NOI18N
        result.put(TOKEN_JAVAEXEC_ENV, ""); // NOI18N
        result.put(TOKEN_JAVA_ARGS, ""); // NOI18N
        result.put(TOKEN_JAVA_JVMARGS, ""); // NOI18N
        result.put(TOKEN_JAVA_CWD, ""); // NOI18N
        result.put(TOKEN_JAVA_ENV, ""); // NOI18N
        if (extraArgs.isEmpty() && contextParams.isEmpty()) {
            return result;
        }
        ExplicitProcessParameters changedParams = ExplicitProcessParameters.builder().
                // Cannot read the rest of the commandline; custom args
                // are unsupported at the moment.
                // args(args).
                combine(contextParams).
                build();

        // need to pass JVM args and program args separately
        if (changedParams.getLauncherArguments() != null) {
            List<String> params = changedParams.getLauncherArguments();
            String jvmArgs = Utilities.escapeParameters(params.toArray(new String[0]));
            result.put(TOKEN_JAVA_JVMARGS, jvmArgs);

            String prop = Utilities.escapeParameters(new String[] {
                "-PrunJvmArgs=" + jvmArgs  // NOI18N
                
            });
            result.put(TOKEN_JAVAEXEC_JVMARGS,  prop);
        }
        if (changedParams.getArguments() != null && !changedParams.getArguments().isEmpty()) {
            List<String> params = changedParams.getArguments();
            String args = Utilities.escapeParameters(params.toArray(new String[0]));

            String prop = Utilities.escapeParameters(new String[] {
                args
            });
            result.put(TOKEN_JAVA_ARGS, args);
            result.put(TOKEN_JAVAEXEC_ARGS, "--args " + prop);  // NOI18N
        }
        if (changedParams.getWorkingDirectory() != null) {
            String wd = Utilities.escapeParameters(new String[] {
                changedParams.getWorkingDirectory().getAbsolutePath()
            });
            result.put(TOKEN_JAVA_CWD, wd);
            String prop = Utilities.escapeParameters(new String[] {
                "-PrunWorkingDir=" + wd     // NOI18N
            });
            result.put(TOKEN_JAVAEXEC_CWD, prop);
        }
        if (!changedParams.getEnvironmentVariables().isEmpty()) {
            List<String> envParams = new ArrayList<>(changedParams.getEnvironmentVariables().size());
            for (Map.Entry<String, String> entry : changedParams.getEnvironmentVariables().entrySet()) {
                if (entry.getValue() != null) {
                    envParams.add(entry.getKey() + "=" + entry.getValue());
                } else {
                    envParams.add("!" + entry.getKey());
                }
            }
            String env = Utilities.escapeParameters(envParams.toArray(new String[0]));
            result.put(TOKEN_JAVA_ENV, env);
            String prop = Utilities.escapeParameters(new String[] {
                "-PrunEnvironment=" + env   // NOI18N
            });
            result.put(TOKEN_JAVAEXEC_ENV, prop);
        }
        return result;
    }
}
