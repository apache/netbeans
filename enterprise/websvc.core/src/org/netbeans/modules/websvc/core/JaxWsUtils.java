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
package org.netbeans.modules.websvc.core;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModelFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase.Use;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Utilities;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsUtils {

    public static final String HANDLER_TEMPLATE = "Templates/WebServices/MessageHandler.java"; //NOI18N
    private static final String OLD_SOAP12_NAMESPACE = "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/"; //NOI18N
    private static final String SOAP12_NAMESPACE = "http://www.w3.org/2003/05/soap/bindings/HTTP/";  //NOI18N
    private static final String BINDING_TYPE_ANNOTATION = "javax.xml.ws.BindingType"; //NOI18N
    private static int projectType;
    private static boolean jsr109Supported = false;

    /** Creates a new instance of JaxWsUtils */
    public JaxWsUtils() {
    }

    /** This method is called from Refresh Service action
     */
    public static void generateJaxWsImplementationClass(Project project, 
            FileObject targetFolder, String targetName, WsdlModel wsdlModel, 
            org.netbeans.modules.websvc.api.jaxws.project.config.Service service) 
                throws Exception 
    {
        WsdlService wsdlService = wsdlModel.getServiceByName(service.getServiceName());
        WsdlPort wsdlPort = null;
        if (wsdlService != null) {
            wsdlPort = wsdlService.getPortByName(service.getPortName());
        }
        if (wsdlService != null && wsdlPort != null) {
            String serviceID = service.getName();
            initProjectInfo(project);
            boolean isStatelessSB = (projectType == ProjectInfo.EJB_PROJECT_TYPE);
            if (wsdlPort.isProvider()/*from customization*/ || 
                    service.isUseProvider() /*from ws creation wizard*/) 
            {
                generateProviderImplClass(project, targetFolder, null, targetName, 
                        wsdlService, wsdlPort, serviceID, isStatelessSB);
            } else {
                generateJaxWsImplClass(project, targetFolder, targetName, null, 
                        wsdlService, wsdlPort, false, serviceID, isStatelessSB);
            }
        }
    }

    /** This method is called from Create Web Service from WSDL wizard
     */
    public static void generateJaxWsImplementationClass(Project project, 
            FileObject targetFolder, String targetName, URL wsdlURL, 
            WsdlService service, WsdlPort port, boolean useProvider, 
            boolean isStatelessSB) throws Exception 
    {
        if (useProvider) {
            generateJaxWsProvider(project, targetFolder, targetName, 
                    wsdlURL, service, port, isStatelessSB);
        } else {
            initProjectInfo(project);
            generateJaxWsImplClass(project, targetFolder, targetName, 
                    wsdlURL, service, port, true, null, isStatelessSB);
        }
    }

    /** This method is called from Create Web Service from WSDL wizard
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static void generateJaxWsArtifacts(Project project, 
            FileObject targetFolder, String targetName, URL wsdlURL, 
            String service, String port) throws Exception 
    {
        initProjectInfo(project);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        String artifactsPckg = "service." + targetName.toLowerCase(); //NOI18N
        ClassPath classPath = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
        String serviceImplPath = classPath.getResourceName(targetFolder, '.', false);

        boolean jsr109 = true;
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            jsr109 = isJsr109(jaxWsModel);
        }
        jaxWsSupport.addService(targetName, serviceImplPath + "." + 
                targetName, wsdlURL.toExternalForm(), service, port, artifactsPckg, jsr109, false);
    }
    
    public static boolean hasAnnotation( Element element , String fqn ){
        return  getAnnotation(element, fqn)!= null;
    }
    
    public static AnnotationMirror getAnnotation( Element element , String fqn ){
        for( AnnotationMirror mirror : element.getAnnotationMirrors() ){
            if ( hasFqn(mirror, fqn)){
                return mirror;
            }
        }
        return null;
    }
    
    public  static boolean hasFqn( AnnotationMirror mirror , String fqn){
        Element anElement = mirror.getAnnotationType().asElement();
        if ( anElement instanceof TypeElement ){
            return fqn.contentEquals( ((TypeElement)anElement).getQualifiedName());
        }
        return false;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static void generateProviderImplClass(Project project, 
            FileObject targetFolder, FileObject implClass,
            String targetName, final WsdlService service, final WsdlPort port, 
            final String serviceID, final boolean isStatelessSB) throws Exception 
    {
        final JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(
                project.getProjectDirectory());
        FileObject implClassFo = implClass;
        if (implClassFo == null) {
            implClassFo = GenerationUtils.createClass(targetFolder, targetName, null);
            implClassFo.setAttribute("jax-ws-service", Boolean.TRUE);           // NOI18N
            implClassFo.setAttribute("jax-ws-service-provider", Boolean.TRUE);  // NOI18N  
            DataObject.find(implClassFo).setValid(false);
        }
        
        final String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
        final JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        final boolean isIncomplete[] = new boolean[1];
        final CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    // add implementation clause
                    ExpressionTree implClause = make.Identifier(
                            "javax.xml.ws.Provider<javax.xml.transform.Source>"); //NOI18N
                    ClassTree modifiedClass = make.addClassImplementsClause(
                            javaClass, implClause);

                    // add @Stateless annotation
                    if (isStatelessSB) {//Stateless Session Bean
                        TypeElement statelessAn = workingCopy.getElements().
                            getTypeElement("javax.ejb.Stateless"); //NOI18N
                        if ( statelessAn == null ){
                            isIncomplete[0] = true;
                            return;
                        }
                        AnnotationTree StatelessAnnotation = make.Annotation(
                                make.QualIdent(statelessAn),
                                Collections.<ExpressionTree>emptyList());
                        modifiedClass = genUtils.addAnnotation(modifiedClass, 
                                StatelessAnnotation);
                    }
                    TypeElement serviceModeAn = workingCopy.getElements().
                        getTypeElement("javax.xml.ws.ServiceMode"); //NOI18N
                    if ( serviceModeAn == null ){
                        isIncomplete[0] = true;
                        return;
                    }
                    List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                    IdentifierTree idTree = make.Identifier(
                            "javax.xml.ws.Service.Mode.PAYLOAD");       // NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("value"), idTree));  //NOI18N
                    AnnotationTree serviceModeAnnotation = make.Annotation(
                            make.QualIdent(serviceModeAn),
                            attrs);
                    modifiedClass = genUtils.addAnnotation(modifiedClass, 
                            serviceModeAnnotation);

                    TypeElement wsProviderAn = workingCopy.getElements().
                        getTypeElement("javax.xml.ws.WebServiceProvider"); //NOI18N
                    if ( wsProviderAn == null ){
                        isIncomplete[0] = true;
                        return;
                    }
                    attrs = new ArrayList<ExpressionTree>();
                    attrs.add(
                            make.Assignment(make.Identifier("serviceName"), 
                                    make.Literal(service.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("portName"), 
                                    make.Literal(port.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("targetNamespace"), 
                                    make.Literal(port.getNamespaceURI()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("wsdlLocation"), 
                                    make.Literal(wsdlLocation))); //NOI18N

                    AnnotationTree providerAnnotation = make.Annotation(
                            make.QualIdent(wsProviderAn),
                            attrs);
                    modifiedClass = genUtils.addAnnotation(modifiedClass, 
                            providerAnnotation);

                    String type = "javax.xml.transform.Source";     // NOI18N
                    List<VariableTree> params = new ArrayList<VariableTree>();
                    params.add(make.Variable(
                            make.Modifiers(
                            Collections.<Modifier>emptySet(),
                            Collections.<AnnotationTree>emptyList()),
                            "source", // name NOI18N
                            make.Identifier(type), // parameter type
                            null // initializer - does not make sense in parameters.
                            ));//);
                    // create method
                    ModifiersTree methodModifiers = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>emptyList());
                    MethodTree method = make.Method(
                            methodModifiers, // public
                            "invoke", // operation name  NOI18N
                            make.Identifier(type), // return type
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            params,
                            Collections.<ExpressionTree>emptyList(), // throws
                            "{ //TODO implement this method\nthrow new UnsupportedOperationException(\"Not implemented yet.\") }", // NOI18N body text
                            null // default value - not applicable here, used by annotations
                            );

                    modifiedClass = make.addClassMember(modifiedClass, method);
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }

            @Override
            public void cancel() {
            }
            
        };
        ModificationResult result = targetSource.runModificationTask(task);
        if ( isIncomplete[0] && 
                org.netbeans.api.java.source.SourceUtils.isScanInProgress())
        {
            final FileObject implClassArg = implClassFo;
            final Runnable runnable = new Runnable(){
                /* (non-Javadoc)
                 * @see java.lang.Runnable#run()
                 */
                @Override
                public void run() {
                    try {
                        targetSource.runModificationTask(task).commit();
                        //open in editor
                        openInEditor(serviceID, jaxWsSupport, implClassArg);
                    }
                    catch(IOException e){
                        Logger.getLogger( JaxWsUtils.class.getName()).log( 
                                Level.WARNING, null , e);
                    }
                }
            };
            SwingUtilities.invokeLater( new Runnable(){

                @Override
                public void run() {
                    ScanDialog.runWhenScanFinished(runnable, NbBundle.getMessage(
                            JaxWsUtils.class, "LBL_GenerateProvider"));     // NOI18N
                }
            });
        }
        else { 
            //open in editor
            result.commit();
            openInEditor(serviceID, jaxWsSupport, implClassFo);
        }
    }

    private static void openInEditor( String serviceID,
            JAXWSSupport jaxWsSupport, FileObject implClassFo )
            throws DataObjectNotFoundException
    {
        DataObject dobj = DataObject.find(implClassFo);
        if ( dobj == null ){
            return;
        }
        openFileInEditor(dobj);
        /*List services = jaxWsSupport.getServices();
        if (serviceID != null) {
            for (Object serv : services) {
                if (serviceID.equals(((Service) serv).getName())) {

                    final EditCookie editCookie = dobj.getCookie(EditCookie.class);
                    if (editCookie != null) {
                        RequestProcessor.getDefault().post(new Runnable() {

                            public void run() {
                                editCookie.edit();
                            }
                        }, 1000);
                        break;
                    }
                }
            }
        }*/
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static void generateJaxWsProvider(Project project, 
            FileObject targetFolder, String targetName, URL wsdlURL, 
            WsdlService service, WsdlPort port, boolean isStatelessSB) 
                throws Exception 
    {
        initProjectInfo(project);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(
                project.getProjectDirectory());
        String portJavaName = port.getJavaName();
        String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf("."));    // NOI18N
        FileObject implClassFo = GenerationUtils.createClass(targetFolder, 
                targetName, null);
        implClassFo.setAttribute("jax-ws-service", Boolean.TRUE);               // NOI18N
        implClassFo.setAttribute("jax-ws-service-provider", Boolean.TRUE);      // NOI18N
        DataObject.find(implClassFo).setValid(false);
        ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);
        String serviceImplPath = classPath.getResourceName(implClassFo, '.', false);

        boolean jsr109 = true;
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            jsr109 = isJsr109(jaxWsModel);
        }

        String serviceID = jaxWsSupport.addService(targetName, serviceImplPath, 
                wsdlURL.toString(), service.getName(),
                port.getName(), artifactsPckg, jsr109, true);

        generateProviderImplClass(project, targetFolder, implClassFo, targetName, 
                service, port, serviceID, isStatelessSB);

    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static void generateJaxWsImplClass(final Project project, 
            final FileObject targetFolder, final String targetName, final URL wsdlURL, 
            final WsdlService service, final WsdlPort port, final boolean addService, 
            final String serviceID, final boolean isStatelessSB) throws Exception 
    {

        // Use Progress API to display generator messages.
        //ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsUtils.class, "TXT_WebServiceGeneration")); //NOI18N
        //handle.start(100);
        final FileObject implClassFo = GenerationUtils.createClass(targetFolder, 
                targetName, null);
        implClassFo.setAttribute("jax-ws-service", Boolean.TRUE);           // NOI18N
        DataObject.find(implClassFo).setValid(false);
        DataObject.find(implClassFo);

        final JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        
        final boolean isIncomplete[] = new boolean[1];
        final String[] sIdContainer = new String[1];
        
        final CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                if ( !isIncomplete[0] ) {
                    sIdContainer[0] = addService(project, targetFolder, targetName, 
                        wsdlURL, service , port , implClassFo , serviceID , addService);
                }
                JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(
                        project.getProjectDirectory());
                String wsdlLocation = jaxWsSupport.getWsdlLocation(sIdContainer[0]);
                
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    //add @WebService annotation
                    TypeElement wSAn = workingCopy.getElements().getTypeElement(
                            "javax.jws.WebService"); //NOI18N
                    if ( wSAn == null ){
                        isIncomplete[0] = true;
                        return;
                    }
                    List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                    attrs.add(
                            make.Assignment(make.Identifier("serviceName"), 
                                    make.Literal(service.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("portName"), 
                                    make.Literal(port.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("endpointInterface"), 
                                    make.Literal(port.getJavaName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("targetNamespace"), 
                                    make.Literal(port.getNamespaceURI()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("wsdlLocation"), 
                                    make.Literal(wsdlLocation))); //NOI18N

                    AnnotationTree wSAnnotation = make.Annotation(
                            make.QualIdent(wSAn),
                            attrs);
                    ClassTree modifiedClass = genUtils.addAnnotation(javaClass, 
                            wSAnnotation);

                    if (WsdlPort.SOAP_VERSION_12.equals(port.getSOAPVersion())) {
                        TypeElement bindingElement = workingCopy.getElements().
                            getTypeElement(BINDING_TYPE_ANNOTATION);

                        if (bindingElement != null) {
                            List<ExpressionTree> bindingAttrs = 
                                new ArrayList<ExpressionTree>();
                            bindingAttrs.add(make.Assignment(make.Identifier("value"), //NOI18N
                                    make.Literal(OLD_SOAP12_NAMESPACE))); //NOI18N
                            AnnotationTree bindingAnnotation = make.Annotation(
                                    make.QualIdent(bindingElement),
                                    bindingAttrs);
                            modifiedClass = genUtils.addAnnotation(modifiedClass, 
                                    bindingAnnotation);
                        }
                        else {
                            isIncomplete[0] = true;
                        }
                    }

                    // add @Stateless annotation
                    if (isStatelessSB) {//EJB project
                        TypeElement statelessAn = workingCopy.getElements().
                            getTypeElement("javax.ejb.Stateless"); //NOI18N
                        if ( statelessAn == null ){
                            isIncomplete[0] = true;
                            return;
                        }
                        AnnotationTree StatelessAnnotation = make.Annotation(
                                make.QualIdent(statelessAn),
                                Collections.<ExpressionTree>emptyList());
                        modifiedClass = genUtils.addAnnotation(modifiedClass, 
                                StatelessAnnotation);
                    }

                    List<WsdlOperation> operations = port.getOperations();
                    for (WsdlOperation operation : operations) {

                        // return type
                        String returnType = operation.getReturnTypeName();

                        // create parameters
                        List<WsdlParameter> parameters = operation.getParameters();
                        List<VariableTree> params = new ArrayList<VariableTree>();
                        for (WsdlParameter parameter : parameters) {
                            // create parameter:
                            // final ObjectOutput arg0
                            params.add(make.Variable(
                                    make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()),
                                    parameter.getName(), // name
                                    make.Identifier(parameter.getTypeName()), // parameter type
                                    null // initializer - does not make sense in parameters.
                                    ));
                        }

                        // create exceptions
                        Iterator<String> exceptions = operation.getExceptions();
                        List<ExpressionTree> exc = new ArrayList<ExpressionTree>();
                        while (exceptions.hasNext()) {
                            String exception = exceptions.next();
                            TypeElement excEl = workingCopy.getElements().
                                getTypeElement(exception);
                            if (excEl != null) {
                                exc.add(make.QualIdent(excEl));
                            } 
                            else {
                                exc.add(make.Identifier(exception));
                            }
                        }

                        // create method
                        ModifiersTree methodModifiers = make.Modifiers(
                                Collections.<Modifier>singleton(Modifier.PUBLIC),
                                Collections.<AnnotationTree>emptyList());
                        MethodTree method = make.Method(
                                methodModifiers, // public
                                operation.getJavaName(), // operation name
                                make.Identifier(returnType), // return type
                                Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                params,
                                exc, // throws
                                "{ //TODO implement this method\nthrow new UnsupportedOperationException(\"Not implemented yet.\") }", // body text
                                null // default value - not applicable here, used by annotations
                                );

                        modifiedClass = make.addClassMember(modifiedClass, method);
                    }
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }

            @Override
            public void cancel() {
            }
        };
        commitModificationTask(targetSource, task, implClassFo, !isIncomplete[0]);
    }

    public static void openFileInEditor(DataObject dobj) {

        final OpenCookie openCookie = dobj.getCookie(OpenCookie.class);
        if (openCookie != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    openCookie.open();
                }
            }, 1000);
        } else {
            final EditorCookie ec = dobj.getCookie(EditorCookie.class);
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ec.open();
                }
            }, 1000);
        }
    }

    public static String getPackageName(String fullyQualifiedName) {
        String packageName = "";                            // NOI18N
        int index = fullyQualifiedName.lastIndexOf(".");    // NOI18N
        if (index != -1) {
            packageName = fullyQualifiedName.substring(0, index);
        }
        return packageName;
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static String addService(Project project, 
            FileObject targetFolder, String targetName, URL wsdlURL, 
            WsdlService service, WsdlPort port,  FileObject implClassFo , 
            String serviceID , boolean addService ) 
    {
        if (addService) {
            ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);
            String portJavaName = port.getJavaName();
            String serviceImplPath = classPath.getResourceName(implClassFo, '.', false);
            final String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf('.'));
            JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(
                    project.getProjectDirectory());
            boolean jsr109 = true;
            JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                jsr109 = isJsr109(jaxWsModel);
            }
            serviceID = jaxWsSupport.addService(targetName, serviceImplPath, 
                    wsdlURL.toString(), service.getName(), port.getName(), 
                    artifactsPckg, jsr109, false);
            if (serviceID == null) {
                Logger.getLogger(JaxWsUtils.class.getName()).log(Level.WARNING, 
                        "Failed to add service element to nbproject/jax-ws.xml. " +
                        "Either problem with downloading wsdl file or problem with " +
                        "writing into nbproject/jax-ws.xml.");          // NOI18N
                return serviceID;
            }
        }
        return serviceID;
    }
    
    private static void commitModificationTask(final JavaSource javaSource, 
            final Task<WorkingCopy> task, final FileObject fileObject , 
            boolean isComplete ) throws IOException
    {
        ModificationResult result = javaSource.runModificationTask(task);
        if ( !isComplete && 
                org.netbeans.api.java.source.SourceUtils.isScanInProgress())
        {
            final Runnable runnable = new Runnable(){
                /* (non-Javadoc)
                 * @see java.lang.Runnable#run()
                 */
                @Override
                public void run() {
                    try {
                        javaSource.runModificationTask(task).commit();
                        //open in editor
                        openFileInEditor(DataObject.find(fileObject));
                    }
                    catch (IOException e) {
                        Logger.getLogger(JaxWsUtils.class.getName())
                                .log(Level.WARNING, null, e);
                    }
                }
            };
            SwingUtilities.invokeLater( new Runnable(){
                @Override
                public void run() {
                    ScanDialog.runWhenScanFinished( runnable, NbBundle.getMessage(
                            JaxWsUtils.class, "LBL_GenerateWebserviceClass"));  // NOI18N
                }
            });
        }
        else { 
            //open in editor
            result.commit();
            //open in editor
            openFileInEditor(DataObject.find(fileObject));
        }
    }

    private static void initProjectInfo(Project project) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().
            lookup(J2eeModuleProvider.class);
        if (provider != null) {
            String serverInstance = provider.getServerInstanceID();
            if (serverInstance != null) {
                try {
                    J2eePlatform j2eePlatform = Deployment.getDefault().
                    getServerInstance(serverInstance).getJ2eePlatform();
                    WSStack<JaxWs> wsStack = JaxWsStackSupport.getJaxWsStack(
                            j2eePlatform);
                    if (wsStack != null) {
                        jsr109Supported = 
                            wsStack.isFeatureSupported(JaxWs.Feature.JSR109);

                    }
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(JaxWsUtils.class.getName()).log(Level.INFO, 
                            "Failed to find J2eePlatform", ex);         // NOI18N
                }
            }
            J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
            if (J2eeModule.Type.EJB.equals(moduleType)) {
                projectType = ProjectInfo.EJB_PROJECT_TYPE;
            } else if (J2eeModule.Type.WAR.equals(moduleType)) {
                projectType = ProjectInfo.WEB_PROJECT_TYPE;
            } else if (J2eeModule.Type.CAR.equals(moduleType)) {
                projectType = ProjectInfo.CAR_PROJECT_TYPE;
            } else {
                projectType = ProjectInfo.JSE_PROJECT_TYPE;
            }
        } else {
            projectType = ProjectInfo.JSE_PROJECT_TYPE;
        }
    }

    public static boolean isProjectReferenceable(Project clientProject, 
            Project targetProject) 
    {
        if (clientProject == targetProject) {
            return true;
        } 
        else {
            AntArtifactProvider antArtifactProvider = clientProject.getLookup().
            lookup(AntArtifactProvider.class);
            if (antArtifactProvider != null) {
                AntArtifact jarArtifact = getJarArtifact(antArtifactProvider);
                if (jarArtifact != null) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Adding clientProject reference to targetProject
     * 
     */
    public static boolean addProjectReference(Project clientProject, 
            FileObject targetFile) {
        try {
            assert clientProject != null && targetFile != null;
            Project targetProject = FileOwnerQuery.getOwner(targetFile);
            if (clientProject != targetProject) {
                AntArtifactProvider antArtifactProvider = clientProject.getLookup().
                    lookup(AntArtifactProvider.class);
                if (antArtifactProvider != null) {
                    AntArtifact jarArtifact = getJarArtifact(antArtifactProvider);
                    if (jarArtifact != null) {
                        FileObject targetFo = targetFile;
                        if (!"java".equals(targetFile.getExt())) { //NOI18N
                            SourceGroup[] srcGroups =
                                    ProjectUtils.getSources(targetProject).
                                    getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                            if (srcGroups != null && srcGroups.length >0) {
                                targetFo = srcGroups[0].getRootFolder();
                            } else {
                                return false;
                            }
                        }
                        AntArtifact[] jarArtifacts = new AntArtifact[]{jarArtifact};
                        URI[] artifactsUri = jarArtifact.getArtifactLocations();
                        ProjectClassPathModifier.addAntArtifacts(jarArtifacts, 
                                artifactsUri, targetFo, ClassPath.COMPILE);
                        return true;
                    }
                }
            } else {
                return true;
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
        }
        return false;
    }

    private static AntArtifact getJarArtifact(AntArtifactProvider antArtifactProvider) {
        AntArtifact[] artifacts = antArtifactProvider.getBuildArtifacts();
        for (int i = 0; i < artifacts.length; i++) {
            if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(artifacts[i].getType())) {
                return artifacts[i];
            }
        }
        return null;
    }

    public static class WsImportServiceFailedMessage extends NotifyDescriptor.Message {

        public WsImportServiceFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(JaxWsUtils.class, "TXT_CannotGenerateService",    // NOI18N 
                    ex.getLocalizedMessage()),NotifyDescriptor.ERROR_MESSAGE);
        }
    }

    public static class WsImportClientFailedMessage extends NotifyDescriptor.Message {

        public WsImportClientFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(JaxWsUtils.class, "TXT_CannotGenerateClient",  // NOI18N 
                    ex.getLocalizedMessage()), NotifyDescriptor.ERROR_MESSAGE);
        }
    }

    /**
     * Utility for changing the wsdlLocation attribute in external JAXWS external files
     * @param bindingFile FileObject of the external binding file
     * @param relativePath String representing the relative path to the wsdl
     * @return true if modification succeeded, false otherwise.
     */
    public static boolean addRelativeWsdlLocation(FileObject bindingFile, 
            String relativePath) 
    {
        GlobalBindings gb = null;

        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.
            getModelSource(bindingFile, true);
        if (ms != null) {
            BindingsModel bindingsModel = BindingsModelFactory.getDefault().getModel(ms);
            if (bindingsModel != null) {
                gb = bindingsModel.getGlobalBindings();
                if (gb != null) {
                    bindingsModel.startTransaction();
                    gb.setWsdlLocation(relativePath);
                    bindingsModel.endTransaction();
                    return true;
                }
            }
        }
        return false;
    }

    /** Package name validation
     */
    public static boolean isJavaPackage(String pkg) {
        boolean result = false;
        StringTokenizer tukac = new StringTokenizer(pkg, ".", true);
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if (".".equals(token)) {
                if (result) {
                    result = false;
                } else {
                    return false;
                }
            } else {
                if (!Utilities.isJavaIdentifier(token)) {
                    return false;
                }
                result = true;
            }
        }

        return result;
    }

    /** Class/Identifier validation
     */
    public static boolean isJavaIdentifier(String id) {
        boolean result = true;

        if (id == null || id.length() == 0 || !Character.isJavaIdentifierStart(
                id.charAt(0))) 
        {
            result = false;
        } 
        else {
            for (int i = 1, idlength = id.length(); i < idlength; i++) {
                if (!Character.isJavaIdentifierPart(id.charAt(i))) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /** This method ensures the list of steps displayed in the left hand panel
     *  of the wizard is correct for any given displayed panel.
     *
     *  Taken from web/core
     */
    public static String[] createSteps(String[] before, 
            WizardDescriptor.Panel[] panels) 
    {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    public static boolean isEjbJavaEE5orHigher(Project project) {
        ProjectInfo projectInfo = new ProjectInfo(project);
        return isEjbJavaEE5orHigher(projectInfo);
    }

    public static boolean isEjbJavaEE5orHigher(ProjectInfo projectInfo) {
        int projType = projectInfo.getProjectType();
        if (projType == ProjectInfo.EJB_PROJECT_TYPE) {
            EjbJar ejbModule = EjbJar.getEjbJar(projectInfo.getProject().
                    getProjectDirectory());
            if (ejbModule != null && ejbModule.getDeploymentDescriptor() == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCarProject(Project project) {
        ProjectInfo projectInfo = new ProjectInfo(project);
        return isCarProject(projectInfo);
    }

    public static boolean isCarProject(ProjectInfo projectInfo) {
        int projType = projectInfo.getProjectType();
        return projType == ProjectInfo.CAR_PROJECT_TYPE;
    }

    /** Setter for WebService annotation attribute, e.g. serviceName = "HelloService"
     *
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static void setWebServiceAttrValue(final FileObject implClassFo, 
            final String attrName, final String attrValue) 
    {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final boolean isIncomplete[] = new boolean[1];
        final CancellableTask<WorkingCopy> modificationTask = 
            new CancellableTask<WorkingCopy>() 
        {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree classTree = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (classTree != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    ExpressionTree attrExpr =
                            (attrValue == null ? null : 
                                genUtils.createAnnotationArgument(attrName, attrValue));

                    ModifiersTree modif = classTree.getModifiers();
                    List<? extends AnnotationTree> annotations = modif.getAnnotations();
                    List<AnnotationTree> newAnnotations = new ArrayList<AnnotationTree>();

                    TypeElement webServiceEl = workingCopy.getElements().
                        getTypeElement("javax.jws.WebService"); //NOI18N
                    if ( webServiceEl == null ){
                        isIncomplete[0] = true;
                        return;
                    }
                    for (AnnotationTree an : annotations) {
                        IdentifierTree ident = (IdentifierTree) an.getAnnotationType();
                        TreePath anTreePath = workingCopy.getTrees().getPath(
                                workingCopy.getCompilationUnit(), ident);
                        TypeElement anElement = (TypeElement) workingCopy.getTrees().
                            getElement(anTreePath); 
                        if ( anElement == null ){
                            isIncomplete[0] = true;
                        }
                        else if ( anElement.getQualifiedName().
                                contentEquals( webServiceEl.getQualifiedName())) 
                        { 
                            List<? extends ExpressionTree> expressions = 
                                an.getArguments();
                            List<ExpressionTree> newExpressions = 
                                new ArrayList<ExpressionTree>();
                            boolean attrFound = false;
                            for (ExpressionTree expr : expressions) {
                                AssignmentTree as = (AssignmentTree) expr;
                                IdentifierTree id = (IdentifierTree) as.getVariable();
                                if (id.getName().contentEquals(attrName)) {
                                    attrFound = true;
                                    if (attrExpr != null) {
                                        newExpressions.add(attrExpr);
                                    }
                                } else {
                                    newExpressions.add(expr);
                                }
                            }
                            if (!attrFound) {
                                newExpressions.add(attrExpr);
                            }

                            AnnotationTree webServiceAn = 
                                make.Annotation(make.QualIdent(webServiceEl), 
                                        newExpressions);
                            newAnnotations.add(webServiceAn);
                        } else {
                            newAnnotations.add(an);
                        }
                    }

                    ModifiersTree newModifier = make.Modifiers(modif, newAnnotations);
                    workingCopy.rewrite(modif, newModifier);
                }
            }

            @Override
            public void cancel() {
            }
        };
        try {
            ModificationResult result = javaSource.runModificationTask(modificationTask);
            if ( isIncomplete[0] && 
                    org.netbeans.api.java.source.SourceUtils.isScanInProgress())
            {
                final Runnable runnable = new Runnable(){
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        try {
                            javaSource.runModificationTask(modificationTask).commit();
                        }
                        catch(IOException e){
                            Logger.getLogger( JaxWsUtils.class.getName()).log( 
                                    Level.WARNING, null , e);
                        }
                    }
                };
                SwingUtilities.invokeLater( new Runnable(){
                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished( runnable, NbBundle.getMessage(
                                JaxWsUtils.class, "LBL_ConfigureWebservice"));  // NOI18N
                    }
                });
            }
            else { 
                result.commit();
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(JaxWsUtils.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private boolean resolveServiceUrl(Object moduleType, CompilationController controller, 
            TypeElement targetElement, TypeElement wsElement, 
            String[] serviceName, String[] name) throws IOException 
    {
        boolean foundWsAnnotation = false;
        List<? extends AnnotationMirror> annotations = targetElement.getAnnotationMirrors();
        for (AnnotationMirror anMirror : annotations) {
            if (controller.getTypes().isSameType(wsElement.asType(), 
                    anMirror.getAnnotationType())) 
            {
                foundWsAnnotation = true;
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = 
                    anMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, 
                        ? extends AnnotationValue> entry : expressions.entrySet()) 
                {
                    if (entry.getKey().getSimpleName().contentEquals("serviceName")) {      // NOI18N
                        Object value = expressions.get(entry.getKey()).getValue();
                        if (value!= null) {
                            serviceName[0] = URLEncoder.encode(value.toString(), 
                                    "UTF-8"); //NOI18N
                        }
                    } else if (entry.getKey().getSimpleName().contentEquals("name")) {  // NOI18N
                        Object value = expressions.get(entry.getKey()).getValue();
                        if (value != null) {
                            name[0] = URLEncoder.encode(value.toString(), "UTF-8");
                        }
                    }
                    if (serviceName[0] != null && name[0] != null) {
                        break;
                    }
                }
                break;
            } // end if
        } // end for
        return foundWsAnnotation;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static boolean isSoap12(FileObject implClassFo) {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final String[] version = new String[1];
        final CancellableTask<CompilationController> task = 
            new CancellableTask<CompilationController>() 
        {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(
                        controller);
                List<? extends AnnotationMirror> annotations = 
                    typeElement.getAnnotationMirrors();
                boolean foundAnnotation = false;
                for (AnnotationMirror anMirror : annotations) {
                    Element annotationElement = anMirror.getAnnotationType().
                        asElement();
                    String fqn = null;
                    if ( annotationElement instanceof TypeElement ){
                        fqn = ((TypeElement) annotationElement).
                            getQualifiedName().toString();
                    }
                    if ( fqn!= null && BINDING_TYPE_ANNOTATION.contentEquals( fqn )){
                        Map<? extends ExecutableElement, 
                                ? extends AnnotationValue> expressions = 
                                    anMirror.getElementValues();
                        for (Map.Entry<? extends ExecutableElement, 
                                ? extends AnnotationValue> entry : 
                                    expressions.entrySet()) 
                        {
                            if (entry.getKey().getSimpleName().contentEquals("value")) {   //NOI18N
                                version[0] = (String)entry.getValue().getValue();
                                foundAnnotation = true;
                                break;
                            }
                        }

                    }
                    if (foundAnnotation) {
                        break;
                    }
                }
            }
            @Override
            public void cancel() {
            }
        };
        try {
            javaSource.runUserActionTask(task, true);
        } 
        catch (IOException e) {
            Logger.getLogger( JaxWsUtils.class.getName()).log( 
                    Level.WARNING, null , e);
        }
        return version[0] != null &&
                (SOAP12_NAMESPACE.equals(version[0]) || 
                        OLD_SOAP12_NAMESPACE.equals(version[0]));
    }

    public static void setSOAP12Binding(final FileObject implClassFo, 
            final boolean isSOAP12) 
    {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final boolean isIncomplete[] = new boolean[1];
        final CancellableTask<WorkingCopy> modificationTask = 
            new CancellableTask<WorkingCopy>() 
        {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = SourceUtils.
                    getPublicTopLevelElement(workingCopy);
                ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);

                TypeElement bindingElement = workingCopy.getElements().
                    getTypeElement(BINDING_TYPE_ANNOTATION);
                if (bindingElement == null) {
                    isIncomplete[0] = true;
                }
                else {
                    AnnotationTree bindingAnnotation = null;
                    List<? extends AnnotationTree> annots = 
                        javaClass.getModifiers().getAnnotations();
                    for (AnnotationTree an : annots) {
                        Tree ident = an.getAnnotationType();
                        TreePath anTreePath = workingCopy.getTrees().
                            getPath(workingCopy.getCompilationUnit(), ident);
                        TypeElement anElement = (TypeElement) workingCopy.getTrees().
                            getElement(anTreePath);
                        if ( anTreePath == null ){
                            isIncomplete[0] = true;
                        }
                        else if (anElement.getQualifiedName().
                                contentEquals(BINDING_TYPE_ANNOTATION)) 
                        {
                            bindingAnnotation = an;
                            break;
                        }
                    }
                    if (isSOAP12 && bindingAnnotation == null) {

                        ModifiersTree modifiersTree = javaClass.getModifiers();

                        AssignmentTree soapVersion = make.Assignment(
                                make.Identifier("value"),                   //NOI18N
                                make.Literal(OLD_SOAP12_NAMESPACE)); 
                        AnnotationTree soapVersionAnnotation = make.Annotation(
                                make.QualIdent(bindingElement),
                                Collections.<ExpressionTree>singletonList(soapVersion));

                        ModifiersTree newModifiersTree = make.
                            addModifiersAnnotation(modifiersTree, soapVersionAnnotation);

                        workingCopy.rewrite(modifiersTree, newModifiersTree);
                    } 
                    else if (!isSOAP12 && bindingAnnotation != null) {
                        ModifiersTree modifiers = javaClass.getModifiers();
                        ModifiersTree newModifiers = make.
                            removeModifiersAnnotation(modifiers, bindingAnnotation);
                        workingCopy.rewrite(modifiers, newModifiers);
                        CompilationUnitTree compileUnitTree = workingCopy.
                            getCompilationUnit();
                        List<? extends ImportTree> imports = 
                            compileUnitTree.getImports();
                        for (ImportTree imp : imports) {
                            Tree impTree = imp.getQualifiedIdentifier();
                            TreePath impTreePath = workingCopy.getTrees().
                                getPath(workingCopy.getCompilationUnit(), impTree);
                            TypeElement impElement = (TypeElement) workingCopy.getTrees().
                                getElement(impTreePath);
                            if ( impElement == null ){
                                isIncomplete[0] = true;
                            }
                            else if (impElement.getQualifiedName().
                                    contentEquals(BINDING_TYPE_ANNOTATION)) 
                            {
                                CompilationUnitTree newCompileUnitTree = 
                                    make.removeCompUnitImport(compileUnitTree, imp);
                                workingCopy.rewrite(compileUnitTree, newCompileUnitTree);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void cancel() {
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            modifySoap12Binding(javaSource, modificationTask, implClassFo, 
                    isIncomplete[0]);
        } else {
            doModifySoap12Binding(javaSource, modificationTask, implClassFo, 
                    isIncomplete[0]);
        }
    }
    
    private static void modifySoap12Binding(final JavaSource javaSource, 
            final CancellableTask<WorkingCopy> modificationTask, 
            final FileObject implClass, final boolean isIncomplete ) 
    {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                doModifySoap12Binding(javaSource, modificationTask, implClass, isIncomplete);
            }
        });
    }

    private static void doModifySoap12Binding(final JavaSource javaSource, 
            final CancellableTask<WorkingCopy> modificationTask, 
            final FileObject implClass, boolean isIncomplete ) 
    {
        try {
            ModificationResult result = javaSource.runModificationTask(modificationTask);
            if ( isIncomplete && 
                    org.netbeans.api.java.source.SourceUtils.isScanInProgress())
            {
                final Runnable runnable = new Runnable(){
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        modifySoap12Binding(javaSource, modificationTask, 
                                    implClass, false);
                    }
                };
                SwingUtilities.invokeLater( new Runnable(){
                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished( runnable, NbBundle.getMessage(
                                JaxWsUtils.class, "LBL_ConfigureSoapBinding"));  // NOI18N
                    }
                });
            }
            else { 
                result.commit();
                saveFile(implClass);
            }
        }
        catch (IOException ex) {
            Logger.getLogger( JaxWsUtils.class.getName()).log( 
                    Level.WARNING, null , ex);
        }
    }

    private static void saveFile(FileObject file) throws IOException {
        DataObject dataObject = DataObject.find(file);
        if (dataObject != null) {
            SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
            if (cookie != null) {
                cookie.save();
            }
        }
    }

    /** Setter for WebMethod annotation attribute, e.g. operationName = "HelloOperation"
     *
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static void setWebMethodAttrValue(FileObject implClassFo, 
            final ElementHandle<?> method, final String attrName, 
            final String attrValue) 
    {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final boolean isIncomplete[] = new boolean[1];
        final CancellableTask<WorkingCopy> modificationTask = 
            new CancellableTask<WorkingCopy>() 
       {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                Element methodEl = method.resolve(workingCopy);
                if (methodEl == null) {
                    isIncomplete[0] = true;
                }
                else {
                    GenerationUtils genUtils = GenerationUtils.
                        newInstance(workingCopy);
                    if (genUtils != null) {
                        TreeMaker make = workingCopy.getTreeMaker();

                        ExpressionTree attrExpr =
                                (attrValue == null ? null : genUtils.
                                        createAnnotationArgument(attrName, attrValue));

                        MethodTree methodTree = (MethodTree) workingCopy.getTrees().
                            getTree(methodEl);

                        ModifiersTree modif = methodTree.getModifiers();
                        List<? extends AnnotationTree> annotations = 
                                modif.getAnnotations();
                        List<AnnotationTree> newAnnotations = 
                                new ArrayList<AnnotationTree>();

                        boolean foundWebMethodAn = false;

                        TypeElement webMethodEl = workingCopy.getElements().
                            getTypeElement("javax.jws.WebMethod"); //NOI18N
                        if ( webMethodEl == null ){
                            isIncomplete[0] = true;
                            return;
                        }
                        
                        for (AnnotationTree an : annotations) {
                            IdentifierTree ident = (IdentifierTree) an.getAnnotationType();
                            TreePath anTreePath = workingCopy.getTrees().
                                getPath(workingCopy.getCompilationUnit(), ident);
                            TypeElement anElement = (TypeElement) workingCopy.
                                getTrees().getElement(anTreePath);
                            if ( anElement == null ){
                                isIncomplete[0] = true;
                            }
                            else if ( anElement.getQualifiedName().
                                    contentEquals(webMethodEl.getQualifiedName())) 
                            {
                                foundWebMethodAn = true;
                                List<? extends ExpressionTree> expressions = 
                                        an.getArguments();
                                List<ExpressionTree> newExpressions = 
                                    new ArrayList<ExpressionTree>();
                                boolean attrFound = false;
                                for (ExpressionTree expr : expressions) {
                                    AssignmentTree as = (AssignmentTree) expr;
                                    IdentifierTree id = (IdentifierTree) as.getVariable();
                                    if (id.getName().contentEquals(attrName)) {
                                        attrFound = true;
                                        if (attrExpr != null) {
                                            newExpressions.add(attrExpr);
                                        }
                                    } else {
                                        newExpressions.add(expr);
                                    }
                                }
                                if (!attrFound) {
                                    newExpressions.add(attrExpr);
                                }

                                AnnotationTree webMethodAn = make.Annotation(
                                        make.QualIdent(webMethodEl), newExpressions);
                                newAnnotations.add(webMethodAn);
                            } else {
                                newAnnotations.add(an);
                            }
                        }

                        if (!foundWebMethodAn && attrExpr != null) {
                            AnnotationTree webMethodAn = make.Annotation(
                                    make.QualIdent(webMethodEl),
                                    Collections.<ExpressionTree>singletonList(attrExpr));
                            newAnnotations.add(webMethodAn);
                        }

                        ModifiersTree newModifier = make.Modifiers(modif, 
                                newAnnotations);
                        workingCopy.rewrite(modif, newModifier);
                    }
                }
            }
            @Override
            public void cancel() {
            }
        };
        try {
            ModificationResult result = javaSource.runModificationTask(modificationTask);
            if ( isIncomplete[0] && 
                    org.netbeans.api.java.source.SourceUtils.isScanInProgress())
            {
                final Runnable runnable = new Runnable(){
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        try {
                            javaSource.runModificationTask(modificationTask).commit();
                        }
                        catch (IOException ex) {
                            Logger.getLogger(JaxWsUtils.class.getName()).log(
                                    Level.WARNING, null , ex);
                        }
                    }
                };
                SwingUtilities.invokeLater( new Runnable(){
                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished( runnable, NbBundle.getMessage(
                                JaxWsUtils.class, "LBL_ConfigureMethod"));  // NOI18N
                    }
                });
            }
            else { 
                result.commit();
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(JaxWsUtils.class.getName()).log(Level.WARNING, null , ex);
        }

    }

    /** Setter for WebParam annotation attribute, e.g. name = "x"
     *
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static void setWebParamAttrValue(FileObject implClassFo, 
            final ElementHandle<?> methodHandle, final String paramName,
            final String attrName, final String attrValue) 
    {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final boolean isIncomplete[] = new boolean[1];
        final CancellableTask<WorkingCopy> modificationTask = 
            new CancellableTask<WorkingCopy>() 
        {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                Element methodEl = methodHandle.resolve(workingCopy);
                if ( methodEl == null ){
                    isIncomplete[0] = true;
                }
                else {
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    if (genUtils != null) {
                        TreeMaker make = workingCopy.getTreeMaker();

                        ExpressionTree attrExpr =
                                (attrValue == null ? null :
                                    genUtils.createAnnotationArgument(attrName, 
                                            attrValue));

                        MethodTree methodTree = (MethodTree) workingCopy.getTrees().
                            getTree(methodEl);
                        List<? extends VariableTree> parameters = 
                            methodTree.getParameters();

                        TypeElement webParamEl = workingCopy.getElements().
                            getTypeElement("javax.jws.WebParam"); //NOI18N
                        if ( webParamEl == null ){
                            isIncomplete[0] = true;
                            return;
                        }
                        for (VariableTree paramTree : parameters) {
                            if (paramTree.getName().contentEquals(paramName)) {
                                ModifiersTree modif = paramTree.getModifiers();
                                List<? extends AnnotationTree> annotations = 
                                    modif.getAnnotations();
                                List<AnnotationTree> newAnnotations = 
                                    new ArrayList<AnnotationTree>();

                                boolean foundWebParamAn = false;

                                for (AnnotationTree an : annotations) {
                                    IdentifierTree ident = (IdentifierTree) an.
                                        getAnnotationType();
                                    TreePath anTreePath = workingCopy.getTrees().
                                        getPath(workingCopy.getCompilationUnit(), 
                                                ident);
                                    TypeElement anElement = (TypeElement) workingCopy.
                                        getTrees().getElement(anTreePath);
                                    if ( anElement == null ){
                                        isIncomplete[0] = true;
                                    }
                                    else if (anElement.getQualifiedName().
                                            contentEquals(webParamEl.getQualifiedName())) 
                                    { 
                                        foundWebParamAn = true;
                                        List<? extends ExpressionTree> expressions = 
                                            an.getArguments();
                                        List<ExpressionTree> newExpressions = 
                                            new ArrayList<ExpressionTree>();
                                        boolean attrFound = false;
                                        for (ExpressionTree expr : expressions) {
                                            AssignmentTree as = 
                                                (AssignmentTree) expr;
                                            IdentifierTree id = 
                                                (IdentifierTree) as.getVariable();
                                            if (id.getName().contentEquals(attrName)) {
                                                attrFound = true;
                                                if (attrExpr != null) {
                                                    newExpressions.add(attrExpr);
                                                }
                                            } 
                                            else {
                                                newExpressions.add(expr);
                                            }

                                        }
                                        if (!attrFound) {
                                            newExpressions.add(attrExpr);
                                        }

                                        AnnotationTree webParamAn = make.Annotation(
                                                make.QualIdent(webParamEl), 
                                                newExpressions);
                                        newAnnotations.add(webParamAn);
                                    } else {
                                        newAnnotations.add(an);
                                    }
                                }

                                if (!foundWebParamAn && attrExpr != null) {
                                    AnnotationTree webParamAn = make.Annotation(
                                            make.QualIdent(webParamEl),
                                            Collections.<ExpressionTree>singletonList(attrExpr));
                                    newAnnotations.add(webParamAn);
                                }

                                ModifiersTree newModifier = make.Modifiers(modif, newAnnotations);
                                workingCopy.rewrite(modif, newModifier);
                                break;
                            }
                        }
                    }
                }
            }

            public void cancel() {
            }
        };
        try {
            ModificationResult result = javaSource.runModificationTask(modificationTask);
            if ( isIncomplete[0] && 
                    org.netbeans.api.java.source.SourceUtils.isScanInProgress())
            {
                final Runnable runnable = new Runnable(){
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        try {
                            javaSource.runModificationTask(modificationTask).commit();
                        }
                        catch (IOException ex) {
                            Logger.getLogger(JaxWsUtils.class.getName()).log(
                                    Level.WARNING, null , ex);
                        }
                    }
                };
                SwingUtilities.invokeLater( new Runnable(){
                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished( runnable, NbBundle.getMessage(
                                JaxWsUtils.class, "LBL_ConfigureMethodParameter"));  // NOI18N
                    }
                });
            }
            else { 
                result.commit();
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(JaxWsUtils.class.getName()).log(Level.WARNING, null , ex);
        }
    }

    /**
     * @param model the WSDL model the handler is for.
     * @return true if the WSDL model operation parameters could be set in SOAP header
     */
    public static boolean needsSoapHandler(WsdlModel model) {
        //TODO
        return false;
    }

    /**
     * Retrieve map of SOAP header element QName and its java type name.
     * @param model the WSDL model the handler is for.
     * @return map of SOAP header element QName and its java type name.
     */
    public static Map<QName, String> getSoapHandlerParameterTypes(WsdlModel model) {
        return null;
    }

    public static Map<QName, String> getSoapHandlerParameterTypes(PortType portType) {
        Map<QName, String> paramMap = new HashMap<QName, String>();

        Definitions definitions = portType.getModel().getDefinitions();
        Collection<Binding> bindings = definitions.getBindings();
        Binding binding = null;
        for (Binding b : bindings) {
            NamedComponentReference<PortType> portTypeRef = b.getType();
            if (portTypeRef.get().equals(portType)) {
                binding = b;
                break;

            }

        }
        if (binding != null) {
            //Determine if it is a SOAP binding
            List<SOAPBinding> soapBindings = binding.
                getExtensibilityElements(SOAPBinding.class);
            if (soapBindings.size() >
                    0) { //we can assume that this is the only SOAP binding
                Collection<BindingOperation> bindingOperations = 
                    binding.getBindingOperations();
                for (BindingOperation bOp : bindingOperations) {
                    BindingInput bindingInput = bOp.getBindingInput();
                    Collection<SOAPHeader> headers = bindingInput.
                        getExtensibilityElements(SOAPHeader.class);
                    for (SOAPHeader header : headers) {
                        NamedComponentReference<Message> messageRef = 
                            header.getMessage();
                        Message message = messageRef.get();
                        String partName = header.getPart();
                        Collection<Message> messages = definitions.getMessages();
                        for (Message m : messages) {
                            if (m.equals(message)) {
                                Collection<Part> parts = m.getParts();
                                for (Part part : parts) {
                                    if (part.getName().equals(partName)) {
                                        NamedComponentReference<GlobalElement> elementRef = part.getElement();
                                        if (elementRef != null) {
                                            QName qname = elementRef.getQName();
                                            if (!paramMap.containsKey(qname)) {
                                                paramMap.put(qname, "");         // NOI18N
                                            }
                                        } 
                                        else {
                                            NamedComponentReference<GlobalType> typeRef = part.getType();
                                            if (typeRef != null) {
                                                QName qname = typeRef.getQName();
                                                if (!paramMap.containsKey(qname)) {
                                                    paramMap.put(qname, "");     // NOI18N
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return paramMap;
    }

    /**
     * Retrieve SOAP handler for given WSDL model.
     * @param model the WSDL model the handler is for.
     * @return the hanlder or null if none exist.
     */
    public static FileObject getSoapHandler(
            WsdlModel model) {
        //TODO
        return null;
    }

    public static FileObject createSoapHandler(
            FileObject dest, PortType portType, Map<QName, Object> soapHeaderValues)
            throws IOException {
        String handlerName = portType.getName() + "_handler.java";      // NOI18N
        DataObject dataObj = createDataObjectFromTemplate(HANDLER_TEMPLATE, dest, 
                handlerName);

        //TODO Generate code for initializing the header values.
        return dataObj.getPrimaryFile();
    }

    /**
     * Create SOAP handler for given WSDL model.
     * @param destdir destination directory.
     * @param model the WSDL model the handler is for.
     * @param soapHeaderValues values for SOAP header elements.
     */
    public static FileObject createSoapHandler(
            FileObject dest, WsdlModel model, Map<QName, Object> soapHeaderValues) {
        return null;

    }

    public static DataObject createDataObjectFromTemplate(
            String template,
            FileObject targetFolder, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() > 0;

        FileObject templateFO = FileUtil.getConfigFile(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);

        return templateDO.createFromTemplate(dataFolder, targetName);
    }

    public static boolean isInSourceGroup(Project prj, String serviceClass) {

        SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).
            getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup group : sourceGroups) {
            String resource = serviceClass.replace('.', '/') + ".java"; //NOI18N
            if (group.getRootFolder().getFileObject(resource) != null) {
                return true;
            }

        }
        return false;
    }

    /** Test if EJBs are supported in J2EE Container, e.g. in Tomcat they are not
     * 
     * @param project
     * @return
     */
    public static boolean isEjbSupported(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().
            lookup(J2eeModuleProvider.class);

        if (j2eeModuleProvider != null) {
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            if (serverInstanceId == null) {
                return false;
            }
            try {
                J2eePlatform platform = Deployment.getDefault().
                    getServerInstance(serverInstanceId).getJ2eePlatform();
                if (platform.getSupportedTypes().contains(J2eeModule.Type.EJB)) {
                    return true;
                }
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(JaxWsUtils.class.getName()).log(Level.INFO, 
                        "Failed to find J2eePlatform", ex);     // NOI18N
            }
        }
        return false;
    }

    public static boolean isRPCEncoded(URI wsdlURI) {
        try {
            FileObject wsdlFO = FileUtil.toFileObject(new File(wsdlURI));
            
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().
                    getModel(org.netbeans.modules.xml.retriever.catalog.Utilities.
                            createModelSource(wsdlFO, true));
            Definitions definitions = wsdlModel.getDefinitions();
            if (definitions != null) {
                Collection<Binding> bindings = definitions.getBindings();
                for (Binding binding : bindings) {
                    List<SOAPBinding> soapBindings = binding.
                        getExtensibilityElements(SOAPBinding.class);
                    for (SOAPBinding soapBinding : soapBindings) {
                        if (soapBinding.getStyle() == Style.RPC) {
                            Collection<BindingOperation> bindingOperations = 
                                binding.getBindingOperations();
                            for (BindingOperation bindingOperation : bindingOperations) {
                                BindingInput bindingInput = 
                                    bindingOperation.getBindingInput();
                                if (bindingInput != null) {
                                    List<SOAPBody> soapBodies = 
                                        bindingInput.getExtensibilityElements(
                                                SOAPBody.class);
                                    if (soapBodies != null && 
                                            soapBodies.size() > 0) 
                                    {
                                        SOAPBody soapBody = soapBodies.get(0);
                                        if (soapBody.getUse() == Use.ENCODED) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (CatalogModelException ex) {
            Logger.getGlobal().log(Level.INFO, "", ex);
        }

        return false;
    }
    
    public static Service findServiceForServiceName(FileObject createdFile, 
            String serviceName) 
    {
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(createdFile);
        List services = support.getServices();
        if (services.size()>1) {
            Project prj = FileOwnerQuery.getOwner(createdFile);
            for (int i=0;i<services.size()-1;i++) { // check only formerly created services
                Service service = (Service)services.get(i);
                if (service.getWsdlUrl() != null) {
                    // from WSDL
                    if (serviceName.equals(service.getServiceName())) {
                        return service;
                    }
                } else {
                    // from Java
                    if (serviceName.equals(getServiceName(prj, service))) {
                        return service;
                    }
                }
            }
        }
        return null;
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_PARAM_DEREF")
    private static String getServiceName(Project prj, Service service) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(prj).
            getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject implClassFo = null;
        String implClassResource = service.getImplementationClass().
            replace('.', '/') + ".java"; //NOI18N
        final String[] serviceName = new String[1];
        if (srcGroups != null) {
            for (SourceGroup srcGroup: srcGroups) {
                FileObject root = srcGroup.getRootFolder();
                implClassFo = root.getFileObject(implClassResource);
                if (implClassFo != null) break;
            }
        }
        if (implClassFo != null) {
            JavaSource javaSource = JavaSource.forFileObject(implClassFo);
            if (javaSource != null) {
                final CancellableTask<CompilationController> task = 
                    new CancellableTask<CompilationController>() {

                    @Override
                    public void run(CompilationController controller) 
                        throws IOException 
                    {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement classElement = SourceUtils.
                            getPublicTopLevelElement(controller);
                        if (classElement != null ) {
                            List<? extends AnnotationMirror> annotations = 
                                classElement.getAnnotationMirrors();

                            for (AnnotationMirror anMirror : annotations) {
                                Element annotationElement = 
                                    anMirror.getAnnotationType().asElement();
                                String fqn = null;
                                if ( annotationElement instanceof TypeElement ){
                                    fqn = ((TypeElement)annotationElement).
                                        getQualifiedName().toString();
                                }
                                if ("javax.jws.WebService".contentEquals( fqn )) {  // NOI18N
                                    Map<? extends ExecutableElement, 
                                            ? extends AnnotationValue> expressions = 
                                                anMirror.getElementValues();
                                    for (Map.Entry<? extends ExecutableElement, 
                                            ? extends AnnotationValue> entry : 
                                                expressions.entrySet()) 
                                    {
                                        if (entry.getKey().getSimpleName().
                                                contentEquals("serviceName")) { //NOI18N
                                            serviceName[0] = (String) expressions.
                                            get(entry.getKey()).getValue();
                                        }
                                        if (serviceName[0] != null) {
                                            break;
                                        }
                                    }
                                    break;
                                } // end if
                            } // end for
                        }
                    }

                    public void cancel() {
                    }
                };
                try {
                    javaSource.runUserActionTask(task, true);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            if (serviceName[0] == null) {
                serviceName[0] = implClassFo.getName()+"Service"; //NOI18N
            }
        }
        return serviceName[0];
    }

    public static J2eeModule getJ2eeModule(Project prj) {
        J2eeModuleProvider provider = prj.getLookup().lookup(
                J2eeModuleProvider.class);
        if (provider != null) {
            return provider.getJ2eeModule();
        }
        return null;
    }

    public static String getModuleType(Project prj) {
        J2eeModuleProvider provider = (J2eeModuleProvider) prj.getLookup().
        lookup(J2eeModuleProvider.class);
        if (provider != null) {
            J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
            if (J2eeModule.Type.EJB.equals(moduleType)) {
                return "EJB"; //NOI18N
            } else if (J2eeModule.Type.WAR.equals(moduleType)) {
                return "WAR"; //NOI18N
            } else if (J2eeModule.Type.CAR.equals(moduleType)) {
                return "CAR"; //NOI18N
            } else {
                return "UNKNOWN"; //NOI18N
            }
        } else {
            return "J2SE"; //NOI18N
        }

    }

    public static boolean askForSunJaxWsConfig(JaxWsModel jaxWsModel) {
        NotifyDescriptor desc =
               new DialogDescriptor.Confirmation(
               NbBundle.getMessage(JaxWsUtils.class, "MSG_USE_METRO"),  // NOI18N
               DialogDescriptor.YES_NO_OPTION);
        DialogDisplayer.getDefault().notify(desc);
        boolean jsr109 = true;
        if (desc.getValue().equals(DialogDescriptor.YES_OPTION)) {
            // NON JSR 109
            jaxWsModel.setJsr109(Boolean.FALSE);
            try {
                jaxWsModel.write();
            } catch (IOException ex) {
                Logger.getLogger(JaxWsUtils.class.getName()).log(
                        Level.FINE,"jax-ws.xml not yet exists",ex);     // NOI18N
            }
            jsr109 = false;
        } else {
            // JSR 109
            jaxWsModel.setJsr109(Boolean.TRUE);
            try {
                jaxWsModel.write();
            } catch (IOException ex) {
                Logger.getLogger(JaxWsUtils.class.getName()).log(Level.FINE,
                        "jax-ws.xml not yet exists",ex);
            }
            jsr109 = true;
       }
       return jsr109;
    }

    private static boolean isJsr109 (JaxWsModel jaxWsModel) {
        if (jaxWsModel.getJsr109() != null) {
            return jaxWsModel.getJsr109();
        } else if (!jsr109Supported) {
            return askForSunJaxWsConfig(jaxWsModel);
        } else {
            return true;
        }
    }
}
