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
package org.netbeans.modules.svg;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.xml.XMLDataObjectLook;
import org.netbeans.modules.xml.cookies.DataObjectCookieManager;
import org.netbeans.modules.xml.cookies.UpdateDocumentCookie;
import org.netbeans.modules.xml.sync.DataObjectSyncSupport;
import org.netbeans.modules.xml.sync.Synchronizator;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

/**
 *
 * @author Christian Lenz
 */
@NbBundle.Messages({
    "LBL_SVG_LOADER=Files of SVG"
})
@MIMEResolver.ExtensionRegistration(
    displayName = "#LBL_SVG_LOADER",
    mimeType = SVGDataObject.MIME_TYPE,
    extension = {"svg", "SVG"},
    position = 21367
)
@DataObject.Registration(
    mimeType = SVGDataObject.MIME_TYPE,
    iconBase = "org/netbeans/modules/svg/resources/svgLogo.png",
    displayName = "#LBL_SVG_LOADER",
    position = 300
)
@ActionReferences({
    @ActionReference(
        id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 100
    ),
    @ActionReference(
        id = @ActionID(category = "System", id = "org.openide.actions.ViewAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 200,
        separatorAfter = 400
    ),
    @ActionReference(
        id = @ActionID(category = "XML", id = "org.netbeans.modules.xml.tools.actions.CheckAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 600
    ),
    @ActionReference(
        id = @ActionID(category = "XML", id = "org.netbeans.modules.xml.tools.actions.ValidateAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 700,
        separatorAfter = 750
    ),
    @ActionReference(
        id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 800
    ),
    @ActionReference(
        id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 900
    ),
    @ActionReference(
        id = @ActionID(category = "Edit", id = "org.openide.actions.PasteAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 1000,
        separatorAfter = 1100
    ),
    @ActionReference(
        id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 1200
    ),
    @ActionReference(
        id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 1300,
        separatorAfter = 1400
    ),
    @ActionReference(
        id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 1500
    ),
    @ActionReference(
        id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 1550,
        separatorAfter = 1600
    ),
    @ActionReference(
        id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 1700
    ),
    @ActionReference(
        id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
        path = "Loaders/image/svg+xml/Actions",
        position = 1800
    )
})
public final class SVGDataObject extends MultiDataObject implements XMLDataObjectLook, UpdateDocumentCookie {

    public static final String MIME_TYPE = "image/svg+xml";
    private final transient DataObjectCookieManager cookieManager;
    private transient Synchronizator synchronizator;

    public SVGDataObject(final FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        registerEditor(MIME_TYPE, true);

        CookieSet set = getCookieSet();
        cookieManager = new DataObjectCookieManager(this, getCookieSet());

        // add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(is, CheckXMLSupport.DOCUMENT_MODE));
        set.add(new ValidateXMLSupport(is));

        CookieSet.Factory viewCookieFactory = new ViewCookieFactory();
        set.add(ViewCookie.class, viewCookieFactory);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public DataObjectCookieManager getCookieManager() {
        return cookieManager;
    }

    @Override
    public synchronized Synchronizator getSyncInterface() {
        if (synchronizator == null) {
            synchronizator = new DataObjectSyncSupport(SVGDataObject.this);
        }

        return synchronizator;
    }

    @MultiViewElement.Registration(
        displayName = "org.netbeans.modules.svg.Bundle#CTL_SourceTabCaption",
        iconBase = "org/netbeans/modules/svg/resources/svgLogo.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "SVG",
        mimeType = MIME_TYPE,
        position = 1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    public void updateDocumentRoot() {
        setModified(false);
        getPrimaryFile();
    }

    private class ViewCookieFactory implements CookieSet.Factory {

        @Override
        public Node.Cookie createCookie(Class klass) {
            if (klass == ViewCookie.class) {
                return new ViewSupport(org.netbeans.modules.svg.SVGDataObject.this.getPrimaryEntry());
            } else {
                return null;
            }
        }
    }

    private static final class ViewSupport implements ViewCookie {

        private final MultiDataObject.Entry primary;

        public ViewSupport(MultiDataObject.Entry primary) {
            this.primary = primary;
        }

        @Override
        public void view() {
            HtmlBrowser.URLDisplayer.getDefault().showURL(primary.getFile().toURL());
        }

    }
}
