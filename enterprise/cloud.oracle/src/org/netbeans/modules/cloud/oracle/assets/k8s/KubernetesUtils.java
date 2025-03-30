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
package org.netbeans.modules.cloud.oracle.assets.k8s;

import com.oracle.bmc.Region;
import com.oracle.bmc.http.Priorities;
import com.oracle.bmc.http.client.HttpClient;
import com.oracle.bmc.http.client.HttpRequest;
import com.oracle.bmc.http.client.Method;
import com.oracle.bmc.http.client.jersey.JerseyHttpProvider;
import com.oracle.bmc.http.signing.RequestSigningFilter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 *
 * @author Jan Horvath
 */
public class KubernetesUtils {

    public static void runWithClient(ClusterItem cluster, Consumer<KubernetesClient> consumer) {
        if (cluster.getConfig() == null) {
            cluster.update();
        }
        if (cluster.getConfig() == null) {
            throw new RuntimeException("Invalid cluster configuration");
        }
        Config config = prepareConfig(cluster.getConfig(), cluster);
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();) {
            consumer.accept(client);
        } 
    }

    private static Config prepareConfig(String content, ClusterItem cluster) {
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

    private static String getBearerToken(ClusterItem cluster) {
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
    
    public static KubernetesResource findResource(KubernetesClient client, KubernetesResourceList<? extends HasMetadata> existingResources, String resourceName) {
        if (resourceName == null) return null;
        
        for (HasMetadata resource : existingResources.getItems()) {
            if (resourceName.equals(resource.getMetadata().getName())) {
                return resource;
            }
        }
        return null;
    }
}
