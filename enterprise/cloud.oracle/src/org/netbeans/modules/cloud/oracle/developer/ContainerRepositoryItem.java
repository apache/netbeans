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
package org.netbeans.modules.cloud.oracle.developer;

import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;

/**
 *
 * @author Jan Horvath
 */
public final class ContainerRepositoryItem extends OCIItem {

    private Boolean isPublic;
    private Integer imageCount;
    private String namespace;

    public ContainerRepositoryItem(OCID id, String compartmentId, String name, String regionCode, String namespace, Boolean isPublic, Integer imageCount, String tenancyId) {
        super(id, compartmentId, name, tenancyId, regionCode);
        this.namespace = namespace;
        this.isPublic = isPublic;
        this.imageCount = imageCount;
    }

    public ContainerRepositoryItem() {
        super();
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    public String getNamespace() {
        return namespace;
    }
    
    public String getUrl() {
        return String.format("%s.ocir.io/%s/%s", getRegionCode(), getNamespace(), getName());
    }
    
    public String getRegistry() {
        return String.format("%s.ocir.io", getRegionCode());
    }
    
}
