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

import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccountList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.lookup.ServiceProvider;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobList;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingList;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleList;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.netbeans.modules.cloud.oracle.NotificationUtils;
import org.netbeans.modules.cloud.oracle.compute.ClusterItem;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Petrovic
 */
@NbBundle.Messages({
    "CronJobCreationError=Error while creating secret rotation CronJob"
})
@ServiceProvider(service = CommandProvider.class)
public class CreateSecretRotationCronJobCommand implements CommandProvider {

    private static final String COMMAND_CREATE_CRONJOB = "nbls.cloud.assets.cluster.cronjob.create"; //NOI18N
    private static final String SECRET_NAME = "docker-bearer-vscode-generated-ocirsecret"; //NOI18N
    private static final String CRONJOB_NAME = "secret-rotation-cronjob"; //NOI18N
    private static final String CLUSTER_ROLE_BINDING_NAME = "secret-manager-binding"; //NOI18N
    private static final String CLUSTER_ROLE_NAME = "secret-manager"; //NOI18N
    private static final String SERVICE_ACCOUNT_NAME = "create-secret-svc-account"; //NOI18N
    private static final String BASE_IMAGE = "ghcr.io/oracle/oci-cli:latest"; //NOI18N
    private static final String CONTAINER_NAME = "create-secret"; //NOI18N
    private static final int WAITING_TIMEOUT = 60;

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_CREATE_CRONJOB
    ));
    
    private ClusterItem cluster;
    
    @Override
    public Set<String> getCommands() {
        return Collections.unmodifiableSet(COMMANDS);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        return createSecretRotationCronJob();
    }

    public CompletableFuture<Object> createSecretRotationCronJob() {
        CompletableFuture completableFuture = new CompletableFuture();
        this.cluster = CloudAssets.getDefault().getItem(ClusterItem.class);
        KubernetesUtils.runWithClient(cluster, client -> {
            try {
                ServiceAccount serviceAccount = createServiceAccountIfNotExist(client);
                createClusterRoleIfNotExist(client);
                createClusterRoleBindingIfNotExist(client);
                createCronJobIfNotExist(client, serviceAccount);
                completableFuture.complete(null);
            } catch(Exception ex) {
                completableFuture.completeExceptionally(ex);
                NotificationUtils.showErrorMessage(Bundle.CronJobCreationError());
            }
        });
        return completableFuture;
    }

    private void createCronJobIfNotExist(KubernetesClient client, ServiceAccount serviceAccount) {
        CronJobList existingCronJobs = client.batch().v1().cronjobs().inNamespace(cluster.getNamespace()).list();
        CronJob cronJob = (CronJob) KubernetesUtils.findResource(client, existingCronJobs, CRONJOB_NAME);
        if (cronJob != null) {
            if (!secretExist(client)) {
                invokeCronJob(client, cronJob);  
            }
            return;
        }
        cronJob = new CronJobBuilder()
                .withNewMetadata()
                .withName(CRONJOB_NAME)
                .withNamespace(cluster.getNamespace())
                .endMetadata()
                .withNewSpec()
                .withSchedule(getCronExpression())
                .withNewJobTemplate()
                .withNewSpec()
                .withBackoffLimit(0)
                .withTemplate(cronJobPodTemplate(serviceAccount))
                .endSpec()
                .endJobTemplate()
                .endSpec()
                .build();
                
        client.batch().v1()
               .cronjobs()
               .inNamespace(cluster.getNamespace())
               .resource(cronJob)
               .create();
        
        invokeCronJob(client, cronJob);
    }
   
    private boolean secretExist(KubernetesClient client) {
        SecretList existingSecrets = client.secrets().inNamespace(cluster.getNamespace()).list();
        Secret secret = (Secret) KubernetesUtils.findResource(client, existingSecrets, SECRET_NAME);
        return secret != null;
    }
    
    private void invokeCronJob(KubernetesClient client, CronJob cronJob) {
        client.batch().v1()
                .jobs()
                .inNamespace(cluster.getNamespace())
                .resource(new JobBuilder()
                        .withNewMetadata()
                        .withName("cronjob-invocation-" + UUID.randomUUID()) //NOI18N
                        .endMetadata()
                        .withSpec(cronJob.getSpec().getJobTemplate().getSpec())
                        .build())
                .create();
        
        waitForConditionWithTimeout(() -> {
            return secretExist(client);
        }, WAITING_TIMEOUT).join();
    }
    
    private CompletableFuture<Void> waitForConditionWithTimeout(Supplier<Boolean> condition, long timeout) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        ScheduledFuture<?> checkTask = executor.scheduleAtFixedRate(() -> {
            if (condition.get()) {
                future.complete(null);
            }
        }, 0, 5, TimeUnit.SECONDS);

        executor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Condition was not met within the timeout.")); //NOI18N
            }
            checkTask.cancel(true);
            executor.shutdown();
        }, timeout, TimeUnit.SECONDS);

        return future;
    }

    private String getCronExpression() {
        Calendar calendar = Calendar.getInstance();
        int currentMinute = calendar.get(Calendar.MINUTE);        
        return currentMinute + " * * * *";
    }
    
    private PodTemplateSpec cronJobPodTemplate(ServiceAccount serviceAccount) {
        return new PodTemplateSpecBuilder()
                .withNewSpec()
                .withHostNetwork(Boolean.TRUE)
                .addNewContainer()
                .withName(CONTAINER_NAME)
                .withImage(BASE_IMAGE)
                .addNewEnv()
                .withName("OCI_CLI_AUTH") //NOI18N
                .withValue("instance_principal") //NOI18N
                .endEnv()
                .withCommand("/bin/bash", "-c", createSecretCommand()) //NOI18N
                .endContainer()
                .withRestartPolicy("Never") //NOI18N
                .withServiceAccountName(serviceAccount.getMetadata().getName())
                .endSpec()
                .build();
    }

    private ServiceAccount createServiceAccountIfNotExist(KubernetesClient client) {
        ServiceAccountList existingServiceAccounts = client.serviceAccounts().inNamespace(cluster.getNamespace()).list();
        ServiceAccount serviceAccount = (ServiceAccount) KubernetesUtils.findResource(client, existingServiceAccounts, SERVICE_ACCOUNT_NAME);
        if (serviceAccount != null) {
            return serviceAccount;
        }
        serviceAccount = new ServiceAccountBuilder()
                .withNewMetadata()
                .withName(SERVICE_ACCOUNT_NAME)
                .endMetadata()
                .build();
        
        return client.serviceAccounts()
                .inNamespace(cluster.getNamespace())
                .resource(serviceAccount)
                .create();
    }
    
    private void createClusterRoleIfNotExist(KubernetesClient client) {
        ClusterRoleList existingClusterRole = client.rbac().clusterRoles().list();
        ClusterRole clusterRole = (ClusterRole) KubernetesUtils.findResource(client, existingClusterRole, CLUSTER_ROLE_NAME);
        if (clusterRole != null) {
            return;
        }
        clusterRole = new ClusterRoleBuilder()
                .withNewMetadata()
                .withName(CLUSTER_ROLE_NAME)
                .endMetadata()
                .addNewRule()
                .withApiGroups("")
                .withResources("secrets") //NOI18N
                .withVerbs("create", "get", "patch", "delete") //NOI18N
                .endRule()
                .build();
        
        client.rbac().clusterRoles()
                .resource(clusterRole)
                .create();
    }
    
    private void createClusterRoleBindingIfNotExist(KubernetesClient client) {
        ClusterRoleBindingList existingClusterRoleBinding = client.rbac().clusterRoleBindings().list();
        ClusterRoleBinding clusterRoleBinding = (ClusterRoleBinding) KubernetesUtils.findResource(client, existingClusterRoleBinding, CLUSTER_ROLE_BINDING_NAME);
        if (clusterRoleBinding != null) {
            return;
        }
        clusterRoleBinding = new ClusterRoleBindingBuilder()
                .withNewMetadata()
                .withName(CLUSTER_ROLE_BINDING_NAME)
                .endMetadata()
                .addNewSubject()
                .withName(SERVICE_ACCOUNT_NAME)
                .withKind("ServiceAccount") //NOI18N
                .withNamespace(cluster.getNamespace())
                .endSubject()
                .withNewRoleRef()
                .withKind("ClusterRole") //NOI18N
                .withName(CLUSTER_ROLE_NAME)
                .withApiGroup("rbac.authorization.k8s.io") //NOI18N
                .endRoleRef()
                .build();
        
        client.rbac().clusterRoleBindings()
                .resource(clusterRoleBinding)
                .create();
    }
    
    private String createSecretCommand() {
        String repoEndpoint = cluster.getRegionCode() + ".ocir.io"; //NOI18N
        return 
            "KUBECTL_VERSION=\"v1.27.4\"\n" + //NOI18N
            "case \"$(uname -m)\" in\n" + //NOI18N
              "  x86_64) ARCHITECTURE=\"amd64\" ;;\n" + //NOI18N
              "  aarch64) ARCHITECTURE=\"arm64\" ;;\n" + //NOI18N
              "  *) ARCHITECTURE=\"Unknown architecture\" ;;\n" + //NOI18N
             "esac\n" + //NOI18N
            "KUBECTL_URL=\"https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/${ARCHITECTURE}/kubectl\"\n" + //NOI18N
            "mkdir -p /tmp/bin\n" + //NOI18N
            "curl -LO \"${KUBECTL_URL}\"\n" + //NOI18N
            "chmod +x ./kubectl\n" + //NOI18N
            "mv ./kubectl /tmp/bin/kubectl\n" + //NOI18N
            "export PATH=$PATH:/tmp/bin\n" + //NOI18N
            "TOKEN=$(oci raw-request --http-method GET --target-uri https://" + repoEndpoint + "/20180419/docker/token | jq -r '.data.token')\n" + //NOI18N
            "kubectl create secret --save-config --dry-run=client docker-registry " + SECRET_NAME + " --docker-server=" + repoEndpoint + " --docker-username=BEARER_TOKEN --docker-password=\"$TOKEN\" -o yaml | kubectl apply -f - "; //NOI18N
    }

}
