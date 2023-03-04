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

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 *
 * @author rico
 */
public class  FindGlobalReferenceVisitor <T extends NamedReferenceable> extends DefaultSchemaVisitor{
    private Class<T> elementType;
    private String localName;
    private Schema schema;
    private T refType;
    private boolean found;
    
    public T find(Class<T> elementType, String localName, Schema schema){
        if (elementType == null || localName == null || schema == null) {
            throw new IllegalArgumentException("elementType == null");
        }

        this.elementType = elementType;
        this.localName = localName;
        this.schema = schema;
        found = false;
        schema.accept(this);
        return refType;
    }
    
    public void visit(Schema schema) {
        List<SchemaComponent> ch = schema.getChildren();
        for (SchemaComponent c : ch) {
            c.accept(this);
            if(found) return;
        }
    }
    
    public void  visit(GlobalAttributeGroup e) {
        findReference(GlobalAttributeGroup.class, e);
    }
    
    public void visit(GlobalGroup e) {
        findReference(GlobalGroup.class, e);
    }
    
    public void visit(GlobalAttribute e) {
        findReference(GlobalAttribute.class, e);
    }
    
    public void visit(GlobalElement e) {
        findReference(GlobalElement.class, e);
    }
    
    public void visit(GlobalSimpleType e) {
        findReference(GlobalSimpleType.class, e);
    }
    
    public void visit(GlobalComplexType e) {
        findReference(GlobalComplexType.class, e);
    }
    
    public void visit(Notation e) {
        findReference(Notation.class, e);
    }
    
    private void findReference(Class<? extends NamedReferenceable> refClass,
	NamedReferenceable n) {
	if(elementType.isAssignableFrom(refClass)){
	    if(localName.equals(n.getName())){
		refType = elementType.cast(n);
		found = true;
	    }
	}
    }
    
}
