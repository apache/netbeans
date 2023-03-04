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
package org.netbeans.modules.cloud.oracle.adm;

import com.oracle.bmc.adm.ApplicationDependencyManagementClient;
import com.oracle.bmc.adm.model.KnowledgeBaseSummary;
import com.oracle.bmc.adm.requests.ListKnowledgeBasesRequest;
import com.oracle.bmc.adm.responses.ListKnowledgeBasesResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.Children;

/**
 *
 * @author Jan Horvath
 */
public class KnowledgeBaseNode extends OCINode {

    private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/knowledge_base.svg"; // NOI18N

    public KnowledgeBaseNode(OCIItem item) {
        super(item, Children.LEAF);
        setIconBaseWithExtension(ICON);
    }

    public static NodeProvider<KnowledgeBaseItem> createNode() {
        return KnowledgeBaseNode::new;
    }

//    @ItemLoader.Registration(path = "Oracle/KnowledgeBase")
//    public static class KnowledgeBaseLoader implements ItemLoader<OCID> {
//
//        @Override
//        public OCIItem loadItem(OCID key) {
//            try ( ApplicationDependencyManagementClient client 
//                    = new ApplicationDependencyManagementClient(OCIManager.getDefault().getConfigProvider())) {
//                
//                GetKnowledgeBaseRequest request = GetKnowledgeBaseRequest.builder()
//                        .knowledgeBaseId(key.getValue())
//                        .build();
//                GetKnowledgeBaseResponse response = client.getKnowledgeBase(request);
//                KnowledgeBase knowledgeBase = response.getKnowledgeBase();
//                return new KnowledgeBaseItem(key, knowledgeBase.getDisplayName());
//            } catch(BmcException e) {
//                Exceptions.printStackTrace(e);
//            }
//            return null;
//        }
//
//        @Override
//        public OCID fromPersistentForm(String persistedKey) {
//            return OCID.of("Oracle/KnowledgeBase", persistedKey);
//        }
//        
//    }
    
//    @ChildrenProvider.Registration(parentPath = "Oracle/Compartment")
    public static ChildrenProvider.SessionAware<CompartmentItem, KnowledgeBaseItem> listKnowledgeBases() {
        return (compartment, session) -> {
            try ( ApplicationDependencyManagementClient client 
                    = session.newClient(ApplicationDependencyManagementClient.class)) {
                
                ListKnowledgeBasesRequest request = ListKnowledgeBasesRequest.builder()
                        .compartmentId(compartment.getKey().getValue()).build();
                ListKnowledgeBasesResponse response = client.listKnowledgeBases(request);
                List<KnowledgeBaseSummary> projects = response.getKnowledgeBaseCollection().getItems();
                return projects.stream().map(p -> new KnowledgeBaseItem(
                        OCID.of(p.getId(), "KnowledgeBase"), // NOI18N 
                        p.getCompartmentId(),
                        p.getDisplayName(), p.getTimeUpdated())).collect(Collectors.toList());
            }
        };
    }
    
}
