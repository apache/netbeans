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
package org.netbeans.modules.cloud.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Jan Horvath
 */

public class OCITenancyProvider implements ServerInstanceProvider, ChangeListener {

    private final ChangeSupport listeners;
    private final List<ServerInstance> instances;
    private static OCITenancyProvider instance;

    private OCITenancyProvider() {
        listeners = new ChangeSupport(this);
        instances = new ArrayList<> ();
        refresh();
    }

    public static synchronized OCITenancyProvider getProvider() {
        if (instance == null) {
            instance = new OCITenancyProvider();
        }
        return instance;
    }
    
    @Override
    public List<ServerInstance> getInstances() {
        return Collections.unmodifiableList(instances);
    }
    
    private void refresh() {
        OCIManager.getDefault().getTenancy().ifPresent(tenancy -> {
            ServerInstance si = ServerInstanceFactory.createServerInstance(new TenancyInstance(tenancy));
            instances.add(si);
            listeners.fireChange();
        });
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refresh();
    }
    
}
