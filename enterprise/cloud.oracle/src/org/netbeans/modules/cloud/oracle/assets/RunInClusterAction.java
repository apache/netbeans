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

import com.oracle.bmc.Region;
import com.oracle.bmc.http.Priorities;
import com.oracle.bmc.http.client.HttpClient;
import com.oracle.bmc.http.client.HttpRequest;
import com.oracle.bmc.http.client.Method;
import com.oracle.bmc.http.client.jersey.JerseyHttpProvider;
import com.oracle.bmc.http.signing.RequestSigningFilter;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.compute.ClusterItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerTagItem;
import org.netbeans.modules.cloud.oracle.steps.ProjectStep;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

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
    private static final String DEFAULT = "default"; //NOI18N
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
        if (cluster.getConfig() == null) {
            cluster.update();
        }
        if (cluster.getConfig() == null) {
            throw new RuntimeException("Invalid cluster configuration");
        }
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
            runWithClient(cluster, client -> {
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
                            .inNamespace(DEFAULT)
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
                            .inNamespace(DEFAULT)
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
                            .inNamespace(DEFAULT)
                            .resource(newDeployment)
                            .create();
                }
            });
        } finally {
            h.finish();
        }
    }

    private void runWithClient(ClusterItem cluster, Consumer<KubernetesClient> consumer) {
        Config config = prepareConfig(cluster.getConfig(), cluster);
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();) {
            consumer.accept(client);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private Config prepareConfig(String content, ClusterItem cluster) {
        String token = getBearerToken(cluster);
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        Map<String, Object> data = (Map<String, Object>) load.loadFromString(content);

        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users"); //NOI18N
        if (users == null) {
            throw new RuntimeException("Invalid cluster configuration");
        }
        Map exec = null;
        for (Map<String, Object> userEntry : users) {
            Map<String, Object> user = (Map<String, Object>) userEntry.get("user"); //NOI18N
            if (user != null && user.containsKey("exec")) { //NOI18N
                exec = (Map) user.remove("exec"); //NOI18N
            }
        }
        String clusterId = null;
        if (exec != null && "oci".equals(exec.get("command"))) { //NOI18N
            List commandArgs = (List) exec.get("args"); //NOI18N
            boolean clusterIdNext = false;
            for (Object arg : commandArgs) {
                if ("--cluster-id".equals(arg)) {
                    clusterIdNext = true;
                    continue;
                }
                if (clusterIdNext) {
                    clusterId = (String) arg;
                    break;
                }

            }
        }
        if (!cluster.getKey().getValue().equals(clusterId)) {
            throw new RuntimeException("Failed to read cluster config"); //NOI18N
        }
        Dump dump = new Dump(DumpSettings.builder().build());
        String noExec = dump.dumpToString(data);
        Config config = Config.fromKubeconfig(noExec);
        config.setOauthToken(token);
        return config;
    }

    private String getBearerToken(ClusterItem cluster) {
        try {
            OCIProfile profile = OCIManager.getDefault().getActiveProfile(cluster);
            Region region = Region.fromRegionCodeOrId(cluster.getRegionCode());
            URI uri = new URI(String.format(
                    "https://containerengine.%s.oraclecloud.com/cluster_request/%s", //NOI18N
                    region.getRegionId(), cluster.getKey().getValue()
            ));

            RequestSigningFilter requestSigningFilter
                    = RequestSigningFilter.fromAuthProvider(profile.getAuthenticationProvider());

            HttpClient client = JerseyHttpProvider.getInstance().newBuilder()
                    .registerRequestInterceptor(Priorities.AUTHENTICATION, requestSigningFilter)
                    .baseUri(uri).build();

            HttpRequest request = client.createRequest(Method.GET);

            request.execute().toCompletableFuture();
            List<String> tokenUrlList = request.headers().get("authorization"); //NOI18N
            String authorization = URLEncoder.encode(tokenUrlList.get(0), "UTF-8"); //NOI18N
            List<String> dateList = request.headers().get("date"); //NOI18N
            String date = URLEncoder.encode(dateList.get(0), "UTF-8"); //NOI18N
            String baseString = String.format("%s?authorization=%s&date=%s", uri.toASCIIString(), authorization, date); //NOI18N
            byte[] urlBytes = baseString.getBytes(StandardCharsets.UTF_8);
            return Base64.getUrlEncoder().encodeToString(urlBytes);
        } catch (URISyntaxException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
