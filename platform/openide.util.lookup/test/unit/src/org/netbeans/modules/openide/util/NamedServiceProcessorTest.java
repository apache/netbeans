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
package org.netbeans.modules.openide.util;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.NamedServiceDefinition;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NamedServiceProcessorTest extends NbTestCase {
    
    public NamedServiceProcessorTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testNamedDefinition() throws Exception {
        System.setProperty("executed", "false");
        String content = "import " + RunTestReg.class.getCanonicalName() + ";\n"
            + "@RunTestReg(position=10,when=\"now\")\n"
            + "public class Test implements Runnable {\n"
            + "  public void run() { System.setProperty(\"executed\", \"true\"); }\n"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        assertTrue("Compiles OK",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, System.err)
            );
        
        URLClassLoader l = new URLClassLoader(new URL[] { getWorkDir().toURI().toURL() }, NamedServiceProcessorTest.class.getClassLoader());
        Lookup lkp = Lookups.metaInfServices(l, "META-INF/namedservices/runtest/now/below/");
        for (Runnable r : lkp.lookupAll(Runnable.class)) {
            r.run();
        }
        assertEquals("Our runnable was executed", "true", System.getProperty("executed"));
    }
    
    public void testNamedDefinitionWithArray() throws Exception {
        System.setProperty("executed", "false");
        String content = "import " + RunTestArray.class.getCanonicalName() + ";\n"
            + "@RunTestArray(position=10,array={\"now\", \"then\" })\n"
            + "public class Test implements Runnable {\n"
            + "  public void run() { System.setProperty(\"executed\", \"true\"); }\n"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        assertTrue("Compiles OK",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, System.err)
            );
        
        URLClassLoader l = new URLClassLoader(new URL[] { getWorkDir().toURI().toURL() }, NamedServiceProcessorTest.class.getClassLoader());
        Lookup lkp = Lookups.metaInfServices(l, "META-INF/namedservices/runtest/now/");
        for (Runnable r : lkp.lookupAll(Runnable.class)) {
            r.run();
        }
        assertEquals("Our runnable was executed", "true", System.getProperty("executed"));
        System.setProperty("executed", "false");
        Lookup lkp2 = Lookups.metaInfServices(l, "META-INF/namedservices/runtest/then/");
        for (Runnable r : lkp2.lookupAll(Runnable.class)) {
            r.run();
        }
        assertEquals("Our runnable was executed again", "true", System.getProperty("executed"));
    }
    
    public void testDoesNotImplementInterfaces() throws Exception {
        System.setProperty("executed", "false");
        String content = "import " + RunTestReg.class.getCanonicalName() + ";\n"
            + "@RunTestReg(position=10,when=\"now\")\n"
            + "public class Test {\n"
            + "  public void run() { System.setProperty(\"executed\", \"true\"); }\n"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse("Compilation fails",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os)
        );
        String err = new String(os.toByteArray(), StandardCharsets.UTF_8);
        if (err.indexOf("java.lang.Runnable") == -1) {
            fail("The error messages should say something about interface Runnable\n" + err);
        }
        if (err.indexOf("Callable") == -1) {
            fail("The error messages should say something about interface Callable\n" + err);
        }
    }

    public void testDoesImplementInterface() throws Exception {
        System.setProperty("executed", "false");
        String content = "import " + RunTestReg.class.getCanonicalName() + ";\n"
            + "import java.util.concurrent.Callable;\n"
            + "@RunTestReg(position=10,when=\"now\")\n"
            + "public class Test implements Callable<Boolean> {\n"
            + "  public Boolean call() { return true; }\n"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        assertTrue("Compilation succeeds",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, System.err)
        );
    }
    
    public void testMissingPathAttribute() throws Exception {
        String content = "import org.openide.util.lookup.NamedServiceDefinition;\n"
            + "@NamedServiceDefinition(path=\"runtest/@when()/below\",serviceType=Runnable.class)\n"
            + "public @interface Test {\n"
            + "  String noWhenAttributeHere();"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse("Compilation fails",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os)
        );
        String err = new String(os.toByteArray(), StandardCharsets.UTF_8);
        if (err.indexOf("@when()") == -1) {
            fail("The error messages should say something about missing @when\n" + err);
        }
    }

    public void testNonStringPathAttribute() throws Exception {
        String content = "import org.openide.util.lookup.NamedServiceDefinition;\n"
            + "@NamedServiceDefinition(path=\"runtest/@when()/below\",serviceType=Runnable.class)\n"
            + "public @interface Test {\n"
            + "  int when();"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse("Compilation fails",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os)
        );
        String err = new String(os.toByteArray(), StandardCharsets.UTF_8);
        if (err.indexOf("@when()") == -1) {
            fail("The error messages should say something about missing @when\n" + err);
        }
    }

    public void testNonExistentPositionAttribute() throws Exception {
        String content = "import org.openide.util.lookup.NamedServiceDefinition;\n"
            + "@NamedServiceDefinition(path=\"fixed\",serviceType=Runnable.class,position=\"where\")\n"
            + "public @interface Test {\n"
            + "  int when();"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse("Compilation fails",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os)
        );
        String err = new String(os.toByteArray(), StandardCharsets.UTF_8);
        if (err.indexOf("where") == -1) {
            fail("The error messages should say something about missing where\n" + err);
        }
    }
    public void testNonIntegerPositionAttribute() throws Exception {
        String content = "import org.openide.util.lookup.NamedServiceDefinition;\n"
            + "@NamedServiceDefinition(path=\"fixed\",serviceType=Runnable.class,position=\"where\")\n"
            + "public @interface Test {\n"
            + "  Class<?> where();"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse("Compilation fails",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os)
        );
        String err = new String(os.toByteArray(), StandardCharsets.UTF_8);
        if (err.indexOf("where") == -1) {
            fail("The error messages should say something about missing where\n" + err);
        }
    }
    public void testMissingRetention() throws Exception {
        String content = "import org.openide.util.lookup.NamedServiceDefinition;\n"
            + "@NamedServiceDefinition(path=\"fixed\",serviceType=Object.class)\n"
            + "@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)\n"
            + "public @interface Test {\n"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse("Compilation fails",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os)
        );
        String err = new String(os.toByteArray(), StandardCharsets.UTF_8);
        if (err.indexOf("specify @Retention") == -1) {
            fail("The error messages should say something about missing where\n" + err);
        }
        if (err.indexOf("specify @Target") != -1) {
            fail("Be silent about @Target\n" + err);
        }
    }
    public void testMissingTarget() throws Exception {
        String content = "import org.openide.util.lookup.NamedServiceDefinition;\n"
            + "@NamedServiceDefinition(path=\"fixed\",serviceType=Object.class)"
            + "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)\n"
            + "public @interface Test {\n"
            + "}\n";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Test", content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse("Compilation fails",
            AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os)
        );
        String err = new String(os.toByteArray(), StandardCharsets.UTF_8);
        if (err.indexOf("specify @Retention") != -1) {
            fail("Be silent about Retention\n" + err);
        }
        if (err.indexOf("specify @Target") == -1) {
            fail("The error messages should say something about missing where\n" + err);
        }
    }
    
    @NamedServiceDefinition(
        path="runtest/@when()/below",
        serviceType={ Runnable.class, Callable.class }
    )
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public static @interface RunTestReg {
        public int position();
        public String when();
    }
    @NamedServiceDefinition(
        path="runtest/@array()",
        serviceType={ Runnable.class, Callable.class }
    )
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public static @interface RunTestArray {
        public int position();
        public String[] array();
    }
}
