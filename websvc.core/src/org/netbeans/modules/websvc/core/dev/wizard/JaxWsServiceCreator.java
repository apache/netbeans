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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.core.dev.wizard;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.support.ServiceCreator;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.ProjectInfo;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.VariableTree;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.swing.SwingUtilities;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
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

    private ProjectInfo projectInfo;
    private WizardDescriptor wiz;
    private boolean addJaxWsLib;
    private int serviceType;
    private int projectType;

    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsServiceCreator(ProjectInfo projectInfo, WizardDescriptor wiz, 
            boolean addJaxWsLib) 
    {
        this.projectInfo = projectInfo;
        this.wiz = wiz;
        this.addJaxWsLib = addJaxWsLib;
    }

    public void createService() throws IOException {
        serviceType = ((Integer) wiz.getProperty(WizardProperties.WEB_SERVICE_TYPE)).
            intValue();
        projectType = projectInfo.getProjectType();

        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(JaxWsServiceCreator.class, 
                        "TXT_WebServiceGeneration")); //NOI18N
        handle.start(100);

        Runnable r = new Runnable() {

            public void run() {
                try {
                    generateWebService(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if (message != null) {
                        ErrorManager.getDefault().notify(
                                ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }

    public void createServiceFromWsdl() throws IOException {

        //initProjectInfo(project);

        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(JaxWsServiceCreator.class, 
                        "TXT_WebServiceGeneration")); //NOI18N

        Runnable r = new Runnable() {

            public void run() {
                try {
                    handle.start();
                    generateWsFromWsdl15(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if (message != null) {
                        ErrorManager.getDefault().notify(
                                ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                message, NotifyDescriptor.ERROR_MESSAGE);
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
    private void generateWebService(ProgressHandle handle) throws Exception {

        FileObject pkg = Templates.getTargetFolder(wiz);
        String wsName = Templates.getTargetName(wiz);
        Project p = projectInfo.getProject();

        if (serviceType == WizardProperties.FROM_SCRATCH) {
            JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(
                    p.getProjectDirectory());
            if (jaxWsSupport != null) {
                wsName = getUniqueJaxwsName(jaxWsSupport, wsName);
                handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, 
                        "MSG_GEN_WS"), 50); //NOI18N
                //add the JAXWS 2.0 library, if not already added
                if (addJaxWsLib) {
                    addJaxws21Library(p);
                }
                generateJaxWSImplFromTemplate(pkg, wsName, projectType);
                handle.finish();
            } else {
                 DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(JaxWsServiceCreator.class, 
                                "TXT_JaxWsNotSupported"),
                        NotifyDescriptor.ERROR_MESSAGE));
                 handle.finish();
            }
        } else if (serviceType == WizardProperties.ENCAPSULATE_SESSION_BEAN) {
            if (/*(projectType == JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project)) ||*/(ProjectUtil.isJavaEE5orHigher(projectInfo.getProject()) && (projectType == ProjectInfo.WEB_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE)) //NOI18N
                    ) {

                JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(
                        p.getProjectDirectory());
                if (jaxWsSupport != null) {
                    wsName = getUniqueJaxwsName(jaxWsSupport, wsName);
                    handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, 
                            "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
                    Node[] nodes = (Node[]) wiz.getProperty(
                            WizardProperties.DELEGATE_TO_SESSION_BEAN);
                    generateWebServiceFromEJB(wsName, pkg, projectInfo, nodes);
                    handle.finish();
                } else {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(JaxWsServiceCreator.class, 
                                "TXT_JaxWsNotSupported"),
                        NotifyDescriptor.ERROR_MESSAGE));
                    handle.finish();                   
                }
            }
        }
    }

    private FileObject generateJaxWSImplFromTemplate(FileObject pkg, String wsName, 
            int projectType) throws Exception 
    {
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);

        if ((Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN)) { //EJB Web Service
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("EjbWebService", "java"); //NOI18N
        }
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, wsName);
        FileObject createdFile = dobj.getPrimaryFile();
        createdFile.setAttribute("jax-ws-service", java.lang.Boolean.TRUE); // NOI18N
        dobj.setValid(false);
        dobj = DataObject.find(createdFile);
        final JaxWsModel jaxWsModel = projectInfo.getProject().getLookup().
            lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            
            ClassPath classPath = getClassPathForFile( projectInfo.getProject(), 
                    createdFile);
                if (classPath != null) {
                    String serviceImplPath = classPath.getResourceName(createdFile, 
                        '.', false);
                    jaxWsModel.addService(wsName, serviceImplPath);
                    ProjectManager.mutex().writeAccess(new Runnable() {

                    public void run() {
                        try {
                            jaxWsModel.write();
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                });
            }            
            JaxWsUtils.openFileInEditor(dobj);
            displayDuplicityWarning(createdFile);
        }

        return createdFile;
    }

    private String getUniqueJaxwsName(JAXWSSupport jaxWsSupport, String origName) {
        List webServices = jaxWsSupport.getServices();
        List<String> serviceNames = new ArrayList<String>(webServices.size());
        for (Object service : webServices) {
            serviceNames.add(((Service)service).getName());
        }
        return uniqueWSName(origName, serviceNames);
    }

    private String uniqueWSName(final String origName, List<String> names) {
        int uniquifier = 0;
        String truename = origName;
        while (names.contains(truename)) {
            truename = origName + String.valueOf(++uniquifier);
        }
        return truename;
    }

    private void addJaxws21Library(Project project) throws Exception {

        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(sgs[0].
                    getRootFolder(), ClassPath.COMPILE);
            FileObject wsimportFO = classPath.findResource(
                    "com/sun/tools/ws/ant/WsImport.class"); // NOI18N
            if (wsimportFO != null) {
                return;
            }

            Library jaxws21_ext = LibraryManager.getDefault().getLibrary("jaxws21"); //NOI18N
            if (jaxws21_ext != null) {
                try {
                    ProjectClassPathModifier.addLibraries(
                        new Library[] {jaxws21_ext}, sgs[0].getRootFolder(), 
                        ClassPath.COMPILE);
                } 
                catch (IOException e) {
                    throw new Exception("Unable to add JAXWS 21 Library. " + 
                        e.getMessage());
                }
            } 
            else {
                throw new Exception("Unable to add JAXWS 2.1 Library. " +
                    "ProjectClassPathExtender or library not found");
            }

            try {
                FileObject srcRoot = sgs[0].getRootFolder();
                WSUtils.addJaxWsApiEndorsed(project, srcRoot);
            } 
            catch (java.io.IOException ex) {
                Logger.getLogger(JaxWsServiceCreator.class.getName()).log(
                        Level.FINE, "Cannot add JAX-WS-ENDORSED classpath", ex);
            }
        }
    }

    private void generateWsFromWsdl15(final ProgressHandle handle) throws Exception {
        String wsdlFilePath = (String) wiz.getProperty(
                WizardProperties.WSDL_FILE_PATH);
        URL wsdlUrl = null;
        if (wsdlFilePath == null) {
            wsdlUrl = new URL((String) wiz.getProperty(WizardProperties.WSDL_URL));
        } else {
            File normalizedWsdlFilePath = FileUtil.normalizeFile(
                    new File(wsdlFilePath));
            //convert to URI first to take care of spaces
            wsdlUrl = normalizedWsdlFilePath.toURI().toURL();
        }
        final Project p = projectInfo.getProject();
        final URL wsdlURL = wsdlUrl;
        final WsdlService service = (WsdlService) wiz.getProperty(
                WizardProperties.WSDL_SERVICE);
        final Boolean useProvider = (Boolean) wiz.getProperty(
                WizardProperties.USE_PROVIDER);
        if (service == null) {
            FileObject targetFolder = Templates.getTargetFolder(wiz);
            String targetName = Templates.getTargetName(wiz);

            // create a fake implementation class to enable WS functionality (to enable WS node creation)
            if (targetFolder != null) {
                GenerationUtils.createClass(targetFolder, targetName, null);
            }

            WsdlServiceHandler handler = (WsdlServiceHandler) wiz.getProperty(
                    WizardProperties.WSDL_SERVICE_HANDLER);
            JaxWsUtils.generateJaxWsArtifacts(p, targetFolder, targetName, 
                    wsdlURL, handler.getServiceName(), handler.getPortName());
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(
                    WizardProperties.WSDL_MODELER);
            if (wsdlModeler != null && wsdlModeler.getCreationException() != null) {
                handle.finish();
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(JaxWsServiceCreator.class, 
                                "TXT_CannotGenerateArtifacts",
                                wsdlModeler.getCreationException().getLocalizedMessage()),
                                NotifyDescriptor.ERROR_MESSAGE));
            } 
            else {
                handle.finish();
            }
        } else {
            final WsdlPort port = (WsdlPort) wiz.getProperty(
                    WizardProperties.WSDL_PORT);
            final boolean isStatelessSB = (Boolean)wiz.getProperty(
                    WizardProperties.IS_STATELESS_BEAN);
            //String portJavaName = port.getJavaName();   
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(
                    WizardProperties.WSDL_MODELER);
            // don't set the packageName for modeler (use the default one generated from target Namespace)
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                public void modelCreated(WsdlModel model) {
                    if (model == null) {
                        handle.finish();
                        return;
                    }
                    WsdlService service1 = model.getServiceByName(service.getName());
                    WsdlPort port1 = service1.getPortByName(port.getName());

                    port1.setSOAPVersion(port.getSOAPVersion());
                    FileObject targetFolder = Templates.getTargetFolder(wiz);
                    String targetName = Templates.getTargetName(wiz);
                    try {
                        JaxWsUtils.generateJaxWsImplementationClass(p,
                                targetFolder,
                                targetName,
                                wsdlURL,
                                service1, port1, useProvider, isStatelessSB);
                        handle.finish();
                    } catch (Exception ex) {
                        handle.finish();
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,
                                ex);
                    }
                }
            });
        }
    }

    private void generateWebServiceFromEJB(String wsName, FileObject pkg, 
            ProjectInfo projectInfo, Node[] nodes) 
            throws IOException, ServiceAlreadyExistsExeption, PropertyVetoException 
    {

        if (nodes != null && nodes.length == 1) {

            EjbReference ejbRef = nodes[0].getLookup().lookup(EjbReference.class);
            if (ejbRef != null) {

                DataFolder df = DataFolder.findFolder(pkg);
                FileObject template = Templates.getTemplate(wiz);
                FileObject templateParent = template.getParent();
                if ((Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN)) { //EJB Web Service
                    template = templateParent.getFileObject("EjbWebServiceNoOp", 
                            "java"); //NOI18N
                } else {
                    template = templateParent.getFileObject("WebServiceNoOp", 
                            "java"); //NOI18N
                }
                DataObject dTemplate = DataObject.find(template);
                DataObject dobj = dTemplate.createFromTemplate(df, wsName);
                FileObject createdFile = dobj.getPrimaryFile();
                createdFile.setAttribute("jax-ws-service", java.lang.Boolean.TRUE);     // NOI18N
                dobj.setValid(false);
                dobj = DataObject.find(createdFile);

                ClassPath classPath = getClassPathForFile(projectInfo.getProject(), 
                        createdFile);
                if (classPath != null) {
                    String serviceImplPath = classPath.getResourceName(createdFile, 
                            '.', false);
                    generateDelegateMethods(createdFile, ejbRef);

                    final JaxWsModel jaxWsModel = projectInfo.getProject().
                        getLookup().lookup(JaxWsModel.class);
                    if (jaxWsModel != null) {
                        jaxWsModel.addService(wsName, serviceImplPath);
                        ProjectManager.mutex().writeAccess(new Runnable() {

                            public void run() {
                                try {
                                    jaxWsModel.write();
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ex);
                                }
                            }
                        });
                    }
                }
                JaxWsUtils.openFileInEditor(dobj);
                displayDuplicityWarning(createdFile);
            }
        }
    }
    
    private void displayDuplicityWarning(final FileObject createdFile) {
        final String serviceName = createdFile.getName()+"Service"; //NOI18N
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Service serv = JaxWsUtils.findServiceForServiceName(
                        createdFile, serviceName);
                if (serv != null) {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(JaxWsServiceCreator.class,
                                    "MSG_ServiceNameExists", serviceName, 
                                        serv.getImplementationClass()), 
                            NotifyDescriptor.WARNING_MESSAGE));
                }
            }

        });        
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void generateDelegateMethods(final FileObject targetFo, 
            EjbReference ref) throws IOException 
    {
        final JavaSource targetSource = JavaSource.forFileObject(targetFo);
        final DelegateMethodTask modificationTask =new DelegateMethodTask( ref );
        ModificationResult result = targetSource.runModificationTask(
                modificationTask);
        boolean onClassPath = modificationTask.onClassPath();
        String interfaceClass = modificationTask.getInterfaceClass();
        
        if ( modificationTask.isIncomplete() && 
                org.netbeans.api.java.source.SourceUtils.isScanInProgress())
        {
            final Runnable runnable = new Runnable() {
                
                @Override
                public void run() {
                    try {
                        targetSource.runModificationTask(modificationTask).commit();
                        boolean onClassPath = modificationTask.onClassPath();
                        String interfaceClass = modificationTask.getInterfaceClass();
                        notifyMessage(targetFo, onClassPath, interfaceClass);
                    }
                    catch ( IOException e ){
                        Logger.getLogger( JaxWsServiceCreator.class.getName()).
                            log( Level.WARNING, null , e);
                    }
                }
            };
            final Runnable outOfAwt = new Runnable() {
                
                @Override
                public void run() {
                    RequestProcessor.getDefault().post( runnable );
                }
            };
            if ( SwingUtilities.isEventDispatchThread() ){
                ScanDialog.runWhenScanFinished(outOfAwt, NbBundle.getMessage( 
                        JaxWsServiceCreator.class, "LBL_GenerateWebService"));  // NOI18N
            }
            else {
                SwingUtilities.invokeLater( new Runnable() {
                    
                    @Override
                    public void run() {
                        ScanDialog.runWhenScanFinished(outOfAwt, 
                                NbBundle.getMessage( JaxWsServiceCreator.class, 
                                        "LBL_GenerateWebService"));  // NOI18N                    
                    }
                });
            }
        }
        else {
            result.commit();
            notifyMessage(targetFo, onClassPath, interfaceClass);
        }
    }

    private void notifyMessage( final FileObject targetFo, boolean onClassPath,
            final String interfaceClass )
    {
        if (!onClassPath) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(NbBundle.getMessage(
                                    JaxWsServiceCreator.class, 
                                    "MSG_EJB_NOT_ON_CLASSPATH", 
                                    interfaceClass, targetFo.getName()),
                                        NotifyDescriptor.WARNING_MESSAGE));
                }
            });
        }
    }

    private ClassPath getClassPathForFile(Project project, FileObject file) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).
            getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup srcGroup: srcGroups) {
            FileObject srcRoot = srcGroup.getRootFolder();
            if (FileUtil.isParentOf(srcRoot, file)) {
                return ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
            }
        }
        return null;
    }

    private static class DelegateMethodTask implements CancellableTask<WorkingCopy> {
        
        DelegateMethodTask(EjbReference ref) {
            this.ref = ref;
        }

        @Override
        public void run(WorkingCopy workingCopy) throws IOException {
            workingCopy.toPhase(Phase.RESOLVED);

            TreeMaker make = workingCopy.getTreeMaker();

            TypeElement typeElement = SourceUtils.getPublicTopLevelElement(
                    workingCopy);
            if (typeElement != null) {
                VariableTree ejbRefInjection = null;
                interfaceClass = ref.getLocal();
                if (interfaceClass == null) {
                    interfaceClass = ref.getRemote();
                }
                if (interfaceClass == null) {
                    interfaceClass = ref.getEjbClass();
                }

                ejbRefInjection = generateEjbInjection(workingCopy, make);
                if ( isIncomplete ){
                    return;
                }

                if (ejbRefInjection != null) {
                    String comment1 = "Add business logic below. (Right-click in editor and choose"; //NOI18N
                    String comment2 = "\"Insert Code > Add Web Service Operation\")"; //NOI18N
                    make.addComment(ejbRefInjection, Comment.create(
                            Comment.Style.LINE, 0, 0, 4, comment1), false);
                    make.addComment(ejbRefInjection, Comment.create(
                            Comment.Style.LINE, 0, 0, 4, comment2), false);

                    ClassTree javaClass = workingCopy.getTrees().getTree(
                            typeElement);
                    ClassTree modifiedClass = make.insertClassMember(
                            javaClass, 0, ejbRefInjection);

                    if (onClassPath) {
                        TypeElement beanInterface = workingCopy.getElements().
                            getTypeElement(interfaceClass);
                        modifiedClass = generateMethods(workingCopy, make, 
                                typeElement, modifiedClass, beanInterface);
                        if ( isIncomplete ){
                            return;
                        }
                    }

                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }
        }
        @Override
        public void cancel() {
        }
        
        public String getInterfaceClass(){
            return interfaceClass;
        }
        
        public boolean onClassPath(){
            return onClassPath;
        }
        
        public boolean isIncomplete(){
            return isIncomplete;
        }
        
        private VariableTree generateEjbInjection(WorkingCopy workingCopy, 
                TreeMaker make) 
        {

            TypeElement ejbAnElement = workingCopy.getElements().getTypeElement(
                    "javax.ejb.EJB"); //NOI18N
            if ( ejbAnElement == null ){
                isIncomplete = true;
                return null ;
            }
            TypeElement interfaceElement = workingCopy.getElements().
                getTypeElement(interfaceClass); //NOI18N
            if ( interfaceElement == null ){
                isIncomplete = true;
                return null ;
            }

            AnnotationTree ejbAnnotation = make.Annotation(
                    make.QualIdent(ejbAnElement),
                    Collections.<ExpressionTree>emptyList());
            // create method modifier: public and no annotation
            ModifiersTree methodModifiers = make.Modifiers(
                    Collections.<Modifier>singleton(Modifier.PRIVATE),
                    Collections.<AnnotationTree>singletonList(ejbAnnotation));

            onClassPath = interfaceElement != null;

            return make.Variable(
                    methodModifiers,
                    "ejbRef", //NOI18N
                    onClassPath ? make.Type(interfaceElement.asType()) : 
                        make.Identifier(interfaceClass),
                    null);
        }
        
        private ClassTree generateMethods(WorkingCopy workingCopy,
                TreeMaker make,
                TypeElement classElement,
                ClassTree modifiedClass,
                TypeElement beanInterface) throws IOException 
        {

            GeneratorUtilities utils = GeneratorUtilities.get(workingCopy);

            TypeElement webMethodEl = workingCopy.getElements().getTypeElement(
                    "javax.jws.WebMethod"); //NOI18N
            if (webMethodEl == null) {
                isIncomplete = true;
                return modifiedClass;
            }

            // found if bean interface extends another class
            TypeMirror superclass = beanInterface.getSuperclass();
            boolean hasSuperclass = (TypeKind.NONE != superclass.getKind() && 
                    !isObjectClass((DeclaredType)superclass)); //NOI18N
            List<? extends Element> allBeanInterfaceElements = null;
            if (hasSuperclass) {
                allBeanInterfaceElements = workingCopy.getElements().
                    getAllMembers(beanInterface);
            } else {
                allBeanInterfaceElements = beanInterface.getEnclosedElements();
            }

            Set<String> operationNames = new HashSet<String>();
            for (Element el : allBeanInterfaceElements) {
                if (el.getKind() == ElementKind.METHOD && 
                        el.getModifiers().contains(Modifier.PUBLIC)) 
                {
                    ExecutableElement methodEl = (ExecutableElement) el;
                    if (hasSuperclass) {
                        Element classEl = el.getEnclosingElement();
                        if (classEl.getKind() == ElementKind.CLASS && 
                                isObjectClass((TypeElement)classEl)) { 
                            // don't consider Object methods
                            continue;
                        }
                    }

                    MethodTree method = utils.createMethod(
                            (DeclaredType)beanInterface.asType(), methodEl);

                    Name methodName = methodEl.getSimpleName();
                    boolean isVoid = workingCopy.getTypes().
                        getNoType(TypeKind.VOID) == methodEl.getReturnType();

                    String operationName = findUniqueOperationName(
                            operationNames, methodName.toString());
                    operationNames.add(operationName);

                    // generate @WebMethod annotation
                    AssignmentTree opName = make.Assignment(
                            make.Identifier("operationName"),           //NOI18N
                                make.Literal(operationName)); 

                    AnnotationTree webMethodAn = make.Annotation(
                            make.QualIdent(webMethodEl),
                            Collections.<ExpressionTree>singletonList(opName));
                    ModifiersTree modifiersTree = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>singletonList(webMethodAn));

                    // generate @RequestWrapper and @RequestResponse annotations
                    if (!methodName.contentEquals(operationName)) {
                        TypeElement requestWrapperEl = workingCopy.getElements().
                            getTypeElement("javax.xml.ws.RequestWrapper"); //NOI18N
                        TypeElement responseWrapperEl = workingCopy.getElements().
                            getTypeElement("javax.xml.ws.ResponseWrapper"); //NOI18N
                        if ( requestWrapperEl == null || responseWrapperEl == null ){
                            isIncomplete =true;
                            return modifiedClass;
                        }
                        AssignmentTree className = make.Assignment(
                                make.Identifier("className"), //NOI18N
                                    make.Literal(operationName)); 
                        AnnotationTree requestWrapperAn = make.Annotation(
                                make.QualIdent(requestWrapperEl),
                                Collections.<ExpressionTree>singletonList(className));
                        modifiersTree = make.addModifiersAnnotation(
                                modifiersTree, requestWrapperAn);

                        if (!isVoid) { // only if not void                     
                            className = make.Assignment(
                                        make.Identifier("className"), 
                                            make.Literal(operationName + "Response")); //NOI18N
                            AnnotationTree responseWrapperAn = make.Annotation(
                                    make.QualIdent(responseWrapperEl),
                                    Collections.<ExpressionTree>singletonList(
                                            className));
                            modifiersTree = make.addModifiersAnnotation(
                                    modifiersTree, responseWrapperAn);
                        }
                    }

                    // generate @Oneway annotation
                    if (isVoid && method.getThrows().isEmpty()) {
                        TypeElement onewayEl = workingCopy.getElements().
                            getTypeElement("javax.jws.Oneway"); //NOI18N
                        if ( onewayEl == null ){
                            isIncomplete = true;
                            return modifiedClass;
                        }
                        AnnotationTree onewayAn = make.Annotation(
                                make.QualIdent(onewayEl),
                                Collections.<ExpressionTree>emptyList());
                        modifiersTree = make.addModifiersAnnotation(
                                modifiersTree, onewayAn);
                    }
                    // parameters
                    List<? extends VariableTree> params = method.getParameters();
                    List<VariableTree> newParams = new ArrayList<VariableTree>();
                    if (params.size() > 0) {
                        TypeElement paramEl = workingCopy.getElements().
                            getTypeElement("javax.jws.WebParam"); //NOI18N
                        if ( paramEl == null ){
                            isIncomplete = true;
                            return modifiedClass;
                        }
                        for (VariableTree param: params) {
                            String paramName = param.getName().toString();
                            AssignmentTree nameAttr = make.Assignment(
                                    make.Identifier("name"),    //NOI18N
                                        make.Literal(paramName)); 
                            AnnotationTree paramAn = make.Annotation(
                                    make.QualIdent(paramEl),
                                    Collections.
                                        <ExpressionTree>singletonList(nameAttr));
                            ModifiersTree paramModifierTree = 
                                make.addModifiersAnnotation(param.getModifiers(), paramAn);
                            newParams.add(make.Variable(paramModifierTree, 
                                    param.getName(), param.getType(), null));
                        }
                    }
                    
                    // method body
                    List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                    for (VariableElement ve : methodEl.getParameters()) {
                        arguments.add(make.Identifier(ve.getSimpleName()));
                    }
                    MethodInvocationTree inv = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            make.MemberSelect(make.Identifier("ejbRef"), methodName), //NOI18N
                            arguments);

                    StatementTree statement = isVoid ? 
                            make.ExpressionStatement(inv) : make.Return(inv);

                    BlockTree body = make.Block(Collections.
                            singletonList(statement), false);

                    MethodTree delegatingMethod = make.Method(
                            modifiersTree,
                            method.getName(),
                            method.getReturnType(),
                            method.getTypeParameters(),
                            newParams,
                            method.getThrows(),
                            body,
                            null);
                    modifiedClass = make.addClassMember(modifiedClass, 
                            delegatingMethod);
                }
            }
            return modifiedClass;
        }
        
        private boolean isObjectClass(DeclaredType classMirror) {
            return isObjectClass((TypeElement)classMirror.asElement());
        }

        private boolean isObjectClass(TypeElement classElement) {
            if (TypeKind.NONE == classElement.getSuperclass().getKind()) {
                return true;
            } else {
                return false;
            }
        }
        
        private String findUniqueOperationName(Set<String> existingNames, 
                String operationName) 
        {
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
        
        private boolean onClassPath;
        private String interfaceClass;
        private boolean isIncomplete;
        private EjbReference ref;
    }
}
