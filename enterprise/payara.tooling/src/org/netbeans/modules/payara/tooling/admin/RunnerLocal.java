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

import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.TaskEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.utils.JavaUtils;
import org.netbeans.modules.payara.tooling.utils.OsUtils;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.utils.Utils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara server administration command execution using local file
 * access interface.
 * <p/>
 * Class implements Payara server administration functionality trough local
 * file access interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerLocal extends RunnerJava {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara main class to be started when using classpath. */
    private static final String MAIN_CLASS
            = "com.sun.enterprise.glassfish.bootstrap.ASMain";


    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Holding data for command execution. */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    final CommandStartDAS command;

    /** Argument format. */
    private String arguments;

    /**
     * Payara admin command result containing process information.
     * <p/>
     * Result instance life cycle is started with submitting task into
     * <code>ExecutorService</code>'s queue. method <code>call()</code>
     * is responsible for correct <code>TaskState</code> and value
     * handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultProcess result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using local
     * file access interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara Server Administration Command Entity.
     */
    public RunnerLocal(PayaraServer server, Command command) {
        super(server, command);
        this.command = (CommandStartDAS)command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

   /**
     * Create <code>ResultString</code> object corresponding
     * to <code>String</code>command execution value to be returned.
     */
    @Override
    protected Result createResult() {
        return result = new ResultProcess();
    }

    /**
     * Reads response from server and stores internally.
     * <p/>
     * @param in Stream to read data from.
     * @return Always returns <code>false</code>.
     * @throws CommandException in case of stream error.
     */
    @Override
    protected boolean readResponse(InputStream in, HttpURLConnection hconn) {
        return false;
    }

    /**
     * Extracts result value from internal storage.
     * <p/>
     * @return Always returns <code>false</code>.
     */
    @Override
    protected boolean processResponse() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Fake Getters                                                           //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method helpers                                  //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Prepare Java VM options for Payara server execution.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara Server Administration Command Entity.
     * @return Java VM options for Payara server execution
     *         as <cpde>String</code>.
     */
    private static String buildJavaOptions(PayaraServer server,
            CommandStartDAS command) {
        // Java VM options
        StringBuilder sb = new StringBuilder();
        boolean isClasspath = command.classPath != null &&
                command.classPath.length() > 0;
        boolean isOptions = command.javaOpts != null &&
                command.javaOpts.length() > 0;
        sb.append(JavaUtils.VM_CLASSPATH_OPTION).append(' ');
        // Add classpath if exists.
        if (isClasspath) {
            sb.append(command.classPath);
        } else {
            File bootstrapJar = ServerUtils.getJarName(server.getServerHome(),
                    ServerUtils.GF_JAR_MATCHER);
            sb.append(Utils.quote(bootstrapJar.getAbsolutePath()));
        }
        sb.append(' ');
        // Add Java VM options.
        if (isOptions) {
            sb.append(command.javaOpts);
            sb.append(' ');
        }
        // Add startup main class or jar.
        sb.append(MAIN_CLASS);
        sb.append(' ');
        // Add Payara specific options.
        if (command.payaraArgs != null
                && command.payaraArgs.length() > 0) {
            sb.append(command.payaraArgs);
        }
       return sb.toString();
    }

    /**
     * Set server process current directory to domain directory if exists.
     * <p/>
     * No current directory will be set when domain directory does not exist.
     * <p/>
     * @param pb Process builder object where to set current directory.
     */
    @Override
    void setProcessCurrentDir(ProcessBuilder pb) {
        if (command.domainDir != null && command.domainDir.length() > 0) {
            File currentDir = new File(
                    ServerUtils.getDomainConfigPath(command.domainDir));
            if (currentDir.exists()) {
                Logger.log(Level.FINEST,
                        "Setting {0} process current directory to {1}",
                        new Object[]{server.getName(), command.domainDir});
                pb.directory(currentDir);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Payara server administration command execution call.
     * This is an entry point from <code>executor<code>'s serialization queue.
     * <p/>
     * Attempts to start local GalssFish DAS directly using <code>java</code>
     * executable.
     * <p/>
     * @return Task execution state.
     */
    @Override
    public Result call() {
        String javaVmExe = JavaUtils.javaVmExecutableFullPath(command.javaHome);
        File javaVmFile = new File(javaVmExe);
        // Java VM executable should exist.
        if (!javaVmFile.exists()) {
            Logger.log(Level.INFO,
                    "Java VM {0} executable for {1} was not found",
                    new Object[] {javaVmFile.getAbsolutePath(),
                        server.getName()});
            return handleStateChange(TaskState.FAILED,
                    TaskEvent.NO_JAVA_VM, command.getCommand(),
                    server.getName());
        }
        // Java VM should be 1.6.0_0 or greater.
        JavaUtils.JavaVersion javaVersion = JavaUtils.javaVmVersion(javaVmFile);
        Logger.log(Level.FINEST, "Java VM {0} executable version {1}",
                new Object[] {javaVmFile.getAbsolutePath(),
                    javaVersion != null ? javaVersion.toString() : "null"});
        if (javaVersion == null || javaVersion.comapreTo(
                new JavaUtils.JavaVersion(1, 6, 0, 0)) == -1) {
            // Display warning message but try to run server anyway.
            Logger.log(Level.INFO,
                    "Java VM {0} executable version {1} can't be used with {2} "
                    + "but trying to start server anyway.",
                    new Object[] {javaVmFile.getAbsolutePath(),
                        javaVersion != null ? javaVersion.toString() : "null",
                        server.getName()});
        }
        String allArgs = buildJavaOptions(server, command);
        String[] args = OsUtils.parseParameters(javaVmExe, allArgs);
        Logger.log(Level.FINEST,
                "Starting {0} using Java VM {1} and arguments {2}",
                new Object[]{server.getName(), javaVmExe, allArgs});
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Process process;
        setProcessCurrentDir(pb);
        setJavaEnvironment(pb.environment(), command);
        try {
            process = pb.start();
        } catch (IOException ex) {
            return handleStateChange(TaskState.FAILED,
                    TaskEvent.JAVA_VM_EXEC_FAILED, command.getCommand(),
                    server.getName());
        }
        result.value = new ValueProcess(
                javaVmFile.getAbsolutePath(), allArgs, process);
        return handleStateChange(TaskState.COMPLETED, TaskEvent.CMD_COMPLETED,
                    command.getCommand(), server.getName());
    }

}
