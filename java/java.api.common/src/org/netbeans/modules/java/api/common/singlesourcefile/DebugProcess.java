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
import java.util.ArrayList;
import java.util.List;
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
class DebugProcess {

    private static final Logger LOG = Logger.getLogger(DebugProcess.class.getName());
    private final Pattern JVM_ARGS_PATTERN = Pattern.compile("(.*) (--source[ ]* [0-9]*)(.*)");  //NOI18N

    public Process setupProcess(FileObject fileObject, String port) {
        try {

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

            String JavaDebugParams = " " + vmOptions + " -Xdebug -Xrunjdwp:transport=dt_socket,address=" + port + ",server=n "; //NOI18N
            commandsList.add(javaPath + JavaDebugParams + "-cp " + fileObject.getParent().getPath() + " " + fileObject.getName() + " " + arguments); //NOI18N

            ProcessBuilder runFileProcessBuilder = new ProcessBuilder(commandsList);
            runFileProcessBuilder.directory(new File(System.getProperty("user.home"))); //NOI18N
            runFileProcessBuilder.redirectErrorStream(true);
            runFileProcessBuilder.redirectOutput();

            return runFileProcessBuilder.start();

        } catch (IOException ex) {
            LOG.log(
                    Level.WARNING,
                    "Could not get InputStream of Run Process"); //NOI18N
        }
        return null;
    }
}
