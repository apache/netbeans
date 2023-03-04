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

package org.netbeans.modules.j2ee.jpa.refactoring.safedelete;

import java.text.MessageFormat;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jpa.refactoring.PersistenceXmlRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;

/**
 * Handles renaming of the classes that are listed in <code>persistence.xml</code>.
 *
 * @author Erno Mononen
 */
public final class PersistenceXmlSafeDelete extends PersistenceXmlRefactoring {
    
    private final SafeDeleteRefactoring safeDeleteRefactoring;
    
    public PersistenceXmlSafeDelete(SafeDeleteRefactoring safeDeleteRefactoring) {
        this.safeDeleteRefactoring = safeDeleteRefactoring;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return safeDeleteRefactoring;
    }

    protected RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
                                                                     FileObject clazz,
                                                                     PUDataObject pUDataObject,
                                                                     FileObject persistenceXml) {

        return new PersistenceXmlSafeDeleteRefactoringElement(persistenceUnit, JavaIdentifiers.getQualifiedName(clazz), pUDataObject, persistenceXml);
    }

    
    /**
     * A rename element for persistence.xml
     */
    private static class PersistenceXmlSafeDeleteRefactoringElement extends PersistenceXmlRefactoringElement {
        
        public PersistenceXmlSafeDeleteRefactoringElement(PersistenceUnit persistenceUnit,
                String clazz,  PUDataObject puDataObject, FileObject parentFile) {
            super(persistenceUnit, clazz, puDataObject, parentFile);
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            Object[] args = new Object [] {clazz};
            return MessageFormat.format(NbBundle.getMessage(PersistenceXmlSafeDelete.class, "TXT_PersistenceXmlSafeDeleteClass"), args);
        }
        
        public void undoChange() {
            ProviderUtil.addManagedClass(persistenceUnit, clazz, puDataObject);
        }
        
        public void performChange() {
            ProviderUtil.removeManagedClass(persistenceUnit, clazz, puDataObject);
        }
        
    }
    
}
