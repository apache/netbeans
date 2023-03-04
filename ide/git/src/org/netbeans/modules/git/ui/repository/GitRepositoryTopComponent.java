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

package org.netbeans.modules.git.ui.repository;

import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.BorderLayout;
import java.io.*;
import java.util.logging.Level;
import org.netbeans.modules.git.Git;

/**
 * Top component of the Repository view.
 * 
 * @author Maros Sandor
 */
@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_ALWAYS, preferredID=GitRepositoryTopComponent.PREFERRED_ID)
@TopComponent.Registration(mode="output", openAtStartup=false, position=3111)
public class GitRepositoryTopComponent extends TopComponent implements Externalizable {
   
    private static final long serialVersionUID = 1L;    
    
    private RepositoryBrowserPanel         repositoryPanel;
    public static final String     PREFERRED_ID = "GitRepositories"; // NOI18N
    
    private static GitRepositoryTopComponent instance;

    public GitRepositoryTopComponent () {
        putClientProperty("SlidingName", NbBundle.getMessage(GitRepositoryTopComponent.class, "CTL_Repository_TopComponent_Title")); //NOI18N
        setName(NbBundle.getMessage(GitRepositoryTopComponent.class, "CTL_Repository_TopComponent_Title")); // NOI18N
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/git/resources/icons/versioning-view.png"));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GitRepositoryTopComponent.class, "CTL_Repository_TopComponent_Title")); // NOI18N
        repositoryPanel = new RepositoryBrowserPanel();
        add(repositoryPanel, BorderLayout.CENTER);
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    @Override
    protected void componentOpened () {
        super.componentOpened();
        setName(NbBundle.getMessage(GitRepositoryTopComponent.class, "CTL_Repository_TopComponent_Title")); // NOI18N
    }
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized GitRepositoryTopComponent getDefault () {
        if (instance == null) {
            instance = new GitRepositoryTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the GitVersioningTopComponent  instance. Never call {@link #getDefault} directly!
     */
    public static synchronized GitRepositoryTopComponent findInstance () {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Git.LOG.log(Level.FINE, "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof GitRepositoryTopComponent) {
            return (GitRepositoryTopComponent) win;
        }
        Git.LOG.log(Level.FINE,
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType () {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    @Override
    protected String preferredID () {
        return PREFERRED_ID;
    }

    @Override
    public void requestActive () {
        super.requestActive();
        repositoryPanel.requestFocusInWindow();
    }

    void selectRepository (File repository) {
        repositoryPanel.selectRepository(repository);
    }

}
