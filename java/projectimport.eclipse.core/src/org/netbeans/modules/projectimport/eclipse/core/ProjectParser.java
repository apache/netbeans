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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.projectimport.eclipse.core.Workspace.Variable;
import org.netbeans.modules.projectimport.eclipse.core.spi.Facets;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses given project's .project file and fills up the project with found
 * data.
 *
 * @author mkrauskopf
 */
final class ProjectParser {
    
    public static String parse(File dotProject, Set<String> natures, List<Link> links, Set<Variable> variables) throws IOException {
        Document dotProjectXml;
        try {
            dotProjectXml = XMLUtil.parse(new InputSource(Utilities.toURI(dotProject).toString()), false, true, XMLUtil.defaultErrorHandler(), null);
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException(dotProject + ": " + e.toString()).initCause(e); // NOI18N
            throw ioe;
        }
        Element projectDescriptionEl = dotProjectXml.getDocumentElement();
        if (!"projectDescription".equals(projectDescriptionEl.getLocalName())) { // NOI18N
            throw new IllegalStateException("given file is not eclipse .project file"); // NOI18N
        }
        
        Element naturesEl = XMLUtil.findElement(projectDescriptionEl, "natures", null); // NOI18N
        if (naturesEl != null) {
            List<Element> natureEls = XMLUtil.findSubElements(naturesEl);
            if (natureEls != null) {
                for (Element nature : natureEls) {
                    natures.add(nature.getTextContent());
                }
            }
        }
        
        Element linksEl = XMLUtil.findElement(projectDescriptionEl, "linkedResources", null); // NOI18N
        if (linksEl != null) {
            List<Element> linkEls = XMLUtil.findSubElements(linksEl);
            if (linkEls != null) {
                for (Element link : linkEls) {
                    Element locationElement = XMLUtil.findElement(link, "location", null); // NOI18N
                    String loc;
                    if (locationElement == null) {
                        assert XMLUtil.findElement(link, "locationURI", null) != null : XMLUtil.findSubElements(link); // NOI18N
                        // XXX external source root can be defined using IDE variable. For some reason (in Eclipse)
                        // these variables are stored/managed separately from variables which can be used
                        // in classpath. For now these variables are not transfer to NetBeans and normalized
                        // path will be returned instead.
                        loc = resolveLink(XMLUtil.findElement(link, "locationURI", null).getTextContent(), variables); // NOI18N
                    } else {
                        loc = locationElement.getTextContent();
                    }
                    links.add(new Link(XMLUtil.findElement(link, "name", null).getTextContent(),  // NOI18N
                            "1".equals(XMLUtil.findElement(link, "type", null).getTextContent()), // NOI18N
                            loc));
                }
            }
        }
        return XMLUtil.findElement(projectDescriptionEl, "name", null).getTextContent(); //NOI18N
    }
    
    public static Facets readProjectFacets(File projectDir, Set<String> natures) throws IOException {
        if (!natures.contains("org.eclipse.wst.common.project.facet.core.nature")) { // NOI18N
            return null;
        }
        File f = new File(projectDir, ".settings/org.eclipse.wst.common.project.facet.core.xml"); // NOI18N
        if (!f.exists()) {
            return null;
        }
        Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(Utilities.toURI(f).toString()), false, true, XMLUtil.defaultErrorHandler(), null);
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException(f + ": " + e.toString()).initCause(e); // NOI18N
            throw ioe;
        }
        Element root = doc.getDocumentElement();
        if (!"faceted-project".equals(root.getLocalName())) { // NOI18N
            return null;
        }
        
        List<Facets.Facet> facets = new ArrayList<Facets.Facet>();
        List<Element> elements = XMLUtil.findSubElements(root);
        for (Element element : elements) {
            if (!"installed".equals(element.getNodeName())) { // NOI18N
                continue;
            }
            String facet = element.getAttribute("facet"); // NOI18N
            String version = element.getAttribute("version"); // NOI18N
            if (facet != null && version != null) {
                facets.add(new Facets.Facet(facet, version));
            }
        }
        return new Facets(facets);
    }

    private static String resolveLink(String location, Set<Variable> vars) {
        /*
            <link>
                <name>classes-webapp5</name>
                <type>2</type>
                <locationURI>SOME_ROOT/WebApplication5/build/web/WEB-INF/classes</locationURI>
            </link>
         */
        //
        // environment variable are stored in 
        // .metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.resources.prefs
        // which in this case would contain property:
        // pathvariable.SOME_ROOT=/home/david/projs
        for (Variable v : vars) {
            if (location.startsWith(v.getName())) {
                return v.getLocation() + location.substring(v.getName().length());
            }
        }
        return location;
    }
    
}
