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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlChangeListener;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperGenerator;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.openide.NotifyDescriptor;
import org.xml.sax.SAXException;


/*
 *  Children of the web service node, namely,
 *  the operations of the webservice
 */
public class JaxWsChildren extends Children.Keys<Object>/* implements MDRChangeListener  */{
    private java.awt.Image cachedIcon;   
    private static final String OPERATION_ICON = "org/netbeans/modules/websvc/core/webservices/ui/resources/wsoperation.png"; //NOI18N
    
    private FileObject implClass;
    private Service service;
    private FileObject srcRoot;
    
    private WsdlModel wsdlModel;
    private WsdlModeler wsdlModeler;
    private boolean modelGenerationFinished;
    
    private WsdlChangeListener wsdlChangeListener;

    private FileChangeListener fcl;
    
    private RequestProcessor requestProcessor = 
        new RequestProcessor("JaxWs-request-processor");        // NOI18N
    
    private static Comparator<WebOperationInfo> OPERATION_INFO_COMPARATOR = 
            new WebOperationInfoComparator();
    
    public JaxWsChildren(Service service, FileObject srcRoot, FileObject implClass) {
        super();
        this.service = service;
        this.srcRoot=srcRoot;
        this.implClass = implClass;
    } 
    
    private List<ExecutableElement> getPublicMethods(CompilationController controller, TypeElement classElement) throws IOException {
        List<? extends Element> members = classElement.getEnclosedElements();
        List<ExecutableElement> methods = ElementFilter.methodsIn(members);
        List<ExecutableElement> publicMethods = new ArrayList<ExecutableElement>();
        for (ExecutableElement method:methods) {
            Set<Modifier> modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)) {
                publicMethods.add(method);
            }
        }
        return publicMethods;
    }

    @Override
    protected void addNotify() {
        if (isFromWsdl()) {
                FileObject localWsdlFolder = getJAXWSSupport().getLocalWsdlFolderForService(service.getName(),false);
                if (localWsdlFolder == null) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO,"missing folder for wsdl file"); // NOI18
                    updateKeys();
                    return;
                }
                FileObject wsdlFo =
                    localWsdlFolder.getFileObject(service.getLocalWsdlFile());
                if (wsdlFo==null) return;
                if (wsdlModeler==null) { 
                    wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFo.toURL());
                }
                if (wsdlModeler==null) {
                    return;
                }
                wsdlChangeListener = new WsdlChangeListener() {

                    public void wsdlModelChanged( WsdlModel oldWsdlModel,
                            WsdlModel newWsdlModel )
                    {
                        wsdlModel = newWsdlModel;
                        updateKeys();
                        ((JaxWsNode) getNode()).changeIcon();
                    }
                };
                wsdlModeler.addWsdlChangeListener(wsdlChangeListener);
                String packageName = service.getPackageName();
                if (packageName!=null && service.isPackageNameForceReplace()) {
                    // set the package name for the modeler
                    wsdlModeler.setPackageName(packageName);
                } else {
                    wsdlModeler.setPackageName(null);
                }
                JAXWSSupport support = getJAXWSSupport();
                wsdlModeler.setCatalog(support.getCatalog());
                setBindings(support,wsdlModeler,service);
                modelGenerationFinished=false;
                ((JaxWsNode)getNode()).changeIcon();
                wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                    public void modelCreated(WsdlModel model) {
                        modelGenerationFinished=true;                        
                        if (model==null) {
                            DialogDisplayer.getDefault().notify(
                                    new JaxWsUtils.WsImportServiceFailedMessage(wsdlModeler.getCreationException()));
                        }
                    }
                });
        } else {
            assert(implClass != null);
            if (fcl == null) {
                fcl = new FileChangeAdapter() {
                    @Override
                    public void fileChanged(FileEvent fe) {
                        updateKeys();
                    }
                };
                implClass.addFileChangeListener(fcl);
            }
            
            updateKeys();
        }
    }
    
    @Override
    protected void removeNotify() {
        if (wsdlModeler!=null) {
            wsdlModeler.removeWsdlChangeListener(wsdlChangeListener);
        } 
        if (fcl != null) {
            implClass.removeFileChangeListener(fcl);
            fcl = null;
        }
        setKeys(Collections.<Object>emptySet());
    }
    
    private void updateKeys() {
        if (isFromWsdl()) {
            List<WsdlOperation> keys = null;
            if (wsdlModel!=null) {
                WsdlService wsdlService = wsdlModel.getServiceByName(service.getServiceName());
                if (wsdlService!=null) {
                    WsdlPort wsdlPort = wsdlService.getPortByName(service.getPortName());
                    if (wsdlPort!=null) {
                        keys =  wsdlPort.getOperations();
                    }
                }
            }
            if ( keys != null ){
                keys.sort(WsdlOperationComparator.getInstance());
                setKeys(keys);
            }
            else {
                setKeys( Collections.emptyList());
            }
        } else {
            requestProcessor.post(new Runnable() {
                public void run() {
                    final List<?>[] keys = new List<?>[1];
                    if (implClass != null) {
                        JavaSource javaSource = JavaSource.forFileObject(implClass);
                        if (javaSource!=null) {
                            CancellableTask<CompilationController> task = 
                                new CancellableTask<CompilationController>() 
                                {
                                    @Override
                                    public void run(CompilationController controller) 
                                        throws IOException 
                                    {
                                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                                        TypeElement typeElement = SourceUtils.
                                            getPublicTopLevelElement(controller);
                                        // find WS operations
                                        // excluding @WebMethod(exclude=true). See the issue 228292.
                                        List<ExecutableElement> publicMethods = 
                                            getPublicMethods(controller, typeElement);
                                        List<ExecutableElement> webMethods = 
                                            new ArrayList<ExecutableElement>();
                                        // map for storing @WebMethod annotation mirror
                                        java.util.Map<ExecutableElement, AnnotationMirror> webMethodAnnMap = 
                                                new java.util.HashMap<ExecutableElement, AnnotationMirror>();
                                        for(ExecutableElement method:publicMethods) {
                                            AnnotationMirror webMethodAnn = 
                                                getWebMethodAnnotation(method);
                                            if (webMethodAnn != null) {
                                                boolean exclude = false;
                                                java.util.Map<? extends ExecutableElement, 
                                                    ? extends AnnotationValue> expressions = webMethodAnn.getElementValues();
                                                for(Entry<? extends ExecutableElement, 
                                                        ? extends AnnotationValue> entry: 
                                                            expressions.entrySet()) 
                                                {
                                                    if (entry.getKey().getSimpleName().
                                                            contentEquals("exclude"))//NOI18N 
                                                    { 
                                                        Object value = expressions.get(entry.getKey()).getValue();
                                                        if (Boolean.TRUE.equals(value)) {
                                                            exclude = true;
                                                        }
                                                        break;
                                                    }
                                                }
                                                if (!exclude) {
                                                    webMethods.add(method);
                                                    webMethodAnnMap.put(method, webMethodAnn);
                                                }
                                            } 
                                            else {
                                                // add un-annotated public method by default (issue 228292)
                                                webMethods.add(method);
                                            }
                                        } // for

                                        
                                        // create list of operations;
                                        List<WebOperationInfo> webOperations = 
                                            new ArrayList<WebOperationInfo>();
                                        
                                        for (ExecutableElement webMethod:webMethods) {
                                            // web operation name
                                            WebOperationInfo webOperation = 
                                                new WebOperationInfo();
                                            // get @WebMethod annotation from the map
                                            AnnotationMirror webMethodAnn = webMethodAnnMap.get(webMethod);
                                            if (webMethodAnn != null) {
                                                java.util.Map<? extends ExecutableElement, 
                                                    ? extends AnnotationValue> expressions = webMethodAnn.getElementValues();
                                                for(Entry<? extends ExecutableElement, 
                                                        ? extends AnnotationValue> entry: 
                                                            expressions.entrySet()) 
                                                {
                                                    if (entry.getKey().getSimpleName().
                                                            contentEquals("operationName"))//NOI18N 
                                                    { 
                                                        webOperation.setOperationName(
                                                                (String)expressions.get(
                                                                        entry.getKey()).
                                                                            getValue());
                                                    }
                                                }
                                            }
                                            if (webOperation.getOperationName() == null) 
                                            {
                                                webOperation.setOperationName(
                                                        webMethod.getSimpleName().
                                                            toString());
                                            }
                                            
                                            // return type
                                            TypeMirror returnType = webMethod.
                                                getReturnType();
                                            if (returnType instanceof DeclaredType ) 
                                            {
                                                TypeElement element = (TypeElement)(
                                                        (DeclaredType)returnType).
                                                        asElement();
                                                webOperation.setReturnType(
                                                        element.getQualifiedName().toString());
                                            } else { // for primitive type
                                                webOperation.setReturnType(
                                                        returnType.toString());
                                            }                                               
                                            
                                            // parameter types
                                            List<? extends VariableElement> params = 
                                                webMethod.getParameters();
                                            List<String> paramTypes = 
                                                new ArrayList<String>();
                                            for (VariableElement param:params) {
                                                TypeMirror type = param.asType();
                                                if (type instanceof DeclaredType ) {
                                                    TypeElement element = 
                                                        (TypeElement)((DeclaredType)type).
                                                                asElement();
                                                    paramTypes.add(
                                                            element.getQualifiedName().
                                                            toString());
                                                } else { // for primitive type
                                                    paramTypes.add(type.toString());
                                                }
                                            }
                                            webOperation.setParamTypes(paramTypes);
                                            
                                            webOperations.add(webOperation);
                                            }
                                        keys[0] = webOperations;
                                }
                                @Override
                                public void cancel() {
                                }
                                
                                private AnnotationMirror getWebMethodAnnotation(
                                        ExecutableElement method )
                                {
                                    List<? extends AnnotationMirror> annotations = 
                                        method.getAnnotationMirrors();
                                    boolean hasWebMethodAnnotation=false;
                                    for (AnnotationMirror an:annotations) {       
                                        Element anElement = an.getAnnotationType().
                                            asElement();
                                        if ( anElement instanceof TypeElement ) {
                                            hasWebMethodAnnotation = 
                                                "javax.jws.WebMethod".  // NOI18N
                                                    contentEquals(
                                                            ((TypeElement)anElement).
                                                                getQualifiedName());
                                            if ( hasWebMethodAnnotation ){
                                                return an;
                                            }
                                        }
                                    }
                                    return null;
                                }
                            };
                            try {
                                javaSource.runUserActionTask(task, true);
                            } catch (IOException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    }

                    if (keys[0] == null) {
                        keys[0] = Collections.emptyList();
                    }
                    else {
                        ((List<WebOperationInfo>)keys[0]).sort(OPERATION_INFO_COMPARATOR);
                    }
                    setKeys(keys[0]);
                }
            });
        }
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof WsdlOperation) {
            return new Node[] {new OperationNode((WsdlOperation)key)};
        } else if(key instanceof WebOperationInfo) {
            final WebOperationInfo method = (WebOperationInfo)key;
            Node n = new AbstractNode(Children.LEAF) {

                @java.lang.Override
                public java.awt.Image getIcon(int type) {
                    if (cachedIcon == null) {
                        cachedIcon = ImageUtilities.loadImage(OPERATION_ICON);
                    }
                    return cachedIcon;
                }
                
                @Override
                public Action[] getActions(boolean context) {
                    return new Action[]{SystemAction.get(PropertiesAction.class)};
                }

                @Override
                public Action getPreferredAction() {
                    return SystemAction.get(PropertiesAction.class);
                }
                
                @Override
                public String getDisplayName() {
                    return getWebOperationInfoName(method);
                } 
            };
            StringBuffer buf = new StringBuffer();
            for (String paramType:method.getParamTypes()) {
                buf.append(buf.length() == 0 ? paramType : ", "+paramType);
            }
            n.setShortDescription(
                    NbBundle.getMessage(JaxWsChildren.class,"TXT_operationDesc",method.getReturnType(),method.getOperationName(),buf.toString()));
            return new Node[]{n};
        }
        return new Node[0];
    }
    
    private boolean isFromWsdl() {
        return service.getWsdlUrl()!=null;
    }
    
    private JAXWSSupport getJAXWSSupport() {
        return JAXWSSupport.getJAXWSSupport(srcRoot);
    }
    
    private void setBindings(JAXWSSupport support, WsdlModeler wsdlModeler, Service service) {
        Binding[] extbindings = service.getBindings();
        if (extbindings==null || extbindings.length==0) {
            wsdlModeler.setJAXBBindings(null);
            return;
        }
        String[] bindingFiles = new String[extbindings.length];
        for(int i = 0; i < extbindings.length; i++){
            bindingFiles[i] = extbindings[i].getFileName();
        }    

        FileObject bindingsFolder = support.getBindingsFolderForService(getNode().getName(),true);
        List<URL> list = new ArrayList<URL>();
        for (int i=0;i<bindingFiles.length;i++) {
            FileObject fo = bindingsFolder.getFileObject(bindingFiles[i]);
            list.add(fo.toURL());
        }
        URL[] bindings = new URL[list.size()];
        list.<URL>toArray(bindings);
        wsdlModeler.setJAXBBindings(bindings);
    }
    
    void refreshKeys(boolean downloadWsdl, final boolean refreshImplClass, String newWsdlUrl) {
        if (!isFromWsdl()) return;
        super.addNotify();
        // copy to local wsdl first
        JAXWSSupport support = getJAXWSSupport();
            
        if (downloadWsdl) {
                String serviceName = getNode().getName();
                FileObject xmlResorcesFo = support.getLocalWsdlFolderForService(serviceName,true);
                FileObject localWsdl = null;
                try {
                    String oldWsdlUrl = service.getWsdlUrl();
                    boolean jaxWsModelChanged = false;
                    if (newWsdlUrl.length()>0 && !oldWsdlUrl.equals(newWsdlUrl)) {
                         localWsdl = WSUtils.retrieveResource(
                                xmlResorcesFo,
                                new URI(newWsdlUrl));   
                         jaxWsModelChanged = true;
                    } else {
                        localWsdl = WSUtils.retrieveResource(
                                xmlResorcesFo,
                                new URI(oldWsdlUrl));
                    }
                    if (jaxWsModelChanged) {
                        service.setWsdlUrl(newWsdlUrl);
                        FileObject xmlResourcesFo = support.getLocalWsdlFolderForService(serviceName,false);
                        if (xmlResourcesFo!=null) {
                            String localWsdlUrl = FileUtil.getRelativePath(xmlResourcesFo, localWsdl);
                            service.setLocalWsdlFile(localWsdlUrl);
                        }
                        Project project = FileOwnerQuery.getOwner(srcRoot);
                        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
                        if (jaxWsModel!=null) jaxWsModel.write();
                    }  
                    // copy resources to WEB-INF/wsdl/${serviceName}
                    FileObject wsdlFolder = getWsdlFolderForService(support, serviceName);
                    WSUtils.copyFiles(xmlResorcesFo, wsdlFolder);
                } catch (URISyntaxException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (UnknownHostException ex) {
                    ErrorManager.getDefault().annotate(ex,
                            NbBundle.getMessage(JaxWsChildren.class,"MSG_ConnectionProblem"));
                    return;
                } catch (IOException ex) {
                    ErrorManager.getDefault().annotate(ex,
                            NbBundle.getMessage(JaxWsChildren.class,"MSG_ConnectionProblem"));
                    return;
                }
                
                // re-generate also wrapper wsdl file if necessary
                if (localWsdl!=null) {
                    WsdlWrapperHandler handler = null;
                    try {
                        handler = WsdlWrapperGenerator.parse(localWsdl.toURL().toExternalForm());
                    } catch (ParserConfigurationException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (SAXException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    }
                    if (handler != null && !handler.isServiceElement()) {
                        StreamSource source = new StreamSource(localWsdl.toURL().toExternalForm());
                        try {
                            File wrapperWsdlFile = new File(FileUtil.toFile(localWsdl.getParent()), WsdlWrapperGenerator.getWrapperName(localWsdl.toURL())); //NOI18N

                            if(!wrapperWsdlFile.exists()) {
                                try {
                                    wrapperWsdlFile.createNewFile();
                                } catch(IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
                                }
                            }
                            if (wrapperWsdlFile.exists()) {
                                WsdlWrapperGenerator.generateWrapperWSDLContent(wrapperWsdlFile, source, handler.getTargetNsPrefix(),localWsdl.getNameExt());
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                        }
                    }
                }
            }
            FileObject localWsdlFolder = getJAXWSSupport().getLocalWsdlFolderForService(service.getName(),false);
            if (localWsdlFolder == null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(JaxWsChildren.class,"MSG_RefreshWithReplaceWsdl"), //NOI18N
                        NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            FileObject wsdlFo = 
                localWsdlFolder.getFileObject(service.getLocalWsdlFile());
            wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFo.toURL());
            String packageName = service.getPackageName();
            if (packageName!=null && service.isPackageNameForceReplace()) {
                // set the package name for the modeler
                wsdlModeler.setPackageName(packageName);
            } else {
                wsdlModeler.setPackageName(null);
            }
            wsdlModeler.setCatalog(support.getCatalog());
            setBindings(support, wsdlModeler, service);
            
            // re-generate java artifacts
            regenerateJavaArtifacts();
            // update nodes and implementation class
            
            modelGenerationFinished=false;
            ((JaxWsNode)getNode()).changeIcon();
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    wsdlModel=model;
                    modelGenerationFinished=true;
                    ((JaxWsNode)getNode()).changeIcon();
                    if (model==null) {
                        DialogDisplayer.getDefault().notify(
                                new JaxWsUtils.WsImportServiceFailedMessage(wsdlModeler.getCreationException()));
                    }
                    if (model!=null) {
                        try {    
                            // test if serviceName, portName are the same, change if necessary
                            String serviceName = service.getServiceName();
                            String portName = service.getPortName();
                            WsdlService wsdlService = model.getServiceByName(serviceName);
                            boolean jaxWsModelChanged=false;
                            if (wsdlService==null) {
                                if ( !model.getServices().isEmpty() ) {
                                    wsdlService = (WsdlService)model.getServices().get(0);
                                    service.setServiceName(wsdlService.getName());                                   
                                    jaxWsModelChanged=true;
                                }
                            }
                            
                            if (wsdlService != null) {
                                WsdlPort wsdlPort = wsdlService
                                        .getPortByName(portName);
                                if (wsdlPort == null) {
                                    if (!wsdlService.getPorts().isEmpty()) {
                                        wsdlPort = (WsdlPort) wsdlService
                                                .getPorts().get(0);
                                        service.setPortName(wsdlPort.getName());
                                        jaxWsModelChanged = true;
                                    }
                                }
                            }
                            
                            // test if package name for java artifacts hasn't changed
                            String oldPkgName = service.getPackageName();
                            if (wsdlService!=null && oldPkgName!=null && !service.isPackageNameForceReplace()) {
                                String javaName = wsdlService.getJavaName();
                                int dotPosition = javaName.lastIndexOf(".");
                                if (dotPosition>=0) {
                                    String newPkgName = javaName.substring(0,dotPosition);
                                    if (!oldPkgName.equals(newPkgName)) {
                                        service.setPackageName(newPkgName);
                                        jaxWsModelChanged=true;
                                    }
                                }
                            }

                            // save jax-ws model
                            if (jaxWsModelChanged) {
                                Project project = FileOwnerQuery.getOwner(srcRoot);
                                if (project!=null) {
                                    JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
                                    if (jaxWsModel!=null) jaxWsModel.write();
                                }

                            }
                            if (refreshImplClass) {
                                // re-generate implementation class
                                String implClass = service.getImplementationClass();
                                FileObject oldImplClass = srcRoot.getFileObject(implClass.replace('.','/')+".java"); //NOI18N
                                FileObject oldCopy = srcRoot.getFileObject(implClass.replace('.','/')+".java.old"); //NOI18N
                                int index = implClass.lastIndexOf(".");
                                FileObject folder = index>0?srcRoot.getFileObject(implClass.substring(0,index).replace('.','/')):srcRoot;
                                if (folder!=null) {
                                    String name = (index>=0?implClass.substring(index+1):implClass);
                                    if (oldImplClass!=null) {
                                        if (oldCopy!=null) oldCopy.delete();
                                        FileUtil.copyFile(oldImplClass, folder, name+".java", "old"); //NOI18N
                                        oldImplClass.delete();
                                    }
                                    // close the editor representing old impl bean
                                    JaxWsNode parent = (JaxWsNode)getNode();
                                    JaxWsUtils.generateJaxWsImplementationClass(FileOwnerQuery.getOwner(srcRoot),
                                        folder, name, model, service);
                                    FileObject newImplClass = srcRoot.getFileObject(implClass.replace('.','/')+".java"); //NOI18N
                                    if (newImplClass!=null) {
                                        JaxWsChildren.this.implClass=newImplClass;
                                    }
                                    parent.refreshImplClass();
                                }
                            }
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ErrorManager.ERROR,ex);
                        }
                    }
                }
        });
    }
    
    private void regenerateJavaArtifacts() {
        Project project = FileOwnerQuery.getOwner(srcRoot);
        if (project!=null) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
            try {
                String name = service.getName();
                ExecutorTask wsimportTask =
                    ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-service-clean-"+name,"wsimport-service-generate"},null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } 
        }
    }

    
    private FileObject getWsdlFolderForService(JAXWSSupport support, String name) throws IOException {
        FileObject globalWsdlFolder = support.getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject(name);
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        return globalWsdlFolder.createFolder(name);
    }
    
    WsdlModeler getWsdlModeler() {
        return wsdlModeler;
    }
    
    boolean isModelGenerationFinished() {
        return modelGenerationFinished;
    }
    
    private static String getClassName(String fullClassName) {
        StringTokenizer tok = new StringTokenizer(fullClassName,"."); //NOI18N
        String token = "";
        while (tok.hasMoreTokens()) {
            token = tok.nextToken();
        }
        return token;
    }
    
    private static String getWebOperationInfoName(WebOperationInfo method){
        return method.getOperationName()+": "+getClassName(method.getReturnType()); //NOI18N
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
    private static class WebOperationInfoComparator implements Comparator<WebOperationInfo>{
        
        public int compare(WebOperationInfo info1, WebOperationInfo info2) {
            String name1 = getWebOperationInfoName( info1);
            String name2 = getWebOperationInfoName( info2);
            return name1.compareTo(name2);
        }
    }
    
    private static class WebOperationInfo {
        private String operationName;
        private List<String> paramTypes;
        private String returnType;

        String getOperationName() {
            return operationName;
        }

        void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        List<String> getParamTypes() {
            return paramTypes;
        }

        void setParamTypes(List<String> paramTypes) {
            this.paramTypes = paramTypes;
        }

        String getReturnType() {
            return returnType;
        }

        void setReturnType(String returnType) {
            this.returnType = returnType;
        }       
    }
    
}
