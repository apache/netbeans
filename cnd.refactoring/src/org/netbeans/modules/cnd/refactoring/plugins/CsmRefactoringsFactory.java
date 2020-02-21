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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.refactoring.plugins;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.refactoring.api.ChangeParametersRefactoring;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.api.InlineRefactoring;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.spi.*;

/**
 * Factory to support C/C++ refactorings
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class, position=150)
public class CsmRefactoringsFactory implements RefactoringPluginFactory {
   
    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        CsmContext editorContext = refactoring.getRefactoringSource().lookup(CsmContext.class);
        CsmObject ref = CsmRefactoringUtils.findContextObject(refactoring.getRefactoringSource());
        if (ref != null || editorContext != null) {
            if (refactoring instanceof WhereUsedQuery) {
                return new CsmWhereUsedQueryPlugin((WhereUsedQuery) refactoring);
            } else if (refactoring instanceof RenameRefactoring) {
                return new CsmRenameRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (refactoring instanceof ChangeParametersRefactoring) {
                return new ChangeParametersPlugin((ChangeParametersRefactoring) refactoring);
            } else if (refactoring instanceof EncapsulateFieldRefactoring) {
                return new EncapsulateFieldRefactoringPlugin((EncapsulateFieldRefactoring) refactoring);
            } else if (refactoring instanceof EncapsulateFieldsRefactoring) {
                return new EncapsulateFieldsPlugin((EncapsulateFieldsRefactoring) refactoring);
            } else if (refactoring instanceof IntroduceMethodRefactoring) {
                return new IntroduceMethodPlugin((IntroduceMethodRefactoring) refactoring);
            } else if (refactoring instanceof InlineRefactoring) {
                return new InlinePlugin((InlineRefactoring) refactoring);
            }
        }
        return null;
    }
}
