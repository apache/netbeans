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

package org.netbeans.modules.git;

import java.awt.Color;
import java.io.OutputStream;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

public final class ModuleLifecycleManager implements ErrorHandler, EntityResolver {

    private static ModuleLifecycleManager instance;
    private static final String [] otherGitModules = {
        "org.nbgit" // NOI18N
    };

    private ModuleLifecycleManager () {
    }

    static ModuleLifecycleManager getInstance () {
        if (instance == null) {
            instance = new ModuleLifecycleManager();
        }
        return instance;
    }
    
    void disableOtherModules () {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean notify = false;
                outter: for (int i = 0; i < otherGitModules.length; i++) {
                    FileLock lock = null;
                    OutputStream os = null;
                    try {
                        String newModule = otherGitModules[i];
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
                        notify = true;                            
                        lock = fo.lock();
                        os = fo.getOutputStream(lock);
                        
                        XMLUtil.write(document, os, "UTF-8"); // NOI18N
                    } catch (Exception e) {
                        Git.LOG.log(Level.WARNING, null, e);
                    } finally {
                        if (os != null) try { os.close(); } catch (IOException ex) {}
                        if (lock != null) lock.releaseLock();
                    }
                }
                if(notify) {
                    JTextPane ballonDetails = getPane(NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning")); // using the same pane causes the balloon popup
                    JTextPane popupDetails = getPane(NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning"));  // to trim the text to the first line
                    NotificationDisplayer.getDefault().notify(
                            NbBundle.getMessage(ModuleLifecycleManager.class, "MSG_Install_Warning_Title"), //NOI18N
                            ImageUtilities.loadImageIcon("org/netbeans/modules/git/resources/icons/info.png", false),
                            ballonDetails, popupDetails, NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.WARNING);
                }
            }
                        
            private JTextPane getPane(String txt) {
                JTextPane bubble = new JTextPane();
                bubble.setOpaque(false);
                bubble.setEditable(false);
                if (UIManager.getLookAndFeel().getID().equals("Nimbus")) {                   //NOI18N
                    //#134837
                    //http://forums.java.net/jive/thread.jspa?messageID=283882
                    bubble.setBackground(new Color(0, 0, 0, 0));
                }
                bubble.setContentType("text/html");                                          //NOI18N
                bubble.setText(txt);
                return bubble;
            }             
        };
        RequestProcessor.getDefault().post(runnable);
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
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }
    
    @Override
    public void error(SAXParseException exception) {
        Git.LOG.log(Level.INFO, null, exception);
    }

    @Override
    public void fatalError(SAXParseException exception) {
        Git.LOG.log(Level.INFO, null, exception);
    }

    @Override
    public void warning(SAXParseException exception) {
        Git.LOG.log(Level.INFO, null, exception);
    }        

}
