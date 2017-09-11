/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
