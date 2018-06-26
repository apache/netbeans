/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ear.model;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.Exceptions;
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
    public ApplicationMetadataModelImpl(final Project earProject) {
        
        Application ddRoot = null;
        FileObject ddFO = getDeploymentDescriptor(earProject);
        if (ddFO != null) {
            try {
                ddRoot = DDProvider.getDefault().getDDRoot(ddFO);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        if (ddRoot != null) {
            root = ddRoot;
        } else {
            // see javadoc of this class
            root = ProjectManager.mutex().writeAccess(new Mutex.Action<Application>() {
                @Override
                public Application run() {
                    return new ApplicationImpl(earProject);
                }
            });
        }
        metadata = new ApplicationMetadataImpl(root);
    }

    @Override
    public <R> R runReadAction(final MetadataModelAction<ApplicationMetadata, R> action) throws MetadataModelException, IOException {
        try {
            // see javadoc of this class
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<R>() {
                @Override
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
    
    @Override
    public boolean isReady() {
        return true;
    }
    
    @Override
    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<ApplicationMetadata, R> action) throws IOException {
        return new SimpleFuture(runReadAction(action));
    }
    
    private FileObject getDeploymentDescriptor(final Project earProject) {
        EarProvider impl = earProject.getLookup().lookup(EarProvider.class);
        Ear ear = impl.findEar(earProject.getProjectDirectory());
        FileObject ddFO = ear.getDeploymentDescriptor();
//        if (ddFO == null
//                && EarProjectUtil.isDDCompulsory(earProject)
//                ) {
//            try {
//                ddFO = EarProjectGenerator.setupDD(
//                        ear.getJ2eePlatformVersion(),
//                        ear.getMetaInf(),
//                        earProject,
//                        true);
//            } catch (IOException ioe) {
//                Exceptions.printStackTrace(ioe);
//            }
//        }
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
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
        
        @Override
        public boolean isCancelled() {
            return false;
        }
        
        @Override
        public boolean isDone() {
            return true;
        }
        
        @Override
        public R get() throws InterruptedException, ExecutionException {
            return result;
        }
        
        @Override
        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
