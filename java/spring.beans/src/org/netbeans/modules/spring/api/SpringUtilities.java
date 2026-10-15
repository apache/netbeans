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

package org.netbeans.modules.spring.api;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public final class SpringUtilities {

    public static final String SPRING_CLASS_NAME = "org.springframework.core.SpringVersion"; // NOI18N
    private static final String JSTL_CLASS_NAME = "javax.servlet.jsp.jstl.core.Config"; // NOI18N
    private static final String SPRING_WEBMVC_CLASS_NAME = "org.springframework.web.servlet.DispatcherServlet"; // NOI18N
//    private static final String SPRING_WEBMVC_3_0_SPECIFIC_CLASS_NAME="org.springframework.web.servlet.config.MvcNamespaceHandler"; //NOI18N
//    private static final String SPRING_WEBMVC_2_5_SPECIFIC_CLASS_NAME="org.springframework.web.servlet.mvc.throwaway.ThrowawayController"; //NOI18N

    private SpringUtilities() {}

    public static Library findSpringLibrary() {
        return getLibrary(SPRING_CLASS_NAME);
    }

    public static Library findJSTLibrary() {
        // see Issue #169105 - first try "pure" JSTL, if there isn't any then bundled JSTL will suffice
        for (Library library : getJavaLibraries()) {
            if (library.getName().startsWith("jstl")) {
                return library;
            }
        }
        return getLibrary(JSTL_CLASS_NAME);
    }

    public static Library findSpringWebMVCLibrary() {
        return getLibrary(SPRING_WEBMVC_CLASS_NAME);
    }

    public static Library findSpringWebMVCLibrary(String version) {
        for (Library library : getJavaLibraries()) {
            if (containsClass(library, SPRING_WEBMVC_CLASS_NAME)) {
                if (version.equalsIgnoreCase(getSpringWebMVCLibraryVersion(library))) {
                    return library;
                }
            }
        }
        return null;
    }

    public static boolean isSpringLibrary(Library library) {
        if (!"j2se".equals(library.getType())) { //NOI18N
            return false;
        }
        return containsClass(library, SPRING_CLASS_NAME);
    }

    public static boolean isSpringWebMVCLibrary(Library library) {
        if (!"j2se".equals(library.getType())) { //NOI18N
            return false;
        }
        return containsClass(library, SPRING_WEBMVC_CLASS_NAME);
    }

    /**
     * Gets array of all libraries of type "j2se".
     * @return array of all J2SE libraries
     */
    public static Library[] getJavaLibraries() {
        List<Library> libraries = new LinkedList<Library>();
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(library.getType())) { //NOI18N
                continue;
            } else {
                libraries.add(library);
            }
        }
        return libraries.toArray(new Library[0]);
    }

    /**
     * Get implementation version of the library containing org.springframework.core.SpringVersion class
     * or null
     * @param library Spring framework library
     * @return String version
     */
    public static String getSpringLibraryVersion(Library library) {
        return getLibraryVersion(library, SPRING_CLASS_NAME);
    }

    private static String getSpringWebMVCLibraryVersion(Library library) {
        return getLibraryVersion(library, SPRING_WEBMVC_CLASS_NAME);
    }

    private static String getLibraryVersion(Library library, String className) {
        List<URL> urls = library.getContent("classpath"); // NOI18N
        ClassPath cp = createClassPath(urls);
        try {
            FileObject resource = cp.findResource(className.replace('.', '/') + ".class");  //NOI18N
            if (resource==null) {
                return null;
            }
            FileObject ownerRoot = cp.findOwnerRoot(resource);

            if (ownerRoot !=null) { //NOI18N
                if (ownerRoot.getFileSystem() instanceof JarFileSystem) {
                    JarFileSystem jarFileSystem = (JarFileSystem) ownerRoot.getFileSystem();
                    return getImplementationVersion(jarFileSystem);
                }
            }
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    public static String getImplementationVersion(JarFileSystem jarFile) {
        Manifest manifest = jarFile.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        return attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
    }

    public static boolean containsSpring(ClassPath cp) {
        return containsClass(cp, SPRING_CLASS_NAME);
    }

    private static Library getLibrary(String className) {
        for (Library library : getJavaLibraries()) {
            if (containsClass(library, className)) {
                return library;
            }
        }
        return null;
    }

    private static boolean containsClass(Library library, String className) {
        List<URL> urls = library.getContent("classpath"); // NOI18N
        return containsClass(createClassPath(urls), className);
    }

    private static boolean containsClass(ClassPath classPath, String className) {
        String classRelativePath = className.replace('.', '/') + ".class"; //NOI18N
        return classPath.findResource(classRelativePath) != null;
    }

    private static ClassPath createClassPath(List<URL> roots) {
        List<URL> jarRootURLs = new ArrayList<URL>();
        for (URL url : roots) {
            // Workaround for #126307: ClassPath roots should be JAR root URL, not file URLs.
            if (FileUtil.getArchiveFile(url) == null) {
                // Not an archive root URL.
                url = FileUtil.getArchiveRoot(url);
            }
            jarRootURLs.add(url);
        }
        return ClassPathSupport.createClassPath((jarRootURLs.toArray(new URL[0])));
    }
}
