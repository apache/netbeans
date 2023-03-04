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

package org.netbeans.modules.xml.xdm.nodes;
import java.util.List;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;
import org.w3c.dom.CDATASection;

/**
 *
 * @author Ajit
 */
public class CData extends Text implements CDATASection {
    
    public void accept(XMLNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    CData() {
        super();
    }

    CData(String text) {
        this();
        stripCDataMarkers(text);
    }

    private void stripCDataMarkers(String data) {
        String normalizedData = data == null ? "" : data; //NOI18N

        // remove start and end CDATA
        if (data != null && data.startsWith(Token.CDATA_START.getValue()) 
                && data.endsWith(Token.CDATA_END.getValue())) 
        {
            assert data.length() >= Token.CDATA_START.getValue().length() +
            Token.CDATA_END.getValue().length();
            normalizedData = 
                data.substring(Token.CDATA_START.getValue().length(),
                data.length() - Token.CDATA_END.getValue().length());
        }
        setData(normalizedData);
    }
    
    private void addCDataTokens() {
        List<Token> tokens = getTokensForWrite();
        tokens.add(0,Token.CDATA_START);
        tokens.add(Token.CDATA_END);
        setTokens(tokens);
    }
    
    @Override
    public String getNodeValue() {
        return getData();
    }
    
    @Override
    public void setData(String data) {
        super.setData(data);
        addCDataTokens();
    }
    
    @Override
    public short getNodeType() {
        return Node.CDATA_SECTION_NODE;
    }

    @Override
    public String getNodeName() {
        return "#cdata-section"; //NOI18N
    }

}
