/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
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
