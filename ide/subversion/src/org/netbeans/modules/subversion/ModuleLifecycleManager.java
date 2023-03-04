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

package org.netbeans.modules.subversion;

import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.*;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Handles module events distributed by NetBeans module
 * framework.
 *
 * <p>It's registered and instantiated from module manifest.
 *
 * @author Petr Kuzel
 * @author Maros Sandor
 */
public final class ModuleLifecycleManager extends ModuleInstall implements ErrorHandler, EntityResolver {

    static final String [] vcsGenericModules = {
        "org.netbeans.modules.vcs.advanced", // NOI18N
        "org.netbeans.modules.vcs.profiles.cvsprofiles", // NOI18N
        "org.netbeans.modules.vcs.profiles.vss", // NOI18N
        "org.netbeans.modules.vcs.profiles.pvcs", // NOI18N
        "org.netbeans.modules.vcs.profiles.teamware" // NOI18N
    };
    
    @Override
    public void restored() {
        disableOldModules();
    }

    private void disableOldModules() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean notified = false;
                outter: for (int i = 0; i < vcsGenericModules.length; i++) {
                    FileLock lock = null;
                    OutputStream os = null;
                    try {
                        String newModule = vcsGenericModules[i];
                        String newModuleXML = "Modules/" + newModule.replace('.', '-') + ".xml"; // NOI18N
                        FileObject fo = FileUtil.getConfigFile(newModuleXML);
                        if (fo == null) continue;
                        Document document = readModuleDocument(fo);

                        NodeList list = document.getDocumentElement().getElementsByTagName("param"); // NOI18N
                        int n = list.getLength();
                        for (int j = 0; j < n; j++) {
                            Element node = (Element) list.item(j);
                            if ("enabled".equals(node.getAttribute("name"))) { // NOI18N
                                Text text = (Text) node.getChildNodes().item(0);
                                String value = text.getNodeValue();
                                if ("true".equals(value)) { // NOI18N
                                    text.setNodeValue("false"); // NOI18N
                                    break;
                                } else {
                                    continue outter;
                                }
                            }
                        }
                        if (!notified) {
                            JOptionPane.showMessageDialog(Utilities.findDialogParent(), 
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning"),  // NOI18N
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning_Title"),  // NOI18N
                                                          JOptionPane.WARNING_MESSAGE);
                            notified = true;
                        }
                        lock = fo.lock();
                        os = fo.getOutputStream(lock);
                        
                        XMLUtil.write(document, os, "UTF-8"); // NOI18N
                    } catch (Exception e) {
                        Subversion.LOG.log(Level.INFO, e.getMessage(), e);                        
                    } finally {
                        if (os != null) try { os.close(); } catch (IOException ex) {}
                        if (lock != null) lock.releaseLock();
                    }
                }
            }
        };
        org.netbeans.modules.versioning.util.Utils.post(runnable);
    }

    private Document readModuleDocument(FileObject fo) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setEntityResolver(this);
        parser.setErrorHandler(this);
        InputStream is = fo.getInputStream();
        Document document = parser.parse(is);
        is.close();
        return document;
    }

    @Override
    public void uninstalled() {
        Subversion.getInstance().shutdown();
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }
    
    @Override
    public void error(SAXParseException exception) {
        Subversion.LOG.log(Level.INFO, exception.getMessage(), exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        Subversion.LOG.log(Level.INFO, exception.getMessage(), exception);
    }

    @Override
    public void warning(SAXParseException exception) {
        Subversion.LOG.log(Level.INFO, exception.getMessage(), exception);
    }
}
