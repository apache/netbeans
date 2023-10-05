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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.ServiceLocatorStrategy;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.web.beans.CdiUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public final class SendJMSGenerator {

    private static final Logger LOG = Logger.getLogger(SendJMSGenerator.class.getName());

    private static final String PRODUCES = org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef.MESSAGE_DESTINATION_USAGE_PRODUCES;
    
    private final MessageDestination messageDestination;
    private final Project mdbHolderProject;
    
    public SendJMSGenerator(MessageDestination messageDestination, Project mdbHolderProject) {
        this.messageDestination = messageDestination;
        this.mdbHolderProject = mdbHolderProject;
    }
    
    public void genMethods(
            EnterpriseReferenceContainer container,
            final String className,
            String connectionFactoryName,
            FileObject fileObject,
            ServiceLocatorStrategy slStrategy,
            J2eeModuleProvider j2eeModuleProvider) throws IOException {

        Project project = FileOwnerQuery.getOwner(fileObject);
        ContainerClassPathModifier ccpm = project.getLookup().lookup(ContainerClassPathModifier.class);
        if (ccpm != null) {
            ccpm.extendClasspath(fileObject, new String[] {
                ContainerClassPathModifier.API_J2EE //likely too wide, narrow
            });
        }

        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] isInjectionTarget = new boolean[1];
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                isInjectionTarget[0] = InjectionTargetQuery.isInjectionTarget(controller, typeElement);
            }
        }, true);
        InjectionStrategy injectionStrategy = getInjectionStrategy(project, isInjectionTarget[0]);
        boolean jakartaPackageNamespace = isJakartaPackageNamespace(project);
        String destinationFieldName = null;
        String connectionFactoryFieldName = null;
        String factoryName = connectionFactoryName;
        String destinationName = null;

        if (injectionStrategy == InjectionStrategy.INJ_EE7_SOURCES) {
            CdiUtil util = project.getLookup().lookup(CdiUtil.class);
            if (util != null) {
                FileObject beansFile = util.enableCdi();
                if (beansFile != null) {
                    injectionStrategy = InjectionStrategy.INJ_EE7_CDI;
                }
            }
        }
        switch (injectionStrategy) {
            case NO_INJECT:
                factoryName = generateConnectionFactoryReference(container, factoryName, fileObject, className, jakartaPackageNamespace);
                destinationName = generateDestinationReference(container, fileObject, className, jakartaPackageNamespace);
                break;

            case INJ_EE7_SOURCES:
            case INJ_COMMON:
                destinationName = messageDestination.getName();
                if (jakartaPackageNamespace) {
                    connectionFactoryFieldName = createInjectedResource(fileObject, className, factoryName, "jakarta.jms.ConnectionFactory", jakartaPackageNamespace); // NO18N
                    destinationFieldName = createInjectedResource(fileObject, className, destinationName,
                            messageDestination.getType() == Type.QUEUE ? "jakarta.jms.Queue" : "jakarta.jms.Topic",
                            jakartaPackageNamespace); //NOI18N
                } else {
                    connectionFactoryFieldName = createInjectedResource(fileObject, className, factoryName, "javax.jms.ConnectionFactory", jakartaPackageNamespace); // NO18N
                    destinationFieldName = createInjectedResource(fileObject, className, destinationName,
                            messageDestination.getType() == Type.QUEUE ? "javax.jms.Queue" : "javax.jms.Topic",  //NOI18N
                            jakartaPackageNamespace);
                }
                break;

            case INJ_EE7_CDI:
                if (jakartaPackageNamespace) {
                    destinationName = messageDestination.getName();
                    connectionFactoryFieldName = createInjectedFactory(fileObject, className, factoryName, "jakarta.jms.JMSContext", jakartaPackageNamespace); // NO18N
                    destinationFieldName = createInjectedResource(fileObject, className, destinationName,
                            messageDestination.getType() == Type.QUEUE ? "jakarta.jms.Queue" : "jakarta.jms.Topic", //NOI18N
                            jakartaPackageNamespace);
                } else {
                    destinationName = messageDestination.getName();
                    connectionFactoryFieldName = createInjectedFactory(fileObject, className, factoryName, "javax.jms.JMSContext", jakartaPackageNamespace); // NO18N
                    destinationFieldName = createInjectedResource(fileObject, className, destinationName,
                            messageDestination.getType() == Type.QUEUE ? "javax.jms.Queue" : "javax.jms.Topic", //NOI18N
                            jakartaPackageNamespace);
                }
                break;
        }
        
        String sendMethodName = ""; //NOI18N
        if (injectionStrategy != InjectionStrategy.INJ_EE7_CDI && injectionStrategy != InjectionStrategy.INJ_EE7_SOURCES) {
            sendMethodName = createSendMethod(fileObject, className, messageDestination.getName(), jakartaPackageNamespace);
        }
        createJMSProducer(fileObject, className, factoryName, connectionFactoryFieldName, destinationName,
                destinationFieldName, sendMethodName, slStrategy, injectionStrategy, jakartaPackageNamespace);

        if (messageDestination != null
                && injectionStrategy != InjectionStrategy.INJ_EE7_CDI && injectionStrategy != InjectionStrategy.INJ_EE7_SOURCES ) {
            try {
                if (j2eeModuleProvider.getJ2eeModule().getType().equals(J2eeModule.Type.WAR)) {
                    //in the current implementation, reference name is the same as the destination name...
                    j2eeModuleProvider.getConfigSupport().bindMessageDestinationReference(
                            messageDestination.getName(), factoryName, messageDestination.getName(), messageDestination.getType());
                } else if (j2eeModuleProvider.getJ2eeModule().getType().equals(J2eeModule.Type.EJB)) {
                        //in the current implementation, reference name is the same as the destination name...
                        bindMessageDestinationReferenceForEjb(j2eeModuleProvider, fileObject, className,
                                messageDestination.getName(), factoryName, messageDestination.getName(), messageDestination.getType());
                    }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
    }

    private void bindMessageDestinationReferenceForEjb(J2eeModuleProvider j2eeModuleProvider,
            FileObject fileObject,final String className,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type destType) throws ConfigurationException, IOException {

            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
        
        final String[] ejbName = new String[1];
        final String[] ejbType = new String[1];
        
        metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            @Override
            public Void run(EjbJarMetadata metadata) throws Exception {
                Ejb ejb = metadata.findByEjbClass(className);
                if (ejb != null) {
                    ejbName[0] = ejb.getEjbName();
                    if (ejb instanceof Session) {
                        ejbType[0] = EnterpriseBeans.SESSION;
                    } else if (ejb instanceof MessageDriven) {
                        ejbType[0] = EnterpriseBeans.MESSAGE_DRIVEN;
                    } else if (ejb instanceof Entity) {
                        ejbType[0] = EnterpriseBeans.ENTITY;
                    }
                }
                return null;
            }
        });
        
        if (ejbName[0] != null && ejbType[0] != null) {
            j2eeModuleProvider.getConfigSupport().bindMessageDestinationReferenceForEjb(
                    ejbName[0], ejbType[0], referenceName, connectionFactoryName, destName, destType);
        }
    }        

    private String generateConnectionFactoryReference(EnterpriseReferenceContainer container, String referenceName, FileObject referencingFile, String referencingClass, boolean jakartaPackageNamespace) throws IOException {
        ResourceReference ref = ResourceReference.create(
                referenceName,
                jakartaPackageNamespace ? "jakarta.jms.ConnectionFactory": "javax.jms.ConnectionFactory",
                ResourceRef.RES_AUTH_CONTAINER,
                ResourceRef.RES_SHARING_SCOPE_SHAREABLE,
                null
                );
        return container.addResourceRef(ref, referencingFile, referencingClass);
    }
    
    private String generateDestinationReference(EnterpriseReferenceContainer container, FileObject referencingFile, String referencingClass, boolean jakartaPackageNamespace) throws IOException {
        // this may need to generalized later if jms producers are expected
        // in web modules
        ProjectInformation projectInformation = ProjectUtils.getInformation(mdbHolderProject);
        String link = projectInformation.getName() + ".jar#" + messageDestination.getName();
        Project referenceingProject = FileOwnerQuery.getOwner(referencingFile);
        if (mdbHolderProject.equals(referenceingProject)) {
            link = link.substring(link.indexOf('#') + 1);
        }
        String queueClass = "jakarta.jms.Queue";
        String topicClass = "jakarta.jms.Topic";
        if (!jakartaPackageNamespace) {
            queueClass = "javax.jms.Queue";
            topicClass = "javax.jms.Topic";
        }
        MessageDestinationReference ref = MessageDestinationReference.create(
                messageDestination.getName(),
                messageDestination.getType() == MessageDestination.Type.QUEUE ? queueClass : topicClass,
                PRODUCES,
                link
                );
        return container.addDestinationRef(ref, referencingFile, referencingClass);
    }
    
    /**
     * Creates an injected resource field for the given <code>target</code>. The name
     * of the field will be derivated from the given <code>destinationName</code>.
     * @param target the target class
     * @param mappedName the value for resource's mappedName attribute
     * @param fieldType the class of the field.
     * @return name of the created field.
     */
    private String createInjectedResource(FileObject fileObject, String className, String destinationName, String fieldType, boolean jakartaPackageNamespace) throws IOException {
        String fieldName = Utils.makeJavaIdentifierPart(Utils.jndiNameToCamelCase(destinationName, true, "jms")); //NOI18N
        _RetoucheUtil.generateAnnotatedField(
                fileObject,
                className,
                jakartaPackageNamespace ? "jakarta.annotation.Resource" : "javax.annotation.Resource", //NOI18N
                fieldName,
                fieldType,
                Collections.singletonMap("mappedName", destinationName),  //NOI18N
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
        return fieldName;
    }

    /**
     * Creates an injected JMSConnectionFactory field for the given <code>target</code>. The name
     * of the field will be derivated from the given <code>destinationName</code>.
     * @param target the target class
     * @param mappedName the value for resource's mappedName attribute
     * @param fieldType the class of the field.
     * @return name of the created field.
     */
    private String createInjectedFactory(FileObject fileObject, String className, String destinationName, String fieldType, boolean jakartaPackageNamespace) throws IOException {
        String fieldName = "context"; //NOI18N
        final ElementHandle<VariableElement> field = _RetoucheUtil.generateAnnotatedField(
                fileObject,
                className,
                jakartaPackageNamespace ? "jakarta.jms.JMSConnectionFactory" : "javax.jms.JMSConnectionFactory", //NOI18N
                fieldName,
                fieldType,
                Collections.singletonMap("", destinationName),  //NOI18N
                InjectionTargetQuery.isStaticReferenceRequired(fileObject, className)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            @Override
            public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                GenerationUtils genUtils = GenerationUtils.newInstance(parameter);
                TreePath fieldTree = parameter.getTrees().getPath(field.resolve(parameter));
                VariableTree originalTree = (VariableTree) fieldTree.getLeaf();
                ModifiersTree modifiers = originalTree.getModifiers();
                List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(modifiers.getAnnotations());
                annotations.add(0, genUtils.createAnnotation(
                        jakartaPackageNamespace ? "jakarta.inject.Inject" : "javax.inject.Inject" //NOI18N
                ));
                ModifiersTree nueMods = parameter.getTreeMaker().Modifiers(modifiers, annotations);
                parameter.rewrite(modifiers, nueMods);
            }
        }).commit();
        return fieldName;
    }
    
    private String createSendMethod(FileObject fileObject, final String className, String destination, boolean jakartaPackageNamespace) throws IOException {
        final MethodModel.Variable[] parameters = new MethodModel.Variable[] {
            MethodModel.Variable.create(jakartaPackageNamespace ? "jakarta.jms.Session" : "javax.jms.Session", "session"),
            MethodModel.Variable.create(Object.class.getName(), "messageData")
        };
        String methodName = "createJMSMessageFor" + Utils.makeJavaIdentifierPart(Utils.jndiNameToCamelCase(destination, true, null));
        final MethodModel methodModel = MethodModel.create(
                methodName,
                jakartaPackageNamespace ? "jakarta.jms.Message" : "javax.jms.Message",
                "// TODO create and populate message to send\n" +
                (jakartaPackageNamespace ? "jakarta" : "javax") +
                ".jms.TextMessage tm = session.createTextMessage();\n" +
                "tm.setText(messageData.toString());\n"+
                "return tm;\n",
                Arrays.asList(parameters),
                Collections.singletonList(jakartaPackageNamespace ? "jakarta.jms.JMSException" : "javax.jms.JMSException"),
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        return methodName;
    }
    
    private void createJMSProducer(
            FileObject fileObject,
            final String className,
            String connectionFactoryName,
            String connectionFactoryFieldName,
            String destinationName,
            String destinationFieldName,
            String sendMethodName,
            ServiceLocatorStrategy slStrategy,
            InjectionStrategy injectionStrategy,
            boolean jakartaPackageNamespace) throws IOException {
        String destName = Utils.makeJavaIdentifierPart(destinationName.substring(destinationName.lastIndexOf('/') + 1));
        StringBuffer destBuff = new StringBuffer(destName);
        destBuff.setCharAt(0, Character.toUpperCase(destBuff.charAt(0)));

        String body;
        List<String> throwsClause = new ArrayList<String>();
        String parameterType = Object.class.getName();
        switch (injectionStrategy) {
            case INJ_EE7_CDI:
                body = getSendJMSCodeForJMSContext(connectionFactoryFieldName, destinationFieldName);
                parameterType = String.class.getName();
                break;

            case INJ_EE7_SOURCES:
                body = getSendJMSCodeForCreatedJMSContext(connectionFactoryFieldName, destinationFieldName, jakartaPackageNamespace);
                parameterType = String.class.getName();
                break;

            case INJ_COMMON:
                throwsClause.add(jakartaPackageNamespace ? "jakarta.jms.JMSException" : "javax.jms.JMSException");
                body = getSendJMSCodeWithInjectedFields(connectionFactoryFieldName, destinationFieldName, sendMethodName, jakartaPackageNamespace);
                break;

            case NO_INJECT:
            default:
                throwsClause.add(jakartaPackageNamespace ? "jakarta.jms.JMSException" : "javax.jms.JMSException");
                if (slStrategy == null) {
                    body = getSendJMSCode(connectionFactoryName, destinationName, sendMethodName, jakartaPackageNamespace);
                    throwsClause.add("javax.naming.NamingException");
                } else {
                    body = getSendJMSCode(connectionFactoryName, destinationName, sendMethodName, slStrategy, fileObject, className, jakartaPackageNamespace);
                }
                break;
        }

        final MethodModel methodModel = MethodModel.create(
                "sendJMSMessageTo" + destBuff, //NOI18N
                "void", //NOI18N
                body,
                Collections.singletonList(MethodModel.Variable.create(parameterType, "messageData")), //NOI18N
                throwsClause,
                Collections.singleton(Modifier.PRIVATE)
                );
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }

    /**
     * @return String representing the code for send JMS method using injected fields.
     */
    private String getSendJMSCodeWithInjectedFields(String connectionFactoryFieldName,
            String destinationFieldName,
            String messageMethodName,
            boolean jakartaPackageNamespace){
        
        return MessageFormat.format(
                "{3}.jms.Connection connection = null;\n" +
                "{3}.jms.Session session = null;\n" +
                "try '{' \n" +
                "connection = {0}.createConnection();\n" +
                "session = connection.createSession(false,{3}.jms.Session.AUTO_ACKNOWLEDGE);\n" +
                "{3}.jms.MessageProducer messageProducer = session.createProducer({1});\n" +
                "messageProducer.send({2}(session, messageData));\n" +
                " '}' finally '{'\n" +
                "if (session != null) '{'\n" +
                "try '{'\n" +
                " session.close();\n" +
                "'}' catch (JMSException e) '{'\n" +
                "java.util.logging.Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.WARNING, \"Cannot close session\", e);\n" +
                "'}'\n" +
                "'}'\n" +
                "if (connection != null) '{'\n" +
                "connection.close();\n" +
                "'}'\n" +
                "'}'\n",
                connectionFactoryFieldName, destinationFieldName, messageMethodName, jakartaPackageNamespace ? "jakarta" : "javax");
    }

    /**
     * @return String representing the code for send JMS method using injected AutoCloseable JMSContext.
     */
    private String getSendJMSCodeForJMSContext(String contextFieldName,
            String destinationFieldName){
        return MessageFormat.format(
                "{0}.createProducer().send({1}, messageData);", //NOI18N
                contextFieldName, destinationFieldName);
    }

    /**
     * @return String representing the code for send JMS method using injected factory which
     * creates AutoCloseable JMSContext.
     */
    private String getSendJMSCodeForCreatedJMSContext(String connectionFactoryFieldName,
            String destinationFieldName,
            boolean jakartaPackageNamespace){
        return MessageFormat.format(
                "try ({2}.jms.JMSContext context = {0}.createContext()) '{'\n" +                //NOI18N
                "context.createProducer().send({1}, messageData);\n" +    //NOI18N
                "'}'\n",                                                                 //NOI18N
                connectionFactoryFieldName, destinationFieldName, jakartaPackageNamespace ? "jakarta" : "javax");
    }
    
    private String getSendJMSCode(String connectionName, String destinationName,
            String messageMethodName, ServiceLocatorStrategy sls,
            FileObject fileObject, String className, boolean jakartaPackageNamespace) {
        String connectionFactory = sls.genJMSFactory(connectionName, fileObject, className);
        String destination = sls.genDestinationLookup(destinationName, fileObject, className);
        return MessageFormat.format(
                "{3}.jms.ConnectionFactory cf = ({3}.jms.ConnectionFactory) " + connectionFactory + ";\n" +
                "{3}.jms.Connection conn = null;\n" +
                "{3}.jms.Session s = null;\n" +
                "try '{' \n" +
                "conn = cf.createConnection();\n" +
                "s = conn.createSession(false,s.AUTO_ACKNOWLEDGE);\n" +
                "{3}.jms.Destination destination = ({3}.jms.Destination) " + destination + ";\n" +
                "{3}.jms.MessageProducer mp = s.createProducer(destination);\n" +
                "mp.send({2}(s,messageData));\n" +
                " '}' finally '{'\n" +
                "if (s != null) '{'\n"+
                "try '{'\n" +
                " s.close();\n" +
                "'}' catch (JMSException e) '{'\n" +
                "java.util.logging.Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.WARNING, \"Cannot close session\", e);\n" +
                "'}'\n" +
                "'}'\n" +
                "if (conn != null) '{'\n" +
                "conn.close();\n" +
                "'}'\n" +
                "'}'\n",
                new Object[] {connectionName, destinationName, messageMethodName, jakartaPackageNamespace ? "jakarta" : "javax"});
    }
    
    private String getSendJMSCode(String connectionName, String destinationName,
            String messageMethodName, boolean jakartaPackageNamespace) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" +
                "{3}.jms.ConnectionFactory cf = ({3}.jms.ConnectionFactory) c.lookup(\"java:comp/env/{0}\");\n" +
                "{3}.jms.Connection conn = null;\n" +
                "{3}.jms.Session s = null;\n" +
                "try '{' \n" +
                "conn = cf.createConnection();\n" +
                "s = conn.createSession(false,s.AUTO_ACKNOWLEDGE);\n" +
                "{3}.jms.Destination destination = ({3}.jms.Destination) c.lookup(\"java:comp/env/{1}\");\n" +
                "{3}.jms.MessageProducer mp = s.createProducer(destination);\n" +
                "mp.send({2}(s,messageData));\n" +
                " '}' finally '{'\n" +
                "if (s != null) '{'\n"+
                "try '{'\n" +
                " s.close();\n" +
                "'}' catch (JMSException e) '{'\n" +
                "java.util.logging.Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.WARNING, \"Cannot close session\", e);\n" +
                "'}'\n" +
                "'}'\n" +
                "if (conn != null) '{'\n" +
                "conn.close();\n" +
                "'}'\n" +
                "'}'\n",
                new Object[] {connectionName, destinationName, messageMethodName, jakartaPackageNamespace ? "jakarta" : "javax"});
    }

    public static InjectionStrategy getInjectionStrategy(Project project, boolean injectable) {
        if (!injectable) {
            return InjectionStrategy.NO_INJECT;
        }

        J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(project);
        if (! (capabilities.isEjb32LiteSupported() || capabilities.isEjb40LiteSupported())) {
            return InjectionStrategy.INJ_COMMON;
        } else {
            CdiUtil cdiUtil = project.getLookup().lookup(CdiUtil.class);
            if (cdiUtil.isCdiEnabled()) {
                return InjectionStrategy.INJ_EE7_CDI;
            } else {
                return InjectionStrategy.INJ_EE7_SOURCES;
            }
        }
    }

    public static boolean isJakartaPackageNamespace(Project project) {
        J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(project);
        return capabilities.isEjb40LiteSupported();
    }

    public static enum InjectionStrategy {
        INJ_EE7_CDI,
        INJ_EE7_SOURCES,
        INJ_COMMON,
        NO_INJECT
    }
}
