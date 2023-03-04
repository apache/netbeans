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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.impl.WSDLSchemaImpl;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Nam Nguyen
 */
public class TypesImpl extends WSDLComponentBase implements Types {
    private boolean registeredSchemaQNameAttributes;
    
    /** Creates a new instance of TypesImpl */
    public TypesImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public TypesImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.TYPES.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public Collection<Schema> getSchemas(){
        //get list of WSDLSchemas
        List<Schema> schemas = new ArrayList<Schema>();
        List<WSDLSchema> wsdlSchemas = getExtensibilityElements(WSDLSchema.class);
        
        for(WSDLSchema wsdlSchema : wsdlSchemas){
            schemas.add(wsdlSchema.getSchemaModel().getSchema());
            if (! registeredSchemaQNameAttributes) {
                AbstractDocumentModel schemaModel = (AbstractDocumentModel) wsdlSchema.getSchemaModel();
                getModel().getAccess().addQNameValuedAttributes(schemaModel.getQNameValuedAttributes());
                registeredSchemaQNameAttributes = true;
            }
        }
        return schemas;
    }

    @Override
    protected <N extends Node> void updateReference(Element peer, List<N> pathToRoot) {
        super.updateReference(peer, pathToRoot);
        int iPeer = pathToRoot.indexOf(peer);
        assert iPeer > -1 : "Provided peer is outside context path";
        if (iPeer > 0) {
            for (WSDLSchema wschema : getExtensibilityElements(WSDLSchema.class)) {
                Schema schema = wschema.getSchemaModel().getSchema();
                if (schema.referencesSameNode(pathToRoot.get(iPeer-1))) {
                    ((WSDLSchemaImpl)wschema).updateReference(schema.getPeer());
                    break;
                }
            }
        }
    }

    public List<EmbeddableRoot> getAdoptedChildren() {
        return new ArrayList<EmbeddableRoot>(getSchemas());
    }
}
