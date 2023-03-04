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
package org.netbeans.modules.bugtracking.tasks.cache;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
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
