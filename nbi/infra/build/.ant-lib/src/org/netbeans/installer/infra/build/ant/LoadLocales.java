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

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which is capable of loading localized properties data.
 * 
 * @author Kirill Sorokin
 */
public class LoadLocales extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The basename of the .properties file.
     */
    private String basename;
    
    /**
     * Name of the property whose value should be set to the list of found locales.
     */
    private String localesList;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'basename' property.
     * 
     * @param basename New value for the 'basename' property.
     */
    public void setBasename(final String basename) {
        this.basename = basename;
        final File basenameFile = new File(basename);
        if(! (basenameFile.equals(basenameFile.getAbsoluteFile()))) {
		this.basename = new File(getProject().getBaseDir(), basename).getPath();
        }
    }
    
    /**
     * Setter for the 'localesList' property.
     * 
     * @param localesList New value for the 'localesList' property.
     */
    public void setList(final String localesList) {
        this.localesList = localesList;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The properties are loaded from the resource file with the
     * given base name and the corresponding project properties are set. 
     * 
     * @throws org.apache.tools.ant.BuildException if an I/O error occurs.
     */
    public void execute() throws BuildException {
        Utils.setProject(getProject());
        
        String locales = ""; // NOI18N
        
        try {
            // handle the default locale
            File file  = new File(basename + ".properties"); // NOI18N
            
            Properties properties = new Properties();
            
            properties.load(new FileInputStream(file));

            for (Map.Entry<Object, Object> entry: properties.entrySet()) {
                getProject().setProperty(
                        entry.getKey() + ".default", // NOI18N
                        entry.getValue().toString());
            }
            
            for (Locale locale: Locale.getAvailableLocales()) {
                file = new File(basename + "_" + locale + ".properties"); // NOI18N
                if (file.exists()) {
                    locales += " " + locale; // NOI18N
                    properties = new Properties();
                    properties.load(new FileInputStream(file));

                    for (Map.Entry<Object, Object> entry: properties.entrySet()) {
                        getProject().setProperty(
                                "" + entry.getKey() + "." + locale, // NOI18N
                                entry.getValue().toString());
                    }
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
        
        getProject().setProperty(localesList, locales.trim());
    }
}
