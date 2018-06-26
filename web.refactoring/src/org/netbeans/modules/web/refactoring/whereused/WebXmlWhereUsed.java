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
