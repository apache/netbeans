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

package org.netbeans.modules.xml.text.syntax.dom;

import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import java.util.*;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

public class StartTag extends Tag {

    public StartTag(XMLSyntaxSupport support, TokenItem from, int to, String name, Collection attribs) {
        super( support, from, to, name, attribs );
    }

    public boolean hasChildNodes() {
        SyntaxElement next = getNext();
        if (next == null) return false;
        // if not well-formed
        if (next instanceof StartTag && ((StartTag)next).getEndTag() == null) return false;
        if (next instanceof EndTag) return false;
        return true;
    }
    
    public NodeList getChildNodes() {
        
        List list = new ArrayList();
        Node next = hasChildNodes() ? findNext(this) : null;
        
        while (next != null) {
            list.add(next);
            next = next.getNextSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    protected Tag getStartTag() {
        return this;
    }
    
    protected Tag getEndTag() {
        
        SyntaxNode next = findNext();
        
        while (next != null) {
            if (next instanceof EndTag) {
                // check well-formedness
                EndTag endTag = (EndTag) next;
                if (endTag.getTagName().equals(getTagName())) {
                    return endTag;
                } else {
                    return null;
                }
            } else if (next instanceof StartTag) {
                Tag startTag = (Tag) next;
                next = startTag.getEndTag();
                if (next == null) return null;
                next = next.findNext();
            } else {
                next = next.findNext();
            }
        }
        
        return null;
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer( "StartTag(\"" + name + "\" " );
        ret.append(getAttributes().toString());
        return ret.toString() + " " + first() + ")";
    }
    
}

