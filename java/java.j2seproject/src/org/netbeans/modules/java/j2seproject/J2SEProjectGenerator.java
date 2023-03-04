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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder;
import org.netbeans.spi.java.project.support.PreferredProjectPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Parameters;

/**
 * Creates a J2SEProject from scratch according to some initial configuration.
 */
public class J2SEProjectGenerator {
    
    private J2SEProjectGenerator() {}
    
    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @param librariesDefinition project relative or absolute OS path to libraries definition; can be null
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(final File dir, final String name, final String mainClass, 
            final String manifestFile, final String librariesDefinition, boolean skipTests) throws IOException {
        Parameters.notNull("dir", dir); //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        return new J2SEProjectBuilder (dir, name).
                addDefaultSourceRoots().
                skipTests(skipTests).
                setMainClass(mainClass).
                setManifest(manifestFile).
                setLibrariesDefinitionFile(librariesDefinition).
                setSourceLevel(defaultSourceLevel).
                setJavaPlatform(getPlatform()).
                build();
    }

    public static AntProjectHelper createProject(final File dir, final String name,
                                                  final File[] sourceFolders, final File[] testFolders, 
                                                  final String manifestFile, final String librariesDefinition,
                                                  final String buildXmlName) throws IOException {
        Parameters.notNull("dir", dir); //NOI18N
        Parameters.notNull("name", name);   //NOI8N
        Parameters.notNull("sourceFolders", sourceFolders); //NOI18N
        Parameters.notNull("testFolders", testFolders); //NOI18N
        return new J2SEProjectBuilder(dir, name).
                addSourceRoots(sourceFolders).
                addTestRoots(testFolders).
                skipTests(testFolders.length == 0).
                setManifest(manifestFile).
                setLibrariesDefinitionFile(librariesDefinition).
                setBuildXmlName(buildXmlName).
                setSourceLevel(defaultSourceLevel).
                setJavaPlatform(getPlatform()).
                build();
    }
    
    private static JavaPlatform getPlatform() {
        return PreferredProjectPlatform.getPreferredPlatform(
            JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getName());
    }
                    
    //------------ Used by unit tests -------------------
    private static SpecificationVersion defaultSourceLevel;
                
    /**
     * Unit test only method. Sets the default source level for tests
     * where the default platform is not available.
     * @param version the default source level set to project when it is created
     *
     */
    public static void setDefaultSourceLevel (SpecificationVersion version) {
        defaultSourceLevel = version;
    }
}


