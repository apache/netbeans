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
package org.netbeans.modules.cloud.oracle.devops;

import com.oracle.bmc.devops.DevopsClient;
import com.oracle.bmc.devops.model.CreateRepositoryDetails;
import com.oracle.bmc.devops.model.MirrorRepositoryConfig;
import com.oracle.bmc.devops.model.Repository;
import com.oracle.bmc.devops.requests.CreateRepositoryRequest;
import com.oracle.bmc.devops.responses.CreateRepositoryResponse;
import com.oracle.bmc.model.BmcException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.progress.ProgressMonitor.DefaultProgressMonitor;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.QuickPick.Item;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
            id = "org.netbeans.modules.cloud.oracle.actions.AddRepository"
)
@ActionRegistration(
        displayName = "#AddRepository",
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/DevopsProject/Actions", position = 260)
})
@NbBundle.Messages({
    "AddRepository=Add Repository to Project",
    "SelectRepo=Select Repository"})
public class AddRepositoryAction implements ActionListener {

    private final DevopsProjectItem projectItem;

    public AddRepositoryAction(DevopsProjectItem project) {
        this.projectItem = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        Set<Item> repos = new HashSet<> ();
        for (Project project : projects) {
            File prjDir = FileUtil.toFile(project.getProjectDirectory());
            VersioningSystem owner = VersioningSupport.getOwner(prjDir);
            if (owner == null) {
                continue;
            }
            GitRepository git = GitRepository.getInstance(owner.getTopmostManagedAncestor(prjDir));
            try {
                DefaultProgressMonitor monitor = new DefaultProgressMonitor();
                Map<String, GitRemoteConfig> remotes = git.createClient().getRemotes(monitor);
                for (Map.Entry<String, GitRemoteConfig> remote : remotes.entrySet()) {
                    for (String repo : remote.getValue().getUris()) {
                        repos.add(new Item(repo, repo));
                    }
                }
            } catch (GitException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
        }
            
        NotifyDescriptor.QuickPick qp = new NotifyDescriptor.QuickPick(Bundle.SelectRepo(), projectItem.getName(),
            new ArrayList(repos), false);

        if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(qp)) {
            Optional<Item> url = qp.getItems().stream().findFirst();
            if (url.isPresent()) {
                try ( DevopsClient client = new DevopsClient(OCIManager.getDefault().getConfigProvider())) {
                    MirrorRepositoryConfig config = MirrorRepositoryConfig.builder().repositoryUrl(url.get().getLabel()).build();
                    CreateRepositoryDetails details = CreateRepositoryDetails.builder()
                            .mirrorRepositoryConfig(config)
                            .projectId(projectItem.getKey().getValue())
                            .name(projectItem.getKey().getValue())
                            .repositoryType(Repository.RepositoryType.Mirrored)
                            .build();
                    CreateRepositoryRequest request = CreateRepositoryRequest.builder().createRepositoryDetails(details).build();
                    CreateRepositoryResponse response = client.createRepository(request);
                } catch (BmcException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
