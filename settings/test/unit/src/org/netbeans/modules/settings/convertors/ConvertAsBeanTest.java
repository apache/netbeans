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

import org.netbeans.api.settings.ConvertAsJavaBean;
import org.netbeans.junit.NbTestCase;



import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.test.AnnotationProcessorTestUtils;

/** Checks usage of annotation to assign XML properties convertor.
 *
 * @author Jaroslav Tulach
 */
public final class ConvertAsBeanTest extends NbTestCase {
    /** Creates a new instance of XMLPropertiesConvertorTest */
    public ConvertAsBeanTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }
    
    public void testReadWrite() throws Exception {
        AnnoFoo foo = new AnnoFoo();
        foo.setName("xxx");

        DataFolder test = DataFolder.findFolder(FileUtil.getConfigRoot());
        DataObject obj = InstanceDataObject.create(test, null, foo, null);
        final FileObject pf = obj.getPrimaryFile();
        final String content = pf.asText();
        if (content.indexOf("<string>xxx</string>") == -1) {
            fail(content);
        }
        obj.setValid(false);
        DataObject newObj = DataObject.find(pf);
        if (newObj == obj) {
            fail("Strange, objects shall differ");
        }
        InstanceCookie ic = newObj.getLookup().lookup(InstanceCookie.class);
        assertNotNull("Instance cookie found", ic);

        Object read = ic.instanceCreate();
        assertNotNull("Instance created", read);
        assertEquals("Correct class", AnnoFoo.class, read.getClass());
        AnnoFoo readFoo = (AnnoFoo)read;
        assertEquals("property changed", "xxx", readFoo.getName());
    }

    public void testReadWriteOnSubclass() throws Exception {
        HooFoo foo = new HooFoo();
        foo.setName("xxx");

        DataFolder test = DataFolder.findFolder(FileUtil.getConfigRoot());
        DataObject obj = InstanceDataObject.create(test, null, foo, null);
        final FileObject pf = obj.getPrimaryFile();
        final String content = pf.asText();
        if (content.indexOf("<string>xxx</string>") == -1) {
            fail(content);
        }
        obj.setValid(false);
        DataObject newObj = DataObject.find(pf);
        if (newObj == obj) {
            fail("Strange, objects shall differ");
        }
        InstanceCookie ic = newObj.getLookup().lookup(InstanceCookie.class);
        assertNotNull("Instance cookie found", ic);

        Object read = ic.instanceCreate();
        assertNotNull("Instance created", read);
        assertEquals("Correct class", HooFoo.class, read.getClass());
        HooFoo readFoo = (HooFoo)read;
        assertEquals("property changed", "xxx", readFoo.getName());
    }

    @ConvertAsJavaBean(
    )
    public static class AnnoFoo extends Object {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    } // end of AnnoFoo

    public static class HooFoo extends AnnoFoo {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    } // end of HooFoo

    @ConvertAsJavaBean(
        subclasses=false
    )
    public static class JuuFoo extends Object {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    } // end of JuuFoo

    public static class NotFoo extends JuuFoo {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    } // end of NotFoo
    public void testReadWriteNotForSubclasses() throws Exception {
        NotFoo foo = new NotFoo();
        foo.setName("xxx");

        DataFolder test = DataFolder.findFolder(FileUtil.getConfigRoot());
        try {
            DataObject obj = InstanceDataObject.create(test, null, foo, null);
            final FileObject pf = obj.getPrimaryFile();
            final String content = pf.asText();
            fail(content);
        } catch (NotSerializableException ex) {
            // OK
        }
    }

    public void testVerifyHaveDefaultConstructor() throws Exception {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.y.Kuk",
            "import org.netbeans.api.settings.ConvertAsJavaBean;\n" +
            "@ConvertAsJavaBean()\n" +
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
    public void testInterfacesCannotBeAnnotated() throws Exception {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.y.Kuk",
            "import org.netbeans.api.settings.ConvertAsJavaBean;\n" +
            "@ConvertAsJavaBean()\n" +
            "public interface Kuk {\n" +
            "  public int buk(int i);\n" +
            "}\n"
        );
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, err);
        assertFalse("Should fail", res);
        if (err.toString().indexOf("is not loadable") == -1) {
            fail("Wrong error message:\n" + err.toString());
        }
    }
}
