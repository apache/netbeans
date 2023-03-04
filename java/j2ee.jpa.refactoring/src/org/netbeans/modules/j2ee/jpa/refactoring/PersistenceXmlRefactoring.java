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
package org.netbeans.modules.j2ee.jpa.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.jpa.refactoring.util.PositionBoundsResolver;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A base class for persistence.xml refactorings.
 *
 * @author Erno Mononen
 */
public abstract class PersistenceXmlRefactoring extends JavaRefactoringPlugin {
    
    /**
     * The file objects representing the refactoring sources.
     */ 
    private Collection<? extends FileObject> refactoringSources;
    
    @Override
    public Problem fastCheckParameters() {
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(JavaRefactoringPlugin.Phase p) {
        return null;
    }
    
    /**
     * Gets the refactoring sources for this refactoring. Move class and safe delete
     * refactorings may be invoked on multiple files, for other refactorings there 
     * is be just one refactoring source.
     * 
     *@return the file objects representing the refactoring sources, i.e. the objects
     * being refactored.
     */
    protected Collection<? extends FileObject> getRefactoringSources() {
        if (refactoringSources == null){
            refactoringSources = lookupRefactoringSources();
        }
        return refactoringSources;
    }
    
    private Collection<? extends FileObject> lookupRefactoringSources() {
        // move class and safe delete refactorings may be invoked on multiple files
        Collection<? extends FileObject> fosFromLookup = getRefactoring().getRefactoringSource().lookupAll(FileObject.class);
        if (!fosFromLookup.isEmpty()){
            List<FileObject> result = new ArrayList<FileObject>();
            for (FileObject each : fosFromLookup){
                if (each.isFolder()){
                    collectChildren(each, result);
                } else {
                    result.add(each);
                }
            }
            return result;
        }
        NonRecursiveFolder folder = getRefactoring().getRefactoringSource().lookup(NonRecursiveFolder.class);
        if (folder != null){
            return Collections.singleton(folder.getFolder());
        }

        TreePathHandle treePathHandle = getRefactoring().getRefactoringSource().lookup(TreePathHandle.class);
        if (treePathHandle != null) {
            return Collections.singleton(treePathHandle.getFileObject());
        }

        return Collections.<FileObject>emptySet();
    }
    
    /**
     * Recursively collects the java files from the given folder into the
     * given <code>result</code>.
     */
    public static void collectChildren(FileObject folder, List<FileObject> result) {
        for (FileObject child : folder.getChildren()) {
            if (RefactoringUtil.isJavaFile(child)) {
                result.add(child);
            } else if (child.isFolder()) {
                collectChildren(child, result);
            }
        }
    }

    /**
     * Checks whether any of the objects being refactored should be handled by
     * this refactoring. Override in subclasses as needed, the
     * default implementation returns true if any of the refactored objects
     * is a Java class.
     *
     * @return true if the any of the refactoring sources represents a Java class.
     */
    protected boolean shouldHandle(){
        
        for (FileObject refactoringSource : getRefactoringSources()){
            if (shouldHandle(refactoringSource)){
                return true;
            }
        }
        return false;
        
    }
    
    /**
     * Checks whether the given <code>refactoringSource</code> should be handled by
     * this refactoring. Override in subclasses as needed, the
     * default implementation returns true if the given <code>refactoringSource</code>
     * is a class.
     * @param refactoringSource the object being refactored.
     * 
     * @return true if the <code>refactoringSource<code> represents a class that
     * should be handled by persistence.xml refactorings.
     */
    protected boolean shouldHandle(FileObject refactoringSource) {
        final boolean[] result = new boolean[]{false};

        if (RefactoringUtil.isJavaFile(refactoringSource)) {
            JavaSource source = JavaSource.forFileObject(refactoringSource);
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {

                    public void cancel() {
                    }

                    public void run(CompilationController info) throws Exception {
                        info.toPhase(JavaSource.Phase.RESOLVED);
                        TreePathHandle treePathHandle = null;
                        CompilationUnitTree cut = info.getCompilationUnit();
                        if (!cut.getTypeDecls().isEmpty()) {
                            treePathHandle = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), info);
                        }
                        if (treePathHandle == null) {
                            result[0] = false;
                        } else {
                            Element element = treePathHandle.resolveElement(info);
                            if (element != null) {
                                result[0] = element.getKind() == ElementKind.CLASS;
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return result[0];
    }
    
    @Override
    public final Problem preCheck() {
        
        if (!shouldHandle()){
            return null;
        }
        
        Problem result = null;
        
        for (FileObject refactoringSource : getRefactoringSources()){
            for (FileObject persistenceXml : getPersistenceXmls(refactoringSource)) {
                try {
                    ProviderUtil.getPUDataObject(persistenceXml);
                } catch (InvalidPersistenceXmlException ex) {
                    Problem newProblem = new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));
                    result = RefactoringUtil.addToEnd(newProblem, result);
                }
            }
        }
        return result;
        
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        
        if (!shouldHandle()){
            return null;
        }
        
        Problem result = null;
        for (FileObject refactoringSource : getRefactoringSources()) {
            Project project = FileOwnerQuery.getOwner(refactoringSource);
            if (project == null) {
                continue;
            }
            if (!shouldHandle(refactoringSource)){
                continue;
            }
            String classNameFQN = JavaIdentifiers.getQualifiedName(refactoringSource);

            for (FileObject each : getPersistenceXmls(refactoringSource)){
                try {
                    PUDataObject pUDataObject = ProviderUtil.getPUDataObject(each);
                    List<PersistenceUnit> punits = getAffectedPersistenceUnits(pUDataObject, classNameFQN);
                    for (PersistenceUnit persistenceUnit : punits) {
                        refactoringElementsBag.add(getRefactoring(), getRefactoringElement(persistenceUnit, refactoringSource, pUDataObject, each));
                    }
                } catch (InvalidPersistenceXmlException ex) {
                    Problem newProblem =
                            new Problem(false, NbBundle.getMessage(PersistenceXmlRefactoring.class, "TXT_PersistenceXmlInvalidProblem", ex.getPath()));

                    result = RefactoringUtil.addToEnd(newProblem, result);
                }
            }
        }
        
        return result;
    }
    
    /**
     * @return the actual refactoring being performed.
     */
    protected abstract AbstractRefactoring getRefactoring();
    
    /**
     *@return the refactoring element for the given parameters.
     */
    protected abstract RefactoringElementImplementation getRefactoringElement(PersistenceUnit persistenceUnit,
            FileObject clazz, PUDataObject pUDataObject, FileObject persistenceXml);
    
    /**
     * Gets the persistence unit from the given <code>PUDataObject</code> that contain
     * a class matching with the given <code>clazz</code>.
     * @param puDataObject
     * @param clazz the fully qualified name of the class
     *
     * @return the persistence units that contain the given class.
     */
    protected final List<PersistenceUnit> getAffectedPersistenceUnits(PUDataObject pUDataObject, String clazz){
        List<PersistenceUnit> result = new ArrayList<PersistenceUnit>();
        PersistenceUnit[] persistenceUnits = ProviderUtil.getPersistenceUnits(pUDataObject);
        for(PersistenceUnit each : persistenceUnits){
            if (hasClass(each, clazz)){
                result.add(each);
            }
        }
        return result;
    }
    
    
    private static boolean hasClass(PersistenceUnit persistenceUnit, String clazz){
        for (String each : persistenceUnit.getClass2()){
            if (each.equals(clazz)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the persistence.xml files in the project to which the given 
     * <code>refactoringSource</code> belongs.
     * @param refactoringSource 
     * @return the persistence.xml files in the project to which the refactored
     * class belongs or an empty list if the class does not belong to any project.
     */
    protected final List<FileObject> getPersistenceXmls(FileObject refactoringSource){
        Project project = FileOwnerQuery.getOwner(refactoringSource);
        if (project == null){
            return Collections.<FileObject>emptyList();
        }
        
        List<FileObject> result = new ArrayList<FileObject>();
        
        PersistenceScope[] persistenceScopes = PersistenceUtils.getPersistenceScopes(project);
        for (int i = 0; i < persistenceScopes.length; i++) {
            FileObject persistenceXmlFo = persistenceScopes[i].getPersistenceXml();
            if(persistenceXmlFo != null) {
                result.add(persistenceXmlFo);
            }
        }
        
        return result;
    }
    
    protected abstract static class PersistenceXmlRefactoringElement extends SimpleRefactoringElementImplementation {
        
        protected final PersistenceUnit persistenceUnit;
        protected final PUDataObject puDataObject;
        protected final String clazz;
        protected final FileObject parentFile;
        
        protected PersistenceXmlRefactoringElement(PersistenceUnit persistenceUnit,
                String clazz,  PUDataObject puDataObject, FileObject parentFile) {
            this.clazz = clazz;
            this.persistenceUnit = persistenceUnit;
            this.puDataObject = puDataObject;
            this.parentFile = parentFile;
        }
        
        public final String getText() {
            return getDisplayText();
        }
        
        public final Lookup getLookup() {
            return Lookups.singleton(parentFile);
        }
        
        public final FileObject getParentFile() {
            return parentFile;
        }
        
        public final PositionBounds getPosition() {
            try {
                return new PositionBoundsResolver(DataObject.find(parentFile), clazz).getPositionBounds();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
    
}
