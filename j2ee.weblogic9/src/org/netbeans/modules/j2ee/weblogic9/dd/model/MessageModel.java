/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
