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
package org.netbeans.modules.javaee.wildfly.nodes.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class StopModuleCookieImpl implements StopModuleCookie {


    private static final RequestProcessor PROCESSOR = new RequestProcessor("JBoss stop", 1); // NOI18N

    private final String fileName;

    private final Lookup lookup;

    private final ModuleType type;

    private boolean isRunning;

    public StopModuleCookieImpl(String fileName, Lookup lookup) {
        this(fileName, ModuleType.WAR, lookup);
    }

    public StopModuleCookieImpl(String fileName, ModuleType type, Lookup lookup) {
        this.lookup = lookup;
        this.fileName = fileName;
        this.type = type;
        this.isRunning = false;
    }

    @Override
    public Task stop() {
        final WildflyDeploymentManager dm = (WildflyDeploymentManager) lookup.lookup(WildflyDeploymentManager.class);
        final String nameWoExt = fileName.substring(0, fileName.lastIndexOf('.'));
        final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(StopModuleCookieImpl.class,
                "LBL_StopProgress", nameWoExt));

        Runnable r = new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                try {
                    dm.getClient().stopModule(fileName);
                } catch (IOException ex) {
                    Logger.getLogger(StopModuleCookieImpl.class.getName()).log(Level.INFO, null, ex);
                }
                handle.finish();
                isRunning = false;
            }
        };
        handle.start();
        return PROCESSOR.post(r);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

}
