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

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.w3c.dom.Node;

/**
 * It should in future promote to EntityReference implementation.
 * Holds entity reference.
 * <p>
 * Difference from DOM: it's also created for well known entities and
 * character entities.
 *
 * @author Petr Kuzel
 */
public final class EntityReference extends SyntaxNode implements org.w3c.dom.EntityReference  {

    EntityReference(XMLSyntaxSupport syntax, Token<XMLTokenId> token, int start, int end) {
        super(syntax, token, start, end);
    }

    public String getNodeName() {
//        TokenItem target = first().getNext();
//        if (target != null) {
//            String tokenImage = target.getImage();
//            return tokenImage.substring(1, tokenImage.length()-1);
//        } else {
//            return "";  //??? or null
//        }
        return null;
    }

    public short getNodeType() {
        return Node.ENTITY_REFERENCE_NODE;
    }

    public String toString() {
        return "Ref(" + getNodeName() + ")";
    }
}

