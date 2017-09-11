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
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.ELEMENT_NODE) {
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
