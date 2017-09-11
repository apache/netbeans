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

package org.netbeans.api.editor.mimelookup;

import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;


/**
 * Testing basic functionality of MimePath
 *
 * @author Martin Roskanin
 */
public class MimePathTest extends NbTestCase {

    public MimePathTest(java.lang.String testName) {
        super(testName);
    }

    public void testParsing(){
        String path = "text/x-java/text/x-ant+xml/text/html/text/xml";
        MimePath mp = MimePath.parse(path);
        // Repetitive parse should end up the same and it should come from cache.
        assertSame(mp, MimePath.parse(path));
        String parsedPath = mp.getPath();
        assertTrue(path.equals(parsedPath));

        int size = mp.size();
        assertTrue(size == 4);
        
        String one = mp.getMimeType(0);
        String two = mp.getMimeType(1);
        String three = mp.getMimeType(2);
        String four = mp.getMimeType(3);
        
        assertTrue("text/x-java".equals(one));
        assertTrue("text/x-ant+xml".equals(two));
        assertTrue("text/html".equals(three));
        assertTrue("text/xml".equals(four));
        
        MimePath mpPrefix = mp.getPrefix(2);
        assertTrue("text/x-java/text/x-ant+xml".equals(mpPrefix.getPath()));
        
        // Force exceed size of the internal LRU cache and release the created and cached MPs
        for (int op = 0; op < 2; op++) {
            for (int i = 0; i < 10; i++) {
                path = "text/x-java/text/x-test" + i;
                mp = MimePath.parse(path);
                assertSame(mp, MimePath.parse(path));
            }
            System.gc();
        }
    }
    
    public void testMimeTypeCorrectnessCheck() {
        String [] valid = new String [] {
            "text/plain",
            "application/abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!#$&.+-^_"
        };
        String [] invalid = new String [] {
            "/",
            "text",
            "text//aaa",
            "text@aaa",
            "text/aaa/bb",
            "text/ aaa",
            "FontsColors/Make It Fun",
        };

        // Check an empty mime type.
        // This is an exception to the mime type specs, we allow empty mime types
        // and they denote MimePath.EMPTY
        {
            MimePath mimePath = MimePath.get("");
            assertNotNull("MimePath should not be null", mimePath);
            assertEquals("Wrong MimePath size", 0, mimePath.size());
            assertSame("Wrong empty MimePath", MimePath.EMPTY, mimePath);
        }
        
        // Check valid mime types
        for(String mimeType : valid) {
            MimePath mimePath = MimePath.get(mimeType);
            assertNotNull("MimePath should not be null", mimePath);
            assertEquals("Wrong MimePath size", 1, mimePath.size());
            assertEquals("Wrong mime type", mimeType, mimePath.getMimeType(0));
        }
        
        // Check invalid mime types
        for(String mimeType : invalid) {
            try {
                MimePath mimePath = MimePath.get(mimeType);
                fail("Should not create MimePath for an invalid mime type: '" + mimeType + "'");
            } catch (IllegalArgumentException iae) {
                // passed
            }
        }
    }

    // Test mime path -> path[] conversion
    
    public void testNoMapper() {
        MimePath mimePath = MimePath.parse("text/x-jsp/text/x-java/text/x-javadoc");
        List paths = mimePath.getInheritedPaths(null, null);
        checkPaths(
            Arrays.asList(new String [] {
                "text/x-jsp/text/x-java/text/x-javadoc",
                "text/x-java/text/x-javadoc",
                "text/x-javadoc",
                ""
            }),
            paths
        );
    }

    public void testNoMapperCompoundMimeType1() {
        MimePath mimePath = MimePath.parse("text/x-ant+xml/text/x-java/text/x-javadoc");
        List paths = mimePath.getInheritedPaths(null, null);
        checkPaths(
            Arrays.asList(new String [] {
                "text/x-ant+xml/text/x-java/text/x-javadoc",
                "text/xml/text/x-java/text/x-javadoc",
                "text/x-java/text/x-javadoc",
                "text/x-javadoc",
                ""
            }),
            paths
        );
    }
    
    public void testNoMapperCompoundMimeType2() {
        MimePath mimePath = MimePath.parse("text/x-ant+xml/text/x-ant+xml");
        List paths = mimePath.getInheritedPaths(null, null);
        checkPaths(
            Arrays.asList(new String [] {
                "text/x-ant+xml/text/x-ant+xml",
                "text/xml/text/x-ant+xml",
                "text/x-ant+xml",
                "text/xml",
                ""
            }),
            paths
        );
    }

    public void testNoMapperCompoundMimeType3() {
        MimePath mimePath = MimePath.parse("text/x-ant+xml/text/x-java/text/x-ant+xml");
        List paths = mimePath.getInheritedPaths(null, null);
        checkPaths(
            Arrays.asList(new String [] {
                "text/x-ant+xml/text/x-java/text/x-ant+xml",
                "text/xml/text/x-java/text/x-ant+xml",
                "text/x-java/text/x-ant+xml",
                "text/x-ant+xml",
                "text/xml",
                ""
            }),
            paths
        );
    }
    
    public void testDummyMapper() throws Exception {
        MimePath mimePath = MimePath.parse("text/x-jsp/text/x-java/text/x-javadoc");
        List paths = mimePath.getInheritedPaths("PrefixFolder", "SuffixFolder");
        checkPaths(
            Arrays.asList(new String [] {
                "PrefixFolder/text/x-jsp/text/x-java/text/x-javadoc/SuffixFolder",
                "PrefixFolder/text/x-java/text/x-javadoc/SuffixFolder",
                "PrefixFolder/text/x-javadoc/SuffixFolder",
                "PrefixFolder/SuffixFolder"
            }), 
            paths
        );
    }
    
    public void testGetGenericPartOfCompoundMimeType() {
        String generic = MimePath.getGenericPartOfCompoundMimeType("text/x-ant+xml");
        assertNotNull("Didn't detect compound mime type", generic);
        assertEquals("Wrong generic part", "text/xml", generic);
        
        generic = MimePath.getGenericPartOfCompoundMimeType("text/c++");
        assertNull("text/c++ is not a compound mime type", generic);
    }
    
    public void testInheritedType() throws Exception {
        // simple path must recognize the +xml
        MimePath path = MimePath.parse("text/x-ant+xml");
        String parentMime = path.getInheritedType();
        assertEquals("text/xml", parentMime);
        
        // the same for nested path
        path = MimePath.parse("text/x-ant+xml/text/x-java/text/x-ant+xml");
        parentMime = path.getInheritedType();
        assertEquals("text/xml", parentMime);
        
        // only the last component is considered, so no text/x-java as inherited type
        path = MimePath.parse("text/x-ant+xml/text/x-java/text/x-javadoc");
        parentMime = path.getInheritedType();
        assertEquals("", parentMime);
    }
    
    public void testIncludedPaths() throws Exception {
        String path = "text/x-java/text/x-ant+xml/text/html/text/xml";
        MimePath mp = MimePath.parse(path);
        
        List<MimePath> mpaths = mp.getIncludedPaths();
        
        assertEquals(mp, mpaths.get(0));
        assertEquals(MimePath.parse("text/x-ant+xml/text/html/text/xml"), mpaths.get(1));
        assertEquals(MimePath.parse("text/xml/text/html/text/xml"), mpaths.get(2));
        assertEquals(MimePath.parse("text/html/text/xml"), mpaths.get(3));
        assertEquals(MimePath.parse("text/xml"), mpaths.get(4));
    }
    
    private void checkPaths(List expectedPaths, List paths) {
        assertEquals("Wrong number of paths", expectedPaths.size(), paths.size());
        
        for (int i = 0; i < expectedPaths.size(); i++) {
            String expectedPath = (String) expectedPaths.get(i);
            String path = (String) paths.get(i);
            assertEquals("Invalid path", expectedPath, path);
        }
    }
}
