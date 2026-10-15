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

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 * @since 1.21
 */
public final class SharabilityUtility {

    public static final String DEFAULT_LIBRARIES_FILENAME = "nblibraries.properties";

    private SharabilityUtility() {
        super();
    }

    public static String getLibraryLocation(String librariesDir) {
        String librariesDefinition = librariesDir;
        if (librariesDefinition != null) {
            if (!librariesDefinition.endsWith(File.separator)) {
                librariesDefinition += File.separatorChar;
            }
            librariesDefinition += SharabilityUtility.DEFAULT_LIBRARIES_FILENAME;
        }
        return librariesDefinition;
    }

    /**
     * Method makes sure that sharable project always has a correct version of 
     * CopyLibs library. As described in issue 146736 CopyLibs library
     * was enhanced in NetBeans version 6.5 and needs to be automatically upgraded
     * which is ensured by this method as well.
     * @since org.netbeans.modules.java.api.common/1 1.5
     */
    public static void makeSureProjectHasCopyLibsLibrary(final AntProjectHelper helper, final ReferenceHelper refHelper) {
        if (!helper.isSharableProject() || refHelper.getProjectLibraryManager() == null) {
            return;
        }
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run()  {
                Library lib = refHelper.getProjectLibraryManager().getLibrary("CopyLibs");
                if (lib == null) {
                    try {
                        refHelper.copyLibrary(LibraryManager.getDefault().getLibrary("CopyLibs")); // NOI18N
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    // #146736 - check that NB6.5 version of CopyLibs is available:
                    List<URL> roots = lib.getContent("classpath"); // NOI18N
                    // CopyFiles.class was not present in NB 6.1
                    boolean version61 = org.netbeans.spi.java.classpath.support.ClassPathSupport.
                            createClassPath(roots.toArray(new URL[0])).
                            findResource("org/netbeans/modules/java/j2seproject/copylibstask/CopyFiles.class") == null; // NOI18N
                    if (!version61) {
                        return;
                    }
                    // update 6.1 version of CopyLibs library to the latest one:
                    try {
                        refHelper.getProjectLibraryManager().removeLibrary(lib);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    // perform removal of library files in separate try/catch:
                    // if removal fails we can still add CopyLibs library
                    try {
                        FileObject parent = null;
                        for (URL u : roots) {
                            URL u2 = FileUtil.getArchiveFile(u);
                            if (u2 != null) {
                                u = u2;
                            }
                            FileObject fo = URLMapper.findFileObject(u);
                            if (fo != null) {
                                if (parent == null) {
                                    parent = fo.getParent();
                                }
                                fo.delete();
                            }
                        }
                        if (parent != null && parent.getChildren().length == 0 && parent.getNameExt().equals("CopyLibs")) { // NOI18N
                            parent.delete();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    try {
                        // this should recreate latest version of library
                        refHelper.copyLibrary(LibraryManager.getDefault().getLibrary("CopyLibs")); // NOI18N
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }
    
}
