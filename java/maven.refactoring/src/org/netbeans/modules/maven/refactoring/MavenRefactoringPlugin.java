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
package org.netbeans.modules.maven.refactoring;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.ClassUsage;
import org.netbeans.modules.maven.model.ModelOperation;
import static org.netbeans.modules.maven.model.Utilities.performPOMModelOperations;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import static org.netbeans.modules.maven.refactoring.MavenRefactoringPluginFactory.RUN_MAIN_CLASS;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

class MavenRefactoringPlugin implements RefactoringPlugin {

    private static final Logger LOG = Logger.getLogger(MavenRefactoringPlugin.class.getName());

    private final RenameRefactoring refactoring;
    private final WhereUsedQuery query;
    private final TreePathHandle handle;

    MavenRefactoringPlugin(WhereUsedQuery query, TreePathHandle handle) {
        this.query = query;
        this.handle = handle;
        this.refactoring = null;
    }

    MavenRefactoringPlugin(RenameRefactoring refactoring, TreePathHandle handle) {
        this.refactoring = refactoring;
        this.handle = handle;
        this.query = null;
    }

    @Override public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (query != null && !query.getBooleanValue(WhereUsedQuery.FIND_REFERENCES)) {
            return null;
        }
        final AtomicReference<String> fqn = new AtomicReference<String>();
        CancellableTask<CompilationController> info = new CancellableTask<CompilationController>() {
            @Override public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                Element e = handle.resolveElement(info);
                if (e == null) {
                    LOG.log(Level.WARNING, "no element for {0}", handle);
                    return;
                }
                switch (e.getKind()) {
                case CLASS:
                case INTERFACE:
                case ANNOTATION_TYPE:
                case ENUM:
                    fqn.set(info.getElements().getBinaryName((TypeElement) e).toString());
                    break;
                default:
                    LOG.log(Level.FINE, "unexpected element {0}", e);
                }
            }
            @Override public void cancel() {}
        };
        
        if (refactoring != null) {
            ModelOperation<POMModel> renameMainClassProp = (final POMModel model) -> {
                Properties pr = model.getProject().getProperties();
                ElementHandle e = handle.getElementHandle();
                if (e != null) {
                    String oldName = e.getBinaryName();
                    String newName = refactoring.getNewName();

                    if (pr.getProperty(RUN_MAIN_CLASS) != null) {
                        String oldProperty = pr.getProperty(RUN_MAIN_CLASS);
                        if (oldProperty.equals(oldName)) {
                            int lastIndex = oldName.lastIndexOf('.');
                            String newPropertyValue = newName;
                            if (lastIndex >= 0) {
                                String packageName = oldName.substring(0, lastIndex + 1);
                                newPropertyValue = packageName + newPropertyValue;
                            }
                            pr.setProperty(RUN_MAIN_CLASS, newPropertyValue);
                        }
                    }
                }
            };

            try {
                FileObject fo = handle.getFileObject();
                Project p = FileOwnerQuery.getOwner(fo);
                final FileObject pom = p.getProjectDirectory().getFileObject("pom.xml"); // NOI18N
                pom.getFileSystem().runAtomicAction(() -> {
                    performPOMModelOperations(pom, Arrays.asList(renameMainClassProp));
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return null;
        }

        JavaSource source = JavaSource.forFileObject(handle.getFileObject());
        if (source != null) {
            try {
                source.runUserActionTask(info, true);
            } catch (IOException x) {
                LOG.log(Level.WARNING, null, x);
            }
        } else {
            LOG.log(Level.WARNING, "no source for {0}", handle.getFileObject());
        }
        LOG.log(Level.FINE, "for {0} found FQN: {1}", new Object[] {handle, fqn});
        if (fqn.get() != null) {
            long start = System.currentTimeMillis();
            //#209856 ->getLoadedContexts() 
            Iterable<ClassUsage> results = RepositoryQueries.findClassUsagesResult(fqn.get(), RepositoryQueries.getLoadedContexts()).getResults();
            long end = System.currentTimeMillis();
            LOG.log(Level.FINE, "took {0}msec to find {1}", new Object[] {end - start, fqn});
            //TODO do we care reporting to the user somehow?
            for (RepositoryQueries.ClassUsage result : results) {
                for (String clazz : result.getClasses()) {
                    refactoringElements.add(query, new MavenRefactoringElementImplementation(new ReferringClass(result.getArtifact(), clazz)));
                }
            }
        }
        return null;
    }

    @Override public Problem preCheck() {
        return null;
    }

    @Override public Problem checkParameters() {
        return null;
    }

    @Override public Problem fastCheckParameters() {
        return null;
    }

    @Override public void cancelRequest() {}

}
