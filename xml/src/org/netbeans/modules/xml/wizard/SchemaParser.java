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

package org.netbeans.modules.xml.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Get (potentially partial) SchemaInfo from passed XML Schema.
 *
 * @author  Petr Kuzel
 * @see     SchemaInfo
 */
public final class SchemaParser extends DefaultHandler {

    private SchemaInfo info =  new SchemaInfo();

    // root elemnt depth is 0, its children has 1 etc.
    private int depth = 0;
    
    /** Creates a new instance of SchemaParser */
    public SchemaParser() {
    }
    
    public SchemaInfo parse(String sid) {
        if (sid == null) {
            return null;
        } else {
            return parse( new InputSource(sid));
        }
    }
    
    public SchemaInfo parse(InputSource in) {
    
        Util.THIS.debug("SchemaParser started.");                                           // NOI18N
                
        try {
            depth = 0;
            XMLReader parser = XMLUtil.createXMLReader(false, true);
            parser.setContentHandler(this);
            parser.setErrorHandler(this);

            UserCatalog catalog = UserCatalog.getDefault();
            EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
            
            if (res != null) parser.setEntityResolver(res);
            
            parser.parse(in);
            
            return info;
            
        } catch (SAXException ex) {
            Util.THIS.debug("Ignoring ex. thrown while looking for Schema roots:", ex);     // NOI18N
            if (ex.getException() instanceof RuntimeException) {
                Util.THIS.debug("Nested exception:", ex.getException());                    // NOI18N
            }                        
            return info;  // better partial result than nothing
        } catch (IOException ex) {
            Util.THIS.debug("Ignoring ex. thrown while looking for Schema roots:", ex);     // NOI18N
            return info;  // better partial result than nothing
        } finally {
            Util.THIS.debug("SchemaParser stopped.");                                       // NOI18N
        }
        
    }
    
    public void startElement (String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        depth++;
        if (depth > 2) return;
        
        //??? should be more accurate, check ns etc
        // may be, we should be also interested in "defaultForm" attributes
                
        if ("element".equals(localName)) {                                      // NOI18N
            String root = atts.getValue("name");
            if (root != null) {
                Util.THIS.debug("\telement decl: " + root);                     // NOI18N
                info.roots.add(root);
            }
        } else if ("schema".equals(localName)) {                                // NOI18N
            String ns = atts.getValue("targetNamespace");                       // NOI18N
            if (ns != null) {
                Util.THIS.debug("\ttarget namespace: " + ns);                   // NOI18N
                info.namespace = ns;
            }
        }
    }        
    
    public void endElement (String uri, String localName, String qName) {
        depth--;
    }
    
    /**
     * Very basic information structure about schema.
     */
    public static final class SchemaInfo {
        /**
         * Root candidates
         */
        public final Set roots = new TreeSet();
        
        /**
         * Target namespace or <code>null</code>
         */
        public String namespace;
    }
    
    public static String getNamespace(FileObject fobj) {
        SchemaParser parser = new SchemaParser();
        File file = FileUtil.toFile(fobj);
        SchemaParser.SchemaInfo info = parser.parse(file.toURI().toString());            
        if (info == null) return null;        
        return info.namespace;        
    }

    public static SchemaParser.SchemaInfo getRootElements(FileObject fobj) {
        SchemaParser parser = new SchemaParser();
        File file = FileUtil.toFile(fobj);
        SchemaParser.SchemaInfo info = parser.parse(file.toURI().toString());            
        if (info == null) return null;
        else return info;        
    }
    
}
