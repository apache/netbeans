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

package org.netbeans.modules.editor.impl;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vita Stejskal
 * @since 1.39
 */
@MimeLocation(subfolderName=EditorActionsProvider.EDITOR_ACTIONS_FOLDER_NAME, instanceProviderClass=EditorActionsProvider.class)
public final class EditorActionsProvider extends ActionsList implements InstanceProvider<EditorActionsProvider> {

    static final String EDITOR_ACTIONS_FOLDER_NAME = "Actions"; //NOI18N
    
    public static List<Action> getEditorActions(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        EditorActionsProvider provider = MimeLookup.getLookup(mimePath).lookup(EditorActionsProvider.class);
        return provider == null ? Collections.<Action>emptyList() : provider.getActionsOnly();
    }
    
    public static List<Object> getItems(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        EditorActionsProvider provider = MimeLookup.getLookup(mimePath).lookup(EditorActionsProvider.class);
        return provider == null ? Collections.<Object>emptyList() : provider.getAllInstances();
    }
    
    public EditorActionsProvider() {
        this(null);
    }

    private EditorActionsProvider(List<FileObject> keys) {
        super(keys, false, true); // prohibit separators and action-names
    }
    
    public EditorActionsProvider createInstance(List<FileObject> fileObjectList) {
        return new EditorActionsProvider(fileObjectList);
    }
}
