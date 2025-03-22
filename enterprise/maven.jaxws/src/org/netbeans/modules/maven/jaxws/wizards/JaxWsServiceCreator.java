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
package org.netbeans.modules.maven.jaxws.wizards;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.swing.SwingUtilities;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.maven.jaxws.MavenJAXWSSupportImpl;
import org.netbeans.modules.maven.jaxws.MavenWebService;
import org.netbeans.modules.maven.jaxws.WSUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.support.ServiceCreator;
import java.io.IOException;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.jaxws.MavenModelUtils;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radko, Milan Kuchtiak
 */
public class JaxWsServiceCreator implements ServiceCreator {
    private static final String SOAP_BINDING_TYPE = "javax.xml.ws.soap.SOAPBinding";  //NOI18N
    private static final String BINDING_TYPE_ANNOTATION = "javax.xml.ws.BindingType"; //NOI18N
    private static final String JAKARTAEE_SOAP_BINDING_TYPE = "jakarta.xml.ws.soap.SOAPBinding";  //NOI18N
    private static final String JAKARTAEE_BINDING_TYPE_ANNOTATION = "jakarta.xml.ws.BindingType"; //NOI18N
    private static final String SOAP12_HTTP_BINDING = "SOAP12HTTP_BINDING"; //NOI18N
    
    private Project project;
    private WizardDescriptor wiz;
    private final boolean addJaxWsLib;
    private final boolean isWeb;
    private final boolean isEJB;
    private final boolean isJakartaEENameSpace;
    private int serviceType;
    
    private static final Logger LOG = Logger.getLogger(JaxWsServiceCreator.class.getCanonicalName());

    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsServiceCreator(Project project, WizardDescriptor wiz, boolean addJaxWsLib) {
        this.project = project;
        this.wiz = wiz;
        this.addJaxWsLib = addJaxWsLib;
        this.isWeb = WSUtils.isWeb(project);
        this.isEJB = WSUtils.isEJB(project);
        this.isJakartaEENameSpace = WSUtils.isJakartaEENameSpace(project);
    }

    @Override
    public void createService() throws IOException {
        serviceType = ((Integer) wiz.getProperty(WizardProperties.WEB_SERVICE_TYPE));

        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
        handle.start(100);

        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    generateWebService(handle);
                } catch (IOException e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if (message != null) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }

    @Override
    public void createServiceFromWsdl() throws IOException {

        //initProjectInfo(project);

        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N

        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    handle.start(100);
                    generateWsFromWsdl15(handle);
                } catch (IOException e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if (message != null) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }

    //TODO it should be refactored to prevent duplicate code but it is more readable now during development
    private void generateWebService(ProgressHandle handle) throws IOException {

        FileObject pkg = Templates.getTargetFolder(wiz);

        if (serviceType == WizardProperties.FROM_SCRATCH) {
            handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_WS"), 50); //NOI18N
            //add the JAX-WS library, if not already added
            if (addJaxWsLib) {
                MavenModelUtils.addMetroLibrary(project);
            }
            generateJaxWSImplFromTemplate(pkg, isEJB, false, false);
            handle.finish();
        } else if (serviceType == WizardProperties.ENCAPSULATE_SESSION_BEAN) {
            String wsName = Templates.getTargetName(wiz);
            handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
            Node[] nodes = (Node[]) wiz.getProperty(WizardProperties.DELEGATE_TO_SESSION_BEAN);
            generateWebServiceFromEJB(wsName, pkg, nodes);
            handle.finish();
        }
    }

    private void generateWsFromWsdl15(final ProgressHandle handle) throws IOException {
        handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_WS"), 50); //NOI18N

        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(project.getProjectDirectory());
        String wsdlUrl = (String)wiz.getProperty(WizardProperties.WSDL_URL);
        String filePath = (String)wiz.getProperty(WizardProperties.WSDL_FILE_PATH);

        //Boolean useDispatch = (Boolean) wiz.getProperty(ClientWizardProperties.USEDISPATCH);
        //if (wsdlUrl==null) wsdlUrl = "file:"+(filePath.startsWith("/")?filePath:"/"+filePath); //NOI18N

        if(wsdlUrl == null) {
            wsdlUrl = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filePath))).toURL().toExternalForm();
        }
        FileObject localWsdlFolder = jaxWsSupport.getWsdlFolder(true);

        boolean hasSrcFolder = false;
        File srcFile = new File (FileUtil.toFile(project.getProjectDirectory()),"src"); //NOI18N
        if (srcFile.exists()) {
            hasSrcFolder = true;
        } else {
            hasSrcFolder = srcFile.mkdirs();
        }

        if (localWsdlFolder != null) {
            FileObject wsdlFo = null;
            try {
                wsdlFo = WSUtils.retrieveResource(
                        localWsdlFolder,
                        (hasSrcFolder ? new URI(MavenJAXWSSupportImpl.CATALOG_PATH) : new URI("jax-ws-catalog.xml")), //NOI18N
                        new URI(wsdlUrl));
            } catch (URISyntaxException ex) {
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(JaxWsServiceCreator.class, "ERR_IncorrectURI", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (UnknownHostException ex) {
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(JaxWsServiceCreator.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (IOException ex) {
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(JaxWsServiceCreator.class, "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }

            if (wsdlFo != null) {
                final WsdlService wsdlService = (WsdlService) wiz.getProperty(WizardProperties.WSDL_SERVICE);
                final WsdlPort wsdlPort = (WsdlPort) wiz.getProperty(WizardProperties.WSDL_PORT);

                if (wsdlService == null || wsdlPort == null) {
                    WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
                    if (wsdlModeler != null && wsdlModeler.getCreationException() != null) {
                        handle.finish();
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_CannotGenerateArtifacts",
                                wsdlModeler.getCreationException().getLocalizedMessage()),
                                NotifyDescriptor.ERROR_MESSAGE));
                    } else {
                        handle.finish();
                    }
                } else {
                    final boolean isJaxWsLibrary = MavenModelUtils.hasJaxWsAPI(project, isJakartaEENameSpace);
                    final String relativePath = FileUtil.getRelativePath(localWsdlFolder, wsdlFo);
                    final String serviceName = wsdlFo.getName();

                    Preferences prefs = ProjectUtils.getPreferences(project, MavenWebService.class,true);
                    if (prefs != null) {
                        // remember original WSDL URL for service
                        prefs.put(MavenWebService.SERVICE_PREFIX+WSUtils.getUniqueId(wsdlFo.getName(), jaxWsSupport.getServices()), wsdlUrl);
                    }

                    if (!isJaxWsLibrary) {
                        try {
                            MavenModelUtils.addMetroLibrary(project);
                            MavenModelUtils.addJavadoc(project);
                        } catch (Exception ex) {
                            Logger.getLogger(
                                JaxWsServiceCreator.class.getName()).log(
                                    Level.INFO, "Cannot add Metro libbrary to pom file", ex); //NOI18N
                        }
                    }

                    ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                        @Override
                        public void performOperation(POMModel model) {
                            org.netbeans.modules.maven.model.pom.Plugin plugin =
                                    isEJB ?
                                        MavenModelUtils.addJaxWSPlugin(model, "2.0") : //NOI18N
                                        MavenModelUtils.addJaxWSPlugin(model);
                            MavenModelUtils.addWsimportExecution(plugin, 
                                    serviceName, relativePath,null );
                            if (isWeb) { // expecting web project
                                MavenModelUtils.addWarPlugin(model, false);
                            } else { // J2SE Project
                                MavenModelUtils.addWsdlResources(model);
                            }
                        }
                    };
                    Utilities.performPOMModelOperations(project.getProjectDirectory().getFileObject("pom.xml"),
                            Collections.singletonList(operation));

                    // create empty web service implementation class
                    FileObject pkg = Templates.getTargetFolder(wiz);
                    boolean useProvider = (Boolean)wiz.getProperty(WizardProperties.USE_PROVIDER);
                    boolean isStateless = (Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN);
                    
                    final FileObject targetFile = generateJaxWSImplFromTemplate(pkg, isStateless, true, useProvider);

                    // execute wsimport goal
                    RunConfig cfg = RunUtils.createRunConfig(
                            FileUtil.toFile(project.getProjectDirectory()),
                            project,
                            "JAX-WS:wsimport", //NOI18N
                            Collections.singletonList("compile")); //NOI18N
                    ExecutorTask task = RunUtils.executeMaven(cfg);
                    try {
                        task.waitFinished(60000);
                    } catch (InterruptedException ex) {

                    }

                    try {
                        String wsdlLocationPrefix = isWeb ? "WEB-INF/wsdl/" : "META-INF/wsdl/"; //NOI18N
                        generateJaxWsImplClass(targetFile, wsdlService, wsdlPort, wsdlLocationPrefix+relativePath, useProvider);
                        DataObject targetDo = DataObject.find(targetFile);
                        if (targetDo != null) {
                            SaveCookie save = targetDo.getCookie(SaveCookie.class);
                            if (save != null) {
                                save.save();
                            }
                        }

                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                    }
                }

            }
        }

        handle.finish();
    }
    
    private FileObject generateJaxWSImplFromTemplate(FileObject pkg, boolean isEjbTemplate, boolean fromWsdl, boolean useProvider) throws IOException {
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);

        if (useProvider) {
            FileObject templateParent = template.getParent();
            if (isEjbTemplate) {
                template = templateParent.getFileObject("EjbWebServiceProvider", "java"); //NOI18N
            } else {
                template = templateParent.getFileObject("WebServiceProvider", "java"); //NOI18N
            }
        } else if (!fromWsdl && (Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN)) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("EjbWebService", "java"); //NOI18N
        }
        
        DataObject dTemplate = DataObject.find(template);

        DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wiz));
        FileObject createdFile = dobj.getPrimaryFile();
        /*createdFile.setAttribute("jax-ws-service", java.lang.Boolean.TRUE);     // NOI18N
        try {
            dobj.setValid(false);
        }
        catch( PropertyVetoException e ){
            LOG.log(Level.WARNING, null , e);
        }*/
        dobj = DataObject.find(createdFile);
        
           
        openFileInEditor(dobj);

        return createdFile;
    }
    
    private ClassPath getClassPathForFile(Project project, FileObject file) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup srcGroup: srcGroups) {
            FileObject srcRoot = srcGroup.getRootFolder();
            if (FileUtil.isParentOf(srcRoot, file)) {
                return ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
            }
        }
        return null;
    }
    
    public static void openFileInEditor(DataObject dobj) {

        final OpenCookie openCookie = dobj.getCookie(OpenCookie.class);
        if (openCookie != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    openCookie.open();
                }
            }, 1000);
        }
    }

    private void generateJaxWsImplClass(FileObject targetFile, 
            final WsdlService service, final WsdlPort port, 
            final String wsdlLocation, final boolean useProvider) throws IOException {

        final JavaSource targetSource = JavaSource.forFileObject(targetFile);
        final boolean[] isIncomplete = new boolean[1];
        final CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    //add @WebService annotation
                    List<ExpressionTree> attrs = new ArrayList<>();
                    attrs.add(
                            make.Assignment(make.Identifier("serviceName"), 
                                    make.Literal(service.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("portName"), 
                                    make.Literal(port.getName()))); //NOI18N
                    
                    if (!useProvider) {
                        attrs.add(
                            make.Assignment(make.Identifier("endpointInterface"), 
                                    make.Literal(port.getJavaName()))); //NOI18N
                    }
                    attrs.add(
                            make.Assignment(make.Identifier("targetNamespace"), 
                                    make.Literal(port.getNamespaceURI()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("wsdlLocation"), 
                                    make.Literal(wsdlLocation))); //NOI18N

                    final String wspClazz = isJakartaEENameSpace ?
                            "jakarta.xml.ws.WebServiceProvider" : "javax.xml.ws.WebServiceProvider"; //NOI18N
                    final String wsClazz = isJakartaEENameSpace ?
                            "jakarta.jws.WebService" : "javax.jws.WebService"; //NOI18N
                    AnnotationTree WSAnnotation = make.Annotation(
                            useProvider ? 
                                make.QualIdent(wspClazz) : make.QualIdent(wsClazz), attrs);
                    
                    ClassTree  modifiedClass = genUtils.addAnnotation(javaClass, 
                            WSAnnotation);

                    if (WsdlPort.SOAP_VERSION_12.equals(port.getSOAPVersion())) {
                        //if SOAP 1.2 binding, add BindingType annotation
                        TypeElement bindingElement;
                        if (isJakartaEENameSpace) {
                            bindingElement = workingCopy.getElements().
                                getTypeElement(JAKARTAEE_BINDING_TYPE_ANNOTATION);
                        } else {
                            bindingElement = workingCopy.getElements().
                                getTypeElement(BINDING_TYPE_ANNOTATION);
                        }
                        if (bindingElement == null) {
                            isIncomplete[0] = true;
                        }
                        else {
                            TypeElement soapBindingElement;
                            if (isJakartaEENameSpace) {
                                soapBindingElement = workingCopy.
                                    getElements().getTypeElement(JAKARTAEE_SOAP_BINDING_TYPE);
                            } else {
                                soapBindingElement = workingCopy.
                                    getElements().getTypeElement(SOAP_BINDING_TYPE);
                            }
                            ExpressionTree exp = make.MemberSelect(
                                    make.QualIdent(soapBindingElement), SOAP12_HTTP_BINDING);

                            AnnotationTree bindingAnnotation = make.Annotation(
                                    make.QualIdent(bindingElement),
                                    Collections.<ExpressionTree>singletonList(exp));

                            modifiedClass = genUtils.addAnnotation(modifiedClass, 
                                    bindingAnnotation);
                        }
                    }

                    if (!useProvider) {
                        // add @Stateless annotation
                        if (isEJB) {
                            final String statelessClazz = isJakartaEENameSpace ?
                                    "jakarta.ejb.Stateless" : "javax.ejb.Stateless"; //NOI18N
                            TypeElement statelessAn = workingCopy.getElements().
                                    getTypeElement(statelessClazz);
                            if (statelessAn != null) {
                                AnnotationTree StatelessAnnotation = make.Annotation(
                                        make.QualIdent(statelessAn),
                                        Collections.<ExpressionTree>emptyList());
                                modifiedClass = genUtils.addAnnotation(modifiedClass, 
                                        StatelessAnnotation);
                            }
                            else {
                                isIncomplete[0] = true;
                            }
                        }

                        List<WsdlOperation> operations = port.getOperations();
                        for (WsdlOperation operation : operations) {

                            // return type
                            String returnType = operation.getReturnTypeName();

                            // create parameters
                            List<WsdlParameter> parameters = operation.getParameters();
                            List<VariableTree> params = new ArrayList<>();
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
                            List<ExpressionTree> exc = new ArrayList<>();
                            while (exceptions.hasNext()) {
                                String exception = exceptions.next();
                                TypeElement excEl = workingCopy.getElements().getTypeElement(exception);
                                if (excEl != null) {
                                    exc.add(make.QualIdent(excEl));
                                } else {
                                    isIncomplete[0] = true;
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
                    }
                    workingCopy.rewrite(javaClass, modifiedClass);

                }
            }
            @Override
            public void cancel() {
            }
        };
        ModificationResult modificationTask = targetSource.runModificationTask(task);
        if ( isIncomplete[0] && 
                org.netbeans.api.java.source.SourceUtils.isScanInProgress())
        {
            final String title = NbBundle.getMessage(JaxWsServiceCreator.class, 
                    "LBL_GenWsClass");      // NOI18N
            final Runnable runnable = new Runnable() {
                
                @Override
                public void run() {
                    try {
                        targetSource.runModificationTask(task).commit();
                    }
                    catch ( IOException e){
                        Logger.getLogger( JaxWsServiceCreator.class.getCanonicalName()).
                            log(Level.WARNING , null , e);
                    }
                }
            };
            if ( SwingUtilities.isEventDispatchThread() ){
                ScanDialog.runWhenScanFinished(runnable, title);
            }
            else {
                SwingUtilities.invokeLater( new Runnable() {
                    
                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished(runnable, title);
                    }
                });
            }
        }
        else {
            modificationTask.commit();
        }
    }

    private void generateWebServiceFromEJB(String wsName, FileObject pkg, Node[] nodes) throws IOException {

        if (nodes != null && nodes.length == 1) {

            EjbReference ejbRef = nodes[0].getLookup().lookup(EjbReference.class);
            if (ejbRef != null) {

                DataFolder df = DataFolder.findFolder(pkg);
                FileObject template = Templates.getTemplate(wiz);
                FileObject templateParent = template.getParent();
                if ((Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN)) {
                    template = templateParent.getFileObject("EjbWebServiceNoOp", "java"); //NOI18N
                } else {
                    template = templateParent.getFileObject("WebServiceNoOp", "java"); //NOI18N
                }
                DataObject dTemplate = DataObject.find(template);
                DataObject dobj = dTemplate.createFromTemplate(df, wsName);
                FileObject createdFile = dobj.getPrimaryFile();
                /*createdFile.setAttribute("jax-ws-service", java.lang.Boolean.TRUE); // NOI18N
                try {
                    dobj.setValid(false);
                }
                catch( PropertyVetoException e ){
                    LOG.log(Level.WARNING, null , e);
                }*/
                dobj = DataObject.find(createdFile);
                

                ClassPath classPath = getClassPathForFile(project, createdFile);
                if (classPath != null) {
                    final String ejbClazz = isJakartaEENameSpace ?
                            "jakarta/ejb/EJB.class" : "javax/ejb/EJB.class"; //NOI18N
                    if (classPath.findResource(ejbClazz) == null) {
                        // ad EJB API on classpath
                        ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
                        if (modifier != null) {
                            modifier.extendClasspath(createdFile, new String[] {
                                ContainerClassPathModifier.API_EJB
                            });
                        }
                    }
                    generateDelegateMethods(createdFile, ejbRef);
                    openFileInEditor(dobj);
                }
            }
        }
    }
    private void generateDelegateMethods(final FileObject targetFo, final EjbReference ref) throws IOException {
        final boolean[] onClassPath = new boolean[1];
        final String[] interfaceClass = new String[1];

        final JavaSource targetSource = JavaSource.forFileObject(targetFo);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                TreeMaker make = workingCopy.getTreeMaker();

                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                if (typeElement != null) {
                    VariableTree ejbRefInjection = null;
                    interfaceClass[0] = ref.getLocal();
                    if (interfaceClass[0] == null) {
                        interfaceClass[0] = ref.getRemote();
                    }
                    if (interfaceClass[0] == null) {
                        interfaceClass[0] = ref.getEjbClass();
                    }
                    ejbRefInjection = generateEjbInjection(workingCopy, make, interfaceClass[0], onClassPath);

                    if (ejbRefInjection != null) {
                        String comment1 = "Add business logic below. (Right-click in editor and choose"; //NOI18N
                        String comment2 = "\"Web Service > Add Operation\""; //NOI18N
                        make.addComment(ejbRefInjection, Comment.create(Comment.Style.LINE, 0, 0, 4, comment1), false);
                        make.addComment(ejbRefInjection, Comment.create(Comment.Style.LINE, 0, 0, 4, comment2), false);

                        ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);
                        ClassTree modifiedClass = make.insertClassMember(javaClass, 0, ejbRefInjection);

                        if (onClassPath[0]) {
                            TypeElement beanInterface = workingCopy.getElements().getTypeElement(interfaceClass[0]);
                            modifiedClass = generateMethods(workingCopy, make, typeElement, modifiedClass, beanInterface);
                        }

                        workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
            }
            @Override
            public void cancel() {
            }
        };
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                try {
                    targetSource.runModificationTask(modificationTask).commit();
                    if (!onClassPath[0]) {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(
                                        NbBundle.getMessage(JaxWsServiceCreator.class, 
                                                "MSG_EJB_NOT_ON_CLASSPATH", 
                                                interfaceClass[0], targetFo.getName()),
                                                    NotifyDescriptor.WARNING_MESSAGE));
                    }
                }
                catch( IOException e ){
                    Logger.getLogger(JaxWsServiceCreator.class.getCanonicalName()).
                        log(Level.WARNING , null, e);
                }
            }
        };
        final String title = NbBundle.getMessage(JaxWsServiceCreator.class, 
                "LBL_GenDelegateMethods");              // NOI18N
        if ( SwingUtilities.isEventDispatchThread()){
            ScanDialog.runWhenScanFinished( runnable , title);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                
                @Override
                public void run() {
                    ScanDialog.runWhenScanFinished( runnable , title);                    
                }
            });
        }
        
    }
    private VariableTree generateEjbInjection(WorkingCopy workingCopy, TreeMaker make, String beanInterface, boolean[] onClassPath) {

        final String ejbClazz = isJakartaEENameSpace ? "jakarta.ejb.EJB" : "javax.ejb.EJB"; //NOI18N
        TypeElement ejbAnElement = workingCopy.getElements().getTypeElement(ejbClazz);
        TypeElement interfaceElement = workingCopy.getElements().getTypeElement(beanInterface);

        AnnotationTree ejbAnnotation = make.Annotation(
                make.QualIdent(ejbAnElement),
                Collections.<ExpressionTree>emptyList());
        // create method modifier: public and no annotation
        ModifiersTree methodModifiers = make.Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE),
                Collections.<AnnotationTree>singletonList(ejbAnnotation));

        onClassPath[0] = interfaceElement != null;

        return make.Variable(
                methodModifiers,
                "ejbRef", //NOI18N
                onClassPath[0] ? make.Type(interfaceElement.asType()) : make.Identifier(beanInterface),
                null);
    }

    private ClassTree generateMethods(WorkingCopy workingCopy,
            TreeMaker make,
            TypeElement classElement,
            ClassTree modifiedClass,
            TypeElement beanInterface) throws IOException {

        GeneratorUtilities utils = GeneratorUtilities.get(workingCopy);

        List<? extends Element> interfaceElements = beanInterface.getEnclosedElements();
        final String webMethodClazz = isJakartaEENameSpace ? "jakarta.jws.WebMethod" : "javax.jws.WebMethod"; //NOI18N
        TypeElement webMethodEl = workingCopy.getElements().getTypeElement(webMethodClazz);
        assert (webMethodEl != null);
        if (webMethodEl == null) {
            return modifiedClass;
        }

        Set<String> operationNames = new HashSet<>();
        for (Element el : interfaceElements) {
            if (el.getKind() == ElementKind.METHOD) {
                ExecutableElement methodEl = (ExecutableElement) el;
                MethodTree method = utils.createAbstractMethodImplementation(classElement, methodEl);

                Name methodName = methodEl.getSimpleName();
                boolean isVoid = workingCopy.getTypes().getNoType(TypeKind.VOID) == methodEl.getReturnType();

                String operationName = findUniqueOperationName(operationNames, methodName.toString());
                operationNames.add(operationName);

                // generate @WebMethod annotation
                AssignmentTree opName = make.Assignment(make.Identifier("operationName"), make.Literal(operationName)); //NOI18N

                AnnotationTree webMethodAn = make.Annotation(
                        make.QualIdent(webMethodEl),
                        Collections.<ExpressionTree>singletonList(opName));
                ModifiersTree modifiersTree = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>singletonList(webMethodAn));

                // generate @RequestWrapper and @RequestResponse annotations
                if (!methodName.contentEquals(operationName)) {
                    final String reqWrapperClazz = isJakartaEENameSpace ?
                            "jakarta.xml.ws.RequestWrapper" : "javax.xml.ws.RequestWrapper"; //NOI18N
                    final String resWrapperClazz = isJakartaEENameSpace ?
                            "jakarta.xml.ws.ResponseWrapper" : "javax.xml.ws.ResponseWrapper"; //NOI18N
                    TypeElement requestWrapperEl = workingCopy.getElements().getTypeElement(reqWrapperClazz);
                    TypeElement responseWrapperEl = workingCopy.getElements().getTypeElement(resWrapperClazz);
                    AssignmentTree className = make.Assignment(make.Identifier("className"), make.Literal(operationName)); //NOI18N
                    AnnotationTree requestWrapperAn = make.Annotation(
                            make.QualIdent(requestWrapperEl),
                            Collections.<ExpressionTree>singletonList(className));
                    modifiersTree = make.addModifiersAnnotation(modifiersTree, requestWrapperAn);

                    if (!isVoid) { // only if not void
                        className = make.Assignment(make.Identifier("className"), make.Literal(operationName + "Response")); //NOI18N
                        AnnotationTree responseWrapperAn = make.Annotation(
                                make.QualIdent(responseWrapperEl),
                                Collections.<ExpressionTree>singletonList(className));
                        modifiersTree = make.addModifiersAnnotation(modifiersTree, responseWrapperAn);
                    }
                }

                // generate @Oneway annotation
                if (isVoid && method.getThrows().isEmpty()) {
                    final String oneWayClazz = isJakartaEENameSpace ?
                            "jakarta.jws.Oneway" : "javax.jws.Oneway"; //NOI18N
                    TypeElement onewayEl = workingCopy.getElements().getTypeElement(oneWayClazz);
                    AnnotationTree onewayAn = make.Annotation(
                            make.QualIdent(onewayEl),
                            Collections.<ExpressionTree>emptyList());
                    modifiersTree = make.addModifiersAnnotation(modifiersTree, onewayAn);
                }
                // parameters
                List<? extends VariableTree> params = method.getParameters();
                List<VariableTree> newParams = new ArrayList<>();
                if (params.size() > 0) {
                    final String webParamClazz = isJakartaEENameSpace ?
                            "jakarta.jws.WebParam" : "javax.jws.WebParam"; //NOI18N
                    TypeElement paramEl = workingCopy.getElements().getTypeElement(webParamClazz);
                    for (VariableTree param: params) {
                        String paramName = param.getName().toString();
                        AssignmentTree nameAttr = make.Assignment(make.Identifier("name"), make.Literal(paramName)); //NOI18N
                        AnnotationTree paramAn = make.Annotation(
                                make.QualIdent(paramEl),
                                Collections.<ExpressionTree>singletonList(nameAttr));
                        ModifiersTree paramModifierTree = make.addModifiersAnnotation(param.getModifiers(), paramAn);
                        newParams.add(make.Variable(paramModifierTree, param.getName(), param.getType(), null));
                    }
                }

                // method body
                List<ExpressionTree> arguments = new ArrayList<>();
                for (VariableElement ve : methodEl.getParameters()) {
                    arguments.add(make.Identifier(ve.getSimpleName()));
                }
                MethodInvocationTree inv = make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(make.Identifier("ejbRef"), methodName), //NOI18N
                        arguments);

                StatementTree statement = isVoid ? make.ExpressionStatement(inv) : make.Return(inv);

                BlockTree body = make.Block(Collections.singletonList(statement), false);

                MethodTree delegatingMethod = make.Method(
                        modifiersTree,
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        newParams,
                        method.getThrows(),
                        body,
                        null);
                modifiedClass = make.addClassMember(modifiedClass, delegatingMethod);
            }
        }
        return modifiedClass;
    }

    private String findUniqueOperationName(Set<String> existingNames, String operationName) {
        if (!existingNames.contains(operationName)) {
            return operationName;
        } else {
            int i = 1;
            String newName = operationName + "_1"; //NOI18N
            while (existingNames.contains(newName)) {
                newName = operationName + "_" + String.valueOf(++i); //NOI18N
            }
            return newName;
        }
    }
}
