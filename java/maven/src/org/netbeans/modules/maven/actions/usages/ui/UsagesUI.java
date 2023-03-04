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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.actions.usages.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.actions.usages.ui.Bundle.*;
import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBGroupInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.netbeans.modules.maven.spi.IconResources;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.NotificationLineSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Anuradha G (theanuradha-at-netbeans.org)
 */
public final class UsagesUI extends javax.swing.JPanel implements ExplorerManager.Provider {
    private static final @StaticResource String ARTIFACT_BADGE = "org/netbeans/modules/maven/actions/usages/ArtifactBadge.png";

    static final int TYPE_DEPENDENCY = 0;
    static final int TYPE_COMPILE = 1;
    static final int TYPE_TEST = 2;
    static final int TYPE_RUNTIME = 3;
    private final ExplorerManager explorerManager = new ExplorerManager();
    private NotificationLineSupport nls;
    private final Artifact artifact;
    private final String libDef;

    /** Creates new form UsagesUI */
    public UsagesUI(final String libDef, final Artifact artifact) {
        initComponents();
        this.libDef = libDef;
        this.artifact = artifact;
    }

    @Messages({
        "# {0} - maven coordinates",
        "LBL_Repo=<html>Repository Artifacts that use {0} as Dependency </html>", 
        "# {0} - maven coordinates",
        "LBL_Description=<html>Open Projects that use {0} as Dependency </html>", 
        "LBL_Incomplete=Incomplete result, processing indices...", 
        "LBL_Dependancy=As Direct Dependency", 
        "LBL_TYPE_COMPILE=As Compile Time Dependency ", 
        "LBL_TYPE_RUNTIME=As Runtime Dependency", 
         "LBL_TYPE_TEST=As Test Dependency"})
     void initNodes(final String libDef, final Artifact artifact) {
        Children openProjectsChildren = new Children.Keys<Integer>() {

            @Override
            protected Node[] createNodes(Integer type) {
                final List<NbMavenProjectImpl> openProjects = getOpenProjects(artifact, type);
                Children children = new Children.Keys<NbMavenProjectImpl>() {

                    @Override
                    protected Node[] createNodes(NbMavenProjectImpl nmp) {
                        return new Node[]{new OpenProjectNode(nmp)};
                    }

                    @Override
                    protected void addNotify() {
                        super.addNotify();
                        setKeys(openProjects);
                    }
                };
                AbstractNode node = new AbstractNode(children) {

                    @Override
                    public String getHtmlDisplayName() {
                        return getDisplayName();
                    }

                    @Override
                    public Image getIcon(int arg0) {
                        return NodeUtils.getTreeFolderIcon(false);
                    }

                    @Override
                    public Image getOpenedIcon(int arg0) {
                        return NodeUtils.getTreeFolderIcon(true);
                    }
                };
                switch (type) {
                    case TYPE_DEPENDENCY:
                         {
                            node.setDisplayName(LBL_Dependancy());//NOI18N
                        }
                        break;
                    case TYPE_COMPILE:
                         {
                            node.setDisplayName(LBL_TYPE_COMPILE());//NOI18N
                        }
                        break;
                    case TYPE_TEST:
                         {
                            node.setDisplayName(LBL_TYPE_TEST());//NOI18N
                        }
                        break;
                    case TYPE_RUNTIME:
                         {
                            node.setDisplayName(LBL_TYPE_RUNTIME());//NOI18N
                        }
                        break;
                }
                return new Node[]{node};
            }

            @Override
            protected void addNotify() {
                super.addNotify();
                setKeys(new Integer[]{TYPE_DEPENDENCY, TYPE_COMPILE, TYPE_TEST, TYPE_RUNTIME});
            }
        };

        final AbstractNode openProjectsNode = new AbstractNode(openProjectsChildren) {

            @Override
            public String getHtmlDisplayName() {
                return LBL_Description(libDef);
            }

            @Override
            public Image getIcon(int arg0) {
                return NodeUtils.getTreeFolderIcon(false);
            }

            @Override
            public Image getOpenedIcon(int arg0) {
                return NodeUtils.getTreeFolderIcon(true);
            }
        };
        //TODO out of AWT
        Result<NBGroupInfo> result = RepositoryQueries.findDependencyUsageResult(
                                    artifact.getGroupId(),
                                        artifact.getArtifactId(), artifact.getVersion(), null);
        final List<NBGroupInfo> list = result.getResults();
        nls.setWarningMessage(LBL_Incomplete());
        Children repoChildren = new Children.Keys<NBGroupInfo>() {

            @Override
            protected Node[] createNodes(NBGroupInfo ug) {
                return new Node[]{new GroupNode(ug)};
            }

            @Override
            protected void addNotify() {
                super.addNotify();
                setKeys(list);
            }
        };
        AbstractNode repoNode = new AbstractNode(repoChildren) {

            @Override
            public String getHtmlDisplayName() {

                return LBL_Repo(libDef);
            }

            @Override
            public Image getIcon(int arg0) {
                return NodeUtils.getTreeFolderIcon(false);
            }

            @Override
            public Image getOpenedIcon(int arg0) {
                return NodeUtils.getTreeFolderIcon(true);
            }
        };

        Children.Array array = new Children.Array();
        array.add(new Node[]{openProjectsNode, repoNode});
        explorerManager.setRootContext(new AbstractNode(array));
        final BeanTreeView beanTreeView = (BeanTreeView) jScrollPane1;
        beanTreeView.setPopupAllowed(false);
        beanTreeView.setRootVisible(false);

        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        beanTreeView.expandAll();
                    }
                });
            }
        }, 100);
         RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {

                try {
                    explorerManager.setSelectedNodes(new Node[]{openProjectsNode});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        }, 600);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder(null, javax.swing.UIManager.getDefaults().getColor("CheckBoxMenuItem.selectionBackground")));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public List<NbMavenProjectImpl> getOpenProjects(Artifact artifact, int type) {
        List<NbMavenProjectImpl> mavenProjects = new ArrayList<NbMavenProjectImpl>();
        //get all open projects

        Project[] prjs = OpenProjects.getDefault().getOpenProjects();

        for (Project project : prjs) {
            //varify is this a maven project 
            NbMavenProjectImpl mavProj = project.getLookup().lookup(NbMavenProjectImpl.class);
            if (mavProj != null) {

                MavenProject mp = mavProj.getOriginalMavenProject();
                List<Artifact> artifacts = new ArrayList<Artifact>();
                switch (type) {
                    case TYPE_DEPENDENCY:
                        {
                            Set<Artifact> deps = mp.getDependencyArtifacts();
                            if (deps != null) {
                                artifacts.addAll(deps);
                            }
                        }
                        break;
                    case TYPE_COMPILE:
                         {
                            List<Artifact> compArtifs = mp.getCompileArtifacts();
                            if (compArtifs != null) {
                                artifacts.addAll(compArtifs);
                            }
                        }
                        break;
                    case TYPE_TEST:
                         {
                            List<Artifact> testArtifs = mp.getTestArtifacts();
                            if (testArtifs != null) {
                                artifacts.addAll(testArtifs);
                            }
                            List<Artifact> compArtifs = mp.getCompileArtifacts();
                            if (compArtifs != null) {
                                artifacts.removeAll(compArtifs);
                            }
                        }
                        break;
                    case TYPE_RUNTIME:
                         {
                            List<Artifact> runtimeArtifs = mp.getRuntimeArtifacts();
                            if (runtimeArtifs != null) {
                                artifacts.addAll(runtimeArtifs);
                            }
                            List<Artifact> compArtifs = mp.getCompileArtifacts();
                            if (compArtifs != null) {
                                artifacts.removeAll(compArtifs);
                            }
                        }
                        break;
                }

                for (Artifact d : artifacts) {
                    if (d.getGroupId().equals(artifact.getGroupId()) && d.getArtifactId().equals(artifact.getArtifactId()) && d.getVersion().equals(artifact.getVersion())) {

                        mavenProjects.add(mavProj);
                        break;
                    }
                }
            }



        }

        return mavenProjects;

    }

    public void initialize(NotificationLineSupport nls) {
        assert nls != null;
        this.nls = nls;
        initNodes(libDef, artifact);
    }

    private static class GroupNode extends AbstractNode {

        NBGroupInfo group;

        public GroupNode(final NBGroupInfo group) {
            super(new Children.Keys<NBArtifactInfo>() {

                @Override
                protected Node[] createNodes(NBArtifactInfo arg0) {
                    return new Node[]{new ArtifactNode(arg0)};
                }

                @Override
                protected void addNotify() {
                    super.addNotify();
                    setKeys(group.getArtifactInfos());
                }
            });
            this.group = group;

        }

        @Override
        public Image getIcon(int arg0) {
            return NodeUtils.getTreeFolderIcon(false);
        }

        @Override
        public Image getOpenedIcon(int arg0) {
            return NodeUtils.getTreeFolderIcon(true);
        }

        @Override
        public String getDisplayName() {
            return group.getName();
        }
    }

    private static class ArtifactNode extends AbstractNode {

        NBArtifactInfo artifact;

        public ArtifactNode(final NBArtifactInfo artifact) {
            super(new Children.Keys<NBVersionInfo>() {

                @Override
                protected Node[] createNodes(NBVersionInfo arg0) {
                    return new Node[]{new VersionNode(arg0)};
                }

                @Override
                protected void addNotify() {
                    super.addNotify();
                    setKeys(artifact.getVersionInfos());
                }
            });
            this.artifact = artifact;

        }

        @Override
        public Image getIcon(int arg0) {
            Image badge = ImageUtilities.loadImage(ARTIFACT_BADGE, true); //NOI18N
            return badge;
        }

        @Override
        public Image getOpenedIcon(int arg0) {
            return getIcon(arg0);
        }

        @Override
        public String getDisplayName() {
            return artifact.getName();
        }
    }

    private static class VersionNode extends AbstractNode {

        NBVersionInfo version;

        public VersionNode(NBVersionInfo version) {
            super(Children.LEAF);
            this.version = version;
            setIconBaseWithExtension(IconResources.DEPENDENCY_ICON); //NOI18N
        }

        @Override
        public String getDisplayName() {
            return version.getVersion()+" [ "+version.getType()+" ]";
        }

        public @Override Action getPreferredAction() {
            return new AbstractAction() {
                public @Override void actionPerformed(ActionEvent e) {
                    ArtifactViewer.showArtifactViewer(version);
                }
            };
        }

    }

    private static class OpenProjectNode extends AbstractNode {

        private final NbMavenProjectImpl project;
        private final ProjectInformation pi;

        public OpenProjectNode(NbMavenProjectImpl project) {
            super(Children.LEAF);
            this.project = project;
            pi = ProjectUtils.getInformation(project);
        }

        @Override
        public Image getIcon(int arg0) {
            return ImageUtilities.icon2Image(pi.getIcon());
        }

        @Override
        public String getDisplayName() {
            return pi.getDisplayName();
        }
    }
}
