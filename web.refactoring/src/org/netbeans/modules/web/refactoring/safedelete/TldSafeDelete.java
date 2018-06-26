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
