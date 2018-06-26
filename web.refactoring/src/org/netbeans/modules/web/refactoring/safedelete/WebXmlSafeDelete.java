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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.netbeans.modules.web.refactoring.WebXmlRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Safe delete support for servlets, servlet mappings, filters, filter
 * mappings and listeners.
 * 
 * @author Erno Mononen
 */
public class WebXmlSafeDelete extends WebXmlRefactoring{
    
    private final SafeDeleteRefactoring safeDelete;
    private final List<String> classes;
    
    public WebXmlSafeDelete(FileObject webDD, SafeDeleteRefactoring safeDelete) {
        super(webDD);
        this.safeDelete = safeDelete;
        this.classes = RefactoringUtil.getRefactoredClasses(safeDelete);
    }
    
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        for (String clazzFqn : classes) {
            for (Servlet servlet : getServlets(clazzFqn)) {
                refactoringElements.add(safeDelete, new ServletRemoveElement(getWebModel(), webDD, servlet));
                for (ServletMapping mapping : getServletMappings(servlet)) {
                    refactoringElements.add(safeDelete, new ServletMappingRemoveElement(getWebModel(), webDD, mapping));
                }
            }

            for (Filter filter : getFilters(clazzFqn)) {
                refactoringElements.add(safeDelete, new FilterRemoveElement(getWebModel(), webDD, filter));
                for (FilterMapping mapping : getFilterMappings(filter)) {
                    refactoringElements.add(safeDelete, new FilterMappingRemoveElement(getWebModel(), webDD, mapping));
                }
            }

            for (Listener listener : getListeners(clazzFqn)) {
                refactoringElements.add(safeDelete, new ListenerRemoveElement(getWebModel(), webDD, listener));
            }
        }

        return null;
    }
    
    private List<ServletMapping> getServletMappings(Servlet servlet){
        List<ServletMapping> result = new ArrayList<ServletMapping>();
        for (ServletMapping mapping : getWebModel().getServletMapping()){
            if (mapping.getServletName().equals(servlet.getServletName())){
                result.add(mapping);
            }
        }
        return result;
    }
    
    private List<FilterMapping> getFilterMappings(Filter filter){
        List<FilterMapping> result = new ArrayList<FilterMapping>();
        for (FilterMapping mapping : getWebModel().getFilterMapping()){
            if (mapping.getFilterName().equals(filter.getFilterName())){
                result.add(mapping);
            }
        }
        return result;
    }
    
    private static class ServletRemoveElement extends WebRefactoringElement{
        
        private Servlet servlet;
        public ServletRemoveElement(WebApp webApp, FileObject webDD, Servlet servlet) {
            super(webApp, webDD);
            this.servlet = servlet;
        }
        
        @Override
        protected void doChange() {
            webApp.removeServlet(servlet);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {servlet.getServletClass()};
            return MessageFormat.format(NbBundle.getMessage(WebXmlSafeDelete.class, "TXT_WebXmlServletSafeDelete"), args);
        }
        
        
        protected void undo() {
            webApp.addServlet(servlet);
        }

        protected String getName() {
            return servlet.getServletClass();
        }
    }

    private static class ServletMappingRemoveElement extends WebRefactoringElement{
        
        private ServletMapping mapping;
        public ServletMappingRemoveElement(WebApp webApp, FileObject webDD, ServletMapping mapping) {
            super(webApp, webDD);
            this.mapping = mapping;
        }
        
        @Override
        protected void doChange() {
            webApp.removeServletMapping(mapping);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {mapping.getServletName()};
            return MessageFormat.format(NbBundle.getMessage(WebXmlSafeDelete.class, "TXT_WebXmlServletMappingSafeDelete"), args);
        }
        
        protected void undo() {
            webApp.addServletMapping(mapping);
        }

        protected String getName() {
            return mapping.getServletName();
        }
    }

    private static class FilterRemoveElement extends WebRefactoringElement{
        
        private Filter filter;
        public FilterRemoveElement(WebApp webApp, FileObject webDD, Filter filter) {
            super(webApp, webDD);
            this.filter = filter;
        }
        
        @Override
        protected void doChange() {
            webApp.removeFilter(filter);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {filter.getFilterClass()};
            return MessageFormat.format(NbBundle.getMessage(WebXmlSafeDelete.class, "TXT_WebXmlFilterSafeDelete"), args);
        }
        
        protected void undo() {
            webApp.addFilter(filter);
        }

        protected String getName() {
            return filter.getFilterClass();
        }
    }

    private static class FilterMappingRemoveElement extends WebRefactoringElement{
        
        private FilterMapping mapping;
        public FilterMappingRemoveElement(WebApp webApp, FileObject webDD, FilterMapping mapping) {
            super(webApp, webDD);
            this.mapping = mapping;
        }
        
        @Override
        protected void doChange() {
            webApp.removeFilterMapping(mapping);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {mapping.getFilterName()};
            return MessageFormat.format(NbBundle.getMessage(WebXmlSafeDelete.class, "TXT_WebXmlFilterMappingSafeDelete"), args);
        }
        
        @Override
        protected void undo() {
            webApp.addFilterMapping(mapping);
        }

        protected String getName() {
            return null;
        }
    }

    private static class ListenerRemoveElement extends WebRefactoringElement{
        
        private Listener listener;
        public ListenerRemoveElement(WebApp webApp, FileObject webDD, Listener listener) {
            super(webApp, webDD);
            this.listener = listener;
        }
        
        @Override
        protected void doChange() {
            webApp.removeListener(listener);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {listener.getListenerClass()};
            return MessageFormat.format(NbBundle.getMessage(WebXmlSafeDelete.class, "TXT_WebXmlListenerSafeDelete"), args);
        }
        
        @Override
        protected void undo() {
            webApp.addListener(listener);
        }

        protected String getName() {
            return null;
        }
    }
    
}
