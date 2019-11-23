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

package org.netbeans.modules.javaee.project.api.ant.ui.logicalview;


import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 * J2eePlatformNode represents the J2EE platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * @see J2eePlatform
 * @author Andrei Badea
 */
class J2eePlatformNode extends AbstractNode implements PropertyChangeListener, InstanceListener {

    private static final Logger LOGGER = Logger.getLogger(J2eePlatformNode.class.getName());

    private static final String ARCHIVE_ICON = "org/netbeans/modules/javaee/project/ui/resources/jar.gif"; //NOI18N
    private static final String DEFAULT_ICON = "org/netbeans/modules/javaee/project/ui/resources/j2eeServer.gif"; //NOI18N
    private static final String BROKEN_PROJECT_BADGE = "org/netbeans/modules/javaee/project/ui/resources/brokenProjectBadge.gif"; //NOI18N
    
    private static final Icon icon = ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
    
    private static final Image brokenIcon = ImageUtilities.mergeImages(
            ImageUtilities.loadImage(DEFAULT_ICON),
            ImageUtilities.loadImage(BROKEN_PROJECT_BADGE),
            8, 0);

    private final PropertyEvaluator evaluator;
    private final String platformPropName;
    private J2eePlatform platformCache;
    
    private final PropertyChangeListener platformListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (J2eePlatform.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                fireNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
                fireDisplayNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
            }
            if (J2eePlatform.PROP_CLASSPATH.equals(evt.getPropertyName())) {
                postAddNotify();
            }
        }
    };
    private PropertyChangeListener weakPlatformListener;

    private J2eePlatformNode(Project project, PropertyEvaluator evaluator, String platformPropName, ClassPathSupport cs) {
        super(new PlatformContentChildren(project, cs));
        this.evaluator = evaluator;
        this.platformPropName = platformPropName;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        moduleProvider.addInstanceListener(WeakListeners.create(InstanceListener.class, this, moduleProvider));
    }
    
    public static J2eePlatformNode create(Project project, PropertyEvaluator evaluator, String platformPropName, ClassPathSupport cs) {
        return new J2eePlatformNode(project, evaluator, platformPropName, cs);
    }

    @Override
    public String getName () {
        return this.getDisplayName();
    }
    
    @Override
    public String getDisplayName() {
        return "";
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (getPlatform() != null)
            return getPlatform().getDisplayName();
        else 
            return NbBundle.getMessage(J2eePlatformNode.class, "LBL_J2eeServerMissing");
    }
    
    @Override
    public Image getIcon(int type) {
        Image result = null;
        if (getPlatform() != null) {
            result = getPlatform().getIcon();
        }
        return result != null ? result : brokenIcon;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new SystemAction[0];
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //The caller holds ProjectManager.mutex() read lock
        
        if (platformPropName.equals(evt.getPropertyName())) {
            refresh();
        }
    }
    
    private void refresh() {
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    refresh();
                }
            });
            return;
        }

        final J2eePlatform originalPlatform;
        final J2eePlatform platform;
        synchronized (this) {
            originalPlatform = platformCache;
            if (platformCache != null) {
                platformCache.removePropertyChangeListener(weakPlatformListener);
                platformCache = null;
            }
            platform = getPlatform();
        }

        if (originalPlatform != platform) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    J2eePlatformNode.this.fireNameChange(null, null);
                    J2eePlatformNode.this.fireDisplayNameChange(originalPlatform != null ? originalPlatform.getDisplayName() : null,
                            platform != null ? platform.getDisplayName() : null);
                    J2eePlatformNode.this.fireIconChange();

                    // The caller may hold ProjectManager.mutex() read lock (i.e., the propertyChange() method)
                    postAddNotify();
                }
            });
        }
    }
    
    public void instanceAdded(String serverInstanceID) {
        refresh();
    }
    
    public void instanceRemoved(String serverInstanceID) {
        refresh();
    }
    
    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
    }

    private void postAddNotify() {
        LibrariesNode.rp.post (new Runnable () {
            public void run () {
                ((PlatformContentChildren)getChildren()).addNotify ();
            }
        });
    }

    private synchronized J2eePlatform getPlatform () {
        if (platformCache == null) {
            String j2eePlatformInstanceId = this.evaluator.getProperty(this.platformPropName);
            if (j2eePlatformInstanceId != null) {
                platformCache = Deployment.getDefault().getJ2eePlatform(j2eePlatformInstanceId);
            }
            if (platformCache != null) {
                weakPlatformListener = WeakListeners.propertyChange(platformListener, platformCache);
                platformCache.addPropertyChangeListener(weakPlatformListener);
                // the platform has likely changed, so force the node to display the new platform's icon
                this.fireIconChange();
            }
        }
        return platformCache;
    }

    private static class PlatformContentChildren extends Children.Keys<SourceGroup> {

        private final J2eeModuleProvider j2eeModuleProvider;

        PlatformContentChildren(Project project, ClassPathSupport cs) {
            j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        }

        @Override
        protected void addNotify() {
            this.setKeys (this.getKeys());
        }

        @Override
        protected void removeNotify() {
            this.setKeys(Collections.<SourceGroup>emptySet());
        }

        protected Node[] createNodes(SourceGroup sg) {
            return new Node[] {ProjectUISupport.createFilteredLibrariesNode(PackageView.createPackageView(sg), null, null, null, null, null, null)};
        }

        private List<SourceGroup> getKeys () {
            List<SourceGroup> result;
            
            J2eePlatform j2eePlatform = ((J2eePlatformNode)this.getNode()).getPlatform();
            if (j2eePlatform != null) {
                result = new ArrayList<SourceGroup>();
                if (j2eeModuleProvider != null) {
                    try {
                        File[] classpathEntries = j2eePlatform.getClasspathEntries(j2eeModuleProvider.getConfigSupport().getLibraries());
                        addToSourceGroups(classpathEntries, result);
                    } catch (ConfigurationException ex) {
                        // noop
                        LOGGER.log(Level.INFO, null, ex);
                    }
                } else {
                    File[] classpathEntries = j2eePlatform.getClasspathEntries();
                    addToSourceGroups(classpathEntries, result);
                }
            } else {
                result = Collections.<SourceGroup>emptyList();
            }
            
            return result;
        }

        private void addToSourceGroups(File[] classpathEntries, List<SourceGroup> sourceGroups) {
            for (int i = 0; i < classpathEntries.length; i++) {
                FileObject file = FileUtil.toFileObject(classpathEntries[i]);
                if (file != null) {
                    FileObject archiveFile = FileUtil.getArchiveRoot(file);
                    if (archiveFile != null) {
                        sourceGroups.add(ProjectUISupport.createLibrariesSourceGroup(archiveFile, file.getNameExt(), icon, icon));
                    }
                }
            }
        }
    }

}
