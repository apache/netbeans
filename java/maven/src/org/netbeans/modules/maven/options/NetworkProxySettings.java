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
package org.netbeans.modules.maven.options;

import org.netbeans.modules.maven.api.execute.RunUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "PROXY_IGNORE=Do not check",
    "PROXY_NOTICE=Display Mismatch Notice",
    "PROXY_UPDATE=Update User Properties",
    "PROXY_ASK=Ask Before Execution",
    "PROXY_OVERRIDE=Override on execution",
})
public enum NetworkProxySettings {
    /**
     * Do not verify proxy settings.
     */
    IGNORE(Bundle.PROXY_IGNORE()),
    /**
     * Display a notice that proxy settings mismatch.
     */
    NOTICE(Bundle.PROXY_NOTICE()),
    /**
     * Update user's gradle.properties file.
     */
    UPDATE(Bundle.PROXY_UPDATE()),
    /**
     * Ask the user for confirmation.
     */
    ASK(Bundle.PROXY_ASK()),
    /**
     * Automatically override on execution, but do not change gradle.properties.
     */
    OVERRIDE(Bundle.PROXY_OVERRIDE());
    
    private String displayName;
    
    public String toString() {
        return displayName;
    }

    private static final String BRANDING_API_OVERRIDE_ENABLED = "org.netbeans.modules.maven.api.execute.NetworkProxySettings.allowOverride"; // NOI18N
    
    private NetworkProxySettings(String dispName) {
        this.displayName = dispName;
    }
    
    /**
     * Determines if override is a valid option.
     * @return true, if override should be offered as an option
     */
    public static boolean allowProxyOverride() {
        return Boolean.parseBoolean(NbBundle.getMessage(RunUtils.class, BRANDING_API_OVERRIDE_ENABLED));
    }
}
