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
