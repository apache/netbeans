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
package org.netbeans.modules.maven.j2ee.utils;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Helper class for UI and Usage logging.
 * 
 * @author Martin Janicek
 */
public class LoggingUtils {
    
    private static final String UI_LOGGER_NAME = "org.netbeans.ui";
    private static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics";
    private static final Logger UI_LOGGER = Logger.getLogger(UI_LOGGER_NAME);
    private static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME);
    
    
    private LoggingUtils() {
    }
    
    /**
     * Logs the UI gesture using ResourceBundle.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(ResourceBundle bundle, String message, Object[] params) {
        logUsingResourceBundle(UI_LOGGER, bundle, message, params);
    }
    
    public static void logUI(ResourceBundle bundle, String message, Object[] params, String moduleName) {
        logUsingResourceBundle(createUiLogger(moduleName), bundle, message, params);
    }
    
    /**
     * Logs the UI gesture using source class.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(Class srcClass, String message, Object[] params) {
        logUsingSourceClass(UI_LOGGER, srcClass, message, params);
    }
    
    public static void logUI(Class srcClass, String message, Object[] params, String moduleName) {
        logUsingSourceClass(createUiLogger(moduleName), srcClass, message, params);
    }
    

    /**
     * Logs usage data using ResourceBundle.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(ResourceBundle bundle, String message, Object[] params) {
        logUsingResourceBundle(USG_LOGGER, bundle, message, params);
    }
    
    public static void logUsage(ResourceBundle bundle, String message, Object[] params, String moduleName) {
        logUsingResourceBundle(createUsageLogger(moduleName), bundle, message, params);
    }

    /**
     * Logs usage data using source class.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        logUsingSourceClass(USG_LOGGER, srcClass, message, params);
    }
    
    public static void logUsage(Class srcClass, String message, Object[] params, String moduleName) {
        logUsingSourceClass(createUsageLogger(moduleName), srcClass, message, params);
    }
    
    private static void logUsingSourceClass(Logger logger, Class srcClass, String message, Object[] params) {
        LogRecord logRecord = createLogRecord(logger, message, params);
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        if (srcClass != null && srcClass.getPackage() != null) {
            logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        }
        
        logger.log(logRecord);
    }
    
    private static void logUsingResourceBundle(Logger logger, ResourceBundle bundle, String message, Object[] params) {
        Parameters.notNull("bundle", bundle); // NOI18N

        LogRecord logRecord = createLogRecord(logger, message, params);
        logRecord.setResourceBundle(bundle);
        
        logger.log(logRecord);
    }
    
    static Logger createUiLogger(String moduleName) {
        if (moduleName != null && "".equals(moduleName.trim()) == false) {
            return Logger.getLogger(UI_LOGGER_NAME + "." + moduleName);
        }
        
        return UI_LOGGER;
    }
    
    static Logger createUsageLogger(String moduleName) {
        if (moduleName != null && "".equals(moduleName.trim()) == false) {
            return Logger.getLogger(USG_LOGGER_NAME + "." + moduleName);
        }
        
        return USG_LOGGER;
    }
    
    private static LogRecord createLogRecord(Logger logger, String message, Object[] params) {
        Parameters.notNull("message", message); // NOI18N
        
        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(logger.getName());
        if (params != null) {
            logRecord.setParameters(params);
        }
        
        return logRecord;
    }
}
