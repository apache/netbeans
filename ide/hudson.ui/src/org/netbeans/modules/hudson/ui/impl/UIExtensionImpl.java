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
package org.netbeans.modules.hudson.ui.impl;

import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.spi.UIExtension;
import org.netbeans.modules.hudson.ui.api.UI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jhavlin
 */
@ServiceProvider(service = UIExtension.class)
public class UIExtensionImpl extends UIExtension {

    /**
     * Show a build in the UI - Select the node in Services window.
     *
     * @param build Build to show.
     */
    @Override
    public void showInUI(HudsonJobBuild build) {
        HudsonJob job = build.getJob();
        UI.selectNode(job.getInstance().getUrl(),
                job.getName(), Integer.toString(build.getNumber()));
    }

    /**
     * Show a job in the UI - Select the node in Services window.
     *
     * @param job Job to show.
     */
    @Override
    public void showInUI(HudsonJob job) {
        UI.selectNode(job.getInstance().getUrl(), job.getName());
    }
}
