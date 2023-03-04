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
