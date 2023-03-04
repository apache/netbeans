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

package org.netbeans.modules.websvc.rest.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author mkuchtiak
 */
public class XmlDomUtils {

    static void addJaxbXjcTargets(
            FileObject buildScript,
            String targetName,
            String sourceRoot,
            String[] schemaFiles,
            String[] packageNames,
            boolean isInitTarget,
            boolean isNbProject)
            throws IOException, ParserConfigurationException, SAXException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        InputStream targetStream = buildScript.getInputStream();
        Document targetDom = docBuilder.parse(targetStream);
        Element rootNode = targetDom.getDocumentElement();
        if (rootNode != null) {
            if (!isInitTarget) {
                rootNode.appendChild(targetDom.createComment("Target required for SAAS(REST) services: XJCTask initialization"));
                rootNode.appendChild(targetDom.createTextNode("\n"));
                Element targetEl = targetDom.createElement("target");
                targetEl.setAttribute("name", "saas-init-xjc");
                if (!isNbProject) {
                    targetEl.setAttribute("depends", "-init-project");
                }
                Element taskdefEl = targetDom.createElement("taskdef");
                taskdefEl.setAttribute("name", "xjc");
                taskdefEl.setAttribute("classname", "com.sun.tools.xjc.XJCTask");
                Element classpathEl = targetDom.createElement("classpath");
                classpathEl.setAttribute("path", "${libs.jaxb.classpath}");
                taskdefEl.appendChild(classpathEl);
                targetEl.appendChild(taskdefEl);
                rootNode.appendChild(targetEl);
                rootNode.appendChild(targetDom.createTextNode("\n\n"));
            }
            rootNode.appendChild(targetDom.createComment("Target required for SAAS(REST) services: data types generation"));
            rootNode.appendChild(targetDom.createTextNode("\n"));
            Element targetEl = targetDom.createElement("target");
            targetEl.setAttribute("name", targetName);
            targetEl.setAttribute("depends", "saas-init-xjc");
            for (int i=0;i<schemaFiles.length;i++) {
                Element xjcEl = targetDom.createElement("xjc");
                xjcEl.setAttribute("schema", schemaFiles[i]);
                xjcEl.setAttribute("target", "2.1");
                xjcEl.setAttribute("package", packageNames[i]);
                xjcEl.setAttribute("destdir", sourceRoot);
                xjcEl.setAttribute("encoding", "${source.encoding}");
                targetEl.appendChild(xjcEl);
            }
            rootNode.appendChild(targetEl);
            rootNode.appendChild(targetDom.createTextNode("\n\n"));

            targetStream.close();
            
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                //initialize StreamResult with File object to save to file
                OutputStream os = buildScript.getOutputStream();
                StreamResult result = new StreamResult(os);
                DOMSource source = new DOMSource(targetDom);
                transformer.transform(source, result);
                os.close();

            } catch (TransformerException ex) {
                Logger.getLogger(XmlDomUtils.class.getName()).log(Level.WARNING, "Can not save build.xml file", ex);
            }
        }
    }
}
