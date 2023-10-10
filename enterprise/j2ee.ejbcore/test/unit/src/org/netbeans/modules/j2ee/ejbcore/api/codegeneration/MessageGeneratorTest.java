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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MdbPropertiesPanelVisual;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.javaee.specs.support.api.JmsSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class MessageGeneratorTest extends TestBase {
    
    public MessageGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateJavaEE14() throws IOException, VersionNotSupportedException {
        TestModule testModule = createEjb21Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE14");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE14");

        // Queue based MessageDriven EJB in Java EE 1.4
        
        MessageDestination messageDestination = new MessageDestinationImpl("TestMDBQueue", MessageDestination.Type.QUEUE);
        MessageGenerator generator = new MessageGenerator(Profile.J2EE_14, "TestMDBQueueBean", packageFileObject,
                messageDestination, false, Collections.<String, String>emptyMap(), JmsSupport.getInstance(null), true);
        generator.generate();
        
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        MessageDriven messageDriven = (MessageDriven) enterpriseBeans.findBeanByName(
                EnterpriseBeans.MESSAGE_DRIVEN, MessageDriven.EJB_NAME, "TestMDBQueueBean");
        assertNotNull(messageDriven);
        assertEquals("TestMDBQueueBeanMDB", messageDriven.getDefaultDisplayName());
        assertEquals("TestMDBQueueBean", messageDriven.getEjbName());
        assertEquals("testGenerateJavaEE14.TestMDBQueueBean", messageDriven.getEjbClass());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("javax.jms.Queue", messageDriven.getMessageDestinationType());
        assertEquals("TestMDBQueue", messageDriven.getMessageDestinationLink());
        ActivationConfig activationConfig = messageDriven.getActivationConfig();
        assertEquals(2, activationConfig.getActivationConfigProperty().length);
        ActivationConfigProperty acProperty = activationConfig.getActivationConfigProperty()[0];
        assertEquals("acknowledgeMode", acProperty.getActivationConfigPropertyName());
        assertEquals("Auto-acknowledge", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[1];
        assertEquals("destinationType", acProperty.getActivationConfigPropertyName());
        assertEquals("javax.jms.Queue", acProperty.getActivationConfigPropertyValue());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBQueueBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestMDBQueueBean.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Topic based MessageDriven EJB in Java EE 1.4
        
        messageDestination = new MessageDestinationImpl("TestMDBTopic", MessageDestination.Type.TOPIC);
        generator = new MessageGenerator(Profile.J2EE_14, "TestMDBTopicBean", packageFileObject, messageDestination,
                false, Collections.<String, String>emptyMap(), JmsSupport.getInstance(null), true);
        generator.generate();
        
        messageDriven = (MessageDriven) enterpriseBeans.findBeanByName(
                EnterpriseBeans.MESSAGE_DRIVEN, MessageDriven.EJB_NAME, "TestMDBTopicBean");
        assertNotNull(messageDriven);
        assertEquals("TestMDBTopicBeanMDB", messageDriven.getDefaultDisplayName());
        assertEquals("TestMDBTopicBean", messageDriven.getEjbName());
        assertEquals("testGenerateJavaEE14.TestMDBTopicBean", messageDriven.getEjbClass());
        assertEquals("Container", messageDriven.getTransactionType());
        assertEquals("javax.jms.Topic", messageDriven.getMessageDestinationType());
        assertEquals("TestMDBTopic", messageDriven.getMessageDestinationLink());
        activationConfig = messageDriven.getActivationConfig();
        assertEquals(5, activationConfig.getActivationConfigProperty().length);
        acProperty = activationConfig.getActivationConfigProperty()[0];
        assertEquals("acknowledgeMode", acProperty.getActivationConfigPropertyName());
        assertEquals("Auto-acknowledge", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[1];
        assertEquals("subscriptionDurability", acProperty.getActivationConfigPropertyName());
        assertEquals("Durable", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[2];
        assertEquals("clientId", acProperty.getActivationConfigPropertyName());
        assertEquals("TestMDBTopicBean", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[3];
        assertEquals("subscriptionName", acProperty.getActivationConfigPropertyName());
        assertEquals("TestMDBTopicBean", acProperty.getActivationConfigPropertyValue());
        acProperty = activationConfig.getActivationConfigProperty()[4];
        assertEquals("destinationType", acProperty.getActivationConfigPropertyName());
        assertEquals("javax.jms.Topic", acProperty.getActivationConfigPropertyValue());
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBTopicBean.java")), 
                getGoldenFile("testGenerateJavaEE14/TestMDBTopicBean.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // added by both previous generators
        
        AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
        List<String> messageDestinationNames = new ArrayList<String>();
        for (org.netbeans.modules.j2ee.dd.api.common.MessageDestination msgDest : assemblyDescriptor.getMessageDestination()) {
            messageDestinationNames.add(msgDest.getMessageDestinationName());
        }
        assertEquals(2, assemblyDescriptor.getMessageDestination().length);
        assertTrue(messageDestinationNames.contains("TestMDBQueue"));
        assertTrue(messageDestinationNames.contains("TestMDBTopic"));
    }
    
    public void testGenerateJavaEE50() throws IOException {
        TestModule testModule = createEjb30Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE50");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE50");
        
        // Queue based MessageDriven EJB in Java EE 5 defined in annotation
        MessageDestination messageDestination = new MessageDestinationImpl("TestMessageDestination", MessageDestination.Type.QUEUE);
        J2eeProjectCapabilities j2eeProjectCapabilities = J2eeProjectCapabilities.forProject(testModule.getProject());
        MdbPropertiesPanelVisual panel = new MdbPropertiesPanelVisual(j2eeProjectCapabilities);
        panel.setDefaultProperties(messageDestination);
        Map<String, String> properties = panel.getProperties();
        MessageGenerator generator = new MessageGenerator(Profile.JAVA_EE_5, "TestMDBQueueBean", packageFileObject,
                messageDestination, true, properties, JmsSupport.getInstance(null), true);
        generator.generate();
        
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBQueueBean.java")), 
                getGoldenFile("testGenerateJavaEE50/TestMDBQueueBean.java"), 
                FileUtil.toFile(packageFileObject)
                );

        // Topic based MessageDriven EJB in Java EE 5 defined in annotation
        messageDestination = new MessageDestinationImpl("TestMDBTopicBean", MessageDestination.Type.TOPIC);
        panel.setDefaultProperties(messageDestination);
        properties = panel.getProperties();
        generator = new MessageGenerator(Profile.JAVA_EE_5, "TestMDBTopicBean", packageFileObject, messageDestination,
                true, properties, JmsSupport.getInstance(null), true);
        generator.generate();
        
        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBTopicBean.java")),
                getGoldenFile("testGenerateJavaEE50/TestMDBTopicBean.java"),
                FileUtil.toFile(packageFileObject)
                );
    }

    public void testGenerateJavaEE70() throws IOException {
        TestModule testModule = createEjb32Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE70");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE70");

        // Queue based MessageDriven EJB in Java EE 7 defined in annotation
        MessageDestination messageDestination = new MessageDestinationImpl("TestMessageDestination", MessageDestination.Type.QUEUE);
        J2eeProjectCapabilities j2eeProjectCapabilities = J2eeProjectCapabilities.forProject(testModule.getProject());
        MdbPropertiesPanelVisual panel = new MdbPropertiesPanelVisual(j2eeProjectCapabilities);
        panel.setDefaultProperties(messageDestination);
        Map<String, String> properties = panel.getProperties();

        MessageGenerator generator = new MessageGenerator(Profile.JAVA_EE_7_FULL, "TestMDBQueueBean", packageFileObject,
                messageDestination, true, properties, JmsSupport.getInstance(null), true);
        generator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBQueueBean.java")),
                getGoldenFile("testGenerateJavaEE70/TestMDBQueueBean.java"),
                FileUtil.toFile(packageFileObject)
                );

        // Topic based MessageDriven EJB in Java EE 7 defined in annotation
        messageDestination = new MessageDestinationImpl("TestMessageDestination", MessageDestination.Type.TOPIC);
        panel.setDefaultProperties(messageDestination);
        panel.setProperty(org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.ActivationConfigProperties.ACKNOWLEDGE_MODE, org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.ActivationConfigProperties.AcknowledgeMode.DUPS_OK_ACKNOWLEDGE);
        panel.setProperty(org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.ActivationConfigProperties.CONNECTION_FACTORY_LOOKUP, "factoryLookup");
        panel.setProperty(org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.ActivationConfigProperties.MESSAGE_SELECTOR, "selector");
        properties = panel.getProperties();
        generator = new MessageGenerator(Profile.JAVA_EE_7_FULL, "TestMDBTopicBean", packageFileObject, messageDestination, true, properties, JmsSupport.getInstance(null), true);
        generator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBTopicBean.java")),
                getGoldenFile("testGenerateJavaEE70/TestMDBTopicBean.java"),
                FileUtil.toFile(packageFileObject)
                );
    }

    public void testGenerateJavaEE70AnotherDestinationLookup() throws IOException {
        TestModule testModule = createEjb32Module();
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject("testGenerateJavaEE70");
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder("testGenerateJavaEE70");

        // Queue based MessageDriven EJB in Java EE 7 defined in annotation
        MessageDestination messageDestination = new MessageDestinationImpl("TestMessageDestination2", MessageDestination.Type.QUEUE);
        J2eeProjectCapabilities j2eeProjectCapabilities = J2eeProjectCapabilities.forProject(testModule.getProject());
        MdbPropertiesPanelVisual panel = new MdbPropertiesPanelVisual(j2eeProjectCapabilities);
        panel.setDefaultProperties(messageDestination);
        panel.setProperty(org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.ActivationConfigProperties.DESTINATION_LOOKUP, "TestMessageDestination2");
        Map<String, String> properties = panel.getProperties();

        MessageGenerator generator = new MessageGenerator(Profile.JAVA_EE_7_FULL, "TestMDBQueueBean2", packageFileObject,
                messageDestination, true, properties, JmsSupport.getInstance(null), true);
        generator.generate();

        assertFile(
                FileUtil.toFile(packageFileObject.getFileObject("TestMDBQueueBean2.java")),
                getGoldenFile("testGenerateJavaEE70/TestMDBQueueBean2.java"),
                FileUtil.toFile(packageFileObject)
                );
    }

    private static final class MessageDestinationImpl implements MessageDestination {

        private final String name;
        private final MessageDestination.Type type;
        
        public MessageDestinationImpl(String name, MessageDestination.Type type) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }
        
    }

}
