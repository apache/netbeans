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

package org.netbeans.modules.j2ee.api.ejbjar;

/**
 *
 * @author Martin Adamek
 */
public final class ResourceReference {

    private final String resRefName;
    private final String resType;
    private final String resAuth;
    private final String resSharingScope;
    private final String defaultDescription;
    
    private ResourceReference(String resRefName, String resType, String resAuth, String resSharingScope, String defaultDescription) {
        this.resRefName = resRefName;
        this.resType = resType;
        this.resAuth = resAuth;
        this.resSharingScope = resSharingScope;
        this.defaultDescription = defaultDescription;
    }
    
    public static ResourceReference create(String resRefName, String resType, String resAuth, String resSharingScope, String defaultDescription) {
        return new ResourceReference(resRefName, resType, resAuth, resSharingScope, defaultDescription);
    }
    
    public String getResRefName() {
        return resRefName;
    }

    public String getResType() {
        return resType;
    }

    public String getResAuth() {
        return resAuth;
    } 
    
    public String getResSharingScope() {
        return resSharingScope;
    }
    
    public String getDefaultDescription() {
        return defaultDescription;
    }
    
}
