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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import static java.util.Collections.emptyMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import static java.util.Locale.US;
import static java.util.Locale.JAPAN;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.JAPANESE;
import static java.util.Locale.FRENCH;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.jar.Attributes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.base.BundleClass;
import static org.openide.util.NbBundle.*;
import static java.util.Collections.list;
import static java.util.Collections.sort;
import static org.junit.Assert.assertFalse;

// XXX testGetClassBundle
// XXX testGetLocalizedFile
// XXX testDebugLoader
/**
 * Test normal-mode functionality of {@link NbBundle}.
 *
 * @author Jesse Glick, Lukasz Bownik
 */
public class NbBundleTest {

    //--------------------------------------------------------------------------
    private static class Dummy {
    }

    //--------------------------------------------------------------------------
    private ResourceBundle getISOBundle(final Locale locale, final String... entries)
            throws Exception {

        return getBundle("foo.Bundle", locale, fixedLoader("ISO-8859-1", entries));
    }

    //--------------------------------------------------------------------------
    private static void expectNullPointer(final Runnable code) {

        try {
            code.run();
            fail("Null pointer not thrown.");
        } catch (final NullPointerException e) {
            // good
        }
    }

    //--------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {

        Locale.setDefault(US);
        NbBundle.setBranding(null);
        NbBundle.localizedFileCache.clear();
        NbBundle.bundleCache.clear();
        System.clearProperty("java.util.PropertyResourceBundle.encoding");
    }

    //--------------------------------------------------------------------------
    @Test
    public void getLocalizingSuffixes_returnsProperSuffixes_whenNoBrandingIsSet()
            throws Exception {

        Iterator<String> i;

        Locale.setDefault(US);
        i = getLocalizingSuffixes();
        
        assertEquals("_en_US", i.next());
        assertEquals("_en", i.next());
        assertEquals("", i.next());
        assertFalse(i.hasNext());

        Locale.setDefault(JAPAN);
        i = getLocalizingSuffixes();
        
        assertEquals("_ja_JP", i.next());
        assertEquals("_ja", i.next());
        assertEquals("", i.next());
        assertFalse(i.hasNext());
    }

    //--------------------------------------------------------------------------
    @Test
    public void getLocalizingSuffixes_returnsProperSuffixes_whenBrandingIsSet()
            throws Exception {

        Iterator<String> i;
        
        setBranding("f4j_ce");
        Locale.setDefault(US);
        i = getLocalizingSuffixes();
        
        assertEquals("_f4j_ce_en_US", i.next());
        assertEquals("_f4j_ce_en", i.next());
        assertEquals("_f4j_ce", i.next());
        assertEquals("_f4j_en_US", i.next());
        assertEquals("_f4j_en", i.next());
        assertEquals("_f4j", i.next());
        assertEquals("_en_US", i.next());
        assertEquals("_en", i.next());
        assertEquals("", i.next());
        assertFalse(i.hasNext());

        Locale.setDefault(JAPAN);
        i = getLocalizingSuffixes();
        
        assertEquals("_f4j_ce_ja_JP", i.next());
        assertEquals("_f4j_ce_ja", i.next());
        assertEquals("_f4j_ce", i.next());
        assertEquals("_f4j_ja_JP", i.next());
        assertEquals("_f4j_ja", i.next());
        assertEquals("_f4j", i.next());
        assertEquals("_ja_JP", i.next());
        assertEquals("_ja", i.next());
        assertEquals("", i.next());
        assertFalse(i.hasNext());
    }

    //--------------------------------------------------------------------------
    @Test
    public void getBranding_returnsValue_setBy_setBranding() {

        setBranding(null);

        assertNull(getBranding());

        setBranding("abc");

        assertEquals("abc", getBranding());
    }

    //--------------------------------------------------------------------------
    @Test
    public void setBranding_throwsIllegalArgument_whenGivenIllegalBrandingFormat() {

        try {
            setBranding("???");
            fail();
        } catch (IllegalArgumentException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void getBundndle_throwsNullPointer_whenGivenNullArgument() {

        expectNullPointer(() -> getBundle((String) null));
        expectNullPointer(() -> getBundle((Class) null));
        expectNullPointer(() -> getBundle((String) null, US));
        expectNullPointer(() -> getBundle("US", null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getBundle_returnsDefaultBundle_ifNoLanguageSpecificBundleIsPresent()
            throws Exception {

        ResourceBundle rb = getISOBundle(ENGLISH, "foo/Bundle.properties:k=v");

        assertNotNull(rb);
        assertEquals("v", rb.getString("k"));

        try {
            rb.getString("k2");
            fail();
        } catch (MissingResourceException e) {
            assertEquals("k2", e.getKey());
        }

        rb = getISOBundle(US, "foo/Bundle.properties:k=v");

        assertNotNull(rb);
        assertEquals("v", rb.getString("k"));

        rb = getISOBundle(JAPAN, "foo/Bundle.properties:k=v");

        assertNotNull(rb);
        assertEquals("v", rb.getString("k"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getBundle_returnsLanguageSpecificBundle_ifItIsPresent()
            throws Exception {

        ResourceBundle rb = getISOBundle(JAPAN, "foo/Bundle.properties:k=v",
                "foo/Bundle_ja.properties:k=v2");

        assertNotNull(rb);
        assertEquals("v2", rb.getString("k"));
        assertEquals(JAPAN, rb.getLocale());
    }

    //--------------------------------------------------------------------------
    @Test
    public void getBundle_throwsMissingResource_ifNoBundleIsAvailable()
            throws Exception {

        try {
            getISOBundle(ENGLISH);
            fail();
        } catch (MissingResourceException e) {
            // good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void getBundle_returnsMergedBundles_whenBrandingIsPorvided()
            throws Exception {

        setBranding("nb");

        ResourceBundle rb = getISOBundle(US,
                "foo/Bundle.properties:k1=v1\nk2=v2", //default bundle
                "foo/Bundle_nb.properties:k1=v1 NB"); // branded bundle

        assertEquals("v1 NB", rb.getString("k1"));
        assertEquals("v2", rb.getString("k2"));

        List<String> keys = list(rb.getKeys());
        sort(keys);

        assertEquals("[k1, k2]", keys.toString());
    }

    //--------------------------------------------------------------------------
    @Test
    public void getMessage_throwsNullPointer_whenGivenNullArgument() {

        expectNullPointer(() -> getMessage((Class) null, "k1"));
        expectNullPointer(() -> getMessage(Dummy.class, null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getMessage_returnsCorrectMessages_forVariousLocales_ISO()
            throws Exception {

        ClassLoader loader = fixedLoader("ISO-8859-1",
                "org/openide/util/Bundle.properties:"
                + "k1=v1\n"
                + "k2=v2 {0}\n"
                + "k3=v3 {0} {1} {2} {3} {4}",
                "org/openide/util/Bundle_ja.properties:"
                + "k1=v1 ja",
                "org/openide/util/Bundle_fr.properties:"
                + "k1=v1 ô Hélène"
        );
        Class<?> cls = loader.loadClass(Dummy.class.getName());

        assertEquals(loader, cls.getClassLoader());
        assertEquals("v1", getMessage(cls, "k1"));

        Locale.setDefault(JAPAN);

        assertEquals("v1 ja", getMessage(cls, "k1"));
        assertEquals("v2 x", getMessage(cls, "k2", "x"));
        assertEquals("v3 a b c d e", getMessage(cls, "k3", "a", "b", "c", "d", "e"));

        Locale.setDefault(FRENCH);

        assertEquals("v1 ô Hélène", getMessage(cls, "k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getMessage_returnsCorrectMessages_forVariousLocales_UTF()
            throws Exception {

        ClassLoader loader = fixedLoader("UTF-8",
                "org/openide/util/Bundle.properties:"
                + "k1=v1\n"
                + "k2=v2 {0}\n"
                + "k3=v3 {0} {1} {2} {3} {4}",
                "org/openide/util/Bundle_ja.properties:"
                + "k1=v1 ja",
                "org/openide/util/Bundle_fr.properties:"
                + "k2=v2 ô Hélène {0}"
        );
        Class<?> cls = loader.loadClass(Dummy.class.getName());

        assertEquals(loader, cls.getClassLoader());
        assertEquals("v1", getMessage(cls, "k1"));

        Locale.setDefault(JAPAN);

        assertEquals("v1 ja", getMessage(cls, "k1"));
        assertEquals("v2 x", getMessage(cls, "k2", "x"));
        assertEquals("v3 a b c d e", getMessage(cls, "k3", "a", "b", "c", "d", "e"));

        Locale.setDefault(FRENCH);

        assertEquals("v2 ô Hélène chérie", getMessage(cls, "k2", "chérie"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getMessage_usesCharset_setBySystemProperty_ISO() throws Exception {

        ClassLoader loader = fixedLoader("UTF-8",
                "org/openide/util/Bundle.properties:"
                + "k1=yo\n"
                + "k2=where and ouch",
                "org/openide/util/Bundle_fr.properties:"
                + "k1=fr yo\n"
                + "k2=où et aïe"
        );
        Class<?> cls = loader.loadClass(Dummy.class.getName());

        assertEquals(loader, cls.getClassLoader());

        Locale.setDefault(FRENCH);
        System.setProperty("java.util.PropertyResourceBundle.encoding", "ISO-8859-1");

        assertEquals("fr yo", getMessage(cls, "k1"));
        assertNotEquals("où et aïe", getMessage(cls, "k2"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getMessage_usesCharset_setBySystemProperty_UTF() throws Exception {

        ClassLoader loader = fixedLoader("ISO-8859-1",
                "org/openide/util/Bundle.properties:"
                + "k1=yo\n"
                + "k2=where and ouch",
                "org/openide/util/Bundle_fr.properties:"
                + "k1=fr yo\n"
                + "k2=où et aïe"
        );
        Class<?> cls = loader.loadClass(Dummy.class.getName());

        assertEquals(loader, cls.getClassLoader());

        Locale.setDefault(FRENCH);
        System.setProperty("java.util.PropertyResourceBundle.encoding", "UTF-8");
        
        try {
            assertEquals("fr yo", getMessage(cls, "k1"));
            fail();
        } catch (MissingResourceException e) {
            // OK MalformedInputException
        }
    }
    //--------------------------------------------------------------------------
    @Test
    public void getMessage_returnaValueFromResources_loadedBySystemLoder() 
            throws Exception {
        
        // It's OK to ask for message from class on app class path
        assertNotNull(getMessage(BundleClass.class, "OpenIDE-Module-Name"));
    }
    
    //--------------------------------------------------------------------------
    @Test
    public void getMessage_throwsMissingResource_whenGivenInexistentKey_usingSystemLoader() 
            throws Exception {
        
        // Object.class is loaded by system loader
        try { 
            getMessage(Object.class, "whatever"); // 
            fail();
        } catch (MissingResourceException e) {
            // OK
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void getLocalizedValue_throwsNullPointer_whenGivenNullArgument()
            throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");

        expectNullPointer(() -> getLocalizedValue(null, "k1"));
        expectNullPointer(() -> getLocalizedValue(null, "k1", ENGLISH));
        expectNullPointer(() -> getLocalizedValue(map, "k1", null));

        Attributes attr = new Attributes();
        attr.putValue("k1", "v1");
        Attributes.Name k1 = new Attributes.Name("k1");

        expectNullPointer(() -> getLocalizedValue(null, k1));
        expectNullPointer(() -> getLocalizedValue(null, k1, ENGLISH));
        expectNullPointer(() -> getLocalizedValue(attr, null));
        expectNullPointer(() -> getLocalizedValue(attr, null, ENGLISH));
        expectNullPointer(() -> getLocalizedValue(attr, k1, null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getLocalizedValue_returnsNull_whenGivenEmptyMap()
            throws Exception {

        assertNull(getLocalizedValue(emptyMap(), "k1", ENGLISH));
        assertNull(getLocalizedValue(emptyMap(), "k1"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void getLocalizedValue_returnlocalizedValueOrNull_forProperInvocation()
            throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k1_ja", "v1_ja");
        map.put("k1_ja_JP", "v1_ja_JP");

        map.put("k2", "v2");
        map.put("k3_ja", "v3_ja");

        assertNull(getLocalizedValue(map, null));
        assertNull(getLocalizedValue(map, null, ENGLISH));
        
        assertEquals("v1", getLocalizedValue(map, "k1", ENGLISH));
        assertEquals("v1", getLocalizedValue(map, "k1")); // default locale is set to US
        assertEquals("v1_ja", getLocalizedValue(map, "k1", JAPANESE));
        assertEquals("v1_ja_JP", getLocalizedValue(map, "k1", JAPAN));
        

        assertEquals("v2", getLocalizedValue(map, "k2", ENGLISH));
        assertEquals("v2", getLocalizedValue(map, "k2")); // default locale is set to US
        assertEquals("v2", getLocalizedValue(map, "k2", JAPANESE));
        assertEquals("v2", getLocalizedValue(map, "k2", JAPAN));

        assertEquals(null, getLocalizedValue(map, "k3", ENGLISH));
        assertEquals(null, getLocalizedValue(map, "k3")); // default locale is set to US

        assertEquals("v3_ja", getLocalizedValue(map, "k3", JAPANESE));
        assertEquals("v3_ja", getLocalizedValue(map, "k3", JAPAN));

        Attributes attr = new Attributes();
        attr.putValue("k1", "v1");
        attr.putValue("k1_ja", "v1_ja");
        attr.putValue("k1_ja_JP", "v1_ja_JP");
        
        attr.putValue("k2", "v2");
        
        attr.putValue("k3_ja", "v3_ja");

        assertEquals("v1", getLocalizedValue(attr, new Attributes.Name("k1"), ENGLISH));
        assertEquals("v1_ja", getLocalizedValue(attr, new Attributes.Name("k1"), JAPANESE));
        assertEquals("v1_ja_JP", getLocalizedValue(attr, new Attributes.Name("k1"), JAPAN));
        
        assertEquals("v2", getLocalizedValue(attr, new Attributes.Name("k2"), ENGLISH));
        assertEquals("v2", getLocalizedValue(attr, new Attributes.Name("k2"), JAPANESE));
        assertEquals("v2", getLocalizedValue(attr, new Attributes.Name("k2"), JAPAN));
        
        assertEquals(null, getLocalizedValue(attr, new Attributes.Name("k3"), ENGLISH));
        assertEquals("v3_ja", getLocalizedValue(attr, new Attributes.Name("k3"), JAPANESE));
        assertEquals("v3_ja", getLocalizedValue(attr, new Attributes.Name("k3"), JAPAN));

        attr = new Attributes();
        attr.putValue("Think", "Smart");

        assertEquals("think", "THINK".toLowerCase());
        assertEquals("Smart", getLocalizedValue(attr, new Attributes.Name("think")));
        assertEquals("Smart", getLocalizedValue(attr, new Attributes.Name("THINK")));
        
        Locale.setDefault(new Locale("tr", "TR"));
        
        assertEquals("th\u0131nk", "THINK".toLowerCase());
        assertEquals("Smart", getLocalizedValue(attr, new Attributes.Name("think")));
        assertEquals("Smart", getLocalizedValue(attr, new Attributes.Name("THINK")));
    }
    
    //--------------------------------------------------------------------------
    @Test
    public void getLocalizedValue_returnsNull_whenGivenNullKey()
            throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");

        assertNull(getLocalizedValue(map, (String)null));
        assertNull(getLocalizedValue(map, (String)null, ENGLISH));
    }
    /***************************************************************************
     * Creates a loader which can load just fixed resources you supply. Each
     * entry should be of the form
     * <pre>
     * path/to/res1:some contents
     * for res1
     * </pre> Also can define a class named Dummy.class.getName().
     *
     * @param charset use "ISO-8859-1" or "UTF-8" encoding
     **************************************************************************/
    static ClassLoader fixedLoader(final String charset, final String... entries)
            throws Exception {

        final Map<String, byte[]> data = new HashMap<>();
        for (final String entry : entries) {
            final int colon = entry.indexOf(':');
            data.put(entry.substring(0, colon), entry.substring(colon + 1).getBytes(charset));
        }
        return new ClassLoader() {
            @Override
            public URL getResource(final String res) {
                
                if (data.containsKey(res)) {
                    try {
                        return new URL("dummy", null, 0, res, new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL url)
                                    throws IOException {
                                return new URLConnection(url) {
                                    @Override
                                    public void connect() throws IOException {
                                    }

                                    @Override
                                    public InputStream getInputStream()
                                            throws IOException {
                                        return new ByteArrayInputStream(data.get(res));
                                    }
                                };
                            }
                        });
                    } catch (MalformedURLException x) {
                        throw new AssertionError(x);
                    }
                } else {
                    return null;
                }
            }

            @Override
            public Class loadClass(String n) throws ClassNotFoundException {
                
                if (n.equals(Dummy.class.getName())) {
                    InputStream is = NbBundleTest.class.getClassLoader().
                            getResourceAsStream(n.replace('.', '/') + ".class");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int read;
                    try {
                        while ((read = is.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                    } catch (IOException x) {
                        throw new AssertionError(x);
                    }
                    return defineClass(n, baos.toByteArray(), 0, baos.size());
                } else {
                    return super.loadClass(n);
                }
            }
        };
    }
}
