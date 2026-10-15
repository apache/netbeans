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

package org.netbeans.modules.extexecution.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;

/**
 * Utility class to make the local external process creation easier.
 * <p>
 * Builder handle command, working directory, <code>PATH</code> variable and HTTP proxy.
 * <p>
 * This class is <i>immutable</i>.
 * <p>
 * Also see {@link ProcessBuilder#getLocal()}.
 *
 * @author Petr Hejl
 * @see #call()
 */
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
        List<String> commandList = new ArrayList<String>();

        if (BaseUtilities.isWindows() && !ESCAPED_PATTERN.matcher(executable).matches()) {
            commandList.add(escapeString(executable));
        } else {
            commandList.add(executable);
        }

        List<String> args = buildArguments();
        commandList.addAll(args);

        java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder(commandList.toArray(new String[0]));
        if (workingDirectory != null) {
            pb.directory(workingDirectory);
        }

        Map<String, String> pbEnv = pb.environment();
        Map<String, String> env = buildEnvironment(pbEnv);
        pbEnv.putAll(env);
        String uuid = UUID.randomUUID().toString();
        pbEnv.put(WrapperProcess.KEY_UUID, uuid);
        adjustProxy(pb);
        pb.redirectErrorStream(redirectErrorStream);
        logProcess(Level.FINE, pb);
        WrapperProcess wp = new WrapperProcess(pb.start(), uuid);
        return wp;
    }

    /**
     * Logs the given <code>pb</code> using the given <code>level</code>.
     *
     * @param pb the ProcessBuilder to log.
     * @param level the level for logging.
     */
    private void logProcess(final Level level, final java.lang.ProcessBuilder pb) {

        if (!LOGGER.isLoggable(level)) {
            return;
        }

        File dir = pb.directory();
        String basedir = dir == null ? "" : "(basedir: " + dir.getAbsolutePath() + ") "; //NOI18N

        StringBuilder command = new StringBuilder();
        for (Iterator<String> it = pb.command().iterator(); it.hasNext();) {
            command.append(it.next());
            if (it.hasNext()) {
                command.append(' '); //NOI18N
            }
        }

        LOGGER.log(level, "Running: " + basedir + '"' + command.toString() + '"'); //NOI18N
        LOGGER.log(level, "Environment: " + pb.environment()); //NOI18N
    }

    // package level for unit testing
    Map<String, String> buildEnvironment(Map<String, String> original) {
        Map<String, String> ret = new HashMap<String, String>(original);
        ret.putAll(envVariables);

        // Find PATH environment variable - on Windows it can be some other
        // case and we should use whatever it has.
        String pathName = getPathName(original);

        // TODO use StringBuilder
        String currentPath = ret.get(pathName);

        if (currentPath == null) {
            currentPath = "";
        }

        for (File path : paths) {
            currentPath = path.getAbsolutePath().replace(" ", "\\ ") //NOI18N
                    + File.pathSeparator + currentPath;
        }

        if (!"".equals(currentPath.trim())) {
            ret.put(pathName, currentPath);
        }
        return ret;
    }


    // package level for unit testing
    List<String> buildArguments() {
        if (!BaseUtilities.isWindows()) {
            return new ArrayList<String>(arguments);
        }
        List<String> result = new ArrayList<String>(arguments.size());
        for (String arg : arguments) {
            if (arg != null && !ESCAPED_PATTERN.matcher(arg).matches()) {
                result.add(escapeString(arg));
            } else {
                result.add(arg);
            }
        }
        return result;
    }

    public static void putPath(File path, String pathName, boolean prepend, Map<String, String> current) {
        String currentPath = current.get(pathName);

        if (currentPath == null) {
            currentPath = "";
        }

        if (prepend) {
            currentPath = path.getAbsolutePath().replace(" ", "\\ ") //NOI18N
                    + File.pathSeparator + currentPath;
        } else {
            currentPath = currentPath + File.pathSeparator
                    + path.getAbsolutePath().replace(" ", "\\ "); //NOI18N
        }

        if (!"".equals(currentPath.trim())) {
            current.put(pathName, currentPath);
        }
    }

    public static String getPathName(Map<String, String> systemEnv) {
        // Find PATH environment variable - on Windows it can be some other
        // case and we should use whatever it has.
        String pathName = "PATH"; // NOI18N

        if (BaseUtilities.isWindows()) {
            pathName = "Path"; // NOI18N

            for (String keySystem : systemEnv.keySet()) {
                if ("PATH".equals(keySystem.toUpperCase(Locale.ENGLISH))) { // NOI18N
                    pathName = keySystem;
                    break;
                }
            }
        }
        return pathName;
    }

    private static String escapeString(String s) {
        if (s.length() == 0) {
            return "\"\""; // NOI18N
        }

        StringBuilder sb = new StringBuilder();

        boolean hasSpace = false;
        final int slen = s.length();
        char c;

        for (int i = 0; i < slen; i++) {
            c = s.charAt(i);

            if (Character.isWhitespace(c)) {
                hasSpace = true;
                sb.append(c);

                continue;
            }
            sb.append(c);
        }

        if (hasSpace) {
            sb.insert(0, '"'); // NOI18N
            sb.append('"'); // NOI18N
        }
        return sb.toString();
    }

    private void adjustProxy(java.lang.ProcessBuilder pb) {
        String proxy = getNetBeansHttpProxy();
        if (proxy != null) {
            Map<String, String> env = pb.environment();
            if ((env.get("HTTP_PROXY") == null) && (env.get("http_proxy") == null)) { // NOI18N
                env.put("HTTP_PROXY", proxy); // NOI18N
                env.put("http_proxy", proxy); // NOI18N
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
