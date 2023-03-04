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
import org.netbeans.modules.websvc.wsitmodelext.mex.Dialect;
import org.netbeans.modules.websvc.wsitmodelext.mex.Identifier;
import org.netbeans.modules.websvc.wsitmodelext.mex.Location;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataReference;
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
public class MetadataSectionImpl extends MexComponentImpl implements MetadataSection {
    
    /**
     * Creates a new instance of MetadataSectionImpl
     */
    public MetadataSectionImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public MetadataSectionImpl(WSDLModel model){
        this(model, createPrefixedElement(MexQName.METADATA.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public Location getLocation() {      
        return getChild(Location.class);
    }

    public void setLocation(Location loc) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Location.class, LOCATION_PROPERTY, loc, classes);
    }

    public void removeLocation(Location loc) {
        removeChild(LOCATION_PROPERTY, loc);
    }
        
    public Dialect getDialect() {
        return getChild(Dialect.class);
    }

    public void setDialect(Dialect dialect) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Dialect.class, DIALECT_PROPERTY, dialect, classes);
    }

    public void removeDialect(Dialect dialect) {
        removeChild(DIALECT_PROPERTY, dialect);
    }

    public Identifier getIdentifier() {
        return getChild(Identifier.class);
    }

    public void setIdentifier(Identifier id) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Identifier.class, IDENTIFIER_PROPERTY, id, classes);
    }

    public void removeIdentifier(Identifier id) {
        removeChild(IDENTIFIER_PROPERTY, id);
    }

    public MetadataReference getMetadataReference() {
        return getChild(MetadataReference.class);
    }

    public void setMetadataReference(MetadataReference mReference) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(MetadataReference.class, METADATAREFERENCE_PROPERTY, mReference, classes);
    }

    public void removeMetadataReference(MetadataReference mReference) {
        removeChild(METADATAREFERENCE_PROPERTY, mReference);
    }

}
