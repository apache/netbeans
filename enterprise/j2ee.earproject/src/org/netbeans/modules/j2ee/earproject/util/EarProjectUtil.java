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
package org.netbeans.modules.j2ee.earproject.util;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Common utilities for Enterprise project.
 * This is a helper class; all methods are static.
 * @author Tomas Mysik
 */
public final class EarProjectUtil {
    
    private static final Logger UI_LOGGER = Logger.getLogger("org.netbeans.ui.j2ee.earproject"); // NOI18N
    public static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.j2ee.earproject"; // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME); // NOI18N

    private EarProjectUtil() {}

    /**
     * Return <code>true</code> if deployment descriptor exists on the filesystem.
     * <p>
     * This method is useful if we want to write changes to the <i>application.xml</i> file.
     * @param earProject EAR project instance.
     * @return <code>true</code> if deployment descriptor exists on the filesystem for given EAR project.
     * @see org.netbeans.modules.j2ee.earproject.ProjectEar#getDeploymentDescriptor()
     */
    public static boolean isDDWritable(EarProject earProject) {
        return (earProject.getAppModule().getDeploymentDescriptor() != null);
    }
    
    /**
     * Check that the given String is neither <code>null</code> nor of length 0.
     * @param str input String.
     * @return <code>true</code> if input string contains any characters.
     */
    public static boolean hasLength(String str) {
        return str != null && str.length() > 0;
    }
    
    /**
     * Logs the UI gesture.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(ResourceBundle bundle,String message, Object[] params) {
        Parameters.notNull("message", message);
        Parameters.notNull("bundle", bundle);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(UI_LOGGER.getName());
        logRecord.setResourceBundle(bundle);
        if (params != null) {
            logRecord.setParameters(params);
        }
        UI_LOGGER.log(logRecord);
    }    
    
    /**
     * Logs feature usage.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }        
    
    /**
     * Check whether the project is Java EE module (e.g. EJB but not EAR).
     * @param project project to check, can be <code>null</code>.
     * @return <code>true</code> if the project is Java EE module, <code>false</code> otherwise.
     */
    public static boolean isJavaEEModule(Project project) {
        if (project != null
                && project.getLookup().lookup(J2eeModuleProvider.class) != null
                && project.getLookup().lookup(J2eeApplicationProvider.class) == null) {
            return true;
        }
        return false;
    }
}
