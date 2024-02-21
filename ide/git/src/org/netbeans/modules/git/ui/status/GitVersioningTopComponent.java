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

package org.netbeans.modules.git.ui.status;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Top component of the Versioning view.
 * 
 * @author Maros Sandor
 */
@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_ALWAYS, preferredID=GitVersioningTopComponent.PREFERRED_ID)
@TopComponent.Registration(mode="output", openAtStartup=false, position=3110)
public class GitVersioningTopComponent extends TopComponent implements Externalizable, PropertyChangeListener {
   
    private static final long serialVersionUID = 1L;    
    
    private VersioningPanelController         controller;
    private VCSContext              context;
    private String                  contentTitle;
    private String                  branchTitle;
    public static final String     PREFERRED_ID = "GitVersioning"; // NOI18N
    private RepositoryInfo          repositoryInfo;
    
    private static GitVersioningTopComponent instance;
    private File[] files = new File[0];

    public GitVersioningTopComponent () {
        putClientProperty("SlidingName", NbBundle.getMessage(GitVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); //NOI18N
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); //NOI18N

        setName(NbBundle.getMessage(GitVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/git/resources/icons/versioning-view.png"));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GitVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
        controller = new VersioningPanelController();
        controller.setActions(this);
        add(controller.getPanel());
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    @Override
    protected void componentActivated () {
        updateTitle();
        controller.focus();
    }

    @Override
    protected void componentOpened () {
        super.componentOpened();
        refreshContent();
    }

    @Override
    protected void componentClosed () {
        controller.cancelRefresh();
        super.componentClosed();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(this.contentTitle);
        File[] files = context == null
                ? this.files
                : context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        out.writeObject(files);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        contentTitle = (String) in.readObject();
        files = (File[]) in.readObject();
        final List<Node> nodes = new ArrayList<>(files.length);
        for (File file : files) {
            nodes.add(new AbstractNode(Children.LEAF, Lookups.singleton(file)) {
                @Override
                public String getDisplayName() {
                    return getLookup().lookup(File.class).getName();
                }
            });
        }
        Utils.post(new Runnable() {

            @Override
            public void run () {
                try {
                    OpenProjects.getDefault().openProjects().get();
                } catch (InterruptedException | ExecutionException ex) {
                }
                final VCSContext ctx = VCSContext.forNodes(nodes.toArray(new Node[0]));
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run () {
                        if (context == null) {
                            setContext(ctx);
                        }
                    }
                    
                });
            }
        });
    }
    
    public boolean hasContext () {
        return context != null;
    }

    private void refreshContent () {
        if (controller == null) return;  // the component is not showing => nothing to refresh
        updateTitle();
        controller.setContext(context == null ? VCSContext.EMPTY : context);
    }
    
    private void updateTitle () {
        EventQueue.invokeLater(new Runnable (){
            @Override
            public void run () {
                if (contentTitle == null) {
                    setName(NbBundle.getMessage(GitVersioningTopComponent.class, "CTL_Versioning_TopComponent_Title")); // NOI18N
                } else {
                    File baseFile = GitUtils.getRootFile(context);
                    String name = "";
                    if(baseFile != null){
                        name = baseFile.getName();
                    }
                    
                    if (branchTitle == null) {
                        setName(NbBundle.getMessage(GitVersioningTopComponent.class, 
                                "CTL_Versioning_TopComponent_MultiTitle", contentTitle, name.equals(contentTitle)? "": "[" + name + "]"));  // NOI18N
                    } else {
                        setName(NbBundle.getMessage(GitVersioningTopComponent.class, 
                                "CTL_Versioning_TopComponent_Title_ContentBranch", contentTitle, name.equals(contentTitle)? "": "[" + name + "] ", branchTitle)); // NOI18N
                    }
                }                
                setToolTipText(getName());
            }
        });
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized GitVersioningTopComponent getDefault () {
        if (instance == null) {
            instance = new GitVersioningTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the GitVersioningTopComponent  instance. Never call {@link #getDefault} directly!
     */
    public static synchronized GitVersioningTopComponent findInstance () {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Git.LOG.log(Level.FINE, "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof GitVersioningTopComponent) {
            return (GitVersioningTopComponent) win;
        }
        Git.LOG.log(Level.FINE,
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType () {
        // #129268: Need VCSContext to be persistable for this to be set to PERSISTENCE_ALWAYS
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    @Override
    protected String preferredID () {
        return PREFERRED_ID;
    }

    /**
     * Sets files/folders the user wants to synchronize. They are typically activated (selected) nodes.
     * 
     * @param ctx new context of the Versioning view
     */
    public void setContext (VCSContext ctx) {
        assert EventQueue.isDispatchThread();
        assert ctx != null;
        controller.cancelRefresh();
        setEnabled(true);
        setCursor(Cursor.getDefaultCursor());
        context = ctx;
        refreshBranchName();
        refreshContent();
        setToolTipText(getName());
    }

    /**
     * Sets the 'content' portion of Versioning component title.
     * Title pattern: Versioning[ - contentTitle[ - branchTitle]] (10 minutes ago)
     *
     * @param contentTitle a new content title, e.g. "2 projects" // NOI18N
     */
    public void setContentTitle (String contentTitle) {
        this.contentTitle = contentTitle;
        updateTitle();
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (RepositoryInfo.PROPERTY_ACTIVE_BRANCH.equals(evt.getPropertyName())) {
            setBranchTitle(((GitBranch) evt.getNewValue()));
            updateTitle();
        }
    }

    void refreshBranchName () {
        Runnable runnable = new Runnable () {
            @Override
            public void run() {
                if (repositoryInfo != null) {
                    repositoryInfo.removePropertyChangeListener(GitVersioningTopComponent.this);
                    repositoryInfo = null;
                }
                Set<File> repositoryRoots = GitUtils.getRepositoryRoots(context);
                branchTitle = null;
                if (repositoryRoots.size() == 1) {
                    repositoryInfo = RepositoryInfo.getInstance(repositoryRoots.iterator().next());
                    GitBranch branch = repositoryInfo.getActiveBranch();
                    if (branch != null) {
                        setBranchTitle(branch);
                    }
                    repositoryInfo.addPropertyChangeListener(GitVersioningTopComponent.this);
                }
                updateTitle();
            }
        };
        if (EventQueue.isDispatchThread()) {
            Utils.post(runnable);
        } else {
            runnable.run();
        }
    }

    private void setBranchTitle (GitBranch branch) {
        branchTitle = branch.getName();
    }
}
