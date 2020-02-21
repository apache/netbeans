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

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;

/**
 *
 */
/*package*/final class SharedItemConfiguration {
    private final Map<Configuration, ItemConfiguration> itemConfigurations = new HashMap<>();
    private final Item item;

    public SharedItemConfiguration(Item item){
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public ItemConfiguration getItemConfiguration(Configuration configuration) {
        ItemConfiguration res = itemConfigurations.get(configuration);
        if (res == null) {
            res = ProxyItemConfiguration.proxyFactory(configuration, item);
            if (res != null) {
                itemConfigurations.put(configuration, res);
                /*ItemConfiguration old = (ItemConfiguration)*/ configuration.removeAuxObject(res);
                configuration.addAuxObject(res);
            }
        }
        return res;
    }
}
