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

package org.openide.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.Reference;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import static java.nio.charset.CodingErrorAction.*;
import static java.nio.charset.StandardCharsets.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

/** Convenience class permitting easy loading of localized resources of various sorts.
* Extends the functionality of {@link ResourceBundle} to handle branding, and interacts
* better with class loaders in a module system.
* <p>Example usage:
* <pre>
* package com.mycom;
* public class Foo {
*     public String getDisplayName() {
*         return {@link #getMessage(Class,String) NbBundle.getMessage}(Foo.class, "Foo.displayName");
*     }
* }
* </pre>
* will in German locale look for the key {@code Foo.displayName} in
* {@code com/mycom/Bundle_de.properties} and then {@code com/mycom/Bundle.properties} (in that order).
 * Usually however it is easiest to use {@link org.openide.util.NbBundle.Messages}.
*/
public class NbBundle extends Object {

    private static final Logger LOG = Logger.getLogger(NbBundle.class.getName());

    private static final boolean USE_DEBUG_LOADER = Boolean.getBoolean("org.openide.util.NbBundle.DEBUG"); // NOI18N
    private static String brandingToken = null;
    
    private static final UtfThenIsoCharset utfThenIsoCharset = new UtfThenIsoCharset(false);
    private static final UtfThenIsoCharset utfThenIsoCharsetOnlyUTF8 = new UtfThenIsoCharset(true);    
    
    /**
     * Cache of URLs for localized files.
     * Keeps only weak references to the class loaders.
     * @see "#9275"
     */
    static final Map<ClassLoader,Map<String,URL>> localizedFileCache = new WeakHashMap<>();

    /**
     * Cache of resource bundles.
     */
    static final Map<ClassLoader,Map<String,Reference<ResourceBundle>>> bundleCache = new WeakHashMap<>();

    /**
     * Do not call.
     * @deprecated There is no reason to instantiate or subclass this class.
     *             All methods in it are static.
     */
    @Deprecated
    public NbBundle() {
    }

    /** Get the current branding token.
     * @return the branding, or <code>null</code> for none
     */
    public static String getBranding() {
        return brandingToken;
    }

    /** Set the current branding token.
     * The permitted format, as a regular expression:
     * <pre>[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*</pre>
     * <p class="nonnormative">
     * This is normally only called by NetBeans startup code and unit tests.
     * Currently the branding may be specified by passing the <code>--branding</code>
     * command-line option to the launcher.
     * </p>
     * @param bt the new branding, or <code>null</code> to clear
     * @throws IllegalArgumentException if in an incorrect format
     */
    public static void setBranding(String bt) throws IllegalArgumentException {
        if (bt != null && !bt.matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) { // NOI18N
            throw new IllegalArgumentException("Malformed branding token: " + bt); // NOI18N
        }
        brandingToken = bt;
    }

    /**
     * Get a localized and/or branded file in the default locale with the default class loader.
    * <p>Note that use of this call is similar to using the URL protocol <code>nbresloc</code>
    * (which may in fact be implemented using the fuller form of the method).
    * <p>The extension may be null, in which case no final dot will be appended.
    * If it is the empty string, the resource will end in a dot.
    * @param baseName base name of file, as dot-separated path (e.g. <code>some.dir.File</code>)
    * @param ext      extension of file (or <code>null</code>)
    * @return URL of matching localized file
    * @throws MissingResourceException if not found
     * @deprecated Use the <code>nbresloc</code> URL protocol instead. This method does a poor
     *             job of handling resources such as <samp>/some.dir/res.txt</samp> or
     *             <samp>/some/res.txt.sample</samp>.
    */
    @Deprecated
    public static synchronized URL getLocalizedFile(String baseName, String ext)
    throws MissingResourceException {
        return getLocalizedFile(baseName, ext, Locale.getDefault(), getLoader());
    }

    /**
     * Get a localized and/or branded file with the default class loader.
    * @param baseName base name of file, as dot-separated path (e.g. <code>some.dir.File</code>)
    * @param ext      extension of file (or <code>null</code>)
    * @param locale   locale of file
    * @return URL of matching localized file
    * @throws MissingResourceException if not found
     * @deprecated Use the <code>nbresloc</code> URL protocol instead. This method does a poor
     *             job of handling resources such as <samp>/some.dir/res.txt</samp> or
     *             <samp>/some/res.txt.sample</samp>.
    */
    @Deprecated
    public static synchronized URL getLocalizedFile(String baseName, String ext, Locale locale)
    throws MissingResourceException {
        return getLocalizedFile(baseName, ext, locale, getLoader());
    }

    /**
     * Get a localized and/or branded file.
    * @param baseName base name of file, as dot-separated path (e.g. <code>some.dir.File</code>)
    * @param ext      extension of file (or <code>null</code>)
    * @param locale   locale of file
    * @param loader  class loader to use
    * @return URL of matching localized file
    * @throws MissingResourceException if not found
     * @deprecated Use the <code>nbresloc</code> URL protocol instead. This method does a poor
     *             job of handling resources such as <samp>/some.dir/res.txt</samp> or
     *             <samp>/some/res.txt.sample</samp>.
    */
    @Deprecated
    public static synchronized URL getLocalizedFile(String baseName, String ext, Locale locale, ClassLoader loader)
    throws MissingResourceException {
        // [PENDING] in the future, could maybe do something neat if
        // USE_DEBUG_LOADER and ext is "html" or "txt" etc...
        URL lookup = null;
        Iterator<String> it = new LocaleIterator(locale);
        List<String> cacheCandidates = new ArrayList<>(10);
        String baseNameSlashes = baseName.replace('.', '/');
        Map<String,URL> perLoaderCache = localizedFileCache.get(loader);

        if (perLoaderCache == null) {
            localizedFileCache.put(loader, perLoaderCache = new HashMap<>());
        }

        // #31008: better use of domain cache priming.
        // [PENDING] remove this hack in case the domain cache is precomputed
        URL baseVariant;
        String path;

        if (ext != null) {
            path = baseNameSlashes + '.' + ext;
        } else {
            path = baseNameSlashes;
        }

        lookup = perLoaderCache.get(path);

        if (lookup == null) {
            baseVariant = loader.getResource(path);
        } else {
            // who cares? already in cache anyway
            baseVariant = null;
        }

        while (it.hasNext()) {
            String suffix = it.next();

            if (ext != null) {
                path = baseNameSlashes + suffix + '.' + ext;
            } else {
                path = baseNameSlashes + suffix;
            }

            lookup = perLoaderCache.get(path);

            if (lookup != null) {
                break;
            }

            cacheCandidates.add(path);

            if (suffix.length() == 0) {
                lookup = baseVariant;
            } else {
                lookup = loader.getResource(path);
            }

            if (lookup != null) {
                break;
            }
        }

        if (lookup == null) {
            path = baseName.replace('.', '/');

            if (ext != null) {
                path += ('.' + ext);
            }

            throw new MissingResourceException(
                "Cannot find localized resource " + path + " in " + loader, loader.toString(), path
            ); // NOI18N
        } else {
            // Note that this is not 100% accurate. If someone calls gLF on something
            // with a locale/branding combo such as _brand_ja, and the answer is found
            // as _ja, then a subsequent call with param _brand will find this _ja
            // version - since the localizing iterator does *not* have the property that
            // each subsequent item is more general than the previous. However, this
            // situation is very unlikely, so consider this close enough.
            it = cacheCandidates.iterator();

            while (it.hasNext()) {
                perLoaderCache.put(it.next(), lookup);
            }

            return lookup;
        }
    }

    /**
     * Find a localized and/or branded value for a given key and locale.
    * Scans through a map to find
    * the most localized match possible. For example:
    * <p><code><PRE>
    *   findLocalizedValue (hashTable, "keyName", new Locale ("cs_CZ"))
    * </PRE></code>
    * <p>This would return the first non-<code>null</code> value obtained from the following tests:
    * <UL>
    * <LI> <CODE>hashTable.get ("keyName_cs_CZ")</CODE>
    * <LI> <CODE>hashTable.get ("keyName_cs")</CODE>
    * <LI> <CODE>hashTable.get ("keyName")</CODE>
    * </UL>
    *
    * @param table mapping from localized strings to objects
    * @param key the key to look for
    * @param locale the locale to use
    * @return the localized object or <code>null</code> if no key matches
    */
    public static <T> T getLocalizedValue(Map<String,T> table, String key, Locale locale) {
        for (String suffix : NbCollections.iterable(new LocaleIterator(locale))) {
            String physicalKey = key + suffix;
            T v = table.get(physicalKey);

            if (v != null) {
                // ok
                if (USE_DEBUG_LOADER && (v instanceof String)) {
                    // Not read from a bundle, but still localized somehow:
                    @SuppressWarnings("unchecked")
                    T _v = (T) (((String) v) + " (?:" + physicalKey + ")"); // NOI18N;
                    return _v;
                } else {
                    return v;
                }
            }
        }

        return null;
    }

    /**
     * Find a localized and/or branded value for a given key in the default system locale.
    *
    * @param table mapping from localized strings to objects
    * @param key the key to look for
    * @return the localized object or <code>null</code> if no key matches
    * @see #getLocalizedValue(Map,String,Locale)
    */
    public static <T> T getLocalizedValue(Map<String,T> table, String key) {
        return getLocalizedValue(table, key, Locale.getDefault());
    }

    /**
     * Find a localized and/or branded value in a JAR manifest.
    * @param attr the manifest attributes
    * @param key the key to look for (case-insensitive)
    * @param locale the locale to use
    * @return the value if found, else <code>null</code>
    */
    public static String getLocalizedValue(Attributes attr, Attributes.Name key, Locale locale) {
        return getLocalizedValue(attr2Map(attr), key.toString().toLowerCase(Locale.US), locale);
    }

    /**
     * Find a localized and/or branded value in a JAR manifest in the default system locale.
    * @param attr the manifest attributes
    * @param key the key to look for (case-insensitive)
    * @return the value if found, else <code>null</code>
    */
    public static String getLocalizedValue(Attributes attr, Attributes.Name key) {
        // Yes, US locale is intentional! The attribute name may only be ASCII anyway.
        // It is necessary to lowercase it *as ASCII* as in Turkish 'I' does not go to 'i'!
        return getLocalizedValue(attr2Map(attr), key.toString().toLowerCase(Locale.US));
    }

    /** Necessary because Attributes implements Map; however this is dangerous!
    * The keys are Attributes.Name's, not Strings.
    * Also manifest lookups should not be case-sensitive.
    * (Though the locale suffix still will be!)
    */
    private static Map<String,String> attr2Map(Attributes attr) {
        return new AttributesMap(attr);
    }

    // ---- LOADING RESOURCE BUNDLES ----

    /**
    * Get a resource bundle with the default class loader and locale/branding.
    * <strong>Caution:</strong> {@link #getBundle(Class)} is generally
    * safer when used from a module as this method relies on the module's
    * classloader to currently be part of the system classloader. NetBeans
    * does add enabled modules to this classloader, however calls to
    * this variant of the method made in <a href="@org-openide-modules@/org/openide/modules/ModuleInstall.html#validate()">ModuleInstall.validate</a>,
    * or made soon after a module is uninstalled (due to background threads)
    * could fail unexpectedly.
    * @param baseName bundle basename
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static ResourceBundle getBundle(String baseName)
    throws MissingResourceException {
        return getBundle(baseName, Locale.getDefault(), getLoader());
    }

    /** Get a resource bundle in the same package as the provided class,
    * with the default locale/branding and the class' own classloader.
    * The usual style of invocation is {@link #getMessage(Class,String)}
     * or one of the other overloads taking message formats.
    *
    * @param clazz the class to take the package name from
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static ResourceBundle getBundle(Class<?> clazz)
    throws MissingResourceException {
        String name = findName(clazz);

        return getBundle(name, Locale.getDefault(), clazz.getClassLoader());
    }

    /** Finds package name for given class */
    private static String findName(final Class<?> clazz) {
        
        final String clsName = clazz.getName();
        final int indexOfLastDot = clsName.lastIndexOf('.');

        if (indexOfLastDot >= 0) {
            return clsName.substring(0, indexOfLastDot + 1).concat("Bundle"); // NOI18N)
        } else {
            // base package, search for bundle
            return "Bundle"; // NOI18N
        }
    }

    /**
    * Get a resource bundle with the default class loader and branding.
    * @param baseName bundle basename
    * @param locale the locale to use (but still uses {@link #getBranding default branding})
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static ResourceBundle getBundle(String baseName, Locale locale)
    throws MissingResourceException {
        return getBundle(baseName, locale, getLoader());
    }

    /** Get a resource bundle the hard way.
    * @param baseName bundle basename
    * @param locale the locale to use (but still uses {@link #getBranding default branding})
    * @param loader the class loader to use
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader)
    throws MissingResourceException {
        
        if (USE_DEBUG_LOADER) {
            loader = DebugLoader.get(loader);
        }

        // Could more simply use ResourceBundle.getBundle (plus some special logic
        // with MergedBundle to handle branding) instead of manually finding bundles.
        // However this code is faster and has some other desirable properties.
        // Cf. #13847.
        final ResourceBundle b = getBundleFast(baseName, locale, loader);

        if (b != null) {
            return b;
        } else {
            final MissingResourceException e = new MissingResourceException("No such bundle ".concat(baseName), 
                    baseName, null); // NOI18N

            if (Lookup.getDefault().lookup(ClassLoader.class) == null) {
                Exceptions.attachMessage(e, "Class loader not yet initialized in lookup"); // NOI18N
            } else {
                Exceptions.attachMessage(e, "Offending classloader: " + loader); // NOI18N
            }

            throw e;
        }
    }

    /**
     * Get a resource bundle by name.
     * Like {@link ResourceBundle#getBundle(String,Locale,ClassLoader)} but faster,
     * and also understands branding.
     * First looks for <samp>.properties</samp>-based bundles, then <samp>.class</samp>-based.
     * @param name the base name of the bundle, e.g. <samp>org.netbeans.modules.foo.Bundle</samp>
     * @param locale the locale to use
     * @param loader a class loader to search in
     * @return a resource bundle (locale- and branding-merged), or null if not found
     */
    private static ResourceBundle getBundleFast(final String name, 
            final Locale locale, final ClassLoader loader) {
        
        final Map<String,Reference<ResourceBundle>> map = fromBundleCache(loader);      
        final String key = buildKey(name, locale);

        synchronized (map) {
            final Reference<ResourceBundle> ref = map.get(key);
            ResourceBundle bundle = ref != null ? ref.get() : null;

            if (bundle == null) {
                bundle = loadBundle(name, locale, loader);
                if (bundle != null) {
                    map.put(key, new TimedSoftReference<>(bundle, map, key));
                } else {
                    // Used to cache misses as well, to make the negative test faster.
                    // However this caused problems: see #31578.
                }
            }
            return bundle;
        }
    }
    //--------------------------------------------------------------------------
    private static Map<String, Reference<ResourceBundle>> fromBundleCache(
            final ClassLoader loader) {

        synchronized (bundleCache) {
            return bundleCache.computeIfAbsent(loader, l -> new HashMap<>());
        }
    }
    //--------------------------------------------------------------------------
    private static String buildKey(final String name, final Locale locale) {

        final String localeStr = locale.toString();
        final int len = name.length() + 
                (brandingToken != null ? brandingToken.length() : 1) 
                + 2 + localeStr.length();

        return new StringBuilder(len).append(name).
                append('/').
                append(brandingToken != null ? brandingToken : '-').
                append('/').
                append(localeStr).
                toString();
    }

    /**
     * Load a resource bundle (without caching).
     * @param name the base name of the bundle, e.g. <samp>org.netbeans.modules.foo.Bundle</samp>
     * @param locale the locale to use
     * @param loader a class loader to search in
     * @return a resource bundle (locale- and branding-merged), or null if not found
     */
    private static ResourceBundle loadBundle(final String name, 
            final Locale locale, final ClassLoader loader) {
        
        final String sname = name.replace('.', '/');
        final List<String> reversedSuffixes = getReversedLocalizingSuffixes(locale);
        final Properties properties = new Properties();

        for (final String suffix : reversedSuffixes) {
            final String resource = sname + suffix + ".properties";

            
            // #49961: don't use getResourceAsStream; catch all errors opening it
            final URL url = loader != null ? loader.getResource(resource) : 
                    ClassLoader.getSystemResource(resource);

            if (url != null) {
                try {
                    // #51667: but in case we are in USE_DEBUG_LOADER mode, use gRAS (since getResource is not overridden)
                    final InputStream is = USE_DEBUG_LOADER ?
                        (loader != null ? loader.getResourceAsStream(resource) : ClassLoader.getSystemResourceAsStream(resource)) :
                            url.openStream();

                    // #NETBEANS-5181
                    try (final Reader reader = new InputStreamReader(is, newDecoder())) {
                        properties.load(reader);
                    } 
                } catch (final IOException e) {
                    Exceptions.attachMessage(e, "While loading: ".concat(resource)); // NOI18N
                    LOG.log(Level.WARNING, null, e);

                    return null;
                }
            } else if (suffix.length() == 0) {
                // No base *.properties. Try *.class.
                // Note that you may not mix *.properties w/ *.class this way.
                return loadBundleClass(name, sname, locale, reversedSuffixes, loader);
            }
        }

        return new PBundle(NbCollections.checkedMapByFilter(properties, 
                String.class, String.class, true), locale);
    }
    //--------------------------------------------------------------------------
    private static List<String> getReversedLocalizingSuffixes(final Locale locale) {
        
        final LinkedList<String> result = new LinkedList<>();
        
        new LocaleIterator(locale).forEachRemaining(s -> result.addFirst(s));
        return result;
    }
    //--------------------------------------------------------------------------
    private static CharsetDecoder newDecoder() {

        final String encoding = System.getProperty("java.util.PropertyResourceBundle.encoding");
        final UtfThenIsoCharset charset = "UTF-8".equals(encoding) ? 
                utfThenIsoCharsetOnlyUTF8 : utfThenIsoCharset;
        
        return "ISO-8859-1".equals(encoding)
                ? ISO_8859_1.newDecoder() : charset.newDecoder();
    }

    /**
     * Load a class-based resource bundle.
     * @param name the base name of the bundle, e.g. <samp>org.netbeans.modules.foo.Bundle</samp>
     * @param sname the name with slashes, e.g. <samp>org/netbeans/modules/foo/Bundle</samp>
     * @param locale the locale to use
     * @param suffixes a list of suffixes to apply to the bundle name, in <em>increasing</em> order of specificity
     * @param loader a class loader to search in
     * @return a resource bundle (merged according to the suffixes), or null if not found
     */
    private static ResourceBundle loadBundleClass(
            final String name, final String sname, final Locale locale,
            final List<String> suffixes, final ClassLoader loader) {

        if (loader != null && loader.getResource(sname.concat(".class")) == null) { // NOI18N
            // No chance - no base bundle. Don't waste time catching CNFE.
            return null;
        } else {
            ResourceBundle master = null;

            for (final String suffix : suffixes) {
                try {
                    final Class<? extends ResourceBundle> cls
                            = Class.forName(name.concat(suffix), true, loader).
                                    asSubclass(ResourceBundle.class);
                    ResourceBundle bundle = cls.newInstance();

                    if (master == null) {
                        master = bundle;
                    } else {
                        master = new MergedBundle(locale, bundle, master);
                    }
                } catch (final ClassNotFoundException cnfe) {
                    // fine - ignore
                } catch (final Exception e) {
                    LOG.log(WARNING, null, e);
                } catch (final LinkageError e) {
                    LOG.log(WARNING, null, e);
                }
            }
            return master;
        }
    }

    //
    // Helper methods to simplify localization of messages
    //

    /**
     * Finds a localized and/or branded string in a bundle.
    * @param clazz the class to use to locate the bundle (see {@link #getBundle(Class)} for details)
    * @param resName name of the resource to look for
    * @return the string associated with the resource
    * @throws MissingResourceException if either the bundle or the string cannot be found
    */
    public static String getMessage(Class<?> clazz, String resName)
    throws MissingResourceException {
        return getBundle(clazz).getString(resName);
    }

    /**
     * Finds a localized and/or branded string in a bundle and formats the message
    * by passing requested parameters.
    *
    * @param clazz the class to use to locate the bundle (see {@link #getBundle(Class)} for details)
    * @param resName name of the resource to look for
    * @param param1 the argument to use when formatting the message
    * @return the string associated with the resource
    * @throws MissingResourceException if either the bundle or the string cannot be found
    * @see java.text.MessageFormat#format(String,Object[])
    */
    public static String getMessage(Class<?> clazz, String resName, Object param1)
    throws MissingResourceException {
        return getMessage(clazz, resName, new Object[] { param1 });
    }

    /**
     * Finds a localized and/or branded string in a bundle and formats the message
    * by passing requested parameters.
    *
    * @param clazz the class to use to locate the bundle (see {@link #getBundle(Class)} for details)
    * @param resName name of the resource to look for
    * @param param1 the argument to use when formatting the message
    * @param param2 the second argument to use for formatting
    * @return the string associated with the resource
    * @throws MissingResourceException if either the bundle or the string cannot be found
    * @see java.text.MessageFormat#format(String,Object[])
    */
    public static String getMessage(Class<?> clazz, String resName, Object param1, Object param2)
    throws MissingResourceException {
        return getMessage(clazz, resName, new Object[] { param1, param2 });
    }

    /**
     * Finds a localized and/or branded string in a bundle and formats the message
    * by passing requested parameters.
    *
    * @param clazz the class to use to locate the bundle (see {@link #getBundle(Class)} for details)
    * @param resName name of the resource to look for
    * @param param1 the argument to use when formatting the message
    * @param param2 the second argument to use for formatting
    * @param param3 the third argument to use for formatting
    * @return the string associated with the resource
    * @throws MissingResourceException if either the bundle or the string cannot be found
    * @see java.text.MessageFormat#format(String,Object[])
    */
    public static String getMessage(Class<?> clazz, String resName, Object param1, Object param2, Object param3)
    throws MissingResourceException {
        return getMessage(clazz, resName, new Object[] { param1, param2, param3 });
    }

    /**
     * Finds a localized and/or branded string in a bundle and formats the message
    * by passing requested parameters.
    *
    * @param clazz the class to use to locate the bundle (see {@link #getBundle(Class)} for details)
    * @param resName name of the resource to look for
    * @param param1 the argument to use when formatting the message
    * @param param2 the second argument to use for formatting
    * @param param3 the third argument to use for formatting
    * @param param4 the fourth argument to use for formatting
    * @param params fifth, sixth, ... arguments as needed
    * @return the string associated with the resource
    * @throws MissingResourceException if either the bundle or the string cannot be found
    * @see java.text.MessageFormat#format(String,Object[])
    * @since org.openide.util 7.27
    */
    public static String getMessage(Class<?> clazz, String resName, Object param1, Object param2, Object param3, Object param4, Object... params)
    throws MissingResourceException {
        Object[] allParams = new Object[params.length + 4];
        allParams[0] = param1;
        allParams[1] = param2;
        allParams[2] = param3;
        allParams[3] = param4;
        System.arraycopy(params, 0, allParams, 4, params.length);
        return getMessage(clazz, resName, allParams);
    }

    /**
     * Finds a localized and/or branded string in a bundle and formats the message
    * by passing requested parameters.
    *
    * @param clazz the class to use to locate the bundle (see {@link #getBundle(Class)} for details)
    * @param resName name of the resource to look for
    * @param arr array of parameters to use for formatting the message
    * @return the string associated with the resource
    * @throws MissingResourceException if either the bundle or the string cannot be found
    * @see java.text.MessageFormat#format(String,Object[])
    */
    public static String getMessage(Class<?> clazz, String resName, Object[] arr)
    throws MissingResourceException {
        return java.text.MessageFormat.format(getMessage(clazz, resName), arr);
    }

    /** @return default class loader which is used, when we don't have
    * any other class loader. (in function getBundle(String), getLocalizedFile(String),
    * and so on...
    */
    private static ClassLoader getLoader() {
        ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);

        return (c != null) ? c : ClassLoader.getSystemClassLoader();
    }

    /**
     * Get a list of all suffixes used to search for localized/branded resources.
     * Based on the default locale and branding, returns the list of suffixes
     * which various <code>NbBundle</code> methods use as the search order.
     * For example, when {@link #getBranding} returns <code>branding</code>
     * and the default locale is German, you might get a sequence such as:
     * <ol>
     * <li><samp>"_branding_de"</samp>
     * <li><samp>"_branding"</samp>
     * <li><samp>"_de"</samp>
     * <li><samp>""</samp>
     * </ol>
     * @return a read-only iterator of type <code>String</code>
     * @since 1.1.5
     */
    public static Iterator<String> getLocalizingSuffixes() {
        return new LocaleIterator(Locale.getDefault());
    }

    /**
     * Do not use.
     * @param loaderFinder ignored
     * @deprecated Useless.
     */
    @Deprecated
    public static void setClassLoaderFinder(ClassLoaderFinder loaderFinder) {
        throw new Error();
    }

    /**
     * Creates a helper class with static definitions of bundle keys.
     * <p>
     * The generated class will be called {@code Bundle} and be in the same package.
     * Each key is placed in a {@code Bundle.properties} file also in the same package,
     * and the helper class gets a method with the same name as the key
     * (converted to a valid Java identifier as needed)
     * which loads the key from the (possibly now localized) bundle using {@link NbBundle#getMessage(Class, String)}.
     * The method will have as many arguments (of type {@code Object}) as there are message format parameters.
     * </p>
     * <p>It is an error to duplicate a key within a package, even if the duplicates are from different compilation units.</p>
     * <p>Example usage:</p>
     * <pre>
     * package some.where;
     * import org.openide.util.NbBundle.Messages;
     * import static some.where.Bundle.*;
     * import org.openide.DialogDisplayer;
     * import org.openide.NotifyDescriptor;
     * class Something {
     *     &#64;Messages({
     *         "dialog.title=Bad File",
     *         "# {0} - file path",
     *         "dialog.message=The file {0} was invalid."
     *     })
     *     void showError(File f) {
     *         NotifyDescriptor d = new NotifyDescriptor.Message(
     *             dialog_message(f), NotifyDescriptor.ERROR_MESSAGE);
     *         d.setTitle(dialog_title());
     *         DialogDisplayer.getDefault().notify(d);
     *     }
     * }
     * </pre>
     * <p>which generates during compilation {@code Bundle.java}:</p>
     * <pre>
     * class Bundle {
     *     static String dialog_title() {...}
     *     static String dialog_message(Object file_path) {...}
     * }
     * </pre>
     * <p>and {@code Bundle.properties}:</p>
     * <pre>
     * dialog.title=Bad File
     * # {0} - file path
     * dialog.message=The file {0} was invalid.
     * </pre>
     * @since org.openide.util 8.10 (available also on fields since 8.22)
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
    public @interface Messages {
        /**
         * List of key/value pairs.
         * Each must be of the form {@code key=Some Value}.
         * Anything is permitted in the value, including newlines.
         * Unlike in a properties file, there should be no whitespace before the key or around the equals sign.
         * Values containing <code>{0}</code> etc. are assumed to be message formats and so may need escapes for metacharacters such as {@code '}.
         * A line may also be a comment if it starts with {@code #}, which may be useful for translators;
         * it is recommended to use the format {@code # {0} - summary of param}.
         */
        String[] value();
    }

    /**
     * Do not use.
     * @deprecated Useless.
     */
    @Deprecated
    public static interface ClassLoaderFinder {
        /**
         * Do not use.
         * @return nothing
         * @deprecated Useless.
         */
        @Deprecated
        public ClassLoader find();
    }

    private static class AttributesMap extends HashMap<String,String> {
        
        private final Attributes attrs;

        private AttributesMap(final Attributes attrs) {
            
            super(7);
            this.attrs = attrs;
        }

        @Override
        public String get(final Object key) {

            if (key instanceof String) {
                try {
                    return this.attrs.getValue(new Attributes.Name((String)key));
                } catch (final IllegalArgumentException e) {
                    // Robustness, and workaround for reported MRJ locale bug:
                    LOG.log(FINE, null, e);
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * A resource bundle based on <samp>.properties</samp> files (or any map).
     */
    private static final class PBundle extends ResourceBundle {
        private final Map<String,String> m;
        private final Locale locale;

        /**
         * Create a new bundle based on a map.
         * @param m a map from resources keys to values (typically both strings)
         * @param locale the locale it represents <em>(informational)</em>
         */
        public PBundle(Map<String,String> m, Locale locale) {
            this.m = m;
            this.locale = locale;
        }

        public @Override Enumeration<String> getKeys() {
            return Collections.enumeration(m.keySet());
        }

        protected @Override Object handleGetObject(String key) {
            return m.get(key);
        }

        public @Override Locale getLocale() {
            return locale;
        }
    }

    /** Special resource bundle which delegates to two others.
     * Ideally could just set the parent on the first, but this is protected, so...
     */
    private static class MergedBundle extends ResourceBundle {
        private final Locale loc;
        private final ResourceBundle sub1;
        private final ResourceBundle sub2;

        /**
         * Create a new bundle delegating to two others.
         * @param loc the locale it represents <em>(informational)</em>
         * @param sub1 one delegate (taking precedence over the other in case of overlap)
         * @param sub2 the other (weaker) delegate
         */
        public MergedBundle(Locale loc, ResourceBundle sub1, ResourceBundle sub2) {
            this.loc = loc;
            this.sub1 = sub1;
            this.sub2 = sub2;
        }

        public @Override Locale getLocale() {
            return loc;
        }

        public @Override Enumeration<String> getKeys() {
            return Enumerations.removeDuplicates(Enumerations.concat(sub1.getKeys(), sub2.getKeys()));
        }

        protected @Override Object handleGetObject(String key) throws MissingResourceException {
            try {
                return sub1.getObject(key);
            } catch (MissingResourceException mre) {
                // Ignore exception, and...
                return sub2.getObject(key);
            }
        }
    }

    /** This class (enumeration) gives all localized sufixes using nextElement
    * method. It goes through given Locale and continues through Locale.getDefault()
    * Example 1:
    *   Locale.getDefault().toString() -> "_en_US"
    *   you call new LocaleIterator(new Locale("cs", "CZ"));
    *  ==> You will gets: "_cs_CZ", "_cs", "", "_en_US", "_en"
    *
    * Example 2:
    *   Locale.getDefault().toString() -> "_cs_CZ"
    *   you call new LocaleIterator(new Locale("cs", "CZ"));
    *  ==> You will gets: "_cs_CZ", "_cs", ""
    *
    * If there is a branding token in effect, you will get it too as an extra
    * prefix, taking precedence, e.g. for the token "f4jce":
    *
    * "_f4jce_cs_CZ", "_f4jce_cs", "_f4jce", "_f4jce_en_US", "_f4jce_en", "_cs_CZ", "_cs", "", "_en_US", "_en"
    *
    * Branding tokens with underscores are broken apart naturally: so e.g.
    * branding "f4j_ce" looks first for "f4j_ce" branding, then "f4j" branding, then none.
    */
    private static class LocaleIterator implements Iterator<String> {
        /** this flag means, if default locale is in progress */
        private boolean defaultInProgress;

        /** this flag means, if empty suffix was exported yet */
        private boolean empty = false;

        /** current locale, and initial locale */
        private Locale locale;

        /** current locale, and initial locale */
        private final Locale initLocale;

        /** current suffix which will be returned in next calling nextElement */
        private String current;

        /** the branding string in use */
        private String branding;

        //----------------------------------------------------------------------
        public LocaleIterator(final Locale locale) {

            this.locale = locale;
            this.initLocale = locale;
            this.defaultInProgress = locale.equals(Locale.getDefault());
            this.current = "_".concat(locale.toString());
            this.branding = brandingToken != null ? "_".concat(brandingToken) : ""; // NOI18N
        }
        //----------------------------------------------------------------------
        @Override
        public String next() throws NoSuchElementException {

            if (this.current == null) {
                throw new NoSuchElementException();
            }
            final String result = this.branding.concat(this.current);
            updateCurrent();
            return result;
        }
        //----------------------------------------------------------------------
        private void updateCurrent() {

            final int lastUnderbar = this.current.lastIndexOf('_');
            if (lastUnderbar == 0) {
                if (this.empty) {
                    resetIteration();
                } else {
                    this.current = ""; // NOI18N
                    this.empty = true;
                }
            } else {
                if (lastUnderbar == -1) {
                    if (this.defaultInProgress) {
                        resetIteration();
                    } else {
                        // [PENDING] stuff with trying the default locale
                        // after the real one does not actually seem to work...
                        this.locale = Locale.getDefault();
                        this.current = "_".concat(this.locale.toString()); // NOI18N
                        this.defaultInProgress = true;
                    }
                } else {
                    this.current = this.current.substring(0, lastUnderbar);
                }
            }
        }
        //----------------------------------------------------------------------
        private void resetIteration() {

            if (this.branding.isEmpty()) {
                this.current = null; // finish iteration
            } else {
                this.current = "_".concat(this.initLocale.toString());

                final int index = this.branding.lastIndexOf('_');
                this.branding = index > 0 ? this.branding.substring(0, index) : ""; // NOI18N
                this.empty = false;
            }
        }
        //----------------------------------------------------------------------
        @Override
        public boolean hasNext() {

            return this.current != null;
        }
        //----------------------------------------------------------------------
    }
     // end of LocaleIterator
    
    
    /**
     * Local charset to decode using UTF-8 by default, but automatically switching to ISO-8859-1 if UTF-8 decoding fails.
     * 
     */
    private static class UtfThenIsoCharset extends Charset {

        private final boolean onlyUTF8;

        /**
         *
         * @param acceptOnlyUTF8 If true there is no automatic switch to ISO-8859-1 if UTF-8 decoding fails.
         */
        public UtfThenIsoCharset(boolean acceptOnlyUTF8) {
            super(UtfThenIsoCharset.class.getCanonicalName(), null);
            this.onlyUTF8 = acceptOnlyUTF8;
        }

        @Override
        public boolean contains(Charset arg0) {
            return this.equals(arg0);
        }

        @Override
        public CharsetDecoder newDecoder() {
            return new UtfThenIsoDecoder(this, 1.0f, 1.0f);
        }

        @Override
        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

         private final class UtfThenIsoDecoder extends CharsetDecoder {

            private final CharsetDecoder decoderUTF;
            private CharsetDecoder decoderISO;  // Not null means we switched to ISO

            protected UtfThenIsoDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
                super(cs, averageCharsPerByte, maxCharsPerByte);

                decoderUTF = UTF_8.newDecoder()
                        .onMalformedInput(REPORT) // We want to be informed of this error
                        .onUnmappableCharacter(REPORT);  // We want to be informed of this error                
            }

            @Override
            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {

                if (decoderISO != null) {
                    // No turning back once we've switched to ISO
                    return decoderISO.decode(in, out, false);
                }

                // To rewind if need to retry with ISO decoding
                in.mark();
                out.mark();

                
                // UTF decoding
                final CoderResult cr = decoderUTF.decode(in, out, false);
                if (cr.isUnderflow() || cr.isOverflow()) {
                    // Normal results
                    return cr;
                }

                // If we're here there was a malformed-input or unmappable-character error with the UTF decoding
                if (UtfThenIsoCharset.this.onlyUTF8) {
                    // But can't switch to ISO
                    return cr;
                }

                // Switch to ISO
                in.reset();
                out.reset();
                decoderISO = ISO_8859_1.newDecoder();
                return decoderISO.decode(in, out, false);
            }
        }
    }
    
}
