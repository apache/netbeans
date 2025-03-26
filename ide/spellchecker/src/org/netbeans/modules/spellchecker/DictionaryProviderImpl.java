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
package org.netbeans.modules.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.netbeans.modules.spellchecker.spi.dictionary.Dictionary;
import org.netbeans.modules.spellchecker.spi.dictionary.DictionaryProvider;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.spellchecker.spi.dictionary.DictionaryProvider.class)
public class DictionaryProviderImpl implements DictionaryProvider {
    
    /** Creates a new instance of DictionaryProviderImpl */
    public DictionaryProviderImpl() {
    }

    private Map<String, Dictionary> dictionaries = new HashMap<String, Dictionary>();
    
//    public DictionaryImpl getDefault() {
//        return getDictionary(Locale.getDefault());
//    }
    
    public synchronized void clearDictionaries() {
        dictionaries.clear();
    }
    
    public synchronized Dictionary getDictionary(Locale locale) {
        Iterator<String> suffixes = getLocalizingSuffixes(locale);
        
        while (suffixes.hasNext()) {
            Dictionary current = dictionaries.get(suffixes.next());
            
            if (current != null)
                return current;
        }
        
        return createDictionary(locale);
    }
    
    public static synchronized Locale[] getInstalledDictionariesLocales() {
        Collection<Locale> hardcoded = new HashSet<Locale>();
        Collection<Locale> maskedHardcoded = new HashSet<Locale>();
        Collection<Locale> user = new HashSet<Locale>();
        
        for (File dictDir : InstalledFileLocator.getDefault().locateAll("modules/dict", null, false)) {
            File[] children = dictDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().startsWith("dictionary_");
                }
            });
            
            if (children == null)
                continue;
            
            for (int cntr = 0; cntr < children.length; cntr++) {
                String name = children[cntr].getName();
                
                name = name.substring("dictionary_".length());

                Collection<Locale> target;

                if (name.endsWith("_hidden")) {
                    target = maskedHardcoded;
                } else {
                    if (name.endsWith(".description")) {
                        target = hardcoded;
                    } else {
                        target = user;
                    }
                }
                
                int dot = name.indexOf('.');
                
                if (dot != (-1))
                    name = name.substring(0, dot);
                
                target.add(Utilities.name2Locale(name));
            }
        }

        hardcoded.removeAll(maskedHardcoded);
        hardcoded.addAll(user);
        return hardcoded.toArray(new Locale[0]);
    }
    
    private synchronized Dictionary createDictionary(Locale locale) {
        try {
            List<URL> sources = new ArrayList<URL>();
            String suffix = getDictionaryStream(locale, sources);
            
            if (suffix == null) {
                return null;
            }

            Dictionary dict = TrieDictionary.getDictionary(suffix, sources);
//            DictionaryImpl dict = new DictionaryImpl(locale, suffix, streams);
//            
            dictionaries.put(suffix, dict);
            
            return dict;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }

    static String getDictionaryStream(Locale locale, List<URL> streams) throws IOException {
        Iterator suffixes = getLocalizingSuffixes(locale);
        
        while (suffixes.hasNext()) {
            String currentSuffix = (String) suffixes.next();
            
            File file = InstalledFileLocator.getDefault().locate("modules/dict/dictionary" + currentSuffix + ".txt", null, false);
            
            if (file != null) {
                streams.add(file.toURI().toURL());
                return currentSuffix;
            }

            String cnb = null;
            if (currentSuffix.matches("_en(|_GB|_US)")) { // NOI18N
                // Just hardcode this one for now.
                cnb = "org.netbeans.modules.spellchecker.dictionary_en"; // NOI18N
            }
            file = InstalledFileLocator.getDefault().locate("modules/dict/dictionary" + currentSuffix + ".description", cnb, false);

            if (file != null && InstalledFileLocator.getDefault().locate("modules/dict/dictionary" + currentSuffix + ".description_hidden", null, false) == null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

                try {
                    String line;

                    while ((line = in.readLine()) != null) {
                        streams.add(new URL(line));
                    }

                    return currentSuffix;
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        
        return null;
    }

    //Copied from NbBundle:
    /** Get a list of all suffixes used to search for localized resources.
     * Based on the default locale and branding, returns the list of suffixes
     * which various <code>NbBundle</code> methods use as the search order.
     * For example, you might get a sequence such as:
     * <ol>
     * <li><samp>"_branding_de"</samp>
     * <li><samp>"_branding"</samp>
     * <li><samp>"_de"</samp>
     * <li><samp>""</samp>
     * </ol>
     * @return a read-only iterator of type <code>String</code>
     * @since 1.1.5
     */
    static Iterator<String> getLocalizingSuffixes(Locale locale) {
        return new LocaleIterator(locale);
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
    private static class LocaleIterator extends Object implements Iterator<String> {
//        /** this flag means, if default locale is in progress */
//        private boolean defaultInProgress = false;
        
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
        public LocaleIterator(Locale locale) {
            this.locale = this.initLocale = locale;
//            if (locale.equals(Locale.getDefault())) {
//                defaultInProgress = true;
//            }
            current = '_' + locale.toString();
            if (NbBundle.getBranding() == null)
                branding = null;
            else
                branding = "_" + NbBundle.getBranding(); // NOI18N
            //System.err.println("Constructed: " + this);
        }
        
        /** @return next sufix.
         * @exception NoSuchElementException if there is no more locale sufix.
         */
        public String next() throws NoSuchElementException {
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
                    reset();
                else {
                    current = ""; // NOI18N
                    empty = true;
                }
            }
            else {
                if (lastUnderbar == -1) {
//                    if (defaultInProgress)
                        reset();
//                    else {
//                        // [PENDING] stuff with trying the default locale
//                        // after the real one does not actually seem to work...
//                        locale = Locale.getDefault();
//                        current = '_' + locale.toString();
//                        defaultInProgress = true;
//                    }
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
        private void reset() {
            if (branding != null) {
                current = '_' + initLocale.toString();
                int idx = branding.lastIndexOf('_');
                if (idx == 0)
                    branding = null;
                else
                    branding = branding.substring(0, idx);
                empty = false;
            } else {
                current = null;
            }
        }
        
        /** Tests if there is any sufix.*/
        public boolean hasNext() {
            return (current != null);
        }
        
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
    } // end of LocaleIterator

}
