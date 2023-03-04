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

package org.netbeans.modules.javascript.karma.ui.logicalview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.exec.KarmaServers;
import org.netbeans.modules.javascript.karma.exec.KarmaServersListener;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.javascript.karma.util.FileUtils;
import org.netbeans.modules.javascript.karma.util.KarmaUtils;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class KarmaChildrenList implements NodeList<Node>, PreferenceChangeListener {

    static final Logger LOGGER = Logger.getLogger(KarmaChildrenList.class.getName());

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public KarmaChildrenList(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public List<Node> keys() {
        if (KarmaPreferences.isEnabled(project)) {
            return Collections.<Node>singletonList(KarmaNode.create(project));
        }
        return Collections.emptyList();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public Node node(Node key) {
        return key;
    }

    @Override
    public void addNotify() {
        KarmaPreferences.addPreferenceChangeListener(project, WeakListeners.create(PreferenceChangeListener.class, this, KarmaPreferences.class));
    }

    @Override
    public void removeNotify() {
        // noop
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (!KarmaPreferences.isDebug(project)) {
            // possibly close browser tab
            KarmaServers.getInstance().closeDebugUrl(project);
        }
        // possibly restart server
        if (KarmaServers.getInstance().isServerRunning(project)) {
            KarmaServers.getInstance().stopServer(project, false);
            if (KarmaPreferences.isEnabled(project)) {
                ValidationResult result = new KarmaPreferencesValidator()
                        .validate(project)
                        .getResult();
                if (result.isFaultless()) {
                    KarmaServers.getInstance().startServer(project);
                }
            }
        }
        changeSupport.fireChange();
    }

    //~ Inner classes

    private static final class KarmaNode extends AbstractNode implements KarmaServersListener {

        @StaticResource
        private static final String KARMA_ICON = "org/netbeans/modules/javascript/karma/ui/resources/karma.png"; // NOI18N
        @StaticResource
        private static final String WAITING_BADGE = "org/netbeans/modules/javascript/karma/ui/resources/waiting.png"; // NOI18N
        @StaticResource
        private static final String RUNNING_BADGE = "org/netbeans/modules/javascript/karma/ui/resources/running.png"; // NOI18N

        private final Project project;


        @NbBundle.Messages({
            "KarmaNode.displayName=Karma",
            "KarmaNode.description=Test Runner for JavaScript",
        })
        private KarmaNode(Project project) {
            super(Children.LEAF, Lookups.fixed(project));

            assert project != null;
            this.project = project;

            setName("Karma"); // NOI18N
            setDisplayName(Bundle.KarmaNode_displayName());
            setShortDescription(Bundle.KarmaNode_description());
            setIconBaseWithExtension(KARMA_ICON);
        }

        static KarmaNode create(Project project) {
            KarmaNode karmaNode = new KarmaNode(project);
            KarmaServers.getInstance().addKarmaServersListener(karmaNode);
            return karmaNode;
        }

        @Override
        public void destroy() throws IOException {
            KarmaServers.getInstance().removeKarmaServersListener(this);
            super.destroy();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                SystemAction.get(StartKarmaServerAction.class),
                SystemAction.get(StopKarmaServerAction.class),
                SystemAction.get(RestartKarmaServerAction.class),
                null,
                SystemAction.get(DebugKarmaServerAction.class),
                null,
                SystemAction.get(ActiveKarmaConfigAction.class),
                null,
                SystemAction.get(CustomizeKarmaAction.class),
            };
        }

        @Override
        public Image getIcon(int type) {
            return badgeIcon(super.getIcon(type));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return badgeIcon(super.getOpenedIcon(type));
        }

        private Image badgeIcon(Image origImg) {
            Image badge = null;
            if (KarmaServers.getInstance().isServerStarting(project)) {
                badge = ImageUtilities.loadImage(WAITING_BADGE);
            } else if (KarmaServers.getInstance().isServerStarted(project)) {
                badge = ImageUtilities.loadImage(RUNNING_BADGE);
            }
            return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
        }

        @Override
        public void serverStateChanged(Project project) {
            if (this.project.equals(project)) {
                fireIconChange();
                fireOpenedIconChange();
            }
        }

    }

    private static final class StartKarmaServerAction extends BaseNodeAction {

        public StartKarmaServerAction() {
        }

        @Override
        protected void performAction(Project project) {
            KarmaServers.getInstance().startServer(project);
        }

        @Override
        protected boolean enable(Project project) {
            return !KarmaServers.getInstance().isServerRunning(project);
        }

        @NbBundle.Messages("StartKarmaServerAction.name=Start")
        @Override
        public String getName() {
            return Bundle.StartKarmaServerAction_name();
        }

    }

    private static final class StopKarmaServerAction extends BaseNodeAction {

        public StopKarmaServerAction() {
        }

        @Override
        protected void performAction(Project project) {
            KarmaServers.getInstance().stopServer(project, false);
        }

        @Override
        protected boolean enable(Project project) {
            return KarmaServers.getInstance().isServerRunning(project);
        }

        @NbBundle.Messages("StopKarmaServerAction.name=Stop")
        @Override
        public String getName() {
            return Bundle.StopKarmaServerAction_name();
        }

    }

    private static final class RestartKarmaServerAction extends BaseNodeAction {

        public RestartKarmaServerAction() {
        }

        @Override
        protected void performAction(Project project) {
            KarmaServers.getInstance().restartServer(project);
        }

        @Override
        protected boolean enable(Project project) {
            return KarmaServers.getInstance().isServerRunning(project);
        }

        @NbBundle.Messages("RestartKarmaServerAction.name=Restart")
        @Override
        public String getName() {
            return Bundle.RestartKarmaServerAction_name();
        }

    }

    private static final class DebugKarmaServerAction extends CallableSystemAction implements ContextAwareAction {

        public DebugKarmaServerAction() {
        }

        @NbBundle.Messages("DebugKarmaServerAction.name=Debug")
        @Override
        public String getName() {
            return Bundle.DebugKarmaServerAction_name();
        }

        @Override
        public void performAction() {
            assert false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @NbBundle.Messages("DebugKarmaServerAction.browser.none=Confirm Karma debug properties first")
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {

            final class DebugAction extends AbstractAction implements Presenter.Popup {

                private final Project project;


                private DebugAction(Lookup actionContext) {
                    project = actionContext.lookup(Project.class);
                    assert project != null : "Project expected in lookup: " + actionContext;
                    putValue(NAME, Bundle.DebugKarmaServerAction_name());
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean originalDebug = KarmaPreferences.isDebug(project);
                    if (!originalDebug
                            && !KarmaPreferences.isDebugBrowserIdSet(project)) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.DebugKarmaServerAction_browser_none()));
                        project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(JsTestingProviders.CUSTOMIZER_IDENT, null);
                        return;
                    }
                    KarmaPreferences.setDebug(project, !originalDebug);
                }

                @Override
                public JMenuItem getPopupPresenter() {
                    JCheckBoxMenuItem debugMenuItem = new JCheckBoxMenuItem(this);
                    debugMenuItem.setSelected(KarmaPreferences.isDebug(project));
                    return debugMenuItem;
                }

            }
            return new DebugAction(actionContext);
        }

    }

    private static final class ActiveKarmaConfigAction extends CallableSystemAction implements ContextAwareAction {

        public ActiveKarmaConfigAction() {
        }

        @NbBundle.Messages("ActiveKarmaConfigAction.name=Set Configuration")
        @Override
        public String getName() {
            return Bundle.ActiveKarmaConfigAction_name();
        }

        @Override
        public void performAction() {
            assert false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public Action createContextAwareInstance(final Lookup actionContext) {
            class ActiveConfigAction extends AbstractAction implements Presenter.Popup {
                @Override
                public void actionPerformed(ActionEvent e) {
                    assert false;
                }
                @Override
                public JMenuItem getPopupPresenter() {
                    return ConfigMenu.create(actionContext);
                }
            }
            return new ActiveConfigAction();
        }

    }

    private static final class ConfigMenu extends JMenu implements DynamicMenuContent, ActionListener {

        final Project project;


        private ConfigMenu(Lookup actionContext) {
            assert actionContext != null;
            project = actionContext.lookup(Project.class);
            assert project != null : "Project expected in lookup: " + actionContext;
        }

        public static ConfigMenu create(Lookup actionContext) {
            ConfigMenu configMenu = new ConfigMenu(actionContext);
            Mnemonics.setLocalizedText(configMenu, Bundle.ActiveKarmaConfigAction_name());
            return configMenu;
        }

        @Override
        public JComponent[] getMenuPresenters() {
            removeAll();
            // #238803
            File configDir = KarmaUtils.getKarmaConfigDir(project);
            List<File> configs = KarmaUtils.findKarmaConfigs(configDir);
            if (configs.isEmpty()) {
                configs = KarmaUtils.findJsFiles(configDir);
            }
            configs = FileUtils.sortFiles(configs);
            if (!configs.isEmpty()) {
                String activeConfig = KarmaPreferences.getConfig(project);
                for (final File config : configs) {
                    boolean selected = config.getAbsolutePath().equals(activeConfig);
                    JRadioButtonMenuItem configItem = new JRadioButtonMenuItem(config.getName(), selected);
                    configItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            KarmaPreferences.setConfig(project, config.getAbsolutePath());
                        }
                    });
                    add(configItem);
                }
            } else {
                setEnabled(false);
            }
            return new JComponent[] {this};
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            // always rebuild submenu
            return getMenuPresenters();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

    }

    private static final class CustomizeKarmaAction extends BaseNodeAction {

        public CustomizeKarmaAction() {
        }

        @Override
        protected void performAction(Project project) {
            project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(JsTestingProviders.CUSTOMIZER_IDENT, null);
        }

        @Override
        protected boolean enable(Project project) {
            return true;
        }

        @NbBundle.Messages("CustomizeKarmaAction.name=Properties")
        @Override
        public String getName() {
            return Bundle.CustomizeKarmaAction_name();
        }

    }

    private abstract static class BaseNodeAction extends NodeAction {

        protected abstract void performAction(Project project);

        protected abstract boolean enable(Project project);

        @Override
        protected final void performAction(Node[] activatedNodes) {
            Project project = getProject(activatedNodes);
            if (project == null) {
                LOGGER.fine("No project found -> no karma action performed");
                return;
            }
            performAction(project);
        }

        @Override
        protected final boolean enable(Node[] activatedNodes) {
            Project project = getProject(activatedNodes);
            if (project == null) {
                LOGGER.fine("No project found -> no karma action enabled");
                return false;
            }
            return enable(project);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @CheckForNull
        private Project getProject(Node[] activatedNodes) {
            if (activatedNodes.length != 1) {
                return null;
            }
            Node node = activatedNodes[0];
            return node.getLookup().lookup(Project.class);
        }

    }

}
