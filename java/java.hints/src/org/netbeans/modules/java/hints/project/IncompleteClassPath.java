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
package org.netbeans.modules.java.hints.project;

import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class IncompleteClassPath implements ErrorRule<Void> {

    private static final String CODE = "nb.classpath.incomplete";      //NOI18N
    private static final Set<String> CODES = Collections.singleton(CODE);

    @Override
    @NonNull
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    @NonNull
    public String getId() {
        return IncompleteClassPath.class.getName();
    }

    @Override
    @NonNull
    public String getDisplayName() {
        return NbBundle.getMessage(IncompleteClassPath.class, "TXT_IncompleteClassPath");
    }

    @Override
    @NonNull
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        final FileObject file = compilationInfo.getFileObject();
        if (file != null) {
            final Project prj = FileOwnerQuery.getOwner(file);
            if (prj != null) {
                return Collections.<Fix>singletonList (new ResolveFix(prj));
            }
        }
        return Collections.<Fix>emptyList();
    }

    @Override
    public void cancel() {
    }

    public static final class ResolveFix implements Fix {
        private final Project prj;

        ResolveFix(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(IncompleteClassPath.class, "TXT_ResolveProjectProblems");
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ProjectProblems.showCustomizer(prj);
            return null;
        }
    }

}
