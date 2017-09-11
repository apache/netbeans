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
package org.netbeans.modules.bugtracking.tasks.cache;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jpeska
 */
public class DashboardStorageTest extends NbTestCase {

    public DashboardStorageTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        emptyStorage();
    }

    public void testClosedEntriesStorage() throws Exception {
        DashboardStorage storage = DashboardStorage.getInstance();
        List<String> categoryNames = new ArrayList<String>();
        categoryNames.add("Dummy1");
        categoryNames.add("Dummy2");
        categoryNames.add("Dummy3");
        storage.storeClosedCategories(categoryNames);

        List<String> readClosedCategories = storage.readClosedCategories();
        assertEquals(3, readClosedCategories.size());
        assertTrue(readClosedCategories.contains("Dummy1"));
        assertTrue(readClosedCategories.contains("Dummy2"));
        assertTrue(readClosedCategories.contains("Dummy3"));

        categoryNames = Collections.emptyList();
        storage.storeClosedCategories(categoryNames);

        readClosedCategories = storage.readClosedCategories();
        assertEquals(0, readClosedCategories.size());

        List<String> repositoryIds = new ArrayList<String>();
        repositoryIds.add("Dummy1");
        repositoryIds.add("Dummy2");
        repositoryIds.add("Dummy3");
        storage.storeClosedRepositories(repositoryIds);

        List<String> readClosedRepositories = storage.readClosedRepositories();
        assertEquals(3, readClosedRepositories.size());
        assertTrue(readClosedRepositories.contains("Dummy1"));
        assertTrue(readClosedRepositories.contains("Dummy2"));
        assertTrue(readClosedRepositories.contains("Dummy3"));
    }

    public void testCategoryStorage() throws Exception {
        DashboardStorage storage = DashboardStorage.getInstance();
        List<TaskEntry> tasks = new ArrayList<TaskEntry>();
        tasks.add(new TaskEntry("issue1", "repo1"));
        tasks.add(new TaskEntry("issue2", "repo1"));
        tasks.add(new TaskEntry("issue3", "repo2"));
        String categoryName1 = "category1";

        storage.storeCategory(categoryName1, tasks);

        List<CategoryEntry> readCategories = storage.readCategories();
        assertEquals(1, readCategories.size());
        CategoryEntry category = readCategories.get(0);
        assertTrue(category.getTaskEntries().contains(new TaskEntry("issue1", "repo1")));
        assertTrue(category.getTaskEntries().contains(new TaskEntry("issue2", "repo1")));
        assertTrue(category.getTaskEntries().contains(new TaskEntry("issue3", "repo2")));

        String categoryName2 = "category2";
        storage.storeCategory(categoryName2, tasks);

        readCategories = storage.readCategories();
        assertEquals(2, readCategories.size());

        String categoryName2Rename = "category2rename";
        storage.renameCategory(categoryName2, categoryName2Rename);
        assertNull(storage.readCategory(categoryName2));
        assertNotNull(storage.readCategory(categoryName2Rename));
        readCategories = storage.readCategories();
        assertEquals(2, readCategories.size());

        assertFalse(storage.deleteCategory(categoryName2));
        assertTrue(storage.deleteCategory(categoryName2Rename));

        readCategories = storage.readCategories();
        assertEquals(1, readCategories.size());
    }

    private void emptyStorage() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        File f = getStorageRootFile();
        StorageUtils.deleteRecursively(f);
        Field field = DashboardStorage.class.getDeclaredField("storageFolder");
        field.setAccessible(true);
        field.set(DashboardStorage.getInstance(), f);
    }

    private File getStorageRootFile() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        DashboardStorage storage = DashboardStorage.getInstance();
        Method m = storage.getClass().getDeclaredMethod("getStorageRootFile");
        m.setAccessible(true);
        return (File) m.invoke(storage, new Object[0]);
    }
}
