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

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class provides support to run a single Java file without a parent
 * project (JEP-330).
 *
 * @author Sarvesh Kesharwani
 */
@ServiceProvider(service = ActionProvider.class)
public final class SingleJavaSourceRunActionProvider implements ActionProvider {

    private final Map<FileObject, Future<Integer>> running = new WeakHashMap<>();
    private volatile boolean rerun = false;

    @Override
    public String[] getSupportedActions() {
        return new String[]{
            ActionProvider.COMMAND_RUN_SINGLE,
            ActionProvider.COMMAND_DEBUG_SINGLE
        };
    }

    private String getTaskName(String command, String fileName){
        if(command == null || command.isEmpty()) return fileName;
        String action = command.contains(".")
                ? command.substring(0, command.indexOf('.'))
                : command;
        String capitalized = action.substring(0, 1).toUpperCase(Locale.ROOT)
                + action.substring(1).toLowerCase(Locale.ROOT);
        String baseName = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : fileName;
        return String.format("%s (%s)", capitalized, baseName);
    }

    @NbBundle.Messages({
        "CTL_SingleJavaFile=Running Single Java File"
    })
    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        LifecycleManager.getDefault().saveAll();
        FileObject fileObject = SingleSourceFileUtil.getJavaFileWithoutProjectFromLookup(context);
        if (fileObject == null)
            return;
        var previous = running.get(fileObject);
        if (previous != null && !previous.isDone()) {
            rerun = true;
            if (previous.cancel(true)) {
                return;
            }
            rerun = false;
        }

        ExplicitProcessParameters params = ExplicitProcessParameters.buildExplicitParameters(context);
        String preferredEncoding = System.getProperty("native.encoding"); // NOI18N
        ExecutionDescriptor descriptor = new ExecutionDescriptor().
            showProgress(true).
            controllable(true).
            frontWindow(true).
            preExecution(null).
            inputVisible(true).
            charset(preferredEncoding != null ? Charset.forName(preferredEncoding) : null).
            postExecution((exitCode) -> {
                if (rerun) {
                    rerun = false;
                    invokeAction(command, context);
                }
            });
        LaunchProcess process = invokeActionHelper(command, fileObject, params);
        ExecutionService exeService = ExecutionService.newService(
                    process,
                    descriptor, this.getTaskName(command, fileObject.getNameExt()));

        Future<Integer> future = exeService.run();
        if (NbPreferences.forModule(JavaPlatformManager.class).getBoolean(SingleSourceFileUtil.GLOBAL_STOP_AND_RUN_OPTION, false)) {
            running.put(fileObject, future);
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        FileObject fileObject = SingleSourceFileUtil.getJavaFileWithoutProjectFromLookup(context);
        return fileObject != null;
    }

    final LaunchProcess invokeActionHelper (String command, FileObject fo, ExplicitProcessParameters params) {
        JPDAStart start = ActionProvider.COMMAND_DEBUG_SINGLE.equals(command) ?
                new JPDAStart(fo) : null;
        return new LaunchProcess(fo, start, params);
    }

}
