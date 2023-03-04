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
package org.netbeans.modules.openide.loaders.data;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.HelpCtx;

/**
 * @see DataObjectFactoryProcessorTest
 * @author Eric Barboni <skygo@netbeans.org>
 */
@DataObject.Registration(mimeType =  "text/test1", displayName = "labeltest", position = 3565, iconBase = "org/openide/loaders/unknown.gif")
public class DoFPDataObject extends DataObject {

    public DoFPDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }

    @Override
    public boolean isDeleteAllowed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCopyAllowed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMoveAllowed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRenameAllowed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected DataObject handleCopy(DataFolder f) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void handleDelete() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected FileObject handleRename(String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
