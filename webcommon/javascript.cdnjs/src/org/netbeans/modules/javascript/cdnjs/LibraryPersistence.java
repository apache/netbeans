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
package org.netbeans.modules.javascript.cdnjs;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Persistence of libraries.
 *
 * @author Jan Stola
 */
public final class LibraryPersistence {
    /** Name-space used to store the library information. */
    private static final String NAMESPACE_URI = "http://www.netbeans.org/ns/cdnjs-libraries/1"; // NOI18N
    /** Name of the root element. */
    private static final String ELEMENT_LIBRARIES = "libraries"; // NOI18N
    /** Name of the element holding information about one library. */
    private static final String ELEMENT_LIBRARY = "library"; // NOI18N
    /** Name of the element holding information about one library file. */
    private static final String ELEMENT_FILE = "file"; // NOI18N
    /** Name of the attribute storing the name of the library. */
    private static final String ATTR_LIBRARY_NAME = "name"; // NOI18N
    /** Name of the attribute storing the name of the version of the library. */
    private static final String ATTR_VERSION_NAME = "version"; // NOI18N
    /** Name of the attribute storing the local path (relative to the project's folder) to the library file. */
    private static final String ATTR_FILE_LOCAL_PATH = "localPath"; // NOI18N
    /** Name of the attribute storing the relative path to the library file as specified by CDNJS meta-data. */
    private static final String ATTR_FILE_PATH = "path"; // NOI18N
    /** The default instance of this class. */
    private static final LibraryPersistence DEFAULT = new LibraryPersistence();

    private final LibraryListener.Support libraryListenerSupport = new LibraryListener.Support();

    /**
     * Creates a new {@code LibraryPersistence}.
     */
    LibraryPersistence() {
    }

    /**
     * Returns the default instance of this class.
     *
     * @return default instance of this class.
     */
    public static LibraryPersistence getDefault() {
        return DEFAULT;
    }

    /**
     * Adds a {@link LibraryListener} to the listener list. The same
     * listener object may be added more than once, and will be called
     * as many times as it is added. If {@code listener} is {@code null},
     * no exception is thrown and no action is taken.
     * @param listener the {@link LibraryListener} to be added, can be {@code null}
     */
    public void addLibraryListener(@NullAllowed LibraryListener listener) {
        libraryListenerSupport.addLibraryListener(listener);
    }

    /**
     * Removes a {@link LibraryListener} from the listener list.
     * If {@code listener} was added more than once,
     * it will be notified one less time after being removed.
     * If {@code listener} is {@code null}, or was never added, no exception is
     * thrown and no action is taken.
     * @param listener the {@link LibraryListener} to be removed, can be {@code null}
     */
    public void removeLibraryListener(@NullAllowed LibraryListener listener) {
        libraryListenerSupport.removeLibraryListener(listener);
    }

    /**
     * Loads library information for the given project.
     *
     * @param project project whose library information should be loaded.
     * @return library information for the given project.
     */
    public Library.Version[] loadLibraries(Project project) {
        Library.Version[] libraries;
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        Element element = config.getConfigurationFragment(ELEMENT_LIBRARIES, NAMESPACE_URI, true);
        if (element == null) {
            libraries = new Library.Version[0];
        } else{
            NodeList libraryList = element.getElementsByTagNameNS(NAMESPACE_URI, ELEMENT_LIBRARY);
            libraries = new Library.Version[libraryList.getLength()];
            for (int i=0; i<libraryList.getLength(); i++) {
                Element libraryElement = (Element)libraryList.item(i);
                libraries[i] = loadLibrary(libraryElement);
            }
        }
        return libraries;
    }

    /**
     * Loads/parses information about one library from the given DOM element.
     *
     * @param libraryElement element to load information from.
     * @return library version corresponding to the given DOM element.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
            justification = "Not sure about it, really (not my code)")
    private Library.Version loadLibrary(Element libraryElement) {
        String libraryName = libraryElement.getAttribute(ATTR_LIBRARY_NAME);
        String versionName = libraryElement.getAttribute(ATTR_VERSION_NAME);
        Library library = new Library();
        library.setName(libraryName);
        Library.Version version = new Library.Version(library, true);
        version.setName(versionName);
        library.setVersions(new Library.Version[] { version });
        NodeList fileList = libraryElement.getElementsByTagNameNS(NAMESPACE_URI, ELEMENT_FILE);
        String[] files = new String[fileList.getLength()];
        String[] localFiles = new String[fileList.getLength()];
        for (int i=0; i<fileList.getLength(); i++) {
            Element fileElement = (Element)fileList.item(i);
            String path = fileElement.getAttribute(ATTR_FILE_PATH);
            String localPath = fileElement.getAttribute(ATTR_FILE_LOCAL_PATH);
            files[i] = path;
            localFiles[i] = (localPath == null) ? path : localPath;
        }
        version.setFileInfo(files, localFiles);
        return version;
    }

    /**
     * Stores the information about libraries used by the given project.
     *
     * @param project project whose library information should be stored.
     * @param libraries libraries used by the project.
     */
    void storeLibraries(Project project, Library.Version[] libraries) throws IOException {
        Arrays.sort(libraries, new LibraryVersionComparator());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element librariesElement = document.createElementNS(NAMESPACE_URI, ELEMENT_LIBRARIES);
            for (Library.Version library : libraries) {
                Element libraryElement = document.createElementNS(NAMESPACE_URI, ELEMENT_LIBRARY);
                String libraryName = library.getLibrary().getName();
                libraryElement.setAttribute(ATTR_LIBRARY_NAME, libraryName);
                String versionName = library.getName();
                libraryElement.setAttribute(ATTR_VERSION_NAME, versionName);
                String[] files = library.getFiles();
                String[] localFiles = library.getLocalFiles();
                for (int i=0; i<files.length; i++) {
                    Element fileElement = document.createElementNS(NAMESPACE_URI, ELEMENT_FILE);
                    String path = files[i];
                    String localPath = localFiles[i];
                    fileElement.setAttribute(ATTR_FILE_PATH, path);
                    fileElement.setAttribute(ATTR_FILE_LOCAL_PATH, localPath);
                    libraryElement.appendChild(fileElement);
                }
                librariesElement.appendChild(libraryElement);
            }
            AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
            config.putConfigurationFragment(librariesElement, true);
            ProjectManager.getDefault().saveProject(project);

            logLibraryUsage(libraries);

            // fire event
            libraryListenerSupport.fireLibrariesChanged(project);
        } catch (ParserConfigurationException pcex) {
            Logger.getLogger(LibraryPersistence.class.getName()).log(Level.SEVERE,
                    "Unable to store library information!", pcex); // NOI18N
        }
    }

    /** Logger of CDNJS libraries usage. */
    private static final UsageLogger USAGE_LOGGER = new UsageLogger.Builder("org.netbeans.ui.metrics.javascript.cdnjs")  // NOI18N
            .firstMessageOnly(false)
            .create();

    /**
     * Logs the used libraries.
     *
     * @param libraries used libraries.
     */
    private static void logLibraryUsage(Library.Version[] libraries) {
        // ui usage
        USAGE_LOGGER.log(LibraryPersistence.class, "USG_CDNJS_LIBRARY_EDIT"); // NOI18N
        // libraries
        for (Library.Version library : libraries) {
            String version = library.getName();
            String name = library.getLibrary().getName();
            USAGE_LOGGER.log(LibraryPersistence.class, "USG_CDNJS_LIBRARY", name, version); // NOI18N
        }
    }

    /**
     * Comparator of {@code Library.Version}s.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE",
            justification = "No need to be serializable")
    static class LibraryVersionComparator implements Comparator<Library.Version> {
        @Override
        public int compare(Library.Version o1, Library.Version o2) {
            String name1 = o1.getLibrary().getName();
            String name2 = o2.getLibrary().getName();
            return name1.compareTo(name2);
        }
    }

}
