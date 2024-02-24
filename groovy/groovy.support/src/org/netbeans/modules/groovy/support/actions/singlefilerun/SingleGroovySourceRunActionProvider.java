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
package org.netbeans.modules.groovy.support.actions.singlefilerun;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * This implementation provides support to run a single Groovy file without
 * a parent  project. 
 * 
 * @author Petr Pisl
 */
@ServiceProvider(service = ActionProvider.class)
public class SingleGroovySourceRunActionProvider implements ActionProvider {

    private static final String GROOVY_EXTENSION = "groovy";  //NOI18N
    static final Logger LOG = Logger.getLogger(JPDAStart.class.getPackage().getName());
    
    @Override
    public String[] getSupportedActions() {
        return new String[]{
            ActionProvider.COMMAND_RUN_SINGLE,
            ActionProvider.COMMAND_DEBUG_SINGLE
        };
    }

    @NbBundle.Messages({
        "CTL_SingleGroovyFile=Running Single Groovy File",
        "CTL_IsInProject=File is in a project, can not be run single"
    })
    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        FileObject fileObject = getGroovyFile(context);
        if (!isSingleSourceFile(fileObject)) {
            StatusDisplayer.getDefault().setStatusText(Bundle.CTL_IsInProject(), StatusDisplayer.IMPORTANCE_ANNOTATION);
            return;
        }
        InputOutput io = IOProvider.getDefault().getIO(Bundle.CTL_SingleGroovyFile(), false);
        ActionProgress progress = ActionProgress.start(context);
        ExecutionDescriptor descriptor = new ExecutionDescriptor().
                controllable(true).
                frontWindow(true).
                preExecution(null).
                inputOutput(io).
                postExecution((exitCode) -> {
                    progress.finished(exitCode == 0);
                });
        JPDAStart jpdaStart = ActionProvider.COMMAND_DEBUG_SINGLE.equals(command) ?
                new JPDAStart(io, fileObject) : null;
        LaunchProcess process = new LaunchProcess(fileObject, jpdaStart);
        ExecutionService exeService = ExecutionService.newService(
                process,
                descriptor, Bundle.CTL_SingleGroovyFile());
        exeService.run();
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return isSingleSourceFile(getGroovyFile(context));
    }

    private static boolean isSingleSourceFile(FileObject fileObject) {
        if (fileObject == null) {
            return false;
        }
        Project p = FileOwnerQuery.getOwner(fileObject);
        return p == null;
    }
    
    private static FileObject getGroovyFile(Lookup lookup) {
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            if (GROOVY_EXTENSION.equalsIgnoreCase(fObj.getExt())) {
                return fObj;
            }
        }
        for (FileObject fObj : lookup.lookupAll(FileObject.class)) {
            if (GROOVY_EXTENSION.equalsIgnoreCase(fObj.getExt())) {
                return fObj;
            }
        }
        return null;
    }

    private static class LaunchProcess implements Callable<Process> {

        private final FileObject fileObject;
        private final JPDAStart startDebug;

        public LaunchProcess(FileObject file, JPDAStart startDebug) {
            this.fileObject = file;
            this.startDebug = startDebug;
        }

        @Override
        public Process call() throws Exception {
            if (startDebug != null) {
                return setupProcess(startDebug.execute());
            }
            return setupProcess(null);
        }

        private Process setupProcess(String port) throws InterruptedException {
            List<String> commandList = new ArrayList<>();
            FileObject java = JavaPlatformManager.getDefault().getDefaultPlatform().findTool("java"); //NOI18N
            File groovyJar = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-libs-groovy.jar", null, false); //NOI18N

            // the command should be like java -cp .:paht_to_groovy_jars groovy.ui.GroovyMain  path_to_script"
            commandList.add(FileUtil.toFile(java).getAbsolutePath()); // NOI18N
            
            if (port != null) {
                commandList.add("-agentlib:jdwp=transport=dt_socket,address=" + port + ",server=n"); //NOI18N
            }
            commandList.add("-cp"); // NOI18N
            // TODO can we add here some heuristic, from which folder and appropriate path the script can be run?
            // For example we can look for the `src` or `src/groovy` fodler, from which the script can be probably run. 
            commandList.add(String.join(File.pathSeparator, ".", groovyJar.getAbsolutePath()));    //NOI18N

            commandList.add("groovy.ui.GroovyMain");    //NOI18N
            commandList.add(fileObject.getNameExt());
            ProcessBuilder pb = new ProcessBuilder(commandList);
            pb.directory(FileUtil.toFile(fileObject.getParent())).redirectErrorStream(true)
                    .redirectOutput();
            try {
                return pb.start();
            } catch (IOException ex) {
                LOG.info("Could not get InputStream of Run Process");   //NOI18N
            }
            return null;
        }

    }

    
}
