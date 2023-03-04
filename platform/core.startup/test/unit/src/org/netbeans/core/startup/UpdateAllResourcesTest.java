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
package org.netbeans.core.startup;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.netbeans.JarClassLoader;
import org.netbeans.Stamps;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UpdateAllResourcesTest extends NbTestCase{
    public UpdateAllResourcesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        System.getProperties().remove("org.netbeans.core.update.all.resources");
        resetStamps();
    }

    public void testByDefaultTheArchiveIsUpdated() {
        assertTrue("Update was scheduled", Main.updateAllResources());
    }

    public void testNeverUpdate() {
        System.setProperty("org.netbeans.core.update.all.resources", "never");
        assertFalse("Update was not", Main.updateAllResources());
    }

    public void testUpdateAsNotPopulated() throws Exception {
        System.setProperty("org.netbeans.core.update.all.resources", "missing");
        populateCache(false);
        assertFalse("No previous all-resources.dat", JarClassLoader.isArchivePopulated());
        assertTrue("Performs the update", Main.updateAllResources());
    }

    public void testDontUpdateWhenPopulated() throws Exception {
        System.setProperty("org.netbeans.core.update.all.resources", "missing");
        populateCache(true);
        assertFalse("No need to update, everything is populated", Main.updateAllResources());
    }

    private static void populateCache(boolean prep) throws Exception {
        Method init = JarClassLoader.class.getDeclaredMethod("initializeCache");
        init.setAccessible(true);
        init.invoke(null);

        Field fld = JarClassLoader.class.getDeclaredField("archive");
        fld.setAccessible(true);
        Object obj = fld.get(null);
        assertNotNull("Archive is initialized", obj);
        
        Constructor<? extends Object> cnstr = obj.getClass().getDeclaredConstructor(boolean.class);
        cnstr.setAccessible(true);
        fld.set(null, cnstr.newInstance(prep));
        
        assertEquals("Previous all-resources.dat", prep, JarClassLoader.isArchivePopulated());
    }
    private static void resetStamps() throws Exception {
        final Method m = Stamps.class.getDeclaredMethod("main", String[].class);
        m.setAccessible(true);
        m.invoke(null, (Object) new String[]{"reset"});
    }
}
