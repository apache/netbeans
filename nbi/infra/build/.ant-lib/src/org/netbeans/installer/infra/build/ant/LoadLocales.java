/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
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
            for (Object key: properties.keySet()) {
                getProject().setProperty(
                        key + ".default", // NOI18N
                        properties.get(key).toString());
            }
            
            for (Locale locale: Locale.getAvailableLocales()) {
                file = new File(basename + "_" + locale + ".properties"); // NOI18N
                if (file.exists()) {
                    locales += " " + locale; // NOI18N
                    properties = new Properties();
                    properties.load(new FileInputStream(file));
                    
                    for (Object key: properties.keySet()) {
                        getProject().setProperty(
                                "" + key + "." + locale, // NOI18N
                                properties.get(key).toString());
                    }
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
        
        getProject().setProperty(localesList, locales.trim());
    }
}
