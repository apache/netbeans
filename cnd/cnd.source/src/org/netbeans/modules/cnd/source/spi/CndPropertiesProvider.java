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
package org.netbeans.modules.cnd.source.spi;

import java.util.Collection;
import org.openide.loaders.DataNode;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CndPropertiesProvider {

    public abstract void addExtraProperties(DataNode node, Sheet sheet);

    private static CndPropertiesProvider DEFAULT;

    public static synchronized CndPropertiesProvider getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new CndPropertiesProvider.Default();
        }
        return DEFAULT;
    }

    private static final class Default extends CndPropertiesProvider {

        private final Collection<? extends CndPropertiesProvider> providers;

        public Default() {
            providers = Lookup.getDefault().lookupAll(CndPropertiesProvider.class);
        }

        @Override
        public void addExtraProperties(DataNode node, Sheet sheet) {
            for (CndPropertiesProvider provider : providers) {
                provider.addExtraProperties(node, sheet);
            }
        }
    }
}

