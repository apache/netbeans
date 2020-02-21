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
package org.netbeans.modules.cnd.spi;

import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Some CND components, like <code>cnd.editor</code> or
 * <code>cnd.api.project</code> system bridge, want specific cookies in the
 * C/C++ data objects. Such components should implement their own
 * {@link CndCookieProvider}. <code>cnd.source</code> should not depend
 * on other CND modules.
 *
 */
public abstract class CndCookieProvider {
    public interface InstanceContentOwner {
        InstanceContent getInstanceContent();
    }

    public abstract void addLookup(InstanceContentOwner icOwner);

    private static CndCookieProvider DEFAULT;

    public static synchronized CndCookieProvider getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Default();
        }
        return DEFAULT;
    }

    private static final class Default extends CndCookieProvider {

        private final Collection<? extends CndCookieProvider> providers;

        public Default() {
            providers = Lookup.getDefault().lookupAll(CndCookieProvider.class);
        }

        @Override
        public void addLookup(InstanceContentOwner icOwner) {
            for (CndCookieProvider provider : providers) {
                provider.addLookup(icOwner);
            }
        }
    }
}
