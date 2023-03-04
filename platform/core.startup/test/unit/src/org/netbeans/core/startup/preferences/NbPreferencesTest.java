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

package org.netbeans.core.startup.preferences;

import java.util.prefs.Preferences;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Radek Matous
 */
public class NbPreferencesTest extends NbTestCase {    
    public NbPreferencesTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestPreferences.class);
        suite.addTestSuite(TestFileStorage.class);
        suite.addTestSuite(TestPropertiesStorage.class);
        suite.addTestSuite(TestNbPreferencesFactory.class);
        suite.addTestSuite(TestNbPreferencesThreading.class);
        
        return suite;
    }
    
    public static class TestBasicSetup extends NbTestCase {
        public TestBasicSetup(String testName) {
            super(testName);                        
        }
        
        public @Override void run(final TestResult result) {
            //just do registration before code NbTestCase
            NbPreferencesFactory.doRegistration();       
            Preferences.userRoot();                        
            super.run(result);
        }
        
        protected @Override void tearDown() throws Exception {
            super.tearDown();
            /*Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.INFO  ,getName()+ "->" + Statistics.FLUSH.toString());//NOI18N
            logger.log(Level.INFO,getName()+ "->" + Statistics.LOAD.toString());//NOI18N
            logger.log(Level.INFO,getName()+ "->" + Statistics.REMOVE_NODE.toString());//NOI18N
            logger.log(Level.INFO,getName()+ "->" + Statistics.CHILDREN_NAMES.toString());//NOI18N
             **/
        }
        
        protected @Override void setUp() throws Exception {
            super.setUp();                        
            Statistics.CHILDREN_NAMES.reset();
            Statistics.FLUSH.reset();
            Statistics.LOAD.reset();
            Statistics.REMOVE_NODE.reset();
        }           
    }
}
