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
package org.netbeans.modules.java.preprocessorbridge.spi;

import com.sun.source.tree.CompilationUnitTree;
import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * JavaCustomIndexer plugin called during scan on fully attributed trees.
 * @since 1.22
 * @author Tomas Zezula
 */
public interface JavaIndexerPlugin {


    /**
     * Process given attributed compilation unit.
     * @param toProcess the compilation unit to process
     * @param indexable the file being indexed
     * @param services a {@link Lookup} containing javac services (Elements, Types, Trees)
     */
    public void process (@NonNull CompilationUnitTree toProcess, @NonNull Indexable indexable, @NonNull Lookup services);

    /**
     * Handles deletion of given source file.
     * @param indexable the deleted file
     */
    public void delete (@NonNull Indexable indexable);

    /**
     * Called when the {@link JavaIndexerPlugin} is not more used.
     * The implementor may do any clean up, storing of metadata.
     */
    public void finish ();

    /**
     * Factory to create JavaIndexerPlugin.
     * The factory instance should be registered in mime lookup.
     */
    public interface Factory {
        /**
         * Creates a new instance of {@link JavaIndexerPlugin}.
         * @param root the source root for which the plugin is created,
         * may not exist (if the root was deleted).
         * @param cacheFolder used to store metadata
         * @return the new instance of {@link JavaIndexerPlugin} or null
         * if the factory does not handle given source root
         */
        @CheckForNull
        JavaIndexerPlugin create(@NonNull URL root, @NonNull FileObject cacheFolder);
    }
}
