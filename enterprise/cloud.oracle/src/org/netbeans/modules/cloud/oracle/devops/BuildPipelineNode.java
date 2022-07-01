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
package org.netbeans.modules.cloud.oracle.devops;

import com.oracle.bmc.devops.DevopsClient;
import com.oracle.bmc.devops.model.BuildPipelineSummary;
import com.oracle.bmc.devops.requests.ListBuildPipelinesRequest;
import com.oracle.bmc.devops.responses.ListBuildPipelinesResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.NbBundle;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "BuildPipelines=Build Pipelines",})
public class BuildPipelineNode extends OCINode {

    private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/build_pipeline.svg"; // NOI18N

    public BuildPipelineNode(OCIItem item) {
        super(item, Children.LEAF);
        setIconBaseWithExtension(ICON);
    }

    public static NodeProvider<OCIItem> createNode() {
        return BuildPipelineNode::new;
    }

    public static ChildrenProvider<DevopsProjectItem, BuildPipelineItem.BuildPipelineFolder> listDevopsPipelines() {
        return project -> {
            try ( DevopsClient client = new DevopsClient(OCIManager.getDefault().getConfigProvider())) {
                ListBuildPipelinesRequest request = ListBuildPipelinesRequest.builder().projectId(project.getKey().getValue()).build();
                ListBuildPipelinesResponse response = client.listBuildPipelines(request);
                List<BuildPipelineSummary> projects = response.getBuildPipelineCollection().getItems();
                return Collections.singletonList(
                        new BuildPipelineItem.BuildPipelineFolder(OCID.of(project.getKey().getValue(), "BuildPipelineFolder"),
                                Bundle.BuildPipelines(),
                                projects.stream()
                                        .map(p -> new BuildPipelineItem(OCID.of(p.getId(), "BuildPipeline"), p.getDisplayName())) // NOI18N
                                        .collect(Collectors.toList()))
                );
            }
        };
    }

    public static class BuildPipelineFolderNode extends OCINode {

        private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/build_pipeline_folder.svg"; // NOI18N

        public BuildPipelineFolderNode(BuildPipelineItem.BuildPipelineFolder folder) {
            super(folder);
            setIconBaseWithExtension(ICON);
        }
        
        public void refresh() {
            ((OCINode) getParentNode()).refresh();
        }
    }

    public static NodeProvider<BuildPipelineItem.BuildPipelineFolder> createFolderNode() {
        return BuildPipelineFolderNode::new;
    }

    public static ChildrenProvider<BuildPipelineItem.BuildPipelineFolder, BuildPipelineItem> expandRepositories() {
        return repositories -> repositories.getPipelines();
    }
    
    
}