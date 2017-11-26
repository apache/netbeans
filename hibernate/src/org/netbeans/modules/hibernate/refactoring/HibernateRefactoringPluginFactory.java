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
package org.netbeans.modules.hibernate.refactoring;

import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Refactoring plugin factory for refactoring Hibernate mapping files
 * 
 * @author Dongmei Cao
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class HibernateRefactoringPluginFactory implements RefactoringPluginFactory {

    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        if (refactoring instanceof WhereUsedQuery) {
            if (isMappingFile(refactoring)) {
                return new HibernateMappingFindUsagesPlugin((WhereUsedQuery)refactoring);
            } else {
                return new HibernateFindUsagesPlugin((WhereUsedQuery) refactoring);
            }
        } else if (refactoring instanceof RenameRefactoring) {
            if (isMappingFile(refactoring)) {
                return new HibernateMappingRenamePlugin((RenameRefactoring) refactoring);
            } else {
                return new HibernateRenamePlugin((RenameRefactoring) refactoring);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            if (isMappingFile(refactoring)) {
                return new HibernateMappingMovePlugin((MoveRefactoring)refactoring);
                
            } else {
                return new HibernateMovePlugin((MoveRefactoring) refactoring);
            }
        }

        return null;
    }

    public static boolean isMappingFile(AbstractRefactoring refactoring) {
        Lookup refactoringSource = refactoring.getRefactoringSource();
        FileObject fileObject = refactoringSource.lookup(FileObject.class);
        if (fileObject != null &&
                fileObject.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
            return true;
        }
        return false;
    }
}
