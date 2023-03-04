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

import org.netbeans.modules.xml.xam.Model;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Interface describing an abstract model. The model is based on a
 * document representation that represents the persistent form.
 * @author Chris Webster
 * @author Nam Nguyen
 * @author Rico Cruz
 */
public interface DocumentModel<C extends DocumentComponent<C>> extends Model<C>{
    
    /**
     * @return the DOM Document node.
     */
    Document getDocument();
    
    /**
     * Returns model root component.
     */
    C getRootComponent();
    
    /**
     * @return true if two DOM nodes have same identity.
     */
    public boolean areSameNodes(Node n1, Node n2);
    
    /**
     * Return XPath expression for the given component.
     */
    String getXPathExpression(DocumentComponent component);
    
    /**
     * Create component to be added as child of given component.
     */
    C createComponent(C parent, Element element);
    
    /**
     * Find component given a position into the Swing document.
     * @return component if found.
     */
    DocumentComponent findComponent(int position);
}
