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

package org.netbeans.modules.spring.beans.refactoring;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Rohan Ranade
 */
public class PropertyChildFinder {
    private final XMLSyntaxSupport syntaxSupport;
    private final int start;

    private int foundOffset = -1;
    private String value;
    
    public PropertyChildFinder(XMLSyntaxSupport syntaxSupport, int start) {
        this.syntaxSupport = syntaxSupport;
        this.start = start;
    }
    
    public boolean find(String propertyName) throws BadLocationException {
        foundOffset = -1;
        value = null;
        SyntaxElement beanElement = syntaxSupport.getElementChain(start+1);
        if (!syntaxSupport.isStartTag(beanElement)) {
            return false;
        }
        
        Node beanTag = beanElement.getNode();
        if(!BeansElements.BEAN.equals(beanTag.getNodeName())) {
            return false;
        }
        
        NodeList nl = beanTag.getChildNodes();
        for(int i=0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(BeansElements.PROPERTY.equals(n.getNodeName())) {
                String name = SpringXMLConfigEditorUtils.getAttribute(n, BeansAttributes.NAME);
                if(StringUtils.hasText(name) && propertyName.equals(name)) {
                    AttributeValueFinder delegate = new AttributeValueFinder(
                            syntaxSupport, 
                            syntaxSupport.getNodeOffset(n));
                    boolean retVal = delegate.find(BeansAttributes.NAME);
                    foundOffset = delegate.getFoundOffset();
                    value = delegate.getValue();
                    return retVal;
                }
            }
        }
        
        return false;
    }

    public int getFoundOffset() {
        return foundOffset;
    }

    public String getValue() {
        return value;
    }
}
