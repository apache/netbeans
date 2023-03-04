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

import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.spi.ConsoleDataDisplayerImpl;
import org.netbeans.modules.hudson.ui.actions.Hyperlinker;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author jhavlin
 */
public class HudsonConsoleDisplayer extends ConsoleDataDisplayerImpl {

    private final HudsonJob job;
    private final String displayName;
    private OutputWriter out;
    private OutputWriter err;
    private Hyperlinker hyperlinker;
    private InputOutput io;

    public HudsonConsoleDisplayer(HudsonJobBuild build) {
        this.job = build.getJob();
        this.displayName = build.getDisplayName();
    }

    public HudsonConsoleDisplayer(HudsonMavenModuleBuild moduleBuild) {
        this.job = moduleBuild.getBuild().getJob();
        this.displayName = moduleBuild.getDisplayName();
    }

    @Override
    public synchronized void open() {
        hyperlinker = new Hyperlinker(job);
        io = IOProvider.getDefault().getIO(displayName, new Action[]{});
        io.select();
        out = io.getOut();
        err = io.getErr();
    }

    @Override
    public synchronized boolean writeLine(String line) {
        if (out.checkError() || err.checkError() || io.isClosed()) {
            return false;
        }
        OutputWriter stream = line.matches("(?i).*((warn(ing)?|err(or)?)[]:]|failed).*") ? err : out; //NOI18N
        hyperlinker.handleLine(line, stream);
        return true;
    }

    @Override
    public void close() {
        out.close();
        err.close();
    }
}
