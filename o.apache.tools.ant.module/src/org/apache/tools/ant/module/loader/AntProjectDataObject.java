/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
