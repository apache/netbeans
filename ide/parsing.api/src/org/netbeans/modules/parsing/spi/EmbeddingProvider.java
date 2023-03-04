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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;


/**
 * EmbeddingProvider returns sources for embedded languages ({@link Embedding}s)
 * based on lexical analyse of current snapshot. Embedded
 * source can consist from one or more blocks of original source and it can contain
 * some generated parts that has no mirror in the original text. See 
 * {@link Snapshot} class for more information how to create embedded source.
 *
 * @author Jan Jancura
 */
public abstract class EmbeddingProvider extends SchedulerTask {
    
    
    /**
     * Returns {@link Scheduler} class for this SchedulerTask. See
     * {@link Scheduler} documentation for a list of default schedulers,
     * or your your own implementation.
     * 
     * @return              {@link Scheduler} for this SchedulerTask.
     */
    public final Class<? extends Scheduler> getSchedulerClass () {
        return null;
    }

    /**
     * Returns list of {@link Embedding}s based on lexical analyse.
     * 
     * @param snapshot      A snapshot that should be scanned for embeddings.
     * @return              List of {@link Embedding}s.
     */
    public abstract List<Embedding> getEmbeddings (Snapshot snapshot);
    
    /**
     * Returns priority of this source provider.
     * @return              priority of this source provider
     */
    public abstract int getPriority ();

    /**
     * Registration of the {@link EmbeddingProvider}.
     * Creates a mime lookup registration of the {@link TaskFactory} for
     * annotated {@link EmbeddingProvider}. It also provides the target
     * mime type of the created embedding which allows the indexing QuerySupport
     * to correctly mark dirty embedded indexers.
     *
     * @since 1.57
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public @interface Registration {

        /**
         * Mime type to which should be the given {@link EmbeddingProvider} registered.
         */
         public String mimeType();

        /**
         * Mime type of the embedding created by the registered {@link EmbeddingProvider}
         */
        String targetMimeType();
    }
}




