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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Michal Mocnak
 */
public class Util {

    public static final String WAIT_NODE = "wait_node"; //NOI18N
    public static final String INFO_NODE = "info_node"; //NOI18N

    public static final String JDBC_RESOURCE_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/jdbc.gif"; // NOI18N
    public static final String CONNECTOR_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/connector.gif"; // NOI18N
    public static final String APPCLIENT_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/appclient.gif"; // NOI18N
    public static final String JAVAMAIL_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/javamail.gif";// NOI18N
    public static final String JAXRS_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/restservice.png";// NOI18N
    public static final String JAXRS_METHOD_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/method.png";// NOI18N
    public static final String JMS_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/jms.gif";// NOI18N
    public static final String RESOURCES_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/ResNodeNodeIcon.gif";// NOI18N

    public static final String EJB_MESSAGE_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/MessageBean.png";// NOI18N
    public static final String EJB_ENTITY_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/EntityBean.png";// NOI18N
    public static final String EJB_SESSION_ICON
            = "org/netbeans/modules/javaee/wildfly/resources/SessionBean.png";// NOI18N

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    public static Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(WildflyItemNode.class, "LBL_WaitNode_DisplayName")); //NOI18N
        n.setIconBaseWithExtension("org/netbeans/modules/javaee/wildfly/resources/wait.gif"); // NOI18N
        return n;
    }

    /* Creates and returns the instance of the node
     * representing the status 'INFO' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    public static Node createInfoNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(WildflyItemNode.class, "LBL_InfoNode_DisplayName")); //NOI18N
        n.setShortDescription(NbBundle.getMessage(WildflyItemNode.class, "LBL_InfoNode_ToolTip")); //NOI18N
        n.setIconBaseWithExtension("org/netbeans/core/resources/exception.gif"); // NOI18N
        return n;
    }

    public static Method fixJava4071957(Method method) {
        try {
            method.setAccessible(true);
            return method;
        } catch (SecurityException ex) {
            while (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                try {
                    method = method.getDeclaringClass().getSuperclass().getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
            }
            return method;
        }
    }

    /**
     * It only returns string representation of the ModuleType (accorded to the
     * JBoss JMX requirements)
     *
     * @return string representation of the ModuleType
     */
    public static String getModuleTypeString(ModuleType mt) {
        if (mt.equals(ModuleType.EAR)) {
            return "J2EEApplication";
        } else if (mt.equals(ModuleType.WAR)) {
            return "WebModule";
        } else if (mt.equals(ModuleType.EJB)) {
            return "EJBModule";
        }

        return "undefined";
    }

    /**
     * Returns MBean attribute which you can specify via method parameters
     *
     * @return MBean attribute
     */
    public static Object getMBeanParameter(MBeanServerConnection server, String name, String targetObject) {
        try {
            return server.getAttribute(new ObjectName(targetObject), name);
        } catch (InstanceNotFoundException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (AttributeNotFoundException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (MalformedObjectNameException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (NullPointerException ex) {
            // it's normal behaviour when the server is not running
            LOGGER.log(Level.FINE, null, ex);
        } catch (IllegalArgumentException ex) {
            // it's normal behaviour when the server is not running
            LOGGER.log(Level.FINE, null, ex);
        } catch (ReflectionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (MBeanException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    /**
     * Parse web application's deployment descriptor and returns context root.
     * According to the jboss specification, if no context root specification
     * exists, the context root will be the base name of the WAR file.
     *
     * @param descriptor deployment descriptor
     * @param warName name of the war
     * @return context-root of web application
     */
    public static String getWebContextRoot(String descriptor, String warName) {
        String context = getDescriptorContextRoot(descriptor);
        if (context == null) {
            context = getWarContextRoot(warName);
        }

        if ("/ROOT".equals(context)) { // NOI18N
            return "/"; // NOI18N
        }

        return context;
    }

    private static String getDescriptorContextRoot(String descriptor) {
        if (descriptor == null || "".equals(descriptor.trim())) {
            return null;
        }

        Document doc = null;

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(descriptor)));
        } catch (SAXException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }

        org.w3c.dom.Node node = doc.getElementsByTagName("context-root").item(0); // NOI18N

        if (node == null || node.getTextContent() == null) {
            return null;
        }

        String text = node.getTextContent();
        if (!text.startsWith("/")) {
            text = "/" + text;
        }

        return text;
    }

    private static String getWarContextRoot(String warName) {
        if (warName == null) {
            return null;
        }
        if (!warName.endsWith(".war")) {
            return "/" + warName;
        }

        return "/" + warName.substring(0, warName.lastIndexOf(".war"));
    }
}
