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
package org.netbeans.modules.javascript.nodejs.file;

import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject.Registration;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

@Messages({
    "LBL_NpmDebugLog_LOADER=npm-debug.log"
})
@MIMEResolver.Registration(displayName = "npm-debug.log", resource = "../resources/npm-resolver.xml", position = 144)
@Registration(displayName = "#LBL_NpmDebugLog_LOADER", iconBase = "org/netbeans/modules/javascript/nodejs/ui/resources/npm.png", mimeType = "text/npm-log")
@ActionReferences({
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
        position = 100,
        separatorAfter = 200
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
        position = 300
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
        position = 400,
        separatorAfter = 500
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
        position = 600
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
        position = 700,
        separatorAfter = 800
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
        position = 900,
        separatorAfter = 1000
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
        position = 1100,
        separatorAfter = 1200
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
        position = 1300
    ),
    @ActionReference(
        path = "Loaders/text/npm-log/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
        position = 1400
    )
})
public class NpmDebugLogDataObject extends MultiDataObject {
    public NpmDebugLogDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/npm-log", true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
}