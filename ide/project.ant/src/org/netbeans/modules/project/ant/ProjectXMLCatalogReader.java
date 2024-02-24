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

package org.netbeans.modules.project.ant;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Supplies a catalog which lets users validate against project-related XML schemas.
 * @author Jesse Glick
 * @see "issue #49976"
 */
public class ProjectXMLCatalogReader implements CatalogReader, CatalogDescriptor2 {
    
    /** duplicated in project.ant/o.n.m.project.ant.ProjectXMLUtil **/
    private static final String PREFIX = "http://www.netbeans.org/ns/"; // NOI18N
    /** duplicated in project.ant/o.n.m.project.ant.ProjectXMLUtil **/
    private static final String EXTENSION = "xsd"; // NOI18N
    /** duplicated in project.ant/o.n.m.project.ant.ProjectXMLUtil **/
    private static final String CATALOG = "ProjectXMLCatalog"; // NOI18N
    
    /** Default constructor for use from layer. */
    public ProjectXMLCatalogReader() {}

    public String resolveURI(String name) {
        return _resolveURI(name);
    }
    private static String _resolveURI(String name) {
        if (name.startsWith(PREFIX)) {
            FileObject rsrc = FileUtil.getConfigFile(CATALOG + "/" + name.substring(PREFIX.length()) + "." + EXTENSION);
            if (rsrc != null) {
                return rsrc.toURL().toString();
            }
        }
        return null;
    }

    public String resolvePublic(String publicId) {
        return null;
    }

    public String getSystemID(String publicId) {
        return null;
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {}

    public void addPropertyChangeListener(PropertyChangeListener l) {}

    public void removeCatalogListener(CatalogListener l) {}

    public void addCatalogListener(CatalogListener l) {}

    public String getIconResource(int type) {
        return "org/netbeans/modules/project/ui/resources/projectTab.png";
    }

    public void refresh() {}

    public String getShortDescription() {
        return NbBundle.getMessage(ProjectXMLCatalogReader.class, "HINT_project_xml_schemas");
    }

    public Iterator getPublicIDs() {
        return Collections.EMPTY_SET.iterator();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ProjectXMLCatalogReader.class, "LBL_project_xml_schemas");
    }

    /**
     * Validate according to all *.xsd found in catalog.
     * @param dom DOM fragment to validate
     * @throws SAXException if schemas were malformed or the document was invalid
     */
    public static void validate(Element dom) throws SAXException {
        if (FileUtil.getConfigFile(CATALOG) == null) {
            // Probably running from inside a unit test which overrides the system filesystem.
            // Safest and simplest to just skip validation in this case.
            return;
        }
        XMLUtil.validate(dom, projectXmlCombinedSchema());
    }

    private static Schema LAST_USED_SCHEMA;
    private static int LAST_USED_SCHEMA_HASH;
    /** Load ProjectXMLCatalog/**.xsd. Cache the combined schema between runs if the content has not changed. */
    private static synchronized Schema projectXmlCombinedSchema() {
        int hash = 0; // compute hash regardless of ordering of schemas, hence XOR
        List<FileObject> schemas = new ArrayList<FileObject>();
        FileObject root = FileUtil.getConfigFile(CATALOG);
        if (root != null) {
            for (FileObject f : NbCollections.iterable(root.getChildren(true))) {
                if (f.isData() && f.hasExt(EXTENSION)) {
                    schemas.add(f);
                    hash ^= f.getPath().hashCode();
                    hash ^= f.getSize(); // probably close enough
                }
            }
        }
        if (LAST_USED_SCHEMA == null || hash != LAST_USED_SCHEMA_HASH) {
            List<Source> sources = new ArrayList<Source>();
            // nbfs URLs don't seem to work from unit tests, so need to use InputStream constructor
            List<InputStream> streams = new ArrayList<InputStream>();
            try {
                for (FileObject f : schemas) {
                    try {
                        InputStream is = f.getInputStream();
                        streams.add(is);
                        sources.add(new StreamSource(is, f.toURL().toString()));
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                try {
                    LAST_USED_SCHEMA = schemaFactory.newSchema(sources.toArray(new Source[0]));
                    LAST_USED_SCHEMA_HASH = hash;
                } catch (SAXException x) {
                    // Try to determine the culprit and report appropriately.
                    for (FileObject f : schemas) {
                        try {
                            schemaFactory.newSchema(new StreamSource(f.toURL().toString()));
                        } catch (Exception x2) {
                            Exceptions.attachMessage(x2, "While parsing: " + f.getPath()); // NOI18N
                            Exceptions.printStackTrace(x2);
                        }
                    }
                    // Report whole problem, just in case it is due to e.g. merging of schemas together.
                    Exceptions.printStackTrace(x);
                    // Suppress schema validation until fixed.
                    try {
                        return schemaFactory.newSchema();
                    } catch (SAXException x2) {
                        throw new AssertionError(x2);
                    }
                }
            } finally {
                for (InputStream is : streams) {
                    try {
                        is.close();
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
        }
        return LAST_USED_SCHEMA;
    }

    /**
     * Attempt to fix up simple mistakes in a project file.
     * Current strategies:
     * <ul>
     * <li>See if increasing /5 to /6 (or whatever) in the namespace used for "data" helps.
     * <li>See if reordering subelements of "data" helps.
     * </ul>
     * @param dom a faulty DOM tree
     * @param x the exception caused by trying to validate the original DOM tree
     * @return a valid DOM tree, or null if it cannot be corrected
     */
    @SuppressWarnings("fallthrough")
    public static Element autocorrect(Element dom, SAXException x) {
        String error = x.getMessage();
        if (error != null && error.contains("cvc-") && !error.contains("cvc-complex-type.2.4.a:")) { // NOI18N
            // The message from Xerces "Invalid content was found starting with element...".
            // All of the corrections that could be made here would be fixing this error.
            // So if something else is wrong, don't even bother.
            // For a non-Xerces parser, probably the "cvc-" will not be present, so try to fix.
            return null;
        }
        {
            Element attempt = (Element) dom.cloneNode(true);
            NodeList datas = attempt.getElementsByTagName("data");
            if (datas.getLength() > 0) {
                Element data = (Element) datas.item(0);
                String ns = data.getNamespaceURI();
                if (ns != null) {
                    int slash = ns.lastIndexOf('/');
                    if (slash != -1) {
                        try {
                            String ns2 = ns.substring(0, slash + 1) + Integer.toString(Integer.parseInt(ns.substring(slash + 1)) + 1);
                            if (_resolveURI(ns2) != null) {
                                Element data2 = XMLUtil.translateXML(data, ns2);
                                data.getParentNode().replaceChild(data2, data);
                                try {
                                    validate(attempt);
                                    return attempt;
                                } catch (SAXException failed) {}
                            }
                        } catch (NumberFormatException ignoreme) {}
                    }
                }
            }
        }
        // For order corrections, we really rely on the error message to know what to try.
        // Trying every combination would be too slow.
        // This only works with Xerces (which is what we use for now for validation)
        // and assumes the messages have not been translated (the JRE does not do so).
        Matcher m = Pattern.compile(
                "cvc-complex-type[.]2[.]4[.]a: Invalid content was found starting with element '(.+)'. One of '.+' is expected."). // NOI18N
                matcher(error);
        if (m.matches()) {
            String misplacedName = m.group(1);
            Element attempt = (Element) dom.cloneNode(true);
            NodeList datas = attempt.getElementsByTagName("data");
            if (datas.getLength() > 0) {
                Element data = (Element) datas.item(0);
                NodeList stuff = data.getChildNodes();
                int len = stuff.getLength();
                if (len > 1) {
                    int numberOfMisplaced = 0;
                    Node originalFront = stuff.item(0);
                    Node misplaced = null;
                    for (int i = 0; i < len; i++) {
                        Node n = stuff.item(i);
                        if (n instanceof Element) {
                            Element e = (Element) n;
                            boolean matches = misplacedName.equals(e.getLocalName());
                            if (misplaced == null && matches) {
                                misplaced = n;
                            } else if (misplaced != null && !matches) {
                                break;
                            }
                        }
                        if (misplaced != null) {
                            numberOfMisplaced++;
                            data.insertBefore(n, originalFront);
                        }
                    }
                    try {
                        validate(attempt);
                        return attempt;
                    } catch (SAXException failed) {}
                    for (int i = numberOfMisplaced; i < len; i++) {
                        data.insertBefore(stuff.item(i), misplaced);
                        try {
                            validate(attempt);
                            return attempt;
                        } catch (SAXException failed) {}
                    }
                }
            }
        }
        return null;
    }

}
