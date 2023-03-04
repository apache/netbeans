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

package org.netbeans.modules.parsing.spi.indexing;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

/**
 * A factory class used to create {@link EmbeddingIndexer}s.
 * The {@link EmbeddingIndexerFactory} instances are registered in the {@link MimeLookup}
 * under the mime path corresponding to mime type of handled embeddings.
 * <div class="nonnormative">
 * <p>The {@link IndexingSupport} can be used to implement the {@link EmbeddingIndexerFactory}</p>
 * </div>
 * @author Tomas Zezula
 */
public abstract class EmbeddingIndexerFactory extends SourceIndexerFactory {
   
    /**
     * Creates  new {@link Indexer}.
     * @param indexing for which the indexer should be created
     * @param snapshot for which the indexer should be created
     * @return an indexer
     */
    public abstract EmbeddingIndexer createIndexer (final Indexable indexable, final Snapshot snapshot);
    
}
