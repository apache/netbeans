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
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

final class LaunchProcess implements Callable<Process> {
    private static final Pattern JVM_ARGS_PATTERN = Pattern.compile("(.*) (--source[ ]* [0-9]*)(.*)");  //NOI18N

    private final FileObject fileObject;
    private final JPDAStart start;

    LaunchProcess(FileObject fileObject, JPDAStart start) {
        this.fileObject = fileObject;
        this.start = start;
    }

    @Override
    public Process call() throws Exception {
        if (start != null) {
            return setupProcess(start.execute());
        } else {
            return setupProcess(null);
        }
    }

    private Process setupProcess(String port) throws InterruptedException {
        try {
            boolean compile = SingleSourceFileUtil.findJavaVersion() < 11 || SingleSourceFileUtil.hasClassSibling(fileObject);

            if (compile) {
                Process p = SingleSourceFileUtil.compileJavaSource(fileObject);
                if (p.waitFor() != 0) {
                    return p;
                }
            }

            List<String> commandsList = new ArrayList<>();

            FileObject java = JavaPlatformManager.getDefault().getDefaultPlatform().findTool("java"); //NOI18N
            File javaFile = FileUtil.toFile(java);
            String javaPath = javaFile.getAbsolutePath();

            Object argumentsObject = fileObject.getAttribute(SingleSourceFileUtil.FILE_ARGUMENTS);
            String arguments = argumentsObject != null ? ((String) argumentsObject).trim() : ""; // NOI18N

            Object vmOptionsObj = fileObject.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS);
            String vmOptions = vmOptionsObj != null ? ((String) vmOptionsObj) : ""; // NOI18N

            commandsList.add(javaPath);
            if (!vmOptions.isEmpty()) {
                //filtering out --source param from VM option
                Matcher m1 = JVM_ARGS_PATTERN.matcher(vmOptions);
                while (m1.find()) {
                    String group1 = m1.group(1);
                    String group3 = m1.group(3);
                    vmOptions = group1 + group3;
                }
                commandsList.addAll(Arrays.asList(vmOptions.split(" ")));  //NOI18N
            }
            if (port != null) {
                commandsList.add("-agentlib:jdwp=transport=dt_socket,address=" + port + ",server=n"); //NOI18N
            }
            if (compile) {
                commandsList.add("-cp");
                commandsList.add(FileUtil.toFile(fileObject.getParent()).toString());
                commandsList.add(fileObject.getName());
            } else {
                commandsList.add(fileObject.getNameExt());
            }

            if (!arguments.isEmpty()) {
                commandsList.addAll(Arrays.asList(arguments.split(" ")));  //NOI18N
            }

            ProcessBuilder runFileProcessBuilder = new ProcessBuilder(commandsList);
            runFileProcessBuilder.directory(FileUtil.toFile(fileObject.getParent())); //NOI18N
            runFileProcessBuilder.redirectErrorStream(true);
            runFileProcessBuilder.redirectOutput();

            return runFileProcessBuilder.start();
        } catch (IOException ex) {
            SingleSourceFileUtil.LOG.log(
                    Level.WARNING,
                    "Could not get InputStream of Run Process"); //NOI18N
        }
        return null;
    }
}
