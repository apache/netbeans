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
package org.netbeans.modules.profiler.j2ee.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.api.project.Project;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
@StartupExtenderImplementation.Registration(position=100, displayName="#DESC_Profiler",
        startMode=StartMode.PROFILE)
public class ServerProfilerArgsProvider implements StartupExtenderImplementation {

    @Override
    public List<String> getArguments(Lookup context, StartMode mode) {
        if (context.lookup(Project.class) == null) { // project related execution is handled elsewhere
            Profiler p = Lookup.getDefault().lookup(Profiler.class);
            ServerInstance server = context.lookup(ServerInstance.class);
            if (server != null) {
                InstanceProperties ip = server.getLookup().lookup(InstanceProperties.class);
                if (ip != null) {
                    return Arrays.asList(p.getSettings(ip.getProperty("url"), false).getJvmArgs()); //NOI18N
                }
            }
        }
        return Collections.EMPTY_LIST;
    }

}
