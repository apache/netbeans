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

import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistenceapi.EntityClassScopeAccessor;

/**
 * This class is used to create {@link EntityClassScope} instances, which
 * cannot be created directly.
 *
 * @author Andrei Badea
 * @since 1.3
 */
public final class EntityClassScopeFactory {

    private EntityClassScopeFactory() {
    }

    /**
     * Creates an EntityClassScope from the given EntityClassScopeImplementation.
     */
    public static EntityClassScope createEntityClassScope(EntityClassScopeImplementation impl) {
        if(EntityClassScopeAccessor.DEFAULT == null) {//initialize accessor
            Class c = EntityClassScope.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (Exception ex) {
                // XXX should probably use ErrorManager, but this could
                // be called very early during the startup, when EM is not initialized yet?
                ex.printStackTrace();
            }
        }
        return EntityClassScopeAccessor.DEFAULT.createEntityClassScope(impl);
    }
}
