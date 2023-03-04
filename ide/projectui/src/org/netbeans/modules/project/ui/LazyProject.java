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
package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Dummy project that shows a wait node while the real project list is 
 * loaded
 *
 * @author Tim Boudreau, Jaroslav Tulach
 */
final class LazyProject implements
Project, ProjectInformation, LogicalViewProvider, RecommendedTemplates {
    URL url;
    String displayName;
    ExtIcon icon;

    public LazyProject(URL url, String displayName, ExtIcon icon) {
        super();
        this.url = url;
        this.displayName = displayName;
        this.icon = icon;
    }

    @Override
    public FileObject getProjectDirectory() {
        FileObject fo = URLMapper.findFileObject(url);
        if (fo == null) {
            OpenProjectList.LOGGER.warning("Project dir with " + url + " not found!");
            fo = FileUtil.createMemoryFileSystem().getRoot();
        }
        return fo;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Icon getIcon() {
        return icon.getIcon();
    }

    @Override
    public Project getProject() {
        return this;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public Node createLogicalView() {
        return new ProjNode(Lookups.singleton(this));
    }

    @Override
    public Node findPath(Node root, Object target) {
        return null;
    }

    @Override
    public String[] getRecommendedTypes() {
        return new String[] { "simple-files" }; // NOI18N
    }
    
    
    private final class ProjNode extends AbstractNode {
        public ProjNode(Lookup lookup) {
            super(new ProjCh(), lookup);
            
            setName(url.toExternalForm());
            setDisplayName(displayName);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(icon.getIcon());
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action getPreferredAction() {
            OpenProjectList.preferredProject(LazyProject.this);
            return super.getPreferredAction();
        }

        @Override
        public boolean hasCustomizer() {
            return false;
        }

        @Override
        public Action[] getActions(boolean context) {
            OpenProjectList.preferredProject(LazyProject.this);
            return new Action[] { 
                SystemAction.get(LazyProjectInitializing.class),
                CommonProjectActions.closeProjectAction()
            };
        }
    } // end of ProjNode
    private final class ProjCh extends Children.Array {
        @Override
        protected Collection<Node> initCollection() {
            AbstractNode n = new AbstractNode(Children.LEAF);
            n.setName("init"); // NOI18N
            n.setDisplayName(NbBundle.getMessage(ProjCh.class, "MSG_ProjChInit")); 
            n.setIconBaseWithExtension("org/netbeans/modules/project/ui/resources/wait.gif");
            return Collections.singletonList((Node)n);
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            OpenProjectList.preferredProject(LazyProject.this);
        }
    }
}
