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

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.model.*;

/**
 *
 * @author Ayub Khan
 */
public class VenetianBlind extends GardenOfEden {
    
    /**
     * Creates a new instance of VenetianBlind
     */
    public VenetianBlind(SchemaGenerator.Mode mode) {
        super(mode);
    }
    
    protected SchemaGenerator.Pattern getSchemaDesignPattern() {
        return SchemaGenerator.Pattern.VENITIAN_BLIND;
    }
    
    public void visit(Element element) {
        prepareLocalElement(element);
    }
    
    protected void setPeer(final Element element,
            final org.netbeans.modules.xml.schema.model.Element e,
            final ElementReference eref) {
        if(element.getPeer() != null && element.getChildren().size() > 0 &&
                SchemaGeneratorUtil.isGlobalElement(element)) {
            LocalType lct = SchemaGeneratorUtil.getLocalComplexType(e);
            if(lct == null)
                lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
            assert lct != null;
            scParent = lct;
        } else
            super.setPeer(element, e, eref);
    }
    
    protected SchemaComponent getParent(
            AXIComponent axiparent) throws IllegalArgumentException {
        SchemaComponent scParent = null;
        if(axiparent instanceof Element &&
                (SchemaGeneratorUtil.isGlobalElement(axiparent) /*||
                                        isSimpleElementStructure((Element)axiparent)*/)) {
            SchemaComponent e = axiparent.getPeer();
            if(e instanceof ElementReference)
                e = ((ElementReference)e).getRef().get();
            assert e != null;
            SchemaComponent lct = SchemaGeneratorUtil.getLocalComplexType(e);
            if(lct == null) {
                lct = SchemaGeneratorUtil.getGlobalComplexType(e);
                if(lct == null)
                    lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
            }
            assert lct != null;
            scParent = lct;
        } else {
            scParent = super.getParent(axiparent);
        }
        return scParent;
    }
}
