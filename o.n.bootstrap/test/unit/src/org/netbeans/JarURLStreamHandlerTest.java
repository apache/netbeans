/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JarURLStreamHandlerTest extends NbTestCase {
    private File jar;

    public JarURLStreamHandlerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        jar = new File(getWorkDir(), "x.jar");
        JarOutputStream os = new JarOutputStream(
            new FileOutputStream(jar)
        );
        os.putNextEntry(new ZipEntry("fldr/plain.txt"));
        os.write("Ahoj\n".getBytes());
        os.closeEntry();
        os.close();
        JarClassLoader registerJarSource = new JarClassLoader(
            Collections.nCopies(1, jar),
            new ClassLoader[] { getClass().getClassLoader() }
        );
        assertNotNull("Registered", registerJarSource);
    }

    public void testNormalHandler() throws Exception {
        URL root = new URL("jar:" + Utilities.toURI(jar) + "!/");
        URL plain = new URL(root, "/fldr/plain.txt", ProxyURLStreamHandlerFactory.originalJarHandler());
        assertTrue("Contains the plain.txt part: " + plain, plain.toExternalForm().contains("fldr/plain.txt"));
        assertContent("Ahoj", plain);
    }

    public void testNbHandler() throws Exception {
        URL root = new URL("jar:" + Utilities.toURI(jar) + "!/");
        URL plain = new URL(root, "/fldr/plain.txt", new JarClassLoader.JarURLStreamHandler(null));
        assertTrue("Contains the plain.txt part: " + plain, plain.toExternalForm().contains("fldr/plain.txt"));
        assertContent("Ahoj", plain);
    }

    private void assertContent(String ahoj, URL plain) throws IOException {
        DataInputStream is = new DataInputStream(plain.openStream());
        byte[] arr = new byte[100];
        is.readFully(arr, 0, ahoj.length());
        assertEquals("Expected", ahoj, new String(arr, 0, ahoj.length()));
    }
    
}
