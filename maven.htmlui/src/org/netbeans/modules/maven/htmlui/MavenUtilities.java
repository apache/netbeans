/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.maven.htmlui;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.NoSuchFileException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.modules.maven.api.FileUtilities;

final class MavenUtilities {

    private static final Logger LOG = Logger.getLogger(MavenUtilities.class.getName());
    private static final String DEFINITION = "android.sdk.path";
    private static final String NBDEFINITION = "netbeans.installation";

    static final String HEADER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    static final String HEADER_SETTINGS = "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "  xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n";
    static final String FOOTER_SETTINGS = "</settings>\n";

    private final File settings;

    MavenUtilities(File settings) {
        this.settings = settings;
    }

    String readAndroidSdkPath() {
        return readProperty(DEFINITION);
    }

    private String readProperty(final String tag) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document settingsDoc = dBuilder.parse(this.settings);
            NodeList elementsByTagName = settingsDoc.getElementsByTagName(tag);
            if (elementsByTagName.getLength() >0) return elementsByTagName.item(0).getTextContent();
            return null;
        } catch (NoSuchFileException ex) {
            LOG.log(Level.FINE, "Cannot find " + settings, ex);
            return null;
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Cannot read " + settings, ex);
            return null;
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.INFO, "Cannot read " + settings, ex);
            return null;
        } catch (SAXException ex) {
            LOG.log(Level.INFO, "Cannot read " + settings, ex);
            return null;
        }
    }

    void writeAndroidSdkPath(String path) {
        writeProperty("android.sdk.path", path);
    }

    private void writeProperty(String name, String value) {
        try {
            String dump;
            if (settings.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder;
                dBuilder = dbFactory.newDocumentBuilder();
                Document settingsDoc = dBuilder.parse(this.settings);
                settingsDoc.getDocumentElement().normalize();
                NodeList profiles = settingsDoc.getElementsByTagName("profiles");
                Node parent = null;
                if (profiles.getLength() > 0) {
                    parent = profiles.item(0);
                } else {
                    parent = settingsDoc.createElement("profiles");
                    settingsDoc.appendChild(parent);
                }

                Document doc = parent.getOwnerDocument();

                Node fragmentNode = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder().parse(
                                new InputSource(new StringReader(singleProfile(name, value))))
                        .getDocumentElement();
                fragmentNode = doc.importNode(fragmentNode, true);
                parent.appendChild(fragmentNode);
                doc.getDocumentElement().normalize();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(settingsDoc);
                StreamResult result = new StreamResult(settings);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
            } else {
                settings.getParentFile().mkdirs();
                dump = HEADER_XML + HEADER_SETTINGS
                        + "  <profiles>\n"
                        + singleProfile(name, value)
                        + "  </profiles>\n"
                        + FOOTER_SETTINGS;
                Document newDoc = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder().newDocument();
                Node fragmentNode = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder().parse(
                                new InputSource(new StringReader(dump)))
                        .getDocumentElement();
                newDoc.adoptNode(fragmentNode);
                                newDoc.getDocumentElement().normalize();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(newDoc);
                StreamResult result = new StreamResult(settings);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Cannot modify " + settings, ex);
        } catch (ParserConfigurationException ex) {
             LOG.log(Level.INFO, "Cannot modify " + settings, ex);
        } catch (SAXException ex) {
             LOG.log(Level.INFO, "Cannot modify " + settings, ex);
        } catch (TransformerConfigurationException ex) {
            LOG.log(Level.INFO, "Cannot modify " + settings, ex);
        } catch (TransformerException ex) {
             LOG.log(Level.INFO, "Cannot modify " + settings, ex);
        }
    }

    static MavenUtilities getDefault() {
        return new MavenUtilities(FileUtilities.getUserSettingsFile(true));
    }

    private static String singleProfile(String name, String path) {
        return "    <profile>\n"
                + "      <id>" + name + "</id>\n"
                + "      <activation>\n"
                + "        <property>\n"
                + "          <name>!" + name + "</name>\n"
                + "        </property>\n"
                + "      </activation>\n"
                + "      <properties>\n"
                + "        <" + name + ">" + path + "</" + name + ">\n"
                + "      </properties>\n"
                + "    </profile>\n";
    }

    void writeNetBeansInstallation(String path) {
        writeProperty("netbeans.installation", path);
    }

    String readNetBeansInstallation() {
        return readProperty(NBDEFINITION);
    }

}
