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

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Prevents refactoring from creating DataObjects for .svn folders and
 * therefore touching (copying/moving) them.
 *
 * @author Tomas Stupka
 */
public class SvnMetadataFolderLoader extends DataLoader {

    public SvnMetadataFolderLoader() {
        super(DataFolder.class.getName());
    }

    @Override
    protected DataObject handleFindDataObject(FileObject fo, RecognizedFiles recognized) throws IOException {
        if(!fo.isFolder() || !fo.getName().endsWith("svn")) {                   // NOI18N
            return null;
        }

        File f = FileUtil.toFile(fo);
        if(f == null) {
            return null;
        }
        
        if(SvnUtils.isPartOfSubversionMetadata(f)) {
            IOException e = new IOException("Do not create DO for .svn metadata: " + f); //NOI18N
            Exceptions.attachSeverity(e, Level.FINE);
            throw e;
        }
        return null;
    }
}
