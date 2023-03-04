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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.ProxyBinaryIndexerFactory;
import org.openide.filesystems.FileObject;

/**
 * An binary indexer with declared constraints on the binary.
 * The subclasses of this indexer are registered by {@link ConstrainedBinaryIndexer.Registration}.
 * @author Tomas Zezula
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 1.48
 */
public abstract class ConstrainedBinaryIndexer {

    /**
     * Indexes given binary root.
     * @param files the files passed the file name and mime type constrain check,
     * categorized by the mime types. When only file name check is done the files
     * are passed with the mime type <code>content/unknown</code>
     * @param context of indexer, contains information about index storage, indexed root.
     */
    protected abstract void index(
        @NonNull Map<String, ? extends Iterable<? extends FileObject>> files,
        @NonNull Context context);

    /**
     * Notifies the indexer that a binary root is going to be scanned.
     *
     * @param context The indexed binary root.
     *
     * @return <code>false</code> means that the whole root should be rescanned
     *   (eg. no up to date check is done, etc)
     */
    protected boolean scanStarted (@NonNull final Context context) {
        return true;
    }

    /**
     * Notifies the indexer that scanning of a binary root just finished.
     *
     * @param context The indexed binary root.
     *
     */
    protected void scanFinished (@NonNull final Context context) {
    }

    /**
     * Called by indexing infrastructure to notify indexer that roots were deregistered,
     * for example the project owning these roots was closed. The indexer may free memory caches
     * for given roots or do any other clean up.
     *
     * @param removedRoots the iterable of removed roots
     */
    protected void rootsRemoved (@NonNull final Iterable<? extends URL> removedRoots) {
    }

    /**
     * The annotation to register {@link ConstrainedBinaryIndexer} with constraints on
     * the scanned binary. The registered indexer is loaded and executed only of
     * the constraints are fulfilled.
     *
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {

        /**
         * Return the name of this indexer. This name should be unique because
         * GSF will use this name to produce a separate data directory for each
         * indexer where it has its own storage.
         *
         * @return The indexer name. This does not need to be localized since it
         * is never shown to the user, but should contain filesystem safe
         * characters.
         *
         * @see BinaryIndexerFactory
         */
        String indexerName();

        /**
         * Return the version stamp of the schema that is currently being stored
         * by this indexer. Along with the index name this string will be used
         * to create a unique data directory for the database.
         *
         * Whenever you incompatibly change what is stored by the indexer,
         * update the version stamp.
         *
         * @return The version stamp of the current index.
         *
         * @see BinaryIndexerFactory
         */
        int indexVersion();

        /**
         * At least one of these resources has to be present to trigger this
         * indexer. Use
         * <code>{}</code> if the resource check should be skipped.
         *
         * @return one or more relative paths inside the binary files
         * that will be checked for existence. The paths are delimited by '/' and
         * do not start with delimiter.
         */
        String[] requiredResource() default {};

        /**
         * One or more mime types that have to be present inside of the binary
         * to enable this indexer. Use
         * <code>{}</code> if the mime type check should be skipped. The mime
         * type check can be expensive especially for mime types which require
         * file reading, for such mime types consider to prefer name pattern.
         *
         * @return one or more mimetypes this indexer processing
         */
        String[] mimeType() default {};

        /**
         * Regular expression of file names which have to be present
         * inside of the binary to enable this indexer. Use
         * <code>""</code> if the file name check should be skipped.
         *
         * @return file name regular expression this indexer processing
         * @since 1.50
         */
        String namePattern() default "";    //NOI18N
    }

    private static BinaryIndexerFactory create(final Map<String,Object> params) {
        return new ProxyBinaryIndexerFactory(params);
    }
}
