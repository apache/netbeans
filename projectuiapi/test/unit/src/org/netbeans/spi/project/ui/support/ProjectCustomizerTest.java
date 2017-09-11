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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.spi.project.ui.support;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.DelegateCategoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * @author Jan Lahoda
 */
public class ProjectCustomizerTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ProjectCustomizerTest.class);
    }

    public ProjectCustomizerTest(String testName) {
        super(testName);
    }

    public void testCategoriesAreReclaimable() throws Exception {
        if (Utilities.isMac()) { //#238765 apparently something is different on mac and the setup of the test is not  correct. I could not find what that is, so just disabled the test
            return;
        }
        final Reference<?>[] refs = new Reference<?>[4];
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Category test1 = Category.create("test1", "test1", null);
                Category test2 = Category.create("test2", "test3", null, test1);
                Category test3 = Category.create("test3", "test3", null);
                refs[1] = new WeakReference<Object>(test1);
                refs[2] = new WeakReference<Object>(test2);
                refs[3] = new WeakReference<Object>(test3);
                Dialog d = ProjectCustomizer.createCustomizerDialog(new Category[] {test2, test3}, new CategoryComponentProvider() {
                    public JComponent create(Category category) {
                        return new JPanel();
                    }
                }, null, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //ignore
                    }
                }, HelpCtx.DEFAULT_HELP);
                d.dispose();
                refs[0] = new WeakReference<Object>(d);
            }
        });
        
        for (Reference<?> ref : refs) {
            assertGC("Is reclaimable", ref);
        }
    }
    
    public void testReadCategories() throws Exception {
        FileObject customizerFO = FileUtil.createFolder(FileUtil.getConfigRoot(), "Projects/test/Customizer");
        // - One         | one
        // + Category #1 | two
        //   - Three     | three
        // + Category #2 |
        //   - Four      | four
        DelegateCategoryProvider dcp = new DelegateCategoryProvider(DataFolder.findFolder(customizerFO), null);
        Category categories[] = dcp.readCategories(DataFolder.findFolder(customizerFO));
        assertNotNull(categories);
        assertEquals(3, categories.length);
        assertEquals("one", categories[0].getName());
        assertEquals("One", categories[0].getDisplayName());
        assertEquals("one", dcp.create(categories[0]).getName());
        assertEquals("Category1", categories[1].getName());
        assertEquals("Category #1", categories[1].getDisplayName());
        assertEquals("two", dcp.create(categories[1]).getName());
        Category[] subcategories = categories[1].getSubcategories();
        assertEquals(1, subcategories.length);
        assertEquals("three", subcategories[0].getName());
        assertEquals("Three", subcategories[0].getDisplayName());
        assertEquals("three", dcp.create(subcategories[0]).getName());
        assertEquals("Category2", categories[2].getName());
        assertEquals("Category #2", categories[2].getDisplayName());
        assertEquals(null, dcp.create(categories[2]).getName());
        subcategories = categories[2].getSubcategories();
        assertEquals(1, subcategories.length);
        assertEquals("four", subcategories[0].getName());
        assertEquals("Four", subcategories[0].getDisplayName());
        assertEquals("four", dcp.create(subcategories[0]).getName());
    }
    private static abstract class TestCCP implements CompositeCategoryProvider {
        final String name;
        TestCCP(String name) {
            this.name = name;
        }
        public JComponent createComponent(Category category, Lookup context) {
            JComponent c = new JPanel();
            c.setName(name);
            return c;
        }
    }
    @CompositeCategoryProvider.Registration(
        projectType="test",
        position=100)
    public static class TestCCP1 extends TestCCP {
        public TestCCP1() {
            super("one");
        }
        public Category createCategory(Lookup context) {
            return Category.create("one", "One", null);
        }
    }
    @CompositeCategoryProvider.Registration(
        projectType="test",
        category="Category1",
        categoryLabel="Category #1",
        position=200)
    public static class TestCCP2 extends TestCCP {
        public TestCCP2() {
            super("two");
        }
        public Category createCategory(Lookup context) {
            throw new AssertionError("Self");
        }
    }
    @CompositeCategoryProvider.Registration(
        projectType="test",
        category="Category1",
        position=100)
    public static class TestCCP3 extends TestCCP {
        public TestCCP3() {
            super("three");
        }
        public Category createCategory(Lookup context) {
            return Category.create("three", "Three", null);
        }
    }
    @CompositeCategoryProvider.Registration(
        projectType="test",
        category="Category2",
        position=100)
    public static class TestCCP4 extends TestCCP {
        public TestCCP4() {
            super("four");
        }
        public Category createCategory(Lookup context) {
            return Category.create("four", "Four", null);
        }
    }
    
}
