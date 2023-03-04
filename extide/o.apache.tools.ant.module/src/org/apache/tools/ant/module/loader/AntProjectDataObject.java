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

package org.apache.tools.ant.module.loader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.apache.tools.ant.module.api.AntProjectCookie;
import static org.apache.tools.ant.module.loader.AntProjectDataObject.*;
import org.apache.tools.ant.module.nodes.AntProjectNode;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObject.Registration;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@MIMEResolver.Registration(
        displayName="#AntResolver",
        position=310,
        resource="../resources/ant-mime-resolver.xml"
)
@Registration(displayName="#AntProjectDataObject", iconBase="org/apache/tools/ant/module/resources/AntIcon.gif", mimeType=MIME_TYPE)
@ActionReferences({
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.OpenAction"), path=ACTIONS, position=100),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.CutAction"), path=ACTIONS, position=600, separatorBefore=500),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.CopyAction"), path=ACTIONS, position=700, separatorAfter=800),
    @ActionReference(id=@ActionID(category="Edit", id="org.openide.actions.DeleteAction"), path=ACTIONS, position=900),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.RenameAction"), path=ACTIONS, position=1000, separatorAfter=1100),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.SaveAsTemplateAction"), path=ACTIONS, position=1200),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.FileSystemAction"), path=ACTIONS, position=1250, separatorAfter=1300),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.ToolsAction"), path=ACTIONS, position=1400),
    @ActionReference(id=@ActionID(category="System", id="org.openide.actions.PropertiesAction"), path=ACTIONS, position=1500)
})
@Messages({
    "AntProjectDataObject=Ant Scripts",
    "AntResolver=Ant <project> XML files"
})
public class AntProjectDataObject extends MultiDataObject implements PropertyChangeListener {

    public static final String MIME_TYPE = "text/x-ant+xml"; // NOI18N
    public static final String ACTIONS = "Loaders/text/x-ant+xml/Actions";

    public AntProjectDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add (new AntProjectDataEditor (this));
        FileObject prim = getPrimaryFile ();
        AntProjectCookie proj = new AntProjectSupport (prim);
        cookies.add (proj);
        if (proj.getFile () != null) {
            cookies.add (new AntActionInstance (proj));
        }
        cookies.add(new CheckXMLSupport(DataObjectAdapters.inputSource(this)));
        addPropertyChangeListener (this);
    }
    
    @Override protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase="org/apache/tools/ant/module/resources/AntIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="ant",
        mimeType=MIME_TYPE,
        position=1
    )
    @Messages("CTL_SourceTabCaption=&Source")
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected Node createNodeDelegate () {
        return new AntProjectNode (this);
    }

    void addSaveCookie (final SaveCookie save) {
        if (getCookie (SaveCookie.class) == null) {
            getCookieSet ().add (save);
            setModified (true);
        }
    }

    void removeSaveCookie (final SaveCookie save) {
        if (getCookie (SaveCookie.class) == save) {
            getCookieSet ().remove (save);
            setModified (false);
        }
    }

    public void propertyChange (PropertyChangeEvent ev) {
        String prop = ev.getPropertyName ();
        if (prop == null || prop.equals (DataObject.PROP_PRIMARY_FILE)) { // #11979
            // XXX this might be better handled by overriding FileEntry.rename/move:
            getCookie(AntProjectSupport.class).setFileObject(getPrimaryFile());
        }
    }

}
