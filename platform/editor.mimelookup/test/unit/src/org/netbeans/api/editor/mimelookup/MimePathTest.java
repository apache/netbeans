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
