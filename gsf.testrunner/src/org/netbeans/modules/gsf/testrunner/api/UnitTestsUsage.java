/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
