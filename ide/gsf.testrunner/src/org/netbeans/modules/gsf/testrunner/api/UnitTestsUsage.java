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
package org.netbeans.modules.gsf.testrunner.api;

import java.net.URI;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Class that helps to log usage of unit test library used in a project to create or run tests.
 *
 * @author Theofanis Oikonomou
 */
public final class UnitTestsUsage {
    
    private static UnitTestsUsage INSTANCE;
    private HashMap<URI, String> projectsAlreadyLogged;

    private UnitTestsUsage() {
        projectsAlreadyLogged = new HashMap<URI, String>();
    }
    
    public static UnitTestsUsage getInstance() {
        if(INSTANCE != null) {
            return INSTANCE;
        }
        return new UnitTestsUsage();
    }
    
    /**
     * Logs usage of unit test library used in a project to create or run tests.
     *
     * @param projectURI the project's URI
     * @param unitTestLibrary the unit test's library used, e.g. JUNIT3 or JUNIT4
     */
    public void logUnitTestUsage(URI projectURI, String unitTestLibrary) {
        assert projectURI != null : "Project's URI cannot be null";
        assert unitTestLibrary != null : "Unit test's library cannot be null";
        Logger logger = Logger.getLogger("org.netbeans.ui.metrics.unittestslibrary");   // NOI18N
        LogRecord rec = new LogRecord(Level.INFO, "USG_UNIT_TESTS_LIBRARY"); //NOI18N
        if (unitTestLibrary.isEmpty()) {
            return;
        }

        if (!projectsAlreadyLogged.containsKey(projectURI)) {
            projectsAlreadyLogged.put(projectURI, unitTestLibrary.toString());
        } else {
            String unitTestLibrariesUsed = projectsAlreadyLogged.get(projectURI);
            if (unitTestLibrariesUsed.equals(unitTestLibrary)) {
                return;
            } else {
                projectsAlreadyLogged.put(projectURI, unitTestLibrary);
            }
        }
        rec.setParameters(new Object[]{unitTestLibrary.toString()});
        rec.setLoggerName(logger.getName());
        logger.log(rec);
    }
    
}
