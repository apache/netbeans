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
package org.netbeans.modules.settings.convertors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FactoryMethodTest extends NbTestCase {
    private File src;
    private File dst;

    public FactoryMethodTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        src = new File(getWorkDir(), "src");
        src.mkdirs();

        dst = new File(getWorkDir(), "dst");
        dst.mkdirs();
    }
    
    
    
    public void testMethodOfGivenNameMustExists() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), StandardCharsets.UTF_8);
        
        if (out.indexOf(" Method named create was not found") == -1) {
            fail("Should warn about missing method\n" + out);
        }
    }

    public void testMethodOfGivenNameMustHaveNoArguments() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "  public static Bean create(boolean x) { return null; }",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), StandardCharsets.UTF_8);
        
        if (out.indexOf("no parameters") == -1) {
            fail("Should warn about arguments of the method\n" + out);
        }
    }
    public void testMethodOfGivenNameMustBeStatic() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "  public Bean create() { return null; }",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), StandardCharsets.UTF_8);
        
        if (out.indexOf("has to be static") == -1) {
            fail("Should warn method not being static\n" + out);
        }
    }
    
    public void testMethodOfGivenNameMustHaveTheSameReturnType() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "  public static String create() { return null; }",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), StandardCharsets.UTF_8);
        
        if (out.indexOf("must return Bean") == -1) {
            fail("Should about wrong return type\n" + out);
        }
    }
}
