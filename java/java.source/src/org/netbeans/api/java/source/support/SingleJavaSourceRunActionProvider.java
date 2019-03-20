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
import javax.lang.model.SourceVersion;
import org.openide.filesystems.FileObject;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.JavaNode;
import org.netbeans.spi.project.ActionProvider;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class provides support to run a single Java file without a parent
 * project (JEP-330).
 *
 * @author Sarvesh Kesharwani
 */
@ServiceProvider(service = ActionProvider.class)
public class SingleJavaSourceRunActionProvider implements ActionProvider {

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_RUN_SINGLE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        FileObject fileObject = getJavaFileWithoutProjectFromLookup(context);
        String filePath = fileObject.getPath();
        String arguments = NbPreferences.forModule(SingleJavaSourceRunActionProvider.class).get(fileObject.getName() + "_SINGLE_FILE_RUN_ARGUMENTS", "");
        String vmOptions = NbPreferences.forModule(SingleJavaSourceRunActionProvider.class).get(fileObject.getName() + "_SINGLE_FILE_RUN_VM_OPTIONS", "");
        ExecutionDescriptor descriptor = new ExecutionDescriptor().controllable(true).frontWindow(true).
                preExecution(null).postExecution(null);
        String javaPath = "\"" + System.getProperty("java.home") + "\\bin\\java\"";
        ExecutionService exeService = ExecutionService.newService(
                new RunProcess(javaPath + " " + vmOptions + " " + filePath + " " + arguments),
                descriptor, "Running Single Java File");
        Future<Integer> exitCode = exeService.run();
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        // JEP-330 is supported only on JDK-11 and above.
        try {
            SourceVersion r11 = SourceVersion.valueOf("RELEASE_11");
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return getJavaFileWithoutProjectFromLookup(context) != null;
    }

    private FileObject getJavaFileWithoutProjectFromLookup(Lookup lookup) {
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if (p == null && dObj.getNodeDelegate() instanceof JavaNode) {
                return fObj;
            }
        }
        return null;
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
