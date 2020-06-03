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
package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.spi.model.services.CsmVisibilityQueryProvider;
import org.openide.util.Lookup;

/**
 */
public class CsmVisibilityQuery {

    private static Collection<? extends CsmVisibilityQueryProvider> providers = null;

    /**
     * Constructor.
     */
    private CsmVisibilityQuery() {
    }

    /** Static method to obtain the provider.
     * @return the provider
     */
    public static synchronized Collection<? extends CsmVisibilityQueryProvider> getProviders() {
        if (providers != null) {
            return providers;
        }
        providers = Lookup.getDefault().lookupAll(CsmVisibilityQueryProvider.class);
        return providers;
    }

    /**
     * Checks visibility.
     *
     * @param obj - code model object
     * @return visibility
     */
    public static boolean isVisible(CsmObject obj) {
        boolean visible = true;
        for (CsmVisibilityQueryProvider provider : getProviders()) {
            visible &= provider.isVisible(obj);
        }
        return visible;
    }
}
