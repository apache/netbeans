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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.JBRemoteAction;
import org.netbeans.modules.j2ee.jboss4.JBoss5ProfileServiceProxy;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
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

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    public static Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(JBItemNode.class, "LBL_WaitNode_DisplayName")); //NOI18N
        n.setIconBaseWithExtension("org/netbeans/modules/j2ee/jboss4/resources/wait.gif"); // NOI18N
        return n;
    }

    /* Creates and returns the instance of the node
     * representing the status 'INFO' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    public static Node createInfoNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(JBItemNode.class, "LBL_InfoNode_DisplayName")); //NOI18N
        n.setShortDescription(NbBundle.getMessage(JBItemNode.class, "LBL_InfoNode_ToolTip")); //NOI18N
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
     * Checks if the Jboss installation has installed remote management package
     *
     * @return is remote management supported
     */
    public static boolean isRemoteManagementSupported(Lookup lookup) {
        JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
        if (dm == null) {
            return false;
        }
        try {
            dm.invokeRemoteAction(new JBRemoteAction<Boolean>() {

                @Override
                public Boolean action(MBeanServerConnection connection, JBoss5ProfileServiceProxy profileService) throws Exception {
                    // FIXME is this refletion needed
                    ObjectName searchPattern = new ObjectName("jboss.management.local:*");
                    Method method = connection.getClass().getMethod("queryMBeans", new Class[]{ObjectName.class, QueryExp.class});
                    method = fixJava4071957(method);
                    Set managedObj = (Set) method.invoke(connection, new Object[]{searchPattern, null});

                    return !managedObj.isEmpty();
                }
            });
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        return true;
    }

    /**
     * Checks if the specified object is deployed in JBoss Application Server
     *
     * @return if specified object is deployed
     */
    public static boolean isObjectDeployed(JBDeploymentManager dm, final ObjectName searchPattern) {
        try {
            dm.invokeRemoteAction(new JBRemoteAction<Boolean>() {

                @Override
                public Boolean action(MBeanServerConnection connection, JBoss5ProfileServiceProxy profileService) throws Exception {
                    // FIXME is this reflection really needed
                    Method method = connection.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class});
                    method = fixJava4071957(method);
                    Set managedObj = (Set) method.invoke(connection, new Object[] {searchPattern, null});

                    return managedObj.size() > 0;
                }

            });
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        return false;
    }

    /**
     * It only returns string representation of the ModuleType (accorded to the JBoss JMX requirements)
     *
     * @return string representation of the ModuleType
     */
    public static String getModuleTypeString(ModuleType mt) {
        if(mt.equals(ModuleType.EAR))
            return "J2EEApplication";
        else if(mt.equals(ModuleType.WAR))
            return "WebModule";
        else if(mt.equals(ModuleType.EJB))
            return "EJBModule";

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
     * According to the jboss specification, if no context root specification exists,
     * the context root will be the base name of the WAR file.
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
