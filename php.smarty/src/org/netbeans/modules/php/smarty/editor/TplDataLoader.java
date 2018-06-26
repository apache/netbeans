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

package org.netbeans.modules.php.smarty.editor;

import java.io.IOException;
import static org.netbeans.modules.php.smarty.editor.TplDataLoader.ACTIONS;
import static org.netbeans.modules.php.smarty.editor.TplDataLoader.MIME_TYPE;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject.Registration;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
/**
 * Loader for Tpl DataObjects.
 *
 * @author Martin Fousek
 */
@Registration(position = 10998, displayName = "TPL", mimeType = MIME_TYPE)
@ActionReferences({
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"), path = ACTIONS, position = 100),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), path = ACTIONS, position = 300, separatorBefore = 200),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), path = ACTIONS, position = 400),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.PasteAction"), path = ACTIONS, position = 500, separatorAfter = 600),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.NewAction"), path = ACTIONS, position = 700),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), path = ACTIONS, position = 800),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), path = ACTIONS, position = 900, separatorAfter = 1000),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"), path = ACTIONS, position = 1100, separatorAfter = 1200),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"), path = ACTIONS, position = 1300, separatorAfter = 1400),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"), path = ACTIONS, position = 1500),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), path = ACTIONS, position = 1600)
})
public class TplDataLoader extends UniFileLoader {

    private static final long serialVersionUID = -5805535261731217882L;

    public static final String MIME_TYPE = "text/x-tpl";
    public static final String ACTIONS = "Loaders/"+MIME_TYPE+"/Actions";

    public TplDataLoader() {
        super("org.netbeans.modules.php.smarty.editor.TplDataObject"); // NOI18N
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE); // NOI18N
    }

    @Override
    protected MultiDataObject createMultiObject(final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new TplDataObject(primaryFile, this);
    }

    /** Get the default display name of this loader.
     * @return default display name
     */
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(TplDataLoader.class, "PROP_TplLoader_Name");
    }

    @Override
    protected String actionsContext() {
        return ACTIONS; // NOI18N
    }

}
