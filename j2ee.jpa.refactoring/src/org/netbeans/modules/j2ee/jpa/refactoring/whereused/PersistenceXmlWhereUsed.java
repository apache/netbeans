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


package org.netbeans.modules.j2ee.jpa.refactoring.whereused;


import java.io.IOException;
import java.text.MessageFormat;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jpa.refactoring.PersistenceXmlRefactoring;
import org.netbeans.modules.j2ee.jpa.refactoring.RefactoringUtil;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.openide.util.Exceptions;

/**
 * Handles renaming of the classes that are listed in <code>persistence.xml</code>.
 *
 * @author Erno Mononen
 */
public final class PersistenceXmlWhereUsed extends PersistenceXmlRefactoring {
    
    private final WhereUsedQuery whereUsedQuery;
    
    public PersistenceXmlWhereUsed(WhereUsedQuery refactoring) {
        this.whereUsedQuery = refactoring;
    }
    
    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElementsBag) {
        final Problem[] result = {null};
        final TreePathHandle handle = whereUsedQuery.getRefactoringSource().lookup(TreePathHandle.class);
        if (handle == null) {
            return null;
        }
        if (TreeUtilities.CLASS_TREE_KINDS.contains(handle.getKind())) {
            final ClasspathInfo cpInfo = getClasspathInfo(whereUsedQuery);
            JavaSource source = JavaSource.create(cpInfo, new FileObject[]{handle.getFileObject()});
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void run(CompilationController ci) throws Exception {
                        ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Element resElement = handle.resolveElement(ci);
                        TypeElement type = (TypeElement) resElement;
                        String clazz = type.getQualifiedName().toString();
                        for (FileObject each : getPersistenceXmls(handle.getFileObject())) {
                            try {
                                PUDataObject pUDataObject = ProviderUtil.getPUDataObject(each);
                                for (PersistenceUnit persistenceUnit : getAffectedPersistenceUnits(pUDataObject, clazz)) {
                                    refactoringElementsBag.add(getRefactoring(), getRefactoringElement(persistenceUnit, handle.getFileObject(), pUDataObject, each));
                                }
                            } catch (InvalidPersistenceXmlException ex) {
                                Problem newProblem
                                        = new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));
                                result[0] = RefactoringUtil.addToEnd(newProblem, result[0]);
                            }
                        }
                    }
                }, false);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result[0];
    }
    
    
    protected AbstractRefactoring getRefactoring() {
        return whereUsedQuery;
    }
    
    protected RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
            FileObject clazz,
            PUDataObject pUDataObject,
            FileObject persistenceXml) {
        
        return new PersistenceXmlWhereUsedRefactoringElement(persistenceUnit, JavaIdentifiers.getQualifiedName(clazz), pUDataObject, persistenceXml);
    } 

    protected class PersistenceXmlWhereUsedRefactoringElement extends PersistenceXmlRefactoringElement {
        
        public PersistenceXmlWhereUsedRefactoringElement(PersistenceUnit persistenceUnit,
                String clazz,  PUDataObject puDataObject, FileObject parentFile) {
            super(persistenceUnit, clazz, puDataObject, parentFile);
        }
        
        /**
         * Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        @Override
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(PersistenceXmlWhereUsedRefactoringElement.class, "TXT_PersistenceXmlClassWhereUsed"), clazz);
        }
        
        @Override
        public void performChange() {
            // nothing to do here
        }    
    }
}
