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
package org.netbeans.modules.php.smarty.editor.completion.entries;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.php.smarty.editor.completion.entries.CodeCompletionParamMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Martin Fousek
 */
public class CodeCompletionEntries {


    public CodeCompletionEntries() {
    }

    protected static Collection<CodeCompletionEntryMetadata> readAllCodeCompletionEntriesFromXML(InputStream inputStream, String completionType) throws IOException, ParserConfigurationException, SAXException {
        Collection<CodeCompletionEntryMetadata> ccEntries = new ArrayList<CodeCompletionEntryMetadata>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList allEntriesList = doc.getElementsByTagName("entry");
        for (int i = 0; i < allEntriesList.getLength(); i++) {
            Node ccNode = allEntriesList.item(i);

            if (ccNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) ccNode;
                String desc = elem.getElementsByTagName("description").item(0).getTextContent();
                String url = elem.getElementsByTagName("url").item(0).getTextContent();
                String help = "";
                Collection<CodeCompletionParamMetadata> params = Collections.<CodeCompletionParamMetadata>emptyList();
                NodeList attributes = elem.getElementsByTagName("attributes");
//                if (completionType.equals("built-in-functions") || completionType.equals("custom-functions")) {
//                    help = generateHelpForFunctions(desc, attributes);
//                    params = getParametersForFunction(attributes);
//                } else {
//                    help = generateHelpForVariableModifiers(desc, attributes);
//                    params = null;
//                }
                ccEntries.add(new CodeCompletionEntryMetadata(elem.getAttribute("name"), help, url, params));
            }
        }

        return ccEntries;
    }

    private static String generateHelpForFunctions(String desc, NodeList attributesRoot) {
        Element parent = (Element) (attributesRoot.item(0));
        if (parent != null) {
            StringBuilder help = new StringBuilder(desc);
            help.append("<br><br><table border=1><tr style=\"font-weight:bold\"><td>Attribute name</td><td>Type</td><td>Required</td><td>Default</td><td>Description</td></tr>");
            NodeList attributes = parent.getChildNodes();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element attribute = (Element) attributes.item(i);
                    help.append("<tr><td>").append(attribute.getAttribute("name")).append("</td>");
                    help.append(getRestOfAttributeParams(attribute));
                    help.append("</tr>");
                }
            }
            help.append("</table>");
            return help.toString();
        } else {
            return desc;
        }
    }

    private static String generateHelpForVariableModifiers(String desc, NodeList attributesRoot) {
        Element parent = (Element) (attributesRoot.item(0));
        if (parent != null) {
            StringBuilder help = new StringBuilder(desc);
            help.append("<br><br><table border=1><tr style=\"font-weight:bold\"><td>Parameter Position</td><td>Type</td><td>Required</td><td>Default</td><td>Description</td></tr>");
            NodeList attributes = parent.getChildNodes();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element attribute = (Element) attributes.item(i);
                    help.append("<tr><td>").append(attribute.getAttribute("position")).append("</td>");
                    help.append(getRestOfAttributeParams(attribute));
                    help.append("</tr>");
                }
            }
            help.append("</table>");
            return help.toString();
        } else {
            return desc;
        }
    }

    private static String getRestOfAttributeParams(Element attributeParams) {
        String help = "";
        help += "<td>" + attributeParams.getElementsByTagName("type").item(0).getTextContent() + "</td>";
        help += "<td>" + attributeParams.getElementsByTagName("required").item(0).getTextContent() + "</td>";
        help += "<td>" + attributeParams.getElementsByTagName("default").item(0).getTextContent() + "</td>";
        help += "<td>" + attributeParams.getElementsByTagName("description").item(0).getTextContent() + "</td>";
        return help;
    }

    private static Collection<CodeCompletionParamMetadata> getParametersForFunction(NodeList attributesRoot) {
        Element parent = (Element) (attributesRoot.item(0));
        if (parent != null) {
            Collection<CodeCompletionParamMetadata> params = new ArrayList<CodeCompletionParamMetadata>();
            NodeList attributes = parent.getChildNodes();
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element attribute = (Element) attributes.item(i);
                    String name = attribute.getAttribute("name");
                    String help = generateHelpFunctionParameters(attribute);
                    CodeCompletionParamMetadata ccpm = new CodeCompletionParamMetadata(name, help);
                    assert (ccpm != null);
                    params.add(ccpm);

                }
            }
            return params;
        } else {
            return null;
        }
    }

    private static String generateHelpFunctionParameters(Element attribute) {
        String help = attribute.getElementsByTagName("description").item(0).getTextContent() + "<br><br><table border=1>";
        help += "<tr><td style=\"font-weight:bold\">Type</td><td>" + attribute.getElementsByTagName("type").item(0).getTextContent() + "</td></tr>";
        help += "<tr><td style=\"font-weight:bold\">Required</td><td>" + attribute.getElementsByTagName("required").item(0).getTextContent() + "</td></tr>";
        help += "<tr><td style=\"font-weight:bold\">Default</td><td>" + attribute.getElementsByTagName("default").item(0).getTextContent() + "</td></tr>";
        help += "</table>";
        return help;
    }
}

