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
package org.netbeans.modules.web.common.sourcemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author martin
 */
public class SourceMapTest extends CslTestBase {
    
    public SourceMapTest() {
        super(SourceMapTest.class.getName());
    }
    
    public void testParseAll() throws IOException {
        FileObject dir = FileUtil.toFileObject(getDataDir());
        dir = dir.getFileObject("sourcemaps");
        for (FileObject fo : dir.getChildren()) {
            if (!fo.getExt().equalsIgnoreCase("map")) {
                continue;
            }
            System.out.println("parsing "+fo);
            SourceMap.parse(fo.asText());
            System.out.println("DONE.");
        }
    }
    
    public void testParseSourceMap() throws IOException {
        System.out.println("parse");
        FileObject grsm = getTestFile("sourcemaps/greeter.js.map");
        SourceMap sm = SourceMap.parse(grsm.asText());
        int lastOL = -1;
        int lastOC = -1;
        for (int l = 0; l < 10; l++) {
            for (int c = 0; c < 1000; c++) {
                Mapping mapping = sm.findMapping(l, c);
                if (mapping == null) {
                    continue ;
                }
                int ol = mapping.getOriginalLine();
                int oc = mapping.getOriginalColumn();
                if (ol == lastOL && oc == lastOC) {
                    continue;
                }
                String source = sm.getSourcePath(mapping.getSourceIndex());
                System.out.println("<"+l+","+c+">:ORIG: source: "+source+", "+
                    "<"+ol+","+oc+">, "+
                    "name: "+"?");
                
                Mapping imapping = sm.findInverseMapping(source, ol, oc);
                if (l != imapping.getOriginalLine()) {
                    System.out.println("<"+l+","+c+"> is mapped to <"+ol+","+oc+">, but inverse mapping has different line: "+imapping.getOriginalLine());
                }
                if (c != imapping.getOriginalColumn()) {
                    System.out.println("<"+l+","+c+"> is mapped to <"+ol+","+oc+">, but inverse mapping has different column: "+imapping.getOriginalColumn());
                }
                
                //assertEquals("<"+l+","+c+"> is mapped to <"+ol+","+oc+">, but inverse mapping has different line:", l, imapping.getOriginalLine());
                //assertEquals("<"+l+","+c+"> is mapped to <"+ol+","+oc+">, but inverse mapping has different column:", c, imapping.getOriginalColumn());
                
                lastOL = ol;
                lastOC = oc;
            }
        }
    }
    
    public void testGetAllMappings() throws IOException {
        System.out.println("\nALL Mappings:");
        FileObject grsm = getTestFile("sourcemaps/greeter.js.map");
        SourceMap sm = SourceMap.parse(grsm.asText());
        FileObject goldenDirectMappingFO = getTestFile("sourcemaps/greeter.dmapping");
        String goldenDirectMapping = goldenDirectMappingFO.asText();
        BufferedReader gdm = new BufferedReader(new StringReader(goldenDirectMapping));
        for (int l = 0; l < 100; l++) {
            List<Mapping> allLineMappings = sm.findAllMappings(l);
            if (allLineMappings != null) {
                for (Mapping m : allLineMappings) {
                    String map = l+","+m.getColumn()+"->"+m.getOriginalLine()+","+m.getOriginalColumn();
                    String line = gdm.readLine();
                    if (line == null) {
                        throw new IOException("Golden file finished prematurely when processing line "+l+" and mapping: "+map);
                    }
                    assertEquals(line, map);
                }
            }
        }
        assertNull(gdm.readLine());
    }
    
    public void testGetAllInverseMappings() throws IOException {
        System.out.println("\nALL Mappings:");
        FileObject grsm = getTestFile("sourcemaps/greeter.js.map");
        SourceMap sm = SourceMap.parse(grsm.asText());
        FileObject goldenInverseMappingFO = getTestFile("sourcemaps/greeter.imapping");
        String goldenInverseMapping = goldenInverseMappingFO.asText();
        BufferedReader gim = new BufferedReader(new StringReader(goldenInverseMapping));
        for (int l = 0; l < 100; l++) {
            List<Mapping> allLineMappings = sm.findAllInverseMappings("greeter.ts", l);
            if (allLineMappings != null) {
                for (Mapping m : allLineMappings) {
                    String map = l+","+m.getColumn()+"->"+m.getOriginalLine()+","+m.getOriginalColumn();
                    String line = gim.readLine();
                    if (line == null) {
                        throw new IOException("Golden file finished prematurely when processing line "+l+" and mapping: "+map);
                    }
                    assertEquals(line, map);
                }
            }
        }
        assertNull(gim.readLine());
    }
    
    public void testFindMapping() throws IOException {
        FileObject grsm = getTestFile("sourcemaps/greeter.js.map");
        SourceMap sm = SourceMap.parse(grsm.asText());
        for (int l = 0; l < 100; l++) {
            List<Mapping> allLineMappings = sm.findAllMappings(l);
            if (allLineMappings != null) {
                int mi = 0;
                Mapping m = allLineMappings.get(mi);
                Mapping nm = (++mi < allLineMappings.size()) ? allLineMappings.get(mi) : null;
                assertEquals(m, sm.findMapping(l));
                for (int c = 0; c < 1000; c++) {
                    if (nm != null && c >= nm.getColumn()) {
                        m = nm;
                        nm = (++mi < allLineMappings.size()) ? allLineMappings.get(mi) : null;
                    }
                    assertEquals("<"+l+","+c+">:", m, sm.findMapping(l, c));
                }
            }
        }
    }

    public void testFindInverseMapping() throws IOException {
        FileObject grsm = getTestFile("sourcemaps/greeter.js.map");
        SourceMap sm = SourceMap.parse(grsm.asText());
        for (int l = 0; l < 100; l++) {
            List<Mapping> allLineMappings = sm.findAllInverseMappings("greeter.ts", l);
            if (allLineMappings != null) {
                int mi = 0;
                Mapping m = allLineMappings.get(mi);
                Mapping nm = (++mi < allLineMappings.size()) ? allLineMappings.get(mi) : null;
                assertEquals(m, sm.findInverseMapping("greeter.ts", l));
                for (int c = 0; c < 1000; c++) {
                    if (nm != null && c >= nm.getColumn()) {
                        m = nm;
                        nm = (++mi < allLineMappings.size()) ? allLineMappings.get(mi) : null;
                    }
                    assertEquals("<"+l+","+c+">:", m, sm.findInverseMapping("greeter.ts", l, c));
                }
            }
        }
    }

    public void testGetSources() throws IOException {
        FileObject grsm = getTestFile("sourcemaps/greeter.js.map");
        SourceMap sm = SourceMap.parse(grsm.asText());
        List<String> sources = sm.getSources();
        assertEquals(1, sources.size());
        assertEquals("greeter.ts", sources.get(0));
    }

    /**
     * Test of getSourcePath method, of class SourceMap.
     *
    @Test
    public void testGetSourcePath() {
        System.out.println("getSourcePath");
        int sourceIndex = 0;
        SourceMap instance = null;
        String expResult = "";
        String result = instance.getSourcePath(sourceIndex);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class SourceMap.
     *
    @Test
    public void testGetName() {
        System.out.println("getName");
        int nameIndex = 0;
        SourceMap instance = null;
        String expResult = "";
        String result = instance.getName(nameIndex);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findMapping method, of class SourceMap.
     *
    @Test
    public void testFindMapping_int_int() {
        System.out.println("findMapping");
        int line = 0;
        int column = 0;
        SourceMap instance = null;
        Mapping expResult = null;
        Mapping result = instance.findMapping(line, column);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findMapping method, of class SourceMap.
     *
    @Test
    public void testFindMapping_int() {
        System.out.println("findMapping");
        int line = 0;
        SourceMap instance = null;
        Mapping expResult = null;
        Mapping result = instance.findMapping(line);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findInverseMapping method, of class SourceMap.
     *
    @Test
    public void testFindInverseMapping() {
        System.out.println("findInverseMapping");
        String sourcePath = "";
        int originalLine = 0;
        int originalColumn = 0;
        SourceMap instance = null;
        Mapping expResult = null;
        Mapping result = instance.findInverseMapping(sourcePath, originalLine, originalColumn);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    * */
    
}
