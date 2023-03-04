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
package org.netbeans.modules.textmate.lexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.textmate.lexer.api.GrammarInjectionRegistration;
import org.netbeans.modules.textmate.lexer.api.GrammarRegistration;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CreateRegistrationProcessorTest extends NbTestCase {
    
    public CreateRegistrationProcessorTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testGrammarOK() throws Exception {
        System.setProperty("executed", "false");
        AnnotationProcessorTestUtils.makeSource(getWorkDir(),
                                                "Test",
                                                "import " + GrammarRegistration.class.getCanonicalName() + ";\n" +
                                                "@GrammarRegistration(mimeType=\"text/test\",grammar=\"grammar.json\")\n" +
                                                "public class Test {}\n");
        try (Writer w = new FileWriter(new File(getWorkDir(), "grammar.json"))) {
            w.write("{ \"scopeName\": \"test\", " +
                    " \"patterns\": [\n" +
                    "]}\n");
        }

        File outDir = new File(getWorkDir(), "out");

        outDir.mkdirs();

        assertTrue("Compiles OK",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, outDir, null, System.err)
            );
        
        try (Reader r = new InputStreamReader(new FileInputStream(new File(new File(outDir, "META-INF"), "generated-layer.xml")), StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            int read;
            
            while ((read = r.read()) != (-1)) {
                content.append((char) read);
            }

            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                         "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.2//EN\"\n" +
                         "                            \"http://www.netbeans.org/dtds/filesystem-1_2.dtd\">\n" +
                         "<filesystem>\n" +
                         "    <folder name=\"Editors\">\n" +
                         "        <folder name=\"text\">\n" +
                         "            <folder name=\"test\">\n" +
                         "                <file name=\"grammar.json\" url=\"nbresloc:/grammar.json\">\n" +
                         "                    <!--Test-->\n" +
                         "                    <attr name=\"textmate-grammar\" stringvalue=\"test\"/>\n" +
                         "                </file>\n" +
                         "            </folder>\n" +
                         "        </folder>\n" +
                         "    </folder>\n" +
                         "</filesystem>\n",
                         content.toString());
        }
    }

    public void testInjectionGrammarOK() throws Exception {
        System.setProperty("executed", "false");
        AnnotationProcessorTestUtils.makeSource(getWorkDir(),
                                                "Test",
                                                "import " + GrammarInjectionRegistration.class.getCanonicalName() + ";\n" +
                                                "@GrammarInjectionRegistration(grammar=\"injection-grammar.json\", injectTo={\"test\"})\n" +
                                                "public class Test {}\n");
        try (Writer w = new FileWriter(new File(getWorkDir(), "injection-grammar.json"))) {
            w.write("{ \"scopeName\": \"test.injection\", " +
                    " \"patterns\": [\n" +
                    "]}\n");
        }

        File outDir = new File(getWorkDir(), "out");

        outDir.mkdirs();

        assertTrue("Compiles OK",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, outDir, null, System.err)
            );

        try (Reader r = new InputStreamReader(new FileInputStream(new File(new File(outDir, "META-INF"), "generated-layer.xml")), StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            int read;

            while ((read = r.read()) != (-1)) {
                content.append((char) read);
            }

            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                         "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.2//EN\"\n" +
                         "                            \"http://www.netbeans.org/dtds/filesystem-1_2.dtd\">\n" +
                         "<filesystem>\n" +
                         "    <folder name=\"Editors\">\n" +
                         "        <file name=\"injection-grammar.json\" url=\"nbresloc:/injection-grammar.json\">\n" +
                         "            <!--Test-->\n" +
                         "            <attr name=\"textmate-grammar\" stringvalue=\"test.injection\"/>\n" +
                         "            <attr name=\"inject-to\" stringvalue=\"test\"/>\n" +
                         "        </file>\n" +
                         "    </folder>\n" +
                         "</filesystem>\n",
                         content.toString());
        }
    }
}
