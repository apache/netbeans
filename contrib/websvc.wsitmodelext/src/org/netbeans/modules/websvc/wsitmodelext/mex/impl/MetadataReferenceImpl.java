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

package org.netbeans.modules.websvc.wsitmodelext.mex.impl;

import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address10;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataReference;
import org.netbeans.modules.websvc.wsitmodelext.mex.MexQName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class MetadataReferenceImpl extends MexComponentImpl implements MetadataReference {
    
    /**
     * Creates a new instance of MetadataReferenceImpl
     */
    public MetadataReferenceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MetadataReferenceImpl(WSDLModel model){
        this(model, createPrefixedElement(MexQName.METADATAREFERENCE.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public Address10 getAddress() {
        return getChild(Address10.class);
    }

    public void setAddress(Address10 addr) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Address10.class, ADDRESS_PROPERTY, addr, classes);
    }

    public void removeAddress(Address10 addr) {
        removeChild(ADDRESS_PROPERTY, addr);
    }

}
