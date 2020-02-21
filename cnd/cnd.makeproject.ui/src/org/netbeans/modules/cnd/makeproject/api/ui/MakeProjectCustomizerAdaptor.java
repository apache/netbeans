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
package org.netbeans.modules.cnd.makeproject.api.ui;

import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Sheet;

/**
 *
 */
public abstract class MakeProjectCustomizerAdaptor implements MakeProjectCustomizerEx {

    @Override
    public abstract String getCustomizerId();

    @Override
    public abstract String getIconPath();

    @Override
    public String getMakefileWriter() {
        return null;
    }

    @Override
    public Action[] getActions(Project project, List<Action> actions) {
        return null;
    }
    
    @Override
    public Object[] getLookup(FileObject getProjectDirectory, Object[] base) {
        return base;
    }

    @Override
    public Sheet getPropertySheet(Sheet sheet) {
        return sheet;
    }

    @Override
    public CustomizerNode getRootPropertyNode(CustomizerNode rootPopertyNode) {
        return rootPopertyNode;
    }

    @Override
    public boolean isArchiverConfiguration() {
        return false;
    }

    @Override
    public boolean isCompileConfiguration() {
        return true;
    }

    @Override
    public boolean isDynamicLibraryConfiguration() {
        return true;
    }

    @Override
    public boolean isLibraryConfiguration() {
        return true;
    }

    @Override
    public boolean isLinkerConfiguration() {
        return true;
    }

    @Override
    public boolean isStandardManagedConfiguration() {
        return false;
    }

    @Override
    public boolean isApplicationConfiguration() {
        return false;
    }
}
