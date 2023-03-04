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
import java.text.MessageFormat;
import java.text.ParseException;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author martin
 */
public class SourceMapsTranslatorTest extends CslTestBase {
    
    private static final MessageFormat FORMAT_MAPPING = new MessageFormat("{0},{1}->{2},{3}");
    
    public SourceMapsTranslatorTest() {
        super(SourceMapsTranslatorTest.class.getName());
    }
    
    public void testGetLocations() throws IOException, ParseException {
        FileObject grsm = getTestFile("sourcemaps/greeter.js.map");
        SourceMap sm = SourceMap.parse(grsm.asText());
        SourceMapsTranslator smt = SourceMapsTranslator.create();
        FileObject sourceFile = getTestFile("sourcemaps/greeter.ts");
        FileObject compiledFile = getTestFile("sourcemaps/greeter.js");
        smt.registerTranslation(sourceFile, grsm.getNameExt());
        
        FileObject goldenDirectMappingFO = getTestFile("sourcemaps/greeter.dmapping");
        String goldenDirectMapping = goldenDirectMappingFO.asText();
        BufferedReader gdm = new BufferedReader(new StringReader(goldenDirectMapping));
        String line;
        while ((line = gdm.readLine()) != null) {
            Object[] mapping = FORMAT_MAPPING.parse(line);
            int cl = Integer.parseInt((String) mapping[0]);
            int cc = Integer.parseInt((String) mapping[1]);
            int sl = Integer.parseInt((String) mapping[2]);
            int sc = Integer.parseInt((String) mapping[3]);
            
            SourceMapsTranslator.Location compiledLocation = new SourceMapsTranslator.Location(compiledFile, cl, cc);
            SourceMapsTranslator.Location sourceLocation = smt.getSourceLocation(compiledLocation);
            assertEquals(line+" Bad source line:", sl, sourceLocation.getLine());
            assertEquals(line+" Bad source column:", sc, sourceLocation.getColumn());
        }
        
        FileObject goldenInverseMappingFO = getTestFile("sourcemaps/greeter.imapping");
        String goldenInverseMapping = goldenInverseMappingFO.asText();
        BufferedReader gim = new BufferedReader(new StringReader(goldenInverseMapping));
        while ((line = gim.readLine()) != null) {
            Object[] mapping = FORMAT_MAPPING.parse(line);
            int sl = Integer.parseInt((String) mapping[0]);
            int sc = Integer.parseInt((String) mapping[1]);
            int cl = Integer.parseInt((String) mapping[2]);
            int cc = Integer.parseInt((String) mapping[3]);
            
            SourceMapsTranslator.Location sourceLocation = new SourceMapsTranslator.Location(sourceFile, sl, sc);
            SourceMapsTranslator.Location compiledLocation = smt.getCompiledLocation(sourceLocation);
            assertEquals(line+" Bad compiled line:", cl, compiledLocation.getLine());
            assertEquals(line+" Bad compiled column:", cc, compiledLocation.getColumn());
        }
        
    }
}
