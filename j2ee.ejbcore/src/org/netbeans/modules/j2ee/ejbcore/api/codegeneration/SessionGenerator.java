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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
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
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.ejbcore.action.BusinessMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session.TimerOptions;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;

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
    private final boolean isSimplified;
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
            String sessionType, boolean isSimplified, boolean hasBusinessInterface, boolean isXmlBased,
            TimerOptions timerOptions, boolean exposeTimer, boolean nonPersistentTimer) {
        return new SessionGenerator(wizardTargetName, pkg, hasRemote, hasLocal, sessionType, isSimplified,
                hasBusinessInterface, isXmlBased, timerOptions, exposeTimer, nonPersistentTimer, false);
    }

    protected SessionGenerator(String wizardTargetName, FileObject pkg, boolean hasRemote, boolean hasLocal,
            String sessionType, boolean isSimplified, boolean hasBusinessInterface, boolean isXmlBased,
            TimerOptions timerOptions, boolean exposeTimer, boolean nonPersistentTimer, boolean isTest) {
        this.pkg = pkg;
        this.remotePkg = pkg;
        this.hasRemote = hasRemote;
        this.hasLocal = hasLocal;
        this.sessionType = sessionType;
        this.isSimplified = isSimplified;
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
        if (isSimplified) {
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
