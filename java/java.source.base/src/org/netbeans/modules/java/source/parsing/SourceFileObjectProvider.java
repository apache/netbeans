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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;

/**
 * Factory for creating {@link JavaFileObject}s used by the {@link JavacParser}.
 * The unit tests may implement this interface to force the {@link JavacParser} to
 * use different implementation of the {@link JavaFileObject}.
 * @see JavacParser
 * @see JavaFileObject
 * @see JavaFileManager
 * @author Tomas Zezula
 */
public interface SourceFileObjectProvider {
        
    /**
     * Creates {@link JavaFileObject} for given file under given root.
     * @param handle for which the {@link JavaFileObject} should be created
     * @param filter used to read the content
     * @param content of the file object if null, the snapshot from fo is taken
     * @param renderNow should be the file content rendered immediately
     * @return the {@link JavaFileObject}
     * @throws java.io.IOException on io failure.
     */
    @NonNull
    public abstract AbstractSourceFileObject createJavaFileObject (
            @NonNull AbstractSourceFileObject.Handle handle,
            @NullAllowed JavaFileFilterImplementation filter,
            @NullAllowed CharSequence content,
            boolean renderNow) throws IOException;
}
