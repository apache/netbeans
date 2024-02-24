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
package org.netbeans.modules.javascript.cdnjs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 * CDNJS library provider, i.e., provider of the libraries available
 * on https://cdnjs.com/ server.
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
public final class LibraryProvider {
    /** Name of the 'versions' property. */
    private static final String PROPERTY_VERSIONS = "assets"; // NOI18N
    /** Name of the 'version name' property. */
    private static final String PROPERTY_VERSION_NAME = "version"; // NOI18N
    /** Name of the 'files' property. */
    private static final String PROPERTY_FILES = "files"; // NOI18N
    /** Name of the 'file name' property. */
    private static final String PROPERTY_FILE_NAME = "name"; // NOI18N
    /** Name of the 'result' property. */
    private static final String PROPERTY_RESULT = "results"; // NOI18N
    /** Name of the 'name' property. */
    private static final String PROPERTY_NAME = "name"; // NOI18N
    /** Name of the 'description' property. */
    private static final String PROPERTY_DESCRIPTION = "description"; // NOI18N
    /** Name of the 'homepage' property. */
    private static final String PROPERTY_HOMEPAGE = "homepage"; // NOI18N

    /** The only instance of this provider. */
    private static final LibraryProvider INSTANCE = new LibraryProvider();
    /** Cache of the search results. It maps the search term to the search result. */
    private final Map<String,WeakReference<Library[]>> cache =
            Collections.synchronizedMap(new HashMap<>());
    private final Map<String,WeakReference<Library>> entryCache =
            Collections.synchronizedMap(new HashMap<>());

    /**
     * Creates a new {@code LibraryProvider}.
     */
    private LibraryProvider() {
    }

    /**
     * Returns the only instance of this class.
     * 
     * @return (the only) instance of this class.
     */
    public static LibraryProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Finds the libraries matching the given search term. The resulting
     * {@code Library} instances potentially don't have the versions property
     * set. If that is the case the Library needs to be updated with the
     * {@link #updateLibraryVersions} call.
     *
     * @param searchTerm search term.
     * otherwise.
     */
    public Library[] findLibraries(String searchTerm) {
        WeakReference<Library[]> reference = cache.get(searchTerm);
        Library[] result = null;
        if (reference != null) {
            result = reference.get();
        }
        if (result == null) {
            String searchURL = getSearchURL(searchTerm);
            String urlContent = readUrl(searchURL);
            Library[] libraries = null;
            if (urlContent != null) {
                libraries = parse(urlContent);
            }
            cache.put(searchTerm, new WeakReference<>(libraries));
            result = libraries;
        }
        return result;
    }

    /**
     * Update a library returned by {@link #findLibraries(java.lang.String)}.
     * The full library data is fetched and the {@code versions} property is
     * filled.
     *
     * @param library to be updated
     */
    public void updateLibraryVersions(Library library) {
        Objects.nonNull(library);
        if(library.getVersions() != null && library.getVersions().length > 0) {
            return;
        }
        Library cachedLibrary = getCachedLibrary(library.getName());
        if(cachedLibrary != null) {
            library.setVersions(cachedLibrary.getVersions());
            return;
        }
        String data = readUrl(getLibraryDataUrl(library.getName()));
        if(data != null) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject libraryData = (JSONObject)parser.parse(data);
                updateLibrary(library, libraryData);
                entryCache.put(library.getName(), new WeakReference<>(library));
            } catch (ParseException ex) {
                Logger.getLogger(LibraryProvider.class.getName()).log(Level.INFO, null, ex);
            }
        }
    }

    /**
     * Load the full data for the supplied library. All fields are populated,
     * including the {@code versions} property.
     *
     * @param libraryName
     * @return
     */
    public Library loadLibrary(String libraryName) {
        Library cachedLibrary = getCachedLibrary(libraryName);
        if(cachedLibrary != null) {
            return cachedLibrary;
        }
        String data = readUrl(getLibraryDataUrl(libraryName));
        if (data != null) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject libraryData = (JSONObject) parser.parse(data);
                Library library = createLibrary(libraryData);
                entryCache.put(library.getName(), new WeakReference<>(library));
                return library;
            } catch (ParseException ex) {
                Logger.getLogger(LibraryProvider.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return null;
    }

    private Library getCachedLibrary(String name) {
        WeakReference<Library> cachedEntry = entryCache.get(name);
        if (cachedEntry != null) {
            return cachedEntry.get();
        } else {
            return null;
        }
    }

    private String getLibraryDataUrl(String libraryName) {
        String encodedLibraryName;
        try {
            encodedLibraryName = URLEncoder.encode(libraryName, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ueex) {
            // Should not happen, UTF-8 should be supported everywhere
            Logger.getLogger(LibraryProvider.class.getName()).log(Level.SEVERE, null, ueex);
            encodedLibraryName = libraryName;
        }
        return String.format(ASSET_URL_PATTERN, encodedLibraryName);
    }

    /**
     * URL pattern for library files.
     * {0} library name
     * {1} version name
     * {2} file name
     */
    private static final String LIBRARY_FILE_URL_PATTERN = System.getProperty(
            "netbeans.cdnjs.downloadurl", // NOI18N
            "https://cdnjs.cloudflare.com/ajax/libs/{0}/{1}/{2}"); // NOI18N

    /**
     * Downloads the specified file of the given library version. The data are saved
     * into a temporary file that is returned.
     * 
     * @param version library version whose file should be downloaded
     * (only libraries/versions returned by this provider can be downloaded).
     * @param fileIndex 0-based index of the file (in the version's list of files).
     * @return downloaded (temporary) file.
     * @throws IOException when the downloading of the file failed.
     */
    public File downloadLibraryFile(Library.Version version, int fileIndex) throws IOException {
        String libraryName = version.getLibrary().getName();
        String versionName = version.getName();
        String[] fileNames = version.getFiles();
        String fileName = fileNames[fileIndex];
        String url = MessageFormat.format(LIBRARY_FILE_URL_PATTERN, libraryName, versionName, fileName);
        URL urlObject = new URL(url);
        URLConnection urlConnection = urlObject.openConnection();
        try (InputStream input = urlConnection.getInputStream()) {
            int index = fileName.lastIndexOf('.');
            String prefix = (index == -1) ? fileName : fileName.substring(0,index);
            if (prefix.length() < 3) {
                prefix = "tmp" + prefix; // NOI18N
            }
            String suffix = (index == -1) ? "" : fileName.substring(index);
            File file = Files.createTempFile(prefix, suffix).toFile();
            try (OutputStream output = new FileOutputStream(file)) {
                FileUtil.copy(input, output);
                return file;
            }
        }
    }

    /** URL of the search web service. */
    static final String SEARCH_URL_PREFIX =
            System.getProperty("netbeans.cdnjs.searchurl", // NOI18N
            "https://api.cdnjs.com/libraries?fields=description,homepage,assets&search="); // NOI18N
    /** URL to fetch asset data */
    static final String ASSET_URL_PATTERN =
            System.getProperty("netbeans.cdnjs.asseturlpattern", // NOI18N
            "https://api.cdnjs.com/libraries/%1$s?fields=name,description,homepage,assets"); // NOI18N

    /**
     * Comparator that helps to sort library versions.
     */
    static final Comparator<Pair<Library.Version, Version>> VERSION_COMPARATOR = new Comparator<Pair<Library.Version, Version>>() {
        @Override
        public int compare(Pair<Library.Version, Version> pair1, Pair<Library.Version, Version> pair2) {
            return Version.Comparator.getInstance(false).compare(pair1.second(), pair2.second());
        }
    };

    private static void extractVersionInformation(JSONObject data, Library library) {
        JSONArray versionsData = (JSONArray) data.get(PROPERTY_VERSIONS);
        if (versionsData != null) {
            Library.Version[] versions = new Library.Version[versionsData.size()];
            for (int i = 0; i < versions.length; i++) {
                JSONObject versionData = (JSONObject) versionsData.get(i);
                versions[i] = createVersion(library, versionData);
            }
            sort(versions);
            library.setVersions(versions);
        } else {
            library.setVersions(new Library.Version[0]);
        }
    }

    /**
     * Sorts the library versions (in a descending order).
     *
     * @param versions versions to sort.
     */
    private static void sort(Library.Version[] versions) {
        Pair<Library.Version, Version>[] pairs = new Pair[versions.length];
        for (int i = 0; i < versions.length; i++) {
            Library.Version libraryVersion = versions[i];
            Version version = Version.parse(libraryVersion.getName());
            pairs[i] = Pair.of(libraryVersion, version);
        }
        Arrays.sort(pairs, VERSION_COMPARATOR);
        for (int i = 0; i < versions.length; i++) {
            versions[i] = pairs[i].first();
        }
    }

    /**
     * Creates a library version for the given JSON data.
     *
     * @param library owning library.
     * @param data    JSON data describing the library version.
     *
     * @return library version that corresponds to the given JSON data.
     */
    private static Library.Version createVersion(Library library, JSONObject data) {
        Library.Version version = new Library.Version(library, false);

        String versionName = (String) data.get(PROPERTY_VERSION_NAME);
        version.setName(versionName);

        JSONArray filesData = (JSONArray) data.get(PROPERTY_FILES);
        String[] files = new String[filesData.size()];
        for (int i = 0; i < files.length; i++) {
            Object fileInfo = filesData.get(i);
            String fileName;
            if (fileInfo instanceof JSONObject) {
                JSONObject fileData = (JSONObject) fileInfo;
                fileName = (String) fileData.get(PROPERTY_FILE_NAME);
            } else {
                fileName = fileInfo.toString();
            }
            files[i] = fileName;
        }
        version.setFileInfo(files, null);

        return version;
    }

    /**
     * Reads the content of the given URL.
     *
     * @param url URL whose content should be read.
     *
     * @return content of the given URL.
     */
    static String readUrl(String url) {
        String urlContent = null;
        try {
            URL urlObject = new URL(url);
            URLConnection urlConnection = urlObject.openConnection();
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }
            urlContent = content.toString();
        } catch (MalformedURLException muex) {
            Logger.getLogger(LibraryProvider.class.getName()).log(Level.INFO, null, muex);
        } catch (IOException ioex) {
            Logger.getLogger(LibraryProvider.class.getName()).log(Level.INFO, null, ioex);
        }
        return urlContent;
    }

    String getSearchURL(String searchTerm) {
        String encodedSearchTerm;
        try {
            encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ueex) {
            // Should not happen, UTF-8 should be supported everywhere
            Logger.getLogger(LibraryProvider.class.getName()).log(Level.SEVERE, null, ueex);
            encodedSearchTerm = searchTerm;
        }
        return SEARCH_URL_PREFIX + encodedSearchTerm;
    }

    /**
     * Parses the given JSON result of the search.
     *
     * @param data search result.
     *
     * @return libraries returned in the search result.
     */
    Library[] parse(String data) {
        Library[] libraries = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject searchResult = (JSONObject) parser.parse(data);
            JSONArray libraryArray = (JSONArray) searchResult.get(PROPERTY_RESULT);
            libraries = new Library[libraryArray.size()];
            for (int i = 0; i < libraries.length; i++) {
                JSONObject libraryData = (JSONObject) libraryArray.get(i);
                libraries[i] = createLibrary(libraryData);
            }
        } catch (ParseException pex) {
            Logger.getLogger(LibraryProvider.class.getName()).log(Level.INFO, null, pex);
        }
        return libraries;
    }

    /**
     * Creates a library for the given JSON data.
     *
     * @param data JSON data describing the library.
     *
     * @return library that corresponds to the given JSON data.
     */
    Library createLibrary(JSONObject data) {
        Library library = new Library();

        updateLibrary(library, data);

        return library;
    }

    void updateLibrary(Library library, JSONObject data) {
        String name = (String) data.get(PROPERTY_NAME);
        library.setName(name);

        String description = (String) data.get(PROPERTY_DESCRIPTION);
        library.setDescription(description);

        String homepage = (String) data.get(PROPERTY_HOMEPAGE);
        library.setHomePage(homepage);

        extractVersionInformation(data, library);
    }
}
