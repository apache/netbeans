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
package org.netbeans.modules.cnd.makeproject.ui;

import java.io.IOException;
import org.netbeans.modules.cnd.makeproject.api.wizards.DefaultMakeProjectLocationProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=DefaultMakeProjectLocationProvider.class)
public class DefaultMakeProjectLocationProviderImpl extends DefaultMakeProjectLocationProvider {

    @Override
    public String getDefaultProjectFolder() {
        return ProjectChooser.getProjectsFolder().getPath();
    }

    @Override
    public String getDefaultProjectFolder(ExecutionEnvironment env) {
        try {
            if (env.isLocal()) {
                return getDefaultProjectFolder();
            } else {
                return HostInfoUtils.getHostInfo(env).getUserDir() + '/' + ProjectChooser.getProjectsFolder().getName();  //NOI18N
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        } catch (ConnectionManager.CancellationException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        }
        return null;
    }
}
