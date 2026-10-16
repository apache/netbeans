/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The apache-rat tasked used to report missing/wrong licensing in the
 * repository reports the resources with absolute paths. This task rewrites
 * the names to be relative to the root of the repository.
 */
public class RatRelativize extends Task {

    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void execute() throws BuildException {
        Path root = file.getParentFile().getParentFile().getParentFile().toPath();
        try {
            // Use the DOM parser for covenience. It should be possible to
            // handle this in a streaming manor, but it is much easier to load
            // the 15MB, process them and dump them back.
            Document doc = DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .parse(file);

            // Fetch all resource Elements (don't work directly on the NodeList
            // as this kill performance (the nodelist is refreshed on each
            // modifying access to the DOM)
            NodeList nl = doc.getElementsByTagName("resource");
            List<Element> elementsToModify = new ArrayList<>(nl.getLength());
            for(int i = 0; i < nl.getLength(); i++) {
                elementsToModify.add((Element) nl.item(i));
            }

            for(Element resourceElement: elementsToModify) {
                Path absolutePath = Path.of(resourceElement.getAttribute("name"));
                Path relativePath = root.relativize(absolutePath);
                resourceElement.setAttribute("name", relativePath.toString());
            }

            TransformerFactory
                    .newDefaultInstance()
                    .newTransformer()
                    .transform(new DOMSource(doc), new StreamResult(file));
        } catch (ParserConfigurationException | RuntimeException | SAXException | IOException | TransformerException ex) {
            throw new BuildException(ex);
        }
    }
}
