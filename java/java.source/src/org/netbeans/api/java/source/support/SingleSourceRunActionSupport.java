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
package org.netbeans.api.java.source.support;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.openide.filesystems.FileObject;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.NbPreferences;
/**
 *
 * @author Sarvesh Kesharwani
 */
public class SingleSourceRunActionSupport {
    
    private String[] supportedActions = {ActionProvider.COMMAND_RUN_SINGLE};
    
    public boolean isActionSupported (String action) {
        for (String supportedAction : supportedActions) {
            if (supportedAction.equalsIgnoreCase(action))
                return true;
        }
        return false;
    }

    public void invokeAction(String command, FileObject fileObject) throws IllegalArgumentException {
        String filePath = fileObject.getPath();
        String arguments = NbPreferences.forModule(SingleSourceRunActionSupport.class).get(fileObject.getName() + "_SINGLE_FILE_RUN_ARGUMENTS", "");
        String vmOptions = NbPreferences.forModule(SingleSourceRunActionSupport.class).get(fileObject.getName() + "_SINGLE_FILE_RUN_VM_OPTIONS", "");
        ExecutionDescriptor descriptor = new ExecutionDescriptor().controllable(true).frontWindow(true).
                preExecution(null).postExecution(null);
        String javaPath = "\"" + System.getProperty("java.home") + "\\bin\\java\"";
        ExecutionService exeService = ExecutionService.newService(
                new RunProcess(javaPath + " " + vmOptions + " " + filePath + " " + arguments),
                descriptor, "Running Single Java File");
        Future<Integer> exitCode = exeService.run();
    }

    private class RunProcess implements Callable<Process> {

        private final String command;
        private final String dirPath;

        public RunProcess(String command, String dirPath) {
            this.command = command;
            this.dirPath = dirPath;
        }
        
        public RunProcess(String command) {
            this.command = command;
            this.dirPath = System.getProperty("user.home");
        }

        public Process call() throws Exception {
            ProcessBuilder runFileProcessBuilder = new ProcessBuilder(command);
            runFileProcessBuilder.directory(new File(dirPath));
            runFileProcessBuilder.redirectErrorStream(true);
            return runFileProcessBuilder.start();
        }
    }
    
}
