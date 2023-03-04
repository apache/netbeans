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

package org.netbeans.modules.j2ee.earproject.model;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.javaee.project.api.ear.EarDDGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Default implemetation of the SPI for <code>MetadataModel</code>.
 * <p>
 * This class uses {@link org.netbeans.api.project.ProjectManager#mutex() write mutex}
 * because it reads metadata model which should to be immutable during reading.
 * @author Tomas Mysik
 */
public class ApplicationMetadataModelImpl implements MetadataModelImplementation<ApplicationMetadata> {
    
    private final Application root;
    private final ApplicationMetadata metadata;
    
    /**
     * Constructor with all properties.
     * @param earProject EAR project instance for which corresponding model is created.
     */
    public ApplicationMetadataModelImpl(final EarProject earProject) {
        
        Application ddRoot = null;
        FileObject ddFO = getDeploymentDescriptor(earProject);
        if (ddFO != null) {
            try {
                ddRoot = DDProvider.getDefault().getDDRoot(ddFO);
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, null, ioe);
            }
        }
        if (ddRoot != null) {
            root = ddRoot;
        } else {
            // see javadoc of this class
            root = ProjectManager.mutex().writeAccess(new Mutex.Action<Application>() {
                public Application run() {
                    return new ApplicationImpl(earProject);
                }
            });
        }
        metadata = new ApplicationMetadataImpl(root);
    }

    public <R> R runReadAction(final MetadataModelAction<ApplicationMetadata, R> action) throws MetadataModelException, IOException {
        try {
            // see javadoc of this class
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<R>() {
                public R run() throws Exception {
                    enterRunReadAction();
                    try {
                        return action.run(metadata);
                    } finally {
                        leaveRunReadAction();
                    }
                }
            });
        } catch (MutexException mutexException) {
            throw new MetadataModelException(mutexException.getException());
        }
    }
    
    public boolean isReady() {
        return true;
    }
    
    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<ApplicationMetadata, R> action) throws IOException {
        return new SimpleFuture<R>(runReadAction(action));
    }
    
    private FileObject getDeploymentDescriptor(final EarProject earProject) {
        FileObject ddFO = earProject.getAppModule().getDeploymentDescriptor();
        if (ddFO == null && DDHelper.isApplicationXMLCompulsory(earProject)) {
            ddFO = EarDDGenerator.setupDD(earProject, true);
        }
        return ddFO;
    }
    
    private void enterRunReadAction() {
        Application application = metadata.getRoot();
        if (application instanceof ApplicationImpl) {
            ((ApplicationImpl) application).enterRunReadAction();
        }
    }

    private void leaveRunReadAction() {
        Application application = metadata.getRoot();
        if (application instanceof ApplicationImpl) {
            ((ApplicationImpl) application).leaveRunReadAction();
        }
    }
    
    private static final class SimpleFuture<R> implements Future<R> {
        
        private volatile R result;
        
        public SimpleFuture(R result) {
            this.result = result;
        }
        
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
        
        public boolean isCancelled() {
            return false;
        }
        
        public boolean isDone() {
            return true;
        }
        
        public R get() throws InterruptedException, ExecutionException {
            return result;
        }
        
        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
