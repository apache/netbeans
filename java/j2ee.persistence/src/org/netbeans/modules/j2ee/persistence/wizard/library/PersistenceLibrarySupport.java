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
package org.netbeans.modules.j2ee.persistence.wizard.library;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

/**
 * Various stuff copied mostly from org.netbeans.modules.project.libraries,
 * because it is not possible to add library to Library manager throught some API.
 */
public class PersistenceLibrarySupport {

    public static final String VOLUME_TYPE_CLASSPATH = "classpath";       //NOI18N
    public static final String VOLUME_TYPE_SRC = "src";       //NOI18N
    public static final String VOLUME_TYPE_JAVADOC = "javadoc";       //NOI18N
    public static final String LIBRARY_TYPE = "j2se";       //NOI18N
    static final String[] VOLUME_TYPES = new String[]{
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC,};
    private static final String LIBRARIES_REPOSITORY = "org-netbeans-api-project-libraries/Libraries";  //NOI18N
    private static int MAX_DEPTH = 3;
    private FileObject storage = null;
    private static PersistenceLibrarySupport instance;

    private PersistenceLibrarySupport() {
    }

    public static PersistenceLibrarySupport getDefault() {
        if (instance == null) {
            instance = new PersistenceLibrarySupport();
        }
        return instance;
    }

    public void addLibrary(LibraryImplementation library) {
        this.initStorage();
        assert this.storage != null : "Storage is not initialized";
        try {
            writeLibrary(this.storage, library);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static final FileObject createStorage() {
        try {
            return FileUtil.createFolder(FileUtil.getConfigRoot(), LIBRARIES_REPOSITORY);
        } catch (IOException e) {
            return null;
        }
    }

    private synchronized void initStorage() {
        if (this.storage == null) {
            this.storage = createStorage();
        }
    }

    private void writeLibrary(final FileObject storage, final LibraryImplementation library) throws IOException {
        storage.getFileSystem().runAtomicAction( () -> {
            FileObject fo = storage.createData(library.getName(), "xml");   //NOI18N
            writeLibraryDefinition(fo, library);
        });
    }

    private static void writeLibraryDefinition(final FileObject definitionFile, final LibraryImplementation library) throws IOException {
        try (FileLock lock = definitionFile.lock();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(definitionFile.getOutputStream(lock), StandardCharsets.UTF_8))) {
            
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");      //NOI18N
            out.println("<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">"); //NOI18N
            out.println("<library version=\"1.0\">");       			//NOI18N
            out.println("\t<name>" + library.getName() + "</name>");        //NOI18N
            out.println("\t<type>" + library.getType() + "</type>");
            String description = library.getDescription();
            if (description != null && description.length() > 0) {
                out.println("\t<description>" + description + "</description>");   //NOI18N
            }
            String localizingBundle = library.getLocalizingBundle();
            if (localizingBundle != null && localizingBundle.length() > 0) {
                out.println("\t<localizing-bundle>" + XMLUtil.toElementContent(localizingBundle) + "</localizing-bundle>");   //NOI18N
            }
            String[] volumeTypes = VOLUME_TYPES;
            for (int i = 0; i < volumeTypes.length; i++) {
                out.println("\t<volume>");      //NOI18N
                out.println("\t\t<type>" + volumeTypes[i] + "</type>");   //NOI18N
                List volume = library.getContent(volumeTypes[i]);
                if (volume != null) {
                    //If null -> broken library, repair it.
                    for (Iterator eit = volume.iterator(); eit.hasNext();) {
                        URL url = (URL) eit.next();
                        out.println("\t\t<resource>" + XMLUtil.toElementContent(url.toExternalForm()) + "</resource>"); //NOI18N
                    }
                }
                out.println("\t</volume>");     //NOI18N
            }
            out.println("</library>");  //NOI18N
        }
    }

    // from org.netbeans.modules.java.j2seproject.queries.JavadocForBinaryQueryImpl
    /**
     * Tests if the query accepts the root as valid JavadocRoot,
     * the query accepts the JavaDoc root, if it can find the index-files
     * or index-all.html in the root.
     * @param rootURL the javadoc root
     * @return true if the root is a valid Javadoc root
     */
    public static boolean isValidLibraryJavadocRoot(final URL rootURL) {
        assert rootURL != null && rootURL.toExternalForm().endsWith("/");
        final FileObject root = URLMapper.findFileObject(rootURL);
        if (root == null) {
            return false;
        }
        return findIndexFolder(root, 1) != null;
    }

    private static FileObject findIndexFolder(FileObject fo, int depth) {
        if (depth > MAX_DEPTH) {
            return null;
        }
        if (fo.getFileObject("index-files", null) != null || fo.getFileObject("index-all.html", null) != null) {  //NOI18N
            return fo;
        }
        FileObject[] children = fo.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isFolder()) {
                FileObject result = findIndexFolder(children[i], depth + 1);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     *@return true if the given classpath contains a class with the given name.
     */
    private static boolean containsClass(ClassPath cp, String className) {
        String classRelativePath = className.replace('.', '/') + ".class"; //NOI18N
        return cp.findResource(classRelativePath) != null;
    }

    /**
     *@return true if the given library contains a service with the given name.
     */
    public static boolean containsService(Library library, String serviceName) {
        String serviceRelativePath = "META-INF/services/" + serviceName; //NOI18N
        return containsPath(library.getContent("classpath"), serviceRelativePath); //NOI18N
    }

    /**
     *@return true if the given library contains a class with the given name.
     */
    public static boolean containsClass(LibraryImplementation library, String className) {
        String classRelativePath = className.replace('.', '/') + ".class"; //NOI18N
        return containsPath(library.getContent("classpath"), classRelativePath); //NOI18N
    }

    /**
     *@return true if the given library contains a service with the given name.
     */
    public static boolean containsService(LibraryImplementation library, String serviceName) {
        String serviceRelativePath = "META-INF/services/" + serviceName; //NOI18N
        return containsPath(library.getContent("classpath"), serviceRelativePath); //NOI18N
    }

    private static boolean containsPath(List<URL> roots, String relativePath) {
        ClassPath cp = ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
        return cp.findResource(relativePath) != null;
    }

    private static ClassPath getLibraryClassPath(Library library) {
        List<URL> urls = library.getContent("classpath"); //NOI18N
        URL[] result = urls.toArray(new URL[0]);
        return ClassPathSupport.createClassPath(result);
    }

    /**
     * @return the library in which given persistence unit's provider
     * is defined, or null none could be found.
     */
    public static Library getLibrary(PersistenceUnit pu) {
        return getLibrary(ProviderUtil.getProvider(pu));
    }

    /**
     * @return the library in which given provider
     * is defined, or null none could be found.
     */
    public static Library getLibrary(Provider provider) {
        List<ProviderLibrary> libraries = createLibraries(provider!=null ? provider.getProviderClass() : null);
        for (ProviderLibrary each : libraries) {
            if (provider.equals(each.getProvider())) {
                return each.getLibrary();
            }
        }
        return null;
    }

    /**
     *
     * add jdbc driver jdbc driver
     * called in separate rp request
     * method is DISABLED and do nothing for now
     */
    public static void addDriver(final Project project, final JDBCDriver driver) {
        if(true) {
            return;
        }
        RequestProcessor.getDefault().post( () -> {
            Sources sources = ProjectUtils.getSources(project);
            if (sources == null) {
                return;
            }
            SourceGroup groups[] = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (groups == null || groups.length < 1) {
                return;
            }
            SourceGroup firstGroup = groups[0];
            FileObject fo = firstGroup.getRootFolder();
            if (fo == null) {
                return;
            }
            ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
            if (classPath == null) {
                classPath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
            }
            if (classPath == null) {
                return;
            }
            String resourceName = driver.getClassName().replace('.', '/') + ".class"; // NOI18N
            FileObject fob = classPath.findResource(resourceName); // NOI18N
            if (fob == null) {
                for (URL url : driver.getURLs()) {
                    FileObject jarO = URLMapper.findFileObject(url);
                    if (jarO != null) {
                        File jar = FileUtil.toFile(jarO);
                        URL u = null;
                        try {
                            u = jar.toURI().toURL();
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        FileObject jarFile = FileUtil.toFileObject(jar);
                        if (jarFile == null) {
                            continue;
                        }
                        if (FileUtil.isArchiveFile(jarFile)) {
                            u = FileUtil.getArchiveRoot(u);
                        }
                        try {
                            ProjectClassPathModifier.addRoots(new URL[]{u}, fo, ClassPath.COMPILE);
                        } catch (IOException | UnsupportedOperationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                
            }
        });
    }

    private static List<ProviderLibrary> createLibraries() {
        return createLibraries(null);
    }
    private static List<ProviderLibrary> createLibraries(String providerClass) {
        List<ProviderLibrary> providerLibs = new ArrayList<>();
        for (Library each : LibraryManager.getDefault().getLibraries()) {
            if (!"j2se".equals(each.getType())) { // NOI18N
                continue;
            }
            ClassPath cp = getLibraryClassPath(each);
            Provider provider = extractProvider(cp, providerClass);
            if (provider != null && (containsClass(cp, "javax.persistence.EntityManager") || containsClass(cp, "jakarta.persistence.EntityManager"))) { //NOI18N
                providerLibs.add(new ProviderLibrary(each, cp, provider));
            }
        }
        providerLibs.sort((ProviderLibrary l1, ProviderLibrary l2) -> {
            String name1 = l1.getLibrary().getDisplayName();
            String name2 = l2.getLibrary().getDisplayName();
            return name1.compareToIgnoreCase(name2);
        });
        return providerLibs;
    }

    /**
     * Gets the persistence providers that are defined in the libraries
     * of the IDE.
     * 
     * @return list of the providers that are defined in the IDE's libraries.
     */
    public static List<Provider> getProvidersFromLibraries() {
        List<Provider> providerLibs = new ArrayList<>();
        for (ProviderLibrary each : createLibraries()) {
            providerLibs.add(each.getProvider());
        }
        providerLibs.sort((Provider p1, Provider p2) -> {
            String name1 = p1.getDisplayName();
            String name2 = p2.getDisplayName();
            return name1.compareToIgnoreCase(name2);
        });
        return providerLibs;
    }

    /**
     * Gets the first library from the libraries registered in 
     * the IDE that contains a persistence provider.
     * 
     * @return the first library containing a persistence provider or null
     * if there were no libraries containing a provider.
     */
    public static Library getFirstProviderLibrary() {
        List<ProviderLibrary> libraries = createLibraries();
        if (!libraries.isEmpty()) {
            return libraries.get(0).getLibrary();
        }
        return null;
    }

    private static Provider extractProvider(ClassPath cp, String providerClass) {
        for (Provider each : ProviderUtil.getAllProviders()) {
            if ((providerClass == null || providerClass.equals(each.getProviderClass())) && each.isOnClassPath(cp)) {
                return each;
            }
        }
        return null;
    }

    /**
     * Encapsulates info on a library representing a persistence provider.
     */
    private static class ProviderLibrary {

        private final Library library;
        // the cp of the library
        private final ClassPath classPath;
        // the provider that the library contains
        private final Provider provider;

        public ProviderLibrary(Library library, ClassPath classPath, Provider provider) {
            assert library != null;
            assert classPath != null;
            assert provider != null;
            this.library = library;
            this.classPath = classPath;
            this.provider = provider;
        }

        public ClassPath getClassPath() {
            return classPath;
        }

        public Library getLibrary() {
            return library;
        }

        public Provider getProvider() {
            return provider;
        }
    }
}
