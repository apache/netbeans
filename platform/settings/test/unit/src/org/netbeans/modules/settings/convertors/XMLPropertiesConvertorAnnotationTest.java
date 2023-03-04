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

import java.io.*;

import java.util.Properties;
import org.netbeans.junit.NbTestCase;


import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.spi.settings.Convertor;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.test.AnnotationProcessorTestUtils;

/** Checks usage of annotation to assign XML properties convertor.
 *
 * @author Jaroslav Tulach
 */
public final class XMLPropertiesConvertorAnnotationTest extends NbTestCase {
    /** Creates a new instance of XMLPropertiesConvertorTest */
    public XMLPropertiesConvertorAnnotationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testReadWrite() throws Exception {
        FileObject dtdFO = Repository.getDefault().getDefaultFileSystem().
            findResource("/xml/lookups/NetBeans_org_netbeans_modules_settings_xtest/DTD_XML_FooSetting_2_0.instance");
        assertNotNull("Provider not found", dtdFO);
        Convertor c = XMLPropertiesConvertor.create(dtdFO);
        AnnoFoo foo = new AnnoFoo();
        foo.setProperty1("xxx");
        CharArrayWriter caw = new CharArrayWriter(1024);
        c.write(caw, foo);
        caw.flush();
        caw.close();
        CharArrayReader car = new CharArrayReader(caw.toCharArray());
        Object obj = c.read(car);
        assertEquals(foo, obj);
        assertEquals("HooFoo is the class", HooFoo.class, obj.getClass());
    }

    @ConvertAsProperties(
        dtd="-//NetBeans org.netbeans.modules.settings.xtest//DTD XML FooSetting 2.0//EN"
    )
    public static class AnnoFoo extends FooSetting {
        public Object readProperties(Properties p) {
            HooFoo n = new HooFoo();
            n.setProperty1(p.getProperty("p1"));
            return n;
        }
        public void writeProperties(Properties p) {
            p.setProperty("p1", this.getProperty1());
        }
    }
    public static class HooFoo extends AnnoFoo {
    }

    public void testVerifyHaveDefaultConstructor() throws Exception {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.y.Kuk",
            "import org.netbeans.api.settings.ConvertAsProperties;\n" +
            "@ConvertAsProperties(dtd=\"-//x.y//Kuk//EN\")\n" +
            "public class Kuk {\n" +
            "  public Kuk(int i) {}\n" +
            "}\n"
        );
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, err);
        assertFalse("Should fail", res);
        if (err.toString().indexOf("x.y.Kuk must have a no-argument constructor") == -1) {
            fail("Wrong error message:\n" + err.toString());
        }
    }
    public void testVerifyReadProperties() throws Exception {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.y.Kuk",
            "import org.netbeans.api.settings.ConvertAsProperties;\n" +
            "@ConvertAsProperties(dtd=\"-//x.y//Kuk//EN\")\n" +
            "public class Kuk {\n" +
            "  public Kuk() {}\n" +
            "  public void writeProperties(java.util.Properties p){}\n" +
            "}\n"
        );
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, err);
        assertFalse("Should fail", res);
        if (err.toString().indexOf("x.y.Kuk must have proper readProperties method") == -1) {
            fail("Wrong error message:\n" + err.toString());
        }
    }
    public void testVerifyWriteProperties() throws Exception {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.y.Kuk",
            "import org.netbeans.api.settings.ConvertAsProperties;\n" +
            "@ConvertAsProperties(dtd=\"-//x.y//Kuk//EN\")\n" +
            "public class Kuk {\n" +
            "  public Kuk() {}\n" +
            "  public void readProperties(java.util.Properties p){}\n" +
            "}\n"
        );
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, err);
        assertFalse("Should fail", res);
        if (err.toString().indexOf("x.y.Kuk must have proper writeProperties method") == -1) {
            fail("Wrong error message:\n" + err.toString());
        }
    }
    public void testVerifyWritePropertiesReturnsVoid() throws Exception {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.y.Kuk",
            "import org.netbeans.api.settings.ConvertAsProperties;\n" +
            "@ConvertAsProperties(dtd=\"-//x.y//Kuk//EN\")\n" +
            "public class Kuk {\n" +
            "  public Kuk() {}\n" +
            "  public void readProperties(java.util.Properties p){}\n" +
            "  public int writeProperties(java.util.Properties p){}\n" +
            "}\n"
        );
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, err);
        assertFalse("Should fail", res);
        if (err.toString().indexOf("x.y.Kuk must have proper writeProperties method") == -1) {
            fail("Wrong error message:\n" + err.toString());
        }
    }
}
