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
package org.netbeans.nbbuild.extlibs;

import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Hector Espert
 */
public class ConfigureProxyTest extends NbTestCase {
    
    private ConfigureProxy configureProxy;

    public ConfigureProxyTest(String name) {
        super(name);
    }
    
    @Override
    public void setUp() {
        configureProxy = new ConfigureProxy();
        
        Project project = new Project();
        configureProxy.setProject(project);
    }
    
    public void testExecute() throws MalformedURLException {
        configureProxy.setConnectTo("http://netbeans.apache.org");
        configureProxy.execute();

        assertNotNull(configureProxy.getProject().getUserProperty("http.proxyHost"));
        assertNotNull(configureProxy.getProject().getUserProperty("http.proxyPort"));
    }
    
    public void testExecuteCustomProperties() throws MalformedURLException {
        configureProxy.setConnectTo("http://netbeans.apache.org");
        configureProxy.setHostProperty("proxyHost");
        configureProxy.setPortProperty("proxyPort");
        configureProxy.execute();

        assertNotNull(configureProxy.getProject().getUserProperty("proxyHost"));
        assertNotNull(configureProxy.getProject().getUserProperty("proxyPort"));
    }
    
    public void testExecuteFailedConnection() throws MalformedURLException {
        try {
            configureProxy.setConnectTo("http://notfound.not");
            configureProxy.execute();
            fail("Exception is expected");
        } catch (BuildException buildException) {
            assertEquals(IOException.class, buildException.getCause().getClass());
        }
    }
    
}
