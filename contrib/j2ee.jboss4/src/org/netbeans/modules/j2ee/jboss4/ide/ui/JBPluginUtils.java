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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.openide.filesystems.JarFileSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBPluginUtils {

    public static final String SERVER_4_XML = File.separator + "deploy" + File.separator + // NOI18N
                "jbossweb-tomcat55.sar" + File.separator + "server.xml"; // NOI18N

    public static final String SERVER_4_2_XML = File.separator + "deploy" + File.separator + // NOI18N
                "jboss-web.deployer" + File.separator + "server.xml"; // NOI18N

//    public static final String SERVER_5_XML = File.separator + "deployers" + File.separator + // NOI18N
//                "jbossweb.deployer" + File.separator + "server.xml"; // NOI18N

     public static final String SERVER_5_XML = File.separator + "deploy" + File.separator + // NOI18N
                "jbossweb.sar" + File.separator + "server.xml"; // NOI18N

    public static final Version JBOSS_5_0_0 = new Version("5.0.0"); // NOI18N
    
    public static final Version JBOSS_5_0_1 = new Version("5.0.1"); // NOI18N

    public static final Version JBOSS_6_0_0 = new Version("6.0.0"); // NOI18N

    public static final Version JBOSS_7_0_0 = new Version("7.0.0"); // NOI18N

    public static final Version JBOSS_7_1_0 = new Version("7.1.0"); // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(JBPluginUtils.class.getName());

    private static final Version DOM4J_SERVER = new Version("4.0.4"); // NOI18N

    public static final String LIB = "lib" + File.separator;

    public static final String MODULES_BASE = "modules" + File.separator;

    public static final String MODULES_BASE_7 = "modules" + File.separator + "system"
            + File.separator + "layers" + File.separator + "base" + File.separator;

    public static final String CLIENT = "client" + File.separator;

    public static final String COMMON = "common" + File.separator;

    // For JBoss 5.0 under JBOSS_ROOT_DIR/lib
    public static final String[] JBOSS5_CLIENT_LIST = {
        "javassist.jar",
        "jbossall-client.jar",
        "jboss-deployment.jar",
        "jnp-client.jar",
        "jbosssx-client.jar",
        "jboss-client.jar",
        "jboss-common-core.jar",
        "jboss-logging-log4j.jar",
        "jboss-logging-spi.jar"
    };

    public static  List<URL> getJB5ClientClasspath(String serverRoot) throws
            MalformedURLException {

        List<URL> urlList = new ArrayList<URL>();

        File clientDir = new File(serverRoot, JBPluginUtils.CLIENT);
        if (clientDir.exists()) {

            for (String jar : JBPluginUtils.JBOSS5_CLIENT_LIST) {
                File jarFile = new File(clientDir, jar);
                if (jarFile.exists()) {
                    urlList.add(jarFile.toURI().toURL());
                }
            }
        }
        return urlList;
    }

    //--------------- checking for possible domain directory -------------
    private static List<String> domainRequirements4x;

    private static synchronized List<String> getDomainRequirements4x() {
        if (domainRequirements4x == null) {
            domainRequirements4x = new ArrayList<String>(13);
            Collections.addAll(domainRequirements4x,
                    "conf", // NOI18N
                    "deploy", // NOI18N
                    "lib", // NOI18N
                    "conf/jboss-service.xml", // NOI18N
                    "lib/jboss-j2ee.jar", // NOI18N
                    "lib/jboss.jar", // NOI18N
                    "lib/jbosssx.jar", // NOI18N
                    "lib/jboss-transaction.jar", // NOI18N
                    "lib/jmx-adaptor-plugin.jar", // NOI18N
                    "lib/jnpserver.jar", // NOI18N
                    "lib/log4j.jar", // NOI18N
                    // not present in 4.0.0
                    //"lib/xmlentitymgr.jar", // NOI18N
                    "deploy/jmx-invoker-service.xml"); // NOI18N
        }
        return domainRequirements4x;
    }

    private static List<String> domainRequirements5x;

    private static synchronized List<String> getDomainRequirements5x() {
        if (domainRequirements5x == null) {
            domainRequirements5x = new ArrayList<String>(11);
            Collections.addAll(domainRequirements5x,
                    "conf", // NOI18N
                    "deploy", // NOI18N
                    "deployers", // NOI18N
                    "lib", // NOI18N
                    "conf/jboss-service.xml", // NOI18N
                    "conf/bootstrap.xml", // NOI18N
                    "deploy/jmx-invoker-service.xml"   // NOI18N
                    );
        }
        return domainRequirements5x;
    }

    private static List<String> domainRequirements6x;

    private static synchronized List<String> getDomainRequirements6x() {
        if (domainRequirements6x == null) {
            domainRequirements6x = new ArrayList<String>(11);
            Collections.addAll(domainRequirements6x,
                    "conf", // NOI18N
                    "deploy", // NOI18N
                    "deployers", // NOI18N
                    "lib", // NOI18N
                    "conf/jboss-service.xml", // NOI18N
                    "conf/bootstrap.xml", // NOI18N
                    "deploy/hdscanner-jboss-beans.xml" // NOI18N
                    );
        }
        return domainRequirements6x;
    }

    private static List<String> domainRequirements7x;

    private static synchronized List<String> getDomainRequirements7x() {
        if (domainRequirements7x == null) {
            domainRequirements7x = new ArrayList<String>(11);
            Collections.addAll(domainRequirements7x,
                    "configuration", // NOI18N
                    "deployments", // NOI18N
                    "lib" // NOI18N
                    );
        }
        return domainRequirements7x;
    }


    //--------------- checking for possible server directory -------------
    private static List<String> serverRequirements4x;

    private static synchronized List<String> getServerRequirements4x() {
        if (serverRequirements4x == null) {
            serverRequirements4x = new ArrayList<String>(6);
            Collections.addAll(serverRequirements4x,
                    "bin", // NOI18N
                    "client", // NOI18N
                    "lib", // NOI18N
                    "server", // NOI18N
                    "lib/jboss-common.jar", // NOI18N
                    "lib/endorsed/resolver.jar"); // NOI18N
        }
        return serverRequirements4x;
    }

    private static List<String> serverAlterRequirements4x;

    private static synchronized List<String> getServerAlterRequirements4x() {
        if (serverAlterRequirements4x == null) {
            serverAlterRequirements4x = new ArrayList<String>(8);
            Collections.addAll(serverAlterRequirements4x,
                    "bin", // NOI18N
                    "client", // NOI18N
                    "lib", // NOI18N
                    "server", // NOI18N
                    "lib/jboss-common.jar", // NOI18N
                    "client/jaxb-xjc.jar", // NOI18N
                    "client/jaxb-impl.jar", // NOI18N
                    "client/jaxb-api.jar"); // NOI18N
        }
        return serverAlterRequirements4x;
    }

    private static List<String> serverRequirements5And6x;

    private static synchronized List<String> getServerRequirements5And6x() {
        if (serverRequirements5And6x == null) {
            serverRequirements5And6x = new ArrayList<String>(6);
            Collections.addAll(serverRequirements5And6x,
                    "bin", // NOI18N
                    "client", // NOI18N
                    "lib", // NOI18N
                    "server", // NOI18N
                    "common/lib", // NOI18N
                    "lib/dom4j.jar", // NOI18N
                    "lib/jboss-dependency.jar", // NOI18N
                    "lib/jboss-common-core.jar", // NOI18N
                    "lib/endorsed"); // NOI18N
        }
        return serverRequirements5And6x;
    }

    private static List<String> serverRequirements7x;
    private static synchronized List<String> getServerRequirements7x() {
        if (serverRequirements7x == null) {
            serverRequirements7x = new ArrayList<String>(6);
            Collections.addAll(serverRequirements7x,
                    "bin", // NOI18N
                    "modules", // NOI18N
                    "jboss-modules.jar"); // NOI18N
        }
        return serverRequirements7x;
    }

    @NonNull
    public static String getModulesBase(String serverRoot) {
        File file = new File(serverRoot, MODULES_BASE_7);
        if (file.isDirectory()) {
            return MODULES_BASE_7;
        }
        return MODULES_BASE;
    }

    //------------  getting exists servers---------------------------
    /**
     * returns Hashmap
     * key = server name
     * value = server folder full path
     */
    public static Hashtable getRegisteredDomains(String serverLocation){
        Hashtable result = new Hashtable();
        //  String domainListFile = File.separator+"common"+File.separator+"nodemanager"+File.separator+"nodemanager.domains";  // NOI18N

        File serverDirectory = new File(serverLocation);

        if (isGoodJBServerLocation(serverDirectory, (Version) null)) {
            Version version = getServerVersion(serverDirectory);            
            File file;
            String[] files;
            if(version != null && "7".equals(version.getMajorNumber())) {
                files = new String[]{"standalone", "domain"};
                file = serverDirectory;
            } else {
                file = new File(serverLocation + File.separator + "server");  // NOI18N
                files = file.list(new FilenameFilter(){
                    @Override
                    public boolean accept(File dir, String name){
                        if ((new File(dir.getAbsolutePath()+File.separator+name)).isDirectory()) return true;
                        return false;
                    }
                });
            }

            if (files != null) {
                for (int i = 0; i<files.length; i++) {
                    String path = file.getAbsolutePath() + File.separator + files[i];

                    if (isGoodJBInstanceLocation(serverDirectory, new File(path))) {
                        result.put(files[i], path);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isGoodJBInstanceLocation(File candidate, List<String> requirements){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, requirements)) {
            return false;
        }
        return true;
    }

    private static boolean isGoodJBInstanceLocation4x(File serverDir, File candidate) {
        if (!isGoodJBInstanceLocation(candidate, getDomainRequirements4x())) {
            return false;
        }
        Version version = getServerVersion(serverDir);
        if (version == null) {
            // optimistic expectation
            return true;
        }

        if (version.compareToIgnoreUpdate(DOM4J_SERVER) > 0) {
            // in server lib
            File dom4j = new File(candidate, "lib/dom4j.jar"); // NOI18N
            return dom4j.exists() && dom4j.canRead();
        }
        return true;
    }

    private static boolean isGoodJBInstanceLocation5x(File serverDir, File candidate){
        return isGoodJBInstanceLocation(candidate, getDomainRequirements5x());
    }

    private static boolean isGoodJBInstanceLocation6x(File serverDir, File candidate){
        return isGoodJBInstanceLocation(candidate, getDomainRequirements6x());
    }

    private static boolean isGoodJBInstanceLocation7x(File serverDir, File candidate){
        return isGoodJBInstanceLocation(candidate, getDomainRequirements7x());
    }

    public static boolean isGoodJBInstanceLocation(File serverDir, File candidate){
        Version version = getServerVersion(serverDir);
        if (version == null || (!"4".equals(version.getMajorNumber())
                && !"5".equals(version.getMajorNumber()) // NOI18N
                && !"6".equals(version.getMajorNumber()) // NOI18N
                && !"7".equals(version.getMajorNumber()))) { // NOI18N
            return JBPluginUtils.isGoodJBInstanceLocation4x(serverDir, candidate)
                    || JBPluginUtils.isGoodJBInstanceLocation5x(serverDir, candidate)
                    || JBPluginUtils.isGoodJBInstanceLocation6x(serverDir, candidate)
                    || JBPluginUtils.isGoodJBInstanceLocation7x(serverDir, candidate);
        }

        return ("4".equals(version.getMajorNumber()) && JBPluginUtils.isGoodJBInstanceLocation4x(serverDir, candidate)) // NOI18N
                || ("5".equals(version.getMajorNumber()) && JBPluginUtils.isGoodJBInstanceLocation5x(serverDir, candidate)) // NOI18N
                || ("6".equals(version.getMajorNumber()) && JBPluginUtils.isGoodJBInstanceLocation6x(serverDir, candidate)) // NOI18N
                || ("7".equals(version.getMajorNumber()) && JBPluginUtils.isGoodJBInstanceLocation7x(serverDir, candidate)); // NOI18N
    }

    private static boolean isGoodJBServerLocation(File candidate, List<String> requirements){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, requirements)) {
            return false;
        }
        return true;
    }

    private static boolean isGoodJBServerLocation4x(File candidate) {
        if (!isGoodJBServerLocation(candidate, getServerRequirements4x())
                && !isGoodJBServerLocation(candidate, getServerAlterRequirements4x())) {
            return false;
        }
        Version version = getServerVersion(candidate);
        if (version == null) {
            // optimistic expectation
            return true;
        }

        if (version.compareToIgnoreUpdate(DOM4J_SERVER) <= 0) {
            // in server lib
            File dom4j = new File(candidate, "lib/dom4j.jar"); // NOI18N
            return dom4j.exists() && dom4j.canRead();
        }
        return true;
    }

    private static boolean isGoodJBServerLocation5x(File candidate){
        return isGoodJBServerLocation(candidate, getServerRequirements5And6x());
    }

    private static boolean isGoodJBServerLocation6x(File candidate){
        return isGoodJBServerLocation(candidate, getServerRequirements5And6x());
    }

    private static boolean isGoodJBServerLocation7x(File candidate){
        return isGoodJBServerLocation(candidate, getServerRequirements7x());
    }

    public static boolean isGoodJBServerLocation(@NonNull File candidate, @NullAllowed Version version) {
        Version realVersion = version;
        if (realVersion == null) {
            realVersion = getServerVersion(candidate);
        }
        if (realVersion == null || (!"4".equals(realVersion.getMajorNumber())
                && !"5".equals(realVersion.getMajorNumber())
                && !"6".equals(realVersion.getMajorNumber())
                && !"7".equals(realVersion.getMajorNumber()))) { // NOI18N
            return JBPluginUtils.isGoodJBServerLocation4x(candidate)
                    || JBPluginUtils.isGoodJBServerLocation5x(candidate)
                    || JBPluginUtils.isGoodJBServerLocation5x(candidate)
                    || JBPluginUtils.isGoodJBServerLocation7x(candidate);
        }

        return ("4".equals(realVersion.getMajorNumber()) && JBPluginUtils.isGoodJBServerLocation4x(candidate)) // NOI18n
                || ("5".equals(realVersion.getMajorNumber()) && JBPluginUtils.isGoodJBServerLocation5x(candidate)) // NOI18N
                || ("6".equals(realVersion.getMajorNumber()) && JBPluginUtils.isGoodJBServerLocation6x(candidate)) // NOI18N
                || ("7".equals(realVersion.getMajorNumber()) && JBPluginUtils.isGoodJBServerLocation7x(candidate)); // NOI18N
    }

    public static boolean isJB4(JBDeploymentManager dm) {
        String installDir = dm.getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
        Version version = getServerVersion(new File(installDir));
        if (version == null) {
            return isGoodJBServerLocation4x(new File(installDir));
        }

        return "4".equals(version.getMajorNumber()); // NOI18N
    }

    public static boolean isGoodJBLocation(File server, File domain) {
        return (JBPluginUtils.isGoodJBServerLocation4x(server)
                && JBPluginUtils.isGoodJBInstanceLocation4x(server, domain))
                    || (JBPluginUtils.isGoodJBServerLocation5x(server)
                        && JBPluginUtils.isGoodJBInstanceLocation5x(server, domain));
    }

    /**
     * Checks whether the given candidate has all required childrens. Children
     * can be both files and directories. Method does not distinguish between them.
     *
     * @return true if the candidate has all files/directories named in requiredChildren,
     *             false otherwise
     */
    private static boolean hasRequiredChildren(File candidate, List<String> requiredChildren) {
        if (null == candidate || null == candidate.list()) {
            return false;
        }
        if (null == requiredChildren) {
            return true;
        }

        for (String next : requiredChildren) {
            File test = new File(candidate.getPath() + File.separator + next);
            if (!test.exists()) {
                return false;
            }
        }
        return true;
    }

    //--------------------------------------------------------------------

    /**
     *
     *
     */
    public static String getDeployDir(String domainDir){
        Version version = JBPluginUtils.getServerVersion(new File(JBPluginProperties.getInstance().getInstallLocation()));
        if("7".equals(version.getMajorNumber())) {
            return domainDir + File.separator + "deployments"; //NOI18N
        }
        return domainDir + File.separator + "deploy"; //NOI18N
        //todo: get real deploy path
    }

    public static String getHTTPConnectorPort(String domainDir) {
        String defaultPort = "8080"; // NOI18N

        /*
         * Following block is trying to solve different server versions.
         */
        File serverXmlFile = new File(domainDir + SERVER_4_XML);
        if (!serverXmlFile.exists()) {
            serverXmlFile = new File(domainDir + SERVER_4_2_XML);
            if (!serverXmlFile.exists()) {
                serverXmlFile = new File(domainDir + SERVER_5_XML);
                if (!serverXmlFile.exists()) {
                    return defaultPort;
                }
            }
        }

        InputStream inputStream = null;
        Document document = null;
        try {
            inputStream = new FileInputStream(serverXmlFile);
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            } finally {
                inputStream.close();
            }

            // get the root element
            Element root = document.getDocumentElement();

            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("Service")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++) {
                        Node ch = nl.item(j);

                        if (ch.getNodeName().equals("Connector")) {  // NOI18N
                            String port = ch.getAttributes().getNamedItem("port").getNodeValue();
                            if (port.startsWith("$")) {
                                // FIXME check properties somehow
                                return defaultPort;
                            }
                            try {
                                Integer.parseInt(port);
                                return port;
                            } catch (NumberFormatException ex) {
                                return defaultPort;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
            // it is ok
            // it optional functionality so we don't need to look at any exception
        }

        return defaultPort;
    }

    public static int getJnpPortNumber(String domainDir) {
        String jnpPort = getJnpPort(domainDir);
        if(jnpPort != null) {
            jnpPort = jnpPort.trim();
            if (jnpPort.length() > 0) {
                try {
                    return Integer.parseInt(jnpPort);
                } catch(NumberFormatException e) {
                    // pass through to default
                }
            }
        }  
        return 1099;
    }

    public static int getJmxPortNumber(JBProperties jb, InstanceProperties ip) {
        String strPort = ip.getProperty(JBPluginProperties.PROPERTY_JMX_PORT);
        if (strPort == null || strPort.trim().isEmpty()) {
            return getDefaultJmxPortNumber(jb.getServerVersion());
        }
        try {
            return Integer.parseInt(strPort.trim());
        } catch(NumberFormatException e) {
            // pass through to default
        }
        return getDefaultJmxPortNumber(jb.getServerVersion());
    }

    public static String getJnpPort(String domainDir) {

        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";

        InputStream inputStream = null;
        Document document = null;
        try {
            inputStream = new FileInputStream(xmlFile);
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            } finally {
                inputStream.close();
            }

            // get the root element
            Element root = document.getDocumentElement();

            // get the child nodes
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("mbean")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    if (!child.getAttributes().getNamedItem("name").getNodeValue().equals("jboss:service=Naming")) //NOI18N
                        continue;
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);

                        if (ch.getNodeName().equals("attribute")) {  // NOI18N
                            if (!ch.getAttributes().getNamedItem("name").getNodeValue().equals("Port")) //NOI18N
                                continue;
                             return ch.getFirstChild().getNodeValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return "";
    }

    public static String getRMINamingServicePort(String domainDir){

        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";

        InputStream inputStream = null;
        Document document = null;
        try {
            inputStream = new FileInputStream(xmlFile);
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            } finally {
                inputStream.close();
            }

            // get the root element
            Element root = document.getDocumentElement();

            // get the child nodes
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("mbean")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    if (!child.getAttributes().getNamedItem("name").getNodeValue().equals("jboss:service=Naming")) //NOI18N
                        continue;
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);

                        if (ch.getNodeName().equals("attribute")) {  // NOI18N
                            if (!ch.getAttributes().getNamedItem("name").getNodeValue().equals("RmiPort")) //NOI18N
                                continue;
                             return ch.getFirstChild().getNodeValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return "";
    }

    public static String getRMIInvokerPort(String domainDir){

        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";

        InputStream inputStream = null;
        Document document = null;
        try {
            inputStream = new FileInputStream(xmlFile);
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            } finally {
                inputStream.close();
            }

            // get the root element
            Element root = document.getDocumentElement();

            // get the child nodes
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("mbean")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    if (!child.getAttributes().getNamedItem("name").getNodeValue().equals("jboss:service=invoker,type=jrmp")) //NOI18N
                        continue;
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);

                        if (ch.getNodeName().equals("attribute")) {  // NOI18N
                            if (!ch.getAttributes().getNamedItem("name").getNodeValue().equals("RMIObjectPort")) //NOI18N
                                continue;
                             return ch.getFirstChild().getNodeValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return "";
    }

      /** Return true if the specified port is free, false otherwise. */
    public static boolean isPortFree(int port) {
        ServerSocket soc = null;
        try {
            soc = new ServerSocket(port);
        } catch (IOException ioe) {
            return false;
        } finally {
            if (soc != null)
                try { soc.close(); } catch (IOException ex) {} // noop
        }

        return true;
    }

    /**
     * Return the version of the server located at the given path.
     * If the server version can't be determined returns <code>null</code>.
     *
     * @param serverPath path to the server directory
     * @return specification version of the server
     */
    @CheckForNull
    public static Version getServerVersion(File serverPath) {
        assert serverPath != null : "Can't determine version with null server path"; // NOI18N

        File systemJarFile = new File(serverPath, "lib/jboss-system.jar"); // NOI18N
        Version version = getVersion(systemJarFile);
        if (version == null) {
            // check for JBoss AS 7
            File serverDir = new File(serverPath, getModulesBase(serverPath.getAbsolutePath()) + "org/jboss/as/server/main");
            File[] files = serverDir.listFiles(new JarFileFilter());
            if (files != null) {
                for (File jarFile : files) {
                    version = getVersion(jarFile);
                    if(version != null) {
                        break;
                    }
                }
            }
        }
        return version;
    }

    static class JarFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    }

    public static int getDefaultJmxPortNumber(Version version) {
        if (version != null && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_7_0_0) >= 0) {
            return 9999;
        } else {
            return 1090;
        }
    }

    private static Version getVersion(File systemJarFile) {
        if (!systemJarFile.exists()) {
            return null;
        }

        try {
            JarFileSystem systemJar = new JarFileSystem();
            systemJar.setJarFile(systemJarFile);
            Attributes attributes = systemJar.getManifest().getMainAttributes();
            String version = attributes.getValue("Specification-Version"); // NOI18N
            if (version != null) {
                return new Version(version);
            }
            return null;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }
    
    /**
     * Class representing the JBoss version.
     * <p>
     * <i>Immutable</i>
     *
     * @author Petr Hejl
     */
    public static final class Version implements Comparable<Version> {

        private String majorNumber = "0";

        private String minorNumber = "0";

        private String microNumber = "0";

        private String update = "";

        /**
         * Constructs the version from the spec version string.
         * Expected format is <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>.
         *
         * @param version spec version string with the following format:
         *             <code>MAJOR_NUMBER[.MINOR_NUMBER[.MICRO_NUMBER[.UPDATE]]]</code>
         */
        public Version(String version) {
            assert version != null : "Version can't be null"; // NOI18N

            String[] tokens = version.split("\\.");

            if (tokens.length >= 4) {
                update = tokens[3];
            }
            if (tokens.length >= 3) {
                microNumber = tokens[2];
            }
            if (tokens.length >= 2) {
                minorNumber = tokens[1];
            }
            majorNumber = tokens[0];
        }

        /**
         * Returns the major number.
         *
         * @return the major number. Never returns <code>null</code>.
         */
        public String getMajorNumber() {
            return majorNumber;
        }

        /**
         * Returns the minor number.
         *
         * @return the minor number. Never returns <code>null</code>.
         */
        public String getMinorNumber() {
            return minorNumber;
        }

        /**
         * Returns the micro number.
         *
         * @return the micro number. Never returns <code>null</code>.
         */
        public String getMicroNumber() {
            return microNumber;
        }

        /**
         * Returns the update.
         *
         * @return the update. Never returns <code>null</code>.
         */
        public String getUpdate() {
            return update;
        }

        /**
         * {@inheritDoc}<p>
         * Two versions are equal if and only if they have same major, minor,
         * micro number and update.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            if (this.majorNumber != other.majorNumber
                    && (this.majorNumber == null || !this.majorNumber.equals(other.majorNumber))) {
                return false;
            }
            if (this.minorNumber != other.minorNumber
                    && (this.minorNumber == null || !this.minorNumber.equals(other.minorNumber))) {
                return false;
            }
            if (this.microNumber != other.microNumber
                    && (this.microNumber == null || !this.microNumber.equals(other.microNumber))) {
                return false;
            }
            if (this.update != other.update
                    && (this.update == null || !this.update.equals(other.update))) {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}<p>
         * The implementation consistent with {@link #equals(Object)}.
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.majorNumber != null ? this.majorNumber.hashCode() : 0);
            hash = 17 * hash + (this.minorNumber != null ? this.minorNumber.hashCode() : 0);
            hash = 17 * hash + (this.microNumber != null ? this.microNumber.hashCode() : 0);
            hash = 17 * hash + (this.update != null ? this.update.hashCode() : 0);
            return hash;
        }

        /**
         * {@inheritDoc}<p>
         * Compares the versions based on its major, minor, micro and update.
         * Major number is the most significant. Implementation is consistent
         * with {@link #equals(Object)}.
         */
        public int compareTo(Version o) {
            int comparison = compareToIgnoreUpdate(o);
            if (comparison != 0) {
                return comparison;
            }
            return update.compareTo(o.update);
        }

        /**
         * Compares the versions based on its major, minor, micro. Update field
         * is ignored. Major number is the most significant.
         *
         * @param o version to compare with
         */
        public int compareToIgnoreUpdate(Version o) {
            int comparison = majorNumber.compareTo(o.majorNumber);
            if (comparison != 0) {
                return comparison;
            }
            comparison = minorNumber.compareTo(o.minorNumber);
            if (comparison != 0) {
                return comparison;
            }
            return microNumber.compareTo(o.microNumber);
        }

    }

}
