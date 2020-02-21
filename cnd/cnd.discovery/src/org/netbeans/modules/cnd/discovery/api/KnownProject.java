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

package org.netbeans.modules.cnd.discovery.api;

import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class KnownProject {
    /** well known project name */
    public static final String PROJECT = "project"; // NOI18N
    /** path to well known project sources */
    public static final String ROOT = "root"; // NOI18N
    /** path to created netbeans projects */
    public static final String NB_ROOT = "netbeans-project"; // NOI18N

    private static final Default DEFAULT = new Default();

    public abstract boolean canCreate(Map<String,String> parameters);
    public abstract boolean create(Map<String,String> parameters);

    protected KnownProject() {
    }
    
    /**
     * Static method to obtain the CsmSelect implementation.
     * @return the selector
     */
    public static synchronized KnownProject find(Map<String, String> parameters)  {
        return DEFAULT.find(parameters);
    }
    
    /**
     * Implementation of the default creator
     */  
    private static final class Default {
        private final Lookup.Result<KnownProject> res;
        Default() {
            res = Lookup.getDefault().lookupResult(KnownProject.class);
        }

        private KnownProject find(Map<String, String> parameters) {
            for (KnownProject creator : res.allInstances()) {
                if (creator.canCreate(parameters)){
                    return creator;
                }
            }
            return null;
        }
    }
}
