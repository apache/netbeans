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

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.ListBucketsRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "BcuketDesc=Bucket: {0}"
})
public class BucketNode extends OCINode {

    private static final String BUCKET_ICON = "org/netbeans/modules/cloud/oracle/resources/bucket.svg"; // NOI18N

    public BucketNode(BucketItem bucket) {
        super(bucket, Children.LEAF);
        setName(bucket.getName());
        setDisplayName(bucket.getName());
        setIconBaseWithExtension(BUCKET_ICON);
        setShortDescription(Bundle.BcuketDesc(bucket.getName()));
    }

    public static NodeProvider<BucketItem> createNode() {
        return BucketNode::new;
    }

    /**
     * Retrieves list of Vaults belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of
     * {@code BucketItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider.SessionAware<CompartmentItem, BucketItem> getBuckets() {
        return (compartmentId, session) -> {
            ObjectStorageClient client = session.newClient(ObjectStorageClient.class);

            ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .namespaceName(session.getTenancy().get().getName())
                    .limit(88)
                    .build();

            List<BucketItem> x = client.listBuckets(listBucketsRequest)
                    .getItems()
                    .stream()
                    .map(d -> new BucketItem(
                            OCID.of(d.getName(), "Bucket"), //NOI18N
                            compartmentId.getKey().getValue(),
                            d.getName(),
                            d.getNamespace())
                    )
                    .collect(Collectors.toList());
            return x;
        };
    }

}
