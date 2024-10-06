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
package org.netbeans.modules.cloud.oracle.assets;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.compute.ClusterItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerTagItem;
import org.netbeans.modules.cloud.oracle.steps.ProjectStep;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.RunInClusterAction"
)
@ActionRegistration(
        displayName = "#RunInCluster",
        asynchronous = true
)

@NbBundle.Messages({
    "RunInCluster=Run in OKE Cluster",
    "Deploying=Deploying project \"{0}\" to the cluster \"{1}\""
})
public class RunInClusterAction implements ActionListener {

    private final ContainerTagItem context;
    private static final String APP = "app"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(RunInClusterAction.class);

    public RunInClusterAction(ContainerTagItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RP.post(() -> runInCluster());
    }

    private void runInCluster() {
        ClusterItem cluster = CloudAssets.getDefault().getItem(ClusterItem.class);
        ProgressHandle h;
        String projectName;
        try {
            Project project = Steps.getDefault()
                    .executeMultistep(new ProjectStep(), Lookup.EMPTY)
                    .thenApply(values -> {
                        return values.getValueForStep(ProjectStep.class);
                    })
                    .get();
            ProjectInformation pi = ProjectUtils.getInformation(project);
            projectName = pi.getDisplayName();
            h = ProgressHandle.createHandle(Bundle.Deploying(projectName, cluster.getName()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        try {
            h.start();
            KubernetesUtils.runWithClient(cluster, client -> {
                Deployment existingDeployment = null;
                DeploymentList dList = client.apps().deployments().list();
                for (Deployment deployment : dList.getItems()) {
                    if (projectName.equals(deployment.getMetadata().getName())) {
                        existingDeployment = deployment;
                    }
                }
                if (existingDeployment != null) {
                    client.apps()
                            .deployments()
                            .inNamespace(cluster.getNamespace())
                            .withName(projectName)
                            .edit(d -> new DeploymentBuilder(d)
                            .editSpec()
                            .editTemplate()
                            .editSpec()
                            .editFirstContainer()
                            .withImage(context.getUrl()) // New image version
                            .endContainer()
                            .endSpec()
                            .endTemplate()
                            .endSpec()
                            .build());

                    client.apps()
                            .deployments()
                            .inNamespace(cluster.getNamespace())
                            .withName(projectName)
                            .edit(d -> new DeploymentBuilder(d)
                            .editSpec()
                            .editTemplate()
                            .editMetadata()
                            .addToAnnotations("kubectl.kubernetes.io/restartedAt", Instant.now().toString()) //NOI18N
                            .endMetadata()
                            .endTemplate()
                            .endSpec()
                            .build());

                } else {
                    Deployment newDeployment = new DeploymentBuilder()
                            .withNewMetadata()
                            .withName(projectName)
                            .addToLabels(APP, projectName)
                            .endMetadata()
                            .withNewSpec()
                            .withReplicas(3)
                            .withNewSelector()
                            .addToMatchLabels(APP, projectName)
                            .endSelector()
                            .withNewTemplate()
                            .withNewMetadata()
                            .addToLabels(APP, projectName)
                            .endMetadata()
                            .withNewSpec()
                            .addNewImagePullSecret().withName("docker-bearer-vscode-generated-ocirsecret").endImagePullSecret() //NOI18N
                            .addNewContainer()
                            .withName(projectName)
                            .withImage(context.getUrl())
                            .addNewPort()
                            .withContainerPort(8080)
                            .endPort()
                            .endContainer()
                            .endSpec()
                            .endTemplate()
                            .endSpec()
                            .build();

                    client.apps()
                            .deployments()
                            .inNamespace(cluster.getNamespace())
                            .resource(newDeployment)
                            .create();
                }
            });
        } finally {
            h.finish();
        }
    }

}
