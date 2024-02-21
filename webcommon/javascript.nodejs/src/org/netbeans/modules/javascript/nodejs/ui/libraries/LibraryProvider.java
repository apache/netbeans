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

package org.netbeans.modules.javascript.nodejs.ui.libraries;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.openide.util.RequestProcessor;

/**
 * npm library provider.
 * 
 * The clients of this provider are expected to use {@link #findLibraries}
 * method to search libraries matching given search term. The search
 * for the libraries is performed asynchronously. Hence, this method
 * returns {@code null} when it is called for the first time for the given
 * search term. The clients should register property change listeners
 * on the provider to be notified when the result of the search is available.
 * The property change events fired by the provider will have the property
 * name set to the search term and the new value to the result of the search.
 * The new value may be set to {@code null} when the search failed for
 * some reason (the new value is set to an empty array when the result
 * of the search is empty).
 * 
 * @author Jan Stola
 */
public class LibraryProvider {

    private static final Logger LOGGER = Logger.getLogger(LibraryProvider.class.getName());

    /** Library providers for individual projects. */
    private static final Map<Project,LibraryProvider> providers =
            Collections.synchronizedMap(new WeakHashMap<Project,LibraryProvider>());
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(LibraryProvider.class.getName(), 3);
    /** Project for which the libraries should be provided. */
    private final Project project;
    /** Cache of the search results. It maps the search term to the search result. */
    private final Map<String,WeakReference<Library[]>> searchCache =
            Collections.synchronizedMap(new HashMap<String,WeakReference<Library[]>>());
    /** Cache of library details. It maps name of the library/package to the library details. */
    private final Map<String,WeakReference<Library>> detailCache =
            Collections.synchronizedMap(new HashMap<String,WeakReference<Library>>());
    /** Property change support. */
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Creates a new {@code LibraryProvider} for the given project.
     * 
     * @param project project for which the libraries should be provided.
     */
    private LibraryProvider(Project project) {
        this.project = project;
    }

    /**
     * Returns library provider for the given project.
     * 
     * @param project project for which the library provider should be returned.
     * @return library provider for the given project.
     */
    public static synchronized LibraryProvider forProject(Project project) {
        LibraryProvider provider = providers.get(project);
        if (provider == null) {
            provider = new LibraryProvider(project);
            providers.put(project, provider);
        }
        return provider;
    }

    /**
     * Adds a property change listener to this provider. The listener
     * is notified whenever a new search result is available.
     * 
     * @param listener listener to register.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener from this provider.
     * 
     * @param listener listener to unregister.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Finds the libraries matching the given search term. This method returns
     * {@code null} when the result of the search is not present in the cache
     * already. It starts the corresponding search in this case and reports
     * its result by firing a property change event with the property name
     * equal to the given search term. The result of the search can be obtained
     * through the new value property of the event or by another invocation
     * of this method. The first approach is recommended as it allows
     * to recognize that the search failed. The new value property of the
     * event is set to {@code null} in such case.
     * 
     * @param searchTerm search term.
     */
    public Library[] findLibraries(String searchTerm) {
        WeakReference<Library[]> reference = searchCache.get(searchTerm);
        Library[] result = null;
        if (reference != null) {
            result = reference.get();
        }
        if (result == null) {
            SearchTask task = new SearchTask(searchTerm);
            RP.post(task);
        }
        return result;
    }

    /**
     * Returns details of the library/package with the given name.
     * 
     * @param libraryName name of the library/package.
     * @param cachedOnly if {@code true} then the details will be returned when
     * they are cached only (i.e., {@code null} will be returned if they are not
     * cached and no attempt will be made to obtain the details from the server).
     * @return details of the library/package with the given name.
     */
    public Library libraryDetails(String libraryName, boolean cachedOnly) {
        WeakReference<Library> reference = detailCache.get(libraryName);
        Library result = null;
        if (reference != null) {
            result = reference.get();
        }
        if (result == null && !cachedOnly) {
            assert !EventQueue.isDispatchThread();
            NpmExecutable executable = NpmExecutable.getDefault(project, false);
            if (executable != null) {
                JSONObject details = executable.view(libraryName);
                if (details != null) {
                    result = parseLibraryDetails(details);
                    reference = new WeakReference<>(result);
                    detailCache.put(libraryName, reference);
                }
            }
        }
        return result;
    }

    /**
     * Returns the installed libraries/packages.
     * 
     * @return map that maps the library name to the installed version.
     * Returns {@code null} when the attempt to determine the installed
     * libraries failed. When no library is installed then an empty array
     * is returned.
     */
    public Map<String,String> installedLibraries() {
        Map<String,String> result = null;
        NpmExecutable executable = NpmExecutable.getDefault(project, false);
        if (executable != null) {
            JSONObject json = executable.list(0);
            if (json != null) {
                result = new HashMap<>();
                JSONObject dependencies = (JSONObject)json.get("dependencies"); // NOI18N
                if (dependencies != null) {
                    Set<Map.Entry<Object, Object>> entrySet = dependencies.entrySet();
                    for (Map.Entry<Object, Object> entry : entrySet) {
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        if (value instanceof JSONObject) {
                            JSONObject libraryInfo = (JSONObject)value;
                            String versionName = (String)libraryInfo.get("version"); // NOI18N
                            if (versionName != null) {
                                String libraryName = key.toString();
                                result.put(libraryName, versionName);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns library that corresponds to the JSON object obtained
     * as a result of a call to {@code npm view --json} command.
     * 
     * @param viewInfo result of some {@code npm view --json} command.
     * @return library that corresponds to the given JSON object.
     */
    private Library parseLibraryDetails(JSONObject viewInfo) {
        String name = (String)viewInfo.get("name"); // NOI18N
        Library library = new Library(name);

        String latestVersionName = (String)viewInfo.get("version"); // NOI18N
        Library.Version latestVersion = null;

        Library.Version[] versions;
        Object versionsObject = viewInfo.get("versions"); // NOI18N
        if (versionsObject instanceof JSONArray) {
            JSONArray versionArray = (JSONArray)viewInfo.get("versions"); // NOI18N
            versions = new Library.Version[versionArray.size()];
            for (int i=0; i<versionArray.size(); i++) {
                String versionName = (String)versionArray.get(i);
                Library.Version version = new Library.Version(library, versionName);
                if (versionName.equals(latestVersionName)) {
                    latestVersion = version;
                }
                versions[i] = version;
            }
        } else {
            latestVersion = new Library.Version(library, versionsObject.toString());
            versions = new Library.Version[] { latestVersion };
        }
        library.setVersions(versions);
        library.setLatestVersion(latestVersion);

        return library;
    }

    /**
     * Updates the cache with the result of the search.
     * 
     * @param searchTerm search term.
     * @param libraries libraries matching the search term.
     */
    void updateCache(String searchTerm, Library[] libraries) {
        if (libraries != null) {
            WeakReference<Library[]> reference = new WeakReference<>(libraries);
            searchCache.put(searchTerm, reference);
        }
        propertyChangeSupport.firePropertyChange(searchTerm, null, libraries);
    }

    /**
     * Search task - a task that performs one search for libraries matching
     * the given search term.
     */
    private class SearchTask implements Runnable {
        /** Search term. */
        private final String searchTerm;

        /**
         * Creates a new {@code SearchTask} for the given search term.
         * 
         * @param searchTerm search term.
         */
        SearchTask(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        @Override
        public void run() {
            NpmExecutable executable = NpmExecutable.getDefault(project, false);
            if (executable != null) {
                String result = executable.search(searchTerm);
                Library[] libraries = result == null ? null : parseSearchResult(result);
                updateCache(searchTerm, libraries);
            }
        }

        /**
         * Parses the output of npm search call.
         * Assumes output format of <code>npm search --parseable --long</code>.
         * 
         * @param searchResult output of the npm search call.
         * @return libraries/packages returned by the search or {@code null}
         *         in case of unexpected search result
         */
        @CheckForNull
        private Library[] parseSearchResult(String searchResult) {
            String[] lines = searchResult.split("\n"); // NOI18N
            ArrayList<Library> libraries = new ArrayList<>(lines.length);
            
            for (String line : lines) {
                String[] columns = line.split("\t");
                if (columns.length < 5) continue;
                
                Library library = new Library(columns[0].trim());
                library.setDescription(columns[1].trim());
                Library.Version latestVersion = new Library.Version(library, columns[4].trim());
                library.setLatestVersion(latestVersion);
                if(columns.length > 5)library.setKeywords(columns[5].split(" ")); //this can be optional
                
                libraries.add(library);
            }
            
            return libraries.toArray(new Library[0]);
        }

    }

}
