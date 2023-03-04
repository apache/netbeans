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
