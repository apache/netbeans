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
package org.netbeans.modules.websvc.editor.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.TreeUtilities;

import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.RulesEngine;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 * @author Ajit.Bhate@Sun.COM
 */
public class WebServicesHintsProvider {
    
    private static final Object singleInstanceLock = new Object();
    private static WebServicesHintsProvider runningInstance = null;
    
    private FileObject file;
    private boolean cancelled = false;
    private ProblemContext context = null;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
    private final Object cancellationLock = new Object();
    private WSDLModel wsdlModel;
    private Service service;
    private ComponentListener changeListener;
    
    private static final RequestProcessor WS_HINTS_RP = new RequestProcessor(WebServicesHintsProvider.class);
    
    public WebServicesHintsProvider(FileObject file) {
        this.file = file;
    }
    
    public void cancel() {
        cancelled = true;
        
        synchronized(cancellationLock){
            if (context != null){
                context.setCancelled(true);
            }
        }
    }
    
    public void run(final CompilationInfo info) throws Exception{
        synchronized(singleInstanceLock){
            if (runningInstance != null){
                runningInstance.cancel();
            }
            runningInstance = this;
            // the 'cancelled' flag must be reset as the instance of WebServicesHintsProvider is reused
            cancelled = false;
            problemsFound.clear();
            for (Tree tree : info.getCompilationUnit().getTypeDecls()){
                if (isCancelled()){
                    break;
                }
                
                if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())){
                    TreePath path = info.getTrees().getPath(info.getCompilationUnit(), tree);
                    TypeElement javaClass = (TypeElement) info.getTrees().getElement(path);
                    if (javaClass != null) {
                        initServiceMetadata(javaClass);
                        createProblemContext(info, javaClass);

                        RulesEngine rulesEngine = new WebServicesRulesEngine();
                        javaClass.accept(rulesEngine, context);
                        problemsFound.addAll(rulesEngine.getProblemsFound());

                        synchronized(cancellationLock){
                            context = null;
                        }
                    }
                }
            }
            
            //TODO: should we really reset the errors if the task is cancelled?
            HintsController.setErrors(file, "WebService Verification", problemsFound); //NOI18N
            runningInstance = null;
        }
    }
    
    private void createProblemContext(CompilationInfo info,
            TypeElement javaClass){
        context = new ProblemContext();
        context.setJavaClass(javaClass);
        context.setFileObject(file);
        context.setCompilationInfo(info);
        if(service!=null) context.addUserObject(service);
        if(wsdlModel!=null) context.addUserObject(wsdlModel);
    }
    
    private void initServiceMetadata(TypeElement javaClass) {
        if (service == null) {
            Project owner = FileOwnerQuery.getOwner(file);
            if(owner!=null) {
                JaxWsModel jaxwsModel = owner.getLookup().lookup(JaxWsModel.class);
                if (jaxwsModel != null) {
                    service = jaxwsModel.findServiceByImplementationClass(javaClass.getQualifiedName().toString());
                }
            }
        }
        if (service != null && service.getLocalWsdlFile()!=null) {
            JAXWSSupport jaxwsSupport = JAXWSSupport.getJAXWSSupport(file);
            if(jaxwsSupport!=null) {
                FileObject wsdlFolder = jaxwsSupport.getLocalWsdlFolderForService(service.getName(), false);
                if(wsdlFolder!=null) {
                    FileObject wsdlFo = wsdlFolder.getFileObject(service.getLocalWsdlFile());
                    if ( wsdlFo == null ){
                        return;
                    }
                    WSDLModel tmpModel = WSDLModelFactory.getDefault().getModel(
                            Utilities.getModelSource(wsdlFo, true));
                    if(tmpModel!=wsdlModel) {
                        if(wsdlModel!=null) {
                            if(changeListener!=null) {
                                wsdlModel.removeComponentListener(changeListener);
                                changeListener = null;
                            }
                        }
                        wsdlModel = tmpModel;
                        if(wsdlModel!=null) {
                            if(changeListener==null) 
                                changeListener = WeakListeners.create(ComponentListener.class,
                                        new WsdlModelListener(file), wsdlModel);
                            wsdlModel.addComponentListener(changeListener);
                        }
                    }
                }
            }
        }
    }
    
    public boolean isCancelled(){
        return cancelled;
    }
    
    public List<? extends ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    public static class ProblemFinderCompInfo extends WebServicesHintsProvider implements CancellableTask<CompilationInfo>{
        public ProblemFinderCompInfo(FileObject file){
            super(file);
        }
    }
    
    public static class ProblemFinderCompControl extends WebServicesHintsProvider implements CancellableTask<CompilationController>{
        public ProblemFinderCompControl(FileObject file){
            super(file);
        }
        
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            super.run(controller);
        }
    }

    private abstract class RescanTrigger implements Runnable {
        private FileObject file;
        private JavaSource javaSrc;
        
        private final RequestProcessor.Task wsHintsTask = WS_HINTS_RP.create(this);
        
        RescanTrigger(FileObject file){
            this.file = file;
        }
        
        void rescan(){
            if (javaSrc == null) {
                javaSrc = JavaSource.forFileObject(file);
            }
            if (javaSrc != null){
                try{
                    if(EventQueue.isDispatchThread()) {
                        wsHintsTask.schedule(100);
                    } else {
                        javaSrc.runUserActionTask(new ProblemFinderCompControl(file), true);
                    }
                } catch (IOException e){
                }
            }
        }
        
        @Override
        public void run() {
            try {
                javaSrc.runUserActionTask(new ProblemFinderCompControl(file), true);
            } catch (IOException e){}
        }
    }
    
    private class WsdlModelListener extends RescanTrigger implements ComponentListener {
        WsdlModelListener(FileObject file){
            super(file);
        }
        public void valueChanged(ComponentEvent evt) {
            if(!WebServicesHintsProvider.this.isCancelled()) {
                rescan();
            }
        }
        public void childrenAdded(ComponentEvent evt) {
            if(!WebServicesHintsProvider.this.isCancelled()) {
                rescan();
            }
        }
        public void childrenDeleted(ComponentEvent evt) {
            if(!WebServicesHintsProvider.this.isCancelled()) {
                rescan();
            }
        }
    }
}
