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

package org.netbeans.modules.projectimport.eclipse.core;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public class ProjectOpenHookImpl extends ProjectOpenedHook{

    private static final RequestProcessor PROJ_OPEN_HOOK_RESYNCHRONIZER = new RequestProcessor("Eclipse.Resynchronizer"); // NOI18N
    private static RequestProcessor.Task currentTask;
    
    public ProjectOpenHookImpl() {}
    
    @Override
    protected synchronized void projectOpened() {
        if (currentTask == null)  {
            currentTask = PROJ_OPEN_HOOK_RESYNCHRONIZER.create(new RunnableImpl());
        }
        // coalesce events from multiple project being opened.
        currentTask.schedule(10000);
    }

    @Override
    protected void projectClosed() {
    }

    private static class RunnableImpl implements Runnable {

        public RunnableImpl() {
        }

        public void run() {
            try {
                // coalesce events from multiple project being opened.
                OpenProjects.getDefault().openProjects().get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ProjectOpenHookImpl.class.getName()).log(Level.INFO, null, ex);
            }
            
            new UpdateAllProjects().update(true);
        }
    }

}
