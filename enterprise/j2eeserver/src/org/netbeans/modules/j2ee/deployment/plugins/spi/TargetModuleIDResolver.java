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

/**
 *
 * AutoUndeploySupport.java
 *
 * Created on February 12, 2004, 3:57 PM
 * @author  nn136682
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;

/**
 * Service provided by plugin for lookup TargetModuleID.
 * This service basically help J2EE framework identify the target modules
 * needs to be undeployed before a safe full deployment can happen.
 */
public abstract class TargetModuleIDResolver {

    public static final String KEY_CONTEXT_ROOT = "contextRoot";
    public static final String KEY_CONTENT_DIR = "contentDirs";
    public static final TargetModuleID[] EMPTY_TMID_ARRAY = new TargetModuleID[0];
    private static String[] lookupKeys = null;

    public final String[] getLookupKeys() {
        if (lookupKeys == null) {
            lookupKeys = new String[] {
                KEY_CONTEXT_ROOT, KEY_CONTENT_DIR
            };
        }
        return lookupKeys;
    }

    /**
     * Return the list of TargetModuleIDs that could match the given lookup info.
     * @param targetModuleInfo lookup info, keyed by list returned by #getLookupKeys
     * @return array of root TargetModuleIDs. 
     */
    public abstract TargetModuleID[] lookupTargetModuleID(java.util.Map targetModuleInfo, Target[] targetList);
}
