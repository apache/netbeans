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

package org.netbeans.modules.git.remote;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class ModuleLifecycleManager implements ErrorHandler, EntityResolver {

    private static ModuleLifecycleManager instance;
    private static final String [] otherGitModules = {
        "org.nbgit" // NOI18N
    };

    private ModuleLifecycleManager () {
    }

    static synchronized ModuleLifecycleManager getInstance () {
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
                        if (fo == null) {
                            continue;
                        }
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
                        if (os != null) {
                            try { os.close(); } catch (IOException ex) {}
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                }
                if(notify) {
                    JTextPane ballonDetails = getPane(NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning")); // using the same pane causes the balloon popup
                    JTextPane popupDetails = getPane(NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning"));  // to trim the text to the first line
                    NotificationDisplayer.getDefault().notify(
                            NbBundle.getMessage(ModuleLifecycleManager.class, "MSG_Install_Warning_Title"), //NOI18N
                            ImageUtilities.loadImageIcon("org/netbeans/modules/git/remote/resources/icons/info.png", false),
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
