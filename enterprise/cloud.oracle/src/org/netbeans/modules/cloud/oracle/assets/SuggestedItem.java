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
package org.netbeans.modules.cloud.oracle.assets;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "SelectDatabases=Select Oracle Autonomous Database",
    "SelectVault=Select OCI Vault",
    "SelectBucket=Select Object Storage Bucket",
    "SelectCluster=Select Oracle Container Engine",
    "SelectCompute=Select Compute Instance"
})
public final class SuggestedItem extends OCIItem {

    private final String path;
    private final Set<String> exclusivePaths;

    public SuggestedItem(String path, String name, Set<String> exclusivePaths) {
        super(OCID.of("", "Suggested"), null, name); //NOI18N
        this.path = path;
        this.exclusivePaths = exclusivePaths;
    }

    public String getPath() {
        return path;
    }

    public Set<String> getExclusivePaths() {
        return exclusivePaths;
    }

    public static SuggestedItem forPath(String path) {
        switch (path) {
            case "Databases": //NOI18N
                return new SuggestedItem("Databases", Bundle.SelectDatabases(), Collections.emptySet()); //NOI18N
            case "Vault": //NOI18N
                return new SuggestedItem("Vault", Bundle.SelectVault(), Collections.emptySet()); //NOI18N
            case "Bucket": //NOI18N
                return new SuggestedItem("Bucket", Bundle.SelectBucket(), Collections.emptySet()); //NOI18N
            case "Cluster": //NOI18N
                return new SuggestedItem("Cluster", Bundle.SelectCluster(), Collections.singleton("ComputeInstance")); //NOI18N
            case "ComputeInstance": //NOI18N
                return new SuggestedItem("ComputeInstance", Bundle.SelectCompute(), Collections.singleton("Cluster")); //NOI18N
            default:
                throw new IllegalArgumentException("");
        }
    }
}
