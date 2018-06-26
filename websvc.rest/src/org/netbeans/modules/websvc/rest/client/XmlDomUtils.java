/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
