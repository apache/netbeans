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
