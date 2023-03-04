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

package org.netbeans.modules.profiler.oql.repository.api;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Value object for an OQL query category<br/>
 * Use {@linkplain OQLQueryRepository#listCategories()} or its variants
 * to obtain this class instances.
 * @author Jaroslav Bachorik
 */
public final class OQLQueryCategory {
    private final String id;
    private final String name;
    private final String description;
    private final OQLQueryRepository repository;
    
    OQLQueryCategory(@NonNull OQLQueryRepository repository, @NonNull String id,
                     @NonNull String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.repository = repository;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @NonNull
    String getID() {
        return id;
    }

    @NonNull
    public List<? extends OQLQueryDefinition> listQueries() {
        return repository.listQueries(this);
    }

    @NonNull
    public List<? extends OQLQueryDefinition> listQueries(@NonNull String pattern) {
        return repository.listQueries(this, pattern);
    }

    @Override
    public String toString() {
        return name;
    }
}
