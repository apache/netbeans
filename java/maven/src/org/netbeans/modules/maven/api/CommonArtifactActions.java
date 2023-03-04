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
package org.netbeans.modules.maven.api;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.netbeans.modules.maven.actions.CreateLibraryAction;
import org.netbeans.modules.maven.actions.ViewJavadocAction;
import org.netbeans.modules.maven.actions.scm.CheckoutAction;
import org.netbeans.modules.maven.actions.usages.FindArtifactUsages;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.api.Bundle.*;

/**
 *
 * @author Anuradha G (anuradha@codehaus.org)
 */
public class CommonArtifactActions {

    private CommonArtifactActions() {}

    public static Action createViewJavadocAction(Artifact artifact) {
       return new ViewJavadocAction(artifact);

    }

    public static Action createFindUsages(Artifact artifact) {
        
        return new FindArtifactUsages(artifact);
    }

    public static Action createViewArtifactDetails(Artifact art, List<ArtifactRepository> remoteRepos) {
        return new ShowArtifactAction(art, remoteRepos);
    }

    /**
     * create an action instance that performs scm checkout based on the MavenProject
     * instance provided in the lookup parameter. If no MavenProject is provided
     * up front it will listen on addition later. Without a MavenProject instance, it's disabled.
     * Only for use from artifact viewer window.
     * NOT to be used with global Lookup instances.
     */
    public static Action createScmCheckoutAction(Lookup lkp) {
        return new CheckoutAction(lkp);
    }

    /**
     * create an action instance that create a NetBeans library based on the MavenProject
     * instance provided in the lookup parameter. If no MavenProject is provided
     * up front it will listen on addition later. Without a MavenProject instance, it's disabled.
     *
     * NOT to be used with global Lookup instances.
     * @param lkp
     * @return action
     *
     */
    public static Action createLibraryAction(Lookup lkp) {
        return new CreateLibraryAction(lkp);
    }

    private static class ShowArtifactAction extends AbstractAction {
        private Artifact artifact;
        private List<ArtifactRepository> repos;

        @Messages("ACT_View_Details=View Artifact Details")
        ShowArtifactAction(Artifact art, List<ArtifactRepository> repos) {
            this.artifact = art;
            this.repos = repos;
            putValue(NAME, ACT_View_Details());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String hint = (String) getValue("PANEL_HINT"); //NOI18N
            ArtifactViewer.showArtifactViewer(artifact, repos, hint);
        }
    }
    
}
