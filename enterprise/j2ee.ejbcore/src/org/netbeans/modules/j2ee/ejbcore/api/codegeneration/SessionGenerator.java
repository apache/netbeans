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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session.TimerOptions;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Generator of Session EJBs for EJB 2.1 and 3.0
 *
 * @author Martin Adamek
 */
public final class SessionGenerator {

    public static final String EJB21_EJBCLASS = "Templates/J2EE/EJB21/SessionEjbClass.java"; // NOI18N
    public static final String EJB21_LOCAL = "Templates/J2EE/EJB21/SessionLocal.java"; // NOI18N
    public static final String EJB21_LOCALHOME = "Templates/J2EE/EJB21/SessionLocalHome.java"; // NOI18N
    public static final String EJB21_REMOTE = "Templates/J2EE/EJB21/SessionRemote.java"; // NOI18N
    public static final String EJB21_REMOTEHOME = "Templates/J2EE/EJB21/SessionRemoteHome.java"; // NOI18N

    public static final String EJB30_STATELESS_EJBCLASS = "Templates/J2EE/EJB30/StatelessEjbClass.java"; // NOI18N
    public static final String EJB30_STATEFUL_EJBCLASS = "Templates/J2EE/EJB30/StatefulEjbClass.java"; // NOI18N
    public static final String EJB30_LOCAL = "Templates/J2EE/EJB30/SessionLocal.java"; // NOI18N
    public static final String EJB30_REMOTE = "Templates/J2EE/EJB30/SessionRemote.java"; // NOI18N

    public static final String EJB40_STATELESS_EJBCLASS = "Templates/J2EE/EJB40/StatelessEjbClass.java"; // NOI18N
    public static final String EJB40_STATEFUL_EJBCLASS = "Templates/J2EE/EJB40/StatefulEjbClass.java"; // NOI18N
    public static final String EJB40_LOCAL = "Templates/J2EE/EJB40/SessionLocal.java"; // NOI18N
    public static final String EJB40_REMOTE = "Templates/J2EE/EJB40/SessionRemote.java"; // NOI18N
    public static final String EJB40_SINGLETON_EJBCLASS = "Templates/J2EE/EJB40/SingletonEjbClass.java"; // NOI18N

    public static final String EJB31_SINGLETON_EJBCLASS = "Templates/J2EE/EJB31/SingletonEjbClass.java"; // NOI18N

    public static final String ANNOTATION_LOCAL_BEAN = "javax.ejb.LocalBean";

    public static final String TEMPLATE_PROPERTY_INTERFACES = "interfaces"; //NOI18N
    public static final String TEMPLATE_PROPERTY_LOCAL_BEAN = "annotationLocalBean"; //NOI18N

    // informations collected in wizard
    private final FileObject pkg;
    private FileObject remotePkg;
    private final boolean hasRemote;
    private final boolean hasLocal;
    private final String sessionType;
    private final Profile enterpriseProfile;
//    private final boolean hasBusinessInterface;
    private final boolean isXmlBased;

    // EJB naming options
    private final EJBNameOptions ejbNameOptions;
    private final String ejbName;
    private final String ejbClassName;
    private final String remoteName;
    private final String remoteHomeName;
    private final String localName;
    private final String localHomeName;
    private final String displayName;

    private final String packageName;
    private final String packageNameWithDot;

    private final Map<String, Object> templateParameters;

    public static SessionGenerator create(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal,
            String sessionType, Profile enterpriseProfile, boolean hasBusinessInterface, boolean isXmlBased,
            TimerOptions timerOptions, boolean exposeTimer, boolean nonPersistentTimer) {
        return new SessionGenerator(wizardTargetName, pkg, hasRemote, hasLocal, sessionType, enterpriseProfile,
                hasBusinessInterface, isXmlBased, timerOptions, exposeTimer, nonPersistentTimer, false);
    }

    protected SessionGenerator(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal,
            String sessionType, Profile enterpriseProfile, boolean hasBusinessInterface, boolean isXmlBased,
            TimerOptions timerOptions, boolean exposeTimer, boolean nonPersistentTimer, boolean isTest) {
        this.pkg = pkg;
        this.remotePkg = pkg;
        this.hasRemote = hasRemote;
        this.hasLocal = hasLocal;
        this.sessionType = sessionType;
        this.enterpriseProfile = enterpriseProfile;
//        this.hasBusinessInterface = hasBusinessInterface;
        this.isXmlBased = isXmlBased;
        this.ejbNameOptions = new EJBNameOptions();
        this.ejbName = ejbNameOptions.getSessionEjbNamePrefix() + wizardTargetName + ejbNameOptions.getSessionEjbNameSuffix();
        this.ejbClassName = ejbNameOptions.getSessionEjbClassPrefix() + wizardTargetName + ejbNameOptions.getSessionEjbClassSuffix();
        this.remoteName = ejbNameOptions.getSessionRemotePrefix() + wizardTargetName + ejbNameOptions.getSessionRemoteSuffix();
        this.remoteHomeName = ejbNameOptions.getSessionRemoteHomePrefix() + wizardTargetName + ejbNameOptions.getSessionRemoteHomeSuffix();
        this.localName = ejbNameOptions.getSessionLocalPrefix() + wizardTargetName + ejbNameOptions.getSessionLocalSuffix();
        this.localHomeName = ejbNameOptions.getSessionLocalHomePrefix() + wizardTargetName + ejbNameOptions.getSessionLocalHomeSuffix();
        this.displayName = ejbNameOptions.getSessionDisplayNamePrefix() + wizardTargetName + ejbNameOptions.getSessionDisplayNameSuffix();
        this.packageName = EjbGenerationUtil.getSelectedPackageName(pkg);
        this.packageNameWithDot = packageName + ".";
        this.templateParameters = new HashMap<String, Object>();
        // fill all possible template parameters
        this.templateParameters.put("package", packageName);
        this.templateParameters.put("localInterface", packageNameWithDot + localName);
        this.templateParameters.put("remoteInterface", packageNameWithDot + remoteName);
        // set timer options if available
        if (timerOptions != null) {
            this.templateParameters.put("timerExist", true); //NOI18N
            this.templateParameters.put("timerString", getScheduleAnnotationValue(timerOptions, nonPersistentTimer)); //NOI18N
            this.templateParameters.put("exposeTimer", exposeTimer && (hasLocal || hasRemote)); //NOI18N
        } else {
            this.templateParameters.put("timerExist", false); //NOI18N
            this.templateParameters.put("exposeTimer", false); //NOI18N
        }
        if (isTest) {
            // set date, time and user to values used in goldenfiles
            this.templateParameters.put("date", "{date}");
            this.templateParameters.put("time", "{time}");
            this.templateParameters.put("user", "{user}");
        }
    }

    public static String getScheduleAnnotationValue(TimerOptions timerOptions, boolean nonPersistentTimer) {
        String timerValue = timerOptions.getAnnotationValue();
        return nonPersistentTimer ? timerValue + ", persistent = false" : timerValue; //NOI18N
    }

    public void initRemoteInterfacePackage(Project projectForRemoteInterface, String remoteInterfacePackageName, FileObject ejbSourcePackage) throws IOException {
        remotePkg = SessionGenerator.createRemoteInterfacePackage(projectForRemoteInterface, remoteInterfacePackageName, ejbSourcePackage);
    }

    public static FileObject createRemoteInterfacePackage(Project projectForRemoteInterface, String remoteInterfacePackageName, FileObject ejbSourcePackage) throws IOException {
        assert ProjectUtils.getSources(projectForRemoteInterface).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA).length > 0;
        FileObject root = ProjectUtils.getSources(projectForRemoteInterface).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)[0].getRootFolder();
        FileObject remotePkg = FileUtil.createFolder(root, remoteInterfacePackageName.replace('.', '/'));
        // add project where remote interface is defined to classpath of project where EJB is going to be implemented:
        ProjectClassPathModifier.addProjects(new Project[]{projectForRemoteInterface}, ejbSourcePackage, ClassPath.COMPILE);
        // make sure project where remote interfrace is going to be defined has javax.ejb API available:
        assert LibraryManager.getDefault().getLibrary("javaee-api-6.0") != null;
        if (ClassPath.getClassPath(remotePkg, ClassPath.COMPILE).findResource("javax/ejb") == null) {
            try {
                // first try JavaClassPathConstants.COMPILE_ONLY - if remotePkg represents
                // Maven project then it will work; J2SE project on the other hand will fail
                // and simple ClassPath.COMPILE should be used instead:
                ProjectClassPathModifier.addLibraries(new Library[]{LibraryManager.getDefault().getLibrary("javaee-api-6.0")}, remotePkg, JavaClassPathConstants.COMPILE_ONLY);
            } catch (UnsupportedOperationException e) {
                ProjectClassPathModifier.addLibraries(new Library[]{LibraryManager.getDefault().getLibrary("javaee-api-6.0")}, remotePkg, ClassPath.COMPILE);
            }
        }
        return remotePkg;
    }

    public FileObject generate() throws IOException {
        FileObject resultFileObject = null;
        if (enterpriseProfile.isAtLeast(Profile.JAKARTA_EE_9_WEB)) {
            resultFileObject = generateEJB40Classes();

            //put these lines in a common function at the appropriate place after EA1
            //something like public EjbJar getEjbJar()
            //This method will be used whereever we construct/get DD object graph to ensure
            //corresponding config listners attached to it.
            Project project = FileOwnerQuery.getOwner(pkg);
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
        } else if (enterpriseProfile.isAtLeast(Profile.JAVA_EE_5)) {
            resultFileObject = generateEJB30Classes();

            //put these lines in a common function at the appropriate place after EA1
            //something like public EjbJar getEjbJar()
            //This method will be used whereever we construct/get DD object graph to ensure
            //corresponding config listners attached to it.
            Project project = FileOwnerQuery.getOwner(pkg);
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();

            if (isXmlBased) {
                generateEJB30Xml();
            }
        } else {
            resultFileObject = generateEJB21Classes();

            //put these lines in a common function at the appropriate place after EA1
            //something like public EjbJar getEjbJar()
            //This method will be used whereever we construct/get DD object graph to ensure
            //corresponding config listners attached to it.
            Project project = FileOwnerQuery.getOwner(pkg);
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();

            if (isXmlBased) {
                generateEJB21Xml();
            }
        }
        return resultFileObject;
    }

    private FileObject generateEJB21Classes() throws IOException {
        FileObject ejbClassFO = GenerationUtils.createClass(EJB21_EJBCLASS, pkg, ejbClassName, null, templateParameters);
        if (hasRemote) {
            GenerationUtils.createClass(EJB21_REMOTE,  remotePkg, remoteName, null, templateParameters);
            GenerationUtils.createClass(EJB21_REMOTEHOME, remotePkg, remoteHomeName, null, templateParameters);
        }
        if (hasLocal) {
            GenerationUtils.createClass(EJB21_LOCAL, pkg, localName, null, templateParameters);
            GenerationUtils.createClass(EJB21_LOCALHOME, pkg, localHomeName, null, templateParameters);
        }
        return ejbClassFO;
    }

    private FileObject generateEJB30Classes() throws IOException {
        String ejbClassTemplateName = "";
        if (sessionType.equals(Session.SESSION_TYPE_STATELESS)){
            ejbClassTemplateName = EJB30_STATELESS_EJBCLASS;
        } else if (sessionType.equals(Session.SESSION_TYPE_STATEFUL)){
            ejbClassTemplateName = EJB30_STATEFUL_EJBCLASS;
        } else if (sessionType.equals(Session.SESSION_TYPE_SINGLETON)){
            ejbClassTemplateName = EJB31_SINGLETON_EJBCLASS;
        } else{
            assert false;
        }

        if (hasLocal && hasRemote){
            this.templateParameters.put(TEMPLATE_PROPERTY_INTERFACES, remoteName + ", " + localName); //NOI18N
        } else if (hasLocal){
            this.templateParameters.put(TEMPLATE_PROPERTY_INTERFACES, localName);
        } else if (hasRemote){
            this.templateParameters.put(TEMPLATE_PROPERTY_INTERFACES, remoteName);
        } else {
            Project project = FileOwnerQuery.getOwner(pkg);
            J2eeProjectCapabilities projectCap = J2eeProjectCapabilities.forProject(project);
            if (projectCap != null && projectCap.isEjb31Supported()){
                this.templateParameters.put(TEMPLATE_PROPERTY_LOCAL_BEAN, Boolean.TRUE.toString());
            }
        }

        final FileObject ejbClassFO = GenerationUtils.createClass(ejbClassTemplateName,  pkg, ejbClassName, null, templateParameters);
        if (hasRemote) {
            GenerationUtils.createClass(EJB30_REMOTE, remotePkg, remoteName, null, templateParameters);
        }
        if (hasLocal) {
            GenerationUtils.createClass(EJB30_LOCAL, pkg, localName, null, templateParameters);
        }

        return ejbClassFO;
    }

    private FileObject generateEJB40Classes() throws IOException {
        String ejbClassTemplateName = "";
        if (sessionType.equals(Session.SESSION_TYPE_STATELESS)){
            ejbClassTemplateName = EJB40_STATELESS_EJBCLASS;
        } else if (sessionType.equals(Session.SESSION_TYPE_STATEFUL)){
            ejbClassTemplateName = EJB40_STATEFUL_EJBCLASS;
        } else if (sessionType.equals(Session.SESSION_TYPE_SINGLETON)){
            ejbClassTemplateName = EJB40_SINGLETON_EJBCLASS;
        } else{
            assert false;
        }

        if (hasLocal && hasRemote){
            this.templateParameters.put(TEMPLATE_PROPERTY_INTERFACES, remoteName + ", " + localName); //NOI18N
        } else if (hasLocal){
            this.templateParameters.put(TEMPLATE_PROPERTY_INTERFACES, localName);
        } else if (hasRemote){
            this.templateParameters.put(TEMPLATE_PROPERTY_INTERFACES, remoteName);
        } else {
            this.templateParameters.put(TEMPLATE_PROPERTY_LOCAL_BEAN, Boolean.TRUE.toString());
        }

        final FileObject ejbClassFO = GenerationUtils.createClass(ejbClassTemplateName,  pkg, ejbClassName, null, templateParameters);
        if (hasRemote) {
            GenerationUtils.createClass(EJB40_REMOTE, remotePkg, remoteName, null, templateParameters);
        }
        if (hasLocal) {
            GenerationUtils.createClass(EJB40_LOCAL, pkg, localName, null, templateParameters);
        }

        return ejbClassFO;
    }

    private void generateEJB21Xml() throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
        FileObject ddFO = ejbModule.getDeploymentDescriptor();
        if (ddFO == null && ejbModule.getMetaInf() != null){
            String resource = "org-netbeans-modules-j2ee-ejbjarproject/ejb-jar-2.1.xml";    //NOI18N
            ddFO = FileUtil.copyFile(FileUtil.getConfigFile(resource), ejbModule.getMetaInf(), "ejb-jar"); //NOI18N
        }
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFO); // EJB 2.1
        if (ejbJar == null) {
            String fileName = ddFO == null ? null : FileUtil.getFileDisplayName(ddFO);
            Logger.getLogger(SessionGenerator.class.getName()).warning("EjbJar not found for " + fileName); //NOI18N
            return;
        }
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session session = null;
        if (beans == null) {
            beans  = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        session = beans.newSession();
        session.setEjbName(ejbName);
        session.setDisplayName(displayName);
        session.setEjbClass(packageNameWithDot + ejbClassName);

        if (hasRemote) {
            session.setRemote(packageNameWithDot + remoteName);
            session.setHome(packageNameWithDot + remoteHomeName);
        }
        if (hasLocal) {
            session.setLocal(packageNameWithDot + localName);
            session.setLocalHome(packageNameWithDot + localHomeName);
        }

        session.setSessionType(sessionType);
        session.setTransactionType("Container"); // NOI18N
        beans.addSession(session);
        // add transaction requirements
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor == null) {
            assemblyDescriptor = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        }
        ContainerTransaction containerTransaction = assemblyDescriptor.newContainerTransaction();
        containerTransaction.setTransAttribute("Required"); //NOI18N
        org.netbeans.modules.j2ee.dd.api.ejb.Method method = containerTransaction.newMethod();
        method.setEjbName(ejbName);
        method.setMethodName("*"); //NOI18N
        containerTransaction.addMethod(method);
        assemblyDescriptor.addContainerTransaction(containerTransaction);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
    }

    private void generateEJB30Xml() throws IOException {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

//    TODO - could be rewrite as an insert action
//    private void generateTimerMethodForBean(final FileObject bean, String methodName, TimerOptions timerOptions) {
//        MethodModel.Annotation annotation = MethodModel.Annotation.create(
//                "javax.ejb.Schedule", timerOptions.getTimerOptionsAsMap()); // NOI18N
//        final MethodModel method = MethodModel.create(
//                methodName,
//                "void", // NOI18N
//                "System.out.println(\"Timer event: \" + new java.util.Date());", // NOI18N
//                Collections.<MethodModel.Variable>emptyList(),
//                Collections.<String>emptyList(),
//                Collections.<Modifier>emptySet(),
//                Collections.singletonList(annotation)
//                );
//
//        final BusinessMethodGenerator generator = BusinessMethodGenerator.create(packageNameWithDot + ejbClassName, bean);
//        RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                try {
//                    generator.generate(method, hasLocal, hasRemote);
//
//                    // save the document after adding timer method
//                    EditorCookie cookie = DataObject.find(bean).getLookup().lookup(EditorCookie.class);
//                    if (cookie != null) {
//                        cookie.saveDocument();
//                    }
//                } catch (DataObjectNotFoundException ex) {
//                   Logger.getLogger(SessionGenerator.class.getName()).log(Level.INFO, null, ex);
//                } catch (IOException ioe) {
//                    Logger.getLogger(SessionGenerator.class.getName()).log(Level.INFO, null, ioe);
//                }
//            }
//        });
//    }

      //TODO: RETOUCHE WS
//    /**
//     * Special case for generating a Session implementation bean for web services
//     */
//    public String generateWebServiceImplBean(String ejbName, FileObject pkg, Project project, String delegateData) throws java.io.IOException {
//        String pkgName = EjbGenerationUtil.getSelectedPackageName(pkg, project);
//        Bean b = genUtil.getDefaultBean();
//        b.setCommentDataEjbName(ejbName);
//        b.setClassname(true);
//        b.setClassnameName(EjbGenerationUtil.getBeanClassName(ejbName)); //NOI18N
//        b.setDelegateData(delegateData);
//        if (pkgName!=null) {
//            b.setClassnamePackage(pkgName);
//        }
//
//        // generate bean class
//        EjbJar ejbModule = EjbJar.getEjbJar(pkg);
//        boolean simplified = ejbModule.getJ2eePlatformVersion().equals(J2eeModule.JAVA_EE_5);
//        return null;//genUtil.generateBeanClass(simplified ? SESSION_TEMPLATE_WS_JAVAEE5 : SESSION_TEMPLATE, b, pkgName, pkg);
//    }

}
