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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.cloud.oracle.assets.PropertiesGenerator;

/**
 *
 * @author Dusan Petrovic
 */
public class ConfigMapProvider {
    
    private static final String APP = "app"; //NOI18N
    public static final String VOLUME_MOUNT_PATH = "/etc/conf"; //NOI18N
    public static final String ENVIRONMENT = "oraclecloud"; //NOI18N
    public static final String CONFIG_VOLUME_NAME = "configuration";
    public static final String BOOTSTRAP_PROPERTIES_FILE = "bootstrap-" + ENVIRONMENT + ".properties"; //NOI18N
    public static final String APPLICATION_PROPERTIES_FILE = "application-" + ENVIRONMENT + ".properties"; //NOI18N
    
    private final String projectName;
    private final ClusterItem cluster;
    private final PropertiesGenerator propertiesGenerator;

    public ConfigMapProvider(String projectName, ClusterItem cluster) {
        this.projectName = projectName;
        this.cluster = cluster;
        this.propertiesGenerator = new PropertiesGenerator(false);
    }

    public String getMicronautConfigFiles() {
        return new StringBuilder(VOLUME_MOUNT_PATH)
                .append("/")
                .append(APPLICATION_PROPERTIES_FILE)
                .append(",")
                .append(VOLUME_MOUNT_PATH)
                .append("/")
                .append(BOOTSTRAP_PROPERTIES_FILE)
                .toString();
    }
    
    public void createConfigMap() {
        KubernetesUtils.runWithClient(cluster, client -> {
            ConfigMapList cmList = client.configMaps().inNamespace(cluster.getNamespace()).list();
            ConfigMap configMap = (ConfigMap) KubernetesUtils.findResource(client, cmList, projectName);
            if (configMap != null) {
                updateConfigMap(client);
                return;
            }
            createConfigMap(client);   
        });
    }

    public ConfigMapVolumeSource getVolumeSource() {
        return new ConfigMapVolumeSourceBuilder()
            .withName(projectName)
            .addNewItem()
            .withKey(APPLICATION_PROPERTIES_FILE)
            .withPath(APPLICATION_PROPERTIES_FILE)
            .endItem()
            .addNewItem()
            .withKey(BOOTSTRAP_PROPERTIES_FILE)
            .withPath(BOOTSTRAP_PROPERTIES_FILE)
            .endItem()
            .build();
    }

    private void updateConfigMap(KubernetesClient client) {
        Map<String, String> applicationProperties = propertiesGenerator.getApplication();
        Map<String, String> bootstrapProperties = propertiesGenerator.getBootstrap();
        
        client.configMaps()
                .inNamespace(cluster.getNamespace())
                .withName(projectName)
                .edit(cm -> new ConfigMapBuilder(cm)
                .removeFromData(APPLICATION_PROPERTIES_FILE)
                .removeFromData(BOOTSTRAP_PROPERTIES_FILE)
                .addToData(APPLICATION_PROPERTIES_FILE, toFileLikeKeys(applicationProperties))
                .addToData(BOOTSTRAP_PROPERTIES_FILE, toFileLikeKeys(bootstrapProperties))
                .build());
    }

    private ConfigMap createConfigMap(KubernetesClient client) {
        Map<String, String> applicationProperties = propertiesGenerator.getApplication();
        Map<String, String> bootstrapProperties = propertiesGenerator.getBootstrap();
        
        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(projectName)
                .addToLabels(APP, projectName)
                .endMetadata()
                .addToData(APPLICATION_PROPERTIES_FILE, toFileLikeKeys(applicationProperties))
                .addToData(BOOTSTRAP_PROPERTIES_FILE, toFileLikeKeys(bootstrapProperties))
                .build();
                
        return client.configMaps()
                .inNamespace(cluster.getNamespace())
                .resource(configMap)
                .create();
    }
    
    private String toFileLikeKeys(Map<String, String> properties) {
        StringBuilder res = new StringBuilder("");
        TreeMap<String, String> sortedProperties = new TreeMap<>(properties);
        for (Map.Entry<String, String> entry : sortedProperties.entrySet()) {
            res.append(entry.getKey())
                    .append("=") //NOI18N
                    .append(entry.getValue())
                    .append("\n");//NOI18N
        }
        return res.toString();
    }
}
