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

import com.oracle.bmc.containerengine.ContainerEngineClient;
import com.oracle.bmc.containerengine.requests.CreateKubeconfigRequest;
import com.oracle.bmc.containerengine.responses.CreateKubeconfigResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.adm.URLProvider;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
public final class ClusterItem extends OCIItem implements URLProvider{
    private String config = null;
    private String namespace = "default"; //NOI8N

    public ClusterItem(OCID id, String compartmentId, String name, String tenancyId, String regionCode) {
        super(id, compartmentId, name, tenancyId, regionCode);
    }

    public ClusterItem() {
        super();
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        String oldNamespace = this.namespace;
        this.namespace = namespace;
        firePropertyChange("namespace", oldNamespace, namespace); //NOI18N
    }
    
    public void update() {
        OCIProfile profile = OCIManager.getDefault().getActiveProfile(this);
        ContainerEngineClient containerEngineClient = profile.newClient(ContainerEngineClient.class);

        
        CreateKubeconfigRequest request = CreateKubeconfigRequest.builder()
                .clusterId(getKey().getValue())
                .build();
        
        CreateKubeconfigResponse response = containerEngineClient.createKubeconfig(request);
        
        try {
            setConfig(new String(response.getInputStream().readAllBytes(), Charset.defaultCharset()));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public URL getURL() {
        if (getKey().getValue() != null && getRegion() != null) {
            try {
                URI uri = new URI(String.format("https://cloud.oracle.com/containers/clusters/%s?region=%s", getKey().getValue(), getRegion()));
                return uri.toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        } 
        return null;
    }
    
}
