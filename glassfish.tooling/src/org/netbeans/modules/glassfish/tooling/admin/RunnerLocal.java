/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.admin;

import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.TaskEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;
import org.netbeans.modules.glassfish.tooling.utils.JavaUtils;
import org.netbeans.modules.glassfish.tooling.utils.OsUtils;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;
import org.netbeans.modules.glassfish.tooling.utils.Utils;

/**
 * GlassFish server administration command execution using local file
 * access interface.
 * <p/>
 * Class implements GlassFish server administration functionality trough local
 * file access interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerLocal extends RunnerJava {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish main class to be started when using classpath. */
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
     * GlassFish admin command result containing process information.
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
     * @param server  GlassFish server entity object.
     * @param command GlassFish Server Administration Command Entity.
     */
    public RunnerLocal(GlassFishServer server, Command command) {
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
     * Prepare Java VM options for Glassfish server execution.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish Server Administration Command Entity.
     * @return Java VM options for Glassfish server execution
     *         as <cpde>String</code>.
     */
    private static String buildJavaOptions(GlassFishServer server,
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
                    ServerUtils.GFV3_JAR_MATCHER);
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
        // Add Glassfish specific options.
        if (command.glassfishArgs != null
                && command.glassfishArgs.length() > 0) {
            sb.append(command.glassfishArgs);
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
     * GlassFish server administration command execution call.
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
