/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.settings.examples;

import java.util.Properties;

/**
 *
 * @author  Jan Pokorsky
 */
public final class JavaCompilerSetting {
    private final static String PROP_DEBUG = "debug"; //NOI18N
    private final static String PROP_DEPRECATION = "deprecation"; //NOI18N
    private final static String PROP_CLASS_PATH = "classPath"; //NOI18N
    private final static String PROP_EXEC_PATH = "path"; //NOI18N


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
