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

package org.netbeans.modules.websvc.wsitmodelext.rm.impl;

import org.netbeans.modules.websvc.wsitmodelext.rm.MaxReceiveBufferSize;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMSQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class MaxReceiveBufferSizeImpl extends RMMSComponentImpl implements MaxReceiveBufferSize {
    
    /**
     * Creates a new instance of MaxReceiveBufferSizeImpl
     */
    public MaxReceiveBufferSizeImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MaxReceiveBufferSizeImpl(WSDLModel model){
        this(model, createPrefixedElement(RMMSQName.MAXRECEIVEBUFFERSIZE.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setMaxReceiveBufferSize(String maxReceiveBufferSize) {
        setText(MAX_RECEIVE_BUFFER_SIZE_CONTENT_VALUE_PROPERTY, maxReceiveBufferSize);
    }

    public String getMaxReceiveBufferSize() {
        return getText();
    }    
}
