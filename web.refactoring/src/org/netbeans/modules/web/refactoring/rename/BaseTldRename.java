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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

import java.util.List;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.netbeans.modules.web.refactoring.TldRefactoring;
import org.netbeans.modules.web.taglib.model.FunctionType;
import org.netbeans.modules.web.taglib.model.ListenerType;
import org.netbeans.modules.web.taglib.model.TagType;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.taglib.model.ValidatorType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * A base class for rename refactorings in tld files.
 *
 * @author Erno Mononen
 */
abstract class BaseTldRename extends TldRefactoring{
    
    protected final WebModule webModule;
    
    public BaseTldRename(WebModule webModule) {
        this.webModule = webModule;
    }
    
    /**
     * @return the elements representing the classes that are affected 
     * by this refactoring.
     */ 
    protected abstract List<RenameItem> getAffectedClasses();
    
    protected abstract AbstractRefactoring getRefactoring();
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        
        Problem problem = null;
        for(TaglibHandle taglibHandle : getTaglibs(webModule)){
            if (!taglibHandle.isValid()) {
                problem = RefactoringUtil.addToEnd(new Problem(false, 
                        NbBundle.getMessage(BaseTldRename.class, "TXT_TLdInvalidProblem", taglibHandle.getTldFile())), 
                        problem);
                continue;
            }
            Taglib taglib = taglibHandle.getTaglib();
            for (RenameItem item : getAffectedClasses()){
                
                String clazz = item.getOldFqn();
                String newName = item.getNewFqn();
                
                for (TagType tagType : taglib.getTag()){
                    if (clazz.equals(tagType.getTagClass())){
                        refactoringElements.add(getRefactoring(), new TagClassRenameElement(clazz, newName, tagType, taglib, taglibHandle.getTldFile()));
                    }
                    if (clazz.equals(tagType.getTeiClass())){
                        refactoringElements.add(getRefactoring(), new TeiClassRenameElement(clazz, newName, tagType, taglib, taglibHandle.getTldFile()));
                    }
                }
                for (FunctionType functionType : taglib.getFunction()){
                    if (clazz.equals(functionType.getFunctionClass())){
                        refactoringElements.add(getRefactoring(), new FunctionTypeRenameElement(clazz, newName, functionType, taglib, taglibHandle.getTldFile()));
                    }
                }
                ValidatorType validatorType = taglib.getValidator();
                if (validatorType != null && clazz.equals(validatorType.getValidatorClass())){
                    refactoringElements.add(getRefactoring(), new ValidatorRenameElement(clazz, newName, validatorType, taglib, taglibHandle.getTldFile()));
                }
                for (ListenerType listenerType : taglib.getListener()){
                    if (clazz.equals(listenerType.getListenerClass())){
                        refactoringElements.add(getRefactoring(), new ListenerRenameElement(clazz, newName, listenerType, taglib, taglibHandle.getTldFile()));
                    }
                }
            }
        }
        return problem;
    }

    private static class TagClassRenameElement extends TldRefactoringElement{
        
        private final String newName;
        private final TagType tagType;
        
        public TagClassRenameElement(String clazz, String newName, TagType tagType, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
            this.newName = newName;
            this.tagType = tagType;
        }
        
        public String getDisplayText() {
            return NbBundle.getMessage(TldRename.class, "TXT_TaglibTagClassRename", clazz, newName);
        }
        
        public void performChange() {
            tagType.setTagClass(newName);
            write();
        }
        
        @Override
        public void undoChange() {
            tagType.setTagClass(clazz);
            write();
        }
        
    }
    
    private static class TeiClassRenameElement extends TldRefactoringElement{
        
        private final String newName;
        private final TagType tagType;
        
        public TeiClassRenameElement(String clazz, String newName, TagType tagType, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
            this.newName = newName;
            this.tagType = tagType;
        }
        
        public String getDisplayText() {
            return NbBundle.getMessage(TldRename.class, "TXT_TaglibTeiClassRename", clazz, newName);
        }
        
        public void performChange() {
            tagType.setTeiClass(newName);
            write();
        }
        
        @Override
        public void undoChange() {
            tagType.setTeiClass(clazz);
            write();
        }
        
    }
    
    private static class ValidatorRenameElement extends TldRefactoringElement{
        
        private final String newName;
        private final ValidatorType validatorType;
        
        public ValidatorRenameElement(String clazz, String newName, ValidatorType validatorType, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
            this.newName = newName;
            this.validatorType = validatorType;
        }
        
        public String getDisplayText() {
            return NbBundle.getMessage(TldRename.class, "TXT_TaglibValidatorClassRename", clazz, newName);
        }
        
        public void performChange() {
            validatorType.setValidatorClass(newName);
            write();
        }
        
        @Override
        public void undoChange() {
            validatorType.setValidatorClass(clazz);
            write();
        }
        
    }
    
    private static class FunctionTypeRenameElement extends TldRefactoringElement{
        
        private final String newName;
        private final FunctionType functionType;
        
        public FunctionTypeRenameElement(String clazz, String newName, FunctionType functionType, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
            this.newName = newName;
            this.functionType = functionType;
        }
        
        public String getDisplayText() {
            return NbBundle.getMessage(TldRename.class, "TXT_TaglibFunctionClassRename", clazz, newName);
        }
        
        public void performChange() {
            functionType.setFunctionClass(newName);
            write();
        }
        
        @Override
        public void undoChange() {
            functionType.setFunctionClass(clazz);
            write();
        }
        
    }
    
    private static class ListenerRenameElement extends TldRefactoringElement{
        
        private final String newName;
        private final ListenerType listenerType;
        
        public ListenerRenameElement(String clazz, String newName, ListenerType listenerType, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
            this.newName = newName;
            this.listenerType = listenerType;
        }
        
        public String getDisplayText() {
            return NbBundle.getMessage(TldRename.class, "TXT_TaglibListenerClassRename", clazz, newName);
        }
        
        public void performChange() {
            listenerType.setListenerClass(newName);
            write();
        }
        
        @Override
        public void undoChange() {
            listenerType.setListenerClass(clazz);
            write();
        }
        
    }
    
}
