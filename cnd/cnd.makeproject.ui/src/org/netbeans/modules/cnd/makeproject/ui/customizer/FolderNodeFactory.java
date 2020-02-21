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
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class FolderNodeFactory {

    private FolderNodeFactory() {
    }

    public static Node createRootNodeFolder(Lookup lookup) {
        ArrayList<CustomizerNode> descriptions = new ArrayList<>(); //new CustomizerNode[2];
        descriptions.add(createGeneralFolderDescription(lookup));
        descriptions.add(ItemNodeFactory.createCCompilerDescription(lookup));
        descriptions.add(ItemNodeFactory.createCCCompilerDescription(lookup));

        Folder[] folders = lookup.lookup(Folder[].class); 
        boolean linker = true;
        for (Folder folder : folders) {
            if (folder == null || (!folder.isTest() && !folder.isTestLogicalFolder() && !folder.isTestRootFolder())) {
                linker = false;
            }
        }
        if (linker) {
            descriptions.add(createLinkerDescription(lookup));
        }

        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), descriptions.toArray(new CustomizerNode[descriptions.size()]), lookup);  // NOI18N

        return new PropertyNode(rootDescription);
    }

    private static CustomizerNode createGeneralFolderDescription(Lookup lookup) {
        return new GeneralFolderCustomizerNode(
                "GeneralItem", getString("LBL_Config_General"), null, lookup); // NOI18N
    }

    // Linker
    private static CustomizerNode createLinkerDescription(Lookup lookup) {
        return new LinkerGeneralCustomizerNode("Linker", getString("LBL_LINKER_NODE"), null, lookup); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getBundle(MakeCustomizer.class).getString(s);
    }
    
    public static Sheet getGeneralSheet(FolderConfiguration fc) {
        Sheet sheet = new Sheet();

        Sheet.Set set = new Sheet.Set();
        set.setName("FolderConfiguration"); // NOI18N
        set.setDisplayName(getString("FolderConfigurationTxt"));
        set.setShortDescription(getString("FolderConfigurationHint"));
        set.put(new StringRONodeProp(getString("NameTxt"), fc.getFolder().getDisplayName()));
        sheet.put(set);

        return sheet;
    }

    private static class StringRONodeProp extends PropertySupport<String> {

        private final String value;

        public StringRONodeProp(String name, String value) {
            super(name, String.class, name, name, true, false);
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String v) {
        }
    }
}
