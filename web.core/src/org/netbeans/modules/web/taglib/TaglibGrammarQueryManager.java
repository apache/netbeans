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

package org.netbeans.modules.web.taglib;
import java.util.Enumeration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.netbeans.modules.xml.api.model.DTDUtil;
import org.netbeans.api.xml.services.UserCatalog;

/** Taglib Grammar provided code completion for jsptaglibrary specified by XML schema.
 *
 * @author  mk115033
 */
public class TaglibGrammarQueryManager extends org.netbeans.modules.xml.api.model.GrammarQueryManager
{
    private static final String XMLNS_ATTR="xmlns"; //NOI18N
    private static final String TAGLIB_TAG="taglib"; //NOI18N
       
    public java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        if (ctx.getFileObject() == null) return null;
        Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
                return null; // null for taglibs specified by DTD
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tag = element.getTagName();
                if (TAGLIB_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && TaglibCatalog.J2EE_NS.equals(xmlns)) //NOI18N
                            return org.openide.util.Enumerations.singleton (next);
                    }
                }
        }
        
        return null;
    }
    
    public java.beans.FeatureDescriptor getDescriptor() {
        return new java.beans.FeatureDescriptor();
    }
    
    /** Returns pseudo DTD for code completion
    */
    public org.netbeans.modules.xml.api.model.GrammarQuery getGrammar(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        UserCatalog catalog = UserCatalog.getDefault();
        if (catalog != null) {
            EntityResolver resolver = catalog.getEntityResolver();
            if (resolver != null) {
                try {
                    InputSource inputSource = resolver.resolveEntity(TaglibCatalog.TAGLIB_2_1_ID, null);
                    if (inputSource!=null) {
                        return DTDUtil.parseDTD(true, inputSource);
                    }
                } catch(SAXException e) {
                } catch(java.io.IOException e) {
                }
            }
        }
        return null;
    }
    
}
