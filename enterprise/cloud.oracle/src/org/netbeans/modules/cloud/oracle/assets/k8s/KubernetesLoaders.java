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

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "ForwardingPorts=Forwarding http://localhost:{0} to a pod {1}"

})
public class KubernetesLoaders {

    public static List<PodItem> loadPods(ClusterItem cluster, List<String> deploymentNames) {
        final List<PodItem> result = new ArrayList<>();
        KubernetesUtils.runWithClient(cluster, client -> {
            for (String name : deploymentNames) {
                Deployment deployment = client
                        .apps()
                        .deployments()
                        .inNamespace(cluster.getNamespace())
                        .withName(name)
                        .get();
                if (deployment == null) {
                    continue;
                }

                var labelSelector = deployment
                        .getSpec()
                        .getSelector()
                        .getMatchLabels();

                PodList podList = client.pods()
                        .inNamespace(cluster.getNamespace())
                        .withLabels(labelSelector)
                        .list();
                for (Pod pod : podList.getItems()) {
                    result.add(new PodItem(cluster,
                            pod.getMetadata().getNamespace(),
                            pod.getMetadata().getName()));
                }
            }
        });
        return result;
    }

}
