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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses a content of the .classpath file.
 *
 * @author mkrauskopf
 */
final class DotClassPathParser {
    
    private DotClassPathParser() {/* empty constructor */}

    public static DotClassPath parse(File dotClasspath, List<Link> links) throws IOException {
        Document dotClasspathXml;
        try {
            dotClasspathXml = XMLUtil.parse(new InputSource(Utilities.toURI(dotClasspath).toString()), false, true, XMLUtil.defaultErrorHandler(), null);
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException(dotClasspath + ": " + e.toString()).initCause(e); //NOI18N
            throw ioe;
        }
        Element classpathEl = dotClasspathXml.getDocumentElement();
        if (!"classpath".equals(classpathEl.getLocalName())) { // NOI18N
            return empty();
        }
        List<Element> classpathEntryEls;
        try {
            classpathEntryEls = XMLUtil.findSubElements(classpathEl);
        } catch (IllegalArgumentException x) {
            throw new IOException(x);
        }
        if (classpathEntryEls == null) {
            return empty();
        }
        
        // accessrules are ignored as they are not supported in NB anyway, eg:
        /*
        <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER">
            <accessrules>
                <accessrule kind="accessible" pattern="com/sun/management/*"/>
                <accessrule kind="nonaccessible" pattern="com/sun/**"/>
            </accessrules>
        </classpathentry>
         */ 

        List<DotClassPathEntry> classpath = new ArrayList<DotClassPathEntry>();
        List<DotClassPathEntry> sources = new ArrayList<DotClassPathEntry>();
        DotClassPathEntry output = null;
        DotClassPathEntry jre = null;
        for (Element classpathEntry : classpathEntryEls) {
            Map<String, String> props = new HashMap<String, String>();
            NamedNodeMap attrs = classpathEntry.getAttributes();
            String linkName = null;
            for (int i=0; i <attrs.getLength(); i++) {
                Node n = attrs.item(i);
                String key = n.getNodeName();
                String value = classpathEntry.getAttribute(n.getNodeName());
                if (DotClassPathEntry.ATTRIBUTE_PATH.equals(key)) {
                    String resolvedLink = resolveLink(value, links);
                    if (resolvedLink != null) {
                        linkName = value;
                        value = resolvedLink;
                    }
                }
                props.put(key, value);
            }
            Element entryAttrs = XMLUtil.findElement(classpathEntry, "attributes", null); //NOI18N
            if (entryAttrs != null) {
                /*
                <classpathentry kind="lib" path="/home/dev/hibernate-annotations-3.3.1.GA/lib/hibernate-commons-annotations.jar" sourcepath="/home/dev/hibernate-annotations-3.3.1.GA/src">
                    <attributes>
                        <attribute name="javadoc_location" value="file:/home/dev/hibernate-annotations-3.3.1.GA/doc/api/"/>
                    </attributes>
                </classpathentry>
                 */
                List<Element> attrsList = XMLUtil.findSubElements(entryAttrs);
                if (attrsList != null) {
                    for (Element e : attrsList) {
                        props.put(e.getAttribute("name"), e.getAttribute("value")); //NOI18N
                    }
                }
            }
            DotClassPathEntry entry = new DotClassPathEntry(props, linkName);
            if (entry.getKind() == DotClassPathEntry.Kind.SOURCE) {
                sources.add(entry);
            } else if (entry.getKind() == DotClassPathEntry.Kind.OUTPUT) {
                assert output == null : "there should be always just one default output"; //NOI18N
                output = entry;
            } else if (entry.getKind() == DotClassPathEntry.Kind.CONTAINER &&
                    entry.getRawPath().startsWith(Workspace.DEFAULT_JRE_CONTAINER)) {
                jre = entry;
            } else {
                classpath.add(entry);
            }
        }
        return new DotClassPath(classpath, sources, output, jre);
    }

    static DotClassPath empty() {
        return new DotClassPath(new ArrayList<DotClassPathEntry>(), new ArrayList<DotClassPathEntry>(), null, null);
    }
    
    private static String resolveLink(String value, List<Link> links) {
        String[] v = EclipseUtils.splitVariable(value);
        for (Link l : links) {
            if (l.getName().equals(v[0])) {
                return l.getLocation() + v[1];
            }
        }
        return null;
    }
    
}
