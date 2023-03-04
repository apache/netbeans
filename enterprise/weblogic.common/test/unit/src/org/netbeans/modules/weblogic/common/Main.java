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

package org.netbeans.modules.weblogic.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Collection;
import org.netbeans.api.extexecution.base.BaseExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.netbeans.modules.weblogic.common.api.WebLogicDeployer;
import org.netbeans.modules.weblogic.common.api.WebLogicRuntime;

/**
 *
 * @author Petr Hejl
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String serverHome = "/home/petr/software/wls12130/wlserver";//args[0];
        String domainHome = "/home/petr/software/wls12130/user_projects/domains/mydomain";//;args[1];
        //String artifact = args[2];
        WebLogicConfiguration config = WebLogicConfiguration.forLocalDomain(
                new File(serverHome), new File(domainHome), new WebLogicConfiguration.Credentials() {

            @Override
            public String getUsername() {
                return "weblogic";
            }

            @Override
            public String getPassword() {
                return "welcome1";
            }
        });
//        WebLogicConfiguration config = WebLogicConfiguration.forRemoteDomain(
//                new File(serverHome), "192.168.56.101", 7001, false, new WebLogicConfiguration.Credentials() {
//
//            @Override
//            public String getUsername() {
//                return "weblogic";
//            }
//
//            @Override
//            public String getPassword() {
//                return "welcome1";
//            }
//        });

        WebLogicRuntime runtime = WebLogicRuntime.getInstance(config);
        runtime.startAndWait(new DefaultFactory(), new DefaultFactory(), null);
        System.out.println("Started");

        runtime.startAndWait(new DefaultFactory(), new DefaultFactory(), null);
        System.out.println("Started again");

        WebLogicDeployer deployer = WebLogicDeployer.getInstance(config, null, null);
        Collection<WebLogicDeployer.Application> apps = deployer.list(null).get();
        for (WebLogicDeployer.Application app : apps) {
            System.out.println("Application " + app);
        }

        runtime.stopAndWait(new DefaultFactory(), new DefaultFactory());
        System.out.println("Stopped");

//        WebLogicDeployer deployer = WebLogicDeployer.getInstance(config, null);
//        Future<Boolean> ret = deployer.deploy(new File(artifact), null, new String[0]);
//        System.out.println("Deployed: " + ret.get());
    }

    private static class DefaultFactory implements BaseExecutionDescriptor.InputProcessorFactory {

        @Override
        public InputProcessor newInputProcessor() {
            return InputProcessors.copying(new BufferedWriter(new OutputStreamWriter(System.out)));
        }
    }
}
