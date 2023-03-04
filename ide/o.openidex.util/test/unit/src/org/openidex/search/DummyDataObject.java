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

package org.openidex.search;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Marian Petras
 */
public class DummyDataObject extends MultiDataObject {
    
    public DummyDataObject(FileObject fo, MultiFileLoader loader)
                                            throws DataObjectExistsException {
        super(fo, loader);
    }

    public boolean isDeleteAllowed() {
        return false;
    }

    public boolean isCopyAllowed() {
        return false;
    }

    public boolean isMoveAllowed() {
        return false;
    }

    public boolean isRenameAllowed() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected DataObject handleCopy(DataFolder f) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void handleDelete() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected FileObject handleRename(String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected FileObject handleMove(DataFolder df) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
