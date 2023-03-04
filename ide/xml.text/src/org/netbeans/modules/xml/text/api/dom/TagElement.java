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
package org.netbeans.modules.xml.text.api.dom;

import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Interface implemented by start/end tags. Use it to check what kind
 * of tag the SyntaxElement is. SyntaxElement can be only casted if its
 * {@link SyntaxElement#getType) returns {@link Node#ELEMENT_NODE}.
 * <p/>
 * {@link #getStartTag} and {@link #getEndTag} can be used to navigate over to
 * the paired element (and then possibly next in the textual order). Note that start
 * tag returns itself as start tag, and end tag returns itself from its {@code getEndTag}.
 * A self-closing tag will return itself from both methods.
 * <p/>
 * In order to access element's name or attributes, please use DOM API (e.g.
 * {@code element.getNode().getNodeName()} to get tag name).
 * 
 * @author Svatopluk Dedic
 * @since 1.60
 */
public interface TagElement extends SyntaxElement {

    /**
     * @return true, if the element is a regular start element
     */
    public boolean isStart();
    
    /**
     * @return true, if the element is a regular closing element.
     */
    public boolean isEnd();

    /**
     * @return true, if self-closing element without any textual content
     */
    public boolean isSelfClosing();

    /**
     * Start element for this TagElement. Returns itself if {@link #isStart} or {@link #isSelfClosing()} is true.
     * May return {@code null} if the document is not well formed
     * @return corresponding start element.
     */
    @CheckForNull
    public TagElement getStartTag();

    /**
     * Element element for this TagElement. Returns itself if {@link #isEnd} or {@link #isSelfClosing()} is true.
     * May return {@code null} if the document is not well formed
     * @return corresponding start element.
     */
    @CheckForNull
    public TagElement getEndTag();
    
}
