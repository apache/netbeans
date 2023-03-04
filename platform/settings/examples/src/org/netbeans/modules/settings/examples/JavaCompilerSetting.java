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

package org.netbeans.modules.settings.examples;

import java.util.Properties;

/**
 *
 * @author  Jan Pokorsky
 */
public final class JavaCompilerSetting {
    private static final String PROP_DEBUG = "debug"; //NOI18N
    private static final String PROP_DEPRECATION = "deprecation"; //NOI18N
    private static final String PROP_CLASS_PATH = "classPath"; //NOI18N
    private static final String PROP_EXEC_PATH = "path"; //NOI18N


    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
    
    private boolean debug;
    private boolean deprecation;
    private String classpath = ""; //NOI18N
    private String path = ""; //NOI18N
    
    public JavaCompilerSetting() {
    }
    
    /** Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    private void readProperties(Properties p) {
        this.classpath = p.getProperty(PROP_CLASS_PATH);
        this.path = p.getProperty(PROP_EXEC_PATH);
        String bool = p.getProperty(PROP_DEBUG);
        if (bool != null)
            this.debug = Boolean.valueOf(bool).booleanValue();
        else
            this.debug = false;
                
        bool = p.getProperty(PROP_DEPRECATION);
        if (bool != null)
            this.deprecation = Boolean.valueOf(bool).booleanValue();
        else
            this.deprecation = false;
    }
    
    private void writeProperties(Properties p) {
        p.setProperty(PROP_CLASS_PATH, getClasspath());
        p.setProperty(PROP_EXEC_PATH, getPath());
        p.setProperty(PROP_DEPRECATION, String.valueOf(isDeprecation()));
        p.setProperty(PROP_DEBUG, String.valueOf(isDebug()));
    }
    
    public boolean isDebug() {
        return this.debug;
    }
    public void setDebug(boolean debug) {
        boolean oldDebug = this.debug;
        this.debug = debug;
        propertyChangeSupport.firePropertyChange(PROP_DEBUG, oldDebug, debug);
    }
    public boolean isDeprecation() {
        return this.deprecation;
    }
    public void setDeprecation(boolean deprecation) {
        boolean oldDeprecation = this.deprecation;
        this.deprecation = deprecation;
        propertyChangeSupport.firePropertyChange(PROP_DEPRECATION, oldDeprecation, deprecation);
    }
    public String getClasspath() {
        return this.classpath;
    }
    public void setClasspath(String classpath) {
        String oldClasspath = this.classpath;
        this.classpath = classpath;
        propertyChangeSupport.firePropertyChange(PROP_CLASS_PATH, oldClasspath, classpath);
    }
    public String getPath() {
        return this.path;
    }
    public void setPath(String path) {
        String oldPath = this.path;
        this.path = path;
        propertyChangeSupport.firePropertyChange(PROP_EXEC_PATH, oldPath, path);
    }
    
}
