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
package org.netbeans.modules.cpplite.editor.file;

import java.io.IOException;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.textmate.lexer.api.GrammarRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages({
    "LBL_HPP_LOADER=Files of HPP"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_HPP_LOADER",
        mimeType = MIMETypes.HPP,
        extension = {"hpp", "hxx", "hh", "h++"},
        position = 1000300
)
@DataObject.Registration(
        mimeType = MIMETypes.HPP,
        iconBase = HPPDataObject.ICON,
        displayName = "#LBL_HPP_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/" + MIMETypes.HPP + "/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    ),
    @ActionReference(
            path = "Editors/" + MIMETypes.HPP + "/Popup",
            id = @ActionID(category = "Refactoring", id = "org.netbeans.modules.refactoring.api.ui.WhereUsedAction"),
            position = 1400
    ),
    @ActionReference(
            path = "Editors/" + MIMETypes.HPP + "/Popup",
            id = @ActionID(category = "Refactoring", id = "org.netbeans.modules.refactoring.api.ui.RenameAction"),
            position = 1500,
            separatorAfter = 1550
    )
})
@GrammarRegistration(grammar="resources/cpp.tmLanguage.json", mimeType=MIMETypes.HPP)
public class HPPDataObject extends MultiDataObject {

    @StaticResource
    static final String ICON = "org/netbeans/modules/cpplite/editor/file/resources/HDataIcon.gif";

    public HPPDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(MIMETypes.HPP, true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
        displayName = "#Source",
        iconBase = HPPDataObject.ICON,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        mimeType = MIMETypes.HPP,
        preferredID = "hpp.source",
        position = 100
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
}
