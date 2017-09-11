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
package org.netbeans.modules.java.source.queriesimpl;

import java.io.IOException;
import java.util.List;
import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.queries.api.QueryException;
import org.netbeans.modules.java.source.queries.api.Queries;
import org.netbeans.modules.java.source.queries.api.Updates;
import org.netbeans.modules.java.source.queries.spi.QueriesController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=QueriesController.class)
public class QueriesControllerImpl implements QueriesController {

    @Override
    public <R> R runQuery(@NonNull Context<R> ctx) throws QueryException {
        final URL url = ctx.getURL();
        assert url != null;
        final FileObject file = URLMapper.findFileObject(url);
        if (file == null) {
            throw new QueryException("Cannot map URL: " + url.toExternalForm() + " to FileObject");
        }
        final JavaSource src = JavaSource.forFileObject(file);
        assert src != null;
        final QueryTask<CompilationController, R> qt =
            new QueryTask<CompilationController, R>(ctx);
        try {
            src.runUserActionTask(qt, true);
        } catch (IOException ioe) {
            throw new QueryException(ioe);
        }
        return qt.getResult();
    }

    @Override
    public boolean runUpdate(@NonNull Context<Boolean> ctx) throws QueryException {
        final URL url = ctx.getURL();
        assert url != null;
        final FileObject file = URLMapper.findFileObject(url);
        if (file == null) {
            throw new QueryException("Cannot map URL: " + url.toExternalForm() + " to FileObject");
        }
        final JavaSource src = JavaSource.forFileObject(file);
        assert src != null;
        final QueryTask<WorkingCopy, Boolean> qt =
                new QueryTask<WorkingCopy, Boolean>(ctx);
        try {
            final ModificationResult result = src.runModificationTask(qt);
            if (qt.getResult() == Boolean.TRUE) {
                List<? extends ModificationResult.Difference> diffs = result.getDifferences(file);
                if (diffs != null) {
                    for (ModificationResult.Difference diff : diffs) {
                        diff.setCommitToGuards(true);
                    }
                }
                result.commit();
                return true;
            } else {
                return false;
            }
        } catch (IOException ioe) {
            throw new QueryException(ioe);
        }
    }

    private static class QueryTask<C extends CompilationController, R> implements Task<C> {

        private final Context<R> ctx;
        private R result;

        private QueryTask(@NonNull final Context<R> ctx) {
            assert ctx != null;
            this.ctx = ctx;
        }

        @Override
        public void run(C parameter) throws Exception {
            result = ctx.execute(new JavaOperationsImpl<R>(parameter));
        }

        public R getResult() {
            return result;
        }
    }
}
