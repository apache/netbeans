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
package org.netbeans.api.extexecution.base;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Lookup;

/**
 * Allows to augment or replace process parameters for a single execution action.
 * The class is intended to be used by launchers which build parameters based on some
 * persistent configuration (project, workspace) to allow additions, or replacements
 * for a single execution only.
 * <p>
 * It is <b>strongly recommended</b> for any feature that performs execution of a process to support {@link ExplicitProcessParameters},
 * from a contextual {@link Lookup}, or at worst from {@link Lookup#getDefault()}. It will allow for future customizations and
 * automation of the feature, enhancing the process launch for various environments, technologies etc.
 * <p>
 * <i>Note:</i> please refer also to {@code StartupExtender} API in the {@code extexecution} module, which contributes globally
 * to launcher arguments.
 * <p>
 * Two groups of parameters are recognized: {@link #getLauncherArguments()}, which should be passed
 * first to the process (i.e. launcher parameters) and {@link #getArguments()} that represent the ordinary
 * process arguments.
 * <div class="nonnormative">
 * For <b>java applications</b> when {@code java} executable is used to launch the application, or even Maven project (see below), the <b>launcherArgs</b> should correspond to VM
 * arguments, and <b>args</b> correspond to the main class' arguments (passed to the main class). Additional environment variables can be specified.
 * </div>
 * <p>
 * If the object is marked as {@link #isArgReplacement()}, the launcher implementor SHOULD replace all
 * default or configured parameters with contents of this instruction. Both arguments and launcherArguments can have value {@code null}, which means "undefined": 
 * in that case, the relevant group of configured parameters should not be affected.
 * <p>
 * Since these parameters are passed <b>externally</b>, there's an utility method, {@link #buildExplicitParameters(org.openide.util.Lookup)}
 * that builds the explicit parameter instruction based on {@link Lookup} contents. The parameters are
 * merged in the order of the {@link Builder#position(int)  configured rank} and appearance (in the sort ascending order). 
 * The default rank is {@code 0}, which allows both append or prepend parameters. If an item's 
 * {@link #isArgReplacement()} is true, all arguments collected so far are discarded.
 * <p>
 * <div class="nonnormative">
 * If the combining algorithm is acceptable for the caller's purpose, the following pattern may be used to build the final
 * command line:
 * <div>
 * {@snippet file="org/netbeans/api/extexecution/base/ExplicitProcessParametersTest.java" region="decorateWithExplicitParametersSample"}
 * </div>
 * This example will combine some args and extra args from project, or configuration with arguments passed from the
 * {@code runContext} Lookup. 
 * Supposing that a Maven project module supports {@code ExplicitProcessParameters} (it does from version 2/2.144), the caller may influence or override the
 * parameters passed to the maven exec:exec task (for Run action) this way:
 * <code><pre>
 *   ActionProvider ap = ... ; // obtain ActionProvider from the project.
 *   ExplicitProcessParameters explicit = ExplicitProcessParameters.builder().
 *           launcherArg("-DvmArg2=2").
 *           arg("paramY").
 *      build();
 *   ap.invokeAction(ActionProvider.COMMAND_RUN, Lookups.fixed(explicit));
 * </pre></code>
 * By default, <b>args</b> instruction(s) will discard the default parameters, so the above example will also <b>ignore</b> all application
 * parameters provided in maven action mapping. The caller may, for example, want to just <b>append</b> parameters (i.e. list of files ?) and
 * completely replace (default) VM parameters which may be unsuitable for the operation:
 * {@snippet file="org/netbeans/api/extexecution/base/ExplicitProcessParametersTest.java" region="testDiscardDefaultVMParametersAppendAppParameters"}
 * <p>
 * Note that multiple {@code ExplicitProcessParameters} instances may be added to the Lookup, acting as append or replacement
 * for the parameters collected so far.
 * </div>
 * @author sdedic
 * @since 1.16
 */
public final class ExplicitProcessParameters {
    final int position;
    private final List<String>    launcherArguments;
    private final List<String>    arguments;
    private final boolean  replaceArgs;
    private final boolean  replaceLauncherArgs;
    private final File workingDirectory;
    private final Map<String, String> environmentVars;

    private ExplicitProcessParameters(int position, List<String> launcherArguments, 
            List<String> arguments, boolean appendArgs, boolean appendLauncherArgs,
            File workingDirectory, Map<String, String> environmentVars) {
        this.position = position;
        this.launcherArguments = launcherArguments == null ? null : Collections.unmodifiableList(launcherArguments);
        this.arguments = arguments == null ? null : Collections.unmodifiableList(arguments);
        this.replaceArgs = appendArgs;
        this.replaceLauncherArgs = appendLauncherArgs;
        this.workingDirectory = workingDirectory;
        this.environmentVars = environmentVars == null ? null : Collections.unmodifiableMap(environmentVars);
    }
    
    private static final ExplicitProcessParameters EMPTY = new ExplicitProcessParameters(0, null, null, false, false, null, null);
    
    /**
     * Returns an empty instance of parameters that has no effect. DO NOT check for emptiness by
     * equality or reference using the instance; use {@link #isEmpty()}.
     * @return empty instance.
     */
    public static ExplicitProcessParameters empty() {
        return EMPTY;
    }
    
    /**
     * Returns true, if the instance has no effect when {@link Builder#combine}d onto base parameters.
     * @return true, if no effect is expected.
     */
    public boolean isEmpty() {
        boolean change = false;
        if (isArgReplacement() || isLauncherArgReplacement()) {
            return false;
        }
        return  ((arguments == null) || arguments.isEmpty()) &&
                (launcherArguments == null || launcherArguments.isEmpty()) &&
                workingDirectory == null &&
                (environmentVars == null || environmentVars.isEmpty());
    }

    /**
     * Returns the arguments to be passed. Returns {@code null} if the object does not
     * want to alter the argument list. 
     * @return arguments to be passed or {@code null} if the argument list should not be altered.
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Returns the launcher arguments to be passed. Returns {@code null} if the object does not
     * want to alter the argument list.
     * @return arguments to be passed or {@code null} if the launcher argument list should not be altered.
     */
    public List<String> getLauncherArguments() {
        return launcherArguments;
    }
    
    /**
     * Instructs to replace arguments collected so far.
     * @return true, if arguments collected should be discarded.
     */
    public boolean isArgReplacement() {
        return replaceArgs;
    }

    /**
     * Instructs to replace launcher arguments collected so far.
     * @return true, if launcher arguments collected should be discarded.
     */
    public boolean isLauncherArgReplacement() {
        return replaceLauncherArgs;
    }
    
    /**
     * Returns the argument lists merged. Launcher arguments (if any) are passed first, followed
     * by {@code middle} (if any), then (normal) arguments. The method is a convenience to build
     * a complete command line for the launcher + command + command arguments.
     * @return combined arguments.
     */
    public @NonNull List<String> getAllArguments(List<String> middle) {
        List<String> a = new ArrayList<>();
        if (launcherArguments != null) {
            a.addAll(launcherArguments);
        }
        if (middle != null && !middle.isEmpty()) {
            a.addAll(middle);
        }
        if (arguments != null) {
            a.addAll(arguments);
        }
        return a;
    }
    
    /**
     * Returns the argument lists merged. Launcher arguments (if any) are passed first, followed
     * by {@code middle} (if any), then (normal) arguments. The method is a convenience to build
     * a complete command line for the launcher + command + command arguments.
     * @return combined arguments.
     */
    public @NonNull List<String> getAllArguments(@NullAllowed String... middle) {
        return getAllArguments(middle == null ? Collections.emptyList() : Arrays.asList(middle));
    }

    /**
     * Returns working directory to be set for the process.
     *
     * @return working directory, or <code>nul</code>
     * @since 1.20
     */
    public @CheckForNull File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Returns a map of additional environment variables to be set for the process.
     * Always non-null. Values of existing environment variables are overridden.
     * A <code>null</code> value of a variable should be interpreted as a removal
     * of that variable from the environment.
     *
     * @return map of additional environment variables
     * @since 1.20
     */
    public @NonNull Map<String, String> getEnvironmentVariables() {
        return environmentVars != null ? environmentVars : Collections.emptyMap();
    }

    /**
     * Merges ExplicitProcessParameters instructions found in the Lookup. See {@link #buildExplicitParameters(java.util.Collection)}
     * for more details.
     * @param context context for the execution
     * @return merged instructions
     */
    @NonNull
    public static ExplicitProcessParameters buildExplicitParameters(Lookup context) {
        return buildExplicitParameters(context.lookupAll(ExplicitProcessParameters.class));
    }
    
    /**
     * Merges individual instruction. 
     * This method serves as a convenience and uniform ("standard") methods to merge argument lists for process execution. Should be used
     * whenever a process (build, run, tool, ...) is executed. If the feature diverges, it should document how it processes the
     * {@link ExplicitProcessParameters}. It is <b>strongly recommended</b> to support explicit parameters in order to allow for 
     * customizations and automation.
     * <p>
     * Processes instructions in the order of {@link Builder#position(int)} and appearance. Whenever an item is flagged as
     * a replacement, all arguments (launcher arguments) collected to that point are discarded. Item's arguments (launcher arguments)
     * will become the only ones listed.
     * <p>
     * <i>Note:</i> if a replacement instruction and all the following (if any) have {@link #getArguments()} {@code null} (= no change), 
     * the result will report <b>no change</b>. It is therefore possible to <b>discard all contributions</b> by appending a no-change replacement 
     * last.
     * <p>
     * Environment variables are overridden by newly set variables.
     * 
     * @param items individual instructions.
     * @return combined instructions.
     */
    public static ExplicitProcessParameters buildExplicitParameters(Collection<? extends ExplicitProcessParameters> items) {
        List<? extends ExplicitProcessParameters> all = new ArrayList<>(items);
        all.sort((a, b) -> a.position - b.position);
        Builder b = builder();
        for (ExplicitProcessParameters item : all) {
            b.combine(item);
        }
        return b.build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builds the {@link ExplicitProcessParameters} instance. The builder initially:
     * <ul>
     * <li><b>appends</b> launcher arguments
     * <li><b>replaces</b> (normal) arguments
     * </ul>
     * and the mode can be overridden for each group.
     */
    public static final class Builder {
        private int position = 0;
        private List<String> launcherArguments = null;
        private List<String> arguments = null;
        private Boolean  replaceArgs;
        private Boolean  replaceLauncherArgs;
        private File workingDirectory = null;
        private Map<String, String> environmentVars;
        
        private void initArgs() {
            if (arguments == null) {
                arguments = new ArrayList<>();
            }
        }
        
        /**
         * Appends a single argument. {@code null} is ignored.
         * @param a argument
         * @return the builder
         */
        public Builder arg(@NullAllowed String a) {
            if (a == null) {
                return this;
            }
            initArgs();
            arguments.add(a);
            return this;
        }

        /**
         * Appends arguments in the list. {@code null} is ignored as well as {@code null}
         * items in the list.
         * @param args argument list
         * @return the builder
         */
        public Builder args(@NullAllowed List<String> args) {
            if (args == null) {
                return this;
            }
            // init even if the list is empty.
            initArgs();
            args.forEach(this::arg);
            return this;
        }

        /**
         * Appends arguments in the list. {@code null} is ignored as well as {@code null}
         * items in the list.
         * @param args argument list
         * @return the builder
         */
        public Builder args(@NullAllowed String... args) {
            if (args == null) {
                return this;
            }
            return args(Arrays.asList(args));
        }
        
        private void initLauncherArgs() {
            if (launcherArguments == null) {
                launcherArguments = new ArrayList<>();
            }
        }
        
        /**
         * Appends a single launcher argument. {@code null} is ignored.
         * @param a launcher argument
         * @return the builder
         */
        public Builder launcherArg(@NullAllowed String a) {
            if (a == null) {
                return this;
            }
            initLauncherArgs();
            launcherArguments.add(a);
            return this;
        }

        /**
         * Appends arguments in the list. {@code null} is ignored as well as {@code null}
         * items in the list.
         * @param args argument list
         * @return the builder
         */
        public Builder launcherArgs(@NullAllowed List<String> args) {
            if (args == null) {
                return this;
            }
            initLauncherArgs();
            args.forEach(this::launcherArg);
            return this;
        }

        /**
         * Appends arguments in the list. {@code null} is ignored as well as {@code null}
         * items in the list.
         * @param args argument list
         * @return the builder
         */
        public Builder launcherArgs(@NullAllowed String... args) {
            if (args == null) {
                return this;
            }
            return launcherArgs(Arrays.asList(args));
        }
        
        /**
         * Changes the combining  mode for args. Setting to true instructs
         * that all arguments that may precede should be discarded and the
         * arguments provided by the built {@link ExplicitProcessParameters} are the only
         * ones passed to the process.
         * @param replace true to replace, false to append
         * @return the builder
         */
        public Builder replaceArgs(boolean replace) {
            this.replaceArgs = replace;
            return this;
        }
        
        /**
         * Changes the combining mode for launcher args. Setting to true instructs
         * that all arguments that may precede should be discarded and the
         * launcher arguments provided by the built {@link ExplicitProcessParameters} are the only
         * ones passed to the process.
         * @param replace true to replace, false to append
         * @return the builder
         */
        public Builder replaceLauncherArgs(boolean replace) {
            this.replaceLauncherArgs = replace;
            return this;
        }

        /**
         * Sets working directory to be used for the process.
         *
         * @param workingDirectory the working directory
         * @return the builder
         * @since 1.20
         */
        public Builder workingDirectory(File workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        /**
         * Provide additional environment variables for the process. Values of
         * existing environment variables are overridden. <code>null</code> values
         * are interpreted as removal of the respective variables from the environment.
         *
         * @param env a map of additional environment variables
         * @return the builder
         * @since 1.20
         */
        public Builder environmentVariables(Map<String, String> env) {
            if (!env.isEmpty()) {
                if (this.environmentVars == null) {
                    this.environmentVars = new HashMap<>();
                }
                this.environmentVars.putAll(env);
            }
            return this;
        }

        /**
         * Provide an additional environment variable for the process. If the variable
         * already exists, it's overridden with the new value.
         *
         * @param name name of the environment variable
         * @param value value of the environment variable, or <code>null</code> in which case an existing variable is to be removed.
         * @return the builder
         * @since 1.20
         */
        public Builder environmentVariable(String name, String value) {
            if (this.environmentVars == null) {
                this.environmentVars = new HashMap<>();
            }
            this.environmentVars.put(name, value);
            return this;
        }

        /**
         * Defines a position for combining. The default rank is {@code 0}. When used in a collection in
         * {@link ExplicitProcessParameters#buildExplicitParameters(java.util.Collection)}, instances are sorted
         * by their position, in ascending order (lowest first).
         * 
         * @param position rank of the instruction
         * @return the builder
         */
        public Builder position(int position) {
            this.position = position;
            return this;
        }
        
        /**
         * Apply {@link ExplicitProcessParameters} on top of this Builder's state.
         * It will merge in the passed instruction as described in {@link ExplicitProcessParameters#buildExplicitParameters(java.util.Collection)}.
         * 
         * @param p the instruction to combine
         * @return the modified builder
         */
        public Builder combine(@NullAllowed ExplicitProcessParameters p) {
            if (p == null) {
                return this;
            }
            if (p.isLauncherArgReplacement()) {
                launcherArguments = null;
                if (p.getLauncherArguments() != null) {
                    replaceLauncherArgs = true;
                } else {
                    replaceLauncherArgs = null;
                }
            }
            if (p.isArgReplacement()) {
                arguments = null;
                if (p.getArguments() != null) {
                    replaceArgs = true;
                } else {
                    replaceArgs = null;
                }
            }
            if (p.getLauncherArguments() != null) {
                launcherArgs(p.getLauncherArguments());
            }
            if (p.getArguments() != null) {
                args(p.getArguments());
            }
            if (p.getWorkingDirectory() != null) {
                workingDirectory(p.getWorkingDirectory());
            }
            if (!p.getEnvironmentVariables().isEmpty()) {
                environmentVariables(p.getEnvironmentVariables());
            }
            return this;
        }
        
        /**
         * Produces the {@link ExplicitProcessParameters} instruction.
         * @return the {@link ExplicitProcessParameters} instance.
         */
        public ExplicitProcessParameters build() {
            boolean aa = replaceArgs != null ? replaceArgs : arguments != null;
            boolean apa = replaceLauncherArgs != null ? replaceLauncherArgs : false;
            
            return new ExplicitProcessParameters(position, launcherArguments, arguments, 
                    // if no args / launcher args given and no explicit instruction on append,
                    // make the args appending.
                    aa, apa, workingDirectory, environmentVars);
        }
    }
}
