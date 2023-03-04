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
package org.openide.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.jar.Attributes;
import org.openide.util.base.BundleClass;
import junit.framework.TestCase;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotEquals;

// XXX testGetClassBundle
// XXX testGetLocalizedFile
// XXX testDebugLoader

/**
 * Test normal-mode functionality of {@link NbBundle}.
 * @author Jesse Glick
 */
public class NbBundleTest extends TestCase {

    public NbBundleTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        NbBundle.setBranding(null);
        NbBundle.localizedFileCache.clear();
        NbBundle.bundleCache.clear();
        System.clearProperty("java.util.PropertyResourceBundle.encoding");
    }

    public static void testLocalizingSuffixes() throws Exception {
        assertEquals("_en_US,_en,", locSuff());
        Locale.setDefault(Locale.JAPAN);
        assertEquals("_ja_JP,_ja,", locSuff());
        NbBundle.setBranding("f4j_ce");
        Locale.setDefault(Locale.US);
        assertEquals("_f4j_ce_en_US,_f4j_ce_en,_f4j_ce,_f4j_en_US,_f4j_en,_f4j,_en_US,_en,", locSuff());
        Locale.setDefault(Locale.JAPAN);
        assertEquals("_f4j_ce_ja_JP,_f4j_ce_ja,_f4j_ce,_f4j_ja_JP,_f4j_ja,_f4j,_ja_JP,_ja,", locSuff());
    }

    private static String locSuff() {
        StringBuffer b = new StringBuffer();
        boolean first = true;
        Iterator<String> it = NbBundle.getLocalizingSuffixes();
        while (it.hasNext()) {
            if (first) {
                first = false;
            } else {
                b.append(',');
            }
            b.append(it.next());
        }
        return b.toString();
    }

    public void testGetBundle() throws Exception {
        ResourceBundle rb = NbBundle.getBundle("foo.Bundle", Locale.ENGLISH, fixedLoader(false, "foo/Bundle.properties:k=v"));
        assertEquals("v", rb.getString("k"));
        try {
            rb.getString("kkk");
            fail();
        } catch (MissingResourceException mre) {
            // OK
        }
        rb = NbBundle.getBundle("foo.Bundle", Locale.US, fixedLoader(false, "foo/Bundle.properties:k=v"));
        assertEquals("v", rb.getString("k"));
        rb = NbBundle.getBundle("foo.Bundle", Locale.JAPAN, fixedLoader(false, "foo/Bundle.properties:k=v"));
        assertEquals("v", rb.getString("k"));
        rb = NbBundle.getBundle("foo.Bundle", Locale.JAPAN, fixedLoader(false, "foo/Bundle.properties:k=v", "foo/Bundle_ja.properties:k=v2"));
        assertEquals("v2", rb.getString("k"));
        assertEquals(Locale.JAPAN, rb.getLocale());
        try {
            NbBundle.getBundle("foo.Bundle", Locale.ENGLISH, fixedLoader(false));
            fail();
        } catch (MissingResourceException mre) {
            // OK
        }
        NbBundle.setBranding("nb");
        rb = NbBundle.getBundle("foo.Bundle", Locale.US, fixedLoader(false, "foo/Bundle.properties:k1=v1\nk2=v2", "foo/Bundle_nb.properties:k1=v1 NB"));
        assertEquals("v1 NB", rb.getString("k1"));
        assertEquals("v2", rb.getString("k2"));
        List<String> keys = new ArrayList<String>(Collections.list(rb.getKeys()));
        Collections.sort(keys);
        assertEquals("[k1, k2]", keys.toString());
    }

    public void testGetMessageISO() throws Exception {
        ClassLoader l = fixedLoader(false,
                "org/openide/util/Bundle.properties:"
                + "k1=v1\n"
                + "k2=v2 {0}\n"
                + "k3=v3 {0} {1} {2} {3} {4}",
                "org/openide/util/Bundle_ja.properties:"
                + "k1=v1 ja",
                "org/openide/util/Bundle_fr.properties:"
                + "k1=v1 ô Hélène"
        );
        Class<?> c = l.loadClass(Dummy.class.getName());
        assertEquals(l, c.getClassLoader());
        assertEquals("v1", NbBundle.getMessage(c, "k1"));
        Locale.setDefault(Locale.JAPAN);
        assertEquals("v1 ja", NbBundle.getMessage(c, "k1"));
        assertEquals("v2 x", NbBundle.getMessage(c, "k2", "x"));
        assertEquals("v3 a b c d e", NbBundle.getMessage(c, "k3", "a", "b", "c", "d", "e"));
        Locale.setDefault(Locale.FRENCH);
        assertEquals("v1 ô Hélène", NbBundle.getMessage(c, "k1"));
    }

    public void testGetMessageUTF8() throws Exception {
        ClassLoader l = fixedLoader(true,
                "org/openide/util/Bundle.properties:"
                + "k1=v1\n"
                + "k2=v2 {0}\n"
                + "k3=v3 {0} {1} {2} {3} {4}",
                "org/openide/util/Bundle_ja.properties:"
                + "k1=v1 ja",
                "org/openide/util/Bundle_fr.properties:"
                + "k2=v2 ô Hélène {0}"
        );
        Class<?> c = l.loadClass(Dummy.class.getName());
        assertEquals(l, c.getClassLoader());
        assertEquals("v1", NbBundle.getMessage(c, "k1"));
        Locale.setDefault(Locale.JAPAN);
        assertEquals("v1 ja", NbBundle.getMessage(c, "k1"));
        assertEquals("v2 x", NbBundle.getMessage(c, "k2", "x"));
        assertEquals("v3 a b c d e", NbBundle.getMessage(c, "k3", "a", "b", "c", "d", "e"));
        Locale.setDefault(Locale.FRENCH);
        assertEquals("v2 ô Hélène chérie", NbBundle.getMessage(c, "k2", "chérie"));
    }

    public void testSystemPropertyISO() throws Exception {
        ClassLoader l = fixedLoader(true,
                "org/openide/util/Bundle.properties:"
                + "k1=yo\n"
                + "k2=where and ouch",
                "org/openide/util/Bundle_fr.properties:"
                + "k1=fr yo\n"
                + "k2=où et aïe"
        );
        Class<?> c = l.loadClass(Dummy.class.getName());
        assertEquals(l, c.getClassLoader());
        Locale.setDefault(Locale.FRENCH);
        System.setProperty("java.util.PropertyResourceBundle.encoding", "ISO-8859-1");
        assertEquals("fr yo", NbBundle.getMessage(c, "k1"));
        assertNotEquals("où et aïe", NbBundle.getMessage(c, "k2"));
    }

    public void testSystemPropertyUTF() throws Exception {
        ClassLoader l = fixedLoader(false,
                "org/openide/util/Bundle.properties:"
                + "k1=yo\n"
                + "k2=where and ouch",
                "org/openide/util/Bundle_fr.properties:"
                + "k1=fr yo\n"
                + "k2=où et aïe"
        );
        Class<?> c = l.loadClass(Dummy.class.getName());
        assertEquals(l, c.getClassLoader());
        Locale.setDefault(Locale.FRENCH);
        System.setProperty("java.util.PropertyResourceBundle.encoding", "UTF-8");
        try {
            assertEquals("fr yo", NbBundle.getMessage(c, "k1"));
            fail();
        } catch (MissingResourceException mre) {
            // OK MalformedInputException
        }

    }

    static class Dummy {}
    /**
     * Creates a loader which can load just fixed resources you supply. Each
     * entry should be of the form
     * <pre>
     * path/to/res1:some contents
     * for res1
     * </pre> Also can define a class named Dummy.class.getName().
     *
     * @param useUTF8 If false use ISO-8859-1 encoding, otherwise UTF-8
     */
    private static ClassLoader fixedLoader(boolean useUTF8, String... entries) throws Exception {
        final Map<String, byte[]> data = new HashMap<String, byte[]>();
        for (String entry : entries) {
            int colon = entry.indexOf(':');
            data.put(entry.substring(0, colon), entry.substring(colon + 1).getBytes(useUTF8 ? "UTF-8" : "ISO-8859-1"));
        }
        return new ClassLoader() {
            @Override
            public URL getResource(final String res) {
                if (data.containsKey(res)) {
                    //System.err.println("hit for " + res);
                    try {
                        return new URL("dummy", null, 0, res, new URLStreamHandler() {
                            protected URLConnection openConnection(URL u) throws IOException {
                                return new URLConnection(u) {
                                    public void connect() throws IOException {}
                                    @Override
                                    public InputStream getInputStream() throws IOException {
                                        return new ByteArrayInputStream(data.get(res));
                                    }
                                };
                            }
                        });
                    } catch (MalformedURLException x) {
                        throw new AssertionError(x);
                    }
                } else {
                    //System.err.println("miss for " + res);
                    return null;
                }
            }

            @Override
            public Class loadClass(String n) throws ClassNotFoundException {
                if (n.equals(Dummy.class.getName())) {
                    InputStream is = NbBundleTest.class.getClassLoader().getResourceAsStream(n.replace('.', '/') + ".class");
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

    public static void testGetLocalizedValue() throws Exception {
        Map<String,String> m = new HashMap<String,String>();
        m.put("k1", "v1");
        m.put("k1_ja", "v1_ja");
        m.put("k1_ja_JP", "v1_ja_JP");
        m.put("k2", "v2");
        m.put("k3_ja", "v3_ja");
        assertEquals("v1", NbBundle.getLocalizedValue(m, "k1", Locale.ENGLISH));
        assertEquals("v1_ja", NbBundle.getLocalizedValue(m, "k1", Locale.JAPANESE));
        assertEquals("v1_ja_JP", NbBundle.getLocalizedValue(m, "k1", Locale.JAPAN));
        assertEquals("v2", NbBundle.getLocalizedValue(m, "k2", Locale.ENGLISH));
        assertEquals("v2", NbBundle.getLocalizedValue(m, "k2", Locale.JAPANESE));
        assertEquals("v2", NbBundle.getLocalizedValue(m, "k2", Locale.JAPAN));
        assertEquals(null, NbBundle.getLocalizedValue(m, "k3", Locale.ENGLISH));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(m, "k3", Locale.JAPANESE));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(m, "k3", Locale.JAPAN));
        Attributes attr = new Attributes();
        attr.putValue("k1", "v1");
        attr.putValue("k1_ja", "v1_ja");
        attr.putValue("k1_ja_JP", "v1_ja_JP");
        attr.putValue("k2", "v2");
        attr.putValue("k3_ja", "v3_ja");
        assertEquals("v1", NbBundle.getLocalizedValue(attr, new Attributes.Name("k1"), Locale.ENGLISH));
        assertEquals("v1_ja", NbBundle.getLocalizedValue(attr, new Attributes.Name("k1"), Locale.JAPANESE));
        assertEquals("v1_ja_JP", NbBundle.getLocalizedValue(attr, new Attributes.Name("k1"), Locale.JAPAN));
        assertEquals("v2", NbBundle.getLocalizedValue(attr, new Attributes.Name("k2"), Locale.ENGLISH));
        assertEquals("v2", NbBundle.getLocalizedValue(attr, new Attributes.Name("k2"), Locale.JAPANESE));
        assertEquals("v2", NbBundle.getLocalizedValue(attr, new Attributes.Name("k2"), Locale.JAPAN));
        assertEquals(null, NbBundle.getLocalizedValue(attr, new Attributes.Name("k3"), Locale.ENGLISH));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(attr, new Attributes.Name("k3"), Locale.JAPANESE));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(attr, new Attributes.Name("k3"), Locale.JAPAN));
        attr = new Attributes();
        attr.putValue("Think", "Smart");
        assertEquals("think", "THINK".toLowerCase());
        assertEquals("Smart", NbBundle.getLocalizedValue(attr, new Attributes.Name("think")));
        assertEquals("Smart", NbBundle.getLocalizedValue(attr, new Attributes.Name("THINK")));
        Locale.setDefault(new Locale("tr", "TR"));
        assertEquals("th\u0131nk", "THINK".toLowerCase());
        assertEquals("Smart", NbBundle.getLocalizedValue(attr, new Attributes.Name("think")));
        assertEquals("Smart", NbBundle.getLocalizedValue(attr, new Attributes.Name("THINK")));
    }

    /** @see "#57815" */
    public static void testSystemClassLoaderLoadedClasses() throws Exception {
        assertNotNull("OK to ask for message from class on app CP", NbBundle.getMessage(BundleClass.class, "OpenIDE-Module-Name"));
        try {
            NbBundle.getMessage(Object.class, "whatever");
            fail();
        } catch (MissingResourceException x) {
            // OK
        }
    }

    public void testDebugInputStream() throws Exception {
        assertEquals("basic annotation works", "{key=val (17:1)}", debugIS("key=val\n", true));
        assertEquals("annotation disabled unless loc", "{key=val}", debugIS("key=val\n", false));
        assertEquals("comments ignored", "{key=val (17:2)}", debugIS("# some comment\nkey=val\n", true));
        assertEquals("simple multiline values handled", "{k1=vee one (17:1)}", debugIS("k1=vee \\\none\n", true));
        assertEquals("whitespace starting cont lines ignored", "{k1=vee one (17:1)}", debugIS("k1=vee \\\n    one\n", true));
        assertEquals("whitespace around key ignored", "{k1=v1 (17:1)}", debugIS(" k1 =v1\n", true));
        assertEquals("whitespace before value ignored", "{k1=v1 (17:1)}", debugIS("k1= v1\n", true));
        assertEquals("whitespace after value significant", "{k1=v1  (17:1)}", debugIS("k1=v1 \n", true));
        assertEquals("trailing newline not required", "{k1=v1 (17:1)}", debugIS("k1=v1", true));
        assertEquals("complex case", "{k1=vee one (17:1), k2=vee two  (17:3)}", debugIS(" k1 = vee \\\n    one\n k2  =  vee \\\n    two ", true));
        assertEquals("NOI18N works", "{k1=v1}", debugIS("#NOI18N\nk1=v1\n", true));
        assertEquals("I18N works", "{k1=v1 (17:2)}", debugIS("#I18N\nk1=v1\n", false));
        // XXX \ in key
        // XXX #PART{,NO}I18N
        // XXX Unicode escapes
        // XXX ':' rather than '='
        // XXX \r\n
        // XXX key with no value, or empty value
        // XXX value beginning with \
    }
    private static String debugIS(String s, boolean loc) throws IOException {
        InputStream dis = new NbBundle.DebugLoader.DebugInputStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.ISO_8859_1)), 17, loc);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = dis.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        Properties p = new Properties();
        p.load(new ByteArrayInputStream(baos.toByteArray()));
        return new TreeMap<String,String>(NbCollections.checkedMapByFilter(p, String.class, String.class, true)).toString();
    }

}
