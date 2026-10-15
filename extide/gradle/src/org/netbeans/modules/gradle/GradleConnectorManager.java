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
package org.netbeans.modules.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.gradle.tooling.GradleConnector;
import org.netbeans.api.project.Project;
import org.openide.modules.OnStop;

/**
 *
 * @author lkishalmi
 */
public final class GradleConnectorManager {
    
    private final Map<Project, GradleConnector> projectConnector = new WeakHashMap<>();
    private final List<GradleConnector> connectors = new ArrayList<>();

    private static final GradleConnectorManager INSTANCE = new GradleConnectorManager();
    private static final Logger LOG = Logger.getLogger(GradleConnectorManager.class.getName());
    
    private GradleConnectorManager() {}
    
    public static GradleConnectorManager getDefault() {
        return INSTANCE;
    } 
    
    public GradleConnector getConnector(Project prj) {
        synchronized (connectors) {
            GradleConnector ret = projectConnector.computeIfAbsent(prj, p -> {
                GradleConnector conn = GradleConnector.newConnector();
                connectors.add(conn);
                return conn;
            });
            return ret;            
        }
    }
    
    
    public void disconnectAll() {
        LOG.info("Disconnecting from Gradle Daemons."); //NOI18N
        synchronized (connectors) {
            projectConnector.clear();
            
            connectors.forEach(GradleConnector::disconnect);
            connectors.clear();
        }
        LOG.info("Disconnecting from Gradle Daemons. Done."); //NOI18N
    } 
    
    @OnStop
    public static class DisconnectGradle implements Runnable {

        @Override
        public void run() {
            GradleConnectorManager.getDefault().disconnectAll();
        }
        
    } 
}
