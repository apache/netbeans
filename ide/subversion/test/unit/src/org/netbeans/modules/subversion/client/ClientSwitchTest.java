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

package org.netbeans.modules.subversion.client;

import org.netbeans.modules.subversion.AbstractSvnTestCase;
import org.netbeans.modules.subversion.SvnModuleConfig;

/**
 *
 * @author tomas
 */
public class ClientSwitchTest extends AbstractSvnTestCase {
    
    public ClientSwitchTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSwitchClient () throws Exception {
        SvnClientFactory.resetClient();
        SvnModuleConfig.getDefault().setPreferredFactoryType(null);
        assertEquals(SvnClientFactory.FACTORY_TYPE_JAVAHL, SvnModuleConfig.getDefault().getPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_JAVAHL));
        assertTrue(SvnClientFactory.isJavaHl());
        
        // switch to svnkit
        SvnModuleConfig.getDefault().setPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_SVNKIT);
        SvnClientFactory.resetClient();
        assertEquals(SvnClientFactory.FACTORY_TYPE_SVNKIT, SvnModuleConfig.getDefault().getPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_JAVAHL));
        assertTrue(SvnClientFactory.isSvnKit());
        
        // switch to commandline
        SvnClientFactory.resetClient();
        SvnModuleConfig.getDefault().setPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_COMMANDLINE);
        assertEquals(SvnClientFactory.FACTORY_TYPE_COMMANDLINE, SvnModuleConfig.getDefault().getPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_JAVAHL));
        assertTrue(SvnClientFactory.isCLI());
        
        // switch to javahl
        SvnClientFactory.resetClient();
        SvnModuleConfig.getDefault().setPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_JAVAHL);
        assertEquals(SvnClientFactory.FACTORY_TYPE_JAVAHL, SvnModuleConfig.getDefault().getPreferredFactoryType(SvnClientFactory.FACTORY_TYPE_JAVAHL));
        assertTrue(SvnClientFactory.isJavaHl());
        
    }
}
