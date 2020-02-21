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
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

class GeneralFolderCustomizerNode extends CustomizerNode {

    public GeneralFolderCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        // IG: folder -> folders
        Folder[] folders = getContext().getFolders();
        List<Sheet> out = new ArrayList<>();
        if (folders != null) {
            for (Folder folder : folders) {
                Sheet generalSheet = FolderNodeFactory.getGeneralSheet(folder.getFolderConfiguration(configuration));
                out.add(generalSheet);
            }
        }
        return out.isEmpty() ? null : out.toArray(new Sheet[out.size()]);
    }
}
