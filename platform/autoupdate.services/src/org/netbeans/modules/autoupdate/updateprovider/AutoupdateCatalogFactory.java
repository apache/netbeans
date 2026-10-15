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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogFactory {
    private static final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.updateproviders.AutoupdateCatalogFactory");
    
    private AutoupdateCatalogFactory () {
    }
    
    private static final String UPDATE_VERSION_PROP = "netbeans.autoupdate.version"; // NOI18N
    private static final String UPDATE_VERSION = "1.23"; // NOI18N
    private static final String IDE_HASH_CODE = "netbeans.hash.code"; // NOI18N
    private static final String SYSPROP_COUNTRY = "netbeans.autoupdate.country"; // NOI18N
    private static final String SYSPROP_LANGUAGE = "netbeans.autoupdate.language"; // NOI18N
    private static final String SYSPROP_VARIANT = "netbeans.autoupdate.variant"; // NOI18N
    private static final String PROP_QUALIFIED_IDENTITY = "qualifiedId"; // NOI18N
    
    public static final String ORIGINAL_URL = "originalUrl"; // NOI18N
    public static final String ORIGINAL_DISPLAY_NAME = "originalDisplayName"; // NOI18N
    public static final String ORIGINAL_ENABLED = "originalEnabled"; // NOI18N
    public static final String ORIGINAL_CATEGORY_NAME = "originalCategoryName"; // NOI18N
    public static final String ORIGINAL_CATEGORY_ICON_BASE = "originalCategoryIconBase"; // NOI18N
    public static final String ORIGINAL_TRUSTED = "originalTrusted"; // NOI18N
    
    public static UpdateProvider createUpdateProvider (FileObject fo) {
        String sKey = (String) fo.getAttribute ("url_key"); // NOI18N
        URL url;
        String name;
        if (sKey != null) {
            err.log(Level.WARNING, "{0}: url_key attribute deprecated in favor of url", fo.getPath());
            String remoteBundleName = (String) fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
            assert remoteBundleName != null : "remoteBundleName should found in fo: " + fo;
            String localizedValue = null;
            ResourceBundle bundle = null;
            try {
                if (remoteBundleName == null) {
                    err.log(Level.WARNING, "Cannot find Bundle to read 'url_key' attribute {0}, trying use the 'url_key' itself...", sKey);
                    localizedValue = sKey;
                } else {
                    bundle = NbBundle.getBundle(remoteBundleName);
                    localizedValue = bundle.getString (sKey);
                }
                url = new URL (localizedValue);
            } catch (MissingResourceException mre) {
                assert false : bundle + " should contain key " + sKey;
                return null;
            } catch (MalformedURLException urlex) {
                assert false : "MalformedURLException when parsing name " + localizedValue;
                return null;
            }
            name = sKey;
        } else {
            Object o = fo.getAttribute("url"); // NOI18N
            try {
                if (o instanceof String) {
                    url = new URL ((String) o);
                } else {
                    url = (URL) o;
                }
            } catch (MalformedURLException urlex) {
                err.log (Level.INFO, urlex.getMessage (), urlex);
                return null;
            }
            name = fo.getName();
        }
        if (url == null) {
            return null;
        }
        url = modifyURL (url);
        String categoryName = (String) fo.getAttribute ("category"); // NOI18N
        CATEGORY category;
        try {
            if (categoryName == null) {
                category = CATEGORY.COMMUNITY;
            } else {
                category = CATEGORY.valueOf(categoryName);
            }
        } catch (IllegalArgumentException ex) {
            // OK, not a valid name
            category = null;
        }
        String categoryIconBase = (String) fo.getAttribute ("iconBase"); // NOI18N
        Preferences providerPreferences = getPreferences().node(name);
        ProviderCategory pc;
        if (category == null) {
            if (categoryName == null || categoryIconBase == null) {
                throw new IllegalStateException("Provide category and iconBase for " + fo); // NOI18N
            }
            pc = ProviderCategory.create(categoryIconBase, categoryName);
            providerPreferences.put (ORIGINAL_CATEGORY_ICON_BASE, categoryIconBase);
        } else {
            pc = ProviderCategory.forValue(category);
        }
        AutoupdateCatalogProvider au_catalog = new AutoupdateCatalogProvider(name, displayName(fo), url, pc);
        providerPreferences.put (ORIGINAL_URL, url.toExternalForm ());
        providerPreferences.put (ORIGINAL_DISPLAY_NAME, au_catalog.getDisplayName ());
        providerPreferences.put (ORIGINAL_CATEGORY_NAME, au_catalog.getProviderCategory().getName());
        providerPreferences.put (ORIGINAL_CATEGORY_ICON_BASE, au_catalog.getProviderCategory().getIconBase());
        
        Boolean en = (Boolean) fo.getAttribute("enabled"); // NOI18N        
        if (en != null) {
            providerPreferences.putBoolean (ORIGINAL_ENABLED, en);
        }

        Boolean trusted = (Boolean) fo.getAttribute("trusted"); // NOI18N
        if(trusted != null) {
            au_catalog.setTrusted(trusted);
            providerPreferences.putBoolean(ORIGINAL_TRUSTED, trusted);
        }

        return au_catalog;
    }
    
    @Deprecated
    public static Object createXMLAutoupdateType (FileObject fo) throws IOException {
        return createUpdateProvider (fo);
    }

    // helper methods
    private static String displayName (FileObject fo) {
        String displayName = null;
        
        if (fo != null) {
            try {
                FileSystem fs = fo.getFileSystem ();
                String x = fs.getDecorator().annotateName ("", Collections.singleton (fo)); // NOI18N
                if (!x.isEmpty()) {
                    displayName = x;
                }
            } catch (FileStateInvalidException e) {
                // OK, never mind.
            }
        }
        if (displayName == null) {
            displayName = NbBundle.getMessage (AutoupdateCatalogFactory.class, "CTL_CatalogUpdatesProviderFactory_DefaultName");
        }
        
        return displayName;
    }
    
    private  static Preferences getPreferences() {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate"); // NOI18N
    }    
    
    private static URL modifyURL (URL original) {
        URL updateURL = null;
        
        if ( System.getProperty (UPDATE_VERSION_PROP) == null ) {
            System.setProperty (UPDATE_VERSION_PROP, UPDATE_VERSION);
        }
        
        if (System.getProperty (IDE_HASH_CODE) == null) {
            String id = getPreferences ().get (PROP_QUALIFIED_IDENTITY, null);
            if (id == null) {
                // can ignore it, property used only for logging purposes
                Logger.getLogger(AutoupdateCatalogFactory.class.getName()).fine("Property PROP_IDE_IDENTITY hasn't been initialized yet."); // NOI18N
                id = "";
            }
            String prefix = NbBundle.getMessage (AutoupdateCatalogFactory.class, "URL_Prefix_Hash_Code"); // NOI18N
            System.setProperty (IDE_HASH_CODE, "".equals (id) ? prefix + "0" : prefix + id); // NOI18N
            //catching strange IDs like
            //unique=-n+NB0c15fdc4f-2182-40c3-b6d8-ae09ef28922a_526df012-fe24-4849-b343-b4d77b11f6e6
            assert !id.startsWith("-n+") : "Generated identity (" + id + ") is of wrong format";
        }
        
        try {
            updateURL = new URL (encode (replace (original.toString ())));
        } catch (MalformedURLException urlex) {
            err.log (Level.INFO, urlex.getMessage(), urlex);
        }

        return updateURL;
        
    }
    
    private static String encode (String stringURL) {
	String rval = stringURL;
            int q = stringURL.indexOf ('?');
            if(q > 0) {
        StringBuilder buf = new StringBuilder(stringURL.substring(0, q + 1));
		StringTokenizer st = new StringTokenizer (stringURL.substring (q + 1), "&");
		while(st.hasMoreTokens ()) {
                    String a = st.nextToken ();
                    try {
                        int ei = a.indexOf ('=');
                        if(ei < 0) {
                            buf.append(URLEncoder.encode (a, "UTF-8"));
                        } else {
                            buf.append(URLEncoder.encode (a.substring(0, ei), "UTF-8"));
                            buf.append ('=');
                            String tna = a.substring( ei+1);
                            int tni = tna.indexOf('%');
                            if( tni < 0) {
                                buf.append (URLEncoder.encode (tna, "UTF-8"));
                            } else {
                                buf.append (URLEncoder.encode (tna.substring (0, tni), "UTF-8"));
                                buf.append ('%');
                                buf.append (URLEncoder.encode (tna.substring (tni+1), "UTF-8"));
                            }
                        }
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(AutoupdateCatalogFactory.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                    if (st.hasMoreTokens ()) {
                        buf.append('&');
                    }
                }
                rval = buf.toString ();
            }

	return rval;
    }
    
    private static String replace (String string) {

        // First of all set our system properties
        setSystemProperties ();

        if (string == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        int index, prevIndex;
        index = prevIndex = 0;
        while ((index = string.indexOf('{', index)) != -1 && index < string.length() - 1) {

            if ( string.charAt( index + 1 ) == '{' || string.charAt( index + 1 ) != '$'  ) {
                ++index;
                continue;
            }

            sb.append( string.substring (prevIndex, index) );
            int endBracketIndex = string.indexOf('}', index);
            if (endBracketIndex != -1) {
                String whatToReplace = string.substring (index + 2, endBracketIndex);
                sb.append (getReplacement (whatToReplace));
            }
            prevIndex = endBracketIndex == -1 ? index + 2 : endBracketIndex + 1;
            ++index;
        }

        if (prevIndex < string.length() - 1) {
            sb.append(string.substring(prevIndex));
        }

        return sb.toString ();
    }

    private static void setSystemProperties() {
            
        if ( System.getProperty( SYSPROP_COUNTRY, null ) == null ) {
            System.setProperty( SYSPROP_COUNTRY, java.util.Locale.getDefault().getCountry() );
        }
        if ( System.getProperty( SYSPROP_LANGUAGE, null ) == null ) {
            System.setProperty( SYSPROP_LANGUAGE, java.util.Locale.getDefault().getLanguage() );
        }
        if ( System.getProperty( SYSPROP_VARIANT, null ) == null ) {
            System.setProperty( SYSPROP_VARIANT, java.util.Locale.getDefault().getVariant() );
        }
    }
    
    private static String getReplacement (String whatToReplace) {        
        return System.getProperty (whatToReplace, "");
    }
    
}
