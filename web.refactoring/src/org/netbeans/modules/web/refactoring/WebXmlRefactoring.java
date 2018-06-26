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
package org.netbeans.modules.web.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A base class for web.xml refactorings.
 *
 * @author Erno Mononen
 */
public abstract class WebXmlRefactoring implements WebRefactoring{

    private static final Logger LOGGER = Logger.getLogger(WebXmlRefactoring.class.getName());

    protected final FileObject webDD;

    private AtomicReference<WebApp> webModel = new AtomicReference<WebApp>();

    protected WebXmlRefactoring(FileObject webDD) {
        this.webDD = webDD;
    }
    
    @Override
    public Problem preCheck() {
        WebApp model = getWebModel();
        if (model == null) {
            return new Problem(false, NbBundle.getMessage(WebXmlRefactoring.class, "TXT_WebXmlNotReadable"));
        } else if (model.getStatus() == WebApp.STATE_INVALID_UNPARSABLE){
            return new Problem(false, NbBundle.getMessage(WebXmlRefactoring.class, "TXT_WebXmlInvalidProblem"));
        } else if (model.getStatus() == WebApp.STATE_INVALID_OLD_VERSION){
            return new Problem(false, NbBundle.getMessage(WebXmlRefactoring.class, "TXT_WebXmlOldVersion"));
        }
        return null;
        
    }
    
    protected List<Servlet> getServlets(String clazz){
        List<Servlet> result = new ArrayList<Servlet>();
        for(Servlet servlet : getWebModel().getServlet())
            if (clazz.equals(servlet.getServletClass())){
                result.add(servlet);
            }
        return result;
    }
    
    protected List<Filter> getFilters(String clazz){
        List<Filter> result = new ArrayList<Filter>();
        for (Filter filter : getWebModel().getFilter()){
            if (clazz.equals(filter.getFilterClass())){
                result.add(filter);
            }
        }
        return result;
    }
    
    protected List<Listener> getListeners(String clazz){
        List<Listener> result = new ArrayList<Listener>();
        for (Listener listener : getWebModel().getListener()){
            if (clazz.equals(listener.getListenerClass())){
                result.add(listener);
            }
        }
        return result;
    }
    
    protected List<EjbRef> getEjbRefs(String clazz, boolean remote){
        List<EjbRef> result = new ArrayList<EjbRef>();
        for (EjbRef ejbRef : getWebModel().getEjbRef()){
            if (remote && clazz.equals(ejbRef.getRemote())){
                result.add(ejbRef);
            } else if (clazz.equals(ejbRef.getHome())){
                result.add(ejbRef);
            }
        }
        return result;
    }
    
    protected List<EjbLocalRef> getEjbLocalRefs(String clazz, boolean localHome){
        List<EjbLocalRef> result = new ArrayList<EjbLocalRef>();
        for (EjbLocalRef ejbLocalRef : getWebModel().getEjbLocalRef()){
            if (localHome && clazz.equals(ejbLocalRef.getLocalHome())){
                result.add(ejbLocalRef);
            } else if (clazz.equals(ejbLocalRef.getLocal())){
                result.add(ejbLocalRef);
            }
        }
        return result;
    }

    protected WebApp getWebModel() {
        if (webDD == null) {
            return null;
        }

        WebApp model = webModel.get();
        if (model != null) {
            return model;
        }

        try {
            model = DDProvider.getDefault().getDDRoot(webDD);
            webModel.compareAndSet(null, model);
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
        }
        return model;
    }

    protected abstract static class WebRefactoringElement extends SimpleRefactoringElementImplementation{
        
        protected final WebApp webApp;
        protected final FileObject webDD;
        
        public WebRefactoringElement(WebApp webApp, FileObject webDD) {
            this.webApp = webApp;
            this.webDD = webDD;
        }
        
        public void performChange() {
            doChange();
            writeDD();
        }
        
        private void writeDD(){
            try{
                webApp.write(webDD);
            }catch(IOException ioe){
                Exceptions.printStackTrace(ioe);
            }
        }
        
        protected abstract void doChange();
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getParentFile() {
            return webDD;
        }
        
        public PositionBounds getPosition() {
            try {
                //XXX: does not work correctly when a class is specified more than once in web.xml
                return new PositionBoundsResolver(DataObject.find(webDD),getName()).getPositionBounds();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        @Override
        public void undoChange() {
            undo();
            writeDD();
        }
        
        protected abstract String getName();
        
        protected abstract void undo();
    }
    
}
