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
package org.netbeans.modules.web.refactoring.whereused;

import java.text.MessageFormat;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
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
 * Finds usages of classes in tld files.
 *
 * @author Erno Mononen
 */
public class TldWhereUsed extends TldRefactoring{
    
    private final WhereUsedQuery whereUsedQuery;
    private final WebModule webModule;
    private final String clazz;
    
    public TldWhereUsed(String clazz, WebModule wm, WhereUsedQuery whereUsedQuery) {
        this.clazz = clazz;
        this.whereUsedQuery = whereUsedQuery;
        this.webModule = wm;
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Problem problem = null;

        for(TaglibHandle taglibHandle : getTaglibs(webModule)){
            if (!taglibHandle.isValid()) {
                problem = RefactoringUtil.addToEnd(new Problem(false, 
                        NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibWhereUsedInvalidProblem", taglibHandle.getTldFile())), 
                        problem);
                continue;
            }
            Taglib taglib = taglibHandle.getTaglib();
            for (TagType tagType : taglib.getTag()){
                if (clazz.equals(tagType.getTagClass())){
                    refactoringElements.add(whereUsedQuery, new TagClassWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
                if (clazz.equals(tagType.getTeiClass())){
                    refactoringElements.add(whereUsedQuery, new TeiClassWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
            for (FunctionType functionType : taglib.getFunction()){
                if (clazz.equals(functionType.getFunctionClass())){
                    refactoringElements.add(whereUsedQuery, new FunctionWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
            ValidatorType validatorType = taglib.getValidator();
            if (validatorType != null && clazz.equals(validatorType.getValidatorClass())){
                refactoringElements.add(whereUsedQuery, new ValidatorWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
            }
            for (ListenerType listenerType : taglib.getListener()){
                if (clazz.equals(listenerType.getListenerClass())){
                    refactoringElements.add(whereUsedQuery, new ListenerWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
        }
        
        return problem;
    }
    
    private static class TagClassWhereUsedElement extends TldRefactoringElement {
        
        public TagClassWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibTagClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
    private static class TeiClassWhereUsedElement extends TldRefactoringElement {
        
        public TeiClassWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibTeiClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
    private static class FunctionWhereUsedElement extends TldRefactoringElement {
        
        public FunctionWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibFunctionClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
        
    }
    
    private static class ValidatorWhereUsedElement extends TldRefactoringElement {
        
        public ValidatorWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibValidatorClassWhereUsed"), clazz);
        }

        public void performChange() {
            // do nothing
        }
    }
    
    private static class ListenerWhereUsedElement extends TldRefactoringElement {
        
        public ListenerWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibListenerClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
}

