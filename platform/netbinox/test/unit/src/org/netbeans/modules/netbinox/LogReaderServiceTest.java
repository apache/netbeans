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
package org.netbeans.modules.netbinox;

import java.util.Locale;
import java.util.logging.Level;
import junit.framework.Test;
import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Is ExtendedLogReaderService service provided?
 *
 * @author Tomas Stupka
 */
public class LogReaderServiceTest extends NbTestCase {
    public LogReaderServiceTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration()
                .addTest(LogReaderServiceTest.class)
                .honorAutoloadEager(true)
                .clusters(".*")
                .failOnException(Level.WARNING)/*.failOnMessage(Level.WARNING)*/
                .gui(false)
        );
    }
    

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
    }

    public void testExtendedLogReaderServiceAvailable() throws Exception {
        Framework f = IntegrationTest.findFramework();
        BundleContext bc = f.getBundleContext();
        
        ServiceTracker logReaderTracker = new ServiceTracker(bc, ExtendedLogReaderService.class.getName(), null);
        logReaderTracker.open();
                
        LogReaderService logReader = (ExtendedLogReaderService) logReaderTracker.getService();
        assertNotNull(logReader);
            
    }
}