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
