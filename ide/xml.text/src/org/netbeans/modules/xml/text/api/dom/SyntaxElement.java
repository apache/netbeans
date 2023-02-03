/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.text.api.dom;

import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.w3c.dom.Node;

/**
 * Abstract predecessor for all syntax elements - nodes, tags, attributes. 
 * SyntaxElement represents a piece of XML document, similar to W3C Node, exposes offset
 * into the underlying text. Since the syntax model
 * can represent an error, which is not a W3C Node. For regular pieces of
 * text, W3C Node can be obtained ({@link #getNode}. Nodes obtained from SyntaxElements can be converted back
 * using {@link XMLSyntaxSupport#getSyntaxElement}.
 * <p/>
 * Obtaining previous or next element may result in lexing through the doucment - a document read
 * lock is internally obtained. If the caller plans to traverse through the document, document read lock for the
 * whole operation should be obtained, i.e. using {@link Document#render}.
 * <p/>
 * Erroneous pieces of text are represented by SyntaxElement, which is NOT convertible to a Node. Error elements
 * have the {@link #getType} or {@link #NODE_ERROR}. 
 * </p>
 * <i>Note:</i> use {@link #getType} instead of {@code instanceof} operator to check for a particular
 * element type. <b>Do not downcast</b> the {@code SyntaxElement}, use {@link #getNode} to get the DOM Node
 * implementation. If the {@link #getType} returns {@link Node#ELEMENT_NODE}, it is safe to downcast 
 * {@code SyntaxElement} to a {@link TagElement} and use extended interface.
 * 
 * @author sdedic
 * @since 1.60
 */
public interface SyntaxElement {
    /**
     * Special type of element which represent an Erroneous piece of text
     */
    public static final int NODE_ERROR = -1;
    
    /**
     * Returns the next element in textual order.
     * @return next element or {@code null}
     */
    @CheckForNull
    public SyntaxElement getNext();
    
    /**
     * Provides the previous lexical element, in textual order
     * @return previous element or {@code null}
     */
    @CheckForNull
    public SyntaxElement getPrevious();
    
    /**
     * @return length of the element, including children
     */
    public int getElementLength();
    
    /**
     * Provides element's offset in the underlying document. Note that unless
     * accessed under document read-lock, the offset may not point a the correct place
     * in the document.
     * @return offset of the element start
     */
    public int getElementOffset();
    
    /**
     * Returns type of the node. The return value is one of the W3C Node types, 
     * or {@link #NODE_ERROR}
     * @return type of element
     */
    public int getType();
    
    /**
     * Returns Node, if the instance can be represented in W3C DOM model.
     * @return represented Node or {@code null}.
     */
    @CheckForNull
    public <T extends Node> T getNode();
    
    /**
     * Returns the parent element.
     * @return parent element or {@code null}
     */
    @CheckForNull
    public SyntaxElement getParentElement();
    
}
