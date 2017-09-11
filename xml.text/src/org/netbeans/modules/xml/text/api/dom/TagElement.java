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
