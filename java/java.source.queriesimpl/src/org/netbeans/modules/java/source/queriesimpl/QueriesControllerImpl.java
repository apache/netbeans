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
