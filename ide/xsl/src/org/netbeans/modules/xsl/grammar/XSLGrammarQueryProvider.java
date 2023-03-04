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
package org.netbeans.modules.xsl.grammar;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.xsl.XSLDataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Provide DTD grammar. It must be registered at layer.
 *
 * @author  Petr Kuzel
 */
public class XSLGrammarQueryProvider extends GrammarQueryManager {
    
    static final String PUBLIC = "!!! find it out";                             // NOI18N
    static final String SYSTEM = "!!! find it out";                             // NOI18N
    static final String NAMESPACE = XSLGrammarQuery.XSLT_NAMESPACE_URI;
    
    private String prefix = null;
    
    public Enumeration enabled(GrammarEnvironment ctx) {

        if (ctx.getFileObject() == null) return null;
        
        prefix = null;
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tag = element.getTagName();
                if (tag.indexOf(":") == -1) {  // NOI18N
                    if ("transformation".equals(tag) || "stylesheet".equals(tag)) { // NOI18N
                        String ns = element.getAttribute("xmlns"); // NOI18N
                        if (NAMESPACE.equals(ns)) {
                            return org.openide.util.Enumerations.singleton (next);
                        }
                    }
                } else {
                    prefix = tag.substring(0, tag.indexOf(":"));  // NOI18N
                    String local = tag.substring(tag.indexOf(":") + 1); // NOI18N
                    if ("transformation".equals(local) || "stylesheet".equals(local)) { // NOI18N
                        String ns = element.getAttribute("xmlns:" + prefix); // NOI18N
                        if (NAMESPACE.equals(ns)) {
                            return org.openide.util.Enumerations.singleton (next);
                        }
                    }
                }
            }
        }
        
        // try mime type
        FileObject fo = ctx.getFileObject();
        if (fo != null) {
            if (XSLDataObject.MIME_TYPE.equals(fo.getMIMEType())) {
                // almost forever, until client uses its own invalidation
                // rules based e.g. on new node detection at root level
                // or MIME type listening
                return org.openide.util.Enumerations.empty();
            }
        }
        
        return null;
    }    
    
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    public GrammarQuery getGrammar(GrammarEnvironment input) {
        return null; // <- use this statement when parsing of XSL schema is used
                     //    for code completion in NB 6.5
        // comment the try-block below when parsing of XSL schema is used 
        // for code completion in NB 6.5
        /* 
        try {
            FileObject fo = input.getFileObject();
            if (fo == null) throw new IllegalStateException("GrammarEnvironment has changed between enabled() and getGrammar()!"); // NOI18N     // NOI18N
            DataObject dataObj = DataObject.find(fo);
            return new XSLGrammarQuery(dataObj);
            
        } catch (DataObjectNotFoundException e) {
            throw new IllegalStateException("Missing DataObject " + e.getFileObject() + "!"); // NOI18N
        } 
        */
    }
}
