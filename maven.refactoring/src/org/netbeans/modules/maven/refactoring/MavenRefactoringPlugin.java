/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
