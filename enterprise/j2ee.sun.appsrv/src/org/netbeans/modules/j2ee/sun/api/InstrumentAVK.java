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

package org.netbeans.modules.j2ee.sun.api;


import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
/**
 *
 * @author Nitya Doraisamy
 */
public interface InstrumentAVK {

    /*
     *@param : SunDeploymentManager associated with server
     */
    public void setDeploymentManager(SunDeploymentManagerInterface sdm);

    /*
     *@param : onOff - indicates whether to add or remove istrumentation
     *for AVK
     *Server could be running or stopped
     *Turning On Instrumentation : edit server classpath and jvm option
     *Turning Off Instrumentation : remove classpath and jvm option
     */
    public void setAVK(boolean onOff);
    
    /*
     *Server should be stopped before this is called. Handled by GenerateReportAction
     *in AVK plugin
     *Runs report tool and then launches browser with generated report
     */
    public void generateReport();
    
     /* Show Dialog Descriptor to choose between Static / Dynamic Verification
      * @return : static / dynamic / none
      */
    public boolean createAVKSupport(DeploymentManager dm, J2eeModuleProvider target);
}
