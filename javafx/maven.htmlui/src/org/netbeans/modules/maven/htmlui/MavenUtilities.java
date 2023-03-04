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
package org.netbeans.modules.maven.htmlui;

import java.io.File;
import java.io.FileWriter;
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
    private static final String MOEDEFINITION = "moe.launcher.simulators";
    private static final String ROBOVMDEFINITION = "robovm.iosDeviceName";

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
            if (!this.settings.isFile()) {
                return null;
            }
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
                NodeList profilesNode = settingsDoc.getElementsByTagName("profiles");
                Node parent = null;
                if (profilesNode.getLength() > 0) {
                    parent = profilesNode.item(0);
                } else {
                    parent = settingsDoc.createElement("profiles");
                    settingsDoc.getDocumentElement().appendChild(parent);
                }

                Document doc = parent.getOwnerDocument();

                NodeList profiles = parent.getChildNodes();
                for (int i = 0; i < profiles.getLength();) {
                    Node profileNode = profiles.item(i);
                    if (name.equals(profileId(profileNode))) {
                        parent.removeChild(profileNode);
                    } else {
                        i++;
                    }
                }

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

                FileWriter w = new FileWriter(settings);
                w.write(dump);
                w.close();
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

    private static String profileId(Node profile) {
        NodeList children = profile.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node ch = children.item(i);
            if (ch.getNodeName().equals("id")) {
                return ch.getTextContent();
            }
        }
        return null;
    }

    void writeNetBeansInstallation(String path) {
        writeProperty(NBDEFINITION, path);
    }

    String readNetBeansInstallation() {
        return readProperty(NBDEFINITION);
    }

    void writeMoeDevice(String id) {
        writeProperty(MOEDEFINITION, id);
    }

    String readMoeDevice() {
        return readProperty(MOEDEFINITION);
    }

    void writeRobovmDeviceName(String name) {
        writeProperty(ROBOVMDEFINITION, name);
    }

}
