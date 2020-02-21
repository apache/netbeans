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
