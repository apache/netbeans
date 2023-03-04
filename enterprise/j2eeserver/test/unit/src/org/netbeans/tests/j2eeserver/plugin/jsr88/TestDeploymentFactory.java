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

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

/**
 *
 * @author Petr Hejl
 */
public class TestDeploymentFactory implements DeploymentFactory {

    private Map managers = new HashMap();

    private final String prefix;

    /** Creates a new instance of DepFactory */
    protected TestDeploymentFactory(String prefix) {
        assert prefix != null;
        this.prefix = prefix;
    }

    public static TestDeploymentFactory create(Map map) {
        return new TestDeploymentFactory((String) map.get("prefix"));
    }

    public synchronized javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(String str, String str1, String str2) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException {
        TestDeploymentManager manager = (TestDeploymentManager) managers.get(str + str1 + str2);
        if (manager == null){
            manager = new TestDeploymentManager(str, str1, str2);
            managers.put(str, manager);
        }
        return manager;
    }
    
    public synchronized javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(String str) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException {
        TestDeploymentManager manager = (TestDeploymentManager) managers.get(str);
        if (manager == null) {
            manager = new TestDeploymentManager(str,"","");
            managers.put(str, manager);
        }
        return manager;
    }
    
    public String getDisplayName() {
        return "Sample JSR88 plugin";// PENDING parameterize this.
    }
    
    public String getProductVersion() {
        return "0.9";// PENDING version this plugin somehow?
    }
    
    public boolean handlesURI(String str) {
        return (str != null && str.startsWith(prefix));
    }
    
}
