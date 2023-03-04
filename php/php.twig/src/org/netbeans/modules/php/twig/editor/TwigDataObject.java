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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor;

import java.io.IOException;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import static org.netbeans.modules.php.twig.editor.TwigDataObject.ACTIONS;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject.Registration;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages("TwigResolver=Twig Files")
@MIMEResolver.ExtensionRegistration(displayName = "#TwigResolver", position = 125, extension = "twig", mimeType = TwigLanguage.TWIG_MIME_TYPE)
@Registration(displayName = "TWIG", iconBase = TwigDataObject.ICON_LOCATION, mimeType = TwigLanguage.TWIG_MIME_TYPE)
@ActionReferences(value = {
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.OpenAction"), path = ACTIONS, position = 100, separatorAfter = 200),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), path = ACTIONS, position = 300),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), path = ACTIONS, position = 400, separatorAfter = 500),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), path = ACTIONS, position = 600),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.RenameAction"), path = ACTIONS, position = 700, separatorAfter = 800),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"), path = ACTIONS, position = 900, separatorAfter = 1000),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"), path = ACTIONS, position = 1100, separatorAfter = 1200),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.ToolsAction"), path = ACTIONS, position = 1300),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), path = ACTIONS, position = 1400)})
public class TwigDataObject extends MultiDataObject {

    static final String ACTIONS = "Loaders/" + TwigLanguage.TWIG_MIME_TYPE + "/Actions"; // NOI18N
    @StaticResource
    static final String ICON_LOCATION = "org/netbeans/modules/php/twig/resources/twig-icon.png"; // NOI18N

    public TwigDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
        super(pf, loader);
        registerEditor(TwigLanguage.TWIG_MIME_TYPE, true);
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @Messages("Source=&Source")
    @MultiViewElement.Registration(
            displayName = "#Source",
    iconBase = ICON_LOCATION,
    persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
    mimeType = TwigLanguage.TWIG_MIME_TYPE,
    preferredID = "php.twig.source",
    position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
}
