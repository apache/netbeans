/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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

    private static final class ResolveFix implements Fix {

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
