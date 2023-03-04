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

package org.netbeans.modules.xml.xam.dom;

import org.netbeans.modules.xml.xam.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A component in model.
 * 
 */
public interface DocumentComponent<C extends DocumentComponent> extends Component<C> {
    public static final String TEXT_CONTENT_PROPERTY = "textContent";

    /**
     * Returns the DOM element corresponding to this component.
     */
    Element getPeer();

    /**
     * @return attribute string value or null if the attribute is currently undefined
     */
    String getAttribute(Attribute attribute);
    
    /**
     * Sets the attribute value.
     * @param eventPropertyName name property change event to fire.
     * @param attribute the attribute to set value for.
     * @param value for the attribute.
     */
    void setAttribute(String eventPropertyName, Attribute attribute, Object value);
    
    /**
     * Returns true if the component is part of the document model.
     */
    boolean isInDocumentModel();
    
    /**
     * Returns the position of this component in the schema document,
     * expressed as an offset from the start of the document.
     * @return the position of this component in the document
     */
    int findPosition();

    /**
     * Returns true if the node referenced by this component is n.
     */
    boolean referencesSameNode(Node n);

    /**
     * Returns child component backed by given element node.
     */
    public C findChildComponent(Element e);

    /**
     * Returns position of the attribute by the given name, or -1 if not found.
     */
    public int findAttributePosition(String attributeName);
}
