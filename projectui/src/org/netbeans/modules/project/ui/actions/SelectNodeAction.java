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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.project.ui.ProjectTab;
import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

public class SelectNodeAction extends LookupSensitiveAction implements ContextAwareAction {
    
    @StaticResource private static final String SELECT_IN_PROJECTS_ICON = "org/netbeans/modules/project/ui/resources/projectTab.png";
    @StaticResource private static final String SELECT_IN_FILES_ICON = "org/netbeans/modules/project/ui/resources/filesTab.png";
    
    private final String findIn;
    
    @ActionID(id = "org.netbeans.modules.project.ui.SelectInProjects", category = "Window/SelectDocumentNode")
    @ActionRegistration(displayName = "#LBL_SelectInProjectsAction_MainMenuName", lazy=false)
    @ActionReferences({
        @ActionReference(path = "Shortcuts", name = "DS-1"),
        @ActionReference(path = "Menu/GoTo", position = 2600, separatorBefore = 2500),
        @ActionReference(path = "Editors/TabActions", position = 100)
    })
    @Messages("LBL_SelectInProjectsAction_MainMenuName=Select in Pro&jects")
    public static SelectNodeAction inProjects() {
        return new SelectNodeAction(SELECT_IN_PROJECTS_ICON, LBL_SelectInProjectsAction_MainMenuName(), ProjectTab.ID_LOGICAL, null);
    }
    
    @ActionID(id = "org.netbeans.modules.project.ui.SelectInFiles", category = "Window/SelectDocumentNode")
    @ActionRegistration(displayName = "#LBL_SelectInFilesAction_MainMenuName", lazy=false)
    @ActionReferences({
        @ActionReference(path = "Shortcuts", name = "DS-2"),
        @ActionReference(path = "Menu/GoTo", position = 2700)
    })
    @Messages("LBL_SelectInFilesAction_MainMenuName=Sele&ct in Files")
    public static SelectNodeAction inFiles() {
        return new SelectNodeAction(SELECT_IN_FILES_ICON, LBL_SelectInFilesAction_MainMenuName(), ProjectTab.ID_PHYSICAL, null);
    }
    
    private SelectNodeAction(String icon, String name, String findIn, Lookup lookup) {
        super(null, lookup, new Class<?>[] {DataObject.class, FileObject.class});
        putValue("iconBase", icon);
        this.findIn = findIn;
        this.setDisplayName( name );
    }
       
    protected @Override void actionPerformed( Lookup context ) {
        FileObject fo = getFileFromLookup( context );
        if ( fo != null ) {
            ProjectTab pt  = ProjectTab.findDefault( findIn );      
            pt.selectNodeAsync( fo );
        }
    }
    
    protected @Override void refresh(Lookup context, boolean immediate) {
        FileObject fo = getFileFromLookup( context );
        setEnabled( fo != null );        
    }
    
    private FileObject getFileFromLookup( Lookup context ) {
   
        FileObject fo = context.lookup(FileObject.class);     
        if (fo != null) {
            return fo;
        }

        DataObject dobj = context.lookup(DataObject.class);
        
        return dobj == null ? null : dobj.getPrimaryFile();
    }

    @Override public Action createContextAwareInstance(Lookup context) {
        return new SelectNodeAction((String) getValue("iconBase"), (String) getValue(NAME), findIn, context);
    }
    
}
