/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
public final class ResourceUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static

    private static HashMap<String, ResourceBundle> loadedBundles =
            new HashMap<String, ResourceBundle>();


    // strings //////////////////////////////////////////////////////////////////////
    public static String getString(final String baseName, final String key) {
        return getString(baseName, key, ResourceUtils.class.getClassLoader());
    }

    public static String getString(final String baseName, final String key, final ClassLoader loader) {
        return getBundleMessage(baseName, Locale.getDefault(), loader, key);
    }

    public static String getString(final Class clazz, final String key) {
        return getBundleMessage(getBundleResource(clazz), Locale.getDefault(), clazz.getClassLoader(), key);
    }

    public static String getString(
            final String baseName,
            final String key,
            final Object... arguments) {
        return getString(baseName, key, ResourceUtils.class.getClassLoader(), arguments);
    }

    public static String getString(
            final String baseName,
            final String key,
            final ClassLoader loader,
            final Object... arguments) {
        return format(getString(baseName, key, loader), arguments);
    }

    public static String getString(
            final Class clazz,
            final String key,
            final Object... arguments) {
        return format(getString(clazz, key), arguments);
    }

    public static Map <Locale, String> getStrings(
            final String baseName,
            final String key,
            final ClassLoader loader,
            final Object... arguments) {
        Map <Locale, String> map = getBundleMessagesMapForKey(baseName, key, loader);
        if(arguments.length == 0) {
            return map;
        } else {
            Map <Locale, String> result = new HashMap<Locale, String>();
            for(Locale locale : map.keySet()) {
                result.put(locale, format(map.get(locale), arguments));
            }
            return result;
        }
    }
    
    public static Map <Locale, String> getStrings(
            final String baseName,
            final String key,
            final Object... arguments) {
        return getStrings(baseName, key, ResourceUtils.class.getClassLoader(), arguments);
    }

    public static Map <Locale, String> getStrings(
            final Class clazz,
            final String key,
            final Object... arguments) {
        return getStrings(getBundleResource(clazz), key, clazz.getClassLoader(), arguments);
    }

    private static String format(
            final String message,
            final Object... arguments) {
        return message == null ? null : StringUtils.format(message, arguments);
    }

    // resources ////////////////////////////////////////////////////////////////////
    public static InputStream getResource(
            final String name) {
        return getResource(name, ResourceUtils.class.getClassLoader());
    }

    public static InputStream getResource(
            final String path,
            final ClassLoader loader) {
        return loader.getResourceAsStream(path);
    }

    /**
     * Returns the size of the resource file.
     * @param resource Resource name
     * @return size of the resource or 
     *      <i>-1</i> if the resource was not found or any other error occured
     */
    public static long getResourceSize(
            final String resource) {
        InputStream is = null;
        long size = 0;
        try {
            is = getResource(resource);
            if(is==null) { // resource was not found
                return -1;
            }
            byte [] buf = new byte [BUFFER_SIZE];
            while(is.available()>0) {
                size += is.read(buf);
            }
        } catch (IOException ex) {
            size = -1;
        } finally {
            try {
                if(is!=null) {
                    is.close();
                }
            } catch (IOException e){
            }
        }
        return size;
    }

    public static String getResourceFileName(
            final String resource) {
        return resource.substring(resource.lastIndexOf("/")+1);
    }
    public static String getResourceClassName(Class c) {
        return getResourceClassName(c.getName());
    }
    public static String getResourceClassName(String className) {
        return (className.replace(".", "/") + ".class");
    }
    // private //////////////////////////////////////////////////////////////////////
    private static ResourceBundle loadBundle(
            final String baseName,
            final Locale locale,
            final ClassLoader loader) {
        final String bundleId = loader.toString() + baseName +
                (locale.toString().equals(StringUtils.EMPTY_STRING) ? StringUtils.EMPTY_STRING : ("_" + locale));

        ResourceBundle bundle = (ResourceBundle) loadedBundles.get(bundleId);

        if (bundle == null && !loadedBundles.containsKey(bundleId)) {
            try {
                bundle = ResourceBundle.getBundle(baseName, locale, loader);                
            } catch (MissingResourceException e) {
                LogManager.log("Can`t find bundle " + baseName +
                        " using [" + locale + "] locale" +
                        " and [" + loader + "] classloader",
                        e);
                //throw e;
            }
            loadedBundles.put(bundleId, bundle);
        }

        return bundle;
    }

    private static String getBundleMessage(
            final String baseName,
            final Locale locale,
            final ClassLoader loader,
            final String key) {
        ResourceBundle bundle = loadBundle(baseName, locale, loader);
        String message = null;
        if (bundle != null) {
            try {                
                message = bundle.getString(key);
            } catch (MissingResourceException e) {
                if(locale.toString().length() > 0) {
                    String [] parts = locale.toString().split(StringUtils.UNDERSCORE);
                    String upLocale = StringUtils.asString(parts, 0, parts.length - 1, StringUtils.UNDERSCORE);                    
                    return getBundleMessage(baseName, StringUtils.parseLocale(upLocale), loader, key);
                } else {
                    LogManager.log("Can`t load message in bundle " + baseName +
                        " for key " + key +
                        " using [" + loader + "] classloader");
                }
            }            
        }
        return message;
    }

    private static Map <Locale, String> getBundleMessagesMapForKey(
            final String baseName,
            final String key,
            final ClassLoader loader) {
        Map <Locale, String> map = new HashMap <Locale, String> ();
        List <Locale> list = new ArrayList <Locale> ();
        list.add(new Locale(StringUtils.EMPTY_STRING));
        list.addAll(Arrays.asList(Locale.getAvailableLocales()));
        for(Locale locale : list) {
            ResourceBundle bundle = loadBundle(baseName, locale, loader);            
            if(bundle!=null && locale.equals(bundle.getLocale()) && !map.containsKey(bundle.getLocale())) {
                map.put(locale, getBundleMessage(baseName, locale, loader, key));
            }
        }
        return map;
    }
    
    private static String getBundleResource(final Class clazz) {
        return clazz.getPackage().getName() + BUNDLE_FILE_SUFFIX;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ResourceUtils() {
        // does nothing
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int BUFFER_SIZE =
            40960; // NOMAGI
    public static final String BUNDLE_FILE_SUFFIX =
            ".Bundle"; // NOI18N
}
