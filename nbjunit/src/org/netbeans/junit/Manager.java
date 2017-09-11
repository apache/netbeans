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

package org.netbeans.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.junit.diff.Diff;

/**
 *
 * @author  vstejskal
 */
public class Manager extends Object {

    /** Creates new Manager */
    private Manager() {
    }

    public static final String JUNIT_PROPERTIES_FILENAME = "junit.properties";
    public static final String JUNIT_PROPERTIES_LOCATION_PROPERTY = "junit.properties.file";

    protected static final String PROP_DIFF_IMPL        = "nbjunit.diff.impl";
    protected static final String DEFAULT_DIFF_IMPL     = "org.netbeans.junit.diff.SimpleDiff";    
    protected static Diff systemDiff         = null;
    protected static Properties fPreferences            = null;
    
    
    // workdir stuff
        /**
     * name of the system property defining root workdir direcory
     * - it must be set before running tests using workdir
     * - in the case of running tests from XTest framework, this 
     *   property is set by the framework itself
     * - otherwise the default is ${java.io.tmpdir}/tests
     */
    public static final String NBJUNIT_WORKDIR = "nbjunit.workdir";
    
    // nbjunit home dir - directory where nbjunit.jar and other files are stored
    public static final String NBJUNIT_HOME = "nbjunit.home";
    
    
    static {
        fPreferences = new Properties();
        fPreferences.put(PROP_DIFF_IMPL, DEFAULT_DIFF_IMPL);
    }
    
    public static Diff getSystemDiff() {
        String diffImplName;
                
        if (null == systemDiff) {
            readProperties();
            diffImplName = fPreferences.getProperty(PROP_DIFF_IMPL);            
            systemDiff = instantiateDiffImpl(diffImplName);
            if (null == systemDiff && !diffImplName.equals(DEFAULT_DIFF_IMPL)) {
                systemDiff = instantiateDiffImpl(DEFAULT_DIFF_IMPL);
            }
        }        
        return systemDiff;
    }
    
    public static String getWorkDirPath() {
        String path = System.getProperty(NBJUNIT_WORKDIR);
                
        if (path == null) {            
            // try to get property from user's settings
            readProperties();            
            path = fPreferences.getProperty(NBJUNIT_WORKDIR);
        }
        if (path != null) {
            path = path.replace('/', File.separatorChar);
        } else {
            // Fallback value, guaranteed to be defined.
            path = System.getProperty("java.io.tmpdir") + File.separatorChar + "tests-" + System.getProperty("user.name");
        }
        return path;
    }
    
    public static String getNbJUnitHomePath() throws IOException {
        String path = System.getProperty(NBJUNIT_HOME);
                
        if (path == null) {            
            // try to get property from user's settings
            readProperties();            
            path = fPreferences.getProperty(NBJUNIT_HOME);
        }
        if (path != null) {
            path = path.replace('/', File.separatorChar);
            return path;
        } else {
            throw new IOException("Cannot determine NbJUnit home. Please make sure you have "+NBJUNIT_HOME
                        +" propery set in your "+JUNIT_PROPERTIES_FILENAME+" file.");
        }

    }    
    
    
    public static File getNbJUnitHome() throws IOException {
        File nbJUnitHome = normalizeFile(new File(getNbJUnitHomePath()));
        if (nbJUnitHome.isDirectory()) {            
            return nbJUnitHome;
        } else {
            throw new IOException("Property "+NBJUNIT_HOME+" does not point to nbjunit home.");
        }
    }
    
    protected static Diff instantiateDiffImpl(String diffImplName) {
        Diff     impl = null;
        Class<?>            clazz;
        Object              diffImpl = null;
        Class []            prmString = null;
        Method              method;
        Enumeration         propNames;
        
        try {
            prmString = new Class [] { Class.forName("java.lang.String") };
            
            // instantiate the diff class
            clazz = Class.forName(diffImplName);
            diffImpl = clazz.newInstance();

            if (diffImpl instanceof Diff) {
                impl = (Diff) diffImpl;
            
                propNames = fPreferences.propertyNames();
                while (propNames.hasMoreElements()) {
                    String propName = (String) propNames.nextElement();
                    
                    if (propName.equals(PROP_DIFF_IMPL) || !propName.startsWith(PROP_DIFF_IMPL))
                        continue;
                    
                    String setter = "set" + propName.substring(PROP_DIFF_IMPL.length() + 1);
                    try {
                        method = clazz.getMethod(setter, prmString);
                    }
                    catch (NoSuchMethodException e) {
                        System.out.println("The method " + setter + " not fond in class " + diffImplName + ".");
                        method = null;
                    }
                    if (null != method)
                        method.invoke(impl, new Object [] { fPreferences.getProperty(propName, "") });
                }
            }
        }
        catch (Exception e) {
            // ignore exception
        }
        return impl;
    }
    
    private static File getPreferencesFile() {
        String junitPropertiesLocation = System.getProperty(Manager.JUNIT_PROPERTIES_LOCATION_PROPERTY);
        if (junitPropertiesLocation != null) {
            File propertyFile = new File(junitPropertiesLocation);
            if (propertyFile.exists()) {
                return propertyFile;
            }
        }
        // property file was not found - lets fall back to defaults
        String home= System.getProperty("user.home");
        return new File(home, Manager.JUNIT_PROPERTIES_FILENAME);
    }
    
    protected static void readProperties() {
        try {
            File propFile = getPreferencesFile();
            InputStream is= new FileInputStream(propFile);
            fPreferences= new Properties(fPreferences);
            try {
                fPreferences.load(is);
            } finally {
                is.close();
            }
        } 
        catch (IOException e) {
            // ignore
        }
    }

    /**
     * Normalize java.io.File, that is make sure that returned File has
     * normalized case on Windows; that old Windows 8.3 filename is normalized;
     * that Unix symlinks are not followed; that relative path is changed to 
     * absolute; etc.
     * @param file file to normalize
     * @return normalized file
     */
    public static File normalizeFile(File file) {
        Runnable off = Log.internalLog();
        try {
            // taken from org.openide.util.FileUtil
            if (System.getProperty ("os.name").startsWith("Windows")) { // NOI18N
                // On Windows, best to canonicalize.
                try {
                    file = file.getCanonicalFile();
                } catch (IOException e) {
                    Logger.getLogger(Manager.class.getName()).warning("getCanonicalFile() on file " + file + " failed: " + e);
                    // OK, so at least try to absolutize the path
                    file = file.getAbsoluteFile();
                }
            } else {
                // On Unix, do not want to traverse symlinks.
                file = new File(file.toURI().normalize()).getAbsoluteFile();
            }
            return file;
        } finally {
            off.run();
        }
    }
}
