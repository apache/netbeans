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

package org.netbeans.modules.languages.dataobject;

import org.netbeans.modules.languages.LanguagesManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;

import java.io.IOException;
import org.openide.loaders.FileEntry;


public class LanguagesDataLoader extends MultiFileLoader {

    private static final long serialVersionUID = 1L;

    public LanguagesDataLoader() {
        super("org.netbeans.modules.languages.dataobject.LanguagesDataObject");
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(LanguagesDataLoader.class, "LBL_mf_loader_name");
    }

    protected String actionsContext() {
        return "Loaders/Languages/Actions";
    }

    protected FileObject findPrimaryFile (FileObject fo) {
        String mimeType = fo.getMIMEType ();
        if (LanguagesManager.getDefault ().createDataObjectFor (mimeType))
            return fo;
        return null;
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) 
    throws DataObjectExistsException, IOException {
        String mimeType = primaryFile.getMIMEType ();
        if (LanguagesManager.getDefault ().createDataObjectFor (mimeType))
            return new LanguagesDataObject (primaryFile, this);
        return null;
    }

    protected Entry createPrimaryEntry (
        MultiDataObject obj,
        FileObject primaryFile
    ) {
        return new FileEntry (obj, primaryFile);
    }

    protected Entry createSecondaryEntry (
        MultiDataObject obj,
        FileObject secondaryFile
    ) {
        return new FileEntry (obj, secondaryFile);
    }
    
    public int getNBSFiles () {
        return 1;
    }
}
