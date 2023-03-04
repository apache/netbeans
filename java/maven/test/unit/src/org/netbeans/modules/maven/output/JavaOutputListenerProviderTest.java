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
package org.netbeans.modules.maven.output;

import java.io.File;
import junit.framework.*;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint
 */
public class JavaOutputListenerProviderTest extends TestCase {
    private JavaOutputListenerProvider provider;
    public JavaOutputListenerProviderTest(java.lang.String testName) {
        super(testName);
    }
   
    @Override
    protected void setUp() throws java.lang.Exception {
        provider = new JavaOutputListenerProvider(null);
    }

    @SuppressWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    public void testRecognizeLine() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojoexecute#compiler:testCompile", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Compiling 1 source file to /home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/target/test-classes", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("/home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/src/test/java/org/codehaus/mevenide/netbeans/output/JavaOutputListenerProviderTest.java:[31,1] illegal start of type", visitor);
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();
        // happens with external command line parsing sometimes..
        provider.processLine("[WARNING] /home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/src/test/java/org/codehaus/mevenide/netbeans/output/JavaOutputListenerProviderTest.java:[31,1] deprecated", visitor);
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("K:\\jsr144-private\\common-1_4\\workspace\\ri\\oss_common_j2eesdk-1_4-src-ri\\oss_cbe_party_ri\\..\\src\\main\\java\\ossj\\common\\cbe\\party\\PartyValueIteratorImpl.java:[22,7] ossj.common.cbe.party.PartyValueIteratorImpl is not abstract and does not override abstract method getNextPartys(int) in javax.oss.cbe.party.PartyValueIterator", visitor);
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("C:\\lfo\\pers\\projects\\mojos\\maven-hello-plugin\\src\\main\\java\\org\\laurentforet\\mojos\\hello\\GreetingMojo.java:[14,8] cannot find symbol", visitor);
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();
        
        //MEVENIDE-473
        provider.processLine("Compilation failure\r\n\r\n/home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/src/test/java/org/codehaus/mevenide/netbeans/output/JavaOutputListenerProviderTest.java:[14,8] cannot find symbol", visitor);
        assertNotNull(visitor.getOutputListener());
        CompileAnnotation ann = (CompileAnnotation) visitor.getOutputListener();
        assertEquals(ann.clazzfile.getAbsolutePath(), 
                FileUtil.normalizeFile(new File("/home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/src/test/java/org/codehaus/mevenide/netbeans/output/JavaOutputListenerProviderTest.java")).getAbsolutePath());
        visitor.resetVisitor();
        provider.sequenceFail("mojoexecute#compiler:testCompile", visitor);
        
        if (Utilities.isWindows()) { // #197381
            provider.processLine("Compiling 1 source file to c:\\DOCUME~1\\My Self\\prj\\target\\classes", visitor);
            provider.processLine("C:\\Documents and Settings\\My Self\\prj\\src\\main\\java\\test\\prj\\App.java:[11,45] not a statement", visitor); // after MCOMPILER-140
            ann = (CompileAnnotation) visitor.getOutputListener();
            assertNotNull(ann);
            assertEquals("C:\\Documents and Settings\\My Self\\prj\\src\\main\\java\\test\\prj\\App.java", ann.clazzfile.getAbsolutePath());
            visitor.resetVisitor();
            provider.processLine("Compiling 1 source file to X:\\Documents and Settings\\My Self\\prj\\target\\classes", visitor);
            provider.processLine("\\Documents and Settings\\My Self\\prj\\src\\main\\java\\test\\prj\\App.java:[11,45] not a statement", visitor);
            ann = (CompileAnnotation) visitor.getOutputListener();
            assertNotNull(ann);
            assertEquals("X:\\Documents and Settings\\My Self\\prj\\src\\main\\java\\test\\prj\\App.java", ann.clazzfile.getAbsolutePath());
            provider.processLine("Compiling 1 source file to \\\\server\\share\\prj\\target\\classes", visitor);
            provider.processLine("\\\\server\\share\\prj\\src\\main\\java\\test\\prj\\App.java:[11,45] not a statement", visitor);
            ann = (CompileAnnotation) visitor.getOutputListener();
            assertNotNull(ann);
            assertEquals("\\\\server\\share\\prj\\src\\main\\java\\test\\prj\\App.java", ann.clazzfile.getAbsolutePath());
            visitor.resetVisitor();
        }
    }
}
