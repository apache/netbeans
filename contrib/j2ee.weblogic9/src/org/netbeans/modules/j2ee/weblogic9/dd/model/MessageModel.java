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
package org.netbeans.modules.j2ee.weblogic9.dd.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.schema2beans.NullEntityResolver;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Hejl
 */
public final class MessageModel extends BaseDescriptorModel {

    private static final Pattern SCHEMA_1031 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-jms/1\\.[0-3]/weblogic-jms\\.xsd"); // NOI18N

    private static final Pattern SCHEMA_1211 = Pattern.compile("http://xmlns\\.oracle\\.com/weblogic/weblogic-jms/1\\.[4]/weblogic-jms\\.xsd"); // NOI18N

    private final WeblogicJms bean;

    private MessageModel(WeblogicJms bean) {
        super(bean);
        this.bean = bean;
    }

    public static MessageModel forFile(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            return forInputStream(is);
        } finally {
            is.close();
        }
    }

    public static MessageModel forInputStream(InputStream is) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);

        Document doc;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(NullEntityResolver.newInstance());
            doc = builder.parse(is);
        } catch (SAXException ex) {
            throw new RuntimeException(NbBundle.getMessage(EarApplicationModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(NbBundle.getMessage(EarApplicationModel.class, "MSG_CantCreateXMLDOMDocument"), ex);
        }

        String value = doc.getDocumentElement().getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"); // NOI18N
        if (SCHEMA_1031.matcher(value).matches()) {
            return new MessageModel(org.netbeans.modules.j2ee.weblogic9.dd.jms1031.WeblogicJms.createGraph(doc));
        } else if (SCHEMA_1211.matcher(value).matches()) {
            return new MessageModel(org.netbeans.modules.j2ee.weblogic9.dd.jms1211.WeblogicJms.createGraph(doc));
        } else {
            return new MessageModel(org.netbeans.modules.j2ee.weblogic9.dd.jms1211.WeblogicJms.createGraph(doc));
        }
    }

    public static MessageModel generate(@NullAllowed Version serverVersion) {
        if (serverVersion != null) {
            if (serverVersion.isAboveOrEqual(VERSION_12_1_1)) {
                return generate1211();
            } else if (serverVersion.isAboveOrEqual(VERSION_10_3_1)) {
                return generate1031();
            }
        }
        return generate1031();
    }

    public List<MessageDestination> getMessageDestinations(
            org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type type, boolean includeLocal) {
        List<MessageDestination> ret = new ArrayList<MessageDestination>();
        switch (type) {
            case QUEUE:
                for (QueueType qType : bean.getQueue()) {
                    String jndiName = qType.getJndiName();
                    if (jndiName == null && includeLocal) {
                        jndiName = qType.getLocalJndiName();
                    }
                    if (jndiName != null) {
                        MessageDestination element = new MessageDestination(
                                qType.getName(), jndiName, type);
                        ret.add(element);
                    }
                }
                break;
            case TOPIC:
                for (TopicType tType : bean.getTopic()) {
                    String jndiName = tType.getJndiName();
                    if (jndiName == null && includeLocal) {
                        jndiName = tType.getLocalJndiName();
                    }
                    if (jndiName != null) {
                        MessageDestination element = new MessageDestination(
                                tType.getName(), tType.getJndiName(), type);
                        ret.add(element);
                    }
                }
                break;
            default:
        }

        return ret;
    }

    public void addMessageDestination(MessageDestination destination) {
        switch (destination.getType()) {
            case QUEUE:
                QueueType qType = bean.addQueue();
                qType.setName(destination.getResourceName());
                qType.setJndiName(destination.getJndiName());
                break;
            case TOPIC:
                TopicType tType = bean.addTopic();
                tType.setName(destination.getResourceName());
                tType.setJndiName(destination.getJndiName());
                break;
            default:
        }
    }

    private static MessageModel generate1031() {
        org.netbeans.modules.j2ee.weblogic9.dd.jms1031.WeblogicJms webLogicJms = new org.netbeans.modules.j2ee.weblogic9.dd.jms1031.WeblogicJms();
        webLogicJms.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicJms.setAttributeValue("xsi:schemaLocation", "http://xmlns.oracle.com/weblogic/weblogic-jms http://xmlns.oracle.com/weblogic/weblogic-jms/1.0/weblogic-jms.xsd"); // NOI18N
        return new MessageModel(webLogicJms);
    }

    private static MessageModel generate1211() {
        org.netbeans.modules.j2ee.weblogic9.dd.jms1211.WeblogicJms webLogicJms = new org.netbeans.modules.j2ee.weblogic9.dd.jms1211.WeblogicJms();
        webLogicJms.setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        webLogicJms.setAttributeValue("xsi:schemaLocation", "http://xmlns.oracle.com/weblogic/weblogic-jms http://xmlns.oracle.com/weblogic/weblogic-jms/1.4/weblogic-jms.xsd"); // NOI18N
        return new MessageModel(webLogicJms);
    }

    public static class MessageDestination {

        private final String resourceName;

        private final String jndiName;

        private final org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type type;

        public MessageDestination(String resourceName, String jndiName, Type type) {
            this.resourceName = resourceName;
            this.jndiName = jndiName;
            this.type = type;
        }

        public String getJndiName() {
            return jndiName;
        }

        public String getResourceName() {
            return resourceName;
        }

        public Type getType() {
            return type;
        }
    }
}
