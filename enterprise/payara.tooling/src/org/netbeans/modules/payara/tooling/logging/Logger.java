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
package org.netbeans.modules.payara.tooling.logging;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.utils.JavaUtils;

/**
 * Payara IDE SDK Logger.
 * <p>
 * Facade to access IDE Logger methods.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class Logger {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara IDE SDK logger name. Deprecated. */
    private static final String LOGGER_NAME = "org.netbeans.modules.payara.tooling";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Find or create a logger for Payara IDE SDK. If a logger has
     * already been created with the given name it is returned.  Otherwise
     * a new logger is created.
     * <p>
     * If a new logger is created its log level will be configured
     * based on the LogManager configuration and it will configured
     * to also send logging output to its parent's Handlers.  It will
     * be registered in the LogManager global name space.
     * <p>
     * @return Payara IDE SDK Logger
     * @deprecated  Instantiate Logger class!
     */
    public static java.util.logging.Logger getLogger() {
        return java.util.logging.Logger.getLogger(LOGGER_NAME);
    }

    /**
     * Check if a message of the given level would actually be logged
     * by this logger. This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *<p>
     * @param  level  A message logging level.
     * @return <code>true</code> if the given message level is currently being
     *         logged or <code>false</code> otherwise.
     * @deprecated  Instantiate Logger class!
     */
    public static boolean loggable(Level level) {
        return getLogger().isLoggable(level);
    }

    /**
     * Log a message, with associated <code>Throwable</code> information.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     * @param level  One of the message level identifiers, e.g., SEVERE.
     * @param msg    The string message (or a key in the message catalog).
     * @param thrown <code>Throwable</code> associated with log message.
     * @deprecated  Instantiate Logger class!
     */
    public static void log(Level level, String msg, Throwable thrown) {
        getLogger().log(level, msg, thrown);
    }

    /**
     * Log a message, with one object parameter.
     * <p>
     * If the logger is currently enabled for the given message 
     * level then a corresponding LogRecord is created and forwarded 
     * to all the registered output Handler objects.
     * <p>
     * @param	level   One of the message level identifiers, e.g. SEVERE.
     * @param   msg	The string message (or a key in the message catalog).
     * @param   param	Parameter to the message.
     * @deprecated  Instantiate Logger class!
     */
    public static void log(Level level, String msg, Object param) {
        getLogger().log(level, msg, param);
    }

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message 
     * level then a corresponding LogRecord is created and forwarded 
     * to all the registered output Handler objects.
     * <p>
     * @param	level   One of the message level identifiers, e.g. SEVERE.
     * @param   msg	The string message (or a key in the message catalog).
     * @param   params	Array of parameters to the message.
     * @deprecated  Instantiate Logger class!
     */
    public static void log(Level level, String msg, Object params[]) {
        getLogger().log(level, msg, params);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Properties file containing log messages. */
    static final String MESSAGES_FILE = "LogMessages";

    /** Properties file containing exception messages. */
    static final String EXCEPTIONS_FILE = "ExceptionMessages";

    /** Properties file suffix. */
    static final String PROPERTIES_FILE_SUFFIX = ".properties";

    /** Message key elements separator. */
    private static final char KEY_SEPARATOR = '.';

    /** Log messages cache for individual packages. */
    private static final Map<Package, Properties> logProps = new HashMap<>();

    /** Exception messages cache for individual packages. */
    private static final Map<Package, Properties> excProps = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build properties file name with suffix.
     * <p/>
     * @param file    Messages catalog file name.
     * @param locales Add locale suffix when <code>true</code> or do not add it
     *                when <code>false</code>.
     * @return Messages catalog file name with properties file suffix.
     */
    private static String buildPropertiesFileName(final String file,
            final boolean locales) {
        StringBuilder sb = new StringBuilder(
                file.length() + PROPERTIES_FILE_SUFFIX.length());
        sb.append(file);
        sb.append(PROPERTIES_FILE_SUFFIX);
        return sb.toString();
    }

    /**
     * Return message from package properties catalog map with given key.
     * <p/>
     * Properties catalog for package of provided class will be loaded from
     * provided file when missing. 
     * <p/>
     * @param file     Messages catalog file name.
     * @param propsMap Package to properties catalog mapping.
     * @param c        Class to determine properties file package.
     * @param key      Exception message properties key.
     * @return Message from package properties catalog map with given key.
     */
    private static String message(final String file,
            final Map<Package, Properties> propsMap, final Class c,
            final String key) {
        Package pkg = c.getPackage();
        Properties props;
        synchronized(propsMap) {
            props = propsMap.get(pkg);
            if (props == null) {
                props = new Properties();
                URL url = JavaUtils.getPropertiesURL(
                        c, buildPropertiesFileName(file, false));
                if (url != null) {
                    try {
                        url.openStream();
                        props.load(url.openStream());
                        // Initialize properties as empty and send log message
                        // on IOException
                    } catch (IOException ioe) {
                        props = new Properties();
//                    Logger.log(Level.INFO, "Error reading {0} from {1}",
//                            new String[] {file, pkg.getName()});
                    }
                }
                propsMap.put(pkg, props);
            }
        }
        String property = props.getProperty(key);
        return property != null ? property : key;
    }

    /**
     * Return message from log messages properties file with given key.
     * <p/>
     * @param c   Class to determine properties file package.
     * @param key Exception message properties key.
     * @return Message from log messages properties file with given key.
     */
    public static String logMsg(Class c, String key) {
        return message(MESSAGES_FILE, logProps, c, key);
    }

    /**
     * Return message from exception messages properties file with given key.
     * <p/>
     * @param c   Class to determine properties file package.
     * @param key Exception message properties key.
     * @return Message from exception messages properties file with given key.
     */
    public static String excMsg(Class c, String key) {
        return message(EXCEPTIONS_FILE, excProps, c, key);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger name (derived from full class name including package). */
    private final String name;

    /** Logger package (derived from class package). */
    private final Class cl;

    /** {@link java.util.logging.Logger} instance. */
    private final java.util.logging.Logger logger;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of library internal logger.
     * <p/>
     * @param c Class where logger instance was created.
     */
    public Logger(final Class c) {
        name = c.getName();
        cl = c;
        logger = java.util.logging.Logger.getLogger(name);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds message key as
     * <code>&lt;class_name&gt;.&lt;method_name&gt;.&lt;key&gt;</code>.
     * <p/>
     * @param method The message string method key.
     * @param key    The message string key.
     * @return Message key as
     * <code>&lt;class_name&gt;.&lt;method_name&gt;.&lt;key&gt;</code>.
     */
    public String buildKey(final String method, final String key) {
        if (method == null || key == null) {
            throw new IllegalArgumentException("Key value shall not be null.");
        }
        String clName = cl.getSimpleName();
        StringBuilder sb = new StringBuilder(
                clName.length() + method.length() + key.length() + 2);
        sb.append(clName);
        sb.append(KEY_SEPARATOR);
        sb.append(method);
        sb.append(KEY_SEPARATOR);
        sb.append(key);
        return sb.toString();
    }

    /**
     * Return message from exception messages properties file with given key.
     * <p/>
     * @param method The message string method key.
     * @param key    The message string key.
     * @return Message from exception messages properties file with given key.
     */
    public String excMsg(final String method, final String key) {
        return message(EXCEPTIONS_FILE, excProps, cl, buildKey(method, key));
    }

    /**
     * Return message from exception messages properties file with given key
     * and attributes.
     * <p/>
     * @param method The message string method key.
     * @param key    The message string key.
     * @param attrs  Message attributes.
     * @return Message from exception messages properties file with given key
     *         and attributes.
     */
    public String excMsg(final String method, final String key,
            final String ...attrs) {
        String message =  message(
                EXCEPTIONS_FILE, excProps, cl, buildKey(method, key));
        return MessageFormat.format(message, (Object[])attrs);
    }

    /**
     * Check if a message of the given level would actually be logged
     * by this logger. This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *<p>
     * @param  level  A message logging level.
     * @return <code>true</code> if the given message level is currently being
     *         logged or <code>false</code> otherwise.
     */
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    /**
     * Log a message, with no arguments from log messages catalog.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param level  One of the message level identifiers, e.g., SEVERE.
     * @param method The message string method key.
     * @param key    The message string key.
     */
    public void log(Level level, final String method, final String key) {
        logger.log(level, logMsg(cl, buildKey(method, key)));
    }    

    /**
     * Log a message, with associated <code>Throwable</code> information
     * from log messages catalog.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     * @param level  One of the message level identifiers, e.g., SEVERE.
     * @param method The message string method key.
     * @param key    The message string key.
     * @param thrown <code>Throwable</code> associated with log message.
     */
    public void log(final Level level, final String method, final String key,
            final Throwable thrown) {
        logger.log(level, logMsg(cl, buildKey(method, key)), thrown);
    }

    /**
     * Log a message with one object parameter from log messages catalog.
     * <p>
     * If the logger is currently enabled for the given message 
     * level then a corresponding LogRecord is created and forwarded 
     * to all the registered output Handler objects.
     * <p>
     * @param level  One of the message level identifiers, e.g. SEVERE.
     * @param method The message string method key.
     * @param key    The message string key.
     * @param param	 Parameter to the message.
     */
    public void log(final Level level, final String method, final String key,
            final Object param) {
        logger.log(level, logMsg(cl, buildKey(method, key)), param);
    }

    /**
     * Log a message with an array of object arguments
     * from log messages catalog.
     * <p>
     * If the logger is currently enabled for the given message 
     * level then a corresponding LogRecord is created and forwarded 
     * to all the registered output Handler objects.
     * <p>
     * @param level  One of the message level identifiers, e.g. SEVERE.
     * @param method The message string method key.
     * @param key    The message string key.
     * @param params Array of parameters to the message.
     */
    public void log(final Level level, final String method, final String key,
            final Object params[]) {
        logger.log(level, logMsg(cl, buildKey(method, key)), params);
    }

    /**
     * Log an exception message as is.
     * @param level   One of the message level identifiers, e.g. SEVERE.
     * @param message The message to be logged as is.
     */
    public void exception(final Level level, String message) {
        logger.log(level, message);
    }

}
