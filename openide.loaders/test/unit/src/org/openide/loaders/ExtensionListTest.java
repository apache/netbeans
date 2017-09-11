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

package org.openide.loaders;

import java.io.IOException;
import java.util.Enumeration;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExtensionListTest extends TestCase {
    
    public ExtensionListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddExtension() {
        ExtensionList instance = new ExtensionList();
        instance.addExtension("html");
        instance.addExtension("txt");
        instance.addExtension("java");
        Enumeration<String> en = instance.extensions();
        
        assertEquals("html", en.nextElement());
        assertEquals("java", en.nextElement());
        assertEquals("txt", en.nextElement());
        assertFalse(en.hasMoreElements());
        
        assertTrue(instance.isRegistered("x.java"));
        assertTrue(instance.isRegistered("x.html"));
        assertTrue(instance.isRegistered("x.txt"));
        assertFalse(instance.isRegistered("x.form"));
    }     

    public void testAddMime() throws IOException {
        ExtensionList instance = new ExtensionList();
        instance.addMimeType("text/x-java");
        instance.addMimeType("text/html");
        instance.addMimeType("text/plain");
        Enumeration<String> en = instance.mimeTypes();
        
        assertEquals("text/html", en.nextElement());
        assertEquals("text/plain", en.nextElement());
        assertEquals("text/x-java", en.nextElement());
        assertFalse(en.hasMoreElements());
        

        MockServices.setServices(MockMimeR.class);
        
        FileObject fo = FileUtil.getConfigRoot().createData("My.xml");
        assertFalse("XML files are not recognized", instance.isRegistered(fo));
        assertEquals("Instantiated", 1, MockMimeR.cnt);
    }   
    
    public static final class MockMimeR extends MIMEResolver {
        static int cnt;
        
        public MockMimeR() {
            super("text/xml");
            cnt++;
        }
        
        @Override
        public String findMIMEType(FileObject fo) {
            fail("Shall not be called at all");
            return null;
        }
        
    }
}
