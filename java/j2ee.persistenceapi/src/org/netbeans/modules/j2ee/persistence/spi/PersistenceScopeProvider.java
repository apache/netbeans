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

import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.openide.filesystems.FileObject;

/**
 * Provides the persistence scope for the given file.
 *
 * @author Andrei Badea
 */
public interface PersistenceScopeProvider {

    /**
     * Returns the persistence scope for the given file.
     *
     * @return a persistence scope or null if there was no persistence
     *         scope for the given file.
     */
    public PersistenceScope findPersistenceScope(FileObject fo);
}
