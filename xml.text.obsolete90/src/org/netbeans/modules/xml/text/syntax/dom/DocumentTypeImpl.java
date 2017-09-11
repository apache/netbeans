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

package org.netbeans.modules.xml.text.syntax.dom;

import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;
import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * It should envolve in DocumentType implementation.
 *
 * @author Petr Kuzel
 */
public class DocumentTypeImpl extends SyntaxNode implements DocumentType, XMLTokenIDs {

    public DocumentTypeImpl(XMLSyntaxSupport syntax, TokenItem first, int to) {
        super (syntax, first, to);
    }

    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
    }
        
    public String getPublicId() {
        TokenItem first = first();
        String doctype = first.getImage();
        if (doctype.indexOf("PUBLIC") != -1) {                                  // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                String publicId = next.getImage();
                return publicId.substring(1, publicId.length() - 1);
            }
        }
        return null;
    }
    
    public org.w3c.dom.NamedNodeMap getNotations() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getName() {
        //<!DOCTYPE id ...
        String docType = first().getImage();
        int idIndex = docType.indexOf(' ');
        if(idIndex > 0) {
            int idEndIndex = docType.indexOf(' ', idIndex + 1);
            if(idEndIndex > 0 && idEndIndex > idIndex) {
                return docType.substring(idIndex + 1, idEndIndex);
            }
        }
        return null;
    }
    
    public org.w3c.dom.NamedNodeMap getEntities() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public String getSystemId() {
        TokenItem first = first();
        String doctype = first.getImage();
        if (doctype.indexOf("PUBLIC") != -1) {                                  // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                next = next.getNext();
                if (next == null) return null;
                next = next.getNext();
                if (next != null && next.getTokenID() == VALUE) {
                    String system = next.getImage();
                    return system.substring(1, system.length() -1);
                }
            }
        } else if (doctype.indexOf("SYSTEM") != -1) {                           // NOI18N
            TokenItem next = first.getNext();
            if (next != null && next.getTokenID() == VALUE) {
                String system = next.getImage();
                return system.substring(1, system.length() - 1);
            }
        }        
        return null;
    }
    
    public String getInternalSubset() {
        return null;
    }
    
}

