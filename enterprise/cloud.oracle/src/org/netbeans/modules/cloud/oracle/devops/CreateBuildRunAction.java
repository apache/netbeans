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
import com.oracle.bmc.devops.model.BuildRun;
import com.oracle.bmc.devops.model.CreateBuildRunDetails;
import com.oracle.bmc.devops.requests.CreateBuildRunRequest;
import com.oracle.bmc.devops.responses.CreateBuildRunResponse;
import com.oracle.bmc.model.BmcException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.CreateBuildRun"
)
@ActionRegistration(
        displayName = "#CreateBuildRunAction",
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/BuildPipeline/Actions", position = 260)
})
@NbBundle.Messages({
    "CreateBuildRunAction=Run Build",})
public class CreateBuildRunAction implements ActionListener {

    private final BuildPipelineItem pipeline;

    public CreateBuildRunAction(BuildPipelineItem pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try ( DevopsClient client = new DevopsClient(OCIManager.getDefault().getConfigProvider())) {
            CreateBuildRunDetails createBuildRunDetails = CreateBuildRunDetails.builder()
                    .buildPipelineId(pipeline.getKey().getValue()).build();

            CreateBuildRunRequest request = CreateBuildRunRequest.builder().createBuildRunDetails(createBuildRunDetails).build();

            CreateBuildRunResponse response = client.createBuildRun(request);

            BuildRun buildRun = response.getBuildRun();
        } catch (BmcException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
