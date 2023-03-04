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
package org.netbeans.modules.jshell.j2se;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.startup.StartupExtender.StartMode;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.modules.jshell.project.LaunchedProjectOpener;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.util.Lookup;

/**
 * Hooks onto the J2SE project startup, and injects JShell agent as java agent.
 *
 * @author sdedic
 */
@StartupExtenderImplementation.Registration(displayName = "Java Shell", position = 10000, argumentsQuoted = false,
    startMode = { StartMode.DEBUG, StartMode.NORMAL })
public class JShellStartupExtender implements StartupExtenderImplementation {
    private static final Logger LOG = Logger.getLogger(JShellStartupExtender.class.getName());
    
    @Override
    public List<String> getArguments(Lookup context, StartMode mode) {
        LaunchedProjectOpener.init();
        
        Project p = context.lookup(Project.class);
        if (p == null) {
            return Collections.emptyList();
        }
        
        LOG.log(Level.FINE, "Augmenting {0} of project {1}", new Object[] { mode, p });
        
        InetSocketAddress isa;
        ShellAgent agent;
        // first check that the project has JShell enabled:
        if (!ShellProjectUtils.isJShellRunEnabled(p)) {
            LOG.log(Level.FINE, "Request for agent: Project {0} does not enable Java Shell.", p);
            return Collections.emptyList();
        }
        try {
            agent = ShellLaunchManager.getInstance().openForProject(p, 
                    mode == StartMode.DEBUG || mode == StartMode.TEST_DEBUG);
            isa = agent.getHandshakeAddress();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Could not obtain handshake address and key: ", ex);
            return Collections.emptyList();
        }
        LOG.log(Level.FINE, "Connect address is: {0}:{1}", new Object[] { isa.getHostString(), isa.getPort() });
        
        J2SEPropertyEvaluator  prjEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        JavaPlatform platform = ShellProjectUtils.findPlatform(p);
        List<String> args = ShellLaunchManager.buildLocalJVMAgentArgs(platform, agent, prjEval.evaluator()::getProperty);

        args.addAll(ShellProjectUtils.launchVMOptions(p));

        LOG.log(Level.FINE, "Final args: {0}", args);
        return args;
    }
}
