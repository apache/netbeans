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
package org.netbeans.modules.testng.ui.wizards;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject.Registration;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@Registration(displayName = "#Loaders/text/x-testng+xml/Factories/org-netbeans-modules-testng-TestNGSuiteDataLoader.instance", iconBase = "org/netbeans/modules/testng/ui/resources/testng.gif", mimeType = "text/x-testng+xml")
@ActionReferences(value = {
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.OpenAction"), path = "Loaders/text/x-testng+xml/Actions", position = 100, separatorAfter = 200),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), path = "Loaders/text/x-testng+xml/Actions", position = 300),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), path = "Loaders/text/x-testng+xml/Actions", position = 400, separatorAfter = 500),
    @ActionReference(id =
    @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), path = "Loaders/text/x-testng+xml/Actions", position = 600),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.RenameAction"), path = "Loaders/text/x-testng+xml/Actions", position = 700, separatorAfter = 800),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"), path = "Loaders/text/x-testng+xml/Actions", position = 900, separatorAfter = 1000),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"), path = "Loaders/text/x-testng+xml/Actions", position = 1100, separatorAfter = 1200),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.ToolsAction"), path = "Loaders/text/x-testng+xml/Actions", position = 1300),
    @ActionReference(id =
    @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), path = "Loaders/text/x-testng+xml/Actions", position = 1400)})
@MIMEResolver.Registration(
displayName="#suite.resolver",
position=153,
resource="../resources/testng-suite-resolver.xml")
@NbBundle.Messages("suite.resolver=TestNGSuite File")
public class TestNGSuiteDataObject extends MultiDataObject {

    public static final String MIME_TYPE = "text/x-testng+xml";

    public TestNGSuiteDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new CheckXMLSupport(DataObjectAdapters.inputSource(this)));
        cookies.add(new ValidateXMLSupport(DataObjectAdapters.inputSource(this)));
        registerEditor(MIME_TYPE, true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(displayName = "#CTL_SourceTabCaption",
        iconBase = "org/netbeans/modules/testng/ui/resources/testng.gif",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "testng",
        mimeType = MIME_TYPE,
        position = 1
    )
    @NbBundle.Messages("CTL_SourceTabCaption=&Source")
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
}
