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

import org.eclipse.persistence.jpa.jpql.tools.spi.IEntity;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeVisitor;
import org.eclipse.persistence.jpa.jpql.tools.spi.IQuery;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery;

/**
 *
 * @author sp153251
 */
public class Entity extends ManagedType implements IEntity {

    public Entity(org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity entity, IManagedTypeProvider provider){
        super((PersistentObject) entity, provider);
    }

    @Override
    public void accept(IManagedTypeVisitor imtv) {
        imtv.visit(this);
    }

    @Override
    public String getName() {
        return ((org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity)getPersistentObject()).getName();
    }

    @Override
    public IQuery getNamedQuery(String string) {
        NamedQuery nq = ((org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity)getPersistentObject()).newNamedQuery();
        nq.setQuery(string);
        nq.setName("");
        return new Query(nq, string, getProvider());
    }

    @Override
    Attributes getAttributes() {
        org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity entity = (org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity) getPersistentObject();
        return entity.getAttributes();
    }
}
