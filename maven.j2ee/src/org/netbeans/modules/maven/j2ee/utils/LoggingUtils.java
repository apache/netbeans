/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
