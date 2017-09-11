/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
