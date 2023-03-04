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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu.Separator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectList.TemplateItem;
import org.netbeans.modules.project.ui.ProjectUtilities;
import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter.Popup;

/** Action for invoking the project sensitive NewFile Wizard
 */
@Messages({
    "LBL_NewFileAction_Name=&New File...",
    "LBL_NewFileAction_PopupName=New",
    "# {0} - Name of the template", "LBL_NewFileAction_Template_PopupName={0}..."
})
public class NewFile extends ProjectAction implements PropertyChangeListener, Popup {

    private static final RequestProcessor RP = new RequestProcessor(NewFile.class);
    private static final RequestProcessor INSTANTIATE_RP = new RequestProcessor(NewFile.class.getName() + ".INSTANTIATE_RP", 5);
    private static final Logger LOG = Logger.getLogger(NewFile.class.getName());

    private JMenu subMenu;

    public NewFile() {
        this( null );
    }

    @Messages("LBL_NewFileAction_Tooltip=New File...")
    public NewFile( Lookup context ) {
        super((String) null, LBL_NewFileAction_Name(), null, context);
        putValue("iconBase","org/netbeans/modules/project/ui/resources/newFile.png"); //NOI18N
        putValue(SHORT_DESCRIPTION, LBL_NewFileAction_Tooltip());
        OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        refresh(getLookup(), true);
    }

    @Override
    protected void refresh(Lookup context, boolean immediate) {
        // #59615: update synch if possible; only replan if not already in EQ.
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
//issue 192354 - getOpenProjects() is expensive at times, uses read mutex on projects.
//                setEnabled(OpenProjectList.getDefault().getOpenProjects().length > 0);
                setDisplayName(LBL_NewFileAction_Name());
            }
        });
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new JMenuItem(this);
        menu.setIcon(null);
        Mnemonics.setLocalizedText(menu, (String) getValue(Action.NAME));
        // XXX accelerator not displayed here for some reason...why???
        return menu;
    }

    //private NewFileWizard wizardIterator;

    @Override
    protected void actionPerformed( Lookup context ) {
        doPerform( context, null, true );
    }

    private void doPerform( Lookup context, final DataObject template, boolean includeTemplatesWithProjects ) {

        if ( context == null ) {
            context = getLookup();
        }
        
        final NewFileWizard wd = new NewFileWizard( preselectedProject( context, includeTemplatesWithProjects ), includeTemplatesWithProjects /* , null */ );
        
        DataFolder preselectedFolder = preselectedFolder( context );
        if ( preselectedFolder != null ) {
            wd.setTargetFolder( preselectedFolder );
        }
        

        INSTANTIATE_RP.post(new Runnable() {
            @Override public void run() {
        try {
            Set resultSet = template == null ? wd.instantiate () : wd.instantiate( template );

            if (resultSet == null || resultSet.isEmpty ()) {
                // no new object, no work
                return ;
            }

            Iterator it = resultSet.iterator ();

            while (it.hasNext ()) {
                Object obj = it.next ();
                DataObject newDO = null;
                if (obj instanceof DataObject) {
                    newDO = (DataObject) obj;
                } else if (obj instanceof FileObject) {
                    try {
                        newDO = DataObject.find ((FileObject) obj);
                    } catch (DataObjectNotFoundException x) {
                        // XXX
                        assert false : obj;
                    }
                } else {
                    assert false : obj;
                }
                if (newDO != null) {
                    ProjectUtilities.openAndSelectNewObject (newDO);
                }
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

        // Update the Templates LRU for given project
        //Project project = Templates.getProject( wd );
        FileObject foTemplate = Templates.getTemplate( wd );
        OpenProjectList.getDefault().updateTemplatesLRU( foTemplate );
            }
        });
    }

    // Context Aware action implementation -------------------------------------

    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new NewFile( actionContext );
    }

    // Presenter.Popup implementation ------------------------------------------

    public JMenuItem getSubmenuPopupPresenter() {
        if (subMenu == null) {
            subMenu = new JMenu(LBL_NewFileAction_PopupName());
        }
        return subMenu;
    }

    protected void fillSubMenu() {
        fillSubMenu(subMenu, getLookup());
    }

    // Private methods ---------------------------------------------------------

    @CheckForNull
    private Project preselectedProject( Lookup context, boolean inProject ) {
        Project preselectedProject = Arrays.stream(
                ActionsUtil.getProjectsFromLookup(context, null))
                .findFirst()
                .orElse(null);
        //Ignore artificial (ProjectConvertor) projects which do not provide RecommendedTemplates
        if (preselectedProject != null && !isImportant(preselectedProject)) {
            Project p = null;
            FileObject dir = preselectedProject.getProjectDirectory().getParent();
            while (dir != null) {
                p = FileOwnerQuery.getOwner(dir);
                if (p != null && isImportant(p)) {
                    break;
                }
                p = null;
                dir = dir.getParent();
            }
            if (p != null) {
                preselectedProject = p;
            }
        }

        //TODO candidate for removal
        if ( preselectedProject == null && inProject) {
            // No project context => use main project
            preselectedProject = OpenProjectList.getDefault().getMainProject();
            if (preselectedProject == null && OpenProjectList.getDefault().getOpenProjects().length > 0) {
                // No main project => use the first one
                preselectedProject = OpenProjectList.getDefault().getOpenProjects()[0];
            }
        }

        return preselectedProject;
    }

    private static boolean isImportant(@NonNull final Project p) {
        if (!ProjectConvertors.isConvertorProject(p)) {
            return true;
        }
        final RecommendedTemplates pt = p.getLookup().lookup(RecommendedTemplates.class);
        if (pt == null) {
            return false;
        }
        final Project rOwner = ProjectConvertors.getNonConvertorOwner(p.getProjectDirectory());
        final RecommendedTemplates rot = rOwner == null ?
                null :
                p.getLookup().lookup(RecommendedTemplates.class);
        return pt != rot;
    }

    private DataFolder preselectedFolder( Lookup context ) {

        DataFolder preselectedFolder = null;

        // Try to find selected folder
        preselectedFolder = context.lookup(DataFolder.class);
        if ( preselectedFolder == null ) {
            // No folder selectd try with DataObject
            DataObject dobj = context.lookup(DataObject.class);
            if ( dobj != null) {
                // DataObject found => we'll use the parent folder
                preselectedFolder = dobj.getFolder();
            } else {
                // fallback
                FileObject fo = context.lookup(FileObject.class);
                if(fo != null) {
                    if(fo.isFolder()) {
                        preselectedFolder = DataFolder.findFolder(fo);
                    } else if(fo.getParent() != null) {                        
                        preselectedFolder = DataFolder.findFolder(fo.getParent());                    
                    }
                }
            }
        }

        LOG.log(Level.FINE, "#210919: found preselected folder {0} for {1}", new Object[] {preselectedFolder, context});
        return preselectedFolder;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refresh(Lookup.EMPTY, false);
    }

    private static final String TEMPLATE_PROPERTY = "org.netbeans.modules.project.ui.actions.NewFile.Template"; // NOI18N
    private static final String IN_PROJECT_PROPERTY = "org.netbeans.modules.project.ui.actions.NewFile.InProject"; // NOI18N


    @Messages({
        "LBL_NewFileAction_File_PopupName=Other...",
        "NewFile.please_wait=Please wait..."
    })
    private void fillSubMenu(final JMenu menuItem, final Lookup lookup) {
        menuItem.removeAll();
        JMenuItem wait = new JMenuItem(NewFile_please_wait());
        wait.setEnabled(false);
        menuItem.add(wait);
        final Pair<List<Project>, List<FileObject>> data = ActionsUtil.mineFromLookup(lookup);
        RP.post(new Runnable() {
            @Override public void run() {
                Project projects[] = ActionsUtil.getProjects(data);
                final Project project = projects.length > 0 ? projects[0] : null;
                final List<TemplateItem> items = OpenProjectList.prepareTemplates(project, getLookup());
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        menuItem.removeAll();
                        ActionListener menuListener = new PopupListener();
                        for (TemplateItem i : items) {
                            JMenuItem item = new JMenuItem(
                                    LBL_NewFileAction_Template_PopupName(i.displayName),
                                    i.icon);
                            item.addActionListener(menuListener);
                            item.putClientProperty(TEMPLATE_PROPERTY, i.template);
                            item.putClientProperty(IN_PROJECT_PROPERTY, project != null);
                            menuItem.add(item);
                        }
                        if (!items.isEmpty()) {
                            menuItem.add(new Separator());
                        }
                        JMenuItem fileItem = new JMenuItem(LBL_NewFileAction_File_PopupName(), (Icon) getValue(Action.SMALL_ICON));
                        fileItem.addActionListener(menuListener);
                        fileItem.putClientProperty(TEMPLATE_PROPERTY, null);
                        fileItem.putClientProperty(IN_PROJECT_PROPERTY, project != null);
                        menuItem.add(fileItem);
                        // #205616 - need to refresh please wait node
                        menuItem.getPopupMenu().pack();
                    }
                });
            }
        });
    }

    private class PopupListener implements ActionListener {

        @Override
        public void actionPerformed( ActionEvent e ) {
            JMenuItem source = (JMenuItem)e.getSource();

            Boolean inProject = (Boolean)source.getClientProperty( IN_PROJECT_PROPERTY );
            DataObject template = (DataObject)source.getClientProperty( TEMPLATE_PROPERTY );

            if ( inProject != null && inProject == Boolean.FALSE ) {
                doPerform( null, template, false );
            }
            else {
                doPerform( null, template, true );
            }
        }

    }

    /**
     * Variant for folder context menus that makes a submenu.
     */
    public static final class WithSubMenu extends NewFile {

        public WithSubMenu() {}

        private WithSubMenu(Lookup actionContext) {
            super(actionContext);
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return new DynaMenu(LBL_NewFileAction_PopupName());
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new WithSubMenu(actionContext);
        }

        private final class DynaMenu extends JMenu implements DynamicMenuContent {

            public DynaMenu(String a) {
                super(a);
            }

            @Override
            public JComponent[] getMenuPresenters() {
                JComponent jc = getSubmenuPopupPresenter();
                fillSubMenu();
                return new JComponent[]{ jc };
            }

            @Override
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                JComponent jc = getSubmenuPopupPresenter();
                fillSubMenu();
                return new JComponent[]{ jc };
            }
        }
    }


}
