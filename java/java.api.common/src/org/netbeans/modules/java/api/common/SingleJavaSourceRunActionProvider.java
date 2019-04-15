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
package org.netbeans.modules.java.api.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class provides support to run a single Java file without a parent
 * project (JEP-330).
 *
 * @author Sarvesh Kesharwani
 */
@ServiceProvider(service = ActionProvider.class)
public class SingleJavaSourceRunActionProvider implements ActionProvider {
    
    private static final String FILE_ARGUMENTS = "single_file_run_arguments"; //NOI18N
    private static final String FILE_VM_OPTIONS = "single_file_vm_options"; //NOI18N

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_RUN_SINGLE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        FileObject fileObject = getJavaFileWithoutProjectFromLookup(context);
        if (fileObject == null) 
            return;
        String filePath = fileObject.getPath();
        Object argumentsObject = fileObject.getAttribute(FILE_ARGUMENTS);
        String arguments = argumentsObject != null ? (String) argumentsObject : "";
        Object vmOptionsObject = fileObject.getAttribute(FILE_VM_OPTIONS);
        String vmOptions = vmOptionsObject != null ? (String) vmOptionsObject : "";
        ExecutionDescriptor descriptor = new ExecutionDescriptor().controllable(true).frontWindow(true).
                preExecution(null).postExecution(null);
        List<String> commandsList = new ArrayList<>();
        if (Utilities.isUnix()) {
            commandsList.add("bash");
            commandsList.add("-c");
        }
        File javaPathFile = new File(new File(new File(System.getProperty("java.home")), "bin"), "java");
        String javaPath = "\"" + javaPathFile.getAbsolutePath() + "\"";
        commandsList.add(javaPath + " " + vmOptions + " " + filePath + " " + arguments);
        ExecutionService exeService = ExecutionService.newService(
                new RunProcess(commandsList),
                descriptor, "Running Single Java File");
        Future<Integer> exitCode = exeService.run();
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        // JEP-330 is supported only on JDK-11 and above.
        String javaVersion = System.getProperty("java.specification.version");
        if (javaVersion.startsWith("1.")) {
            javaVersion = javaVersion.substring(2);
        }
        int version = Integer.parseInt(javaVersion);
        return version >= 11;
    }

    private FileObject getJavaFileWithoutProjectFromLookup(Lookup lookup) {
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if (p == null && fObj.getExt().equalsIgnoreCase("java")) {
                return fObj;
            }
        }
        return null;
    }

    private class RunProcess implements Callable<Process> {

        private final String dirPath;
        private final List<String> commandsList;

        public RunProcess(String command, String dirPath) {
            this.dirPath = dirPath;
            commandsList = new ArrayList<>();
            commandsList.add(command);
        }

        public RunProcess(String command) {
            commandsList = new ArrayList<>();
            commandsList.add(command);
            this.dirPath = System.getProperty("user.home");
        }

        public RunProcess(List<String> commandsList) {
            this.commandsList = commandsList;
            this.dirPath = System.getProperty("user.home");
        }

        public Process call() throws Exception {
            ProcessBuilder runFileProcessBuilder = new ProcessBuilder(commandsList);
            runFileProcessBuilder.directory(new File(dirPath));
            runFileProcessBuilder.redirectErrorStream(true);
            return runFileProcessBuilder.start();
        }
    }
        
}
