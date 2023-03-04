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
