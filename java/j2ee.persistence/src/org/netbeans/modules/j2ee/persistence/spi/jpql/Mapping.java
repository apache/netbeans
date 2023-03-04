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
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.lang.annotation.Annotation;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMappingType;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration;
import org.netbeans.modules.j2ee.persistence.spi.jpql.support.JPAAttribute;

/**
 *
 * @author sp153251
 */
public class Mapping implements IMapping {
    
    private final ManagedType parent;
    private IMappingType mappingType;
    private IType type;
    private JPAAttribute attribute;


    public Mapping(ManagedType parent, JPAAttribute attrib){
        this.parent = parent;
        this.attribute = attrib;
    }
    

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public IManagedType getParent() {
        return parent;
    }

    @Override
    public IType getType() {
        if(type == null){
            if(attribute.getType() != null) {
               type = parent.getProvider().getTypeRepository().getType(attribute.getType().getQualifiedName().toString());
            } else if(attribute.getClass1() !=null) {
                type = parent.getProvider().getTypeRepository().getType(attribute.getClass1());
            } else {
                type = parent.getProvider().getTypeRepository().getType(attribute.getTypeName());
                if(type == null) {
                    //fall back to simplest definition.
                    type = new Type(parent.getProvider().getTypeRepository(), attribute.getTypeName());
                }
            }
        }
        return type;
    }

    @Override
    public ITypeDeclaration getTypeDeclaration() {
        return getType().getTypeDeclaration();
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(IMapping o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public int getMappingType() {
        return attribute.getMappingType();
    }

    @Override
    public boolean isCollection() {
        return (attribute.getMappingType() == IMappingType.ELEMENT_COLLECTION || attribute.getMappingType() == IMappingType.ONE_TO_MANY || attribute.getMappingType() == IMappingType.MANY_TO_MANY);
    }

    @Override
    public boolean isProperty() {
        return (attribute.getMappingType() == IMappingType.BASIC) || (attribute.getMappingType() == IMappingType.ID);
    }

    @Override
    public boolean isRelationship() {
        return (attribute.getMappingType() == IMappingType.MANY_TO_MANY) || (attribute.getMappingType() == IMappingType.MANY_TO_ONE) || (attribute.getMappingType() == IMappingType.ONE_TO_MANY) || (attribute.getMappingType() == IMappingType.ONE_TO_ONE);
        
    }

    @Override
    public boolean isTransient() {
        return (attribute.getMappingType() == IMappingType.TRANSIENT);
    }

    @Override
    public boolean isEmbeddable() {
        return (attribute.getMappingType() == IMappingType.EMBEDDED) || (attribute.getMappingType() == IMappingType.EMBEDDED_ID);
    }
    
}
