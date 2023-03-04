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
package org.netbeans.modules.php.latte;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages("LatteResolver=Latte Templates")
@MIMEResolver.ExtensionRegistration(displayName = "#LatteResolver", position = 1983, extension = "latte", mimeType = LatteLanguage.LATTE_MIME_TYPE)
@DataObject.Registration(displayName = "LATTE", iconBase = LatteDataObject.LATTE_ICON, mimeType = LatteLanguage.LATTE_MIME_TYPE, position = 1983)
@ActionReferences(value = {
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.OpenAction"), path = LatteDataObject.ACTIONS, position = 100, separatorAfter = 200),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), path = LatteDataObject.ACTIONS, position = 300),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), path = LatteDataObject.ACTIONS, position = 400, separatorAfter = 500),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), path = LatteDataObject.ACTIONS, position = 600),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.RenameAction"), path = LatteDataObject.ACTIONS, position = 700, separatorAfter = 800),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"), path = LatteDataObject.ACTIONS, position = 900, separatorAfter = 1000),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"), path = LatteDataObject.ACTIONS, position = 1100, separatorAfter = 1200),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.ToolsAction"), path = LatteDataObject.ACTIONS, position = 1300),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), path = LatteDataObject.ACTIONS, position = 1400)
})
public class LatteDataObject extends MultiDataObject {
    static final String ACTIONS = "Loaders/" + LatteLanguage.LATTE_MIME_TYPE + "/Actions"; //NOI18N
    static final String LATTE_ICON = "org/netbeans/modules/php/latte/resources/latte_icon.png"; //NOI18N

    public LatteDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
        super(pf, loader);
        registerEditor(LatteLanguage.LATTE_MIME_TYPE, true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @MultiViewElement.Registration(
        displayName = "#LBL_Latte_EDITOR",
        iconBase = LATTE_ICON,
        mimeType = LatteLanguage.LATTE_MIME_TYPE,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "Latte",
        position = 1000
    )
    @Messages("LBL_Latte_EDITOR=Source")
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup lookup) {
        return new MultiViewEditorElement(lookup);
    }
}
