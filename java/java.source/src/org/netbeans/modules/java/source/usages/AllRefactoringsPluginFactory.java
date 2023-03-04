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

package org.netbeans.modules.java.source.usages;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavaSourceSupportAccessor;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class, position=90)
public class AllRefactoringsPluginFactory implements RefactoringPluginFactory {

    private static final Logger LOGGER = Logger.getLogger(AllRefactoringsPluginFactory.class.getName());
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        return new RefactoringPluginImpl();
    }
    
    private static final class RefactoringPluginImpl implements RefactoringPlugin {

        public Problem preCheck() {
            return null;
        }

        public Problem checkParameters() {
            return null;
        }

        public Problem fastCheckParameters() {
            return null;
        }

        public void cancelRequest() {}

        public Problem prepare(RefactoringElementsBag refactoringElements) {
            refactoringElements.getSession().addProgressListener(new ProgressListener() {
                public void start(ProgressEvent event) {
                    LOGGER.log(Level.FINE, "Refactoring started, locking RepositoryUpdater");
//                    RepositoryUpdater.getDefault().lockRU();
                    IndexingController.getDefault().enterProtectedMode();
                }
                public void step(ProgressEvent event) {}
                public void stop(ProgressEvent event) {
                    LOGGER.log(Level.FINE, "Refactoring finished, unlocking RepositoryUpdater");
//                    RepositoryUpdater.getDefault().unlockRU(new Runnable() {
                    IndexingController.getDefault().exitProtectedMode(new Runnable() {
                        public void run() {
                            LOGGER.log(Level.FINE, "Refreshing editor panes:");
                            for (FileObject f : JavaSourceSupportAccessor.ACCESSOR.getVisibleEditorsFiles()) {
                                JavaSource source = JavaSource.forFileObject(f);
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    LOGGER.log(Level.FINE, "Refreshing file={0}, JavaSource={1}", new Object[] {f, source});
                                }
                                if (source != null) {
                                    JavaSourceAccessor.getINSTANCE().revalidate(source);
                                }
                            }
                            LOGGER.log(Level.FINE, "done.");
                        }
                    });
                }
            });
            
            return null;
        }
        
    }

}
