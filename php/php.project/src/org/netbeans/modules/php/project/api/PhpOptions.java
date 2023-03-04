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

package org.netbeans.modules.php.project.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.*;

/**
 * Helper class to get actual PHP properties like debugger port etc.
 * Use {@link #getInstance()} to get class instance.
 * <p>
 * Since 1.4 it is possible to listen to changes in particular PHP options.
 * @author Tomas Mysik
 * @since 1.2
 */
public final class PhpOptions {
    public static final String PROP_PHP_INTERPRETER = "propPhpInterpreter"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_PORT = "propPhpDebuggerPort"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_SESSION_ID = "propPhpDebuggerSessionId"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_STOP_AT_FIRST_LINE = "propPhpDebuggerStopAtFirstLine"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_WATCHES_AND_EVAL = "propPhpDebuggerWatchesAndEval"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_MAX_STRUCTURES_DEPTH = "propPhpDebuggerMaxStructuresDepth"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_MAX_CHILDREN = "propPhpDebuggerMaxChildren"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_SHOW_URLS = "propPhpDebuggerShowUrls"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_SHOW_CONSOLE = "propPhpDebuggerShowConsole"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_RESOLVE_BREAKPOINTS = "propPhpDebuggerResolveBreakpoints"; // NOI18N
    public static final String PROP_PHP_GLOBAL_INCLUDE_PATH = "propPhpGlobalIncludePath"; // NOI18N

    private static final PhpOptions INSTANCE = new PhpOptions();

    private final PropertyChangeSupport propertyChangeSupport;

    private PhpOptions() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        getPhpOptions().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                String key = evt.getKey();
                String newValue = evt.getNewValue();
                if (PHP_INTERPRETER.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_INTERPRETER, null, newValue);
                } else if (PHP_DEBUGGER_PORT.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_PORT, null, Integer.valueOf(newValue));
                } else if (PHP_DEBUGGER_SESSION_ID.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_SESSION_ID, null, newValue);
                } else if (PHP_DEBUGGER_STOP_AT_FIRST_LINE.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_STOP_AT_FIRST_LINE, null, Boolean.valueOf(newValue));
                } else if (PHP_DEBUGGER_WATCHES_AND_EVAL.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_WATCHES_AND_EVAL, null, Boolean.valueOf(newValue));
                } else if (PHP_DEBUGGER_MAX_STRUCTURES_DEPTH.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_MAX_STRUCTURES_DEPTH, null, Boolean.valueOf(newValue));
                } else if (PHP_DEBUGGER_MAX_CHILDREN.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_MAX_CHILDREN, null, Boolean.valueOf(newValue));
                } else if (PHP_DEBUGGER_SHOW_URLS.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_SHOW_URLS, null, Boolean.valueOf(newValue));
                } else if (PHP_DEBUGGER_SHOW_CONSOLE.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_SHOW_CONSOLE, null, Boolean.valueOf(newValue));
                } else if (PHP_DEBUGGER_RESOLVE_BREAKPOINTS.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_RESOLVE_BREAKPOINTS, null, Boolean.valueOf(newValue));
                } else if (PHP_GLOBAL_INCLUDE_PATH.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_GLOBAL_INCLUDE_PATH, null, newValue);
                }
            }
        });
    }

    public static PhpOptions getInstance() {
        return INSTANCE;
    }

    private org.netbeans.modules.php.project.ui.options.PhpOptions getPhpOptions() {
        return org.netbeans.modules.php.project.ui.options.PhpOptions.getInstance();
    }

    /**
     * Get the PHP interpreter file path.
     * @return the PHP interpreter file path or <code>null</code> if none is found.
     */
    public String getPhpInterpreter() {
        return getPhpOptions().getPhpInterpreter();
    }

    /**
     * Get the port for PHP debugger, the default is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_PORT}</code>.
     * @return the port for PHP debugger.
     */
    public int getDebuggerPort() {
        return getPhpOptions().getDebuggerPort();
    }

    /**
     * Get the session ID for PHP debugger, the default is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_SESSION_ID}</code>.
     * @return the session ID for PHP debugger.
     */
    public String getDebuggerSessionId() {
        return getPhpOptions().getDebuggerSessionId();
    }

    /**
     * Get the maximum data length to be retrieved from PHP debugger, the default is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_MAX_DATA_LENGTH}</code>.
     * @return the maximum data length to be retrieved from PHP debugger.
     * @since 2.87
     */
    public int getDebuggerMaxDataLength() {
        return getPhpOptions().getDebuggerMaxDataLength();
    }

    /**
     * Get the maximum depth of structures to be retrieved from PHP debugger, the default is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_MAX_STRUCTURES_DEPTH}</code>.
     * @return the maximum depth of structures to be retrieved from PHP debugger.
     * @since 2.33
     */
    public int getDebuggerMaxStructuresDepth() {
        return getPhpOptions().getDebuggerMaxStructuresDepth();
    }

    /**
     * Get the maximum number of children to be retrieved from PHP debugger, the default is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_MAX_CHILDREN}</code>.
     * @return the maximum number of children to be retrieved from PHP debugger.
     * @since 2.33
     */
    public int getDebuggerMaxChildren() {
        return getPhpOptions().getDebuggerMaxChildren();
    }

    /**
     * Check whether debugger stops at the first line of a PHP script. The default value is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_STOP_AT_FIRST_LINE}</code>.
     * @return <code>true</code> if the debugger stops at the first line of a PHP script, <code>false</code> otherwise.
     */
    public boolean isDebuggerStoppedAtTheFirstLine() {
        return getPhpOptions().isDebuggerStoppedAtTheFirstLine();
    }

    /**
     * Check whether debugger allows to use watches and balloon evaluation. The default value is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_WATCHES_AND_EVAL}</code>.
     * @return <code>true</code> if the debugger allows to use watches and balloon evaluation, <code>false</code> otherwise.
     * @since 2.25
     */
    public boolean isDebuggerWatchesAndEval() {
        return getPhpOptions().isDebuggerWatchesAndEval();
    }

    /**
     * Check whether debugger shows requested URLs to the Output Window. The default value is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_SHOW_URLS}</code>.
     * @return <code>true</code> if the debugger shows requested URLs, <code>false</code> otherwise.
     * @since 2.35
     */
    public boolean isDebuggerShowRequestedUrls() {
        return getPhpOptions().isDebuggerShowUrls();
    }

    /**
     * Check whether debugger shows debugger console to the Output Window. The default value is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_SHOW_CONSOLE}</code>.
     * @return <code>true</code> if the debugger shows debugger console, <code>false</code> otherwise.
     * @since 2.35
     */
    public boolean isDebuggerShowDebuggerConsole() {
        return getPhpOptions().isDebuggerShowConsole();
    }

    /**
     * Check whether debugger requests breakpoint resolution. The default value is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_RESOLVE_BREAKPOINTS}</code>.
     * @return <code>true</code> if the debugger requests breakpoint resolution, <code>false</code> otherwise.
     * @since 2.149
     */
    public boolean isDebuggerResolveBreakpoints() {
        return getPhpOptions().isDebuggerResolveBreakpoints();
    }

    /**
     * Get the global PHP include path.
     * @return the global PHP include path or an empty String if no folders are defined.
     * @see #getPhpGlobalIncludePathAsArray()
     */
    public String getPhpGlobalIncludePath() {
        return getPhpOptions().getPhpGlobalIncludePath();
    }

    /**
     * Get the global PHP include path as array of strings.
     * @return the global PHP include path as array or an empty array of strings if no folders are defined.
     * @see #getPhpGlobalIncludePath()
     * @since 2.7
     */
    public String[] getPhpGlobalIncludePathAsArray() {
        return PropertyUtils.tokenizePath(getPhpGlobalIncludePath());
    }

    /**
     * @since 1.4
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @since 1.4
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
