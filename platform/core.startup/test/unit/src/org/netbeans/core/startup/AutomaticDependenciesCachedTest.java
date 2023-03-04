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

package org.netbeans.core.startup;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;


/** Make sure automatic dependencies are parsed only on first start.
 * 
 * @author Jaroslav Tulach
 */
public class AutomaticDependenciesCachedTest extends NbTestCase {
    public AutomaticDependenciesCachedTest(String name) {
        super(name);
    }

    public static Test suite() throws IOException {
        assertFalse("Not consulted yet", LogConfig.consulted);
        System.setProperty("java.util.logging.config.class", LogConfig.class.getName());
        LogManager.getLogManager().readConfiguration();
        assertTrue("LogConfig class consulted", LogConfig.consulted);
        
        NbTestSuite s = new NbTestSuite();
        
        s.addTest(
            NbModuleSuite.emptyConfiguration().
            addTest(AutomaticDependenciesCachedTest.class, "testFirstStart").
            enableModules("platform\\d*", ".*").enableClasspathModules(false).
            honorAutoloadEager(true).gui(false).suite()
        );
        s.addTest(
            NbModuleSuite.emptyConfiguration().
            addTest(AutomaticDependenciesCachedTest.class, "testSecondStart").
            enableModules("platform\\d*", ".*").enableClasspathModules(false).
            reuseUserDir(true).honorAutoloadEager(true).gui(false).suite()
        );
        
        return s;
    }
    
    public void testFirstStart() {
        assertNotNull("Automatic dependencies parsed", System.getProperty("log.msg"));
        System.getProperties().remove("log.msg");
    }
    
    public void testSecondStart() {
        assertNull("Automatic dependencies should not be parsed on subsequent start", 
            System.getProperty("log.msg")
        );
    }
    
    private static final class Observer extends Handler {
        private final Logger logger;

        public Observer(Logger logger) {
            this.logger = logger;
        }
        
        
        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("Parsing automatic dependencies")) {
                System.setProperty("log.msg", record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
            logger.addHandler(this);
        }
        
    }
    
    public static final class LogConfig {
        static boolean consulted;
        
        public LogConfig() {
            consulted = true;
            final Logger logger = Logger.getLogger("org.netbeans.core.startup.AutomaticDependencies");
            Observer o = new Observer(logger);
            o.setLevel(Level.ALL);
            logger.setLevel(Level.ALL);
            logger.addHandler(o);
        }
    }
}
