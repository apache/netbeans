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

package org.netbeans.installer.utils.helper;

import org.netbeans.installer.product.components.Product;

/**
 *
 * @author Kirill Sorokin
 */

public abstract class Dependency {    
    private String uid;
    private Version versionLower;
    private Version versionUpper;
    private Version versionResolved;
    
    protected Dependency(
            final String uid,
            final Version versionLower,
            final Version versionUpper,
            final Version versionResolved) {
        this.uid              = uid;
        this.versionLower     = versionLower;
        this.versionUpper     = versionUpper;
        this.versionResolved = versionResolved;
    }
   
    public String getUid() {
        return uid;
    }
    
    public Version getVersionLower() {
        return versionLower;
    }
    
    public Version getVersionUpper() {
        return versionUpper;
    }
    
    public Version getVersionResolved() {
        return versionResolved;
    }
    
    public void setVersionResolved(final Version version) {
        this.versionResolved = version;
    }
    
    public abstract String getName();
    
    public abstract boolean satisfies(Product product);    
    
}
