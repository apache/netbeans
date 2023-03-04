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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.InputOutput;

/**
 * Connects Ant-based projects to {@link ShellLaunchManager}. Allocate {@link ShellAgent}
 * before the target process starts, attaches the Ant's I/O window to the agent.
 * <p/>
 * Note: two properties are actually used during the startup: run.jvmargs.ide and run.jvmargs. The StartupExtender
 * is called two times, so it actually generates two agents, but at the end only one of the keys propagates to the actual
 * VM launch, so only one agent will be created.
 * 
 * @author sdedic
 */
@ServiceProvider(service = AntLogger.class)
public class LaunchAntListener extends AntLogger {
    private static final Logger LOG = Logger.getLogger(LaunchAntListener.class.getName());

    private String[] MONITOR_TARGETS = { 
        "do-debug-test-single", // NOI18N
        "debug-test-single-nb", // NOI18N
        "do-debug-test-main", // NOI18N
        "debug-test-main-nb", // NOI18N
        "debug-test-with-main", // NOI18N
        "debug-single", // NOI18N
        "debug", // NOI18N
        "run",
    };
    
    private ShellLaunchManager mgr;
    
    private ShellLaunchManager getManager() {
        if (mgr == null) {
            mgr = Lookup.getDefault().lookup(ShellLaunchManager.class);
            if (mgr == null) {
                throw new IllegalStateException("Could not find launch manager"); // NOI18N
            }
        }
        return mgr;
    }

    @Override
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }

    @Override
    public String[] interestedInTargets(AntSession session) {
        return MONITOR_TARGETS;
    }

    @Override
    public void targetStarted(AntEvent event) {
        if (!Arrays.asList(MONITOR_TARGETS).contains(event.getTargetName())) {
            return;
        }
        super.targetStarted(event);
        InputOutput io = event.getSession().getIO();
        String authKey = ShellLaunchManager.getAuthKey(event.evaluate("${run.jvmargs.ide}"));  // NOI18N
        String authKey2 = ShellLaunchManager.getAuthKey(event.evaluate("${run.jvmargs}"));  // NOI18N
        if (authKey == null && authKey2 == null) {
            return;
        }
        String dispName = event.getSession().getDisplayName();
        getManager().attachInputOutput(authKey, io, dispName);
        getManager().attachInputOutput(authKey2, io, dispName);
    }
    
    @Override
    public boolean interestedInSession(AntSession session) {
        LOG.log(Level.FINE, "Checking interestInSession: " + session);
        String s = session.getProperties().get("run.jvmargs");
        return s != null && s.contains("jshell-probe.jar"); // NOI18N
    }
    
    @Override
    public void buildFinished(AntEvent event) {
        LOG.log(Level.FINE, "Got build finished: " + event);
        destroyAgent(event);
    }
    
    private void destroyAgent(AntEvent event) {
        String authKey = ShellLaunchManager.getAuthKey(event.evaluate("${run.jvmargs.ide}")); // NOI18N
        String authKey2 = ShellLaunchManager.getAuthKey(event.evaluate("${run.jvmargs}")); // NOI18N
        // do not block ANT process
        RequestProcessor.getDefault().post(() -> {
            getManager().destroyAgent(authKey);
            getManager().destroyAgent(authKey2);
        });
    }

    @Override
    public void buildInitializationFailed(AntEvent event) {
        LOG.log(Level.FINE, "Got build init failed: " + event);
        destroyAgent(event);
    }
    
}
