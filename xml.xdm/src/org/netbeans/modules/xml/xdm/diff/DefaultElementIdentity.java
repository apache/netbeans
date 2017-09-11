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

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NamedNodeMap;

/*
 * This class is used by DiffFinder to compare 2 elements by identifying attributes
 *
 * @author Ayub Khan
 */
public class DefaultElementIdentity implements ElementIdentity {
    
    /**
     * Creates a new instance of DefaultElementIdentity
     */
    public DefaultElementIdentity() {
    }
    
    public List getIdentifiers() {
        return identifiers;
    }
    
    public void addIdentifier(String identifier) {
        if(!identifiers.contains(identifier))
            identifiers.add(identifier);
    }
    
    public boolean compareElement(org.w3c.dom.Element n1, org.w3c.dom.Element n2, org.w3c.dom.Document doc1, org.w3c.dom.Document doc2) {
        return compareElement(n1, n2, null, doc1, doc2);
    }
    
    protected boolean compareElement(org.w3c.dom.Element n1, org.w3c.dom.Element n2, org.w3c.dom.Node parent1, org.w3c.dom.Document doc1, org.w3c.dom.Document doc2) {
        String qName1 = n1.getLocalName();
        String qName2 = n2.getLocalName();
        String ns1 = ((Node)n1).getNamespaceURI((Document) doc1);
        String ns2 = ((Node)n2).getNamespaceURI((Document) doc2);
        
        if ( qName1.intern() !=  qName2.intern() )
            return false;
        if ( !(ns1 == null && ns2 == null) &&
                !(ns1 != null && ns2 != null && ns1.intern() == ns2.intern() ) )
            return false;
        
        if(parent1 == doc1) return true; //if root no need to compare other identifiers
        
        return compareAttr( n1, n2);
    }
    
    protected boolean compareAttr(org.w3c.dom.Element n1, org.w3c.dom.Element n2) {
        NamedNodeMap attrs1 = n1.getAttributes();
        NamedNodeMap attrs2 = n2.getAttributes();
        
        List<String> nameSet = getIdentifiers();
        if( nameSet.isEmpty() )
            return true;
        else if ( attrs1.getLength() == 0 && attrs2.getLength() == 0 )
            return true;
        
        int matchCount = 0;
        int unmatchCount = 0;
        for ( String name:nameSet ) {
            Node attr1 = (Node) attrs1.getNamedItem( name );
            Node attr2 = (Node) attrs2.getNamedItem( name );
            if ( attr1 == null && attr2 == null )
                continue;
            else if ( attr1 != null && attr2 != null ){
                if ( attr2.getNodeValue().intern() != attr1.getNodeValue().intern() )
                    unmatchCount++;
                else
                    matchCount++;
            } else
                unmatchCount++;
            //check for exact match
            if ( matchCount == 1 )
                return true;
            
            //check for rename
            if ( unmatchCount == 1 && attrs1.getLength() == attrs2.getLength() )
                return false;
        }
        
        //no attributes in attrs1 and attrs2 that match nameSet
        if ( matchCount == 0 && unmatchCount == 0 )
            return true;
        
        return false;
    }
    
    public void clear() {
        identifiers.clear();
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private List<String> identifiers = new ArrayList<String>();
}
