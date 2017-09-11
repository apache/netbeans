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

import javax.swing.text.BadLocationException;
import org.w3c.dom.*;

import org.netbeans.editor.TokenItem;

import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
/**
 * Read-only PI DOM node.
 *
 * @author  Petr Kuzel
 */
public final class ProcessingInstructionImpl extends SyntaxNode {
    
    
    /** Creates a new instance of ProcessingInstructionImpl */
    public ProcessingInstructionImpl(XMLSyntaxSupport syntax, TokenItem from, int to) {
        super(syntax, from, to);
    }
    
    /**
     * A code representing the type of the underlying object, as defined above.
     */
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }
    
    /**
     * The target of this processing instruction. XML defines this as being
     * the first token following the markup that begins the processing
     * instruction.
     * @return implementation may return "xml" as it consider it a PI
     */
    public String getTarget() {
        TokenItem target = first().getNext();
        if (target != null) {
            return target.getImage();
        } else {
            return "";  //??? or null
        }
    }
    
    public String getNodeName() {
        return getTarget();
    }
    
    /**
     * The content of this processing instruction. This is from the first non
     * white space character after the target to the character immediately
     * preceding the <code>?&gt;</code>.
     * @return may return ""
     */
    public String getData() {
        StringBuffer buf = new StringBuffer();
        TokenItem next = first().getNext();
        while (next != null && next.getTokenID() != XMLDefaultTokenContext.PI_CONTENT) {
            next = next.getNext();
        }
        if (next == null) return "";  //??? or null
        do {
            buf.append(next.getImage());
            next = next.getNext();
        } while (next != null && next.getTokenID() == XMLDefaultTokenContext.PI_CONTENT);
        return buf.toString();
    }
    
    public String getNodeValue() {
        return getData();
    }
    
    /**
     * Once again we are read-only implemetation!
     */
    public void setData(String data) throws DOMException {
        throw new ROException();
    }
    
}
