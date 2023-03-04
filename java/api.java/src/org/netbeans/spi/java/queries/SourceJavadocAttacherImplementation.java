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
package org.netbeans.spi.java.queries;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/**
 * A SPI for attaching source roots and javadoc roots to binary roots.
 * The implementations of this interface are registered in global {@link Lookup}.
 * @see ServiceProvider
 * @author Tomas Zezula
 * @since 1.35
 */
public interface SourceJavadocAttacherImplementation {

    /**
     * Attaches a source root provided by this SPI to given binary root.
     * @param root the binary root to attach sources to
     * @param a listener notified about result when attaching is done
     * @throws IOException in case of error
     */
    boolean attachSources(
            @NonNull URL root,
            @NonNull SourceJavadocAttacher.AttachmentListener listener) throws IOException;

    /**
     * Attaches a javadoc root provided by this SPI to given binary root.
     * @param root the binary root to attach javadoc to
     * @param a listener notified about result when attaching is done
     * @throws IOException in case of error
     */
    boolean attachJavadoc(
            @NonNull URL root,
            @NonNull SourceJavadocAttacher.AttachmentListener listener) throws IOException;

    /**
     * Extension into the default {@link SourceJavadocAttacherImplementation} allowing to download or find sources and javadoc for given binary.
     * The extension implementation is registered in the global {@link org.openide.util.Lookup}. Multiple Definers can be defined. Consider to implement
     * {@link Definer2} to indicate the Definer is willing to handle the root.
     * @since 1.49
     */
    interface Definer {
        /**
         * Returns the display name of the {@link Definer}.
         * @return the display name, for example "Maven Repository"
         */
        @NonNull
        String getDisplayName();

        /**
         * Returns the description of the {@link Definer}.
         * @return the description, for example "Downloads artifacts from Maven repository"
         */
        @NonNull
        String getDescription();

        /**
         * Returns the list of downloaded sources which should be attached to the root.
         * @param root the root to download sources for
         * @param cancel the {@link Callable} returning true if the download should be canceled
         * @return the list of source roots
         * Threading: Called in background thread.
         */
        @NonNull
        List<? extends URL> getSources(@NonNull URL root, @NonNull Callable<Boolean> cancel);

        /**
         * Returns the list of downloaded javadocs which should be attached to the root.
         * @param root the root to download javadoc for
         * @param cancel the {@link Callable} returning true if the download should be canceled
         * @return the list of javadoc roots
         * Threading: Called in background thread.
         */
        @NonNull
        List<? extends URL> getJavadoc(@NonNull URL root, @NonNull Callable<Boolean> cancel);
    }
    
    /**
     * Extends the {@link Definer} interface to work better if several providers are defined.
     * The Definer can indicate if it is willing to handle the artifact. For example, Maven may 
     * reject artifacts that are outside of M2 local repository.
     * @since 1.78
     */
    interface Definer2 extends Definer {
        /**
         * Determines if the binary root is acceptable for the Definer. Definers that use
         * binary filename, path or some database to gather coordinates or location for download
         * may reject roots that cannot be processed.
         */
        public boolean accepts(@NonNull URL root);
    }
}
