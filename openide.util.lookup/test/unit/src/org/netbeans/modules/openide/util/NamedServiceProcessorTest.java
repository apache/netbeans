/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.openide.util;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLClassLoader;
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
        String err = new String(os.toByteArray(), "UTF-8");
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
        String err = new String(os.toByteArray(), "UTF-8");
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
        String err = new String(os.toByteArray(), "UTF-8");
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
        String err = new String(os.toByteArray(), "UTF-8");
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
        String err = new String(os.toByteArray(), "UTF-8");
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
        String err = new String(os.toByteArray(), "UTF-8");
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
        String err = new String(os.toByteArray(), "UTF-8");
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
