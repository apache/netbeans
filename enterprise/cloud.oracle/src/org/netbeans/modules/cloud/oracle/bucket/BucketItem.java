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
package org.netbeans.modules.cloud.oracle.bucket;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.cloud.oracle.adm.URLProvider;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
public final class BucketItem extends OCIItem implements URLProvider {

    private String namespace;

    public BucketItem(OCID id, String compartmentId, String name, String namespace, String tenancyId, String regionCode) {
        super(id, compartmentId, name, tenancyId, regionCode);
        this.namespace = namespace;
    }

    public BucketItem() {
        super();
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public int maxInProject() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public URL getURL() {
        if (getKey().getValue() != null && getRegion() != null) {
            try {
                URI uri = new URI(String.format("https://cloud.oracle.com/object-storage/buckets/%s/%s/objects?region=%s",
                                    getNamespace(), getName(), getRegion()));
                return uri.toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        } 
        return null;
    }
    
}
