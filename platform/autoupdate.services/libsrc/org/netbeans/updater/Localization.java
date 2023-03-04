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

package org.netbeans.updater;

import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

class Localization {

    private static final String FILE_SEPARATOR = System.getProperty ("file.separator"); // NOI18N
    private static final String LOCALE_DIR = "modules" + FILE_SEPARATOR + "ext"  + FILE_SEPARATOR + "locale"; // NOI18N
    private static final String BUNDLE_NAME = "org/netbeans/updater/Bundle"; // NOI18N
    private static final String BUNDLE_EXT = ".properties"; // NOI18N
    private static final String UPDATER_JAR = "updater"; // NOI18N
    private static final String UPDATER_JAR_EXT = ".jar"; // NOI18N
    
    private static ClassLoader brandedLoader = null;
    
    private static String brandingToken = null;
    
    private static Map<String, ResourceBundle> bundleCache = new HashMap<String, ResourceBundle>(); // XXX: this is leaking cache

    public static String getBranding() {
        if (brandingToken != null) {
            init();
        }
        return brandingToken;
    }
    
    public static String getBrandedString( String key ) {
        init(); // When not initialized do so
                
        // Let's try to find a bundle
        for( LocaleIterator li = new LocaleIterator( Locale.getDefault() ); li.hasNext(); ) {
            try {
                ResourceBundle bundle = findBrandedBundle( (String)li.next() ); 
                
                if ( bundle != null ) {                    
                    // So we have the bundle, now we need the string
                    String brandedString = bundle.getString( key );
                    if ( brandedString != null ) {
                        return brandedString; // We even found the string
                    }
                    // Continue
                }
            } 
            catch ( java.util.MissingResourceException e ) {
                // No string, no problem, let's try other one
            }
        }
        return null;
    }
    
    private static ResourceBundle findBrandedBundle( String loc ) {
        
        ResourceBundle bundle = bundleCache.get( loc ); // Maybe is in cache
        if ( bundle != null ) {
            return bundle;
        }
        
        // Was not in cache
        
        
        InputStream is = brandedLoader.getResourceAsStream( BUNDLE_NAME + loc + BUNDLE_EXT );        
        if (is != null) {                
            try {
                try {
                    Properties p = new Properties();
                    p.load(is);
                    bundle= new PBundle( p, new Locale( "" ) );
                    bundleCache.put( loc, bundle );
                    return bundle;
                } finally {
                    is.close();
                }
            } catch (IOException e) {                
                return null;
            }
        }
                
        return null;
    }
    
    
    public static URL getBrandedResource( String base, String ext ) {
        init(); // When not initialized do so
        
        // Let's try all the possibilities
        for( LocaleIterator li = new LocaleIterator( Locale.getDefault() ); li.hasNext(); ) {
            URL url = brandedLoader.getResource( base + li.next() + ext );
            if ( url != null ) {                
                return url;
            }
        }
        
        return null;
    }
    
    
    public static InputStream getBrandedResourceAsStream( String base, String ext ) {
        init(); // When not initialized do so
        
        // Let's try all the possibilities
        for( LocaleIterator li = new LocaleIterator( Locale.getDefault() ); li.hasNext(); ) {
            InputStream is = brandedLoader.getResourceAsStream( base + li.next() + ext );
            if ( is != null ) {                
                return is;
            }
        }
        
        return null;
    }
    
    public static void setBranding (String branding) {
        brandingToken = branding;
    }
    
    // Private methods ---------------------------------------------------------
    private static synchronized void init() {
        if (brandingToken == null) {
            // Initialize the branding token
            brandingToken = initBranding();
        }
        if (brandedLoader == null) {
            
            // Fallback to default class loader
            brandedLoader = Localization.class.getClassLoader();
            
            // Try to find some localized jars and store the URLS
            List<URL> locJarURLs = new ArrayList<URL>();
                        
            for( LocaleIterator li = new LocaleIterator( Locale.getDefault() ); li.hasNext(); ) {
                String localeName = li.next().toString ();
                // loop for clusters
                Iterator<File> it = UpdateTracking.clusters (true).iterator ();
                while (it.hasNext ()) {
                    File cluster = it.next();
                    File locJar = new File( cluster.getPath () + FILE_SEPARATOR + LOCALE_DIR + FILE_SEPARATOR + UPDATER_JAR + localeName + UPDATER_JAR_EXT );
                    if ( locJar.exists() ) {  // File exists
                        try {
                            locJarURLs.add( locJar.toURI().toURL() ); // Convert to URL
                        }
                        catch ( MalformedURLException e ) {
                            // dont use and ignore
                        }
                    }
                }
            }
            
            if ( !locJarURLs.isEmpty() ) {  // we've found some localization jars
                // Make an array of URLs
                URL urls[] = new URL[ locJarURLs.size() ];
                locJarURLs.toArray( urls );

                // Create the new classLoader
                brandedLoader = new URLClassLoader( urls, brandedLoader );         
            }
            
        }
    }
    
    /** Returns current branding
     */    
    private static String initBranding() {
        BufferedReader in = null;
        String s = null;
        try {
            if (UpdateTracking.getPlatformDir () == null) {
                return s;
            }
            File brandf = new File (UpdateTracking.getPlatformDir(),
                    "lib" + FILE_SEPARATOR + "branding");  // NOI18N
            in = new BufferedReader(new FileReader(brandf));
            if (in.ready()) {
                XMLUtil.LOG.warning("It's obsolete. Use --branding <branding> instead 'branding' file.");
                s = in.readLine();
            }
        } 
        catch (IOException e) {                            
        } 
        finally {
            if (in != null) try { in.close(); } catch (IOException e) { /* ignore */ };
        }
        return s;
    }
    
    /** 
     * =============================================================================
     *                             N O T I C E
     * -----------------------------------------------------------------------------
     * This class was copyied from NbBundle. The reason is that the updater must not
     * use any NetBeans class in order to be able to update them.
     * -----------------------------------------------------------------------------
     *
     * This class (enumeration) gives all localized sufixes using nextElement
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
    private static class LocaleIterator extends Object implements Iterator {
        /** this flag means, if default locale is in progress */
        private boolean defaultInProgress = false;

        /** this flag means, if empty sufix was exported yet */
        private boolean empty = false;

        /** current locale, and initial locale */
        private Locale locale, initLocale;

        /** current sufix which will be returned in next calling nextElement */
        private String current;

        /** the branding string in use */
        private String branding;

        /** Creates new LocaleIterator for given locale.
        * @param locale given Locale
        */
        public LocaleIterator (Locale locale) {
            this.locale = this.initLocale = locale;
            if (locale.equals(Locale.getDefault())) {
                defaultInProgress = true;
            }
            current = '_' + locale.toString();
            if (brandingToken == null)
                branding = null;
            else
                branding = "_" + brandingToken; // NOI18N
            //System.err.println("Constructed: " + this);
        }

        /** @return next sufix.
        * @exception NoSuchElementException if there is no more locale sufix.
        */
        public Object next () throws NoSuchElementException {
            if (current == null)
                throw new NoSuchElementException();

            final String ret;
            if (branding == null) {
                ret = current;
            } else {
                ret = branding + current;
            }
            int lastUnderbar = current.lastIndexOf('_');
            if (lastUnderbar == 0) {
                if (empty)
                    reset ();
                else {
                    current = ""; // NOI18N
                    empty = true;
                }
            }
            else {
                if (lastUnderbar == -1) {
                    if (defaultInProgress)
                        reset ();
                    else {
                        // [PENDING] stuff with trying the default locale
                        // after the real one does not actually seem to work...
                        locale = Locale.getDefault();
                        current = '_' + locale.toString();
                        defaultInProgress = true;
                    }
                }
                else {
                    current = current.substring(0, lastUnderbar);
                }
            }
            //System.err.println("Returning: `" + ret + "' from: " + this);
            return ret;
        }

        /** Finish a series.
         * If there was a branding prefix, restart without that prefix
         * (or with a shorter prefix); else finish.
         */
        private void reset () {
            if (branding != null) {
                current = '_' + initLocale.toString ();
                int idx = branding.lastIndexOf ('_');
                if (idx == 0)
                    branding = null;
                else
                    branding = branding.substring (0, idx);
                empty = false;
            } else {
                current = null;
            }
        }

        /** Tests if there is any sufix.*/
        public boolean hasNext () {
            return (current != null);
        }

        @Override
        public void remove () throws UnsupportedOperationException {
            throw new UnsupportedOperationException ();
        }

    } // end of LocaleIterator
    
    /**
     * =============================================================================
     *                             N O T I C E
     * -----------------------------------------------------------------------------
     * YASC - Yet Another Stolen Class
     * -----------------------------------------------------------------------------     
     * A resource bundle based on <samp>.properties</samp> files (or any map).
     */
    private static final class PBundle extends ResourceBundle {
        private final Map<String, String> m;
        private final Locale locale;
        /**
         * Create a new bundle based on a map.
         * @param m a map from resources keys to values (typically both strings)
         * @param locale the locale it represents <em>(informational)</em>
         */
        @SuppressWarnings("unchecked")
        public PBundle(Map m, Locale locale) {
            this.m = m;
            this.locale = locale;
        }
        public Enumeration<String> getKeys() {
            return Collections.enumeration(m.keySet());
        }
        protected Object handleGetObject(String key) {
            return m.get(key);
        }
        @Override public Locale getLocale() {
            return locale;
        }
    }
    
}
