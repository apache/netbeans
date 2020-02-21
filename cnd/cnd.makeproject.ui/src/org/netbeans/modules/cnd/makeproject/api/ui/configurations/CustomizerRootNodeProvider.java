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
package org.netbeans.modules.cnd.makeproject.api.ui.configurations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;

public final class CustomizerRootNodeProvider {

    private static CustomizerRootNodeProvider instance = null;

    public static CustomizerRootNodeProvider getInstance() {
        if (instance == null) {
            instance = new CustomizerRootNodeProvider();
        }
        return instance;
    }

    public List<CustomizerNode> getCustomizerNodes(Lookup lookup) {
        ArrayList<CustomizerNode> list = new ArrayList<>();

        // Add nodes from providers register via services
        for (CustomizerNodeProvider provider : getCustomizerNodeProviders()) {
            CustomizerNode node = provider.factoryCreate(lookup);
            if (node != null) {
                list.add(node);
            }
        }
        return list;
    }

    public List<CustomizerNode> getCustomizerNodes(String id, Lookup lookup) {
        ArrayList<CustomizerNode> list = new ArrayList<>();
        List<CustomizerNode> nodes = getCustomizerNodes(lookup);
        for (CustomizerNode n : nodes) {
            if (n != null && n.getName().equals(id)) {
                list.add(n);
            }
        }
        return list;
    }

    /*
     * Get list (dynamic) registered via services
     */
    private static Set<CustomizerNodeProvider> getCustomizerNodeProviders() {
        HashSet<CustomizerNodeProvider> providers = new HashSet<>();
        Lookup.Template<CustomizerNodeProvider> template = new Lookup.Template<>(CustomizerNodeProvider.class);
        Lookup.Result<CustomizerNodeProvider> result = Lookup.getDefault().lookup(template);
        Iterator<? extends CustomizerNodeProvider> iterator = result.allInstances().iterator();
        while (iterator.hasNext()) {
            CustomizerNodeProvider caop = iterator.next();
            providers.add(caop);
        }
        return providers;
    }
}
