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
import org.netbeans.modules.websvc.wsitmodelext.mex.Metadata;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataSection;
import org.netbeans.modules.websvc.wsitmodelext.mex.MexQName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class MetadataImpl extends MexComponentImpl implements Metadata {
    
    /**
     * Creates a new instance of MetadataImpl
     */
    public MetadataImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MetadataImpl(WSDLModel model){
        this(model, createPrefixedElement(MexQName.METADATA.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public MetadataSection getMetadataSection() {
        return getChild(MetadataSection.class);
    }

    public void setMetadataSection(MetadataSection mSection) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(MetadataSection.class, METADATASECTION_PROPERTY, mSection, classes);
    }

    public void removeMetadataSection(MetadataSection mSection) {
        removeChild(METADATASECTION_PROPERTY, mSection);
    }

}
