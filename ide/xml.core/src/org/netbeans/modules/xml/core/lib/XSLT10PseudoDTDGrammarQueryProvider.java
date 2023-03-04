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
package org.netbeans.modules.xml.core.lib;

import java.beans.FeatureDescriptor;
import java.io.InputStream;
import java.util.Enumeration;
import org.netbeans.modules.xml.api.model.*;
import org.openide.util.Enumerations;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Workaround for XSLT 1.0 DTD based completion. Returns a pseudo XSLT1.0 DTD grammar
 * for xsl files which declares the http://www.w3.org/1999/XSL/Transform namespace.
 * 
 * For more info see http://www.w3.org/TR/xslt#dtd
 * 
 * @author mfukala@netbeans.org
 */
public class XSLT10PseudoDTDGrammarQueryProvider extends GrammarQueryManager {

    @Override
    public Enumeration enabled(GrammarEnvironment ctx) {
        Enumeration<Node> en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == Node.ELEMENT_NODE) {
                boolean xslt = false;
                boolean version1x = false;
                NamedNodeMap attrs = next.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    if (attr.getNodeName().startsWith("xmlns")) { //xmlns:xxx="yyy" attribute //NOI18N 
                        if ("http://www.w3.org/1999/XSL/Transform".equals(attr.getNodeValue())) { //NOI18N
                            xslt = true;
                        }
                    }
                    if (attr.getNodeName().endsWith("version")) { //NOI18N
                         version1x = attr.getNodeValue().startsWith("1."); //better support everything pre 2.0
                    }
                }
                if(xslt && version1x) {
                    return Enumerations.singleton(next);
                }
                
            }
        }
        return null;
    }

    @Override
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }

    @Override
    public GrammarQuery getGrammar(GrammarEnvironment env) {
        InputStream istream = this.getClass().getClassLoader().
                getResourceAsStream("org/netbeans/modules/xml/core/resources/xslt10pseudo.dtd"); //NOI18N
        InputSource isource = new InputSource(istream);            
        return DTDUtil.parseDTD(true, isource);
    }
}
