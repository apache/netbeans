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

package org.netbeans.modules.web.project.api;

import java.io.File;
import org.netbeans.api.j2ee.core.Profile;
import org.openide.filesystems.FileObject;

/**
 * Encapsulates data required for creating a new empty web project
 * or for creating a web project from existing sources.
 *
 * @author Radko Najman
 */
public final class WebProjectCreateData {

    //common to create and import project
    private File projectDir;
    private String name;
    private String serverInstanceID;
    private Profile javaEEProfile;
    private String sourceLevel;
    private String javaPlatformName;
    
    //only for create project
    private String contextPath;
    private String sourceStructure;
    
    //only for import project
    //(in the future we will need to add dd location and probably also conf dir
    // and setup dir locations)
    private FileObject wmFO;
    private File[] sourceFolders;
    private File[] testFolders;
    private FileObject docBase;
    private FileObject libFolder;
    private String buildfile;
    private FileObject webInfFolder;
    private boolean javaSourceBased = true;
    
    private String librariesDefinition;

    private boolean webXmlRequired;

    private boolean cdiEnabled;
    
    /**
     * Creates a new instance of WebProjectCreateData
     */
    public WebProjectCreateData() {
    }

    /**
     * Gets the project directory
     * @return the project directory
     */    
    public File getProjectDir() {
        return projectDir;
    }

    /**
     * Sets the project directory.
     * @param projectDir the project directory. Cannot be null.
     */    
    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    /**
     * Gets the project name
     * @return the project name
     */    
    public String getName() {
        return name;
    }

    /**
     * Sets the project name
     * @param name the project name. Cannot be null.
     */    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the server instance ID
     * @return the server instance ID
     */    
    public String getServerInstanceID() {
        return serverInstanceID;
    }

    /**
     * Sets the server instance ID
     * @param serverInstanceID the server instance ID. Cannot be null.
     */    
    public void setServerInstanceID(String serverInstanceID) {
        this.serverInstanceID = serverInstanceID;
    }

    public Profile getJavaEEProfile() {
        return javaEEProfile;
    }

    public void setJavaEEProfile(Profile javaEEProfile) {
        this.javaEEProfile = javaEEProfile;
    }


    /**
     * Gets Java EE version
     * @return Java EE version
     * @deprecated
     */    
    public String getJavaEEVersion() {
        return getJavaEEProfile().toPropertiesString();
    }

    /**
     * Sets Java EE version
     * @param javaEEVersion Java EE version. Cannot be null.
     * @deprecated
     */    
    public void setJavaEEVersion(String javaEEVersion) {
        setJavaEEProfile(Profile.fromPropertiesString(javaEEVersion));
    }

    /**
     * Gets the context path
     * @return the context path
     */    
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path
     * @param contextPath the context path
     */    
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Gets the source structure
     * @return the source structure ({@link WebProjectUtilities#SRC_STRUCT_BLUEPRINTS}
     * or {@link WebProjectUtilities#SRC_STRUCT_JAKARTA})
     */    
    public String getSourceStructure() {
        return sourceStructure;
    }

    /**
     * Sets the source structure
     * @param sourceStructure the source structure ({@link WebProjectUtilities#SRC_STRUCT_BLUEPRINTS}
     * or {@link WebProjectUtilities#SRC_STRUCT_JAKARTA}). Cannot be null.
     */    
    public void setSourceStructure(String sourceStructure) {
        this.sourceStructure = sourceStructure;
    }

    /**
     * Gets the file object representation of the imported web project location
     * @return the file object representation of the imported web project location
     */    
    public FileObject getWebModuleFO() {
        return wmFO;
    }

    /**
     * Sets the file object representation of the imported web project location
     * @param wmFO the file object representation of the imported web project location. Cannot be null.
     */    
    public void setWebModuleFO(FileObject wmFO) {
        this.wmFO = wmFO;
    }

    /**
     * Gets the array of source folders
     * @return the array of source folders
     */    
    public File[] getSourceFolders() {
        return sourceFolders != null ? sourceFolders.clone() : null;
    }

    /**
     * Sets the array of source folders
     * @param sourceFolders the array of source folders. Cannot be null.
     */    
    public void setSourceFolders(File[] sourceFolders) {
        this.sourceFolders = sourceFolders != null ? sourceFolders.clone() : null;
    }

    /**
     * Gets the array of test folders
     * @return the array of test folders
     */    
    public File[] getTestFolders() {
        return testFolders != null ? testFolders.clone() : testFolders;
    }

    /**
     * Sets the array of test folders
     * @param testFolders the array of test folders
     */    
    public void setTestFolders(File[] testFolders) {
        this.testFolders = testFolders != null ? testFolders.clone() : null;
    }

    /**
     * Gets the location of web pages
     * @return the location of web pages
     */    
    public FileObject getDocBase() {
        return docBase;
    }

    /**
     * Sets the location of web pages
     * @param docBase the location of web pages. Cannot be null.
     */    
    public void setDocBase(FileObject docBase) {
        this.docBase = docBase;
    }

    /**
     * Gets the libraries folder
     * @return the libraries folder
     */    
    public FileObject getLibFolder() {
        return libFolder;
    }

    /**
     * Sets the libraries folder
     * @param libFolder the libraries folder
     */    
    public void setLibFolder(FileObject libFolder) {
        this.libFolder = libFolder;
    }

    /**
     * Gets the WEB-INF folder
     * @return the WEB-INF folder
     */    
    public FileObject getWebInfFolder() {
        return webInfFolder;
    }

    /**
     * Sets the WEB-INF folder
     * @param webInfFolder the WEB-INF folder
     */    
    public void setWebInfFolder(FileObject webInfFolder) {
        this.webInfFolder = webInfFolder;
    }

    /**
     * Gets the build script file name
     * @return the build script file name
     */    
    public String getBuildfile() {
        return buildfile;
    }

    /**
     * Sets the build script file name
     * @param buildfile the build script file name
     */    
    public void setBuildfile(String buildfile) {
        this.buildfile = buildfile;
    }
    
    /**
     * Gets the source level
     * @return the source level (e.g. 1.4, 1.5)
     */    
    public String getSourceLevel() {
        return sourceLevel;
    }

    /**
     * Sets the source level
     * @param sourceLevel the source level (e.g. 1.4, 1.5)
     */    
    public void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    /**
     * Gets Java platform name
     * @return Java platform name (from Java Platform Manager)
     */    
    public String getJavaPlatformName() {
        return javaPlatformName;
    }

    /**
     * Sets Java platform name
     * @param javaPlatformName Java platform name (from Java Platform Manager)
     */    
    public void setJavaPlatformName(String javaPlatformName) {
        this.javaPlatformName = javaPlatformName;
    }

//    //Just a debug method to display how the properties are set.    
//    public void log() {
//        System.out.println("\nCREATE & IMPORT");
//        System.out.println("  projectDir: " + (projectDir == null ? "-" : projectDir.getPath()));
//        System.out.println("  name: " + name);
//        System.out.println("  serverInstanceID: " + serverInstanceID);
//        System.out.println("  javaEEVersion: " + javaEEVersion);
//        System.out.println("  contextPath: " + contextPath);
//        System.out.println("  sourceLevel: " + sourceLevel);
//        System.out.println("  javaPlatformName: " + javaPlatformName);
//
//        System.out.println("\nCREATE");
//        System.out.println("  sourceStructure: " + sourceStructure);
//
//        System.out.println("\nIMPORT");
//        System.out.println("  wmFO: " + (wmFO == null ? "-" : wmFO.getPath()));
//        if (sourceFolders != null) {
//            System.out.println("  sourceFolders: ");
//            for (int i = 0; i < sourceFolders.length; i++)
//                System.out.print(sourceFolders[i].getPath() + ", ");
//        }
//        if (testFolders != null) {
//            System.out.println("  testFolders: ");
//            for (int i = 0; i < testFolders.length; i++)
//                System.out.print(testFolders[i].getPath() + ", ");
//        }
//        System.out.println("  docBase: " + (docBase == null ? "-" : docBase.getPath()));
//        System.out.println("  libFolder: " + (libFolder == null ? "-" : libFolder.getPath()));
//        System.out.println("  buildfile: " + buildfile);
//    }

    /**
     * Indicate whether the project is being created from java sources.
     * @param b should the user be able to extend this project with additional 
     * source elements
     */
    public void setJavaSourceBased(boolean b) {
        this.javaSourceBased = b;
    }
    
    /**
     * Determine whether the project was created from java sources.
     * @return if the project supports the addition of java source files
     */
    public boolean getJavaSourceBased() {
        return this.javaSourceBased;
    }

    public String getLibrariesDefinition() {
        return librariesDefinition;
    }

    public void setLibrariesDefinition(String librariesDefinition) {
        this.librariesDefinition = librariesDefinition;
    }

    public boolean isWebXmlRequired() {
        return webXmlRequired;
    }

    public void setWebXmlRequired(boolean webXmlRequired) {
        this.webXmlRequired = webXmlRequired;
    }
    /**
     * As it's data for project creation
     * It's an action during project creation "is to enable cdi or not",
     * It doesn't mean if project have cdi enabled or not and may depend on more options (for ee7 no action to enable cdi is required)
     * TODO: should it be changed to "isCDIEnable" or similar?, check others projects except web for similar naming
     * @return true if cdi need to be enabled(usually by some additional action)
     */
    public boolean isCDIEnabled() {
        return cdiEnabled;
    }

    /**
     * As it's data for project creation
     * It's an action during project creation "is to enable cdi or not",
     * It doesn't mean if project have cdi enabled or not and may depend on more options (for ee7 no action to enable cdi is required)
     * @param cdiEnabled 
     */
    public void setCDIEnabled(boolean cdiEnabled) {
        this.cdiEnabled = cdiEnabled;
    }

    public boolean skipTests() {
        return testFolders == null || testFolders.length == 0;
    }

}
