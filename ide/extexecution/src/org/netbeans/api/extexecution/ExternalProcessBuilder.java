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

package org.netbeans.api.extexecution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.base.Environment;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Utility class to make the local external process creation easier.
 * <p>
 * Builder handle command, working directory, <code>PATH</code> variable and HTTP proxy.
 * <p>
 * This class is <i>immutable</i>.
 *
 * @author Petr Hejl
 * @see #call()
 * @deprecated use {@link org.netbeans.api.extexecution.base.ProcessBuilder#getLocal()}
 */
@Deprecated
public final class ExternalProcessBuilder implements Callable<Process> {

    private static final Logger LOGGER = Logger.getLogger(ExternalProcessBuilder.class.getName());

    private static final Pattern ESCAPED_PATTERN = Pattern.compile("\".*\""); // NOI18N

    // FIXME: get rid of those proxy constants as soon as some NB Proxy API is available
    private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"; // NOI18N

    private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"; // NOI18N

    private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"; // NOI18N

    private final String executable;

    private final File workingDirectory;

    private final boolean redirectErrorStream;

    private final List<String> arguments = new ArrayList<String>();

    private final List<File> paths = new ArrayList<File>();

    private final Map<String, String> envVariables = new HashMap<String, String>();

    /**
     * Creates the new builder that will create the process by running
     * given executable. Arguments must not be part of the string.
     *
     * @param executable executable to run
     */
    public ExternalProcessBuilder(@NonNull String executable) {
        this(new BuilderData(executable));
    }

    private ExternalProcessBuilder(BuilderData builder) {
        this.executable = builder.executable;
        this.workingDirectory = builder.workingDirectory;
        this.redirectErrorStream = builder.redirectErrorStream;
        this.arguments.addAll(builder.arguments);
        this.paths.addAll(builder.paths);
        this.envVariables.putAll(builder.envVariables);
    }

    /**
     * Returns a builder with configured working directory. Process
     * subsequently created by the {@link #call()} method on returned builder
     * will be executed with this directory as current working dir.
     * <p>
     * The default value is undefined. Note that in such case each process has
     * working directory corresponding to the value of <code>user.dir</code>
     * system property.
     * <p>
     * All other properties of the returned builder are inherited from
     * <code>this</code>.
     *
     * @param workingDirectory working directory
     * @return new builder with configured working directory
     */
    @NonNull
    @CheckReturnValue
    public ExternalProcessBuilder workingDirectory(@NonNull File workingDirectory) {
        Parameters.notNull("workingDirectory", workingDirectory);

        BuilderData builder = new BuilderData(this);
        return new ExternalProcessBuilder(builder.workingDirectory(workingDirectory));
    }

    /**
     * Returns a builder with configured error stream redirection. If configured
     * value is <code>true</code> process subsequently created by
     * the {@link #call()} method on returned builder will redirect the error
     * stream to the standard output stream.
     * <p>
     * The default value is <code>false</code>.
     * <p>
     * All other properties of the returned builder are inherited from
     * <code>this</code>.
     *
     * @param redirectErrorStream if <code>true</code> error stream will be
     *             redirected to standard output
     * @return new builder with configured error stream redirection
     */
    @NonNull
    @CheckReturnValue
    public ExternalProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        BuilderData builder = new BuilderData(this);
        return new ExternalProcessBuilder(builder.redirectErrorStream(redirectErrorStream));
    }

    /**
     * Returns a builder with additional path in <code>PATH</code> variable.
     * <p>
     * In the group of paths added by this call the last added path will
     * be the first one in the <code>PATH</code> variable.
     * <p>
     * By default no additional paths are added to <code>PATH</code> variable.
     * <p>
     * All other properties of the returned builder are inherited from
     * <code>this</code>.
     *
     * @param path path to add to <code>PATH</code> variable
     * @return new builder with additional path in <code>PATH</code> variable
     */
    @NonNull
    @CheckReturnValue
    public ExternalProcessBuilder prependPath(@NonNull File path) {
        Parameters.notNull("path", path);

        BuilderData builder = new BuilderData(this);
        return new ExternalProcessBuilder(builder.prependPath(path));
    }

    /**
     * Returns a builder with additional argument for the command. Arguments
     * are passed to executable in the same order in which they are added.
     * <p>
     * By default no additional arguments are passed to executable.
     * <p>
     * All other properties of the returned builder are inherited from
     * <code>this</code>.
     * <p>
     * If there is a need to parse arguments already provided as one big
     * string the method that can help is
     * {@link Utilities#parseParameters(java.lang.String)}.
     *
     *
     * @param argument command argument to add
     * @return new builder with additional argument for the command
     */
    @NonNull
    @CheckReturnValue
    public ExternalProcessBuilder addArgument(@NonNull String argument) {
        Parameters.notNull("argument", argument);

        BuilderData builder = new BuilderData(this);
        return new ExternalProcessBuilder(builder.addArgument(argument));
    }

    /**
     * Returns a builder with additional environment variable for the command.
     * <p>
     * By default no additional environment variables are configured.
     * <p>
     * All other properties of the returned builder are inherited from
     * <code>this</code>.
     *
     * @param name name of the variable
     * @param value value of the variable
     * @return new builder with additional environment variable for the command
     * @see #call()
     */
    @NonNull
    @CheckReturnValue
    public ExternalProcessBuilder addEnvironmentVariable(@NonNull String name, @NonNull String value) {
        Parameters.notNull("name", name);
        Parameters.notNull("value", value);

        BuilderData builder = new BuilderData(this);
        return new ExternalProcessBuilder(builder.addEnvironmentVariable(name, value));
    }

    /**
     * Creates the new {@link Process} based on the properties configured
     * in this builder. Created process will try to kill all its children on
     * call to {@link Process#destroy()}.
     * <p>
     * Process is created by executing the executable with configured arguments.
     * If custom working directory is specified it is used otherwise value
     * of system property <code>user.dir</code> is used as working dir.
     * <p>
     * Environment variables are prepared in following way:
     * <ol>
     *   <li>Get table of system environment variables.
     *   <li>Put all environment variables configured by
     * {@link #addEnvironmentVariable(java.lang.String, java.lang.String)}.
     * This rewrites system variables if conflict occurs.
     *   <li>Get <code>PATH</code> variable and append all paths added
     * by {@link #prependPath(java.io.File)}. The order of paths in <code>PATH</code>
     * variable is reversed to order of addition (the last added is the first
     * one in <code>PATH</code>). Original content of <code>PATH</code> follows
     * the added content.
     *   <li>If neither <code>http_proxy</code> nor <code>HTTP_PROXY</code>
     * environment variable is set then HTTP proxy settings configured in the
     * IDE are stored as <code>http_proxy</code> environment variable
     * (the format of the value is <code>http://username:password@host:port</code>).
     * </ol>
     * @return the new {@link Process} based on the properties configured
     *             in this builder
     * @throws IOException if the process could not be created
     */
    @NonNull
    @Override
    public Process call() throws IOException {
        org.netbeans.api.extexecution.base.ProcessBuilder builder =
                org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        builder.setExecutable(executable);
        if (workingDirectory != null) {
            builder.setWorkingDirectory(workingDirectory.getPath());
        }
        builder.setArguments(arguments);
        builder.setRedirectErrorStream(redirectErrorStream);
        Environment env  = builder.getEnvironment();
        for (File path : paths) {
            env.prependPath("PATH", path.getPath());
        }
        for (Map.Entry<String, String> entry : envVariables.entrySet()) {
            env.setVariable(entry.getKey(), entry.getValue());
        }
        // XXX just to be sure
        adjustProxy(env);

        return builder.call();
    }

    private void adjustProxy(Environment env) {
        String proxy = getNetBeansHttpProxy();
        if (proxy != null) {
            if ((env.getVariable("HTTP_PROXY") == null) && (env.getVariable("http_proxy") == null)) { // NOI18N
                env.setVariable("HTTP_PROXY", proxy); // NOI18N
                env.setVariable("http_proxy", proxy); // NOI18N
            }
            // PENDING - what if proxy was null so the user has TURNED off
            // proxies while there is still an environment variable set - should
            // we honor their environment, or honor their NetBeans proxy
            // settings (e.g. unset HTTP_PROXY in the environment before
            // launching plugin?
        }
    }

    /**
     * FIXME: get rid of the whole method as soon as some NB Proxy API is
     * available.
     */
    private static String getNetBeansHttpProxy() {
        // FIXME use ProxySelector

        String host = System.getProperty("http.proxyHost"); // NOI18N

        if (host == null) {
            return null;
        }

        String portHttp = System.getProperty("http.proxyPort"); // NOI18N
        int port;

        try {
            port = Integer.parseInt(portHttp);
        } catch (NumberFormatException e) {
            port = 8080;
        }

        Preferences prefs = NbPreferences.root().node("org/netbeans/core"); // NOI18N
        boolean useAuth = prefs.getBoolean(USE_PROXY_AUTHENTICATION, false);
        String auth = "";
        if (useAuth) {
            auth = prefs.get(PROXY_AUTHENTICATION_USERNAME, "") + ":" + prefs.get(PROXY_AUTHENTICATION_PASSWORD, "") + '@'; // NOI18N
        }

        // Gem requires "http://" in front of the port name if it's not already there
        if (host.indexOf(':') == -1) {
            host = "http://" + auth + host; // NOI18N
        }

        return host + ":" + port; // NOI18N
    }

    private static class BuilderData {

        private final String executable;

        private File workingDirectory;

        private boolean redirectErrorStream;

        private List<String> arguments = new ArrayList<String>();

        private List<File> paths = new ArrayList<File>();

        private Map<String, String> envVariables = new HashMap<String, String>();

        public BuilderData(String executable) {
            this.executable = executable;
        }

        public BuilderData(ExternalProcessBuilder builder) {
            this.executable = builder.executable;
            this.workingDirectory = builder.workingDirectory;
            this.redirectErrorStream = builder.redirectErrorStream;
            this.arguments.addAll(builder.arguments);
            this.paths.addAll(builder.paths);
            this.envVariables.putAll(builder.envVariables);
        }

        public BuilderData workingDirectory(File workingDirectory) {
            assert workingDirectory != null;

            this.workingDirectory = workingDirectory;
            return this;
        }

        public BuilderData redirectErrorStream(boolean redirectErrorStream) {
            this.redirectErrorStream = redirectErrorStream;
            return this;
        }

        public BuilderData prependPath(File path) {
            assert path != null;

            paths.add(path);
            return this;
        }

        public BuilderData addArgument(String argument) {
            assert argument != null;

            arguments.add(argument);
            return this;
        }

        public BuilderData addEnvironmentVariable(String name, String value) {
            assert name != null;
            assert value != null;

            envVariables.put(name, value);
            return this;
        }
    }


}
