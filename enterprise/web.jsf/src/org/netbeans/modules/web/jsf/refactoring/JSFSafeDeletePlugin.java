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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Petr Pisl, Po-Ting Wu
 */
public class JSFSafeDeletePlugin extends JavaRefactoringPlugin{
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    private TreePathHandle treePathHandle = null;
    
    private static final Logger LOGGER = Logger.getLogger(JSFSafeDeletePlugin.class.getName());
    
    private final SafeDeleteRefactoring refactoring;
    
    /** Creates a new instance of JSFWhereUsedPlugin */
    public JSFSafeDeletePlugin(SafeDeleteRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            NonRecursiveFolder nonRecursivefolder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            Project project;
            
            if (nonRecursivefolder != null){
                // non recursive package
                FileObject folder = nonRecursivefolder.getFolder();
                project = FileOwnerQuery.getOwner(folder);
                if (project != null){
                    String packageName = JSFRefactoringUtils.getPackageName(folder);
                    List <Occurrences.OccurrenceItem> items = Occurrences.getPackageOccurrences(project, packageName, packageName, false);
                    for (Occurrences.OccurrenceItem item : items) {
                        refactoringElements.add(refactoring, new JSFSafeDeleteClassElement(item));
                    }
                }
            }

            if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())){
                project = FileOwnerQuery.getOwner(treePathHandle.getFileObject());
                if (project != null){
                    Element resElement = JSFRefactoringUtils.resolveElement(getClasspathInfo(refactoring), refactoring, treePathHandle);
                    TypeElement type = (TypeElement) resElement;
                    String fqcn = type.getQualifiedName().toString();
                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(project, fqcn, null);
                    for (Occurrences.OccurrenceItem item : items) {
                        refactoringElements.add(refactoring, new JSFSafeDeleteClassElement(item));
                    }
                }
            }
            semafor.set(null);
        }
        return null;
    }
    
    public static class JSFSafeDeleteClassElement extends SimpleRefactoringElementImplementation {
        private final Occurrences.OccurrenceItem item;
        
        JSFSafeDeleteClassElement(Occurrences.OccurrenceItem item){
            this.item = item;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public String getDisplayText() {
            return item.getSafeDeleteMessage();
        }
        
        public void performChange() {
            try {
                item.performSafeDelete();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public Lookup getLookup() {
            return Lookups.singleton(item.getFacesConfig());
        }
        
        public FileObject getParentFile() {
            return item.getFacesConfig();
        }
        
        public PositionBounds getPosition() {
            return item.getChangePosition();
        }
        
        @Override
        public void undoChange() {
            try {
                item.undoSafeDelete();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
}
