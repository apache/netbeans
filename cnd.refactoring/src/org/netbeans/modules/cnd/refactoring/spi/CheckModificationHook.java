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
package org.netbeans.modules.cnd.refactoring.spi;

import java.text.MessageFormat;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.plugins.CsmModificationRefactoringPlugin;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class CheckModificationHook {
    
    /**
     * chain extra problems to existing one based on referencedObject check.
     * To chain problems createProblem method can be used.
     * @param problem existing problems or null
     * @param referencedObject object to check
     * @return new problem or passed one if no own problems are chained.
     */
    public abstract Problem appendProblem(AbstractRefactoring refactoring, Problem problem, CsmObject referencedObject);
    
    protected final Problem defaultCheckIfModificationPossible(Problem problem, CsmObject referencedObject) {
        // check read-only elements
        problem = checkIfModificationPossibleInFile(problem, referencedObject);
        if (problem != null) {
            return problem;
        }
        if (CsmKindUtilities.isMethod(referencedObject)) {
            CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
            if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                Collection<CsmMethod> overridenMethods = CsmVirtualInfoQuery.getDefault().getOverriddenMethods(method, true);
                if (overridenMethods.size() > 1) {
                    // check all overriden methods
                    for (CsmMethod csmMethod : overridenMethods) {
                        problem = checkIfModificationPossibleInFile(problem, csmMethod);
                        CsmFunction def = csmMethod.getDefinition();
                        if (def != null && !csmMethod.equals(def)) {
                            problem = checkIfModificationPossibleInFile(problem, def);
                        }
                    }
                    boolean fatal = (problem != null);
                    String msg = fatal ? getString("ERR_Overrides_Fatal") : getString("ERR_OverridesOrOverriden"); // NOI18N
                    problem = createProblem(problem, fatal, msg);
                }
            }
        }
        return problem;
    }
    
    protected String getString(String key) {
        return NbBundle.getMessage(CsmModificationRefactoringPlugin.class, key);
    }

    protected Problem checkIfModificationPossibleInFile(Problem problem, CsmObject csmObject) {
        CsmFile csmFile = null;
        if (CsmKindUtilities.isFile(csmObject)) {
            csmFile = (CsmFile) csmObject;
        } else if (CsmKindUtilities.isOffsetable(csmObject)) {
            csmFile = ((CsmOffsetable) csmObject).getContainingFile();
        }
        if (csmFile != null) {
            FileObject fo = CsmUtilities.getFileObject(csmFile);
            if (!CsmRefactoringUtils.isRefactorable(fo)) {
                problem = createProblem(problem, true, getCannotRename(fo));
            }
            // check that object is in opened project
            if (problem == null && !CsmRefactoringUtils.isElementInOpenProject(csmFile)) {
                problem = new Problem(false, NbBundle.getMessage(CsmModificationRefactoringPlugin.class, "ERR_ProjectNotOpened"));
                return problem;
            }
        }
        return problem;
    }

    protected String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(CsmModificationRefactoringPlugin.class, "ERR_CannotModifyInFile")).format(new Object[]{r.getNameExt()});
    }

    protected Problem createProblem(Problem problem, boolean fatal, String msg) {
        return CsmModificationRefactoringPlugin.createProblem(problem, fatal, msg);
    }
}
