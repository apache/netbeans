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
package org.netbeans.modules.parsing.implspi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SourceFactory {

    private static final SourceFactory DEFAULT_SOURCE_FACTORY = new DefaultSourceFactory();

    @NonNull
    public static SourceFactory getDefault() {
        SourceFactory sf = Lookup.getDefault().lookup(SourceFactory.class);
        if (sf == null) {
            sf = DEFAULT_SOURCE_FACTORY;
        }
        return sf;
    }

    @CheckForNull
    public abstract Source createSource(
            @NonNull FileObject fo,
            @NonNull String mimeType,
            @NonNull Lookup contents);

    @CheckForNull
    public abstract Source getSource(@NonNull FileObject fo);

    @CheckForNull
    public abstract Source removeSource(@NonNull FileObject fo);

    protected final Source newSource(
            @NonNull final FileObject fo,
            @NonNull final String mimeType,
            @NonNull final Lookup context) {
        return SourceAccessor.getINSTANCE().create(fo, mimeType, context);
    }

    private static class DefaultSourceFactory extends SourceFactory {

        private final Map<FileObject, Reference<Source>> instances = new WeakHashMap<>();

        @Override
        public Source createSource(
                @NonNull FileObject file,
                @NonNull String mimeType,
                @NonNull Lookup context) {
            Parameters.notNull("file", file);   //NOI18N
            Parameters.notNull("mimeType", mimeType);   //NOI18N
            Parameters.notNull("context", context);   //NOI18N
            final Reference<Source> sourceRef = instances.get(file);
            Source source = sourceRef == null ? null : sourceRef.get();
            if (source == null || !mimeType.equals(source.getMimeType())) {
                source = newSource(file, mimeType, context);
                instances.put(file, new WeakReference<>(source));
            }
            return source;
        }

        @Override
        public Source getSource(@NonNull final FileObject file) {
            Parameters.notNull("file", file);   //NOI18N
            final Reference<Source> ref = instances.get(file);
            return ref == null ? null : ref.get();
        }

        @Override
        public Source removeSource(@NonNull final FileObject file) {
            Parameters.notNull("file", file);   //NOI18N
            final Reference<Source> ref = instances.remove(file);
            return ref == null ? null : ref.get();
        }
    }
}
