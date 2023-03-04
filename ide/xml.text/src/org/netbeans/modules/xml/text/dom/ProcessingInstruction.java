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

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.api.lexer.Token;

import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.spi.dom.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Read-only PI DOM node.
 *
 * @author  Petr Kuzel
 */
public final class ProcessingInstruction extends SyntaxNode implements org.w3c.dom.ProcessingInstruction {
    
    private String target;
    private String content;

    /** Creates a new instance of ProcessingInstructionImpl */
    public ProcessingInstruction(XMLSyntaxSupport syntax, Token<XMLTokenId> first,
            int start, int end, String target, String content) {
        super(syntax, first, start, end);
        this.target = target;
        this.content = content;
    }
    
    /**
     * A code representing the type of the underlying object, as defined above.
     */
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }
    
    public String getNodeName() {
        return target;
    }
        
    public String getNodeValue() {
        return content;
    }
    
    /**
     * Once again we are read-only implemetation!
     */
    public void setData(String data) throws DOMException {
        throw new ROException();
    }

    @Override
    public String getTarget() {
        return getNodeName();
    }

    @Override
    public String getData() {
        return getNodeValue();
    }
}
