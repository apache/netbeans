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

package org.netbeans.modules.j2ee.deployment.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.openide.filesystems.FileObject;

/**
 * @author nn136682
 */
public class ConfigFilesListener extends AbstractFilesListener {
    
    private final CopyOnWriteArrayList<? extends ConfigurationFilesListener> consumers;

    // FIXME unfortunately the list crosses the object scope - we should evaluate that
    // and improve that - to risky for now
    public ConfigFilesListener(J2eeModuleProvider provider, CopyOnWriteArrayList<? extends ConfigurationFilesListener> consumers) {
        super(provider);
        this.consumers = consumers;
    }
    
    protected File[] getTargetFiles() {
        //locate the root to listen to
        Collection servers = ServerRegistry.getInstance().getServers();
        ArrayList result = new ArrayList();
        for (Iterator i=servers.iterator(); i.hasNext();) {
            Server s = (Server) i.next();
            String[] paths = s.getDeploymentPlanFiles(provider.getJ2eeModule().getType());
            if (paths == null)
                continue;
            
            for (int j = 0; j < paths.length; j++) {
                File f = provider.getJ2eeModule().getDeploymentConfigurationFile(paths[j]);
                if (f != null)
                    result.add(f);
            }
        }
        return (File[]) result.toArray(new File[0]);
    }

    protected void targetDeleted(FileObject deleted) {
        fireConfigurationFilesChanged(false, deleted);
    }
    
    protected void targetCreated(FileObject added) {
        fireConfigurationFilesChanged(true, added);
    }
    
    protected void targetChanged(FileObject deleted) {
    }
    
    private void fireConfigurationFilesChanged(boolean added, FileObject fo) {
        for (ConfigurationFilesListener cfl : consumers) {
            if (added) {
                cfl.fileCreated(fo);
            } else {
                cfl.fileDeleted(fo);
            }
        }
    }

    protected boolean isTarget(FileObject fo) {
        return isTarget(fo.getNameExt());
    }
    protected boolean isTarget(String fileName) {
        return ServerRegistry.getInstance().isConfigFileName(fileName, provider.getJ2eeModule().getType());
    }
}
