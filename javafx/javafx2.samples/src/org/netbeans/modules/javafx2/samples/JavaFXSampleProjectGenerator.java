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

package org.netbeans.modules.javafx2.samples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Create a sample java project by unzipping a template into some directory.
 * Modify active platform and JavaFX related properties.
 *
 * @author Martin Grebac, Tomas Zezula, Anton Chechel, Petr Somol
 */
public class JavaFXSampleProjectGenerator {

    private static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-project/3"; // NOI18N
    // copy of AntBasedProjectFactorySingleton.PROJECT_NS;
    private static final String ANT_BASED_PROJECT_NAMESPACE = "http://www.netbeans.org/ns/project/1"; // NOI18N

    private JavaFXSampleProjectGenerator() {}

    public static FileObject createProjectFromTemplate(final FileObject template,
            File projectLocation, final String name, final String platformName) throws IOException {
        assert template != null && projectLocation != null && name != null;
        FileObject prjLoc = createProjectFolder(projectLocation);
        if (template.getExt().endsWith("zip")) { // NOI18N
            unzip(template.getInputStream(), prjLoc);
            try {
                // update project.xml                
                File projXml = FileUtil.toFile(prjLoc.getFileObject(AntProjectHelper.PROJECT_XML_PATH));
                Document doc = XMLUtil.parse(new InputSource(Utilities.toURI(projXml).toString()), false, true, null, null);
                NodeList nlist = doc.getElementsByTagNameNS(PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                if (nlist != null) {
                    for (int i = 0; i < nlist.getLength(); i++) {
                        Node n = nlist.item(i);
                        if (n.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element e = (Element) n;
                        replaceText(e, name);
                    }

                    if(!platformName.equals(JavaFXPlatformUtils.DEFAULT_PLATFORM)) {
                        // we don't use default platform
                        Element root = doc.getDocumentElement();
                        Element config = XMLUtil.findElement(root, "configuration", ANT_BASED_PROJECT_NAMESPACE); // NOI18N
                        Element data = XMLUtil.findElement(config, "data", PROJECT_CONFIGURATION_NAMESPACE); // NOI18N
                        // logic taken from J2SEProjectPlatformImpl
                        Element insertBefore = null;
                        for (Element e : XMLUtil.findSubElements(data)) {
                            final String n = e.getNodeName();
                            if (! "name".equals(n) &&                  //NOI18N
                                ! "minimum-ant-version".equals(n)) {   //NOI18N
                                insertBefore = e;
                                break;
                            }
                        }
                        final Element explicitPlatformEl = insertBefore.getOwnerDocument().createElementNS(
                                PROJECT_CONFIGURATION_NAMESPACE,
                                "explicit-platform"); //NOI18N
                        explicitPlatformEl.setAttribute("explicit-source-supported", "true");   //NOI18N
                        data.insertBefore(explicitPlatformEl, insertBefore);
                    }
                    
                    saveXml(doc, prjLoc, AntProjectHelper.PROJECT_XML_PATH);

                    FileObject projectProps = prjLoc.getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    if (projectProps != null) {
                        FileLock lock = projectProps.lock();
                        try {
                            EditableProperties props = new EditableProperties(false);
                            InputStream in = projectProps.getInputStream();
                            try {
                                props.load(in);
                            } finally {
                                in.close();
                            }
                            props.setProperty("platform.active", platformName); // NOI18N
                            
                            OutputStream out = projectProps.getOutputStream(lock);
                            try {
                                props.store(out);
                            } finally {
                                out.close();
                            }
                        } finally {
                            lock.releaseLock();
                        }
                    }
                }

            } catch (Exception e) {
                throw new IOException(e.toString());
            }
            prjLoc.refresh(false);
        }
        return prjLoc;
    }

    private static FileObject createProjectFolder(File projectFolder) throws IOException {
        FileObject projLoc;
        Stack<String> nameStack = new Stack<String>();
        while ((projLoc = FileUtil.toFileObject(projectFolder)) == null) {
            nameStack.push(projectFolder.getName());
            projectFolder = projectFolder.getParentFile();
        }
        while (!nameStack.empty()) {
            projLoc = projLoc.createFolder(nameStack.pop());
            assert projLoc != null;
        }
        return projLoc;
    }

    private static void unzip(InputStream source, FileObject targetFolder) throws IOException {
        //installation
        ZipInputStream zip = new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                if (ent.isDirectory()) {
                    FileUtil.createFolder(targetFolder, ent.getName());
                } else {
                    FileObject destFile = FileUtil.createData(targetFolder, ent.getName());
                    FileLock lock = destFile.lock();
                    try {
                        OutputStream out = destFile.getOutputStream(lock);
                        try {
                            FileUtil.copy(zip, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

    private static File createPrivateProperties(FileObject fo) throws IOException {
        String[] nameElements = AntProjectHelper.PRIVATE_PROPERTIES_PATH.split("/"); // NOI18N
        for (int i = 0; i < nameElements.length - 1; i++) {
            FileObject tmp = fo.getFileObject(nameElements[i]);
            if (tmp == null) {
                tmp = fo.createFolder(nameElements[i]);
            }
            fo = tmp;
        }
        fo = fo.createData(nameElements[nameElements.length - 1]);
        return FileUtil.toFile(fo);
    }

    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     */
    private static void replaceText(Element parent, String name) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text) l.item(i);
                text.setNodeValue(name);
                return;
            }
        }
    }

    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            OutputStream os = xml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
}
