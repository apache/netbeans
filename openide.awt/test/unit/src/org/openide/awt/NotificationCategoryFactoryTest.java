/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.openide.awt;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.List;
import junit.framework.Assert;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author jpeska
 */
public class NotificationCategoryFactoryTest extends NbTestCase {

    public static final String CATEGORY_NAME_A = "nb-notification-unittestA";
    public static final String CATEGORY_DISPLAY_NAME_A = "unitTestCategoryLabelA";
    public static final String CATEGORY_DESCRIPTION_A = "unitTestCategoryDescriptionA";
    public static final String CATEGORY_NAME_B = "nb-notification-unittestB";
    public static final String CATEGORY_DISPLAY_NAME_B = "unitTestCategoryLabelB";
    public static final String CATEGORY_DESCRIPTION_B = "unitTestCategoryDescriptionB";
    public static final String CATEGORY_NAME_C = "nb-notification-unittestC";
    public static final String CATEGORY_DISPLAY_NAME_C = "unitTestCategoryLabelC";
    public static final String CATEGORY_DESCRIPTION_C = "unitTestCategoryDescriptionC";

    static {
        String[] layers = new String[]{"org/openide/awt/mf-layer.xml"};//NOI18N
        IDEInitializer.setup(layers, new Object[0]);
    }

    public NotificationCategoryFactoryTest(String name) {
        super(name);
    }

    public void testGetCategory() {
        NotificationCategoryFactory factory = NotificationCategoryFactory.getInstance();

        List<? extends Category> categories = factory.getCategories();
        categories.removeAll(Category.getDefaultCategories());
        assertEquals(2, categories.size());

        Category cA = categories.get(0);
        assertEquals(CATEGORY_NAME_A, cA.getName());
        assertEquals(CATEGORY_DISPLAY_NAME_A, cA.getDisplayName());
        assertEquals(CATEGORY_DESCRIPTION_A, cA.getDescription());

        Category cB = categories.get(1);
        assertEquals(CATEGORY_NAME_B, cB.getName());
        assertEquals(CATEGORY_DISPLAY_NAME_B, cB.getDisplayName());
        assertEquals(CATEGORY_DESCRIPTION_B, cB.getDescription());

        assertFalse(cA.equals(cB));

        Category category = factory.getCategory(CATEGORY_NAME_A);
        assertNotNull(category);
        assertEquals(CATEGORY_NAME_A, category.getName());

        category = factory.getCategory(CATEGORY_NAME_B);
        assertNotNull(category);

        category = factory.getCategory("unknown category name");
        assertNull(category);

        try {
            factory.getCategory(null);
            fail("null category name is not acceptable");
        } catch (AssertionError e) {
            //expected
        }
    }

    public void testCreate() {
        Category category = NotificationCategoryFactory.create(CATEGORY_NAME_C,
                "org.openide.awt.TestBundle",
                "LBL_unittest_categoryC",
                "HINT_unittest_categoryC");

        assertNotNull(category);
        assertEquals(CATEGORY_NAME_C, category.getName());
        assertEquals(CATEGORY_DISPLAY_NAME_C, category.getDisplayName());
        assertEquals(CATEGORY_DESCRIPTION_C, category.getDescription());
    }

    public static class IDEInitializer extends ProxyLookup {

        public static IDEInitializer DEFAULT_LOOKUP = null;
        private static FileSystem lfs;

        static {
            IDEInitializer.class.getClassLoader().setDefaultAssertionStatus(true);
            System.setProperty("org.openide.util.Lookup", IDEInitializer.class.getName());
            Assert.assertEquals(IDEInitializer.class, Lookup.getDefault().getClass());
        }

        public IDEInitializer() {
            Assert.assertNull(DEFAULT_LOOKUP);
            DEFAULT_LOOKUP = this;
            URL.setURLStreamHandlerFactory(new MyURLHandlerFactory());
        }

        /**
         * Set the global default lookup with the specified content.
         *
         * @param layers xml-layer URLs to be present in the system filesystem.
         * @param instances object instances to be present in the default lookup.
         */
        public static void setup(
                String[] layers,
                Object[] instances) {
            ClassLoader classLoader = IDEInitializer.class.getClassLoader();
            File workDir = new File(Manager.getWorkDirPath());
            URL[] urls = new URL[layers.length];
            int i, k = urls.length;
            for (i = 0; i < k; i++) {
                urls[i] = classLoader.getResource(layers[i]);
            }

            // 1) create repository
            XMLFileSystem systemFS = new XMLFileSystem();
            lfs = FileUtil.createMemoryFileSystem();
            try {
                systemFS.setXmlUrls(urls);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            MyFileSystem myFileSystem = new MyFileSystem(
                    new FileSystem[]{lfs, systemFS});
            Repository repository = new Repository(myFileSystem);

            Object[] lookupContent = new Object[instances.length + 1];
            lookupContent[0] = repository;
            System.arraycopy(instances, 0, lookupContent, 1, instances.length);

            DEFAULT_LOOKUP.setLookups(new Lookup[]{
                Lookups.fixed(lookupContent),
                Lookups.metaInfServices(classLoader),
                Lookups.singleton(classLoader),});
            Assert.assertTrue(myFileSystem.isDefault());
        }

        public static void cleanWorkDir() {
            try {
                Enumeration en = lfs.getRoot().getChildren(false);
                while (en.hasMoreElements()) {
                    ((FileObject) en.nextElement()).delete();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private static class MyFileSystem extends MultiFileSystem {

            public MyFileSystem(FileSystem[] fileSystems) {
                super(fileSystems);
                try {
                    setSystemName("TestFS");
                } catch (PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private static class MyURLHandlerFactory implements URLStreamHandlerFactory {

            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.equals("nbfs")) {
                    return FileUtil.nbfsURLStreamHandler();
                }
                return null;
            }
        }
    }
}
