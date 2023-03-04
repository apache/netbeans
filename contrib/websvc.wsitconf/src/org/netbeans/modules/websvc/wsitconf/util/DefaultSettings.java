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

package org.netbeans.modules.websvc.wsitconf.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.Key;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.UsernameAuthenticationProfile;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Martin Grebac
 */
public class DefaultSettings {

    private static final Logger logger = Logger.getLogger(Util.class.getName());

    private static final String WSSIP = "wssip";
    private static final String XWS_SECURITY_CLIENT = "xws-security-client";
    private static final String XWS_SECURITY_SERVER = "xws-security-server";

    private static final String SERVER_KEYSTORE_BUNDLED = "/org/netbeans/modules/websvc/wsitconf/resources/server-keystore.jks"; //NOI18N
    private static final String SERVER_TRUSTSTORE_BUNDLED = "/org/netbeans/modules/websvc/wsitconf/resources/server-truststore.jks"; //NOI18N
    private static final String CLIENT_KEYSTORE_BUNDLED = "/org/netbeans/modules/websvc/wsitconf/resources/client-keystore.jks"; //NOI18N
    private static final String CLIENT_TRUSTSTORE_BUNDLED = "/org/netbeans/modules/websvc/wsitconf/resources/client-truststore.jks"; //NOI18N

    private static final String PASSWORD = "changeit";

    private static final String STORE_FOLDER_NAME = "certs";

    public static final void fillDefaultsToServer(String serverID) {

        boolean glassfish = ServerUtils.isGlassfish(serverID);

        String serverKeyStorePath = ServerUtils.getStoreLocation(serverID, false, false);
        String serverTrustStorePath = ServerUtils.getStoreLocation(serverID, true, false);
        String clientKeyStorePath = ServerUtils.getStoreLocation(serverID, false, true);
        String clientTrustStorePath = ServerUtils.getStoreLocation(serverID, true, true);

        if (!glassfish) {
            FileObject tomcatLocation = ServerUtils.getTomcatLocation(serverID);
            try {
                FileObject targetFolder = FileUtil.createFolder(tomcatLocation, STORE_FOLDER_NAME);
                DataFolder folderDO = (DataFolder) DataObject.find(targetFolder);
                FileObject foClientKey = FileUtil.getConfigFile("Templates/WebServices/client-keystore.jks"); // NOI18N
                FileObject foClientTrust = FileUtil.getConfigFile("Templates/WebServices/client-truststore.jks"); // NOI18N
                FileObject foServerKey = FileUtil.getConfigFile("Templates/WebServices/server-keystore.jks"); // NOI18N
                FileObject foServerTrust = FileUtil.getConfigFile("Templates/WebServices/server-truststore.jks"); // NOI18N
                FileObject[] filesToCreate = {foClientKey, foClientTrust, foServerKey, foServerTrust };
                for (FileObject fo : filesToCreate) {
                    if (fo != null) {
                        DataObject template = DataObject.find(fo);
                        if (template != null) {
                            if (targetFolder.getFileObject(fo.getName(), fo.getExt()) == null) {
                                template.createFromTemplate(folderDO, fo.getNameExt());
                            }
                        }
                    }
                }

                FileObject tomcatUsers = tomcatLocation.getFileObject("conf/tomcat-users.xml");

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = dbf.newDocumentBuilder();
                Document document = builder.parse(FileUtil.toFile(tomcatUsers));

                NodeList nodes = document.getElementsByTagName("tomcat-users");
                if ((nodes != null) && (nodes.getLength() > 0)) {
                    Node n = nodes.item(0);
                    NodeList users = document.getElementsByTagName("user");
                    boolean foundUser = false;
                    for (int i=0; i < users.getLength(); i++) {
                        Node node = users.item(i);
                        if (node instanceof Element) {
                            Element u = (Element)node;
                            String userAttr = u.getAttribute("name");
                            if (UsernameAuthenticationProfile.DEFAULT_USERNAME.equals(userAttr)) {
                                foundUser = true;
                                break;
                            }
                        }
                    }
                    if (!foundUser) {
                        if (tomcatUsers.getParent().getFileObject("tomcat-users.backup", "xml") == null) {
                            FileUtil.copyFile(tomcatUsers, tomcatUsers.getParent(), "tomcat-users.backup");
                        }

                        Element user = document.createElement("user");
                        user.setAttribute("name", UsernameAuthenticationProfile.DEFAULT_USERNAME);
                        user.setAttribute("password", UsernameAuthenticationProfile.DEFAULT_PASSWORD);
                        user.setAttribute("roles", "tomcat");
                        n.appendChild(user);

                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                        //initialize StreamResult with File object to save to file
                        StreamResult result = new StreamResult(FileUtil.toFile(tomcatUsers));
                        DOMSource source = new DOMSource(document);
                        transformer.transform(source, result);
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.INFO, null, ex);
            }
            return;
        }

        String dstPasswd = getDefaultPassword(serverID);
        try {
            copyKey(SERVER_KEYSTORE_BUNDLED, XWS_SECURITY_SERVER, PASSWORD, PASSWORD, serverKeyStorePath,XWS_SECURITY_SERVER, dstPasswd, false);
            copyKey(SERVER_KEYSTORE_BUNDLED, WSSIP, PASSWORD, PASSWORD, serverKeyStorePath,WSSIP, dstPasswd, false);
            copyKey(SERVER_TRUSTSTORE_BUNDLED, "certificate-authority", PASSWORD, PASSWORD, serverTrustStorePath, "xwss-certificate-authority", dstPasswd, true);
            copyKey(SERVER_TRUSTSTORE_BUNDLED, XWS_SECURITY_CLIENT, PASSWORD, PASSWORD, serverTrustStorePath,XWS_SECURITY_CLIENT, dstPasswd, true);
            copyKey(SERVER_KEYSTORE_BUNDLED, XWS_SECURITY_SERVER, PASSWORD, PASSWORD, serverTrustStorePath,XWS_SECURITY_SERVER, dstPasswd, true);
            copyKey(CLIENT_KEYSTORE_BUNDLED, XWS_SECURITY_CLIENT, PASSWORD, PASSWORD, clientKeyStorePath,XWS_SECURITY_CLIENT, dstPasswd, false);
            copyKey(CLIENT_TRUSTSTORE_BUNDLED, XWS_SECURITY_SERVER, PASSWORD, PASSWORD, clientTrustStorePath,XWS_SECURITY_SERVER, dstPasswd, true);
            copyKey(CLIENT_TRUSTSTORE_BUNDLED, WSSIP, PASSWORD, PASSWORD, clientTrustStorePath,WSSIP, dstPasswd, true);
        } catch (Exception ex) {
            logger.log(Level.INFO, null, ex);
        }
    }

    public static final void fillDefaults(Project project, boolean client, boolean refreshScript) {

        boolean tomcat = ServerUtils.isTomcat(project);
        boolean glassfish = ServerUtils.isGlassfish(project);

        if (tomcat) {
            if (project != null) {
                FileObject tomcatLocation = ServerUtils.getTomcatLocation(project);
                try {
                    FileObject targetFolder = FileUtil.createFolder(tomcatLocation, STORE_FOLDER_NAME);
                    DataFolder folderDO = (DataFolder) DataObject.find(targetFolder);
                    FileObject foClientKey = FileUtil.getConfigFile("Templates/WebServices/client-keystore.jks"); // NOI18N
                    FileObject foClientTrust = FileUtil.getConfigFile("Templates/WebServices/client-truststore.jks"); // NOI18N
                    FileObject foServerKey = FileUtil.getConfigFile("Templates/WebServices/server-keystore.jks"); // NOI18N
                    FileObject foServerTrust = FileUtil.getConfigFile("Templates/WebServices/server-truststore.jks"); // NOI18N
                    FileObject[] filesToCreate = {foClientKey, foClientTrust, foServerKey, foServerTrust };
                    for (FileObject fo : filesToCreate) {
                        if (fo != null) {
                            DataObject template = DataObject.find(fo);
                            if (template != null) {
                                if (targetFolder.getFileObject(fo.getName(), fo.getExt()) == null) {
                                    template.createFromTemplate(folderDO, fo.getNameExt());
                                }
                            }
                        }
                    }

                    if (!client) {
                        FileObject tomcatUsers = tomcatLocation.getFileObject("conf/tomcat-users.xml");

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = dbf.newDocumentBuilder();
                        Document document = builder.parse(FileUtil.toFile(tomcatUsers));

                        NodeList nodes = document.getElementsByTagName("tomcat-users");
                        if ((nodes != null) && (nodes.getLength() > 0)) {
                            Node n = nodes.item(0);
                            NodeList users = document.getElementsByTagName("user");
                            boolean foundUser = false;
                            for (int i=0; i < users.getLength(); i++) {
                                Node node = users.item(i);
                                if (node instanceof Element) {
                                    Element u = (Element)node;
                                    String userAttr = u.getAttribute("name");
                                    if (UsernameAuthenticationProfile.DEFAULT_USERNAME.equals(userAttr)) {
                                        foundUser = true;
                                        break;
                                    }
                                }
                            }
                            if (!foundUser) {
                                if (tomcatUsers.getParent().getFileObject("tomcat-users.backup", "xml") == null) {
                                    FileUtil.copyFile(tomcatUsers, tomcatUsers.getParent(), "tomcat-users.backup");
                                }

                                Element user = document.createElement("user");
                                user.setAttribute("name", UsernameAuthenticationProfile.DEFAULT_USERNAME);
                                user.setAttribute("password", UsernameAuthenticationProfile.DEFAULT_PASSWORD);
                                user.setAttribute("roles", "tomcat");
                                n.appendChild(user);

                                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                                //initialize StreamResult with File object to save to file
                                StreamResult result = new StreamResult(FileUtil.toFile(tomcatUsers));
                                DOMSource source = new DOMSource(document);
                                transformer.transform(source, result);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.INFO, null, ex);
                }
            }
            return;
        }

        String dstPasswd = getDefaultPassword(project);
        if (glassfish) {
            try {
                if (!client && refreshScript) refreshBuildScript(project);
                String serverKeyStorePath = ServerUtils.getStoreLocation(project, false, false);
                String serverTrustStorePath = ServerUtils.getStoreLocation(project, true, false);
                String clientKeyStorePath = ServerUtils.getStoreLocation(project, false, true);
                String clientTrustStorePath = ServerUtils.getStoreLocation(project, true, true);
                copyKey(SERVER_KEYSTORE_BUNDLED, XWS_SECURITY_SERVER, PASSWORD, PASSWORD, serverKeyStorePath,XWS_SECURITY_SERVER, dstPasswd, false);
                copyKey(SERVER_KEYSTORE_BUNDLED, WSSIP, PASSWORD, PASSWORD, serverKeyStorePath,WSSIP, dstPasswd, false);
                copyKey(SERVER_TRUSTSTORE_BUNDLED, "certificate-authority", PASSWORD, PASSWORD, serverTrustStorePath, "xwss-certificate-authority", dstPasswd, true);
                copyKey(SERVER_TRUSTSTORE_BUNDLED, XWS_SECURITY_CLIENT, PASSWORD, PASSWORD, serverTrustStorePath,XWS_SECURITY_CLIENT, dstPasswd, true);
                copyKey(SERVER_KEYSTORE_BUNDLED, XWS_SECURITY_SERVER, PASSWORD, PASSWORD, serverTrustStorePath,XWS_SECURITY_SERVER, dstPasswd, true);
                copyKey(CLIENT_KEYSTORE_BUNDLED, XWS_SECURITY_CLIENT, PASSWORD, PASSWORD, clientKeyStorePath,XWS_SECURITY_CLIENT, dstPasswd, false);
                copyKey(CLIENT_TRUSTSTORE_BUNDLED, XWS_SECURITY_SERVER, PASSWORD, PASSWORD, clientTrustStorePath,XWS_SECURITY_SERVER, dstPasswd, true);
                copyKey(CLIENT_TRUSTSTORE_BUNDLED, WSSIP, PASSWORD, PASSWORD, clientTrustStorePath,WSSIP, dstPasswd, true);
            } catch (Exception ex) {
                logger.log(Level.INFO, null, ex);
            }
        }
    }

    public static void copyKey(String srcPath, String srcAlias, String srcPasswd, String srcKeyPasswd,
                               String dstPath, String dstAlias, String dstPasswd,
                               boolean trustedCertEntry) throws Exception {
        KeyStore srcStore = KeyStore.getInstance("JKS");
        KeyStore dstStore = KeyStore.getInstance("JKS");
        srcStore.load(Util.class.getResourceAsStream(srcPath), srcPasswd.toCharArray());
        InputStream is = new FileInputStream(dstPath);
        try {
            dstStore.load(is, dstPasswd.toCharArray());
            Key privKey = srcStore.getKey(srcAlias, srcKeyPasswd.toCharArray());

            if (is != null) is.close();

            OutputStream os = new FileOutputStream(dstPath);
            try {
                if (privKey == null || trustedCertEntry) {
                    //this is a cert-entry
                    dstStore.setCertificateEntry(dstAlias, srcStore.getCertificate(srcAlias));
                } else {
                    Certificate cert = srcStore.getCertificate(srcAlias);
                    Certificate[] chain = new Certificate[] {cert};
                    dstStore.setKeyEntry(dstAlias, privKey, dstPasswd.toCharArray(), chain);
                }
            } finally {
                if (os != null) {
                    dstStore.store(os, dstPasswd.toCharArray());
                    os.close();
                }
            }
        } finally {
            if (is != null) is.close();
        }
    }

    private static final String BUILD_SCRIPT = "/build.xml";       //NOI18N
    private static final String BACKUP_EXT = ".bak";        //NOI18N
    private static final String IMPORT_WSIT_DEPLOY_XML = "<import file=\"nbproject/wsit-deploy.xml\"/>";    //NOI18N
    private static final String IMPORT_TAG = "<import"; //NOI18N
    private static final String WSIT_DEPLOY_XML_PATH = "nbproject/wsit-deploy.xml"; //NOI18N
    private static final String WSIT_DEPLOY_XSL = "org/netbeans/modules/websvc/wsitconf/resources/wsit-deploy.xsl";  //NOI18N

    public static void refreshBuildScript(Project p) {
        String buildScript = FileUtil.toFile(p.getProjectDirectory()).getPath() + BUILD_SCRIPT;

        BufferedReader reader = null;
        BufferedWriter writer = null;
        String line = null;
        boolean added = false;

        // First check to see if our import statement has already been added.
        try {
            reader = new BufferedReader(new FileReader(buildScript));
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(IMPORT_WSIT_DEPLOY_XML) != -1) {
                    added = true;
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            logger.log(Level.INFO, null, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    logger.log(Level.INFO, null, ex);
                }
            }
        }

        // If our import statement has not been added, add it now.
        if (!added) {
            try {
                // Rename the original to build.xml.bak
                File backupBuildScript = new File(buildScript);
                backupBuildScript.renameTo(new File(buildScript + BACKUP_EXT));

                reader = new BufferedReader(new FileReader(buildScript + BACKUP_EXT));
                writer = new BufferedWriter(new FileWriter(buildScript));
                added = false;
                int index = 0;

                while ((line = reader.readLine()) != null) {
                    if (!added && (index = line.indexOf(IMPORT_TAG)) != -1) {
                        StringBuffer buf = new StringBuffer(line);
                        buf = buf.replace(index, line.length(), IMPORT_WSIT_DEPLOY_XML);
                        writer.write(buf.toString());
                        writer.newLine();
                        added = true;
                    }

                    writer.write(line);
                    writer.newLine();
                }
            } catch (FileNotFoundException ex) {
                logger.log(Level.INFO, null, ex);
            } catch (IOException ex) {
                logger.log(Level.INFO, null, ex);
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }

                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.INFO, null, ex);
                }
            }
        }

        // Now refresh the wsit-deploy.xml itself.
        GeneratedFilesHelper genFilesHelper = new GeneratedFilesHelper(p.getProjectDirectory());

        try {
            genFilesHelper.refreshBuildScript(
                    WSIT_DEPLOY_XML_PATH,
                    Util.class.getClassLoader().getResource(WSIT_DEPLOY_XSL),
                    false);
        } catch (IOException ex) {
            logger.log(Level.INFO, null, ex);
        }
    }

    public static void unfillDefaults(Project p) {
        String buildScript = FileUtil.toFile(p.getProjectDirectory()).getPath() + BUILD_SCRIPT;

        FileObject createUserFile = p.getProjectDirectory().getFileObject("nbproject/wsit.createuser");
        if ((createUserFile != null) && (createUserFile.isValid()) && !(createUserFile.isVirtual())) {
            try {
                createUserFile.delete();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        BufferedReader reader = null;
        BufferedWriter writer = null;
        String line = null;
        boolean added = false;

        // First check to see if our import statement has already been added.
        try {
            reader = new BufferedReader(new FileReader(buildScript));
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(IMPORT_WSIT_DEPLOY_XML) != -1) {
                    added = true;
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            logger.log(Level.INFO, null, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    logger.log(Level.INFO, null, ex);
                }
            }
        }

        // If our import statement has not been added, add it now.
        if (added) {
            try {
                // Rename the original to build.xml.bak
                File backupBuildScript = new File(buildScript);
                backupBuildScript.renameTo(new File(buildScript + BACKUP_EXT));

                reader = new BufferedReader(new FileReader(buildScript + BACKUP_EXT));
                writer = new BufferedWriter(new FileWriter(buildScript));
                added = false;

                while ((line = reader.readLine()) != null) {
                    if ((line.indexOf(IMPORT_WSIT_DEPLOY_XML)) == -1) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            } catch (FileNotFoundException ex) {
                logger.log(Level.INFO, null, ex);
            } catch (IOException ex) {
                logger.log(Level.INFO, null, ex);
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }

                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.INFO, null, ex);
                }
            }
        }
    }

    public static String getDefaultPassword(Project p) {
        String password = KeystorePanel.DEFAULT_PASSWORD;
        if (ServerUtils.isGlassfish(p)) {
            String storeLoc = ServerUtils.getStoreLocation(p, false, false);
            if (!passwordOK(storeLoc, password)) {
                password = Util.getPassword(p);
                if (!passwordOK(storeLoc, password)) {
                    password = "";
                }
            }
        }
        return password;
    }

    public static String getDefaultPassword(String serverID) {
        String password = KeystorePanel.DEFAULT_PASSWORD;
        if (ServerUtils.isGlassfish(serverID)) {
            String storeLoc = ServerUtils.getStoreLocation(serverID, false, false);
            if (!passwordOK(storeLoc, password)) {
                password = KeystorePanel.DEFAULT_PASSWORD2;
                if (!passwordOK(storeLoc, password)) {
                    password = "";
                }
            }
        }
        return password;
    }

    private static boolean passwordOK(String storePath, String password) {
        try {
            Util.getAliases(storePath, password.toCharArray(), KeystorePanel.JKS);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}
