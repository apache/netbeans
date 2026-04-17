/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.wsitconf.wizard;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.ProjectInfo;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.util.GenerationUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class STSWizardCreator {

    protected static final int JSE_PROJECT_TYPE = 0;
    protected static final int WEB_PROJECT_TYPE = 1;
    protected static final int EJB_PROJECT_TYPE = 2;
    
    public static final String STS_WEBSERVICE = "sts-webservice";   // NOI18N
    
    private int projectType;

    private Project project;
    private WizardDescriptor wiz;

    public boolean wsitSupported, jsr109Supported;

    private static final Logger logger = Logger.getLogger(STSWizardCreator.class.getName());
    
    public STSWizardCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
    
    public STSWizardCreator(Project project) {
        this.project = project;
    }
    
    public void createSTS() {
        final ProgressHandle handle = ProgressHandle.createHandle( 
                NbBundle.getMessage(STSWizardCreator.class, "TXT_StsGeneration")); //NOI18N

        initProjectInfo(project);
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    handle.start(100);
                    generateWsFromWsdl15(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if(message != null) {
                        logger.log(Level.INFO, null, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } else {
                        logger.log(Level.INFO, null, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }
    
    private void initProjectInfo(Project project) {
        WsitProvider wsitProvider = project.getLookup().lookup(WsitProvider.class);
        if (wsitProvider != null) {
            jsr109Supported = wsitProvider.isJsr109Project();
            wsitSupported = wsitProvider.isWsitSupported();
        }
        
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
            if (J2eeModule.Type.EJB.equals(moduleType)) {
                projectType = ProjectInfo.EJB_PROJECT_TYPE;
            } else if (J2eeModule.Type.WAR.equals(moduleType)) {
                projectType = ProjectInfo.WEB_PROJECT_TYPE;
                Util.checkMetroRtLibrary(project, false);
            } else if (J2eeModule.Type.CAR.equals(moduleType)) {
                projectType = ProjectInfo.CAR_PROJECT_TYPE;
            } else {
                projectType = ProjectInfo.JSE_PROJECT_TYPE;
            }
        } else {
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            EjbJar em = EjbJar.getEjbJar(project.getProjectDirectory());
            if (wm != null) {
                projectType = WEB_PROJECT_TYPE;
            } else if (em != null) {
                projectType = EJB_PROJECT_TYPE;
            } else {
                projectType = JSE_PROJECT_TYPE;
            }
        }
    }
    
    private void generateWsFromWsdl15(final ProgressHandle handle) throws Exception {
        String wsdlFilePath = (String) wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
        //convert to URI first to take care of spaces
        final URL wsdlURL = normalizedWsdlFilePath.toURI().toURL();
        final WsdlService service = (WsdlService) wiz.getProperty(WizardProperties.WSDL_SERVICE);
        if (service==null) {
            handle.finish();
            return;
        } else {
            final WsdlPort port = (WsdlPort) wiz.getProperty(WizardProperties.WSDL_PORT);
            //String portJavaName = port.getJavaName();   
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
            // don't set the packageName for modeler (use the default one generated from target Namespace)
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                @Override
                public void modelCreated(WsdlModel model) {
                    WsdlService service1 = model.getServiceByName(service.getName());
                    WsdlPort port1 = service1.getPortByName(port.getName());
                    port1.setSOAPVersion(port.getSOAPVersion());
                    FileObject targetFolder = Templates.getTargetFolder(wiz);
                    String targetName = Templates.getTargetName(wiz);
                    try {
                        generateProviderImplClass(project, targetFolder, targetName, service1, port1, wsdlURL);
                        handle.finish();
                    } catch (Exception ex) {
                        handle.finish();
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }
    
    public void generateProviderImplClass(Project project, FileObject targetFolder,
            final String targetName, final WsdlService service, final WsdlPort port, 
            URL wsdlURL) throws Exception 
    {
        initProjectInfo(project);
        
        String serviceID = service.getName();
        
        final JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
            
        FileObject implClassFo = GenerationUtils.createClass(targetFolder, targetName, null);
        ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);            
        String serviceImplPath = classPath.getResourceName(implClassFo, '.', false);
        String portJavaName = port.getJavaName();
        String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf('.'));

        serviceID = jaxWsSupport.addService(targetName, serviceImplPath, 
                wsdlURL.toString(), service.getName(), port.getName(), artifactsPckg, 
                jsr109Supported && Util.isJavaEE5orHigher(project), true);
        final String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
                       
        final String[] fqn = new String[1];
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                if (genUtils!=null) {     
                    TreeMaker make = workingCopy.getTreeMaker();
                    ClassTree javaClass = genUtils.getClassTree();
                    Element element = workingCopy.getTrees().getElement( 
                            workingCopy.getTrees().getPath(
                                    workingCopy.getCompilationUnit(), javaClass));
                    if ( element instanceof TypeElement ){
                        fqn[0] = ((TypeElement)element).getQualifiedName().toString();
                    }
                    ClassTree modifiedClass;
                    
                    // not found on classpath, because the runtime jar is not on classpath by default
                    String baseStsImpl = "com.sun.xml.ws.security.trust.sts.BaseSTSImpl"; //NOI18N

                    // create parameters
                    List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();
                    AnnotationTree resourceAnnotation = make.Annotation(
                        make.QualIdent("javax.annotation.Resource"), //NOI18N
                        Collections.<ExpressionTree>emptyList()
                    );
                    annotations.add(resourceAnnotation);
                    
                    List<VariableTree> classField = new ArrayList<VariableTree>();
                    // final ObjectOutput arg0
                    classField.add(make.Variable(
                            make.Modifiers(
                                Collections.<Modifier>emptySet(),
                                annotations
                            ),
                            "context", // name
                            make.QualIdent("javax.xml.ws.WebServiceContext"), //NOI18N parameter type
                            null // initializer - does not make sense in parameters.
                    ));
                    
                    modifiedClass = genUtils.addClassFields(javaClass, classField);
                    
                    ParameterizedTypeTree t = make.ParameterizedType(
                            make.QualIdent("javax.xml.ws.Provider"), //NOI18N
                            Collections.singletonList(
                                    make.QualIdent("javax.xml.transform.Source"))); //NOI18N
                    modifiedClass = make.addClassImplementsClause(modifiedClass, t);
                    modifiedClass = make.setExtends(modifiedClass, make.Identifier(baseStsImpl));
                    
                    //add @WebServiceProvider annotation
                    List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                    attrs.add(
                        make.Assignment(make.Identifier("serviceName"), make.Literal(service.getName()))); //NOI18N
                    attrs.add(
                        make.Assignment(make.Identifier("portName"), make.Literal(port.getName()))); //NOI18N
                    attrs.add(
                        make.Assignment(make.Identifier("targetNamespace"), make.Literal(port.getNamespaceURI()))); //NOI18N
                    attrs.add(
                        make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlLocation))); //NOI18N
                    AnnotationTree WSAnnotation = make.Annotation(
                        make.QualIdent("javax.xml.ws.WebServiceProvider"), //NOI18N
                        attrs
                    );
                    modifiedClass = genUtils.addAnnotation(modifiedClass, WSAnnotation);
                                        
                    //add @WebServiceProvider annotation
                    TypeElement modeAn = workingCopy.getElements().getTypeElement("javax.xml.ws.ServiceMode"); //NOI18N
                    List<ExpressionTree> attrsM = new ArrayList<ExpressionTree>();

                    ExpressionTree mstree = make.MemberSelect(
                            make.QualIdent("javax.xml.ws.Service.Mode"), "PAYLOAD");    // NOI18N
                    
                    attrsM.add(
                        make.Assignment(make.Identifier("value"), mstree)); //NOI18N
                    AnnotationTree modeAnnot = make.Annotation(
                        make.QualIdent(modeAn), 
                        attrsM
                    );
                    modifiedClass = genUtils.addAnnotation(modifiedClass, modeAnnot);

                    // add @Stateless annotation
                    if (projectType == EJB_PROJECT_TYPE) {//EJB project
                        AnnotationTree statelessAnnotation = make.Annotation(
                                make.QualIdent("javax.ejb.Stateless"),  // NOI18N
                            Collections.<ExpressionTree>emptyList()
                        );
                        modifiedClass = genUtils.addAnnotation(modifiedClass, 
                                statelessAnnotation);
                    }

                    // create parameters
                    List<VariableTree> params = new ArrayList<VariableTree>();
                    // final ObjectOutput arg0
                    params.add(make.Variable(
                            make.Modifiers(
                                Collections.<Modifier>emptySet(),
                                Collections.<AnnotationTree>emptyList()
                            ),
                            "rstElement", // name
                            make.QualIdent("javax.xml.transform.Source"), //NOI18N parameter type
                            null // initializer - does not make sense in parameters.
                    ));

                    // create method
                    ModifiersTree methodModifiers = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>emptyList()
                    );
                    
                    List<ExpressionTree> exc = new ArrayList<ExpressionTree>();
                    
                    MethodTree method = make.Method(
                            methodModifiers, // public
                            "invoke", // operation name
                            make.QualIdent("javax.xml.transform.Source"), //NOI18N return type 
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            params,
                            exc, // throws 
                            "{ return super.invoke(rstElement); }", // body text
                            null // default value - not applicable here, used by annotations
                    );
                    modifiedClass =  make.addClassMember(modifiedClass, method); 
                    
                    // create method
                    ModifiersTree msgContextModifiers = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PROTECTED),
                        Collections.<AnnotationTree>emptyList()
                    );
                    
                    List<ExpressionTree> excMsg = new ArrayList<ExpressionTree>();
                    
                    MethodTree methodMsgContext = make.Method(
                            msgContextModifiers, // public
                            "getMessageContext", // operation name
                            make.QualIdent("javax.xml.ws.handler.MessageContext"), //NOI18N return type 
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            Collections.<VariableTree>emptyList(),
                            excMsg, // throws 
                            "{ MessageContext msgCtx = context.getMessageContext();\nreturn msgCtx; }", // body text
                            null // default value - not applicable here, used by annotations
                    );
                    modifiedClass =  make.addClassMember(modifiedClass, methodMsgContext);                     
                    
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }

            @Override
            public void cancel() { 
            }
        };
        
        targetSource.runModificationTask(task).commit();

        String url = "/" + targetName + "Service";
        String mexUrl = url +"/mex";

        WsitProvider wsitProvider = project.getLookup().lookup(WsitProvider.class);
        if (wsitProvider != null) {
            wsitProvider.addServiceDDEntry(serviceImplPath, mexUrl, targetName);
        }

        FileObject ddFolder = jaxWsSupport.getDeploymentDescriptorFolder();
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
        if(sunjaxwsFile == null){
            WSUtils.generateSunJaxwsFile(ddFolder);
            sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
        }
        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
        Endpoint endpoint = endpoints.newEndpoint();
        endpoint.setEndpointName(Util.MEX_NAME);
        endpoint.setImplementation(Util.MEX_CLASS_NAME);
        endpoint.setUrlPattern(mexUrl);
        endpoints.addEnpoint(endpoint);
        
        if ( fqn[0]!= null ){
            endpoint = endpoints.newEndpoint();
            endpoint.setEndpointName(targetName);
            endpoint.setImplementation(fqn[0]);
            endpoint.setUrlPattern(url);
            endpoints.addEnpoint(endpoint);
        }
        
        FileLock lock = null;
        OutputStream os = null;
        synchronized (this) {
            try{
                lock = sunjaxwsFile.lock();
                os = sunjaxwsFile.getOutputStream(lock);
                endpoints.write(os);
            }finally{
                if(lock != null)
                    lock.releaseLock();

                if(os != null)
                    os.close();
            }
        }
        
        //open in the editor
        DataObject dobj = DataObject.find(implClassFo);
        implClassFo.setAttribute(STS_WEBSERVICE, Boolean.TRUE);
        implClassFo.addFileChangeListener( new FileChangeAdapter(){
           /* (non-Javadoc)
            * @see org.openide.filesystems.FileChangeAdapter#fileDeleted(org.openide.filesystems.FileEvent)
            */
            @Override
            public void fileDeleted( FileEvent fe ) {
                try {
                    jaxWsSupport.removeNonJsr109Entries(Util.MEX_NAME);
                    jaxWsSupport.removeNonJsr109Entries(fqn[0]);
                    jaxWsSupport.removeNonJsr109Entries(targetName);
                }
                catch(IOException e ){
                    logger.log( Level.WARNING, null , e);
                }
            } 
        });
        openFileInEditor(dobj);
    }

    private static void openFileInEditor(DataObject dobj){
        final EditorCookie ec = dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }
        
}
