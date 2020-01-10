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
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Arunava Sinha
 */
class DebugProcess extends Process {

    private static final Logger LOG = Logger.getLogger(DebugProcess.class.getName());
    Process p;
    private Pattern JVM_ARGS_PATTERN = Pattern.compile("(.*) (--source[ ]* [0-9]*)(.*)");  //NOI18N

    public Process setupProcess(CompileProcess compileProcess, FileObject fileObject) {
        try {

            if (compileProcess.exitValue() > 0) {
                return compileProcess;
            }
            List<String> commandsList = new ArrayList<>();
            if (Utilities.isUnix()) {
                commandsList.add("bash");
                commandsList.add("-c");
            }
            File javaBinPath = new File(new File(System.getProperty("java.home")), "bin"); //NOI18N
            String javaPath = javaBinPath.getAbsolutePath() + "//java"; //NOI18N

            Object argumentsObject = fileObject.getAttribute(SingleSourceFileUtil.FILE_ARGUMENTS);
            String arguments = argumentsObject != null ? (String) argumentsObject : ""; //NOI18N

            Object vmOptionsObj = fileObject.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS);
            String vmOptions = vmOptionsObj != null ? (String) vmOptionsObj : ""; //NOI18N

            //filtering out --source param from VM option
            Matcher m1 = JVM_ARGS_PATTERN.matcher(vmOptions);

            while (m1.find()) {
                String group1 = m1.group(1);
                String group3 = m1.group(3);
                vmOptions = group1 + group3;
            }

            String JavaDebugParams = " " + vmOptions + " -agentlib:jdwp=transport=dt_socket,address=" + ",server=y "; //NOI18N
            commandsList.add(javaPath + JavaDebugParams + "-cp " + fileObject.getParent().getPath() + " " + fileObject.getName() + " " + arguments); //NOI18N

            ProcessBuilder runFileProcessBuilder = new ProcessBuilder(commandsList);
            runFileProcessBuilder.directory(new File(System.getProperty("user.home"))); //NOI18N
            runFileProcessBuilder.redirectErrorStream(true);
            runFileProcessBuilder.redirectOutput();

            p = runFileProcessBuilder.start();

        } catch (IOException ex) {
            LOG.log(
                    Level.WARNING,
                    "Could not get InputStream of Run Process"); //NOI18N
        }
        return p;
    }

    @Override
    public OutputStream getOutputStream() {
        PrintStream p1 = new PrintStream(p.getOutputStream(), true);
        p1.append("running debug process......... ");
        p1.flush();
        return p1;
    }

    @Override
    public InputStream getInputStream() {
        return p.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return p.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return p.waitFor();
    }

    @Override
    public int exitValue() {
        return p.exitValue();
    }

    @Override
    public boolean isAlive() {
        return p.isAlive();
    }

    @Override
    public Process destroyForcibly() {
        p.destroyForcibly();
        return this;
    }

    @Override
    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        return p.waitFor(timeout, unit);
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
