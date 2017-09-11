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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses user library xml document.
 */
final class UserLibraryParser {
    
    private UserLibraryParser() {/* empty constructor */}
    
    /** Returns jars contained in the given user library. */
    static boolean getJars(String libName, String xmlDoc, List<String> jars, List<String> javadocs, List<String> sources) throws IOException {
        assert jars.size() == 0 && javadocs.size() == 0 && sources.size() == 0;
        UserLibraryParser parser = new UserLibraryParser();
        Document xml;
        try {
            xml = XMLUtil.parse(new InputSource(new StringReader(xmlDoc)), false, true, XMLUtil.defaultErrorHandler(), null);
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException("Library '"+libName+"' cannot be parsed: " + e.toString()).initCause(e); // NOI18N
            throw ioe;
        }
        
        Element root = xml.getDocumentElement();
        if (!"userlibrary".equals(root.getLocalName())) { //NOI18N
            return false;
        }
        for (Element el : XMLUtil.findSubElements(root)) {
            if (!el.getNodeName().equals("archive")) { //NOI18N
                continue;
            }
            jars.add(el.getAttribute("path")); //NOI18N
            String src = el.getAttribute("sourceattachment"); //NOI18N
            if (src.length() > 0) {
                sources.add(src);
            }
            Element el2 = XMLUtil.findElement(el, "attributes", null); //NOI18N
            if (el2 == null) {
                continue;
            }
            for (Element el3 : XMLUtil.findSubElements(el2)) {
                if (el3.getNodeName().equals("attribute") && "javadoc_location".equals(el3.getAttribute("name"))) { //NOI18N
                    String javadoc = el3.getAttribute("value"); //NOI18N
                    if (javadoc != null) {
                        javadocs.add(javadoc);
                    }
                }
            }
        }
        return jars.size() > 0;
    }
    
}
