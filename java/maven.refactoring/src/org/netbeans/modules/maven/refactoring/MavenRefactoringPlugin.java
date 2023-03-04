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
package org.netbeans.modules.maven.refactoring;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.ClassUsage;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;

class MavenRefactoringPlugin implements RefactoringPlugin {

    private static final Logger LOG = Logger.getLogger(MavenRefactoringPlugin.class.getName());
    
    private final WhereUsedQuery query;
    private final TreePathHandle handle;

    MavenRefactoringPlugin(WhereUsedQuery query, TreePathHandle handle) {
        this.query = query;
        this.handle = handle;
    }

    @Override public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (!query.getBooleanValue(WhereUsedQuery.FIND_REFERENCES)) {
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
