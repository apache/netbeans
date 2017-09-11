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
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
//                DocumentType doctype = (DocumentType) next;
//                if (PUBLIC.equals(doctype.getPublicId()) || SYSTEM.equals(doctype.getSystemId())) {
//                    return new SingletonEnumeration(next);
//                }
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
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
