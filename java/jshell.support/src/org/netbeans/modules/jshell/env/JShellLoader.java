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
package org.netbeans.modules.jshell.env;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "JShellConsole=Java Shell Console"
})
@DataObject.Registration(displayName = "#JShellConsole", mimeType = "text/x-repl")
public final class JShellLoader extends UniFileLoader {                    
    public JShellLoader() {
        super("org.netbeans.modules.jshell.env.JShellDataObject");
        ExtensionList extensions = new ExtensionList();
        extensions.addMimeType("text/x-repl");
        setExtensions(extensions);
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        // console files reside exactly before the work root
        FileObject p = fo.getParent();
        if (p != null && p.getAttribute("jshell.scratch") == Boolean.TRUE) {
            return super.findPrimaryFile(fo);
        }
        return null;
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new JShellDataObject(primaryFile, this);
    }
}
