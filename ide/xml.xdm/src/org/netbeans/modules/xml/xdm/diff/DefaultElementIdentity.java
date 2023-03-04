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
