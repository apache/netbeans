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
package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Matula
 */
abstract class WSITRefactoringPlugin<T extends AbstractRefactoring> extends JavaRefactoringPlugin {
    protected static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.websvc.wsitconf.refactoring");

    protected static final String WS_ANNOTATION = "javax.xml.ws.WebService";
    protected static final String WSDL_LOCATION_ELEMENT = "wsdlLocation";
    
    protected final T refactoring;
    protected final TreePathHandle[] treePathHandle;

    public WSITRefactoringPlugin(T refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class).toArray(new TreePathHandle[0]);
        LOGGER.log(Level.FINE, "refactoring: {0}; refactoring sources: {1}", new Object[]{refactoring.getClass().getName(), Arrays.asList(treePathHandle)});
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        LOGGER.log(Level.FINE, "prepare()");

        Problem result = null;
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        JavaSource source = JavaSource.create(cpInfo, treePathHandle[0].getFileObject());
                fireProgressListenerStart(AbstractRefactoring.PREPARE, 5);
                try {
                    source.runUserActionTask(new CancellableTask<CompilationController>() {

                        public void cancel() {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        public void run(CompilationController info) throws Exception {
                            info.toPhase(JavaSource.Phase.RESOLVED);
                            for (TreePathHandle tph : treePathHandle) {
                                Element el = tph.resolveElement(info);
                                if (el == null) return;
                                
                                switch (el.getKind()) {
                                case METHOD: {
                                    ElementHandle elh = ElementHandle.create(el);
                                    FileObject file = SourceUtils.getFile(elh, info.getClasspathInfo());

                                    if (file == null) {
                                        ErrorManager.getDefault().log(
                                                ErrorManager.INFORMATIONAL, "WSIT: Null instance returned from SourceUtils.getFile; element not found " + el);
                                        return;
                                    }
                                    fireProgressListenerStep();
                                    Element javaClass = el.getEnclosingElement();
                                    if (isWebSvcFromWsdl(javaClass)) return;
                                    fireProgressListenerStep();
                                    JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(file);
                                    if (supp == null) return;
                                    fireProgressListenerStep();
                                    Project p = FileOwnerQuery.getOwner(file);
                                    if (p == null) return;
                                    WSDLModel model = null;
                                    try {
                                        model = WSITModelSupport.getModelForServiceFromJava(file, p, false, null);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    fireProgressListenerStep();
                                    if (model == null) return;
                                    refactoringElements.add(refactoring, createMethodRE(el.getSimpleName().toString(), model));
                                    fireProgressListenerStep();
                                    break;
                                } case CLASS:
                                case INTERFACE:
                                case ANNOTATION_TYPE:
                                case ENUM: {
                                    ElementHandle elh = ElementHandle.create(el);
                                    FileObject file = SourceUtils.getFile(elh, info.getClasspathInfo());

                                    if (file == null) {
                                        ErrorManager.getDefault().log(
                                                ErrorManager.INFORMATIONAL, "WSIT: Null instance returned from SourceUtils.getFile; element not found " + el);
                                        return;
                                    }
                                    fireProgressListenerStep();
                                    if (isWebSvcFromWsdl(el)) return;
                                    fireProgressListenerStep();
                                    JAXWSSupport supp = JAXWSSupport.getJAXWSSupport(file);
                                    if (supp == null) return;
                                    WSDLModel model = null;
                                    fireProgressListenerStep();
                                    Project p = FileOwnerQuery.getOwner(file);
                                    if (p == null) return;
                                    try {
                                        model = WSITModelSupport.getModelForServiceFromJava(file, p, false, null);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    fireProgressListenerStep();
                                    if (model == null){
                                        return;
                                    }
                                    refactoringElements.addFileChange(refactoring, createClassRE(model));
                                    fireProgressListenerStep();
                                    break;
                                }
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                throw (RuntimeException) new RuntimeException().initCause(ioe);
            } finally {
                fireProgressListenerStop();
            }
        
        return result;
    }
            
    protected abstract RefactoringElementImplementation createMethodRE(String methodName, WSDLModel model);
    protected abstract RefactoringElementImplementation createClassRE(WSDLModel model);

    protected static boolean isWebSvcFromWsdl(Element element){
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (WS_ANNOTATION.equals(((TypeElement) ann.getAnnotationType().asElement()).getQualifiedName())) {
                for (ExecutableElement annVal : ann.getElementValues().keySet()) {
                    if (WSDL_LOCATION_ELEMENT.equals(annVal.getSimpleName().toString())){
                        return true;
                    }
                }
            }
        }
        return false;
    }    
    
    protected static Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            Problem p = result;
            while (p.getNext() != null) {
                p = p.getNext();
            }
            p.setNext(problem);
            return result;
        }
    }

    protected abstract static class AbstractRefactoringElement extends SimpleRefactoringElementImplementation {
        protected final WSDLModel model;
        protected final FileObject file;

        public AbstractRefactoringElement(WSDLModel model) {
            this.model = model;
            this.file = Util.getFOForModel(model);
        }

        /** Returns text describing the refactoring element.
         * @return Text.
         */
        public String getText() {
            return getDisplayText();
        }

        /** Returns file that the element affects (relates to)
         * @return File
         */
        public FileObject getParentFile() {
            return file;
        }

        /** Returns position bounds of the text to be affected by this refactoring element.
         */
        public PositionBounds getPosition() {
            return null;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
    
    protected static class AbstractRenameConfigElement extends AbstractRefactoringElement{

        AbstractRenameConfigElement( String oldName, String newName,
                WSDLModel model )
        {
            super(model);
            setOldConfigName(oldName);
            setNewConfigName(newName);
        }
        
        AbstractRenameConfigElement( WSDLModel model )
        {
            super(model);
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.refactoring.spi.RefactoringElementImplementation#getDisplayText()
         */
        @Override
        public String getDisplayText() {
            return NbBundle.getMessage(WSITRenamePackagePlugin.class, 
                    "TXT_WsitXmlClassRename" , oldConfigName, newConfigName );  // NOI18N
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.refactoring.spi.RefactoringElementImplementation#performChange()
         */
        @Override
        public void performChange() {
            FileLock lock = null;
            FileObject parentFile = getParentFile();
            try {
                lock = parentFile.lock();
                parentFile.rename(lock, newConfigName, getParentFile().getExt());
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (lock != null) lock.releaseLock();
            }
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation#undoChange()
         */
        @Override
        public void undoChange() {
            FileLock lock = null;
            FileObject parentFile = getParentFile();
            try {
                lock = parentFile.lock();
                parentFile.rename(lock, oldConfigName, getParentFile().getExt());
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (lock != null) lock.releaseLock();
            }
        }
        
        protected void setOldConfigName( String oldName ){
            oldConfigName = oldName;
        }
        
        protected void setNewConfigName( String newName ){
            newConfigName = newName;
        }
        
        private String oldConfigName;
        private String newConfigName;
    }
}
