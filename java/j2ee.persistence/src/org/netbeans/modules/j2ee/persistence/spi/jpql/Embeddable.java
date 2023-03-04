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

import java.util.Map;
import org.eclipse.persistence.jpa.jpql.tools.spi.IEmbeddable;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeVisitor;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EmbeddableAttributes;
import org.netbeans.modules.j2ee.persistence.spi.jpql.support.JPAAttribute;

/**
 *
 * @author sp153251
 */
public class Embeddable extends ManagedType implements IEmbeddable{

    public Embeddable(org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable element, IManagedTypeProvider provider){
        super((PersistentObject)element, provider);     
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(IManagedTypeVisitor visitor) {
            visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
            return getType().getName();
    }

    @Override
    Map<String, IMapping> initMappings() {
        Map<String, IMapping> mappings = super.initMappings();
        org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable eb = (org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable) getPersistentObject();
        EmbeddableAttributes attrs = eb.getAttributes();
        if(attrs != null){
            Basic[] bs = attrs.getBasic();
            if(bs != null){
                for(Basic b1:bs){
                    mappings.put(b1.getName(), new Mapping(this, new JPAAttribute(getPersistentObject(), b1)));
                }
            }        
        }
        return mappings;
    }
    
    @Override
    Attributes getAttributes() {
        return null;//TODO embeddable attributes?
    }

}
