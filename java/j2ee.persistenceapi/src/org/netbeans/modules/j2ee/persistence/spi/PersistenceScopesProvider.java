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

package org.netbeans.modules.j2ee.persistence.spi;

import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.openide.filesystems.FileObject;

/**
 * Provides a {@link PersistenceScopes} instance. This interface should be
 * implemented together with {@link PersistenceScopeProvider}, even if
 * only a single {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScope}
 * is provided.
 *
 * @author Andrei Badea
 */
public interface PersistenceScopesProvider {

    /**
     * Returns a {@link PersistenceScopes} for the current context.
     *
     * @return an instance of <code>PersistenceScopes</code>; never null.
     */
    PersistenceScopes getPersistenceScopes();

    /**
     * Returns a {@link PersistenceScopes} for the context associated with the given FileObject.
     *
     * @param fo the FileObject
     * @return an instance of <code>PersistenceScopes</code>; never null.
     * @since 1.37
     */
    default PersistenceScopes getPersistenceScopes(FileObject fo) {
        return getPersistenceScopes();
    }
}
