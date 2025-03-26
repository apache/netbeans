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
package org.netbeans.modules.cloud.oracle.compute;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.cloud.oracle.adm.URLProvider;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.modules.cloud.oracle.assets.Validator;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "SuggestAmpere=The Compute Instance has a different architecture than the local machine. Container images built on the local machine may not be compatible with this Compute Instance. Please consider creating a Compute Instance with an Ampere® shape.",
    "SuggestIntel=The Compute Instance has a different architecture than the local machine. Container images built on the local machine may not be compatible with this Compute Instance. Please consider creating a Compute Instance with an AMD or Intel® shape."
})
public final class ComputeInstanceItem extends OCIItem implements URLProvider, Validator {
    private final String AARCH = "aarch64";
    private String publicIp = null;
    private String processorDescription;
    private String username;
    private String imageId;
    
    public ComputeInstanceItem(OCID id, String compartmentId, String name, String processorDescription, String imageId, String publicIp, String username, String tenancyId, String regionCode) {
        super(id, compartmentId, name, tenancyId, regionCode);
        this.processorDescription = processorDescription;
        this.publicIp = publicIp;
        this.username = username;
        this.imageId = imageId;
    }

    public ComputeInstanceItem() {
        super();
    }

    public String getPublicIp() {
        if (publicIp == null) {
            return "-"; //NOI18N
        }
        return publicIp;
    } 
    
    public String getProcessorDescription() {
        return processorDescription;
    }

    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }
    
    public String getImageId() {
        return imageId;
    }

    void setPublicId(String publicIp) {
        this.publicIp = publicIp;
    }
    
    @Override
    public URL getURL() {
        if (getKey().getValue() != null && getRegion() != null) {
            try {
                URI uri = new URI(String.format("https://cloud.oracle.com/compute/instances/%s?region=%s", getKey().getValue(), getRegion()));
                return uri.toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        } 
        return null;
    }

    @Override
    public Result validate() {
        String osArch = System.getProperty("os.arch"); //NOI18N
        String shapeDesc = getProcessorDescription();
        boolean os_arm = AARCH.equals(osArch);
        boolean shape_arm = shapeDesc != null && shapeDesc.contains("Ampere"); //NOI18N
        if (os_arm == shape_arm) {
            return new Result(ValidationStatus.OK, null);
        } else if (os_arm && !shape_arm) {
            return new Result(ValidationStatus.WARNING, Bundle.SuggestAmpere());
        } 
        return new Result(ValidationStatus.WARNING, Bundle.SuggestIntel());
    }

}
