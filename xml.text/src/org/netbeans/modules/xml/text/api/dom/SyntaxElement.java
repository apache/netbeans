/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
 * @sice 1.60
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
