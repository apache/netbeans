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
package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.ui.impl.HudsonConsoleDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 * Displays the console from a Hudson build in the Output Window.
 */
public class ShowBuildConsole extends AbstractAction {

    private final HudsonJobBuild build;
    private final HudsonMavenModuleBuild moduleBuild;

    @Messages("ShowBuildConsole.label=Show Console")
    private ShowBuildConsole(HudsonJobBuild build,
            HudsonMavenModuleBuild moduleBuild) {
        this.build = build;
        this.moduleBuild = moduleBuild;
        putValue(NAME, Bundle.ShowBuildConsole_label());
    }

    public ShowBuildConsole(HudsonMavenModuleBuild moduleBuild) {
        this(moduleBuild.getBuild(), moduleBuild);
    }

    public ShowBuildConsole(HudsonJobBuild build) {
        this(build, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (moduleBuild != null) {
            if (moduleBuild.canShowConsole()) {
                moduleBuild.showConsole(new HudsonConsoleDisplayer(moduleBuild));
            }
        } else if (build.canShowConsole()) {
            build.showConsole(new HudsonConsoleDisplayer(build));
        }
    }

    @Override
    public boolean isEnabled() {
        return build.canShowConsole();
    }
}
