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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.ActivationConfigProperties;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.modules.javaee.resources.api.JndiResourcesDefinition;
import org.netbeans.modules.javaee.specs.support.api.JmsSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Generator of MessageDriven EJBs for EJB 2.1 and 3.0
 *
 * @author Martin Adamek
 */
public final class MessageGenerator {

    private static final String EJB21_EJBCLASS = "Templates/J2EE/EJB21/MessageDrivenEjbClass.java"; // NOI18N
    private static final String EJB30_MESSAGE_DRIVEN_BEAN = "Templates/J2EE/EJB30/MessageDrivenBean.java"; // NOI18N

    private static final String DESTINATION_LOOKUP = "destinationLookup"; //NOI18N

    private static final String QUEUE_INTERFACE = "javax.jms.Queue"; // NOI18N

    private static final String TOPIC_INTERFACE = "javax.jms.Topic"; // NOI18N

    // informations collected in wizard
    private final FileObject pkg;
    private final MessageDestination messageDestination;
    private final boolean isSimplified;
    private final boolean isXmlBased;
    private final Profile profile;
    private final JmsSupport jmsSupport;

    // EJB naming options
    private final EJBNameOptions ejbNameOptions;
    private final String ejbName;
    private final String ejbClassName;
    private final String displayName;
    
    private final String packageName;
    private final String packageNameWithDot;
    
    private final Map<String, Object> templateParameters;

    public static MessageGenerator create(Profile profile, String wizardTargetName, FileObject pkg, MessageDestination messageDestination, boolean isSimplified, Map<String, String> properties, JmsSupport jmsSupport) {
        return new MessageGenerator(profile, wizardTargetName, pkg, messageDestination, isSimplified, properties, jmsSupport, false);
    }

    protected MessageGenerator(Profile profile, String wizardTargetName, FileObject pkg, MessageDestination messageDestination, boolean isSimplified, Map<String, String> properties, JmsSupport jmsSupport, boolean isTest) {
        this.pkg = pkg;
        this.messageDestination = messageDestination;
        this.isSimplified = isSimplified;
        this.isXmlBased = !isSimplified;
        this.ejbNameOptions = new EJBNameOptions();
        this.ejbName = ejbNameOptions.getMessageDrivenEjbNamePrefix() + wizardTargetName + ejbNameOptions.getMessageDrivenEjbNameSuffix();
        this.ejbClassName = ejbNameOptions.getMessageDrivenEjbClassPrefix() + wizardTargetName + ejbNameOptions.getMessageDrivenEjbClassSuffix();
        this.displayName = ejbNameOptions.getMessageDrivenDisplayNamePrefix() + wizardTargetName + ejbNameOptions.getMessageDrivenDisplayNameSuffix();
        this.packageName = EjbGenerationUtil.getSelectedPackageName(pkg);
        this.packageNameWithDot = packageName + ".";
        this.templateParameters = new HashMap<String, Object>();
        this.profile = profile;
        this.jmsSupport = jmsSupport;
        boolean useMappedName = useMappedName();
        if (profile != null && profile.isAtLeast(Profile.JAVA_EE_7_WEB) && jmsSupport.useDestinationLookup()) {
            String destination = properties.get(ActivationConfigProperties.DESTINATION_LOOKUP) == null ||
                    properties.get(ActivationConfigProperties.DESTINATION_LOOKUP).isEmpty() ?
                                   messageDestination.getName() : properties.get(ActivationConfigProperties.DESTINATION_LOOKUP);
            properties.put(DESTINATION_LOOKUP, destination);
        } else {
            if (!useMappedName) {
                properties.put(jmsSupport.activationConfigProperty(), messageDestination.getName());
            }
        }
        // fill all possible template parameters
        this.templateParameters.put("package", packageName);
        this.templateParameters.put("messageDestinationName", messageDestination.getName());
        this.templateParameters.put("activationConfigProperties", transformProperties(properties));
        this.templateParameters.put("useMappedName", useMappedName);
        if (isTest) {
            // set date, time and user to values used in goldenfiles
            this.templateParameters.put("date", "{date}");
            this.templateParameters.put("time", "{time}");
            this.templateParameters.put("user", "{user}");
        }
    }

    private static List<KeyValuePair> transformProperties(Map<String, String> properties) {
        List<KeyValuePair> props = new ArrayList<KeyValuePair>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            props.add(new KeyValuePair(entry.getKey(), entry.getValue()));
        }
        return props;
    }

    public FileObject generate() throws IOException {
        FileObject resultFileObject = null;
        if (isSimplified) {
            resultFileObject = generateEJB30Classes();
            if (isXmlBased) {
                generateEJB30Xml();
            }
        } else {
            resultFileObject = generateEJB21Classes();
            if (isXmlBased) {
                try {
                    generateEJB21Xml();
                } catch (VersionNotSupportedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            try {
                Project project = FileOwnerQuery.getOwner(pkg);
                J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
                j2eeModuleProvider.getConfigSupport().bindMdbToMessageDestination(
                        ejbName,
                        messageDestination.getName(),
                        messageDestination.getType());
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return resultFileObject;
    }
    
    private FileObject generateEJB21Classes() throws IOException {
        return GenerationUtils.createClass(EJB21_EJBCLASS,  pkg, ejbClassName, null, templateParameters);
    }
    
    private boolean isQueue() {
        return MessageDestination.Type.QUEUE.equals(messageDestination.getType());
    }
    
    private FileObject generateEJB30Classes() throws IOException {
        FileObject mdb = GenerationUtils.createClass(EJB30_MESSAGE_DRIVEN_BEAN,  pkg, ejbClassName, null, templateParameters);
        if (messageDestination instanceof JmsDestinationDefinition
                && ((JmsDestinationDefinition) messageDestination).isToGenerate()) {
            generateJMSDestinationDefinition(mdb, (JmsDestinationDefinition) messageDestination);
        }
        return mdb;
    }

    private void generateJMSDestinationDefinition(FileObject classFile, final JmsDestinationDefinition def) throws IOException {
        JavaSource js = JavaSource.forFileObject(classFile);
        js.runModificationTask(new Task<WorkingCopy>() {
            @Override
            public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement classElement = parameter.getElements().getTypeElement(packageNameWithDot + ejbClassName);
                ClassTree classTree = parameter.getTrees().getTree(classElement);
                ModifiersTree modifiers = classTree.getModifiers();
                TypeElement el = parameter.getElements().getTypeElement(JndiResourcesDefinition.ANN_JMS_DESTINATION);

                TreeMaker tm = parameter.getTreeMaker();
                List<ExpressionTree> values = new ArrayList<ExpressionTree>(2);
                ExpressionTree nameQualIdent = tm.QualIdent("name"); //NOI18N
                values.add(tm.Assignment(nameQualIdent, tm.Literal(def.getName())));
                ExpressionTree classnameQualIdent = tm.QualIdent("interfaceName"); //NOI18N
                values.add(tm.Assignment(classnameQualIdent, tm.Literal(getInterfaceName(def))));
                ExpressionTree resourceAdapterQualIdent = tm.QualIdent("resourceAdapter"); //NOI18N
                values.add(tm.Assignment(resourceAdapterQualIdent, tm.Literal("jmsra"))); //NOI18N
                ExpressionTree destinationNameQualIdent = tm.QualIdent("destinationName"); //NOI18N
                values.add(tm.Assignment(destinationNameQualIdent, tm.Literal(getPhysicalName(def.getName())))); //NOI18N

                List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(modifiers.getAnnotations());
                annotations.add(0, tm.Annotation(tm.QualIdent(el), values));
                ModifiersTree nueMods = tm.Modifiers(modifiers, annotations);
                parameter.rewrite(modifiers, nueMods);
            }
        }).commit();
    }

    private static String getPhysicalName(String jndiName) {
        int lastSlashIndex = jndiName.lastIndexOf("/"); //NOI18N
        if (lastSlashIndex == -1) {
            return jndiName;
        } else {
            return jndiName.substring(lastSlashIndex + 1);
        }
    }

    private static String getInterfaceName(JmsDestinationDefinition def) {
        String interfaceName = null;
        switch (def.getType()) {
            case QUEUE:
                interfaceName = QUEUE_INTERFACE;
                break;
            case TOPIC:
                interfaceName = TOPIC_INTERFACE;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return interfaceName;
    }
    
    @SuppressWarnings("deprecation") //NOI18N
    private void generateEJB21Xml() throws IOException, VersionNotSupportedException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(pkg);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ejbModule.getDeploymentDescriptor()); // EJB 2.1
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        MessageDriven messageDriven = null;
        if (beans == null) {
            beans  = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        messageDriven = beans.newMessageDriven();
        ActivationConfig config = messageDriven.newActivationConfig();
        ActivationConfigProperty destProp = config.newActivationConfigProperty();
        destProp.setActivationConfigPropertyName("destinationType"); // NOI18N
        ActivationConfigProperty ackProp = config.newActivationConfigProperty();
        ackProp.setActivationConfigPropertyName("acknowledgeMode"); // NOI18N
        ackProp.setActivationConfigPropertyValue("Auto-acknowledge"); // NOI18N
        config.addActivationConfigProperty(ackProp);
        if (isQueue()) {
            String queue = "javax.jms.Queue"; // NOI18N
            messageDriven.setMessageDestinationType(queue);
            destProp.setActivationConfigPropertyValue(queue);
        } else {
            String topic = "javax.jms.Topic"; // NOI18N
            messageDriven.setMessageDestinationType(topic);
            destProp.setActivationConfigPropertyValue(topic);
            ActivationConfigProperty durabilityProp = config.newActivationConfigProperty();
            durabilityProp.setActivationConfigPropertyName("subscriptionDurability"); // NOI18N
            durabilityProp.setActivationConfigPropertyValue("Durable"); // NOI18N
            config.addActivationConfigProperty(durabilityProp);
            
            ActivationConfigProperty clientIdProp = config.newActivationConfigProperty();
            clientIdProp.setActivationConfigPropertyName("clientId"); // NOI18N
            clientIdProp.setActivationConfigPropertyValue(ejbName); // NOI18N
            config.addActivationConfigProperty(clientIdProp);
            
            ActivationConfigProperty subscriptionNameProp = config.newActivationConfigProperty();
            subscriptionNameProp.setActivationConfigPropertyName("subscriptionName"); // NOI18N
            subscriptionNameProp.setActivationConfigPropertyValue(ejbName); // NOI18N
            config.addActivationConfigProperty(subscriptionNameProp);
            
        }
        config.addActivationConfigProperty(destProp);
        messageDriven.setActivationConfig(config);
        messageDriven.setEjbName(ejbName);
        messageDriven.setDisplayName(displayName);
        messageDriven.setEjbClass(packageNameWithDot + ejbClassName);
        messageDriven.setTransactionType(MessageDriven.TRANSACTION_TYPE_CONTAINER);
        
        beans.addMessageDriven(messageDriven);
        // add transaction requirements
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        if (assemblyDescriptor == null) {
            assemblyDescriptor = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        }
        org.netbeans.modules.j2ee.dd.api.common.MessageDestination ddMessageDestination = assemblyDescriptor.newMessageDestination();
        String destinationLink = messageDestination.getName();
        ddMessageDestination.setDisplayName("Destination for " + displayName);
        ddMessageDestination.setMessageDestinationName(destinationLink);
        assemblyDescriptor.addMessageDestination(ddMessageDestination);
        
        messageDriven.setMessageDestinationLink(destinationLink);
        ContainerTransaction containerTransaction = assemblyDescriptor.newContainerTransaction();
        containerTransaction.setTransAttribute("Required"); //NOI18N
        Method method = containerTransaction.newMethod();
        method.setEjbName(ejbName);
        method.setMethodName("*"); //NOI18N
        containerTransaction.addMethod(method);
        assemblyDescriptor.addContainerTransaction(containerTransaction);
        ejbJar.write(ejbModule.getDeploymentDescriptor());
    }
    
    private void generateEJB30Xml() throws IOException {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    private boolean useMappedName() {
        // JavaEE7 platform should always use portable, compatible way if possible
        if (profile != null && profile.isAtLeast(Profile.JAVA_EE_7_WEB) && jmsSupport.useDestinationLookup()) {
            return false;
        } else {
            return jmsSupport.useMappedName();
        }
    }

    public static final class KeyValuePair {

        private String key;
        private String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
