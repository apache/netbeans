/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.startup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;

// XXX testNbfsURLStreamHandler

/**
 * Test functionality of internal URLs.
 * @author Jesse Glick
 */
public class NbURLStreamHandlerFactoryTest extends NbTestCase {
    
    private static final String[] RESOURCES = {
        "/test/who",
        "/test/who_one",
        "/test/yes",
        "/test/something",
        "/test/something_ja",
        "/test/something_foo",
        "/test/something_foo_ja",
        "/test/something.html",
        "/test/something_ja.html",
        "/test/something_foo.html",
        "/test/something_foo_ja.html",
        "/test/something.html.template",
        "/test/something.html_ja.template",
        "/test/something.html_foo.template",
        "/test/something.html_foo_ja.template",
        "/test.dir/something.html",
        "/test.dir/something_ja.html",
        "/test.dir/something_foo.html",
        "/test.dir/something_foo_ja.html",
        "/test.dir/something",
        "/test.dir/something_ja",
        "/test.dir/something_foo",
        "/test.dir/something_foo_ja",
    };
    
    public NbURLStreamHandlerFactoryTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        Main.initializeURLFactory();
        super.setUp();
        MockServices.setServices(TestClassLoader.class, TestURLStreamHandlerFactory.class);
    }
    
    public void testNbResourceStreamHandlerAndURLStreamHandlerFactoryMerging() throws Exception {
        // Basic usage of nbres.
        checkNbres("/test/something.html");
        // Usage of nbres with artificial param.
        checkNbres("/test/something.html?unique=123456789", "/test/something.html");
        // Test usage with no extension.
        checkNbres("/test/something");
        // Check usage with multiple extensions.
        checkNbres("/test/something.html.template");
        // Check bad URL.
        URL u = new URL("nbres:/bogus");
        try {
            u.openConnection().connect();
            fail("Should not be able to connect to a bogus nbres URL!");
        } catch (IOException e) {
            // OK.
        }
        // Now test nbresloc. First, basic usage.
        checkNbresLoc("/test/something", ".html");
        // Check bare resources.
        checkNbresLoc("/test/something", "");
        // And, double-extension resources (cf. #53130).
        checkNbresLoc("/test/something.html", ".template");
        // Check also dots in the path prefix.
        checkNbresLoc("/test.dir/something", ".html");
        checkNbresLoc("/test.dir/something", "");
        // Check bad URL.
        u = new URL("nbresloc:/bogus");
        try {
            u.openConnection().connect();
            fail("Should not be able to connect to a bogus nbresloc URL!");
        } catch (IOException e) {
            // OK.
        }
        sndFastGetResourceLoc();
    }
    
    // tests that loading of localized resources tries unlocalized version first
    // to optimize for more common case (where class loader cache are more efficient too)
    private void sndFastGetResourceLoc() throws Exception {
        TestClassLoader.firstFindRequest(); // reset
        URL u = new URL("nbresloc:/test/who");
        // Should initially be getting an unlocalized version:
        NbBundle.setBranding("one");
        Locale.setDefault(Locale.US);
        u.openConnection().connect();
        assertEquals("test/who", TestClassLoader.firstFindRequest());
        u = new URL("nbresloc:/test/yes");
        u.openConnection().connect();
        assertEquals("test/yes", TestClassLoader.firstFindRequest());
    }
    
    private static void checkNbres(String path) throws Exception {
        checkNbres(path, path);
    }
    
    private static void checkNbres(String path, String file) throws Exception {
        URL u = new URL("nbres:" + path);
        assertEquals(file, suck(u));
        assertEquals(file.endsWith(".html") ? "text/html" : null, contentType(u));
        assertEquals(file.length(), contentLength(u));
    }
    
    private static void checkNbresLoc(String base, String ext) throws Exception {
        String path = base + ext;
        String type = ext.equals(".html") ? "text/html" : null;
        URL u = new URL("nbresloc:" + path);
        // Should initially be getting an unlocalized version:
        NbBundle.setBranding(null);
        Locale.setDefault(Locale.US);
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
        // Make sure branding works.
        NbBundle.setBranding("foo");
        path = base + "_foo" + ext;
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
        // Check unbranded but localized resources.
        NbBundle.setBranding(null);
        Locale.setDefault(Locale.JAPAN);
        path = base + "_ja" + ext;
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
        // Check both together.
        NbBundle.setBranding("foo");
        path = base + "_foo_ja" + ext;
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
    }
    
    private static String suck(URL u) throws Exception {
        URLConnection conn = u.openConnection();
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(is, baos);
        is.close();
        return baos.toString("UTF-8");
    }
    
    private static String contentType(URL u) throws Exception {
        URLConnection conn = u.openConnection();
        return conn.getContentType();
    }
    
    private static int contentLength(URL u) throws Exception {
        URLConnection conn = u.openConnection();
        return conn.getContentLength();
    }
    
    /**
     * Only serves requests for resources in {@link #RESOURCES}.
     * "/foo" is serviced with the URL "testurl:/foo".
     */
    public static final class TestClassLoader extends ClassLoader {
        
        static String first;
        
        static String firstFindRequest() {
            String f = first;
            first = null;
            return f;
        }
        
        protected URL findResource(String name) {
            if (first == null) {
                first = name;
            }
            
            if (!name.startsWith("/")) {
                name = "/" + name;
            }
            if (Arrays.asList(RESOURCES).contains(name)) {
                try {
                    return new URL("testurl:" + name);
                } catch (MalformedURLException e) {
                    assert false : e;
                    return null;
                }
            } else {
                return null;
            }
        }

        protected Enumeration<URL> findResources(String name) throws IOException {
            URL u = findResource(name);
            if (u != null) {
                return Enumerations.singleton(u);
            } else {
                return Enumerations.empty();
            }
        }
        
    }
    
    public static final class TestURLStreamHandlerFactory implements URLStreamHandlerFactory {
        
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (protocol.equals("testurl")) {
                return new TestURLStreamHandler();
            } else {
                return null;
            }
        }
        
    }
    
    private static final class TestURLStreamHandler extends URLStreamHandler {
        
        protected URLConnection openConnection(URL u) throws IOException {
            return new TestURLConnection(u);
        }
        
    }
    
    /**
     * Serves up a URL by just returning the path as its text.
     * Content length should match contents.
     * Content type is text/html for *.html, null for all else.
     */
    private static final class TestURLConnection extends URLConnection {
        
        private final String path;
        
        public TestURLConnection(URL u) {
            super(u);
            assertEquals("testurl", u.getProtocol());
            this.path = u.getPath();
        }

        public void connect() throws IOException {}
        
        public int getContentLength() {
            return path.length();
        }

        public String getContentType() {
            if (path.endsWith(".html")) {
                return "text/html";
            } else {
                return null;
            }
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(path.getBytes("UTF-8"));
        }

    }
    
}
