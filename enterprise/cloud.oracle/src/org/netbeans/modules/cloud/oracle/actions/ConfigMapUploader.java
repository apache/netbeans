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
package org.netbeans.modules.cloud.oracle.actions;

import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showMessage;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.assets.ConfigMapProvider;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.compute.ClusterItem;
import org.netbeans.modules.cloud.oracle.steps.CompartmentStep;
import org.netbeans.modules.cloud.oracle.steps.ItemTypeStep;
import org.netbeans.modules.cloud.oracle.steps.ProjectStep;
import org.netbeans.modules.cloud.oracle.steps.SuggestedStep;
import org.netbeans.modules.cloud.oracle.steps.TenancyStep;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Petrovic
 */
@NbBundle.Messages({
    "SuggestRerun=The changes will take place only after restarting the application",
    "ClusterNotPresent=Please add the OKE Cluster first"
})
public class ConfigMapUploader {
    
    
    public static void uploadConfigMap(CompletableFuture<Object> future) {
        ClusterItem cluster = CloudAssets.getDefault().getItem(ClusterItem.class);
        if (cluster == null) {
            showMessage(Bundle.ClusterNotPresent());
            return;
        }
        Steps.NextStepProvider nsProvider = Steps.NextStepProvider.builder()
            .stepForClass(ProjectStep.class, (s) -> new TenancyStep())
            .build();
        Lookup lookup = Lookups.fixed(nsProvider);
        Steps.getDefault().executeMultistep(new ProjectStep(), lookup)
                .thenAccept(values -> {
                    Project project = values.getValueForStep(ProjectStep.class);
                    ProjectInformation projectInfo = ProjectUtils.getInformation(project); 
                    ConfigMapProvider configMapProvider = new ConfigMapProvider(projectInfo.getDisplayName(), cluster);
                    configMapProvider.createConfigMap();
                    
                    showMessage(Bundle.SuggestRerun());
                });   
    }
}
