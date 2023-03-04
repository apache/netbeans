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

package org.netbeans.modules.parsing.spi;

import java.util.List;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;


/**
 * ParserBasedEmbeddingProvider returns sources for embedded 
 * languages based on parser result for current language and snapshot. Embedded
 * snapshot can consist from one or more blocks of original snapshot and it can contain
 * some generated parts that has no mirror in the original text. See 
 * {@link Snapshot} class for more information how to create embedded snapshot.
 *
 * @author Jan Jancura
 */
public abstract class ParserBasedEmbeddingProvider<T extends Parser.Result> extends SchedulerTask {
    
    /**
     * Returns list of {@link Embedding}s based on parser results.
     * 
     * @param result        A parser result.
     * @return              List of embedded sources.
     */
    public abstract List<Embedding> getEmbeddings (
        T                   result
    );
    
    /**
     * Returns priority of this source provider.
     * @return              priority of this source provider
     */
    public abstract int getPriority ();
}




