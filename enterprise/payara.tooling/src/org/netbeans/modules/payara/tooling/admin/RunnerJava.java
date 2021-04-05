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
package org.netbeans.modules.payara.tooling.admin;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.modules.payara.tooling.server.config.PayaraConfigManager;
import org.netbeans.modules.payara.tooling.server.config.JavaSEPlatform;
import org.netbeans.modules.payara.tooling.server.config.JavaSESet;
import org.netbeans.modules.payara.tooling.utils.JavaUtils;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraConfig;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara server administration command execution using local Java VM.
 * <p/>
 * @author Tomas Kraus
 */
abstract class RunnerJava extends Runner {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(RunnerJava.class);

    /** Specifies program encapsulated in a JAR file to execute. */
    static final String JAR_PARAM = "-jar";

    /** Character used to separate query string from list of parameters. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final char QUERY_SEPARATOR = ' ';

    /** Character used to separate individual parameters. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final char PARAM_SEPARATOR = ' ';

    /** Character used to assign value to parameter. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    static final char PARAM_ASSIGN_VALUE = ' ';

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get {@link PayaraConfig} instance for provided Payara server which
     * shall not be <code>null</code>.
     * <p/>
     * {@link CommandException} is thrown when configuration object value
     * is <code>null</code>.
     * <p/>
     * @param server Payara server entity object.
     * @return Payara server features configuration which
     *         is not <code>null</code>.
     */
    static PayaraConfig getServerConfig(final PayaraServer server) {
        final String METHOD = "getServerConfig";
        PayaraConfig config = PayaraConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(
                server.getVersion()));
        if (config == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "noConfig"),
                    server.getVersion());
        }
        return config;
    }

    /**
     * Get {@link JavaSESet} instance for provided Payara server
     * features configuration.
     * <p/>
     * @param config Payara server features configuration.
     * @return Payara JavaSE configuration which is not <code>null</code>.
     */
    static JavaSESet getJavaSEConfig(final PayaraConfig config) {
        final String METHOD = "getJavaSEConfig";
        JavaSESet javaSEConfig = config.getJavaSE();
        if (javaSEConfig == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "noJavaSEConfig"));
        }
        return javaSEConfig;
    }

    /**
     * Constructs path to Java VM executable and verifies if it exists.
     * <p/>
     * @param server Payara server entity object.
     * @param command lassFish server administration command with local Java VM.
     * @return Path to Java VM executable
     */
    private static String getJavaVM(final PayaraServer server,
            final CommandJava command) {
        final String METHOD = "getJavaVM";
        String javaVmExe = JavaUtils.javaVmExecutableFullPath(command.javaHome);
        File javaVmFile = new File(javaVmExe);
        // Java VM executable should exist and should be executable.
        if (!javaVmFile.canExecute()) {
            LOGGER.log(Level.INFO, METHOD, "noJavaVMExe", javaVmExe);
            return null;
        }
        return javaVmExe;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandJava command;

    /** Java VM executable. */
    final String javaVMExe;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * command line asadmin interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     * @param query   Query string for this command.
     */
    public RunnerJava(final PayaraServer server, final Command command,
            final String query) {
        super(server, command, null, query);
        final String METHOD = "init";
        if (command instanceof CommandJava) {
            this.command = (CommandJava)command;
        } else {
            throw new CommandException(LOGGER.excMsg(METHOD, "noCommandJava"));
        }
        javaVMExe = getJavaVM(server, this.command);
        if (javaVMExe == null) {
            throw new CommandException(LOGGER.excMsg(METHOD, "noJavaVMExe"),
                    new Object[] {this.command.javaHome, server.getName()});
        }
    }

    /**
     * Constructs an instance of administration command executor using
     * command line asadmin interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerJava(final PayaraServer server, final Command command) {
        this(server, command, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Do not send information to the server via HTTP POST by default.
     * <p/>
     * @return <code>true</code> if using HTTP POST to send to server
     *         or <code>false</code> otherwise
     */
    /**
     * Do not send information to the server via HTTP POST by default.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     * @return Always returns <code>false</code>.
     */
    @Override
    public boolean getDoOutput() {
        return false;
    }

    /**
     * Inform whether this runner implementation accepts gzip format.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     * @return Always returns <code>false</code>.
     */
    @Override
    public boolean acceptsGzip() {
        return false;
    }

    /**
     * Build Payara administration interface command URL.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     * @return Always returns <code>null</code>.
     * @throws <code>CommandException</code> if there is a problem with building
     *         command URL.
     */
    @Override
    protected String constructCommandUrl() throws CommandException {
        return null;
    }

    /**
     * The type of HTTP method used to access administration interface command.
     * <p/>
     * This method makes no sense for this runner.
     * <p/>
     * @return Always returns <code>null</code>.
     */
    @Override
    protected String getRequestMethod() {
        return null;
    }

    /**
     * Handle sending data to server using HTTP administration command interface.
     * <p/>
     * Does nothing. This method makes no sense for this runner.
     */
    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method helpers                                  //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Verifies if Java VM version is supported by provided Payara server.
     * <p/>
     * @return Value of <code>true</code> when Java VM executable version
     *         is known and supported by provided Payara server
     *         or <code>false</code> otherwise.
     */
    boolean verifyJavaVM() {
        final String METHOD = "verifyJavaVM";
        // Java VM executable version must be known.
        JavaUtils.JavaVersion javaVersion
                = JavaUtils.javaVmVersion(new File(javaVMExe));
        if (javaVersion == null) {
            LOGGER.log(Level.INFO, METHOD, "unknown", javaVMExe);
            return false;
        } else {
            LOGGER.log(Level.FINEST, METHOD, "info",
                    new Object[] {javaVMExe, javaVersion.toString()});
        }
        // Java VM executable version must be supported by provided server.
        Set<JavaSEPlatform> platforms =
                getJavaSEConfig(getServerConfig(server)).platforms();
        if (!platforms.contains(javaVersion.toPlatform())) {
            LOGGER.log(Level.INFO, METHOD, "unsupported",
                    new Object[] {javaVMExe, server.getName()});
            return false;
        }
        return true;
    }

    /**
     * Prepare Java VM environment for Payara server execution.
     * <p/>
     * @param env     Process builder environment <code>Map</code>.
     * @param command Payara Server Administration Command Entity.
     */
    static void setJavaEnvironment(Map<String,String> env,
            CommandJava command) {
        // Java VM home stored in AS environment variables JAVA_HOME and AS_JAVA
        env.put(JavaUtils.JAVA_HOME_ENV, command.javaHome);
        env.put(ServerUtils.AS_JAVA_ENV, command.javaHome);
    }

    /**
     * Set server process current directory to domain directory if exists.
     * <p/>
     * No current directory will be set when domain directory does not exist.
     * <p/>
     * @param pb Process builder object where to set current directory.
     */
    void setProcessCurrentDir(ProcessBuilder pb) {
        final String METHOD = "setProcessCurrentDir";
        String domainsFolder = server.getDomainsFolder();
        if (domainsFolder != null && domainsFolder.length() > 0) {
            File currentDir = new File(
                    ServerUtils.getDomainConfigPath(domainsFolder));
            if (currentDir.exists()) {
                LOGGER.log(Level.FINEST, METHOD, "dir",
                        new Object[] {server.getName(), currentDir});
                pb.directory(currentDir);
            }
        }
    }

}
