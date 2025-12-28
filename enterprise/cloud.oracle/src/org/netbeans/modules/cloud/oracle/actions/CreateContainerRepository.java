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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.assets.OpenProjectsFinder;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.requests.ContainerRepositoryRequest;
import org.netbeans.modules.cloud.oracle.requests.OCIItemCreationDetails;
import org.netbeans.modules.cloud.oracle.steps.CompartmentStep;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Petrovic
 */

@NbBundle.Messages({
    "CreateContainerRepository=Create new container repository",
    "EnterName=Enter OCI resource name"
})
public class CreateContainerRepository implements OCIItemCreator {
    
    private static final String REPOSITORY_NAME_FIELD = "name";

    @Override
    public CompletableFuture<? extends OCIItem> create(Steps.Values values, Map<String, Object> params) {
        CompartmentItem compartment = ((CompartmentItem)values.getValueForStep(CompartmentStep.class));
        String displayName = (String) params.get(REPOSITORY_NAME_FIELD);
        OCIItemCreationDetails creationDetails = new ContainerRepositoryRequest(compartment, displayName);
        return new org.netbeans.modules.cloud.oracle.actions.CreateContainerRepositoryCommand().create(creationDetails);
    }
   
    @Override
    public CompletableFuture<Map<String, Object>> steps() {
        Map<String, Object> result = new HashMap<>();
        CompletableFuture future = new CompletableFuture();

        String suggestedName = getSuggestedName(future);
        NotifyDescriptor.InputLine ci = new NotifyDescriptor.InputLine(Bundle.EnterName(), Bundle.EnterName());   
        
        if (suggestedName != null) {
            ci.setInputText(suggestedName);
        }
        
        DialogDisplayer.getDefault().notifyFuture(ci).handle((r, exception) -> {
            result.put(REPOSITORY_NAME_FIELD, r.getInputText());
            
            if (exception != null) {
                future.completeExceptionally(exception);
            } else {
                future.complete(result);
            }
            return null;
        });
        return future;
    }

    private String getSuggestedName(CompletableFuture future) {
        CompletableFuture<Project[]> projects = OpenProjectsFinder.getDefault().findTopLevelProjects();
        ProjectInformation pi = null;
        try {
            Project[] p = projects.get();
            if (p != null && p.length > 0) {
                pi = ProjectUtils.getInformation(p[0]);
            } 
        } catch (InterruptedException | ExecutionException ex) {
            future.completeExceptionally(ex);
        }
        return pi == null ? null : pi.getDisplayName();
    }
}
