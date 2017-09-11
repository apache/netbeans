/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
