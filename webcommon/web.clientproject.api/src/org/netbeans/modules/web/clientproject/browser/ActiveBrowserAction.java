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
package org.netbeans.modules.web.clientproject.browser;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserPickerPopup;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

@ActionID(id="org.netbeans.modules.web.clientproject.browser.ActiveBrowserAction", category="Project")
@ActionRegistration(displayName="#ActiveBrowserAction.reg.name", lazy=false)
@ActionReferences({
    @ActionReference(path="Menu/BuildProject", position=307),
    @ActionReference(path="Toolbars/Build", position=87)
})
public class ActiveBrowserAction extends CallableSystemAction implements LookupListener {

    private final Lookup lookup;
    private @NullAllowed Project currentProject;
    private @NullAllowed ProjectBrowserProvider currentBrowserProvider;
    private final PropertyChangeListener currentBrowserProviderListener;
    private JButton toolbarButton;
    private JMenu menuAction;
    private Lookup.Result<Project> resultPrj;
    private Lookup.Result<DataObject> resultDO;
    private Lookup.Result<FileObject> resultFO;
    private Lookup.Result<ProjectBrowserProvider> resultPBP;
    private LookupListener lookupListener;
    private WebBrowser lastWebBrowser = null;

    private ChangeListener ideBrowserChangeListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            Project p = getCurrentProject();
            ProjectBrowserProvider pbp = null;
            if (p != null) {
                pbp = p.getLookup().lookup(ProjectBrowserProvider.class);
            }
            updateButton(pbp);
        }
    };

    private static final RequestProcessor RP = new RequestProcessor(ActiveBrowserAction.class);

    public ActiveBrowserAction() {
        lookup = LastActivatedWindowLookup.INSTANCE;
        resultPrj = lookup.lookupResult(Project.class);
        resultDO = lookup.lookupResult(DataObject.class);
        resultFO = lookup.lookupResult(FileObject.class);
        resultPrj.addLookupListener(WeakListeners.create(LookupListener.class, this, resultPrj));
        resultDO.addLookupListener(WeakListeners.create(LookupListener.class, this, resultDO));
        resultFO.addLookupListener(WeakListeners.create(LookupListener.class, this, resultFO));
        currentBrowserProviderListener = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (ProjectBrowserProvider.PROP_BROWSER_ACTIVE.equals(evt.getPropertyName())) {
                    updateButton(ActiveBrowserAction.this.getBrowserProvider());
                }
            }
        };
        lookupListener = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                refreshViewLater(true);
            }
        };

        refreshView(false);
    }

    @Override
    public void performAction() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    @NbBundle.Messages({
        "ActiveBrowserAction.name=Set Project Browser"
    })
    public String getName() {
        return Bundle.ActiveBrowserAction_name();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.browser.ActiveBrowserAction"); // NOI18N
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        refreshViewLater(false);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenu m = new JMenu(Bundle.ActiveBrowserAction_name());
        m.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                JMenu m = (JMenu)e.getSource();
                m.removeAll();
                ProjectBrowserProvider pbp = ActiveBrowserAction.this.getBrowserProvider();
                for (Component mi : createMenuItems(pbp != null ? pbp.getActiveBrowser() : null)) {
                    if (mi instanceof JSeparator) {
                        m.addSeparator();
                    } else {
                        m.add(mi);
                    }
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        menuAction = m;
        return m;
    }

    private ProjectBrowserProvider getBrowserProvider() {
        synchronized (this) {
            return currentBrowserProvider;
        }
    }

    @NbBundle.Messages({
        "ActiveBrowserAction.customize=Customize"
    })
    private List<Component> createMenuItems(WebBrowser selectedWebBrowser) {
        List<Component> l = new ArrayList<>();
        final ProjectBrowserProvider pbp = getBrowserProvider();
        if (pbp != null) {
            ButtonGroup group = new ButtonGroup();
            for (WebBrowser wb : pbp.getBrowsers()) {
                JRadioButtonMenuItem mi = new JRadioButtonMenuItem(new SelectBrowserAction(pbp, wb));
                group.add(mi);
                if (selectedWebBrowser != null && wb.getId().equals(selectedWebBrowser.getId())) {
                    mi.setSelected(true);
                }
                l.add(mi);
            }
            if (pbp.hasCustomizer()) {
                l.add(new JSeparator());
                Action customizeAction = new AbstractAction(Bundle.ActiveBrowserAction_customize()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        pbp.customize();
                    }
                };
                JMenuItem mi = new JMenuItem(customizeAction);
                l.add(mi);
            }
        }
        return l;
    }

    private static boolean isSmallToolbarIcon() {
        return 16 == ToolbarPool.getDefault().getPreferredIconSize();
    }

    private static class SelectBrowserAction extends AbstractAction {

        private ProjectBrowserProvider pbp;
        private WebBrowser wb;

        public SelectBrowserAction(ProjectBrowserProvider pbp, WebBrowser wb) {
            super(BrowserUISupport.getLongDisplayName(wb), ImageUtilities.image2Icon(wb.getIconImage(isSmallToolbarIcon())));
            this.pbp = pbp;
            this.wb = wb;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                pbp.setActiveBrowser(wb);
            } catch ( IllegalArgumentException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    @Override
    public JMenuItem getPopupPresenter() {
        return super.getPopupPresenter(); //To change body of generated methods, choose Tools | Templates.
    }

    private static final @StaticResource String GENERIC_SMALL = "org/netbeans/modules/web/clientproject/browser/browser_generic_16x.png"; // NOI18N
    private static final @StaticResource String GENERIC_LARGE = "org/netbeans/modules/web/clientproject/browser/browser_generic_24x.png"; // NOI18N
    private static final @StaticResource String DISABLED_SMALL = "org/netbeans/modules/web/clientproject/browser/browser_disabled_16x.png"; // NOI18N
    private static final @StaticResource String DISABLED_LARGE = "org/netbeans/modules/web/clientproject/browser/browser_disabled_24x.png"; // NOI18N

    @Override
    public Component getToolbarPresenter() {
        final JButton button = new JButton();
        button.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBrowserPickerPopup( button );
            }
        });
        button.setDisabledIcon(badgeWithArrowIcon(
            ImageUtilities.loadImage(isSmallToolbarIcon() ? DISABLED_SMALL : DISABLED_LARGE)));
        button.setEnabled(false);
        button.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
               if ("PreferredIconSize".equals(evt.getPropertyName())) { // NOI18N
                   refreshViewLater(true);
               }
            }
        });
        ProjectBrowserProvider pbp = getBrowserProvider();
        toolbarButton = button;
        updateButton(pbp);
        return button;
    }

    private void refreshViewLater(final boolean projectLookupChange) {
        RP.post(new Runnable() {
            public @Override void run() {
                refreshView(projectLookupChange);
            }
        });
    }

    private void refreshView(boolean projectLookupChange) {
        Project[] selected = getProjectsFromLookup(lookup, null);
        if (selected.length == 1) {
            activeProjectChanged(selected[0], projectLookupChange);
        } else {
            activeProjectChanged(null, projectLookupChange);
        }
    }

    private void activeProjectChanged(Project p, boolean projectLookupChange) {
        ProjectBrowserProvider pbp = null;
        synchronized (this) {
            if (currentProject == p && !projectLookupChange) {
                return;
            }
            if (resultPBP != null) {
                resultPBP.removeLookupListener(lookupListener);
                resultPBP = null;
            }
            if (currentBrowserProvider != null) {
                currentBrowserProvider.removePropertyChangeListener(currentBrowserProviderListener);
                currentBrowserProvider = null;
            }
            currentProject = p;
            if (currentProject != null) {
                resultPBP = currentProject.getLookup().lookupResult(ProjectBrowserProvider.class);
                resultPBP.addLookupListener(lookupListener);
                Collection<? extends ProjectBrowserProvider> c = resultPBP.allInstances();
                currentBrowserProvider = c.isEmpty() ? null : c.iterator().next();
                pbp = currentBrowserProvider;
                if (currentBrowserProvider != null) {
                    currentBrowserProvider.addPropertyChangeListener(currentBrowserProviderListener);
                }
            }
        }
        updateButton(pbp);
    }

    private synchronized Project getCurrentProject() {
        return currentProject;
    }

    @NbBundle.Messages({
        "ActiveBrowserAction.missingProject=Project does not have any browser selected"
    })
    private void updateButton(ProjectBrowserProvider pbp) {
        JButton tb = toolbarButton;
        if (tb != null) {
            if (lastWebBrowser != null) {
                lastWebBrowser.removeChangeListener(ideBrowserChangeListener);
                lastWebBrowser = null;
            }
            if (pbp == null) {
                tb.setIcon(badgeWithArrowIcon(ImageUtilities.loadImage(isSmallToolbarIcon() ? DISABLED_SMALL : DISABLED_LARGE))); // NOI18N
                tb.setDisabledIcon(badgeWithArrowIcon(ImageUtilities.loadImage(isSmallToolbarIcon() ? DISABLED_SMALL : DISABLED_LARGE))); // NOI18N
                tb.setToolTipText(null);
            } else {
                WebBrowser wb = pbp.getActiveBrowser();
                Image im;
                if (wb != null) {
                    im = wb.getIconImage(isSmallToolbarIcon());
                    tb.setToolTipText(BrowserUISupport.getLongDisplayName(wb));
                    wb.addChangeListener(ideBrowserChangeListener);
                } else {
                    im = ImageUtilities.loadImage(isSmallToolbarIcon() ? GENERIC_SMALL : GENERIC_LARGE); // NOI18N
                    tb.setToolTipText(Bundle.ActiveBrowserAction_missingProject());
                }
                tb.setIcon(badgeWithArrowIcon(im));
                lastWebBrowser = wb;
            }
            tb.setEnabled(pbp != null);
        }
        JMenu ma = menuAction;
        if (ma != null) {
            ma.setEnabled(pbp != null);
        }
    }

    private Icon badgeWithArrowIcon(Image im) {
        // #235642
        assert im != null : "Image must be provided";
        return ImageUtilities.image2Icon(ImageUtilities.mergeImages(im,
            ImageUtilities.icon2Image(DropDownButtonFactory.getArrowIcon(false)),
            isSmallToolbarIcon() ? 20 : 28, isSmallToolbarIcon() ? 6 : 10)); // NOI18N
    }

    private void showBrowserPickerPopup( JButton invoker ) {
        final ProjectBrowserProvider provider = getBrowserProvider();
        if( null == provider )
            return;
        final BrowserPickerPopup popup = BrowserPickerPopup.create( provider );
        final ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged( ChangeEvent e ) {
                popup.removeChangeListener( this );
                WebBrowser selBrowser = popup.getSelectedBrowser();
                if( null != selBrowser ) {
                    try {
                        provider.setActiveBrowser( selBrowser );
                    } catch( IllegalArgumentException | IOException ex ) {
                        Exceptions.printStackTrace( ex );
                    }
                }
            }
        };
        popup.addChangeListener( changeListener );
        popup.show( invoker, 0, invoker.getHeight() );
    }

    // XXX: copy&pasted from project.ui.actions.LookupSensitiveAction.LastActivatedWindowLookup
    /**
     * #120721: do not want to use Utilities.actionsGlobalContext since that does not survive focus change,
     * and we would like to mimic the selection tracking behavior of Hacks.keepCurrentProjectNameUpdated.
     */
    static final class LastActivatedWindowLookup extends ProxyLookup implements PropertyChangeListener {

        static final Lookup INSTANCE = new LastActivatedWindowLookup();

        private final TopComponent.Registry reg = TopComponent.getRegistry();

        LastActivatedWindowLookup() {
            reg.addPropertyChangeListener(this);
            updateLookups();
        }

        private void updateLookups() {
            Node[] nodes = reg.getActivatedNodes();
            if( nodes.length == 0 ) {
                TopComponent activeTc = reg.getActivated();
                if( null != activeTc ) {
                    Collection<? extends Node> nodesFromLookup = activeTc.getLookup().lookupAll(Node.class );
                    nodes = nodesFromLookup.toArray(new Node[0] );
                }
            }
            Lookup[] delegates = new Lookup[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                delegates[i] = nodes[i].getLookup();
            }
            setLookups(delegates);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName())) {
                updateLookups();
            }
        }

    }

    // XXX: copied from project.ui.actions.ActionsUtil:
    @NonNull
    public static Project[] getProjectsFromLookup( Lookup lookup, String command ) {
        // First find out whether there is a project directly in the Lookup
        Set<Project> result = new LinkedHashSet<>(); // XXX or use OpenProjectList.projectByDisplayName?
        for (Project p : lookup.lookupAll(Project.class)) {
            result.add(p);
        }
        // Now try to guess the project from fileobjects
        for (FileObject fObj : lookup.lookupAll(FileObject.class)) {
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                result.add( p );
            }
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                result.add( p );
            }
        }
        Project[] projectsArray = result.toArray(new Project[0]);

        if ( command != null ) {
            // All projects have to have the command enabled
            for (Project p : projectsArray) {
                if (!commandSupported(p, command, lookup)) {
                    return new Project[0];
                }
            }
        }

        return projectsArray;
    }

    // XXX: copied from project.ui.actions.ActionsUtil:
    public static boolean commandSupported( Project project, String command, Lookup context ) {
        //We have to look whether the command is supported by the project
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if ( ap != null ) {
            List<String> commands = Arrays.asList(ap.getSupportedActions());
            if ( commands.contains( command ) ) {
                try {
                if (context == null || ap.isActionEnabled(command, context)) {
                    //System.err.println("cS: true project=" + project + " command=" + command + " context=" + context);
                    return true;
                }
                } catch (IllegalArgumentException x) {
                    Logger.getLogger(ActiveBrowserAction.class.getName()).log(Level.INFO, "#213589: possible race condition in MergedActionProvider", x);
                }
            }
        }
        //System.err.println("cS: false project=" + project + " command=" + command + " context=" + context);
        return false;
    }
}
