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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
public class FileConfigurationNode extends DefaultMutableTreeNode {
    private final FileConfigurationImpl file;
    
    public FileConfigurationNode(FileConfigurationImpl file) {
        super(file);
        this.file = file;
    }
    
    @Override
    public String toString() {
        String name = file.getFileName();
        //if (name.lastIndexOf('/')>0) {
        //    name = name.substring(name.lastIndexOf('/')+1);
        //}
        return name;
    }
    
    public FileConfigurationImpl getFile() {
        return file;
    }
    
    public boolean isCheckedInclude() {
        return !file.overrideIncludes();
    }
    
    public void setCheckedInclude(boolean checkedInclude) {
        file.setOverrideIncludes(!checkedInclude);
    }
    
    public boolean isCheckedMacro() {
        return !file.overrideMacros();
    }
    
    public void setCheckedMacro(boolean checkedMacro) {
        file.setOverrideMacros(!checkedMacro);
    }
}
