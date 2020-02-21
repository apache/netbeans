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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

class CustomBuildItemCustomizerNode extends CustomizerNode {

    public CustomBuildItemCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        SharedItemConfiguration[] configurations = getContext().getItems();
        List<Sheet> out = new ArrayList<>();
        if (configurations != null) {
            for (SharedItemConfiguration cfg : configurations) {
                ItemConfiguration itemConfiguration = cfg.getItemConfiguration(configuration);
                if (itemConfiguration != null) {
                    out.add(CustomToolCustomizerNode.getSheet(itemConfiguration.getCustomToolConfiguration()));
                }
            }
        }
        return out.isEmpty() ? null : out.toArray(new Sheet[out.size()]);
    }
}
