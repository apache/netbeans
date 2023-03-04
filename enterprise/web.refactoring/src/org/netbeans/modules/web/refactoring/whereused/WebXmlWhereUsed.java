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
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.refactoring.WebXmlRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Erno Mononen
 */
public class WebXmlWhereUsed extends WebXmlRefactoring{
    
    private final WhereUsedQuery whereUsedQuery;
    private final String clazzFqn;
    
    public WebXmlWhereUsed(FileObject webDD, String clazzFqn, WhereUsedQuery whereUsedQuery) {
        super(webDD);
        this.clazzFqn = clazzFqn;
        this.whereUsedQuery = whereUsedQuery;
    }
    
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        
        for (Servlet servlet : getServlets(clazzFqn)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(getWebModel(), webDD, "TXT_WebXmlServletWhereUsed", clazzFqn));//NO18N
        }
        
        for (Filter filter : getFilters(clazzFqn)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(getWebModel(), webDD, "TXT_WebXmlFilterWhereUsed", clazzFqn));//NO18N
        }
        
        for (Listener listener : getListeners(clazzFqn)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(getWebModel(), webDD, "TXT_WebXmlListenerWhereUsed", clazzFqn));//NO18N
        }
        
        for (EjbRef ejbRef : getEjbRefs(clazzFqn, true)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(getWebModel(), webDD, "TXT_WebXmlRefRemoteWhereUsed", clazzFqn));//NO18N
        }
        
        for (EjbRef ejbRef : getEjbRefs(clazzFqn, false)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(getWebModel(), webDD, "TXT_WebXmlRefHomeWhereUsed", clazzFqn));//NO18N
        }
        
        for (EjbLocalRef ejbLocalRef : getEjbLocalRefs(clazzFqn, true)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(getWebModel(), webDD, "TXT_WebXmlRefLocalHomeWhereUsed", clazzFqn));//NO18N
        }
        
        for (EjbLocalRef ejbLocalRef : getEjbLocalRefs(clazzFqn, true)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(getWebModel(), webDD, "TXT_WebXmlRefLocalWhereUsed", clazzFqn));//NO18N
        }
        
        return null;
    }
    
    private static class WhereUsedElement extends WebRefactoringElement{
        
        private final String clazz;
        private final String bundleKey;
        
        public WhereUsedElement(WebApp webApp, FileObject webDD, String bundleKey, String clazz) {
            super(webApp, webDD);
            this.bundleKey = bundleKey;
            this.clazz = clazz;
        }
        
        
        protected void doChange() {
            // do nothing
        }
        
        protected void undo() {
            // do nothing
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {clazz};
            return MessageFormat.format(NbBundle.getMessage(WebXmlWhereUsed.class, bundleKey), args);
        }

        protected String getName() {
            return clazz;
        }
        
    }
}
