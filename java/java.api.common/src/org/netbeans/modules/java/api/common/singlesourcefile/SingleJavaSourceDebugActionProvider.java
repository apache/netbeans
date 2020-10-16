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

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * This class provides support to debug a single Java file without a parent
 * project
 *
 * @author Arunava Sinha
 */
@ServiceProvider(service = ActionProvider.class)
public final class SingleJavaSourceDebugActionProvider implements ActionProvider {

    private static final Logger LOG = Logger.getLogger(SingleJavaSourceDebugActionProvider.class.getName());
    private static final String CUSTOM_ERR_MSG = "Error on Compilation. Please verify Source code and Compiler VM Options:\n"; //NOI18N
    private static final RequestProcessor BACKGROUND = new RequestProcessor(SingleJavaSourceDebugActionProvider.class.getName(), 100, false, false);
    private static final String IONAME = "Debugging Single Java File"; //NOI18N

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_DEBUG_SINGLE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {

        FileObject fileObject = SingleSourceFileUtil.getJavaFileWithoutProjectFromLookup(context);
        if (fileObject == null) {
            return;
        }

        ExecutionDescriptor descriptor = new ExecutionDescriptor().controllable(true).frontWindow(true).
                preExecution(null).postExecution(null);

        ExecutionService exeService = ExecutionService.newService(
                new Callable<Process>() {
            @Override
            public Process call() {
                CompileProcess compileProcess = new CompileProcess();
                InputOutput io = IOProvider.getDefault().getIO(IONAME, false);

                Process compilePreProcess = compileProcess.setupProcess(fileObject);
                try {
                    int processExitCode = compilePreProcess.waitFor();
                    if (processExitCode != 0) {
                        io.getOut().append(CUSTOM_ERR_MSG);
                        return compilePreProcess;
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                JPDAStart s = new JPDAStart(io, fileObject);
                try {
                    String port = s.execute();
                    DebugProcess debugProcess = new DebugProcess();
                    Process p = debugProcess.setupProcess(fileObject, port);
                    return p;
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                }
                return compilePreProcess;
            }
        }, descriptor, IONAME);

        exeService.run();
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return SingleSourceFileUtil.getJavaFileWithoutProjectFromLookup(context) != null;
    }
}
