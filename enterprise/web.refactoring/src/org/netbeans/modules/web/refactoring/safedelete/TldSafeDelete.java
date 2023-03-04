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
package org.netbeans.modules.web.refactoring.safedelete;

import java.util.List;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.netbeans.modules.web.refactoring.TldRefactoring;
import org.netbeans.modules.web.taglib.model.TagType;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Safe delete for tld files.
 *
 * @author Erno Mononen
 */
public class TldSafeDelete extends TldRefactoring{
    
    private final List<String> classes;
    private final WebModule webModule;
    private final SafeDeleteRefactoring safeDelete;
    
    public TldSafeDelete(SafeDeleteRefactoring safeDelete, WebModule webModule) {
        this.safeDelete = safeDelete;
        this.webModule = webModule;
        this.classes = RefactoringUtil.getRefactoredClasses(safeDelete);
    }
    
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {

        Problem problem = null;
        for (String clazz : classes){
            for (TaglibHandle taglibHandle : getTaglibs(webModule)) {
            if (!taglibHandle.isValid()) {
                problem = RefactoringUtil.addToEnd(new Problem(false, 
                        NbBundle.getMessage(TldSafeDelete.class, "TXT_TldInvalidProblem", taglibHandle.getTldFile())), 
                        problem);
                continue;
            }
                Taglib taglib = taglibHandle.getTaglib();
                for (TagType tagType : taglib.getTag()) {
                    if (clazz.equals(tagType.getTagClass())) {
                        refactoringElements.add(safeDelete, new TagClassSafeDeleteElement(clazz, taglib, taglibHandle.getTldFile(), tagType));
                    }
                }
            }
        }
        return problem;
    }
    
    private static class TagClassSafeDeleteElement extends TldRefactoringElement {
        
        private final TagType tagType;
        
        public TagClassSafeDeleteElement(String clazz, Taglib taglib, FileObject tldFile, TagType tagType) {
            super(clazz, taglib, tldFile);
            this.tagType = tagType;
        }
        
        public String getDisplayText() {
            return NbBundle.getMessage(TldSafeDelete.class, "TXT_TaglibTagClassSafeDelete", tagType.getName());
        }
        
        @Override
        public void undoChange() {
            taglib.addTag(tagType);
            write();
        }
        
        public void performChange() {
            taglib.removeTag(tagType);
            write();
        }

    }
    
}
