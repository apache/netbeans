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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.naming.NamingException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference.EjbRefIType;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.ServiceLocatorStrategy;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class CallEjbGenerator {

    private final EjbReference ejbReference;
    private final String ejbReferenceName;
    private final boolean isDefaultRefName;
    private final boolean isSimplified;
    private final boolean isSession;
    private String ejbName;

    private CallEjbGenerator(final EjbReference ejbReference, String ejbReferenceName, boolean isDefaultRefName) {
        
        this.ejbReference = ejbReference;
        Object[] result = new Object[]{Boolean.FALSE, null, null};
        try {
            MetadataModel<EjbJarMetadata> metadataModel = ejbReference.getEjbModule().getMetadataModel();
            result = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Object[]>() {
                public Object[] run(EjbJarMetadata metadata) throws Exception {
                    Ejb ejb = metadata.findByEjbClass(ejbReference.getEjbClass());
                    return new Object[]{ejb instanceof Session,
                                        metadata.getRoot().getVersion(),
                                        ejb != null ? ejb.getEjbName() : null};
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        this.isDefaultRefName = isDefaultRefName;
        this.isSimplified = result[1] == null ? true : (((BigDecimal)result[1]).doubleValue() > 2.1);
        this.isSession = (Boolean)result[0];
        this.ejbName = (String)result[2];
        this.ejbReferenceName = ejbReferenceName != null ? ejbReferenceName : ejbName;
    }
    
    /**
     * @param ejbReferenceName reference name specified by user in dialog; if null, ejb-name is used
     */
    public static CallEjbGenerator create(EjbReference ejbReference, String ejbReferenceName, boolean isDefaultRefName) {
        return new CallEjbGenerator(ejbReference, ejbReferenceName, isDefaultRefName);
    }
    
    public ElementHandle<? extends Element> addReference(FileObject referencingFO, String referencingClassName, FileObject referencedFO, String referencedClassName, 
            String serviceLocator, EjbReference.EjbRefIType refIType, boolean throwExceptions, Project nodeProject) throws IOException {
        
        ElementHandle<? extends Element> result = null;
        
        // find the project containing the source file
        Project enterpriseProject = FileOwnerQuery.getOwner(referencingFO);
        EnterpriseReferenceContainer erc = enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);

        boolean enterpriseProjectIsJavaEE5 = ProjectUtil.isJavaEE5orHigher(enterpriseProject);
        boolean nodeProjectIsJavaEE5 = ProjectUtil.isJavaEE5orHigher(nodeProject);

        //#157918
        ContainerClassPathModifier ccpm = enterpriseProject.getLookup().lookup(ContainerClassPathModifier.class);
        if (ccpm != null) {
            ccpm.extendClasspath(referencingFO, new String[] {
                ContainerClassPathModifier.API_EJB,
                ContainerClassPathModifier.API_J2EE //is that all that is needed?
            });
        }

        if (enterpriseProjectIsJavaEE5 && InjectionTargetQuery.isInjectionTarget(referencingFO, referencingClassName)) {
            addProjectToClassPath(enterpriseProject, ejbReference, referencingFO, refIType);
        } else if (nodeProjectIsJavaEE5 == enterpriseProjectIsJavaEE5){ // see #75876
            switch(refIType){
                case REMOTE: {
                    erc.addEjbReference(ejbReference, refIType, ejbReferenceName, referencingFO, referencingClassName);
                    break;
                }
                case NO_INTERFACE:
                case LOCAL:{
                    erc.addEjbLocalReference(ejbReference, refIType, ejbReferenceName, referencingFO, referencingClassName);
                    break;
                }
            }
        }
        if (serviceLocator == null) {
            boolean isEE6 = J2eeProjectCapabilities.forProject(enterpriseProject).isEjb31LiteSupported();
            result = generateReferenceCode(referencingFO, referencingClassName, refIType, throwExceptions, !nodeProjectIsJavaEE5, isEE6, nodeProject);
        } else {
            result = generateServiceLocatorLookup(referencingFO, referencingClassName, serviceLocator, refIType, throwExceptions);
        }
        if (serviceLocator != null) {
            erc.setServiceLocatorName(serviceLocator);
        }

        // generate the server-specific resources

        if (refIType == refIType.REMOTE) {
            J2eeModuleProvider j2eeModuleProvider = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
            String referencedEjbName = getEjbName(referencedFO, referencedClassName);
            try {
                if (referencedClassName != null && j2eeModuleProvider.getJ2eeModule().getType().equals(J2eeModule.Type.WAR)) {
                    j2eeModuleProvider.getConfigSupport().bindEjbReference(ejbReferenceName, referencedEjbName);
                } else if (j2eeModuleProvider.getJ2eeModule().getType().equals(J2eeModule.Type.EJB)) {
                    String ejbName = getEjbName(referencingFO, referencingClassName);
                    String ejbType = getEjbType(referencingFO, referencingClassName);
                    if (ejbName != null && ejbType != null) {
                        j2eeModuleProvider.getConfigSupport().bindEjbReferenceForEjb(ejbName, ejbType, ejbReferenceName, referencedEjbName);
                    }
                }
            } catch (ConfigurationException ce) {
                Logger.getLogger("global").log(Level.WARNING, null, ce);
            }
        }
        
        return result;

    }
    
    // private stuff ===========================================================
    
    private ElementHandle<ExecutableElement> generateServiceLocatorLookup(FileObject referencingFO, String referencingClassName, 
            String serviceLocatorName, EjbReference.EjbRefIType refIType, boolean throwExceptions) {
        try {
            return generateServiceLocatorJNDI(
                    referencingFO, 
                    referencingClassName,
                    ejbReference.getHomeName(refIType),
                    ejbReferenceName, 
                    refIType != refIType.LOCAL,
                    ejbReference.getComponentName(refIType),
                    throwExceptions, 
                    serviceLocatorName
                    );
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }
    
    private ElementHandle<? extends Element> generateReferenceCode(
            FileObject fileObject,
            String className,
            EjbReference.EjbRefIType refIType,
            boolean throwExceptions,
            boolean isTargetEjb2x,
            boolean generateGlobalJNDI,
            Project ejbProject) {
        
        ElementHandle<? extends Element> result = null;
        
        try {
            if (InjectionTargetQuery.isInjectionTarget(fileObject, className) && 
               !(Utils.getBeanType(ejbReference).equals(Session.SESSION_TYPE_STATEFUL) && Utils.isServlet(fileObject, className))) {
                if (isTargetEjb2x) {
                    result = generateInjectionEjb21FromEE5(
                            fileObject,
                            className,
                            ejbReference.getHomeName(refIType),
                            ejbReference.getComponentName(refIType)
                            );
                } else {
                    result = generateInjection(
                            fileObject, 
                            className, 
                            ejbReference.getComponentName(refIType)
                            );
                }
            } else {
                result = generateJNDI(
                        fileObject, 
                        className, 
                        throwExceptions,
                        refIType,
                        generateGlobalJNDI,
                        ejbProject
                        );
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        
        return result;
    }

    private static final String LOG_STATEMENT =
            "java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\" ,{0});\n";
    
    private static final String JNDI_LOOKUP_LOCAL =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "{1} rv = ({1}) c.lookup(\"java:comp/env/{0}\");\n" +
            "return rv{2};\n";
    
    private static final String JNDI_LOOKUP_REMOTE =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "Object remote = c.lookup(\"java:comp/env/{0}\");\n" +
            "{1} rv = ({1}) javax.rmi.PortableRemoteObject.narrow(remote, {1}.class);\n" +
            "return rv{2};\n";

    /**
     * Lookup code for EJB 2.x beans in Java SE environments.
     */
    private static final String JNDI_LOOKUP_REMOTE_JAVASE =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "Object remote = c.lookup(\"{0}\");\n" +
            "{1} rv = ({1}) javax.rmi.PortableRemoteObject.narrow(remote, {1}.class);\n" +
            "return rv{2};\n";
    
    private static final String JNDI_LOOKUP_EJB3 =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "return ({1}) c.lookup(\"java:comp/env/{0}\");\n";

    /**
     * Lookup code for EJB 3.1 beans in Java EE 6 environments.
     * {0} - module name [<app name>/]<module name>
     * {1} - bean name
     * {2} - interface class
     */
    private static final String JNDI_LOOKUP_GLOBAL =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "return ({2}) c.lookup(\"java:global/{0}/{1}!{2}\");\n";
    
    /**
     * Lookup code for EJB3 beans in Java SE environments.
     */
    private static final String JNDI_LOOKUP_EJB3_JAVASE =
            "javax.naming.Context c = new javax.naming.InitialContext();\n" +
            "return ({1}) c.lookup(\"{0}\");\n";
    
    /**
     * Lookup code for EJB 2.x beans in Java EE 5 environments.
     * {0} - name of variable representing component interface
     * {1} - name of variable representing home interface
     */
    private static final String JNDI_LOOKUP_EJB3_JAVAEE5 =
            "try '{'\n" +
            "    {0} = {1}.create();\n" +
            "'}' catch(Exception e) '{'\n" +
            "    throw new javax.ejb.EJBException(e);\n" +
            "'}'\n";

    
    private ElementHandle<VariableElement> generateInjection(FileObject fileObject, final String className, final String fieldTypeClass) throws IOException {
        String strippedRefName = ejbReferenceName.substring(ejbReferenceName.lastIndexOf('/') + 1);
        String name = Character.toLowerCase(strippedRefName.charAt(0)) + strippedRefName.substring(1);
        return _RetoucheUtil.generateAnnotatedField(
                fileObject, 
                className, 
                "javax.ejb.EJB", 
                _RetoucheUtil.uniqueMemberName(fileObject, className, name, "ejb"),
                fieldTypeClass, 
                isDefaultRefName ? null : Collections.singletonMap("name", strippedRefName), // XXX still not sure about this, is needed?
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
    }
    
    private ElementHandle<VariableElement> generateInjectionEjb21FromEE5(FileObject fileObject, final String className, final String homeName, final String componentName) throws IOException {
        
        String strippedRefName = ejbReferenceName.substring(ejbReferenceName.lastIndexOf('/') + 1);
        final String name = Character.toLowerCase(strippedRefName.charAt(0)) + strippedRefName.substring(1);
        String strippedHomeName = homeName.substring(homeName.lastIndexOf('.') + 1);
        final String homeFieldName = Character.toLowerCase(strippedHomeName.charAt(0)) + strippedHomeName.substring(1);
        
        // injection of EJB 2.x home interface
        ElementHandle<VariableElement> elementToOpen = _RetoucheUtil.generateAnnotatedField(
                fileObject, 
                className, 
                "javax.ejb.EJB", 
                _RetoucheUtil.uniqueMemberName(fileObject, className, homeFieldName, "ejb"),
                homeName, 
                isDefaultRefName ? null : Collections.singletonMap("name", strippedRefName), // XXX still not sure about this, is needed?
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                TypeElement callerTypeElement = workingCopy.getElements().getTypeElement(className);
                TypeElement componentTypeElement = workingCopy.getElements().getTypeElement(componentName);
        
                // field for EJB 2.x component interface
                VariableTree variableTree = treeMaker.Variable(
                        treeMaker.Modifiers(Collections.singleton(Modifier.PRIVATE)),
                        name,
                        treeMaker.QualIdent(componentTypeElement),
                        null
                        );
                ClassTree classTree = workingCopy.getTrees().getTree(callerTypeElement);
                ClassTree newClassTree = treeMaker.insertClassMember(classTree, 2, variableTree);
                
                // init method with @PostConstruct annotation to initialize component interface variable
                ModifiersTree modifiersTree = treeMaker.Modifiers(Collections.singleton(Modifier.PRIVATE));
                AnnotationTree annotationTree = GenerationUtils.newInstance(workingCopy).createAnnotation("javax.annotation.PostConstruct"); // NOI18N
                modifiersTree = treeMaker.addModifiersAnnotation(modifiersTree, annotationTree);
                
                MethodTree methodTree = treeMaker.Method(
                        modifiersTree,
                        "initialize",
                        treeMaker.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        '{' + MessageFormat.format(JNDI_LOOKUP_EJB3_JAVAEE5, new Object[] { name, homeFieldName }) + '}',
                        null
                        );
                methodTree = (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                newClassTree = treeMaker.insertClassMember(newClassTree, 3, methodTree);
                
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        
        return elementToOpen;
    }
    
    private ElementHandle<ExecutableElement> generateJNDI(final FileObject fileObject, final String className,
            boolean throwCheckedExceptions, EjbRefIType refIType, final boolean global, Project ejbProject) throws IOException {
        final String name = "lookup" + ejbName + refIType;
        String body = null;
        String componentName = ejbReference.getComponentName(refIType);
        String homeName = ejbReference.getHomeName(refIType);
        boolean isLocal = !refIType.equals(refIType.REMOTE);
        List<String> exceptions = new ArrayList<String>();
        boolean isTargetJavaSE = Utils.isTargetJavaSE(fileObject, className);
        String sessionCreate = "";
        if (isSession) {
            sessionCreate = ".create()";
        }
        if (global){
            String moduleFullName = ProjectUtils.getInformation(ejbProject).getName();
            Project j2eeAppProject = Utils.getNestingJ2eeApp(ejbProject);
            if (j2eeAppProject != null && !j2eeAppProject.equals(ejbProject)){
                moduleFullName = ProjectUtils.getInformation(j2eeAppProject).getName() + "/" + moduleFullName;
            }
            body = MessageFormat.format(JNDI_LOOKUP_GLOBAL, new Object[] {moduleFullName, ejbName, componentName});
        } else if (isSimplified && isTargetJavaSE){
            body = MessageFormat.format(JNDI_LOOKUP_EJB3_JAVASE, new Object[] {ejbReference.getEjbClass(), componentName});
        } else if (isSimplified) {
            body = MessageFormat.format(JNDI_LOOKUP_EJB3, new Object[] {ejbReferenceName, componentName});
        } else if (isTargetJavaSE){
            body = MessageFormat.format(JNDI_LOOKUP_REMOTE_JAVASE, new Object[] {homeName, homeName, sessionCreate});
        } else if (!isLocal) {
            body = MessageFormat.format(JNDI_LOOKUP_REMOTE, new Object[] {ejbReferenceName, homeName, sessionCreate});
        } else {
            body = MessageFormat.format(JNDI_LOOKUP_LOCAL, new Object[] {ejbReferenceName, homeName, sessionCreate});
        }
        String returnType = isSimplified ? componentName : homeName;
        exceptions.add(NamingException.class.getName());
        if (isSession) {
            returnType = componentName;
            if (!isSimplified) {
                exceptions.add("javax.ejb.CreateException");
            }
            if (!isSimplified && !isLocal) {
                exceptions.add("java.rmi.RemoteException");
            }
        }
        if (!throwCheckedExceptions) {
            Iterator exIt = exceptions.iterator();
            StringBuffer catchBody = new StringBuffer("try {\n" + body + "}\n"); // NOI18N
            while (exIt.hasNext()) {
                String exceptionName = (String) exIt.next();
                catchBody.append("catch("); // NOI18N
                catchBody.append(exceptionName);
                catchBody.append(' ');  //NOI18N
                String capitalLetters = extractAllCapitalLetters(exceptionName);
                catchBody.append(capitalLetters);
                catchBody.append(") {\n"); //NOI18N
                catchBody.append(MessageFormat.format(LOG_STATEMENT,
                        new Object[] {capitalLetters}));
                catchBody.append("throw new RuntimeException("+capitalLetters+");\n");
                catchBody.append("}\n"); //NOI18N
            }
            body = catchBody.toString();
            exceptions = Collections.<String>emptyList();
        }

        final MethodModel methodModel = MethodModel.create(
                _RetoucheUtil.uniqueMemberName(fileObject, className, name, "ejb"),
                returnType,
                body,
                Collections.<MethodModel.Variable>emptyList(),
                exceptions,
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                ClassTree newClassTree = treeMaker.addClassMember(classTree, methodTree);

                if (global){
                    // field itself
                    VariableTree variableTree = treeMaker.Variable(
                            treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                            _RetoucheUtil.uniqueMemberName(fileObject, className, ejbName, "ejb"),
                            methodTree.getReturnType(),
                            treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                                       treeMaker.Identifier(methodTree.getName()),
                                                       Collections.<ExpressionTree>emptyList())
                            );
                    // adding field to class
                    newClassTree = treeMaker.insertClassMember(newClassTree, 0, variableTree);
                }
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();

        return _RetoucheUtil.getMethodHandle(javaSource, methodModel, className);
        
    }
    
    private ElementHandle<ExecutableElement> generateServiceLocatorJNDI(FileObject referencingFO, final String referencingClassName, String homeName, String refName,
        boolean narrow, String componentName, boolean throwCheckedExceptions, String serviceLocatorName) throws IOException {
        String name = "lookup"+refName.substring(refName.lastIndexOf('/')+1);
        String body = null;
        List<String> exceptions = new ArrayList<String>(3);
        exceptions.add(NamingException.class.getName());
        String returnType = isSimplified ? componentName : homeName;
        boolean genCreate = isSession;
        if (genCreate) {
            returnType = componentName;
            exceptions.add("javax.ejb.CreateException"); //NOI18N
            if (narrow) {
                exceptions.add("java.rmi.RemoteException"); //NOI18N
            }
        }
        Project enterpriseProject = FileOwnerQuery.getOwner(referencingFO);
        ServiceLocatorStrategy sls = ServiceLocatorStrategy.create(enterpriseProject, referencingFO, serviceLocatorName);
        if (narrow) {
            body = sls.genRemoteEjbStringLookup(refName, homeName, referencingFO, referencingClassName, genCreate);
        } else {
            body = sls.genLocalEjbStringLookup(refName, homeName, referencingFO, referencingClassName, genCreate);
        }
        if (!throwCheckedExceptions) {
            Iterator exIt = exceptions.iterator();
            StringBuffer catchBody = new StringBuffer("try {\n" + body + "\n}"); // NOI18N
            while (exIt.hasNext()) {
                String exceptionName = (String) exIt.next();
                catchBody.append(" catch("); // NOI18N
                catchBody.append(exceptionName);
                catchBody.append(' ');  //NOI18N
                String capitalLetters = extractAllCapitalLetters(exceptionName);
                catchBody.append(capitalLetters);
                catchBody.append(") {\n"); //NOI18N
                catchBody.append(MessageFormat.format(LOG_STATEMENT, new Object[] {capitalLetters}));
                catchBody.append("throw new RuntimeException("+capitalLetters+");\n");
                catchBody.append('}');
                body = catchBody.toString();
                exceptions = Collections.<String>emptyList();
            }
        }
            
        final MethodModel methodModel = MethodModel.create(
                _RetoucheUtil.uniqueMemberName(referencingFO, referencingClassName, name, "ejb"),
                returnType,
                body,
                Collections.<MethodModel.Variable>emptyList(),
                exceptions,
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(referencingFO);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(referencingClassName);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();

        return _RetoucheUtil.getMethodHandle(javaSource, methodModel, referencingClassName);

    }
    
    private String extractAllCapitalLetters(String word) {
        StringBuffer caps = new StringBuffer(4);
        for (int i =0; i < word.length(); i++) {
            char character = word.charAt(i);
            if (Character.isUpperCase(character)) {
                caps.append(Character.toLowerCase(character));
            }
        }
        return caps.toString();
    }
    
    private static void addProjectToClassPath(final Project enterpriseProject, final EjbReference ref, FileObject refFO, EjbRefIType refIType) throws IOException {
        
        Project target = Utils.getProject(ref, refIType);
        
        boolean differentProject = target != null && !enterpriseProject.equals(target);
        if (differentProject) {
//            Sources sg = ProjectUtils.getSources(target);
//            SourceGroup[] grp = sg.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (isJEEModule(target)) {
                ProjectClassPathModifier.addProjects(new Project[] {target} , refFO, JavaClassPathConstants.COMPILE_ONLY);
            } else {
                ProjectClassPathModifier.addProjects(new Project[] {target} , refFO, ClassPath.COMPILE);
            }
        }
    }
    
    private static boolean isJEEModule(Project target) {
        return target.getLookup().lookup(J2eeModuleProvider.class) != null;
    }

    private static String getEjbName(FileObject fileObject, final String className) throws IOException {
        MetadataModel<EjbJarMetadata> metadataModel = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject).getMetadataModel();
        return metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                Ejb ejb = metadata.findByEjbClass(className);
                return ejb == null ? null : ejb.getEjbName();
            }
        });

    }    

    private static String getEjbType(FileObject fileObject, final String className) throws IOException {
        
        MetadataModel<EjbJarMetadata> metadataModel = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject).getMetadataModel();
        return metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                String result = null;
                Ejb ejb = metadata.findByEjbClass(className);
                if (ejb instanceof Session) {
                    result = EnterpriseBeans.SESSION;
                } else if (ejb instanceof Entity) {
                    result = EnterpriseBeans.ENTITY;
                } else if (ejb instanceof MessageDriven) {
                    result = EnterpriseBeans.MESSAGE_DRIVEN;
                }
                return result;
            }
        });
        
    }

}
