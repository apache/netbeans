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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class NamedQueryImpl implements NamedQuery {

    private String name;
    private String query;
    
    /**
     * 
     * @param element - entity or mapped superclass element
     * @param name
     * @param query 
     */
    public NamedQueryImpl(String name, String query){
        this.name = name;
        this.query = query;
    }

    @Override
    public void setName(String value) {
        name = value; // NOI18N
    }

    @Override
    public String getName() {
        return name; // NOI18N
    }

    @Override
    public void setQuery(String value) {
        query = value; // NOI18N
    }

    @Override
    public String getQuery() {
        return query; // NOI18N
    }

    @Override
    public void setHint(int index, QueryHint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public QueryHint getHint(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int sizeHint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public void setHint(QueryHint[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public QueryHint[] getHint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int addHint(QueryHint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public int removeHint(QueryHint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    @Override
    public QueryHint newQueryHint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
