/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.product.filters;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.Feature;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Version;

/**
 *
 * @author Kirill Sorokin
 */
public class ProductFilter implements RegistryFilter {
    private String uid;
    
    private List<Platform> platforms;
    
    private Version versionLower;
    private Version versionUpper;
    
    private Status status;
    private DetailedStatus detailedStatus;
    
    private Feature feature;
    
    private Boolean visible;
    
    public ProductFilter() {
        this.platforms = new LinkedList<Platform>();
    }
    
    public ProductFilter(final boolean visible) {
        this();
        
        this.visible = visible;
    }
    
    public ProductFilter(final Platform platform) {
        this();
        
        this.platforms.add(platform);
    }
    
    public ProductFilter(final String uid, final Platform platform) {
        this();
        
        this.uid = uid;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final String uid, final Version version, final Platform platform) {
        this();
        
        this.uid = uid;
        this.versionLower = version;
        this.versionUpper = version;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final String uid, final Version version, final List<Platform> platforms) {
        this();
        
        this.uid = uid;
        this.versionLower = version;
        this.versionUpper = version;
        this.platforms.addAll(platforms);
    }
    
    public ProductFilter(final String uid, final Version versionLower, final Version versionUpper, final Platform platform) {
        this();
        
        this.uid          = uid;
        this.versionLower = versionLower;
        this.versionUpper = versionUpper;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final Status status) {
        this(status, Registry.getInstance().getTargetPlatform());
    }
    
    public ProductFilter(final Status status, final Platform platform) {
        this();
        
        this.status = status;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final DetailedStatus detailedStatus) {
        this(detailedStatus, Registry.getInstance().getTargetPlatform());
    }
    
    public ProductFilter(final DetailedStatus detailedStatus, final Platform platform) {
        this();
        
        this.detailedStatus = detailedStatus;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final Feature feature, final Platform platform) {
        this();
        
        this.feature = feature;
        this.platforms.add(platform);
    }
    
    public boolean accept(final RegistryNode node) {
        if (node instanceof Product) {
            final Product product = (Product) node;
            
            if (uid != null) {
                if (!product.getUid().equals(uid)) {
                    return false;
                }
            }
            
            if ((versionLower != null) && (versionUpper != null)) {
                if (!product.getVersion().newerOrEquals(versionLower) ||
                        !product.getVersion().olderOrEquals(versionUpper)) {
                    return false;
                }
            }
            
            if (platforms.size() > 0) {
                boolean intersects = false;
                
                for (Platform platform: platforms) {
                    for (Platform productPlatform: product.getPlatforms()) {
                        if (platform.isCompatibleWith(productPlatform) || 
                                productPlatform.isCompatibleWith(platform)) {
                            intersects = true;
                        }
                    }
                    
                    if (intersects) break;
                }
                
                if (!intersects) return false;
            }
            
            if (status != null) {
                if (product.getStatus() != status) {
                    return false;
                }
            }
            
            if (detailedStatus != null) {
                if (product.getDetailedStatus() != detailedStatus) {
                    return false;
                }
            }
            
            if (feature != null) {
                for (String id: product.getFeatures()) {
                    if (feature.getId().equals(id)) {
                        return false;
                    }
                }
            }
            
            if (visible != null) {
                if (product.isVisible() != visible.booleanValue()) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
}
