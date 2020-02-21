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
package org.netbeans.modules.remote.ui;

import org.netbeans.modules.dlight.libs.common.FileStatistics;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class StatisticsActions {

    private static final RequestProcessor RP = new RequestProcessor("StatisticsAction", 1); // NOI18N

    @ActionID(id = "org.netbeans.modules.remote.ui.ClearFileStatisticsAction", category = "NativeRemote")
    @ActionRegistration(displayName = "#ClearFileStatisticsAction", lazy = false)
    @ActionReference(path = "Remote/Host/Actions", name = "ClearFileStatisticsAction", position = 99996)
    public static class ClearFileStatisticsAction extends SingleHostAction {

        @Override
        public String getName() {
            return NbBundle.getMessage(HostListRootNode.class, "ClearFileStatisticsAction");
        }

        @Override
        protected boolean enable(ExecutionEnvironment env) {
            return true;
        }

        @Override
        public boolean isVisible(Node node) {
            return Boolean.getBoolean("remote.host.actions.statistics");
        }

        @Override
        protected void performAction(final ExecutionEnvironment env, Node node) {
            FileStatistics.getInstance(FileSystemProvider.getFileSystem(env)).clear();
        }
    }
    
    @ActionID(id = "org.netbeans.modules.remote.ui.ReportFileStatisticsAction", category = "NativeRemote")
    @ActionRegistration(displayName = "#ReportFileStatisticsAction", lazy = true)
    @ActionReference(path = "Remote/Host/Actions", name = "ReportFileStatisticsAction", position = 99997)
    public static class ReportFileStatisticsAction extends SingleHostAction {

        @Override
        public String getName() {
            return NbBundle.getMessage(HostListRootNode.class, "ReportFileStatisticsAction");
        }

        @Override
        protected boolean enable(ExecutionEnvironment env) {
            return true;
        }

        @Override
        public boolean isVisible(Node node) {
            return Boolean.getBoolean("remote.host.actions.statistics");
        }

        @Override
        protected void performAction(final ExecutionEnvironment env, Node node) {
            FileStatistics.getInstance(FileSystemProvider.getFileSystem(env)).report();
        }
    }
}
