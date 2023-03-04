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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vita Stejskal
 */
@MimeLocation(subfolderName=ToolbarActionsProvider.TOOLBAR_ACTIONS_FOLDER_NAME, instanceProviderClass=ToolbarActionsProvider.class)
public final class ToolbarActionsProvider extends ActionsList implements InstanceProvider<ToolbarActionsProvider> {

    private static final Logger LOG = Logger.getLogger(ToolbarActionsProvider.class.getName());
    
    static final String TOOLBAR_ACTIONS_FOLDER_NAME = "Toolbars/Default"; //NOI18N
    private static final String TEXT_BASE_PATH = "Editors/text/base/"; //NOI18N
    
    public static List getToolbarItems(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        ActionsList provider;
        if (mimeType.equals("text/base")) { //NOI18N
            provider = MimeLookup.getLookup(mimePath).lookup(LegacyToolbarActionsProvider.class);
        } else {
            provider = MimeLookup.getLookup(mimePath).lookup(ToolbarActionsProvider.class);
        }
        return provider == null ? Collections.emptyList() : provider.getAllInstances();
    }
    
    public ToolbarActionsProvider() {
        super(null, false, false);
    }

    private ToolbarActionsProvider(List<FileObject> keys) {
        super(keys, true, false);
    }
    
    public ToolbarActionsProvider createInstance(List<FileObject> fileObjectList) {
        return new ToolbarActionsProvider(fileObjectList);
    }
    
    // XXX: This is here to help NbEditorToolbar to deal with legacy code
    // that registered toolbar actions in text/base. The artificial text/base
    // mime type is deprecated and should not be used anymore.
    @MimeLocation(subfolderName=TOOLBAR_ACTIONS_FOLDER_NAME, instanceProviderClass=LegacyToolbarActionsProvider.class)
    public static final class LegacyToolbarActionsProvider extends ActionsList implements InstanceProvider<LegacyToolbarActionsProvider> {

        public LegacyToolbarActionsProvider() {
            this(null);
        }

        private LegacyToolbarActionsProvider(List<FileObject> keys) {
            super(keys, false, false);
        }

        public LegacyToolbarActionsProvider createInstance(List<FileObject> fileObjectList) {
            ArrayList<FileObject> textBaseFilesList = new ArrayList<FileObject>();

            for(Object o : fileObjectList) {
                FileObject fileObject = null;

                if (o instanceof DataObject) {
                    fileObject = ((DataObject) o).getPrimaryFile();
                } else if (o instanceof FileObject) {
                    fileObject = (FileObject) o;
                } else {
                    continue;
                }

                String fullPath = fileObject.getPath();
                int idx = fullPath.lastIndexOf(TOOLBAR_ACTIONS_FOLDER_NAME);
                assert idx != -1 : "Expecting files with '" + TOOLBAR_ACTIONS_FOLDER_NAME + "' in the path: " + fullPath; //NOI18N

                String path = fullPath.substring(0, idx);
                if (TEXT_BASE_PATH.equals(path)) {
                    textBaseFilesList.add(fileObject);
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("The 'text/base' mime type is deprecated, please move your file to the root. Offending file: " + fullPath); //NOI18N
                    }
                }
            }

            return new LegacyToolbarActionsProvider(textBaseFilesList);
        }
    } // End of LegacyToolbarActionsProvider class
}
