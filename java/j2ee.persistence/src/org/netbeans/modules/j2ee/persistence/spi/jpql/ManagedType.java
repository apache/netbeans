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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedType;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embedded;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EmbeddedId;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToOne;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToOne;
import org.netbeans.modules.j2ee.persistence.spi.jpql.support.JPAAttribute;

/**
 *
 * @author sp153251
 */
public abstract class ManagedType implements IManagedType {
    private final PersistentObject element;
    private final IManagedTypeProvider provider;
    private Map<String, IMapping> mappings;
    private IType type;

    public ManagedType(PersistentObject element, IManagedTypeProvider provider){
        this.element = element;
        this.provider = provider;
    }
    

    @Override
    public IMapping getMappingNamed(String val) {
        if(mappings == null) {
            mappings = initMappings();
        }
        return mappings.get(val);
    }

    @Override
    public IManagedTypeProvider getProvider() {
        return provider;
    }

    @Override
    public IType getType() {
        if (type == null) {
            if(((ManagedTypeProvider)provider).isValid()){
                type = provider.getTypeRepository().getType(element.getTypeElement().getQualifiedName().toString());
            }
        }
        return type;
    }

    @Override
    public Iterable<IMapping> mappings() {
        if(mappings == null) {
            mappings = initMappings();
        }
        return Collections.unmodifiableCollection(mappings.values());
    }

    @Override
    public int compareTo(IManagedType o) {
        return getType().getName().compareTo(o.getType().getName());
    }
    
    PersistentObject getPersistentObject(){
        return element;
    }
    
    Map<String, IMapping> initMappings() {
        mappings = new HashMap<String, IMapping>();
        Attributes atrs = getAttributes();
        if(atrs != null){
            ManyToMany[] mms = atrs.getManyToMany();
            if(mms != null){
                for(ManyToMany mm1:mms){
                    mappings.put(mm1.getName(), new Mapping(this, new JPAAttribute(element, mm1)));
                }
            }
            ManyToOne[] mos = atrs.getManyToOne();
            if(mos != null){
                for(ManyToOne mo1:mos){
                    mappings.put(mo1.getName(), new Mapping(this, new JPAAttribute(element, mo1)));
                }
            }
            OneToOne[] oos = atrs.getOneToOne();
            if(oos != null){
                for(OneToOne oo1:oos){
                    mappings.put(oo1.getName(), new Mapping(this, new JPAAttribute(element, oo1)));
                }
            }
            OneToMany[] oms = atrs.getOneToMany();
            if(oms != null){
                for(OneToMany om1:oms){
                    mappings.put(om1.getName(), new Mapping(this, new JPAAttribute(element, om1)));
                }
            }
            Basic[] bs = atrs.getBasic();
            if(bs != null){
                for(Basic b1:bs){
                    mappings.put(b1.getName(), new Mapping(this, new JPAAttribute(element, b1)));
                }
            }
            Id[] ids = atrs.getId();
            if(ids != null){
                for(Id id1:ids){
                    mappings.put(id1.getName(), new Mapping(this, new JPAAttribute(element, id1)));
                }
            }
            try {
                Embedded[] es = atrs.getEmbedded();
                if(es != null){
                    for(Embedded e1:es){
                        mappings.put(e1.getName(), new Mapping(this, new JPAAttribute(element, e1)));
                    }
                }
            } catch (UnsupportedOperationException ex){
                //TODO: implements embedded in attributes
            }
            try {
                EmbeddedId eds = atrs.getEmbeddedId();
                if(eds != null){
                    mappings.put(eds.getName(), new Mapping(this, new JPAAttribute(element, eds)));
                }
            } catch (UnsupportedOperationException ex){
                //TODO: implements embedded in attributes
            }
        }
        return mappings;
    }
    
    abstract Attributes getAttributes(); 
    
}
