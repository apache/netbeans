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

package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

public class PackagingNodeProp extends PropertySupport<PackagingConfiguration> {
    private PackagingConfiguration packagingConfiguration;
    private MakeConfiguration conf;
    
    public PackagingNodeProp(PackagingConfiguration packagingConfiguration, MakeConfiguration conf, String[] txts) {
        super(txts[0], PackagingConfiguration.class, txts[1], txts[2], true, true);
        this.packagingConfiguration = packagingConfiguration;
	this.conf = conf;
    }

//    public String getHtmlDisplayName() {
//        if (vectorConfiguration.getModified())
//            return "<b>" + getDisplayName(); // NOI18N
//        else
//            return null;
//    }
    
    @Override
    public PackagingConfiguration getValue() {
        return packagingConfiguration;
    }
    
    @Override
    public void setValue(PackagingConfiguration v) {
        if (v != null) {
            packagingConfiguration = v; // FIXUP
        }
    }
    
//    public void restoreDefaultValue() {
//        vectorConfiguration.reset();
//    }
    
    @Override
    public boolean supportsDefaultValue() {
        return false;
    }
    
//    public boolean isDefaultValue() {
//        return vectorConfiguration.getValue().size() == 0;
//    }

    @Override
    public PropertyEditor getPropertyEditor() {
	return new Editor(packagingConfiguration);
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canEditAsText")) // NOI18N
            return Boolean.FALSE;
        return super.getValue(attributeName);
    }

    
    
    private class Editor extends PropertyEditorSupport implements ExPropertyEditor {
        private final PackagingConfiguration packagingConfiguration;
        private PropertyEnv env;
        
        public Editor(PackagingConfiguration packagingConfiguration) {
            this.packagingConfiguration = packagingConfiguration;
        }
        
        @Override
        public void setAsText(String text) {
        }
        
        @Override
        public String getAsText() {
            int noFiles = packagingConfiguration.getFiles().getValue().size();
            String val;
            if (noFiles == 0) {
                val = getString("FilesTextZero");
            }
            else if (noFiles == 1) {
                val = getString("FilesTextOne", "" + noFiles, (packagingConfiguration.getFiles().getValue().get(0)).getTo()); // NOI18N
            }
            else {
                val = getString("FilesTextMany", "" + noFiles, (packagingConfiguration.getFiles().getValue().get(0)).getTo() + ", ..."); // NOI18N
            }
            return val;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return new PackagingPanel(packagingConfiguration, this, env, conf);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }

    private static String getString(String s) {
	return NbBundle.getBundle(PackagingNodeProp.class).getString(s);
    }
    
    private static String getString(String s, String a1, String a2) {
        return NbBundle.getMessage(PackagingNodeProp.class, s, a1, a2);
    }
}
