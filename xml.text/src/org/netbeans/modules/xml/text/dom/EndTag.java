/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import java.util.*;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.w3c.dom.*;
import org.netbeans.modules.xml.spi.dom.*;

/**
 * End element of ELEMENT_NODE.
 * //??? should it be implementing Node?
 */
public class EndTag extends Tag {

    public EndTag(XMLSyntaxSupport support, Token<XMLTokenId> from, int start, int end) {
        super(support, from, start, end, from.text().toString().substring(2));
        if (name.equals("")) { // NOI18N
            // self-closing tag ? -- TODO
        }
    }

    /**
     * Create properly bound attributes
     */
    public synchronized org.w3c.dom.NamedNodeMap getAttributes() {
        Tag start = getStartTag();
        if (start != null) {
            return start.getAttributes();
        } else {
            return NamedNodeMapImpl.EMPTY;
        }
    }
    
    public boolean hasChildNodes() {
        BaseSyntaxElement prev = getPrevious();
        if (prev == null) return false;
        if (prev instanceof EndTag && ((EndTag)prev).getStartTag() == null) return false;
        if (prev instanceof StartTag) return false;
        return true;
    }
    
    public NodeList getChildNodes() {
        
        List list = new ArrayList();
        Node prev = hasChildNodes() ? findPrevious(this) : null;
        
        while (prev != null) {
            list.add(0, prev);
            prev = prev.getPreviousSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    public Tag getStartTag() {
        
        SyntaxNode prev = findPrevious();
        
        while (prev != null) {
            if (prev instanceof StartTag) {
                // check well-formedness
                StartTag startTag = (StartTag) prev;
                if (startTag.getNodeName().equals(getNodeName())) {
                    return startTag;
                } else {
                    return null;
                }
            } else if (prev instanceof EndTag) {
                EndTag endTag = (EndTag) prev;
                prev = endTag.getStartTag();
                if (prev == null) return null;
                prev = prev.findPrevious();
            } else {
                prev = prev.findPrevious();
            }
        }
        
        return null;
    }
    
    public Tag getEndTag() {
        return this;
    }

    @Override
    public boolean isStart() {
        return false;
    }

    @Override
    public boolean isEnd() {
        return true;
    }

    @Override
    public boolean isSelfClosing() {
        return false;
    }

    
    public String toString() {
        return "EndTag(\"" + name + "\") " + first();
    }
    
}

