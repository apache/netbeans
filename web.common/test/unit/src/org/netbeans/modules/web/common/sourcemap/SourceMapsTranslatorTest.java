/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
