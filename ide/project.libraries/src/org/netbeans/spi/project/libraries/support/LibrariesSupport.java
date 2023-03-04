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
package org.netbeans.spi.project.libraries.support;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.netbeans.modules.project.libraries.LibrariesModule;
import org.netbeans.modules.project.libraries.LibraryAccessor;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.NamedLibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * SPI Support class.
 * Provides factory method for creating instance of the default LibraryImplementation.
 * @author David Konecny
 * @author Tomas Zezula
 */
public final class LibrariesSupport {

    private static final Logger LOG = Logger.getLogger(LibrariesSupport.class.getName());

    private LibrariesSupport () {
    }

    /**
     * Policy for handling content items which cannot be converted into the desired format.
     * @see #convertURIsToURLs(java.util.List)
     * @see #convertURLsToURIs(java.util.List)
     * @since 1.48
     */
    public enum ConversionMode {
        /**
         * Drop entry silently.
         */
        SKIP,
        /**
         * Entry is dropped but a warning is logged.
         */
        WARN,
        /**
         * {@link #toString(ClassPath.PathConversionMode)} will fail with an {@link IllegalArgumentException}.
         * Useful for unit tests.
         */
        FAIL
    }

    /**
     * Creates default {@link LibraryImplementation3}
     * @param libraryType type of library
     * @param volumeTypes types of supported volumes
     * @return LibraryImplementation3
     * @since 1.39
     */
    @NonNull
    public static LibraryImplementation3 createLibraryImplementation3 (
            @NonNull final String libraryType,
            @NonNull final String... volumeTypes) {
        return new DefaultLibraryImplementation (libraryType, volumeTypes);
    }

    /**
     * Creates default LibraryImplementation
     * @param libraryType type of library
     * @param volumeTypes types of supported volumes
     * @return LibraryImplementation, never return null
     */
    public static LibraryImplementation createLibraryImplementation (String libraryType, String[] volumeTypes) {
        return createLibraryImplementation3(libraryType, volumeTypes);
    }
    
    /**
     * Returns registered {@link LibraryTypeProvider} for given library type. This method 
     * is mostly used by {@link org.netbeans.spi.project.libraries.LibraryProvider} implementators.
     * @param libraryType  the type of library for which the provider should be returned.
     * @return {@link LibraryTypeProvider} for given library type or null, if none is registered.
     * @since org.netbeans.modules.project.libraries/1 1.14
     */
    public static LibraryTypeProvider getLibraryTypeProvider (String libraryType) {
        return LibraryTypeRegistry.getDefault().getLibraryTypeProvider(libraryType);
    }
    
    /**
     * Returns all registered {@link LibraryTypeProvider}s. This method 
     * is mostly used by {@link org.netbeans.spi.project.libraries.LibraryProvider} implementators.
     * @return an array of {@link LibraryTypeProvider}, never returns null.
     * @since org.netbeans.modules.project.libraries/1 1.14
     */
    public static LibraryTypeProvider[] getLibraryTypeProviders () {
        return LibraryTypeRegistry.getDefault().getLibraryTypeProviders();
    }
    
    /**
     * Properly converts possibly relative file path to URI.
     * @param path file path to convert; can be relative; cannot be null
     * @return uri
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI convertFilePathToURI(final @NonNull String path) {
        Parameters.notNull("path", path);   //NOI18N
        try {
            File f = new File(path);
            if (f.isAbsolute()) {
                return BaseUtilities.toURI(f);
            } else {
                // create hierarchical relative URI (that is no schema)
                return new URI(null, null, path.replace('\\', '/'), null);
            }

        } catch (URISyntaxException ex) {
	    IllegalArgumentException y = new IllegalArgumentException();
	    y.initCause(ex);
	    throw y;
        }
    }
    
    /**
     * Properly converts possibly relative URI to file path.
     * @param uri URI convert; can be relative URI; cannot be null
     * @return file path
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static String convertURIToFilePath(URI uri) {
        if (uri.isAbsolute()) {
            return BaseUtilities.toFile(uri).getPath();
        } else {
            String path = uri.getPath();
            if (path.length() > 0 && path.charAt(path.length()-1) == '/') {   //NOI18N
                path = path.substring(0, path.length()-1);
            }
            return path.replace('/', File.separatorChar);   //NOI18N
        }
    }

    /**
     * Converts a list of {@link URI}s into a list of {@link URL}s.
     * @param uris the list of {@link URI}s to be converted
     * @param conversionMode the Policy for handling content items which
     * cannot be converted into {@link URL}s.
     * @return the list of {@link URL}s
     * @throws IllegalArgumentException for unconvertable entry in
     * {@link ConversionMode#FAIL}
     * @since 1.48
     */
    @NonNull
    public static List<URL> convertURIsToURLs(
            @NonNull final List<? extends URI> uris,
            @NonNull final ConversionMode conversionMode) {
        List<URL> content = new ArrayList<>();
        for (URI uri : uris) {
            try {
                content.add(uri.toURL());
            } catch (MalformedURLException ex) {
                switch (conversionMode) {
                    case FAIL:
                        throw new IllegalArgumentException(uri.toString());
                    case WARN:
                        LOG.log(
                            Level.WARNING,
                            "Cannot convert URI: {0} to URL.",  //NOI18N
                            uri);
                        break;
                    case SKIP:
                        break;
                    default:
                        throw new IllegalStateException(conversionMode.name());
                }
            }
        }
        return content;
    }

    /**
     * Converts a list of {@link URL}s into a list of {@link URI}s.
     * @param urls the list of {@link URL}s to be converted
     * @param conversionMode the Policy for handling content items which
     * cannot be converted into @{link URI}s
     * @return the list of {@link URI}s
     * @throws IllegalArgumentException for unconvertable entry in
     * {@link ConversionMode#FAIL}
     * @since 1.48
     */
    @NonNull
    public static List<URI> convertURLsToURIs(
            @NonNull final List<URL> urls,
            @NonNull final ConversionMode conversionMode) {
        List<URI> content = new ArrayList<>();
        for (URL url : urls) {
            try {
                final URI uri = new URI (url.toExternalForm());
                content.add(uri);
            } catch (URISyntaxException e) {
                switch (conversionMode) {
                    case FAIL:
                        throw new IllegalArgumentException(url.toString());
                    case WARN:
                        LOG.log(
                            Level.WARNING,
                            "Cannot convert URL: {0} to URI.",  //NOI18N
                            url);
                        break;
                    case SKIP:
                        break;
                    default:
                        throw new IllegalStateException(conversionMode.name());
                }
            }
        }
        return content;
    }

    /**
     * Helper method to resolve (possibly relative) library content URI to FileObject.
     * 
     * @param libraryLocation library location file; can be null for global libraries
     * @param libraryEntry library entry to resolve
     * @return file object
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static FileObject resolveLibraryEntryFileObject(URL libraryLocation, URI libraryEntry) {
        URI u = resolveLibraryEntryURI(libraryLocation, libraryEntry);
        try {
            return URLMapper.findFileObject(u.toURL());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    /**
     * Helper method to resolve (possibly relative) library content URI.
     * 
     * @param libraryLocation library location file
     * @param libraryEntry relative library entry to resolve
     * @return absolute URI
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI resolveLibraryEntryURI(URL libraryLocation, URI libraryEntry) {
        Parameters.notNull("libraryEntry", libraryEntry); //NOI18N
        if (libraryEntry.isAbsolute()) {
            return libraryEntry;
        } else {
            if (libraryLocation == null) {
                throw new IllegalArgumentException("cannot resolve relative URL without library location"); //NOI18N
            }
            if (!"file".equals(libraryLocation.getProtocol())) { //NOI18N
                throw new IllegalArgumentException("not file: protocol - "+libraryLocation.toExternalForm()); //NOI18N
            }
            if (!libraryLocation.getPath().endsWith(".properties")) { //NOI18N
                throw new IllegalArgumentException("library location must be a file - "+libraryLocation.toExternalForm()); //NOI18N
            }
            URI resolved;
            try {
                resolved = libraryLocation.toURI().resolve(libraryEntry);
            } catch (URISyntaxException x) {
                throw new AssertionError(x);
            }
            if (libraryEntry.getPath().contains("!/")) {
                return URI.create("jar:" + resolved);
            } else {
                return resolved;
            }
        }
    }
    
    /**
     * Returns the URI of the archive file containing the file
     * referred to by a <code>jar</code>-protocol URL.
     * <strong>Remember</strong> that any path within the archive is discarded
     * so you may need to check for non-root entries.
     * @param uri a URI; can be relative URI
     * @return the embedded archive URI, or null if the URI is not a
     *         <code>jar</code>-protocol URI containing <code>!/</code>
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI getArchiveFile(URI uri) {
        String u = uri.toString();
        int index = u.indexOf("!/"); //NOI18N
        if (index != -1) {
            try {
                return new URI(u.substring(u.startsWith("jar:") ? 4 : 0, index)); // NOI18N
            } catch (URISyntaxException e) {
                throw new AssertionError(e);
            }
        }
        return null;
    }
    
    /**
     * Returns a URI representing the root of an archive.
     * @param uri of a ZIP- (or JAR-) format archive file; can be relative
     * @return the <code>jar</code>-protocol URI of the root of the archive
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI getArchiveRoot(URI uri) {
        assert !uri.toString().contains("!/") : uri;
        try {
            return new URI((uri.isAbsolute() ? "jar:" : "") + uri.toString() + "!/"); // NOI18N
        } catch (URISyntaxException ex) {
                throw new AssertionError(ex);
        }
    }

    /**
     * Returns a {@link LibraryStorageArea} for given {@link LibraryManager}.
     * @param manager the {@link LibraryManager} to get a {@link LibraryStorageArea} for
     * @return the {@link LibraryStorageArea}
     * @since 1.48
     */
    @NonNull
    public static LibraryStorageArea getLibraryStorageArea(@NonNull final LibraryManager manager) {
        Parameters.notNull("manager", manager); //NOI18N
        return LibraryAccessor.getInstance().getArea(manager);
    }

    /**
     * Returns a localized (user friendly) name of the {@link LibraryImplementation}.
     * @param impl the library to get the localized name for
     * @return the localized name
     * @since 1.48
     */
    @NonNull
    public static String getLocalizedName(@NonNull final LibraryImplementation impl) {
        Parameters.notNull("impl", impl);   //NOI18N
        if (supportsDisplayName(impl) && ((NamedLibraryImplementation)impl).getDisplayName() != null) {
            return ((NamedLibraryImplementation)impl).getDisplayName();
        }
        final FileObject src = LibrariesModule.getFile(impl);
        if (src != null) {
            Object obj = src.getAttribute("displayName"); // NOI18N
            if (obj instanceof String) {
                return (String)obj;
            }
        }
        if (impl instanceof ForwardingLibraryImplementation) {
            String proxiedName = getLocalizedName(((ForwardingLibraryImplementation)impl).getDelegate());
            if (proxiedName != null) {
                return proxiedName;
            }
        }

        return getLocalizedString(impl.getLocalizingBundle(), impl.getName());
    }

    /**
     * Tests if given {@link LibraryImplementation} supports display name.
     * @param impl the {@link LibraryImplementation} to be checked
     * @return true when given {@link LibraryImplementation} supports display name
     * @since 1.48
     */
    public static boolean supportsDisplayName(final @NonNull LibraryImplementation impl) {
        assert impl != null;
        if (impl instanceof ForwardingLibraryImplementation) {
            return supportsDisplayName(((ForwardingLibraryImplementation)impl).getDelegate());
        }
        return impl instanceof NamedLibraryImplementation;
    }

    /**
     * Returns {@link LibraryImplementation} display name.
     * @param impl the {@link LibraryImplementation} to return display name for
     * @return the display name if supported or null
     * @since 1.48
     */
    public static @CheckForNull String getDisplayName (final @NonNull LibraryImplementation impl) {
        return supportsDisplayName(impl) ?
                ((NamedLibraryImplementation)impl).getDisplayName() :
                null;
    }

    /**
     * Sets {@link LibraryImplementation} display name.
     * @param impl the {@link LibraryImplementation} to set the display name to
     * @param name the display name
     * @return true if given {@link LibraryImplementation} support display name
     * @since 1.48
     */
    public static boolean setDisplayName(
            final @NonNull LibraryImplementation impl,
            final @NullAllowed String name) {
        if (supportsDisplayName(impl)) {
            final NamedLibraryImplementation nimpl = (NamedLibraryImplementation) impl;
            if (!BaseUtilities.compareObjects(nimpl.getDisplayName(), name)) {
                nimpl.setDisplayName(name);
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if given {@link LibraryImplementation} supports properties.
     * @param impl the {@link LibraryImplementation} to be checked
     * @return true when given {@link LibraryImplementation} supports properties
     * @since 1.48
     */
    public static boolean supportsProperties(final @NonNull LibraryImplementation impl) {
        assert impl != null;
        if (impl instanceof ForwardingLibraryImplementation) {
            return supportsProperties(((ForwardingLibraryImplementation)impl).getDelegate());
        }
        return impl instanceof LibraryImplementation3;
    }

    /**
     * Returns {@link LibraryImplementation} properties.
     * @param impl the {@link LibraryImplementation} to return properties for
     * @return the library properties if supported or null
     * @since 1.48
     */
    @NonNull
    public static Map<String,String> getProperties (final @NonNull LibraryImplementation impl) {
        return supportsProperties(impl) ?
                ((LibraryImplementation3)impl).getProperties() :
                Collections.<String,String>emptyMap();
    }

    /**
     * Sets {@link LibraryImplementation} properties.
     * @param impl the {@link LibraryImplementation} to set properties to
     * @param props the properties
     * @return true if given {@link LibraryImplementation} support properties
     * @since 1.48
     */
    public static boolean setProperties(
        final @NonNull LibraryImplementation impl,
        final @NonNull Map<String,String>  props) {
        if (supportsProperties(impl)) {
            final LibraryImplementation3 impl3 = (LibraryImplementation3)impl;
            if (!BaseUtilities.compareObjects(impl3.getProperties(), props)) {
                impl3.setProperties(props);
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if given {@link LibraryImplementation} supports {@link URI} content.
     * @param impl the {@link LibraryImplementation} to be checked
     * @return true when given {@link LibraryImplementation} supports {@link URI} content
     * @since 1.48
     */
    public static boolean supportsURIContent(@NonNull final LibraryImplementation impl) {
        if (impl instanceof ForwardingLibraryImplementation) {
            return supportsURIContent(((ForwardingLibraryImplementation)impl).getDelegate());
        }
        return impl instanceof LibraryImplementation2;
    }

    /**
     * Returns {@link LibraryImplementation} {@link URI} content.
     * @param impl the {@link LibraryImplementation} to return {@link URI} content for
     * @param volumeType the volumeType
     * @param conversionMode conversion failure policy in case the library does not
     * support {@link URI} content and the library {@link URL}s are converted to {@link URI}s
     * @return the library {@link URI} content
     * @throws IllegalArgumentException when unsupported volumeType or for unconvertable entry in
     * {@link ConversionMode#FAIL}
     * @since 1.48
     */
    @NonNull
    public static List<URI> getURIContent(
        @NonNull final LibraryImplementation impl,
        @NonNull final String volumeType,
        @NonNull final ConversionMode conversionMode) {
        return supportsURIContent(impl) ?
            ((LibraryImplementation2)impl).getURIContent(volumeType) :
            convertURLsToURIs(
                impl.getContent(volumeType),
                conversionMode);
    }

    /**
     * Sets {@link LibraryImplementation} {@link URI} content.
     * @param impl the {@link LibraryImplementation} to set the {@link URI} content to
     * @param volumeType the volumeType
     * @param path the {@link URI} content
     * @param conversionMode conversion failure policy in case the library does not
     * support {@link URI} content and the path {@link URI}s are converted to {@link URL}s
     * @return true if given {@link LibraryImplementation} support {@link URI} content
     * @throws IllegalArgumentException when unsupported volumeType or for unconvertable entry in
     * {@link ConversionMode#FAIL}
     * @since 1.48
     */
    public static boolean setURIContent(
        @NonNull final LibraryImplementation impl,
        @NonNull final String volumeType,
        @NonNull final List<URI> path,
        @NonNull final ConversionMode conversionMode) {
        if (supportsURIContent(impl)) {
            final LibraryImplementation2 impl2 = (LibraryImplementation2)impl;
            if (!BaseUtilities.compareObjects(impl2.getURIContent(volumeType), path)) {
                impl2.setURIContent(volumeType, path);
                return true;
            }
        } else {
            impl.setContent(
                volumeType,
                convertURIsToURLs(
                    path,
                    conversionMode));
            return true;
        }
        return false;
    }

    /**
     * Returns the {@link LibraryImplementation} for given {@link Library}.
     * @param library the {@link Library} to return SPI for.
     * @return the {@link LibraryImplementation}
     * @since 1.51
     */
    @NonNull
    public static LibraryImplementation getLibraryImplementation(@NonNull final Library library) {
        Parameters.notNull("library", library); //NOI18N
        return LibraryAccessor.getInstance().getLibraryImplementation(library);
    }

    private static String getLocalizedString (
            final @NullAllowed String bundleResourceName,
            final @NullAllowed String key) {
        if (key == null) {
            return null;
        }
        if (bundleResourceName == null) {
            return key;
        }
        final ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle(bundleResourceName);
        } catch (MissingResourceException mre) {
            // Bundle should have existed.
            LOG.log(Level.INFO, "Wrong resource bundle", mre);      //NOI18N
            return key;
        }
        try {
            return bundle.getString (key);
        } catch (MissingResourceException mre) {
            // No problem, not specified.
            return key;
        }
    }
}
