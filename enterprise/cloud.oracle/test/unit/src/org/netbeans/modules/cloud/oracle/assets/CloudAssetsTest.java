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

import java.net.URISyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.cloud.oracle.assets.k8s.ClusterItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryItem;
import org.netbeans.modules.cloud.oracle.items.OCID;

/**
 *
 * @author Jan Horvath
 */
public class CloudAssetsTest {

    @Test
    public void testStoreLoad() throws URISyntaxException {
        CloudAssets instance = new CloudAssets();
        instance.loadAssets();
        instance.addItem(new DatabaseItem(OCID.of("db-ocid", "Database"), "db-comp-id", "DB1", "http://test", "DB1", "tenancy-id", "reg"));
        instance.addItem(new ClusterItem(OCID.of("cluster-ocid", "Cluster"), "cluster-comp-id", "Cluster1", "tenancy-id", "reg"));
        instance.addItem(new ContainerRepositoryItem(OCID.of("container-repo-ocid", "ContainerRepository"), "container-repo-comp-id", "Repo1", "reg", "namespace", true, 2, "tenancy-id"));
        instance.storeAssets();
        
        CloudAssets instance1 = new CloudAssets();
        instance1.loadAssets();
        
        assertEquals(instance.getItems(), instance1.getItems());
    }

    
}
