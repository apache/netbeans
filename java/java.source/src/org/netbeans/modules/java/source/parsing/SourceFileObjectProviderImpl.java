/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation of {@link SourceFileObjectProvider} used by {@link JavacParser}
 * @author Tomas Zezula
 */
@ServiceProvider(service = SourceFileObjectProvider.class)
public final class SourceFileObjectProviderImpl implements SourceFileObjectProvider {

    @NonNull
    @Override
    public AbstractSourceFileObject createJavaFileObject (
            @NonNull final AbstractSourceFileObject.Handle handle,
            @NullAllowed final JavaFileFilterImplementation filter,
            @NullAllowed final CharSequence content,
            final boolean renderNow,
            final boolean embedded) throws IOException {
        return new SourceFileObject (
            handle,
            filter,
            content,
            renderNow,
            embedded);
    }
}
