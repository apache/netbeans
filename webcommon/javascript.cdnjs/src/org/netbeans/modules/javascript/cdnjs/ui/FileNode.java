/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript.cdnjs.ui;

import java.util.Collection;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Node that represents one library file.
 *
 * @author Jan Stola
 */
public class FileNode extends AbstractNode {
    /** "Install" property of the node. */
    private final InstallProperty installProperty = new InstallProperty();

    /**
     * Creates a new {@code FileNode}.
     * 
     * @param fileName name of a file.
     * @param install default value of the "install" property.
     */
    public FileNode(String fileName, boolean install) {
        super(Children.LEAF);
        setName(fileName);
        installProperty.setValue(install);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.put(installProperty);
        sheet.put(set);
        return sheet;
    }

    /**
     * Collects the names of the files the user is not interested in.
     * 
     * @param refusedFiles collection that should be populated by the refused files.
     */
    void collectRefusedFiles(Collection<String> refusedFiles) {
        if (!installProperty.getValue()) {
            refusedFiles.add(getName());
        }
    }

    /**
     * Property that determines whether the file should be installed or not.
     */
    static final class InstallProperty extends PropertySupport.ReadWrite<Boolean> {
        /** Name of the property. */
        static final String NAME = "instal"; // NOI18N
        /** Value of the property. */
        private boolean value;

        /**
         * Creates a new {@code InstallProperty}.
         */
        @NbBundle.Messages({"FileNode.installProperty.displayName=Install"})
        InstallProperty() {
            super(NAME, Boolean.class, Bundle.FileNode_installProperty_displayName(), null);
        }

        @Override
        public Boolean getValue() {
            return value;
        }

        @Override
        public void setValue(Boolean value) {
            this.value = value;
        }
        
    }
    
}
