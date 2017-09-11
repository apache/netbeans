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
