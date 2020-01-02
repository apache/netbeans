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
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

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

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_DEBUG_SINGLE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {

        FileObject fileObject = SingleSoureFileUtil.getJavaFileWithoutProjectFromLookup(context);
        if (fileObject == null) {
            return;
        }

        DebugProcess debuggerProcess = invokeDebugActionHelper(command, fileObject);
        ExecutionDescriptor descriptor = getDescriptor(fileObject, debuggerProcess).controllable(true).frontWindow(true).frontWindowOnError(true);
        CompileProcess compileProcess = new CompileProcess(fileObject);
        descriptor = descriptor.preExecution(() -> {

            final Process compilePreProcess = compileProcess.setupProcess();
            try {
                int compilationResult = compilePreProcess.waitFor();
                if (compilationResult != 0) {

                    InputOutput log = IOProvider.getDefault().getIO("Debugging Single Java File", false);  //NOI18N
                    OutputWriter out = log.getOut();
                    out.append(CUSTOM_ERR_MSG);
                }

            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).rerunCondition(new ExecutionDescriptor.RerunCondition() {
            @Override
            public void addChangeListener(ChangeListener listener) {

            }

            @Override
            public void removeChangeListener(ChangeListener listener) {

            }

            @Override
            public boolean isRerunPossible() {
                return false;
            }
        });

        Callable<Process> callable = () -> debuggerProcess.setupProcess(compileProcess, fileObject);
        ExecutionService exeService = ExecutionService.newService(
                callable,
                descriptor, "Debuging Single Java File");

        Future<Integer> exitCode = exeService.run();

    }

    private ExecutionDescriptor getDescriptor(final FileObject filObj, DebugProcess debuggerProcess) {
        LineConvertorFactory factory = new LineConvertorFactory() {
            @Override
            public LineConvertor newLineConvertor() {
                return new DebugSingleSourceOutputConvertor(filObj, debuggerProcess);
            }
        };
        ExecutionDescriptor descriptor = new ExecutionDescriptor().outConvertorFactory(factory);

        return descriptor;

    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return SingleSoureFileUtil.getJavaFileWithoutProjectFromLookup(context) != null;
    }

    final DebugProcess invokeDebugActionHelper(String command, FileObject fileObject) {
        String filePath = fileObject.getPath();
        String filePathWithoutExt = filePath.substring(0, filePath.lastIndexOf("."));
        Object argumentsObject = fileObject.getAttribute(SingleSoureFileUtil.FILE_ARGUMENTS);
        String arguments = argumentsObject != null ? (String) argumentsObject : "";

        Object debugVmOptionsObj = fileObject.getAttribute(SingleSoureFileUtil.FILE_VM_OPTIONS);
        String debugVmOptions = debugVmOptionsObj != null ? " -XX:+IgnoreUnrecognizedVMOptions " + (String) debugVmOptionsObj : "";

        return new DebugProcess();
    }

}
