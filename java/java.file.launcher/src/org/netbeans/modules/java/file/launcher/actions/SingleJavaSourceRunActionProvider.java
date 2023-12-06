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
package org.netbeans.modules.java.file.launcher.actions;

import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * This class provides support to run a single Java file without a parent
 * project (JEP-330).
 *
 * @author Sarvesh Kesharwani
 */
@ServiceProvider(service = ActionProvider.class)
public final class SingleJavaSourceRunActionProvider implements ActionProvider {
    @Override
    public String[] getSupportedActions() {
        return new String[]{
            ActionProvider.COMMAND_RUN_SINGLE,
            ActionProvider.COMMAND_DEBUG_SINGLE
        };
    }

    @NbBundle.Messages({
        "CTL_SingleJavaFile=Running Single Java File"
    })
    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        FileObject fileObject = SingleSourceFileUtil.getJavaFileWithoutProjectFromLookup(context);
        if (fileObject == null) 
            return;

        ExplicitProcessParameters params = ExplicitProcessParameters.buildExplicitParameters(context);
        InputOutput io = IOProvider.getDefault().getIO(Bundle.CTL_SingleJavaFile(), false);
        ActionProgress progress = ActionProgress.start(context);
        ExecutionDescriptor descriptor = new ExecutionDescriptor().
            controllable(true).
            frontWindow(true).
            preExecution(null).
            inputOutput(io).
            postExecution((exitCode) -> {
                progress.finished(exitCode == 0);
            });
        LaunchProcess process = invokeActionHelper(io, command, fileObject, params);
        ExecutionService exeService = ExecutionService.newService(
                    process,
                    descriptor, "Running Single Java File");
        Future<Integer> exitCode = exeService.run();
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        FileObject fileObject = SingleSourceFileUtil.getJavaFileWithoutProjectFromLookup(context);
        return fileObject != null;
    }
    
    final LaunchProcess invokeActionHelper (InputOutput io, String command, FileObject fo, ExplicitProcessParameters params) {
        JPDAStart start = ActionProvider.COMMAND_DEBUG_SINGLE.equals(command) ?
                new JPDAStart(io, fo) : null;
        return new LaunchProcess(fo, start, params);
    }
        
}
