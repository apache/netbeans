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
package org.netbeans.modules.css.editor;

import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * @author marekfukala
 */
public class CssDeclarationContext {

    private static final String IE_HACK_POSTFIX = "\\9";
    private static final String IE_STAR_HACK_PREFIX = "*";
    
    private Node declaration, propertyName, propertyValue;

    public CssDeclarationContext(Node declaration) {
        this.declaration = declaration;
        propertyName = NodeUtil.getChildByType(declaration, NodeType.property);
        propertyValue = NodeUtil.getChildByType(declaration, NodeType.propertyValue);
    }

    public Node getDeclaration() {
        return declaration;
    }

    public Node getProperty() {
        return propertyName;
    }

    public Node getPropertyValue() {
        return propertyValue;
    }
    
    public String getPropertyNameImage() {
        return propertyName == null ? null : propertyName.image().toString().trim();
    }
    
    public String getPropertyValueImage() {
        return propertyValue == null ? null : propertyValue.image().toString().trim();
    }
        
    /**
     * Contains the IE star hack?
     * 
     * example: *border: 1px;
     */
    public boolean containsIEStarHack() {
        Node sibling = NodeUtil.getSibling(propertyName, true);
        return sibling != null && LexerUtils.equals(IE_STAR_HACK_PREFIX, sibling.image(), false, false);
    }
    
    /**
     * Contains the \9 declaration hack?
     * 
     * example: border: 0 \9;
     */
    public boolean containsIEBS9Hack() {
        return getPropertyValueImage() != null && getPropertyValueImage().endsWith(IE_HACK_POSTFIX);
    }
    
}
