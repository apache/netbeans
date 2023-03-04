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
package org.netbeans.modules.parsing.lucene.support;

import java.io.IOException;
import java.util.Collection;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Document based index allowing arbitrary Lucene Query.
 * @author Tomas Zezula
 * @since 2.24
 */
public interface DocumentIndex2 extends DocumentIndex {

    /**
     * Performs the Lucene query on the index.
     * @param query to perform
     * @param fieldsToLoad  fields to load into returned document
     * @return  the Collection of {@link IndexDocument} matching the query.
     * @throws IOException  in case of IO error.
     * @throws InterruptedException if the search is interrupted.
     */
    @NonNull
    public  <T> Collection<? extends T> query (
            @NonNull Query query,
            @NonNull Convertor<? super IndexDocument, ? extends T> convertor,
            @NullAllowed String... fieldsToLoad) throws IOException, InterruptedException;

    /**
     * Transactional {@link DocumentIndex2}.
     */
    public interface  Transactional extends DocumentIndex2, DocumentIndex.Transactional {
    }
}
