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

package org.netbeans.modules.server.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class MockInstanceProvider implements ServerInstanceProvider {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final List<ServerInstance> instances = new ArrayList<ServerInstance>();

    public MockInstanceProvider() {
        super();
    }

    public static void registerInstanceProvider(String instanceName, ServerInstanceProvider provider) throws IOException {
        if (provider == null) {
            return;
        }

        Lookup.getDefault().lookup(ModuleInfo.class);

        FileObject servers = FileUtil.getConfigFile(ServerRegistry.SERVERS_PATH);
        FileObject testProvider = FileUtil.createData(servers, instanceName);

        testProvider.setAttribute("instanceOf", ServerInstanceProvider.class.getName()); // NOI18N
        testProvider.setAttribute("instanceCreate", provider); // NOI18N
    }

    public void addInstance(ServerInstance instance) {
        if (instance == null) {
            return;
        }

        synchronized (this) {
            instances.add(instance);
        }
        changeSupport.fireChange();
    }

    public void removeInstance(ServerInstance instance) {
        if (instance == null) {
            return;
        }

        synchronized (this) {
            instances.remove(instance);
        }
        changeSupport.fireChange();
    }

    public void clear() {
        synchronized (this) {
            instances.clear();
        }
        changeSupport.fireChange();
    }

    public List<ServerInstance> getInstances() {
        synchronized (this) {
            return new ArrayList<ServerInstance>(instances);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

}
