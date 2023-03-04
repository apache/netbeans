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
package org.netbeans.tests.xml;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.jellytools.JellyTestCase;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;
import org.openide.util.io.NullOutputStream;

/**
 * Provides the basic support for XML API tests.
 * @author mschovanek
 */
public abstract class XTest extends JellyTestCase {
    public final String CATALOG_BUNDLE   = "org.netbeans.modules.xml.catalog.resources.Bundle";
    public final String CORE_BUNDLE      = "org.netbeans.modules.xml.core.resources.Bundle";
    public final String CSS_BUNDLE       = "org.netbeans.modules.css.resources.Bundle";
    public final String TAX_BUNDLE       = "org.netbeans.tax.resources.Bundle";
    public final String TEXT_BUNDLE      = "org.netbeans.modules.xml.text.resources.Bundle";
    public final String TOOLS_BUNDLE     = "org.netbeans.modules.xml.tools.resources.Bundle";
    public final String TREE_BUNDLE      = "org.netbeans.modules.xml.tree.resources.Bundle";
    
    protected String packageName;
    protected String absolutePath;
    protected String fsName;
    
    /** debug test output */
    protected PrintWriter dbg = new PrintWriter(new NullOutputStream());
    
    /** debug switch */
    protected static boolean DEBUG = false;
    private static boolean LOG_INTO_CONSOLE = false;
    
    public XTest(String testName) {
        super(testName);
    }
    
    
    protected void deleteData(String path) {
        try {
        DataObject dao = TestUtil.THIS.findData(path);
        if (dao != null) dao.delete();
        } catch (Exception ex) {}
    }

    /** @depricated use getPackegeName() */
    protected String packageName() {
        return  getPackageName();
    }

    /** Returns test's package name. */
    protected String getPackageName() {
        if (packageName == null) {
            packageName = this.getClass().getPackage().getName();
        }
        return packageName;
    }

    /** Returns test's package name delimited by 'separator'. */
    protected String getPackageName(String separator) {
        String name = getPackageName();
        name = name.replace(".", separator);
        return name;
    }

    /** Returns data package name  delimited by 'separator'. */
    protected String getDataPackageName(String separator) {
        String name = getPackageName(separator);
        name += separator + "data";
        return name;
    }

    /** Returns absolute test folder path. **/
    protected String getAbsolutePath() {
        if (absolutePath == null) {
            String url = this.getClass().getResource("").toExternalForm();
            absolutePath = TestUtil.toAbsolutePath(TestUtil.findFileObject(url));
        }
        return absolutePath;
    }

    /** Returns test's filesystem display name */
    protected String getFilesystemName() throws FileStateInvalidException {
        if (fsName == null) {
            fsName = TestUtil.findFileObject(packageName(), Utilities.getShortClassName(this.getClass()), "class").getFileSystem().getDisplayName();
        }
        return fsName;
    }
    
    /** Returns default log. @see super#getLog() , @see #logIntoConsole(boolean) */
    public PrintStream getLog() {
        if (LOG_INTO_CONSOLE) {
            return System.out;
        } else {
            return super.getLog();
        }
    }
    
    /** Simple and easy to use method for printing a message to a default log
     * @param message meesage to log
     */    
    public void log(String message) {
        getLog().println(message);
    }
    
    /** Simple and easy to use method for printing a message to a default log
     * @param message meesage to log
     */    
    public void log(String message, Exception ex) {
        getLog().println(message);
        ex.printStackTrace(getLog());
    }
    
    /** Redirects log into console. @see #getLog() */
    public static void logIntoConsole(boolean console) {
        LOG_INTO_CONSOLE = console;
    }
    
    /** Causes the test to sleep for the specified nmuber of milliseconds.
     *  Does not throw any exception.
     */
    protected static void sleepTest(long millis) {
        try {
            Thread.currentThread().sleep(millis);
        } catch (Exception e) { /* do nothig */}
    }
}
