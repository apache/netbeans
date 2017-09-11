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
