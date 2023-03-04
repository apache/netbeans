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

import org.eclipse.persistence.jpa.jpql.tools.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.tools.spi.IQuery;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery;

/**
 *
 * @author sp153251
 */
public class Query implements IQuery{

    private String queryStr;
    private IManagedTypeProvider provider;
    private NamedQuery query;
    
    
    
    public Query(NamedQuery query, String queryStr, IManagedTypeProvider provider){
        this.queryStr = queryStr;
        this.provider = provider;
        this.query = query;
    }
    
    public Query(NamedQuery query, IManagedTypeProvider provider){
        this(query, query.getQuery(), provider);
    }    
    @Override
    public String getExpression() {
        return queryStr;
    }

    @Override
    public IManagedTypeProvider getProvider() {
        return provider;
    }
    
    public NamedQuery getNamedQuery(){
        return query;
    }

    @Override
    public String toString() {
            return super.toString() + ", q = " + getExpression();
    }
}
