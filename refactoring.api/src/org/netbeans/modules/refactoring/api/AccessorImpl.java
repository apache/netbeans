/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.refactoring.api;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
final class AccessorImpl extends APIAccessor {
    @Override
    public Collection<GuardedBlockHandler> getGBHandlers(AbstractRefactoring refactoring) {
        assert refactoring != null;
        return refactoring.getGBHandlers();
    }
    
    @Override
    public boolean hasPluginsWithProgress(AbstractRefactoring refactoring) {
        return refactoring.pluginsWithProgress!=null && !refactoring.pluginsWithProgress.isEmpty();
    }

    @Override
    public Problem chainProblems(Problem p, Problem p1) {
        return AbstractRefactoring.chainProblems(p, p1);
    }
    
    @Override
    public ProblemDetails createProblemDetails(ProblemDetailsImplementation pdi) {
        assert pdi != null;
        return new ProblemDetails(pdi);
    }

    @Override
    public boolean isCommit(RefactoringSession session) {
        return session.realcommit;
    }

    @Override
    public RefactoringElementImplementation getRefactoringElementImplementation(RefactoringElement el) {
        return el.impl;
    }

    @Override
    public boolean hasChangesInGuardedBlocks(RefactoringSession session) {
        return SPIAccessor.DEFAULT.hasChangesInGuardedBlocks(session.getElementsBag());
    }

    @Override
    public boolean hasChangesInReadOnlyFiles(RefactoringSession session) {
        return SPIAccessor.DEFAULT.hasChangesInReadOnlyFiles(session.getElementsBag());
    }
    
    @Override
    public FiltersDescription getFiltersDescription(AbstractRefactoring refactoring) {
        return refactoring.getFiltersDescription();
    }

    @Override
    public void resetFiltersDescription(AbstractRefactoring refactoring) {
        refactoring.resetFiltersDescription();
    }

    @Override
    public boolean isFinished(RefactoringSession session) {
        return session.isFinished();
    }
    
}
